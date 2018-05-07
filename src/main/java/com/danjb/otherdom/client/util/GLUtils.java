package com.danjb.otherdom.client.util;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class GLUtils {

    public static void initGL(int width, int height) {

        GL.createCapabilities();
        
        // Enable depth buffer
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        // Set background colour
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        // Set viewport to the whole window
        GL11.glViewport(0, 0, width, height);

        // Enable alpha blending (transparency)
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Enable back-face culling
//        GL11.glEnable(GL11.GL_CULL_FACE);
//        GL11.glCullFace(GL11.GL_BACK);

        // Check for errors
        int errorCode = GL11.glGetError();
        if (errorCode != GL11.GL_NO_ERROR) {
            throw new RuntimeException(
                    "OpenGL error " + String.valueOf(errorCode)
                    + " during initialisation");
        }
    }

}
