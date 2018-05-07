package com.danjb.otherdom.client.scene;

import java.nio.FloatBuffer;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.danjb.otherdom.client.MousePicker;
import com.danjb.otherdom.client.Texture;
import com.danjb.otherdom.client.Vertex;
import com.danjb.otherdom.client.render.Shaders;

/**
 * A section of terrain, composed of a grid of tiles.
 * 
 * The vertices are created automatically from a grid of height values.
 * 
 * Here is an example of how the vertices would be defined for a TerrainSection
 * where NUM_TILES_PER_ROW = 4:
 *
 *             0     1     2     3
 *             |     |     |     |
 *          0-----1-----2-----3-----4
 *          |  \  |   / |  \  |   / |
 *     0 -- |   \ |  /  |   \ |  /  |
 *          5-----6-----7-----8-----9
 *          |   / |  \  |   / |  \  |
 *     1 -- |  /  |   \ |  /  |   \ |
 *         10----11----12----13----14
 *          |  \  |   / |  \  |   / |
 *     2 -- |   \ |  /  |   \ |  /  |
 *         15----16----17----18----19
 *          |   / |  \  |   / |  \  |
 *     3 -- |  /  |   \ |  /  |   \ |
 *         20----21----22----23----24
 *
 * We alternate the directions of the diagonals to make the terrain appear
 * more natural; otherwise the triangular structure becomes quite obvious.
 */
public class TerrainSection extends WorldModel {

    /*
     * Tile IDs.
     * Currently, these correspond only to the index of each tile within the
     * terrain texture.
     */
    public static final int TILE_WATER = 0;
    public static final int TILE_GRASS = 1;

    private static final Texture TEXTURE = new Texture("terrain.png", false);
    
    /**
     * The width of the terrain texture, in pixels.
     */
    private static final int TEXTURE_WIDTH = 512;

    /**
     * The height of the terrain texture, in pixels.
     */
    private static final int TEXTURE_HEIGHT = 512;

    /**
     * The number of columns in the terrain texture.
     */
    private static final int TEXTURE_TILES_X = 8;

    /**
     * The number of rows in the terrain texture.
     */
    private static final int TEXTURE_TILES_Y = 8;

    /**
     * The width of one tile of the terrain texture, in pixels.
     */
    private static final int TEXTURE_TILE_WIDTH = 
            TEXTURE_WIDTH / TEXTURE_TILES_X;

    /**
     * The height of one tile of the terrain texture, in pixels.
     */
    private static final int TEXTURE_TILE_HEIGHT = 
            TEXTURE_HEIGHT / TEXTURE_TILES_Y;

    /**
     * The number of tiles in one row / column of the TerrainSection.
     */
    public static final int NUM_TILES_PER_ROW = 32;

    /**
     * The width / length of one tile, in metres.
     */
    public static final float TILE_WIDTH = 1;

    /**
     * The width / length of the TerrainSection, in metres.
     */
    public static final float WIDTH = NUM_TILES_PER_ROW * TILE_WIDTH;

    /**
     * Number of vertices per "row" of the TerrainSection.
     */
    private static final int NUM_VERTICES_PER_ROW = NUM_TILES_PER_ROW + 1;

    /**
     * Total number of vertices in the VAO for one TerrainSection.
     * As far as OpenGL is concerned, each tile actually has 6 vertices: 
     * 2 triangles with 3 vertices each. This means there is some duplication
     * of vertices when we pass data to the GPU. This could be avoided using
     * an index buffer, but it adds significant complexity for not much gain.
     */
    public static final int NUM_VERTICES = 
            6 * NUM_TILES_PER_ROW * NUM_VERTICES_PER_ROW;

    /**
     * The lowest possible vertex height, in metres.
     * 0 is considered to be sea level.
     */
    public static final float MIN_HEIGHT = -0.5f;
    
    /**
     * The highest possible vertex height, in metres.
     */
    public static final float MAX_HEIGHT = 0.5f;

    /**
     * Buffer used to store vertex positions (x, y, z).
     */
    private FloatBuffer vertexBuffer = 
            BufferUtils.createFloatBuffer(NUM_VERTICES * 3);
    
