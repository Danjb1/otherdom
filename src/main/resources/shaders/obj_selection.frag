#version 330

uniform vec3 selectionCode;

out vec4 fragColour;

void main() {
    fragColour = vec4(selectionCode, 1.0);
}
