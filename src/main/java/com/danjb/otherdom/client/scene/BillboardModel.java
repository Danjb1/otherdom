package com.danjb.otherdom.client.scene;

import java.nio.FloatBuffer;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.danjb.otherdom.client.Texture;
import com.danjb.otherdom.client.render.Shaders;
import com.danjb.otherdom.client.scene.World.CompassDirection;

/**
 * A 2d sprite in 3d space that is always oriented towards the camera.
 * 
 * Note that BillboardModels never rotate; instead, the vertex shader
 * transforms the vertices in such a way that the model always appears to be
 * facing the camera.
 * 
 * See:
 * https://en.wikibooks.org/wiki/GLSL_Programming/Unity/Billboards
 * 
 * @author Dan Bryce
 */
public class BillboardModel extends WorldModel {

    public static final int NUM_VERTICES = 6;

    /**
     * Width of one frame of the texture, in texture co-ordinates.
     */
    public static final float TEX_FRAME_WIDTH = 
            1.0f / World.NUM_COMPASS_DIRECTIONS;
    
    // The camera starts off facing north, so by default our models face
    // south, towards the camera.
    private CompassDirection facing = CompassDirection.SOUTH;
    
    /**
     * Frame of the texture to draw.
     */
    private int textureFrame;
    
    /**
     * Scale multiplier.
     * 
     * If this is 1, a BillboardModel is drawn 1m high.
     */
    private float scale;

    /**
     * Camera from which the BillboardModel's facing is calculated.
     */
    private Camera camera;
    
    /**
     * Buffer used to store vertex positions (x, y, z).
     */
    private FloatBuffer vertexBuffer = 
            BufferUtils.createFloatBuffer(NUM_VERTICES * 3);
    
    /**
     * Buffer used to store vertex texture co-ordinates (s, t).
     */
    private FloatBuffer texCoordBuffer = 
            BufferUtils.createFloatBuffer(NUM_VERTICES * 2);

    /**
     * ID of the VBO that holds the position of each vertex.
     */
    private int vboIdPositions;

    /**
     * ID of the VBO that holds the texture co-ordinates of each vertex.
     */
    private int vboIdTexCoords = -1;

    /**
     * Selection code that identifies the "type" of object being clicked on.
     */
    private float selectionCode;

    /**
     * Creates a BillboardModel.
     * @param x X-position of this BillboardModel.
     * @param z Z-position of this BillboardModel.
     * @param texture
     * @param scale
     * @param camera
     * @param selectionCode Constant from MousePicker.
     */
    public BillboardModel(float x, float z, Texture texture, float scale, 
            Camera camera, float selectionCode) {
        
        // For now, assume that BillboardModels are always standing at sea level.
        // Later, we will calculate the z-position based on the floor height.
        super(new Vector3f(x, 0.5f * scale, z), texture);
        
        this.scale = scale;
        this.camera = camera;
        this.selectionCode = selectionCode;
        
        /*
         * The positions of the vertices in a BillboardModel are not literal
         * positions in 3d space. Instead, they are just used by the vertex
         * shader to determine where each vertex lies relative to the
         * point-position of the model.
         */
        
        // 1st triangle, with vertices defined in a counter-clockwise order
        createVertex(-0.5f, 0,  0.5f);
        createVertex(-0.5f, 0, -0.5f);
        createVertex( 0.5f, 0,  0.5f);

        // 2nd triangle, with vertices defined in a counter-clockwise order
        createVertex( 0.5f, 0,  0.5f);
        createVertex(-0.5f, 0, -0.5f);
        createVertex( 0.5f, 0, -0.5f);
        
        // Prepare buffer for reading
        vertexBuffer.flip();
        
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

        // Deselect the VAO once we're done setting vertex attribute data
        GL30.glBindVertexArray(0);

        refreshTexture();
    }

    /**
     * Creates a vertex at the given position.
     * @param x
     * @param y
     * @param z
     */
    private void createVertex(float x, float y, float z) {
        vertexBuffer.put(x);
        vertexBuffer.put(y);
        vertexBuffer.put(z);
    }
    
