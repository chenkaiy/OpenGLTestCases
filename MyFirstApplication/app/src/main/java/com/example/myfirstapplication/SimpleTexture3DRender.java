package com.example.myfirstapplication;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.example.myfirstapplication.ESShader;

import android.content.Context;
import android.opengl.GLES31;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
public class SimpleTexture3DRender implements GLSurfaceView.Renderer
{
    // Handle to a program object
    private int mProgramObject;

    // Sampler location
    private int mSamplerLoc;

    // Texture handle
    private int mTexture3DId;

    // Offset location
    private int mOffsetLoc;

    private int mDepth;

    // Additional member variables
    private int mWidth;
    private int mHeight;
    private FloatBuffer mVertices;
    private ShortBuffer mIndices;

    private final float[] mVerticesData =
            {
                    -0.5f, 0.5f, 0.0f, // Position 0
                    0.0f, 0.0f, 1.0f, // TexCoord 0
                    -0.5f, -0.5f, 0.0f, // Position 1
                    0.0f, 1.0f, 1.0f,// TexCoord 1
                    0.5f, -0.5f, 0.0f, // Position 2
                    1.0f, 1.0f, 1.0f,// TexCoord 2
                    0.5f, 0.5f, 0.0f, // Position 3
                    1.0f, 0.0f, 1.0f, // TexCoord 3
            };

    private final short[] mIndicesData =
            {
                    0, 1, 2, 0, 2, 3
            };
    ///
    // Constructor
    //
    public SimpleTexture3DRender ( Context context )
    {

        mVertices = ByteBuffer.allocateDirect ( mVerticesData.length * 4 )
                .order ( ByteOrder.nativeOrder() ).asFloatBuffer();
        mVertices.put ( mVerticesData ).position ( 0 );
        mIndices = ByteBuffer.allocateDirect ( mIndicesData.length * 2 )
                .order ( ByteOrder.nativeOrder() ).asShortBuffer();
        mIndices.put ( mIndicesData ).position ( 0 );
    }

