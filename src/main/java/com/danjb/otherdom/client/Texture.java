package com.danjb.otherdom.client;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.stb.STBImage;

public class Texture {

    private static final String GFX_DIR = Client.RESOURCE_DIR + "gfx/";
    
    private int id;
    
    /**
     * Creates a Texture using the given image file.
     * 
     * See:
     * https://github.com/SilverTiger/lwjgl3-tutorial/wiki/Textures
     * 
     * @param filename
     * @param useNearestNeighbour
     *  Whether to use nearest neighbour interpolation for scaling.
     *  This should be set to true for BillboardModels, as linear interpolation
     *  can produce strange results (e.g. soft edges). 
     */
    public Texture(String filename, boolean useNearestNeighbour) {

        // Read image into a ByteBuffer
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);
        // Note that stbi_load automatically flips the texture so that the
        // origin is in the top-left, rather than the bottom-left.
        ByteBuffer texelData = 
                STBImage.stbi_load(GFX_DIR + filename, w, h, comp, 4);
        if (texelData == null) {
            throw new RuntimeException("Error loading " + filename + ": " +
                    STBImage.stbi_failure_reason());
        }
        int width = w.get();
        int height = h.get();
        
        // Generate texture ID
        id = GL11.glGenTextures();
        
        // Pass our texture to the shader
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height,
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, texelData);

        // Use linear filtering for texture scaling
        int interpolation = useNearestNeighbour ? 
                GL11.GL_NEAREST : GL11.GL_LINEAR;
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MIN_FILTER, interpolation);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MAG_FILTER, interpolation);
        
        // Clamp texture co-ordinates between 0 and 1
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); // Deselect

        int errorCode = GL11.glGetError();
        if (errorCode != GL11.GL_NO_ERROR) {
            throw new RuntimeException(
                    "OpenGL error " + String.valueOf(errorCode)
                    + " loading texture: " + filename);
        }
    }

    /**
     * Binds this Texture.
     * 
     * See:
     * https://www.opengl.org/wiki/Sampler_%28GLSL%29#Binding_textures_to_samplers 
     */
    public void bind() {
        // Bind our texture to texture unit 0
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    public int getId() {
        return id;
    }

}
