#version 330

uniform mat4 projection;
uniform mat4 modelView;
uniform vec3 lightAmbientColour;
uniform float lightAmbientIntensity;
uniform vec3 lightDiffuseAngle;
uniform vec3 lightDiffuseColour;
uniform float lightDiffuseIntensity;

layout(location = 0) in vec3 vertex;
layout(location = 1) in vec3 vertexNormal;
layout(location = 2) in vec3 materialAmbientColour;
layout(location = 3) in vec3 materialDiffuseColour;
layout(location = 4) in vec2 texCoord;

out Data {
    vec4 colour;
    vec2 texCoord;
} DataOut;

void main(void) {
    gl_Position = projection * modelView * vec4(vertex, 1.0);
    
    vec3 ambientComponent =
            lightAmbientIntensity * lightAmbientColour * materialAmbientColour;
    ambientComponent = clamp(ambientComponent, 0.0, 1.0);
    
    // The dot product gives us a measure of how "aligned" 2 vectors are,
    // between 0 and 1. If the light direction and the vertex normal are
    // well-aligned, the vertex should appear more brightly-lit.
    float dotProduct = dot(lightDiffuseAngle, vertexNormal);
    if (dotProduct < 0){
        dotProduct = 0;
    }
    vec3 diffuseComponent = lightDiffuseIntensity * dotProduct * 
            lightDiffuseColour * materialDiffuseColour;
    diffuseComponent = clamp(diffuseComponent, 0.0, 1.0);

    vec3 colourResult = max(diffuseComponent, ambientComponent);
    DataOut.colour = vec4(colourResult, 1.0);
    DataOut.texCoord = texCoord;
}
