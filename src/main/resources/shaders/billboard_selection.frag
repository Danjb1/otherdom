#version 330

uniform vec3 selectionCode;
uniform sampler2D texUnit;

in Data {
    vec2 texCoord;
} DataIn;

out vec4 fragColour;

void main() {
    vec4 texColour = texture(texUnit, DataIn.texCoord);
    
    if (texColour.w == 0.0){
        // Discard transparent fragments
        discard;
    }

    fragColour = vec4(selectionCode, 1.0);
}
