#version 310 es
precision highp float;
in vec2 v_texCoord;
layout(location = 0) out vec4 outColor;
uniform sampler2D s_texture;
void main()
{
    //outColor = texture( s_texture, v_texCoord );
    ivec2 pos = ivec2(v_texCoord.x * 32.0, v_texCoord.y * 32.0);
    outColor = texelFetch(s_texture, pos, 0);
}