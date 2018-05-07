#version 330

in Data {
    vec3 selectionCode;
} DataIn;

out vec4 fragColour;

void main()
{
    fragColour = vec4(DataIn.selectionCode, 1.0);
}
