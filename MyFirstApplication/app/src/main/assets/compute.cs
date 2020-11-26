#version 310 es
layout (local_size_x = 8, local_size_y = 8, local_size_z = 1) in;

uniform float v[1000];
layout(binding = 0, rgba32f) readonly uniform highp image2D input_image;
layout(binding = 1, rgba32f) writeonly uniform highp image2D output_image;

layout(binding = 2, rgba8) writeonly uniform highp image2D output_image8;

shared vec4 scanline[32][32];

void main(void)
{
    ivec2 pos = ivec2(gl_GlobalInvocationID.xy);
    scanline[pos.x][pos.y] = imageLoad(input_image, pos);
    barrier();
    vec4 data = scanline[pos.x][pos.y];
    //data.r = data.r + v[999] ;
    data.r = data.r;
    data.g = data.g;
    data.b = data.b;
    data.a = data.a;
    imageStore(output_image, pos.xy, data);

    data.r = data.r + 0.1;
    data.g = data.g + 0.3;
    data.b = data.b + 0.2;
    data.a = data.a;
    imageStore(output_image8, pos.xy, data);
}