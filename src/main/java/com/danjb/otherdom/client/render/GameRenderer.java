package com.danjb.otherdom.client.render;

import java.util.Map;

import com.danjb.otherdom.client.GLWindow;
import com.danjb.otherdom.client.scene.Player;
import com.danjb.otherdom.client.scene.Scene;
import com.danjb.otherdom.client.scene.World;

public class GameRenderer extends Renderer {

    private Scene scene;
    private TerrainRenderer terrainRenderer;
    private BillboardModelRenderer billboardModelRenderer;
    
    public GameRenderer(GLWindow window, Scene scene) {
        this.scene = scene;
        
        terrainRenderer = new TerrainRenderer(window);
        billboardModelRenderer = new BillboardModelRenderer(window);
    }

    public void render(RenderMode mode) {
        clearScreen();
        terrainRenderer.render(scene, mode);
        renderPlayers(scene, mode);
    }

    public void renderPlayers(Scene scene, RenderMode mode) {

        ShaderProgram shader = null;
        if (mode == RenderMode.OBJ_SELECTION){
            shader = Shaders.billboardSelectionShader;
        } else {
            shader = Shaders.billboardShader;
        }
        shader.use();
        
        World world = scene.getWorld();
        Map<Integer, Player> players = world.getPlayers();
        
        for (Player player : players.values()){
            billboardModelRenderer.render(scene, mode, player.getModel(), shader);
        }

        shader.deselect();
    }

}
