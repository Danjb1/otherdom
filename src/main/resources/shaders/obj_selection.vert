#version 330

uniform mat4 projection;
uniform mat4 modelView;

layout(location = 0) in vec3 vertex;
layout(location = 4) in vec2 texCoord;

out Data {
    vec2 texCoord;
} DataOut;

void main() {
    gl_Position = projection * modelView * vec4(vertex, 1.0);
    DataOut.texCoord = texCoord;
}
