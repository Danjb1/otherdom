package com.danjb.otherdom.client;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.danjb.otherdom.client.render.GameRenderer;
import com.danjb.otherdom.client.render.Renderer.RenderMode;
import com.danjb.otherdom.client.scene.Player;
import com.danjb.otherdom.client.scene.Scene;
import com.danjb.otherdom.client.scene.TerrainSection;
import com.danjb.otherdom.client.scene.World;

public class MousePicker {

    /*
     * Selection codes
     */
    public static final int CODE_NOTHING = 0;
    public static final int CODE_TERRAIN = 1;
    public static final int CODE_PLAYER  = 2;
    
    private GLWindow window;
    private Scene scene;
    
    private GameRenderer renderer;
    
    private ByteBuffer selectedPixel = BufferUtils.createByteBuffer(4);

    public MousePicker(GLWindow window, Scene scene, GameRenderer renderer) {
        this.window = window;
        this.scene = scene;
        this.renderer = renderer;
    }
    
    /**
     * Processes a mouse-click at the given mouse co-ordinates.
     * 
     * @param x
     * @param y
     */
    public void processSelection(int x, int y) {

        // First, find out which object was selected
        byte[] code = getSelectedObjectCode(x, y);
        
        switch (code[0]){
        
        case CODE_NOTHING:
            break;
            
        case CODE_PLAYER:
            System.out.println("clicked on player");
            break;
            
        case CODE_TERRAIN:
            // Determine the section co-ordinates relative to the player's
            // current section (see TerrainSection.createSelectionCode()).
            int offsetX = Byte.toUnsignedInt(code[1]) - 128;
            int offsetY = Byte.toUnsignedInt(code[2]) - 128;
            World world = scene.getWorld();
            Player player = world.getCurrentPlayer();
            int sectionX = player.getSectionX() + offsetX;
            int sectionZ = player.getSectionZ() + offsetY;
            TerrainSection terrainSection = world.getSection(sectionX, sectionZ);
            code = getSelectedFaceCode(x, y);
            int tileX = code[0];
            int tileY = code[1];
            terrainSection.setTile(tileX, tileY, TerrainSection.TILE_GRASS);
            break;
            
        }
    }

    private byte[] getSelectedObjectCode(int x, int y) {
        /*
         * Since this is always performed BEFORE normal rendering and the 
         * window is not refreshed in between, the results of this rendering 
         * operation are never seen by the user.
         */
        renderer.render(RenderMode.OBJ_SELECTION);
        return sampleColour(x, y);
    }

    private byte[] getSelectedFaceCode(int x, int y) {
        /*
         * Since this is always performed BEFORE normal rendering and the 
         * window is not refreshed in between, the results of this rendering 
         * operation are never seen by the user.
         */
        renderer.render(RenderMode.FACE_SELECTION);
        return sampleColour(x, y);
    }

    /**
     * Samples the rendered colour at the given co-ordinates.
     * 
     * @param x
     * @param y
     * @return The sampled colour {r, g, b, a}.
     */
    private byte[] sampleColour(int x, int y) {
        // Transform the y co-ordinate to use the OpenGL (bottom-left) origin
        y = window.getHeight() - y;
        GL11.glReadPixels(x, y, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
                selectedPixel);
        byte r = selectedPixel.get();
        byte g = selectedPixel.get();
        byte b = selectedPixel.get();
        byte a = selectedPixel.get();
        selectedPixel.flip(); // For re-use later
        return new byte[]{r, g, b, a};
    }
    
}