    //
    // Create a simple 2x2 texture image with four different colors
    //
    private int createSimpleTexture3D () {
        int[] textureId = new int[1];

//        byte[] pixels =
//                {
//                        (byte) 0xff, 0, 0, // Red
//                        0, (byte) 0xff, 0, // Green
//                        0, 0, (byte) 0xff, // Blue
//                        (byte) 0xff, (byte) 0xff, 0, // Yellow
//                        0, 0, (byte) 0xff,
//                        (byte) 0xff, 0, (byte) 0xff,
//                        0, (byte) 0xff, (byte) 0xff,
//                        (byte) 0xff, (byte) 0xff, (byte) 0xff
//                };

        int width = 256;
        int height = 256;
        int depth = 256;
        byte[] pixels = new byte[width * height * depth * 3];

        for(int i = 0 ; i < width; i++)
        {
            for(int j = 0 ; j < height ; j++)
            {
                for(int k = 0 ; k < depth ; k++)
                {
                    pixels[i * height * depth * 3 + j * depth * 3 + 3 * k] = (byte)(k  * 255 / depth);
                    pixels[i * height * depth * 3 + j * depth * 3 + 3 * k + 1] = (byte)(j * 255/ height) ;
                    pixels[i * height * depth * 3 + j * depth * 3 + 3 * k + 2] = (byte)(i * 255 / width);
                }
            }
        }

        int halfWidth = width / 2;
        int halfHeight = height / 2;
        byte[] updatePixel = new byte[halfWidth * halfHeight * 3];

        for(int i = 0; i < halfWidth; i++)
        {
            for(int j = 0; j < halfHeight; j++)
            {
                updatePixel[i * halfWidth * 3 + j * 3 + 0] = (byte)0x88;
                updatePixel[i * halfWidth * 3 + j * 3 + 1] = (byte)0x22;
                updatePixel[i * halfWidth * 3 + j * 3 + 2] = (byte)0xbb;
            }
        }

        ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(pixels.length);
        pixelBuffer.put(pixels).position(0);

        ByteBuffer pixelUpdateBuffer = ByteBuffer.allocateDirect(updatePixel.length);
        pixelUpdateBuffer.put(updatePixel).position(0);

        // Use tightly packed data
        GLES31.glPixelStorei(GLES31.GL_UNPACK_ALIGNMENT, 1);

        //  Generate a texture object
        GLES31.glGenTextures(1, textureId, 0);

        // Bind the texture object
        GLES31.glBindTexture(GLES31.GL_TEXTURE_3D, textureId[0]);

        //  Load the texture
        GLES31.glTexImage3D(GLES31.GL_TEXTURE_3D, 0, GLES31.GL_RGB, width, height, depth,0, GLES31.GL_RGB, GLES31.GL_UNSIGNED_BYTE, pixelBuffer);

        // Set the filtering mode
        GLES31.glTexParameteri ( GLES31.GL_TEXTURE_3D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_NEAREST );
        GLES31.glTexParameteri ( GLES31.GL_TEXTURE_3D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_NEAREST );
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_3D, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_CLAMP_TO_EDGE);
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_3D, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_CLAMP_TO_EDGE);
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_3D, GLES31.GL_TEXTURE_WRAP_R, GLES31.GL_CLAMP_TO_EDGE);

        GLES31.glTexSubImage3D(GLES31.GL_TEXTURE_3D, 0, 0, 0, 0, halfWidth, halfHeight ,1, GLES31.GL_RGB, GLES31.GL_UNSIGNED_BYTE, pixelUpdateBuffer);

        return textureId[0];
    }

    private int createSimpleTexture3DImmutable () {
        int[] textureId = new int[1];

//        byte[] pixels =
//                {
//                        (byte) 0xff, 0, 0, // Red
//                        0, (byte) 0xff, 0, // Green
//                        0, 0, (byte) 0xff, // Blue
//                        (byte) 0xff, (byte) 0xff, 0, // Yellow
//                        0, 0, (byte) 0xff,
//                        (byte) 0xff, 0, (byte) 0xff,
//                        0, (byte) 0xff, (byte) 0xff,
//                        (byte) 0xff, (byte) 0xff, (byte) 0xff
//                };



        int width = 128;
        int height = 128;
        int depth = 128;
        byte[] pixels = new byte[width * height * depth * 4];

        for(int i = 0 ; i < width; i++)
        {
            for(int j = 0 ; j < height ; j++)
            {
                for(int k = 0 ; k < depth ; k++)
                {
                    pixels[i * height * depth * 4 + j * depth * 4 + 4 * k] = (byte)(k  * 255 / depth);
                    pixels[i * height * depth * 4 + j * depth * 4 + 4 * k + 1] = (byte)(j * 255/ height) ;
                    pixels[i * height * depth * 4 + j * depth * 4 + 4 * k + 2] = (byte)(i * 255 / width);
                    pixels[i * height * depth * 4 + j * depth * 4 + 4 * k + 3] = (byte)(50);
                }
            }
        }

        int halfWidth = width / 2;
        int halfHeight = height / 2;
        byte[] updatePixel = new byte[halfWidth * halfHeight * 4];

        for(int i = 0; i < halfWidth; i++)
        {
            for(int j = 0; j < halfHeight; j++)
            {
                updatePixel[i * halfWidth * 4 + j * 4 + 0] = (byte)0x11;
                updatePixel[i * halfWidth * 4 + j * 4 + 1] = (byte)0x55;
                updatePixel[i * halfWidth * 4 + j * 4 + 2] = (byte)0x99;
                updatePixel[i * halfWidth * 4 + j * 4 + 3] = (byte)0xaa;
            }
        }

        ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(pixels.length);
        pixelBuffer.put(pixels).position(0);

        ByteBuffer pixelUpdateBuffer = ByteBuffer.allocateDirect(updatePixel.length);
        pixelUpdateBuffer.put(updatePixel).position(0);
        int errorCode = GLES31.glGetError();
        // Use tightly packed data
        GLES31.glPixelStorei(GLES31.GL_UNPACK_ALIGNMENT, 1);
        System.out.println(String.format("Error code is %d, line number is %d", (int)GLES31.glGetError(), 206));
        //  Generate a texture object
        GLES31.glGenTextures(1, textureId, 0);
        System.out.println(String.format("Error code is %d, line number is %d", (int)GLES31.glGetError(), 209));
        // Bind the texture object
        GLES31.glBindTexture(GLES31.GL_TEXTURE_3D, textureId[0]);
        System.out.println(String.format("Error code is %d, line number is %d", (int)GLES31.glGetError(), 212));
        //  Load the texture
        GLES31.glTexStorage3D(GLES31.GL_TEXTURE_3D, 1, GLES31.GL_RGBA8, width, height, depth);
        System.out.println(String.format("Error code is %d, line number is %d", (int)GLES31.glGetError(), 215));

        // Set the filtering mode
        GLES31.glTexParameteri ( GLES31.GL_TEXTURE_3D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_NEAREST );
        GLES31.glTexParameteri ( GLES31.GL_TEXTURE_3D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_NEAREST );
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_3D, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_CLAMP_TO_EDGE);
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_3D, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_CLAMP_TO_EDGE);
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_3D, GLES31.GL_TEXTURE_WRAP_R, GLES31.GL_CLAMP_TO_EDGE);
        System.out.println(String.format("Error code is %d, line number is %d", (int)GLES31.glGetError(), 223));

        GLES31.glTexSubImage3D(GLES31.GL_TEXTURE_3D, 0, 0, 0, 0, width, height , 1, GLES31.GL_RGBA, GLES31.GL_UNSIGNED_BYTE, pixelBuffer);
        GLES31.glTexSubImage3D(GLES31.GL_TEXTURE_3D, 0, 0, 0, 0, halfWidth, halfHeight , 1, GLES31.GL_RGBA, GLES31.GL_UNSIGNED_BYTE, pixelUpdateBuffer);
        System.out.println(String.format("Error code is %d, line number is %d", (int)GLES31.glGetError(), 226));

        return textureId[0];
    }

    ///
    // Initialize the shader and program object
    //
    public void onSurfaceCreated ( GL10 glUnused, EGLConfig config )
    {
        String vShaderStr =
                "#version 300 es              				\n" +
                        "uniform float u_offset;            \n" +
                        "uniform float w_depth;            \n" +
                        "layout(location = 0) in vec4 a_position;   \n" +
                        "layout(location = 1) in vec3 a_texCoord;   \n" +
                        "out vec3 v_texCoord;     	  				\n" +
                        "void main()                  				\n" +
                        "{                            				\n" +
                        "   gl_Position = a_position; 				\n" +
                        "   gl_Position.x += u_offset; 				\n" +
                        "   v_texCoord = a_texCoord;  				\n" +
                        "   v_texCoord.z = w_depth;  				\n" +
                        "}                            				\n";

        String fShaderStr =
                "#version 300 es                                     \n" +
                        "precision mediump float;                            \n" +
                        "in mediump vec3 v_texCoord;                            	 \n" +
                        "layout(location = 0) out mediump  vec4 outColor;             \n" +
                        "uniform mediump sampler3D s_texture;                        \n" +
                        "void main()                                         \n" +
                        "{                                                   \n" +
                        "  outColor = texture(s_texture, v_texCoord);       \n" +
                        //"  outColor = vec4(v_texCoord.z, 0.0, 0.0, 1.0);       \n" +
                        "}                                                   \n";


        // Load the shaders and get a linked program object
        mProgramObject = ESShader.loadProgram ( vShaderStr, fShaderStr );

        // Get the sampler location
        mSamplerLoc = GLES31.glGetUniformLocation ( mProgramObject, "s_texture" );

        // Get the offset location
        mOffsetLoc = GLES31.glGetUniformLocation ( mProgramObject, "u_offset" );

        mDepth = GLES31.glGetUniformLocation ( mProgramObject, "w_depth" );
        // Load the texture
        mTexture3DId = createSimpleTexture3DImmutable ();

        GLES31.glClearColor ( 1.0f, 1.0f, 1.0f, 0.0f );
    }
    // /
    // Draw a triangle using the shader pair created in onSurfaceCreated()
    //
    public void onDrawFrame ( GL10 glUnused ) {
        // Set the viewport
        GLES31.glViewport(0, 0, mWidth, mHeight);

        // Clear the color buffer
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);

        // Use the program object
        GLES31.glUseProgram ( mProgramObject );

        // Load the vertex position
        mVertices.position ( 0 );
        GLES31.glVertexAttribPointer ( 0, 3, GLES31.GL_FLOAT,
                false,
                6 * 4, mVertices );

        // Load the texture coordinate
        mVertices.position ( 3 );
        GLES31.glVertexAttribPointer ( 1, 3, GLES31.GL_FLOAT,
                false,
                6 * 4,
                mVertices );

        GLES31.glEnableVertexAttribArray ( 0 );
        GLES31.glEnableVertexAttribArray ( 1 );

        // Bind the texture
        GLES31.glActiveTexture ( GLES31.GL_TEXTURE0 );
        GLES31.glBindTexture ( GLES31.GL_TEXTURE_3D, mTexture3DId );

        // Set the sampler texture unit to 0
        GLES31.glUniform1i ( mSamplerLoc, 0 );
        GLES31.glUniform1f ( mOffsetLoc, -0.6f );
        GLES31.glUniform1f ( mDepth, 0.0f );
        GLES31.glDrawElements ( GLES31.GL_TRIANGLES, 6, GLES31.GL_UNSIGNED_SHORT, mIndices );

        GLES31.glUniform1f ( mOffsetLoc, 0.6f );
        GLES31.glUniform1f ( mDepth, 0.0f );
        GLES31.glDrawElements ( GLES31.GL_TRIANGLES, 6, GLES31.GL_UNSIGNED_SHORT, mIndices );
    }

    ///
    // Handle surface changes
    //
    public void onSurfaceChanged ( GL10 glUnused, int width, int height )
    {
        mWidth = width;
        mHeight = height;
    }

}
