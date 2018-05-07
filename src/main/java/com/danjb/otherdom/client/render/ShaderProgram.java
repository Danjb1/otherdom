package com.danjb.otherdom.client.render;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 * Class representing a shader program to be run on the GPU.
 */
public class ShaderProgram {

    ////////////////////////////////////////////////////////////////////////////
    // ShaderException
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Exception thrown if any problems occur during shader construction.
     */
    public static class ShaderException extends IOException {

        private static final long serialVersionUID = 1L;
        
        public ShaderException(String message) {
            super(message);
        }
        
    }

    ////////////////////////////////////////////////////////////////////////////
    // Builder
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Class responsible for building a shader program.
     */
    public static class Builder {

        /**
         * ID of the shader program under construction.
         */
        private int programId;
        
        /**
         * Locations of our uniform variables.
         */
        private Map<String, Integer> uniformLocations = new HashMap<>();
        
        /**
         * Creates a new program using the given vertex and fragment shader.
         * 
         * @param vertexShader
         * @param fragmentShader
         * @return This Builder object, for call chaining.
         * @throws IOException
         */
        public Builder createProgram(String vertexShader, String fragmentShader) 
                throws IOException {
            
            // Clear any pre-existing error flag
            GL11.glGetError();
            
            // Load shaders
            int vsId = loadShader(vertexShader, GL20.GL_VERTEX_SHADER);
            int fsId = loadShader(fragmentShader, GL20.GL_FRAGMENT_SHADER);

            // Create a new shader program that links both shaders
            programId = GL20.glCreateProgram();
            GL20.glAttachShader(programId, vsId);
            GL20.glAttachShader(programId, fsId);
            
            return this;
        }

        /**
         * Loads and compiles the given shader's source code.
         * 
         * @param filename
         * @param type
         * @return
         * @throws IOException
         */
        private static int loadShader(String filename, int type)
                throws IOException {
            
            // Read a shader file
            StringBuilder shaderSource = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new FileReader(Shaders.SHADER_DIR + filename))){
                String line;
                while ((line = reader.readLine()) != null) {
                    shaderSource.append(line + "\n");
                }
            }

            // Compile shader source
            int shaderId = GL20.glCreateShader(type);
            GL20.glShaderSource(shaderId, shaderSource);
            GL20.glCompileShader(shaderId);
            if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) 
                    != GL11.GL_TRUE){
                GL20.glDeleteShader(shaderId);
                throw new ShaderException(
                        "Error compiling shader " + filename + ": "
                                + GL20.glGetShaderInfoLog(shaderId));
            }

            return shaderId;
        }

        /**
         * Binds the given shader parameter to the given attribute ID.
         * 
         * @return This Builder object, for call chaining.
         */
        public Builder addAttribute(int attributeId, String parameterName){
            GL20.glBindAttribLocation(programId, attributeId, parameterName);
            return this;
        }
        
        /**
         * Links and validates the current program.
         * @return This Builder object, for call chaining.
         * @throws ShaderException 
         */
        public Builder linkAndValidate() throws ShaderException{
            
            GL20.glLinkProgram(programId);
            int success = GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS);
            if (success != GL11.GL_TRUE){
                GL20.glDeleteProgram(programId);
                throw new ShaderException("Error linking shader program");
            }
            
            GL20.glValidateProgram(programId);
            success = GL20.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS);
            if (success != GL11.GL_TRUE){
                GL20.glDeleteProgram(programId);
                throw new ShaderException("Error validating shader program");
            }
            
            return this;
        }

        /**
         * Retrives the location of a uniform variable for later use.
         * 
         * @param key
         * @param parameterName
         * @return This Builder object, for call chaining.
         * @throws ShaderException
         */
        public Builder addUniform(String key, String parameterName)
                throws ShaderException{
            
            int loc = GL20.glGetUniformLocation(programId, parameterName);
            
            if (loc == -1){
                GL20.glDeleteProgram(programId);
                throw new ShaderException(
                        "Unable to find uniform location: " + parameterName);
            }
            
            uniformLocations.put(key, loc);
            
            return this;
        }

        /**
         * Checks for any errors that may have arisen.
         * 
         * @return This Builder object, for call chaining.
         * @throws ShaderException 
         */
        public Builder errorCheck() throws ShaderException{

            // Check for errors
            int errorCode = GL11.glGetError();
            if (errorCode != GL11.GL_NO_ERROR) {
                throw new ShaderException(
                        "OpenGL error " + String.valueOf(errorCode)
                        + " during shader initialisation: "
                        + GL20.glGetProgramInfoLog(programId));
            }
            
            return this;
        }
        
        public ShaderProgram build(){
            return new ShaderProgram(this);
        }
        
    }

    ////////////////////////////////////////////////////////////////////////////
    // ShaderProgram
    ////////////////////////////////////////////////////////////////////////////

    /**
     * ID of the compiled shader program.
     */
    private int programId;

    /**
     * Locations of our uniform variables.
     */
    private Map<String, Integer> uniformLocations = new HashMap<>();
    
    public ShaderProgram(Builder builder) {
        this.programId = builder.programId;
        this.uniformLocations = builder.uniformLocations;
    }

    /**
     * Starts using this ShaderProgram.
     */
    public void use() {
        GL20.glUseProgram(programId);
    }

    /**
     * Stops using this ShaderProgram.
     */
    public void deselect() {
        GL20.glUseProgram(0);
    }

    public void setUniformMatrix4f(String key, FloatBuffer fb) {
        GL20.glUniformMatrix4fv(uniformLocations.get(key), false, fb);
    }

    public void setUniform1f(String key, float i) {
        GL20.glUniform1f(uniformLocations.get(key), i);
    }

    public void setUniform1i(String key, int i) {
        GL20.glUniform1i(uniformLocations.get(key), i);
    }

    public void setUniform3f(String key, FloatBuffer fb) {
        GL20.glUniform3fv(uniformLocations.get(key), fb);
    }

    public void enableVertexAttributeArray(int attributeId) {
        GL20.glEnableVertexAttribArray(attributeId);
    }

    public void disableVertexAttributeArray(int attributeId) {
        GL20.glEnableVertexAttribArray(attributeId);
    }

}
