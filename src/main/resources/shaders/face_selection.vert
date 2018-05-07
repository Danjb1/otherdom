#version 330

uniform mat4 projection;
uniform mat4 modelView;

layout(location = 0) in vec3 vertex;
layout(location = 5) in vec3 selectionCode;

out Data {
    vec3 selectionCode;
} DataOut;

void main() {
    gl_Position = projection * modelView * vec4(vertex, 1.0);
    DataOut.selectionCode = selectionCode;
}