    /**
     * Buffer used to store vertex normals (x, y, z).
     */
    private FloatBuffer normalBuffer = 
            BufferUtils.createFloatBuffer(NUM_VERTICES * 3);
    
    /**
     * Buffer used to store vertex material ambient colours (r, g, b).
     */
    private FloatBuffer ambientColourBuffer = 
            BufferUtils.createFloatBuffer(NUM_VERTICES * 3);

    /**
     * Buffer used to store vertex material diffuse colours (r, g, b).
     */
    private FloatBuffer diffuseColourBuffer = 
            BufferUtils.createFloatBuffer(NUM_VERTICES * 3);

    /**
     * Buffer used to store vertex texture co-ordinates (s, t).
     */
    private FloatBuffer texCoordBuffer = 
            BufferUtils.createFloatBuffer(NUM_VERTICES * 2);

    /**
     * Buffer used to store selection codes (r, g, b).
     */
    private FloatBuffer selectionCodeBuffer = 
            BufferUtils.createFloatBuffer(NUM_VERTICES * 3);

    /**
     * The IDs of each tile in this TerrainSection.
     */
    private int[][] tileIds = 
            new int[NUM_TILES_PER_ROW][NUM_TILES_PER_ROW];
    
    /**
     * The vertices that make up this TerrainSection.
     * 
     * Note that we define each vertex only once, but when filling buffers to
     * send to the GPU, we write each vertex multiple times, once for every
     * adjacent tile. Since we are essentially sharing vertex objects between
     * multiple tiles, this means that any properties that vary on a per-tile 
     * basis, e.g. textures, cannot be defined in the vertex.
     */
    private Vertex[][] vertices = 
            new Vertex[NUM_VERTICES_PER_ROW][NUM_VERTICES_PER_ROW];
    
    /**
     * ID of the VBO that holds the position of each vertex.
     */
    private int vboIdPositions;

    /**
     * ID of the VBO that holds the normal vector of each vertex.
     */
    private int vboIdNormals;

    /**
     * ID of the VBO that holds the ambient colour of each vertex.
     */
    private int vboIdAmbientColours;

    /**
     * ID of the VBO that holds the diffuse colour of each vertex.
     */
    private int vboIdDiffuseColours;

    /**
     * ID of the VBO that holds the texture co-ordinates of each vertex.
     */
    private int vboIdTexCoords;

    /**
     * ID of the VBO that holds the selection codes of each vertex.
     */
    private int vboIdSelectionCodes;

    /**
     * The co-ordinates of this TerrainSection within the terrain grid.
     */
    private int sectionX, sectionZ;

