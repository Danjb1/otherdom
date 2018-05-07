#version 330

uniform sampler2D texUnit;

in Data {
    vec4 colour;
    vec2 texCoord;
} DataIn;

out vec4 fragColour;

void main() {
    vec4 texColour = texture(texUnit, DataIn.texCoord);
    
    if (DataIn.colour.w == 0.0 || texColour.w == 0.0){
        // Discard transparent fragments, so they don't affect the depth buffer
        discard;
    }
    
    fragColour = DataIn.colour * texColour;
}