    @Override
    public void update() {
        refreshTexture();
    }

    /**
     * Determine which side of this model should be drawn.
     * This is based on the camera angle and model facing.
     * 
     * This table shows some of the possible combinations, and was used
     * to calculate the algorithm:
     * 
     * Camera Facing    Model Facing    Image   (Model - Camera)
     * ----------------------------------------------------------
     * NORTH (0)        NORTH (0)       Back     0
     * NORTH (0)        EAST  (2)       Right    2
     * NORTH (0)        SOUTH (4)       Front    4
     * NORTH (0)        WEST  (6)       Left     6
     * EAST (2)         NORTH (0)       Left    -2  (6)
     * EAST (2)         EAST  (2)       Back     0
     * EAST (2)         SOUTH (4)       Right    2
     * EAST (2)         WEST  (6)       Front    4
     * SOUTH (4)        NORTH (0)       Front   -4  (4)
     * SOUTH (4)        EAST  (2)       Left    -2  (6)
     * SOUTH (4)        SOUTH (4)       Back     0
     * SOUTH (4)        WEST  (6)       Right    2
     * WEST (6)         NORTH (0)       Right   -6  (2)
     * WEST (6)         EAST  (2)       Front   -4  (4)
     * WEST (6)         SOUTH (4)       Left    -2  (6)
     * WEST (6)         WEST  (6)       Back     0
     * 
     * Where minus numbers appear in the final column, we simply add 8 to
     * obtain the required result.
     * 
     * Using these results, we can clearly see that each resulting number
     * in the right-most column corresponds to a direction. We can easily
     * infer the other compass directions as well:
     * 
     *      0 = Back
     *      1 = Back-Right
     *      2 = Right
     *      3 = Front-Right
     *      4 = Front
     *      5 = Front-Left
     *      6 = Left
     *      7 = Back-Left
     */
    private void refreshTexture() {
        int cameraFacing = camera.getCompassAngle().ordinal();
        int modelFacing = facing.ordinal();
        int imageId = modelFacing - cameraFacing;
        if (imageId < 0){
            imageId += World.NUM_COMPASS_DIRECTIONS;
        }
        textureFrame = imageId;
        
        // 1st triangle, with vertices defined in a counter-clockwise order
        setVertexTexture(0, 1);
        setVertexTexture(0, 0);
        setVertexTexture(1, 1);

        // 2nd triangle, with vertices defined in a counter-clockwise order
        setVertexTexture(1, 1);
        setVertexTexture(0, 0);
        setVertexTexture(1, 0);

        // Prepare buffer for reading
        texCoordBuffer.flip();
        
        // Send tex coords to GPU
        GL30.glBindVertexArray(vaoId); // Bind to VAO
        // Create VBO and fill it with vertex texture co-ordinates
        if (vboIdTexCoords == -1){
            vboIdTexCoords = GL15.glGenBuffers();
        }
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIdTexCoords);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texCoordBuffer,
                GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(Shaders.ATTR_TEXTURE_COORDS,
                2, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Deselect
        GL30.glBindVertexArray(0); // Deselect VAO
    }

    /**
     * Sets the texture of the given vertex.
     * @param offsetX 0 = left, 1 = right
     * @param offsetY 0 = top, 1 = bottom
     */
    private void setVertexTexture(float offsetX, float offsetY) {
        float texCoordX = (textureFrame + offsetX) * TEX_FRAME_WIDTH;
        // Not sure why, but the texture is upside-down unless we substract
        // from 1.
        float texCoordY = 1 - (offsetY * 1.0f);
        texCoordBuffer.put(texCoordX);
        texCoordBuffer.put(texCoordY);
    }
    
    public float getScale() {
        return scale;
    }

    public Vector3f getSelectionCode() {
        // We divide by 255 because our shader expects our colours to be
        // floats in the range 0-1.
        return new Vector3f(selectionCode / 255.0f, 1.0f, 1.0f);
    }

}