    /**
     * Creates a new TerrainSection at the given section co-ordinates.
     * 
     * @param sectionX
     * @param sectionZ
     */
    public TerrainSection(int sectionX, int sectionZ){
        super(new Vector3f(sectionX * WIDTH, 0, sectionZ * WIDTH), TEXTURE);
        
        this.sectionX = sectionX;
        this.sectionZ = sectionZ;

        // Create tiles / vertices
        for (int tileZ = 0; tileZ < NUM_TILES_PER_ROW; tileZ++){
            for (int tileX = 0; tileX < NUM_TILES_PER_ROW; tileX++){
                if (sectionX == 0 && sectionZ == 0){
                    tileIds[tileX][tileZ] = TILE_GRASS;
                } else {
                    tileIds[tileX][tileZ] = TILE_WATER;
                }
                createVertex(tileX, tileZ, 0, 0);
                createVertex(tileX, tileZ, 1, 0);
                createVertex(tileX, tileZ, 0, 1);
                createVertex(tileX, tileZ, 1, 1);
            }
        }
        
        // The normals can only be calculated once all vertices have been created
        recalculateVertexNormals();
        
        // Put vertex data into buffers
        for (int tileZ = 0; tileZ < NUM_TILES_PER_ROW; tileZ++){
            boolean flipDiagonal = (tileZ % NUM_TILES_PER_ROW == 1);
            for (int tileX = 0; tileX < NUM_TILES_PER_ROW; tileX++){
                addTileToBuffers(tileX, tileZ, flipDiagonal);
                // Alternate diagonals
                flipDiagonal = !flipDiagonal;
            }
        }

        // Prepare the buffers for reading
        vertexBuffer.flip();
        ambientColourBuffer.flip();
        diffuseColourBuffer.flip();
        normalBuffer.flip();
        texCoordBuffer.flip();
        selectionCodeBuffer.flip();

        // Create VAO and select (bind to) it
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Create VBO and fill it with vertex positions
        vboIdPositions = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIdPositions);
        GL15.glBufferData(
                GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(Shaders.ATTR_VERTEX,
                3, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Deselect

        // Create VBO and fill it with vertex normals
        vboIdNormals = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIdNormals);
        GL15.glBufferData(
                GL15.GL_ARRAY_BUFFER, normalBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(Shaders.ATTR_VERTEX_NORMAL,
                3, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Deselect

        // Create VBO and fill it with vertex colours (ambient)
        vboIdAmbientColours = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIdAmbientColours);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, ambientColourBuffer,
                GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(
                Shaders.ATTR_MATERIAL_AMBIENT_COLOUR,
                3, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Deselect

        // Create VBO and fill it with vertex colours (diffuse)
        vboIdDiffuseColours = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIdDiffuseColours);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, diffuseColourBuffer,
                GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(
                Shaders.ATTR_MATERIAL_DIFFUSE_COLOUR,
                3, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Deselect

        // Create VBO and fill it with vertex texture co-ordinates
        vboIdTexCoords = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIdTexCoords);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texCoordBuffer,
                GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(Shaders.ATTR_TEXTURE_COORDS,
                2, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Deselect

        // Create VBO and fill it with vertex selection codes
        vboIdSelectionCodes = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIdSelectionCodes);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, selectionCodeBuffer,
                GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(Shaders.ATTR_SELECTION_CODE,
                3, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Deselect

        // Deselect the VAO once we're done setting vertex attribute data
        GL30.glBindVertexArray(0);
    }

    /**
     * Creates the vertex at the given co-ordinates, if it does not already exist.
     * 
     * @param tileX
     * @param tileZ
     * @param offsetX 0 = top of tile, 1 = bottom of tile
     * @param offsetZ 0 = top of tile, 1 = bottom of tile
     */
    private void createVertex(int tileX, int tileZ, int offsetX, int offsetZ) {
        
        int vx = tileX + offsetX;
        int vz = tileZ + offsetZ;
        Vertex v = vertices[vx][vz];
        
        if (v != null){
            // Vertex already exists
            return;
        }

        // Create new vertex
        Vector3f pos = new Vector3f(
                vx * TILE_WIDTH,
                0,
                vz * TILE_WIDTH);
        // For now our terrain doesn't have any special lighting properties
        Vector3f ambientColour = new Vector3f(1.0f, 1.0f, 1.0f);
        Vector3f diffuseColour = new Vector3f(1.0f, 1.0f, 1.0f);
        v = new Vertex(pos, ambientColour, diffuseColour);
        vertices[vx][vz] = v;
    }

    /**
     * Populates the various buffers for the tile at the given co-ordinates.
     * 
     * @param tileX
     * @param tileZ
     * @param flipDiagonal
     */
    private void addTileToBuffers(int tileX, int tileZ, boolean flipDiagonal) {

        int id = tileIds[tileX][tileZ];
        
        /*
         * Calculate the texture co-ordinates (relative to the texture size)
         */
        
        // Calculate the co-ordinates of the desired tile within the texture
        int texTileX = id % TEXTURE_TILES_X;
        int texTileY = id / TEXTURE_TILES_X;
        
        // Calculate the pixel co-ordinates of the tile within the texture,
        // add or subtract 0.5 (half-pixel correction), and divide by the 
        // texture size to get the relative texture co-ordinates.
        float texCoordX1 = (texTileX * TEXTURE_TILE_WIDTH + 0.5f) / TEXTURE_WIDTH;
        float texCoordY1 = (texTileY * TEXTURE_TILE_HEIGHT + 0.5f) / TEXTURE_HEIGHT;
        float texCoordX2 = ((texTileX + 1) * TEXTURE_TILE_WIDTH  - 0.5f) / TEXTURE_WIDTH;
        float texCoordY2 = ((texTileY + 1) * TEXTURE_TILE_HEIGHT - 0.5f) / TEXTURE_HEIGHT;
        
        Vertex topLeft     = vertices[tileX][tileZ];
        Vertex topRight    = vertices[tileX + 1][tileZ];
        Vertex bottomLeft  = vertices[tileX][tileZ + 1];
        Vertex bottomRight = vertices[tileX + 1][tileZ + 1];
        
        /*
         * We have to take care to add the vertices in a counter-
         * clockwise order so that the terrain is facing the right way.
         */
        
        if (flipDiagonal){
            // Bottom-left triangle
            addVertex(topLeft, tileX, tileZ, texCoordX1, texCoordY1);
            addVertex(bottomLeft, tileX, tileZ, texCoordX1, texCoordY2);
            addVertex(bottomRight, tileX, tileZ, texCoordX2, texCoordY2);
            
            // Top-right triangle
            addVertex(bottomRight, tileX, tileZ, texCoordX2, texCoordY2);
            addVertex(topRight, tileX, tileZ, texCoordX2, texCoordY1);
            addVertex(topLeft, tileX, tileZ, texCoordX1, texCoordY1);
        } else {
            // Top-left triangle
            addVertex(topLeft, tileX, tileZ, texCoordX1, texCoordY1);
            addVertex(bottomLeft, tileX, tileZ, texCoordX1, texCoordY2);
            addVertex(topRight, tileX, tileZ, texCoordX2, texCoordY1);

            // Bottom-right triangle
            addVertex(topRight, tileX, tileZ, texCoordX2, texCoordY1);
            addVertex(bottomLeft, tileX, tileZ, texCoordX1, texCoordY2);
            addVertex(bottomRight, tileX, tileZ, texCoordX2, texCoordY2);
        }
    }

    /**
     * Adds the properties of the given Vertex to the appropriate buffers.
     * 
     * The texture co-ordinates are supplied as separate parameters as the
     * texture is a property of the tile rather than the vertex.
     * 
     * @param v
     * @param tileX
     * @param tileZ
     * @param texCoordX
     * @param texCoordY
     */
    private void addVertex(Vertex v, float tileX, float tileZ, 
            float texCoordX, float texCoordY) {

        Vector3f pos = v.getPos();
        vertexBuffer.put(pos.x);
        vertexBuffer.put(pos.y);
        vertexBuffer.put(pos.z);

        Vector3f normal = v.getNormal();
        normalBuffer.put(normal.x);
        normalBuffer.put(normal.y);
        normalBuffer.put(normal.z);

        Vector3f ambientColour = v.getAmbientColour();
        ambientColourBuffer.put(ambientColour.x);
        ambientColourBuffer.put(ambientColour.y);
        ambientColourBuffer.put(ambientColour.z);

        Vector3f diffuseColour = v.getDiffuseColour();
        diffuseColourBuffer.put(diffuseColour.x);
        diffuseColourBuffer.put(diffuseColour.y);
        diffuseColourBuffer.put(diffuseColour.z);
        
        texCoordBuffer.put(texCoordX);
        texCoordBuffer.put(texCoordY);

        // We divide by 255 because our shader expects our colours to be
        // floats in the range 0-1.
        selectionCodeBuffer.put(tileX / 255.0f);
        selectionCodeBuffer.put(tileZ / 255.0f);
        selectionCodeBuffer.put(0); // Unused
    }

    /**
     * Calculates the normal vectors at each vertex in the TerrainSection.
     * 
     * See:
     * http://www.lighthouse3d.com/opengl/terrain/index.php?normals
     */
    private void recalculateVertexNormals(){
        
        // First calculate the normals of each tile
        Vector3f[][] tileNormals = 
                new Vector3f[NUM_TILES_PER_ROW][NUM_TILES_PER_ROW];
        for (int z = 0; z < NUM_TILES_PER_ROW; z++){
            for (int x = 0; x < NUM_TILES_PER_ROW; x++){
                tileNormals[x][z] = calculateTileNormal(x, z);
            }
        }
        
        // Now we can calculate the vertex normals from the tile normals
        for (int z = 0; z < NUM_VERTICES_PER_ROW; z++){
            for (int x = 0; x < NUM_VERTICES_PER_ROW; x++){
                Vector3f normal = calculateVertexNormal(x, z, tileNormals);
                vertices[x][z].setNormal(normal);
            }
        }
    }
    
    /**
     * Calculates the normal vector of the tile at the given co-ordinates.
     * 
     * @param x
     * @param z
     * @return
     */
    private Vector3f calculateTileNormal(int x, int z) {
        /*
         * We calculate the normal from the cross-product of 2 vectors that
         * are coplanar to the face.
         * 
         * Tile (x, z) is bound by the 4 vertices at positions:
         *     (x, z,
         *      x + 1, z,
         *      x, z + 1,
         *      x + 1, z + 1)
         * 
         * To find our coplanar vectors, we just need to find 2 vectors that
         * link any 2 of these vertices.
         */
        Vector3f vert1 = vertices[x][z].getPos();
        Vector3f vert2 = vertices[x + 1][z].getPos();
        Vector3f vert3 = vertices[x][z + 1].getPos();
        
        Vector3f coplanar1 = new Vector3f();
        vert1.sub(vert2, coplanar1);
        
        Vector3f coplanar2 = new Vector3f();
        vert2.sub(vert3, coplanar2);
        
        Vector3f normal = new Vector3f();
        coplanar2.cross(coplanar1, normal);
        
        return normal.normalize();
    }

    /**
     * Calculates the normal vector of the vertex at the given co-ordinates.
     * 
     * @param x
     * @param z
     * @param tileNormals
     * @return
     */
    private Vector3f calculateVertexNormal(int x, int z,
            Vector3f[][] tileNormals) {
        /*
         * The normal of each vertex is the normalised sum of the normals of the
         * 4 surrounding tiles.
         * 
         * Vertex (x, z) is surrounded by the 4 tiles at positions:
         *  (x - 1, z - 1,
         *   x, z - 1,
         *   x - 1, z,
         *   x, z)
         */
        try {
            Vector3f normal1 = tileNormals[x - 1][z - 1];
            Vector3f normal2 = tileNormals[x][z - 1];
            Vector3f normal3 = tileNormals[x - 1][z];
            Vector3f normal4 = tileNormals[x][z];
            return normal1
                    .add(normal2)
                    .add(normal3)
                    .add(normal4)
                    .normalize();
        } catch (ArrayIndexOutOfBoundsException ex){
            // If a vertex is at the edge of a TerrainSection, we assume (for 
            // now) that the normal points straight up.
            return new Vector3f(0, 1, 0);
        }
    }

    /**
     * Creates a unique selection code to identify this TerrainSection.
     * 
     * See MousePicker for more details!
     * 
     * @param playerSectionX The co-ordinates of the player's current section.
     * @param playerSectionZ The co-ordinates of the player's current section.
     * @return
     */
    public Vector3f createSelectionCode(int playerSectionX, int playerSectionZ) {

        /*
         * The section code is only 3 bytes long. What we really want to
         * encode in these bytes is this:
         * 
         * 1) Terrain identifier (constant)
         * 2) Section X
         * 3) Section Y
         * 
         * However, this would only allow up to 255 clickable sections in
         * each axis. Therefore we instead use a value relative to the
         * player's current section co-ordinates.
         * 
         * We add 128 to ensure that the value is always positive, as
         * negative numbers seem to be problematic (OpenGL reads the values
         * as unsigned bytes). We then subtract this same value later.
         */
        int offsetX = (byte) (sectionX - playerSectionX) + 128;
        int offsetZ = (byte) (sectionZ - playerSectionZ) + 128;
        
        // We divide by 255 because our shader expects our colours to be
        // floats in the range 0-1.
        return new Vector3f(
                MousePicker.CODE_TERRAIN / 255.0f,
                offsetX / 255.0f,
                offsetZ / 255.0f);
    }
 
    /**
     * Changes the tile at the given co-ordinates.
     * @param tileX
     * @param tileZ
     * @param tileId
     */
    public void setTile(int tileX, int tileZ, int tileId){
        int currentTileId = tileIds[tileX][tileZ];
        if (currentTileId == tileId){
            // Nothing to do
            return;
        }
        tileIds[tileX][tileZ] = tileId;
        textureChanged();
    }

    /**
     * Updates the texture co-ordinate buffer when a tile changes texture.
     */
    private void textureChanged() {

        // Recreate texture co-ordinate buffer
        for (int tileZ = 0; tileZ < NUM_TILES_PER_ROW; tileZ++){
            boolean flipDiagonal = (tileZ % NUM_TILES_PER_ROW == 1);
            for (int tileX = 0; tileX < NUM_TILES_PER_ROW; tileX++){
                addTileToTexCoordBuffer(tileX, tileZ, flipDiagonal);
                // Alternate diagonals
                flipDiagonal = !flipDiagonal;
            }
        }
        
        // Prepare buffer for reading
        texCoordBuffer.flip();

        // Send new data to GPU
        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIdTexCoords);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texCoordBuffer,
                GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Deselect
        GL30.glBindVertexArray(0); // Deselect
    }

    /**
     * Populates the texture co-ordinate buffer for the tile at the given co-ordinates.
     * @param tileX
     * @param tileZ
     * @param flipDiagonal
     */
    private void addTileToTexCoordBuffer(int tileX, int tileZ, boolean flipDiagonal) {

        int id = tileIds[tileX][tileZ];

        /*
         * Calculate the texture co-ordinates (relative to the texture size)
         */
        
        // Calculate the co-ordinates of the desired tile within the texture
        int texTileX = id % TEXTURE_TILES_X;
        int texTileY = id / TEXTURE_TILES_X;
        
        // Calculate the pixel co-ordinates of the tile within the texture,
        // add or subtract 0.5 (half-pixel correction), and divide by the 
        // texture size to get the relative texture co-ordinates.
        float texCoordX1 = (texTileX * TEXTURE_TILE_WIDTH + 0.5f) / TEXTURE_WIDTH;
        float texCoordY1 = (texTileY * TEXTURE_TILE_HEIGHT + 0.5f) / TEXTURE_HEIGHT;
        float texCoordX2 = ((texTileX + 1) * TEXTURE_TILE_WIDTH  - 0.5f) / TEXTURE_WIDTH;
        float texCoordY2 = ((texTileY + 1) * TEXTURE_TILE_HEIGHT - 0.5f) / TEXTURE_HEIGHT;
        
        Vertex topLeft     = vertices[tileX][tileZ];
        Vertex topRight    = vertices[tileX + 1][tileZ];
        Vertex bottomLeft  = vertices[tileX][tileZ + 1];
        Vertex bottomRight = vertices[tileX + 1][tileZ + 1];
        
        /*
         * We have to take care to add the vertices in a counter-
         * clockwise order so that the terrain is facing the right way.
         */
        
        if (flipDiagonal){
            // Bottom-left triangle
            addVertexTexture(bottomRight, texCoordX2, texCoordY2);
            addVertexTexture(bottomLeft, texCoordX1, texCoordY2);
            addVertexTexture(topLeft, texCoordX1, texCoordY1);
            
            // Top-right triangle
            addVertexTexture(topLeft, texCoordX1, texCoordY1);
            addVertexTexture(topRight, texCoordX2, texCoordY1);
            addVertexTexture(bottomRight, texCoordX2, texCoordY2);
        } else {
            // Top-left triangle
            addVertexTexture(topRight, texCoordX2, texCoordY1);
            addVertexTexture(bottomLeft, texCoordX1, texCoordY2);
            addVertexTexture(topLeft, texCoordX1, texCoordY1);

            // Bottom-right triangle
            addVertexTexture(bottomRight, texCoordX2, texCoordY2);
            addVertexTexture(bottomLeft, texCoordX1, texCoordY2);
            addVertexTexture(topRight, texCoordX2, texCoordY1);
        }
    }

    /**
     * Adds the properties of the given Vertex to the texture co-ordinate buffer.
     * 
     * The texture co-ordinates are supplied as separate parameters as the
     * texture is a property of the tile rather than the vertex.
     * 
     * @param v
     * @param texCoordX
     * @param texCoordY
     */
    private void addVertexTexture(Vertex v, float texCoordX, float texCoordY) {
        texCoordBuffer.put(texCoordX);
        texCoordBuffer.put(texCoordY);
    }

}
