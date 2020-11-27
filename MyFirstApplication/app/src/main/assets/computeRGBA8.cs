#version 310 es
layout (local_size_x = 8, local_size_y = 8, local_size_z = 1) in;

layout(binding = 0, rgba8) readonly uniform highp image2D input_image;
layout(binding = 1, rgba8) writeonly uniform highp image2D output_image8;

shared vec4 scanline[8][8];

void main(void)
{
    ivec2 pos = ivec2(gl_GlobalInvocationID.xy);
    scanline[pos.x][pos.y] = imageLoad(input_image, pos);
    barrier();
    vec4 data = scanline[pos.x][pos.y];
    //data.r = data.r + v[999] ;
    data.r = data.r + 0.4;
    data.g = data.g;
    data.b = data.b + 0.3;
    data.a = data.a;

    imageStore(output_image8, pos.xy, data);
}