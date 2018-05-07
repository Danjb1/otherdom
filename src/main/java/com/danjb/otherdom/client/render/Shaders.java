package com.danjb.otherdom.client.render;

import java.io.IOException;

import com.danjb.otherdom.client.Client;

public class Shaders {

    public static final String SHADER_DIR = Client.RESOURCE_DIR + "shaders/";

    // Uniform variable names
    public static final String UNIFORM_PROJECTION = "projection";
    public static final String UNIFORM_MODELVIEW = "modelView";
    public static final String UNIFORM_TEXUNIT = "texUnit";
    public static final String UNIFORM_AMBIENT_COLOUR = "lightAmbientColour";
    public static final String UNIFORM_AMBIENT_INTENSITY = "lightAmbientIntensity";
    public static final String UNIFORM_DIFFUSE_COLOUR = "lightDiffuseColour";
    public static final String UNIFORM_DIFFUSE_INTENSITY = "lightDiffuseIntensity";
    public static final String UNIFORM_DIFFUSE_ANGLE = "lightDiffuseAngle";
    public static final String UNIFORM_SELECTION_CODE = "selectionCode";
    public static final String UNIFORM_SCALE = "scale";

    // Attribute IDs
    public static final int ATTR_VERTEX = 0;
    public static final int ATTR_VERTEX_NORMAL = 1;
    public static final int ATTR_MATERIAL_AMBIENT_COLOUR = 2;
    public static final int ATTR_MATERIAL_DIFFUSE_COLOUR = 3;
    public static final int ATTR_TEXTURE_COORDS = 4;
    public static final int ATTR_SELECTION_CODE = 5;
    
    /**
     * Screen Shader.
     * 
     * Used for rendering the game as normal.
     */
    public static ShaderProgram screenShader;
    private static final String SHADER_SCREEN_VERT = "screen.vert";
    private static final String SHADER_SCREEN_FRAG = "screen.frag";

    /**
     * Object Selection Shader.
     * 
     * Used for rendering the game for object selection.
     * 
     * Each object is given a unique colour code, and rendered as a solid block
     * of colour. This allows us to read the colour at the mouse co-ordinates
     * in order to determine the object that was clicked on.
     */
    public static ShaderProgram objSelectionShader;
    private static final String SHADER_OBJ_SELECTION_VERT = "obj_selection.vert";
    private static final String SHADER_OBJ_SELECTION_FRAG = "obj_selection.frag";

    /**
     * Face Selection Shader.
     * 
     * Used for rendering the game for face selection.
     * 
     * This is like the object selection shader, but it uses a VBO to give each
     * face a different colour.
     */
    public static ShaderProgram faceSelectionShader;
    private static final String SHADER_FACE_SELECTION_VERT = "face_selection.vert";
    private static final String SHADER_FACE_SELECTION_FRAG = "face_selection.frag";

    /**
     * Billboard Shader.
     * 
     * Used for rendering BillboardSprites.
     * 
     * This is like the screen shader but uses a much simpler lighting system
     * that doesn't take into account material colours or diffuse angles.
     */
    public static ShaderProgram billboardShader;
    private static final String SHADER_BILLBOARD_VERT = "billboard.vert";
    private static final String SHADER_BILLBOARD_FRAG = "screen.frag";
    
    /**
     * Billboard Selection Shader.
     * 
     * Used for rendering BillboardSprites for object selection.
     */
    public static ShaderProgram billboardSelectionShader;
    private static final String SHADER_BILLBOARD_SELECTION_VERT = "billboard_selection.vert";
    private static final String SHADER_BILLBOARD_SELECTION_FRAG = "billboard_selection.frag";
    
