package com.danjb.otherdom.client.scene;

/**
 * Class containing all present objects and rendering settings.
 * 
 * @author Dan Bryce
 */
public class Scene {

    private Camera camera;

    private World world;
    
    private LightSettings lightSettings;

    public Scene(World world, Camera camera, LightSettings lightSettings) {
        this.world = world;
        this.camera = camera;
        this.lightSettings = lightSettings;
    }
    
    public Camera getCamera() {
        return camera;
    }
    
    public World getWorld() {
        return world;
    }
    
    public LightSettings getLightSettings() {
        return lightSettings;
    }

}
