package com.danjb.otherdom.client.scene;

import org.joml.Vector3f;

public class LightSettings {

    private Vector3f ambientColour;
    
    private float ambientIntensity;

    private Vector3f diffuseColour;

    /**
     * Vector from a vertex to a directional light source (e.g. sunlight).
     */
    private Vector3f diffuseVector;
    
    private float diffuseIntensity;

    public LightSettings(
            Vector3f ambientColour,
            float ambientIntensity,
            Vector3f diffuseColour,
            Vector3f diffuseVector,
            float diffuseIntensity) {
        this.ambientColour = ambientColour;
        this.ambientIntensity = ambientIntensity;
        this.diffuseColour = diffuseColour;
        this.diffuseVector = diffuseVector.normalize();
        this.diffuseIntensity = diffuseIntensity;
    }
    
    public Vector3f getAmbientColour() {
        return ambientColour;
    }
    
    public float getAmbientIntensity() {
        return ambientIntensity;
    }
    
    public Vector3f getDiffuseColour() {
        return diffuseColour;
    }
    
    public Vector3f getDiffuseVector() {
        return diffuseVector;
    }
    
    public float getDiffuseIntensity() {
        return diffuseIntensity;
    }

}