    /**
     * Creates the ShaderPrograms.
     * 
     * @throws IOException
     * @throws RuntimeException
     */
    public static void setupShaders() throws IOException, RuntimeException {
        
        screenShader = new ShaderProgram.Builder()
                .createProgram(SHADER_SCREEN_VERT, SHADER_SCREEN_FRAG)
                .addAttribute(ATTR_VERTEX, "vertex")
                .addAttribute(ATTR_VERTEX_NORMAL, "vertexNormal")
                .addAttribute(ATTR_MATERIAL_AMBIENT_COLOUR, "materialAmbientColour")
                .addAttribute(ATTR_MATERIAL_DIFFUSE_COLOUR, "materialDiffuseColour")
                .addAttribute(ATTR_TEXTURE_COORDS, "texCoord")
                .linkAndValidate()
                .addUniform(UNIFORM_PROJECTION, "projection")
                .addUniform(UNIFORM_MODELVIEW, "modelView")
                .addUniform(UNIFORM_TEXUNIT, "texUnit")
                .addUniform(UNIFORM_AMBIENT_COLOUR, "lightAmbientColour")
                .addUniform(UNIFORM_AMBIENT_INTENSITY, "lightAmbientIntensity")
                .addUniform(UNIFORM_DIFFUSE_COLOUR, "lightDiffuseColour")
                .addUniform(UNIFORM_DIFFUSE_ANGLE, "lightDiffuseAngle")
                .addUniform(UNIFORM_DIFFUSE_INTENSITY, "lightDiffuseIntensity")
                .errorCheck()
                .build();

        objSelectionShader = new ShaderProgram.Builder()
                .createProgram(SHADER_OBJ_SELECTION_VERT, SHADER_OBJ_SELECTION_FRAG)
                .addAttribute(ATTR_VERTEX, "vertex")
                .linkAndValidate()
                .addUniform(UNIFORM_PROJECTION, "projection")
                .addUniform(UNIFORM_MODELVIEW, "modelView")
                .addUniform(UNIFORM_SELECTION_CODE, "selectionCode")
                .errorCheck()
                .build();

        faceSelectionShader = new ShaderProgram.Builder()
                .createProgram(SHADER_FACE_SELECTION_VERT, SHADER_FACE_SELECTION_FRAG)
                .addAttribute(ATTR_VERTEX, "vertex")
                .addAttribute(ATTR_SELECTION_CODE, "selectionCode")
                .linkAndValidate()
                .addUniform(UNIFORM_PROJECTION, "projection")
                .addUniform(UNIFORM_MODELVIEW, "modelView")
                .errorCheck()
                .build();

        billboardShader = new ShaderProgram.Builder()
                .createProgram(SHADER_BILLBOARD_VERT, SHADER_BILLBOARD_FRAG)
                .addAttribute(ATTR_VERTEX, "vertex")
                .linkAndValidate()
                .addUniform(UNIFORM_PROJECTION, "projection")
                .addUniform(UNIFORM_MODELVIEW, "modelView")
                .addUniform(UNIFORM_TEXUNIT, "texUnit")
                .addUniform(UNIFORM_AMBIENT_COLOUR, "lightAmbientColour")
                .addUniform(UNIFORM_AMBIENT_INTENSITY, "lightAmbientIntensity")
                .addUniform(UNIFORM_DIFFUSE_COLOUR, "lightDiffuseColour")
                .addUniform(UNIFORM_DIFFUSE_INTENSITY, "lightDiffuseIntensity")
                .addUniform(UNIFORM_SCALE, "scale")
                .errorCheck()
                .build();

        billboardSelectionShader = new ShaderProgram.Builder()
                .createProgram(SHADER_BILLBOARD_SELECTION_VERT, SHADER_BILLBOARD_SELECTION_FRAG)
                .addAttribute(ATTR_VERTEX, "vertex")
                .linkAndValidate()
                .addUniform(UNIFORM_PROJECTION, "projection")
                .addUniform(UNIFORM_MODELVIEW, "modelView")
                .addUniform(UNIFORM_TEXUNIT, "texUnit")
                .addUniform(UNIFORM_SELECTION_CODE, "selectionCode")
                .addUniform(UNIFORM_SCALE, "scale")
                .errorCheck()
                .build();
    }

}
