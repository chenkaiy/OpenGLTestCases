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
import android.opengl.GLSurfaceView;

public class MipMap2DRenderer implements GLSurfaceView.Renderer
{


    // Handle to a program object
    private int mProgramObject;

    // Sampler location
    private int mSamplerLoc;

    // Offset location
    private int mOffsetLoc;

    // Texture handle
    private int mTextureId;

    // Additional member variables
    private int mWidth;
    private int mHeight;
    private FloatBuffer mVertices;
    private ShortBuffer mIndices;

    private final float[] mVerticesData =
            {
                    -0.5f,  0.5f, 0.0f, 1.5f,  // Position 0
                    0.0f,  0.0f,              // TexCoord 0
                    -0.5f, -0.5f, 0.0f, 0.75f, // Position 1
                    0.0f,  1.0f,              // TexCoord 1
                    0.5f, -0.5f, 0.0f, 0.75f, // Position 2
                    1.0f,  1.0f,              // TexCoord 2
                    0.5f,  0.5f, 0.0f, 1.5f,  // Position 3
                    1.0f,  0.0f               // TexCoord 3
            };

    private final short[] mIndicesData =
            {
                    0, 1, 2, 0, 2, 3
            };

    private Context mContext;
    ///
    // Constructor
    //
    public MipMap2DRenderer ( Context context )
    {
        mContext = context;

        mVertices = ByteBuffer.allocateDirect ( mVerticesData.length * 4 )
                .order ( ByteOrder.nativeOrder() ).asFloatBuffer();
        mVertices.put ( mVerticesData ).position ( 0 );
        mIndices = ByteBuffer.allocateDirect ( mIndicesData.length * 2 )
                .order ( ByteOrder.nativeOrder() ).asShortBuffer();
        mIndices.put ( mIndicesData ).position ( 0 );
    }

    ///
    //  From an RGB8 source image, generate the next level mipmap
    //
    private byte[] genMipMap2D ( byte[] src, int srcWidth, int srcHeight, int dstWidth, int dstHeight )
    {
        int x,
                y;
        int texelSize = 3;

        byte[] dst = new byte[texelSize * ( dstWidth ) * ( dstHeight )];

        for ( y = 0; y < dstHeight; y++ )
        {
            for ( x = 0; x < dstWidth; x++ )
            {
                int[] srcIndex = new int[4];
                float r = 0.0f,
                        g = 0.0f,
                        b = 0.0f;
                int sample;

                // Compute the offsets for 2x2 grid of pixels in previous
                // image to perform box filter
                srcIndex[0] =
                        ( ( ( y * 2 ) * srcWidth ) + ( x * 2 ) ) * texelSize;
                srcIndex[1] =
                        ( ( ( y * 2 ) * srcWidth ) + ( x * 2 + 1 ) ) * texelSize;
                srcIndex[2] =
                        ( ( ( ( y * 2 ) + 1 ) * srcWidth ) + ( x * 2 ) ) * texelSize;
                srcIndex[3] =
                        ( ( ( ( y * 2 ) + 1 ) * srcWidth ) + ( x * 2 + 1 ) ) * texelSize;

                // Sum all pixels
                for ( sample = 0; sample < 4; sample++ )
                {
                    r += src[srcIndex[sample]];
                    g += src[srcIndex[sample] + 1];
                    b += src[srcIndex[sample] + 2];
                }

                // Average results
                r /= 4.0;
                g /= 4.0;
                b /= 4.0;

                // Store resulting pixels
                dst[ ( y * ( dstWidth ) + x ) * texelSize ] = ( byte ) ( r );
                dst[ ( y * ( dstWidth ) + x ) * texelSize + 1] = ( byte ) ( g );
                dst[ ( y * ( dstWidth ) + x ) * texelSize + 2] = ( byte ) ( b );
            }
        }

        return dst;
    }

    ///
    //  Generate an RGB8 checkerboard image
    //
    private byte[] genCheckImage ( int width, int height, int checkSize )
    {
        int x,
                y;
        byte[] pixels = new byte[width * height * 3];


        for ( y = 0; y < height; y++ )
            for ( x = 0; x < width; x++ )
            {
                byte rColor = 0;
                byte bColor = 0;

                if ( ( x / checkSize ) % 2 == 0 )
                {
                    rColor = ( byte ) ( 127 * ( ( y / checkSize ) % 2 ) );
                    bColor = ( byte ) ( 127 * ( 1 - ( ( y / checkSize ) % 2 ) ) );
                }
                else
                {
                    bColor = ( byte ) ( 127 * ( ( y / checkSize ) % 2 ) );
                    rColor = ( byte ) ( 127 * ( 1 - ( ( y / checkSize ) % 2 ) ) );
                }

                pixels[ ( y * width + x ) * 3] = rColor;
                pixels[ ( y * width + x ) * 3 + 1] = 0;
                pixels[ ( y * width + x ) * 3 + 2] = bColor;
            }

        return pixels;
    }


