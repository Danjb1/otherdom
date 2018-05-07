package com.danjb.otherdom.client.scene;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.danjb.otherdom.client.Texture;

/**
 * A 3d model that exists within the game world.
 * 
 * @author Dan Bryce
 */
public abstract class WorldModel {

    /**
     * The position of this WorldModel within the game world (x, y, z).
     */
    protected Vector3f position;

    /**
     * Orientation represented as a Quaternion.
     */
    protected Quaternionf rotation = new Quaternionf();
    
    /**
     * ID of the Vertex Array Object (VAO) assigned by the GPU.
     * This links together the various buffers containing vertex properties.
     */
    protected int vaoId;

    private Texture texture;
    
    public WorldModel(Vector3f position, Texture texture) {
        this.position = position;
        this.texture = texture;
    }

    public void update(){
        // Nothing to do (yet)
    }

    public Vector3f getPos(){
        return position;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public int getVaoId() {
        return vaoId;
    }

    public Texture getTexture() {
        return texture;
    }

}
