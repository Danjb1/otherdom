package com.danjb.otherdom.client.render;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.danjb.otherdom.client.GLWindow;
import com.danjb.otherdom.client.scene.BillboardModel;
import com.danjb.otherdom.client.scene.Camera;
import com.danjb.otherdom.client.scene.LightSettings;
import com.danjb.otherdom.client.scene.Scene;

public class BillboardModelRenderer extends Renderer {

    private GLWindow window;

    private FloatBuffer fb3 = BufferUtils.createFloatBuffer(3);
    private FloatBuffer fb16 = BufferUtils.createFloatBuffer(16);
    
    private Matrix4f projection = new Matrix4f();
    private Matrix4f modelView = new Matrix4f();
    
    public BillboardModelRenderer(GLWindow window) {
        this.window = window;
    }

    public void render(Scene scene, RenderMode mode, BillboardModel model,
            ShaderProgram shader) {

        Camera camera = scene.getCamera();
        LightSettings lighting = scene.getLightSettings();
     
        // Set projection and modelview, just like TerrainRenderer
        projection.setPerspective(
                camera.getFovY(),
                window.getAspectRatio(), 
                Camera.Z_NEAR,
                Camera.Z_FAR);
        shader.setUniformMatrix4f(
                Shaders.UNIFORM_PROJECTION, projection.get(fb16));
        modelView.setLookAt(
                camera.getPos(),
                camera.getTarget(),
                camera.getUpVector());
        modelView.translate(model.getPos());
        shader.setUniformMatrix4f(
                Shaders.UNIFORM_MODELVIEW, modelView.get(fb16));
        
        if (mode == RenderMode.NORMAL){
            // We use simplified lighting for sprites (no diffuse angle)
            shader.setUniform3f(Shaders.UNIFORM_AMBIENT_COLOUR,
                    lighting.getAmbientColour().get(fb3));
            shader.setUniform1f(Shaders.UNIFORM_AMBIENT_INTENSITY,
                    lighting.getAmbientIntensity());
            shader.setUniform3f(Shaders.UNIFORM_DIFFUSE_COLOUR,
                    lighting.getDiffuseColour().get(fb3));
            shader.setUniform1f(Shaders.UNIFORM_DIFFUSE_INTENSITY,
                    lighting.getDiffuseIntensity());
            
        } else if (mode == RenderMode.OBJ_SELECTION){
            // Determine the selection code for the object being rendered
            Vector3f code = model.getSelectionCode();
            shader.setUniform3f(Shaders.UNIFORM_SELECTION_CODE, code.get(fb3));
        }

        // We always need to set the texture, even in selection mode, because
        // we don't want a mouse click on a transparent pixels to count as
        // clicking on the player.
        model.getTexture().bind();
        // Tell the shader to sample from texture unit 0.
        // This is the default anyway.
        shader.setUniform1i(Shaders.UNIFORM_TEXUNIT, 0);
        
        // Set scale
        shader.setUniform1f(Shaders.UNIFORM_SCALE, model.getScale());
        
        // Bind to the VAO that has all the information about the vertices
        GL30.glBindVertexArray(model.getVaoId());
        shader.enableVertexAttributeArray(Shaders.ATTR_VERTEX);
        shader.enableVertexAttributeArray(Shaders.ATTR_TEXTURE_COORDS);
            
        // Draw the vertices
        GL11.glDrawArrays(
                GL11.GL_TRIANGLES, 0, BillboardModel.NUM_VERTICES);

        // Put everything back to default (deselect)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        shader.disableVertexAttributeArray(Shaders.ATTR_VERTEX);
        shader.disableVertexAttributeArray(Shaders.ATTR_TEXTURE_COORDS);
        
        GL30.glBindVertexArray(0);
    }

}
