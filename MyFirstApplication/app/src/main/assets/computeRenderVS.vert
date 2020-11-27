#version 310 es
precision highp float;
layout(location = 0) in vec4 a_position;
layout(location = 1) in vec2 a_texCoord;
out vec2 v_texCoord;

uniform float OffsetX;
uniform float OffsetY;
void main()
{
    vec4 finalPos = vec4(a_position.x + OffsetX, a_position.y + OffsetY, a_position.z, a_position.w);
    gl_Position = finalPos;
    v_texCoord = a_texCoord;
}