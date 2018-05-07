package com.danjb.otherdom.client.render;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.danjb.otherdom.client.GLWindow;
import com.danjb.otherdom.client.scene.Camera;
import com.danjb.otherdom.client.scene.LightSettings;
import com.danjb.otherdom.client.scene.Player;
import com.danjb.otherdom.client.scene.Scene;
import com.danjb.otherdom.client.scene.TerrainSection;
import com.danjb.otherdom.client.scene.World;

public class TerrainRenderer extends Renderer {

    /**
     * The number of visible TerrainSections surrounding the current section.
     */
    private static final int NUM_VISIBLE_SURROUNDING_SECTIONS = 1;

    private GLWindow window;

    private FloatBuffer fb3 = BufferUtils.createFloatBuffer(3);
    private FloatBuffer fb16 = BufferUtils.createFloatBuffer(16);
    
    private Matrix4f projection = new Matrix4f();
    private Matrix4f modelView = new Matrix4f();
    
    public TerrainRenderer(GLWindow window) {
        this.window = window;
    }
    
    public void render(Scene scene, RenderMode mode) {

        ShaderProgram shader = null;
        if (mode == RenderMode.OBJ_SELECTION){
            shader = Shaders.objSelectionShader;
        } else if (mode == RenderMode.FACE_SELECTION){
            shader = Shaders.faceSelectionShader;
        } else {
            shader = Shaders.screenShader;
        }
        shader.use();
        
        World world = scene.getWorld();
        Player player = world.getCurrentPlayer();
        
        for (int offsetY = -NUM_VISIBLE_SURROUNDING_SECTIONS; 
                offsetY <= NUM_VISIBLE_SURROUNDING_SECTIONS; offsetY++){
            for (int offsetX = -NUM_VISIBLE_SURROUNDING_SECTIONS; 
                    offsetX <= NUM_VISIBLE_SURROUNDING_SECTIONS; offsetX++){
                int sectionX = player.getSectionX() + offsetX;
                int sectionZ = player.getSectionZ() + offsetY;
                TerrainSection section = world.getSection(sectionX, sectionZ);
                render(scene, mode, shader, section);
            }
        }
        
        shader.deselect();
    }

    private void render(Scene scene, RenderMode mode, ShaderProgram shader,
            TerrainSection section) {

        Camera camera = scene.getCamera();
        LightSettings lighting = scene.getLightSettings();
     
        /*
         * Set the projection matrix.
         * This specifies the properties of the camera.
         */
        projection.setPerspective(
                camera.getFovY(),
                window.getAspectRatio(), 
                Camera.Z_NEAR,
                Camera.Z_FAR);
        shader.setUniformMatrix4f(
                Shaders.UNIFORM_PROJECTION, projection.get(fb16));
        
        /*
         * Set the model-view matrix.
         * The result of this is that all vertices in the game world are moved
         * according to the camera position / orientation; the camera itself
         * technically never moves in OpenGL:
         * https://www.opengl.org/archives/resources/faq/technical/viewing.htm
         */
        modelView.setLookAt(
                camera.getPos(),
                camera.getTarget(),
                camera.getUpVector());
        modelView.translate(section.getPos());
        shader.setUniformMatrix4f(
                Shaders.UNIFORM_MODELVIEW, modelView.get(fb16));
        
        if (mode == RenderMode.NORMAL){
            // We needn't bother setting the texture in other render modes,
            // since terrain textures are always solid blocks.
            section.getTexture().bind();
            // Tell the shader to sample from texture unit 0.
            // This is the default anyway.
            shader.setUniform1i(Shaders.UNIFORM_TEXUNIT, 0);
    
            /*
             * Pass the lighting information to the shader.
             * 
             * To compute the diffuse component, we need:
             *  - The vector from the vertex to the light source.
             *  - The vertex normal.
             *  - The diffuse component of the light (RGB).
             *  - The diffuse component of the vertex (RGB).
             *  
             * The diffuse vector and diffuse component of the light are uniform
             * variables, while the vertex normal and diffuse component of the
             * vertex are per-vertex variables.
             *  
             * See:
             * http://www.lighthouse3d.com/opengl/terrain/index.php?light
             */
            shader.setUniform3f(Shaders.UNIFORM_AMBIENT_COLOUR, 
                    lighting.getAmbientColour().get(fb3));
            shader.setUniform1f(Shaders.UNIFORM_AMBIENT_INTENSITY, 
                    lighting.getAmbientIntensity());
            shader.setUniform3f(Shaders.UNIFORM_DIFFUSE_COLOUR, 
                    lighting.getDiffuseColour().get(fb3));
            shader.setUniform3f(Shaders.UNIFORM_DIFFUSE_ANGLE, 
                    lighting.getDiffuseVector().get(fb3));
            shader.setUniform1f(Shaders.UNIFORM_DIFFUSE_INTENSITY, 
                    lighting.getDiffuseIntensity());
            
        } else if (mode == RenderMode.OBJ_SELECTION){
            // Determine the selection code for the object being rendered
            Player player = scene.getWorld().getCurrentPlayer();
            Vector3f code = section.createSelectionCode(
                    player.getSectionX(), player.getSectionZ());
            shader.setUniform3f(Shaders.UNIFORM_SELECTION_CODE, code.get(fb3));
        }
        
        // Bind to the VAO that has all the information about the vertices
        GL30.glBindVertexArray(section.getVaoId());
        shader.enableVertexAttributeArray(
                Shaders.ATTR_VERTEX);

        if (mode == RenderMode.NORMAL){
            shader.enableVertexAttributeArray(
                    Shaders.ATTR_VERTEX_NORMAL);
            shader.enableVertexAttributeArray(
                    Shaders.ATTR_MATERIAL_AMBIENT_COLOUR);
            shader.enableVertexAttributeArray(
                    Shaders.ATTR_MATERIAL_DIFFUSE_COLOUR);
            shader.enableVertexAttributeArray(
                    Shaders.ATTR_TEXTURE_COORDS);
        } else if (mode == RenderMode.FACE_SELECTION){
            shader.enableVertexAttributeArray(
                    Shaders.ATTR_SELECTION_CODE);
        }

        /*
         * Draw the vertices.
         * We could mess around with GL_TRIANGLE_STRIPS and link rows using
         * degenerate triangles, but it's much easier this way and the
         * performance difference is little to none.
         */
        GL11.glDrawArrays(
                GL11.GL_TRIANGLES, 0, TerrainSection.NUM_VERTICES);

        // Put everything back to default (deselect)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        shader.disableVertexAttributeArray(
                Shaders.ATTR_VERTEX);

        if (mode == RenderMode.NORMAL){
            shader.disableVertexAttributeArray(
                    Shaders.ATTR_VERTEX_NORMAL);
            shader.disableVertexAttributeArray(
                    Shaders.ATTR_MATERIAL_AMBIENT_COLOUR);
            shader.disableVertexAttributeArray(
                    Shaders.ATTR_MATERIAL_DIFFUSE_COLOUR);
            shader.disableVertexAttributeArray(
                    Shaders.ATTR_TEXTURE_COORDS);
        } else if (mode == RenderMode.FACE_SELECTION){
            shader.disableVertexAttributeArray(
                    Shaders.ATTR_SELECTION_CODE);
        }
        
        GL30.glBindVertexArray(0);
    }

}