    ///
    // Create a mipmapped 2D texture image
    //
    private int createMipMappedTexture2D( )
    {
        // Texture object handle
        int[]  textureId = new int[1];
        int    width = 256,
                height = 256;
        int    level;
        byte[] pixels;
        byte[] prevImage;
        byte[] newImage;

        pixels = genCheckImage ( width, height, 8 );

        // Generate a texture object
        GLES31.glGenTextures ( 1, textureId, 0 );

        // Bind the texture object
        GLES31.glBindTexture ( GLES31.GL_TEXTURE_2D, textureId[0] );

        // Load mipmap level 0
        ByteBuffer pixelBuffer = ByteBuffer.allocateDirect ( width * height * 3 );
        pixelBuffer.put ( pixels ).position ( 0 );

        GLES31.glTexImage2D ( GLES31.GL_TEXTURE_2D, 0, GLES31.GL_RGB, width, height,
                0, GLES31.GL_RGB, GLES31.GL_UNSIGNED_BYTE, pixelBuffer );

        level = 1;
        prevImage = pixels;

        while ( width > 1 && height > 1 )
        {
            int newWidth,
                    newHeight;

            newWidth = width / 2;

            if ( newWidth <= 0 )
            {
                newWidth = 1;
            }

            newHeight = height / 2;

            if ( newHeight <= 0 )
            {
                newHeight = 1;
            }

            // Generate the next mipmap level
            newImage = genMipMap2D ( prevImage, width, height, newWidth, newHeight );

            // Load the mipmap level
            pixelBuffer = ByteBuffer.allocateDirect ( newWidth * newHeight * 3 );
            pixelBuffer.put ( newImage ).position ( 0 );
            GLES31.glTexImage2D ( GLES31.GL_TEXTURE_2D, level, GLES31.GL_RGB,
                    newWidth, newHeight, 0, GLES31.GL_RGB,
                    GLES31.GL_UNSIGNED_BYTE, pixelBuffer );

            // Set the previous image for the next iteration
            prevImage = newImage;
            level++;

            // Half the width and height
            width = newWidth;
            height = newHeight;
        }


        // Set the filtering mode
        GLES31.glTexParameteri ( GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_NEAREST_MIPMAP_NEAREST );
        GLES31.glTexParameteri ( GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_LINEAR );

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
                        "layout(location = 0) in vec4 a_position;   \n" +
                        "layout(location = 1) in vec2 a_texCoord;   \n" +
                        "out vec2 v_texCoord;     	  				\n" +
                        "void main()                  				\n" +
                        "{                            				\n" +
                        "   gl_Position = a_position; 				\n" +
                        "   gl_Position.x += u_offset; 				\n" +
                        "   v_texCoord = a_texCoord;                \n" +
                        "}                            				\n";

        String fShaderStr =
                "#version 300 es                                     \n" +
                        "precision mediump float;                            \n" +
                        "in vec2 v_texCoord;                            	 \n" +
                        "layout(location = 0) out vec4 outColor;             \n" +
                        "uniform sampler2D s_texture;                        \n" +
                        "void main()                                         \n" +
                        "{                                                   \n" +
                        "  outColor = texture( s_texture, v_texCoord );      \n" +
                        "}                                                   \n";

        // Load the shaders and get a linked program object
        mProgramObject = ESShader.loadProgram ( vShaderStr, fShaderStr );

        // Get the sampler location
        mSamplerLoc = GLES31.glGetUniformLocation ( mProgramObject, "s_texture" );

        // Get the offset location
        mOffsetLoc = GLES31.glGetUniformLocation ( mProgramObject, "u_offset" );

        // Load the texture
        mTextureId = createMipMappedTexture2D();

        GLES31.glClearColor ( 1.0f, 1.0f, 1.0f, 0.0f );
    }

    ///
    // Draw a triangle using the shader pair created in onSurfaceCreated()
    //
    public void onDrawFrame ( GL10 glUnused )
    {
        // Set the viewport
        GLES31.glViewport ( 0, 0, mWidth, mHeight );

        // Clear the color buffer
        GLES31.glClear ( GLES31.GL_COLOR_BUFFER_BIT );

        // Use the program object
        GLES31.glUseProgram ( mProgramObject );

        // Load the vertex position
        mVertices.position ( 0 );
        GLES31.glVertexAttribPointer ( 0, 4, GLES31.GL_FLOAT,
                false,
                6 * 4, mVertices );
        // Load the texture coordinate
        mVertices.position ( 4 );
        GLES31.glVertexAttribPointer ( 1, 2, GLES31.GL_FLOAT,
                false,
                6 * 4,
                mVertices );

        GLES31.glEnableVertexAttribArray ( 0 );
        GLES31.glEnableVertexAttribArray ( 1 );

        // Bind the texture
        GLES31.glActiveTexture ( GLES31.GL_TEXTURE0 );
        GLES31.glBindTexture ( GLES31.GL_TEXTURE_2D, mTextureId );

        // Set the sampler texture unit to 0
        GLES31.glUniform1i ( mSamplerLoc, 0 );

        // Draw quad with nearest sampling
        GLES31.glTexParameteri ( GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_NEAREST );
        GLES31.glUniform1f ( mOffsetLoc, -0.6f );
        GLES31.glDrawElements ( GLES31.GL_TRIANGLES, 6, GLES31.GL_UNSIGNED_SHORT, mIndices );

        // Draw quad with trilinear filtering
        GLES31.glTexParameteri ( GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_LINEAR_MIPMAP_LINEAR );
        GLES31.glUniform1f ( mOffsetLoc, 0.6f );
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
