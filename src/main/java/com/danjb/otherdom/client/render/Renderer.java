package com.danjb.otherdom.client.render;

import org.lwjgl.opengl.GL11;

public class Renderer {

    public static enum RenderMode {
        NORMAL,

        /**
         * RenderMode that enables us to do mouse picking on a per-object basis.
         * 
         * Every object is given a unique colour, which can be used later to
         * determine which one was clicked on.
         *  
         * See:
         * http://www.lighthouse3d.com/tutorials/opengl-selection-tutorial/
         */
        OBJ_SELECTION,

        /**
         * RenderMode that enables us to do mouse picking on a per-face basis.
         * 
         * Every primitive is given a unique colour, which can be used later to
         * determine which one was clicked on.
         * 
         * Also see OBJ_SELECTION.
         */
        FACE_SELECTION,
    }
    
    /**
     * Clears the colour and depth buffers.
     */
    protected void clearScreen() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void setWireframeMode(boolean wireframe){
        if (wireframe){
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        } else {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }
    }
    
}
