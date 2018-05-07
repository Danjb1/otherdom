#version 330

uniform mat4 projection;
uniform mat4 modelView;
uniform vec3 lightAmbientColour;
uniform float lightAmbientIntensity;
uniform vec3 lightDiffuseColour;
uniform float lightDiffuseIntensity;
uniform float scale;

layout(location = 0) in vec3 vertex;
layout(location = 4) in vec2 texCoord;

out Data {
    vec4 colour;
    vec2 texCoord;
} DataOut;

void main(void) {
    // Only the origin is transformed using the modelview matrix
    gl_Position = projection * (modelView * vec4(0.0, 0.0, 0.0, 1.0) + 
            vec4(scale * vertex.x, scale * vertex.z, 0, 0.0));
    
    vec3 ambientComponent = lightAmbientIntensity * lightAmbientColour;
    ambientComponent = clamp(ambientComponent, 0.0, 1.0);

    vec3 diffuseComponent = lightDiffuseIntensity * lightDiffuseColour;
    diffuseComponent = clamp(diffuseComponent, 0.0, 1.0);

    vec3 colourResult = max(diffuseComponent, ambientComponent);
    DataOut.colour = vec4(colourResult, 1.0);
    DataOut.texCoord = texCoord;
}
