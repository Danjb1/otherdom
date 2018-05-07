package com.danjb.otherdom.client;

import org.joml.Vector3f;

import com.danjb.otherdom.client.render.GameRenderer;
import com.danjb.otherdom.client.render.Renderer.RenderMode;
import com.danjb.otherdom.client.scene.Camera;
import com.danjb.otherdom.client.scene.LightSettings;
import com.danjb.otherdom.client.scene.Player;
import com.danjb.otherdom.client.scene.Scene;
import com.danjb.otherdom.client.scene.TerrainSection;
import com.danjb.otherdom.client.scene.World;

public class GameState extends State {

    private Scene scene;
    private World world;
    private GameRenderer renderer;
    private MousePicker mousePicker;
    
    public GameState(Client client) {
        super(client);
        
        GLWindow window = client.getWindow();
        
        // Initialise Scene
        Camera camera = new Camera(window);
        Player player = new Player(
                TerrainSection.NUM_TILES_PER_ROW / 2,
                TerrainSection.NUM_TILES_PER_ROW / 2,
                camera);
        world = new World();
        world.addPlayer(0, player);
        
        /*
         * If the diffuse light is directly above the scene, all vertices will
         * be fully lit, so we use a slanted angle to ensure that we get some
         * cool shading.
         * 
         * For now we are only using white light.
         */
        LightSettings lighting = new LightSettings(
                new Vector3f(1.0f, 1.0f, 1.0f),
                0.1f,
                new Vector3f(1.0f, 1.0f, 1.0f),
                new Vector3f(0.5f, 1.0f, 1.0f),
                1.0f);
        scene = new Scene(world, camera, lighting);
        
        renderer = new GameRenderer(window, scene);
        mousePicker = new MousePicker(window, scene, renderer);
    }

    @Override
    public void processInput(Input input) {
        
        if (input.wasKeyReleased(Input.KEY_MOUSE_LEFT)){
            mousePicker.processSelection(input.getMouseX(), input.getMouseY());
        }
        
        if (input.isKeyDown(Input.KEY_LEFT)){
            scene.getCamera().modAngle(-Camera.ROT_SPEED);
        } else if (input.isKeyDown(Input.KEY_RIGHT)){
            scene.getCamera().modAngle(Camera.ROT_SPEED);
        }
    }

    @Override
    public void update() {
        world.update();
    }

    @Override
    public void render() {
        renderer.render(RenderMode.NORMAL);
    }

}
