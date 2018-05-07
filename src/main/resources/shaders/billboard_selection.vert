#version 330

uniform mat4 projection;
uniform mat4 modelView;
uniform float scale;

layout(location = 0) in vec3 vertex;
layout(location = 4) in vec2 texCoord;

out Data {
    vec2 texCoord;
} DataOut;

void main(void) {
    // Only the origin is transformed using the modelview matrix
    gl_Position = projection * (modelView * vec4(0.0, 0.0, 0.0, 1.0) + 
            vec4(scale * vertex.x, scale * vertex.z, 0, 0.0));
    
    DataOut.texCoord = texCoord;
}
