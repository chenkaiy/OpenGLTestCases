package com.example.myfirstapplication;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.example.myfirstapplication.ESShader;

import android.content.Context;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
public class SimpleTexture2DRender implements GLSurfaceView.Renderer
{

    ///
    // Constructor
    //
    public SimpleTexture2DRender ( Context context )
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
    private int createSimpleTexture2D( )
    {
        // Texture object handle
        int[] textureId = new int[1];

        // 2x2 Image, 3 bytes per pixel (R, G, B)
        byte[] pixels =
                {
                        ( byte ) 0xff,   0,   0, // Red
                        0, ( byte ) 0xff,   0, // Green
                        0,   0, ( byte ) 0xff, // Blue
                        ( byte ) 0xff, ( byte ) 0xff,   0 // Yellow
                };
        ByteBuffer pixelBuffer = ByteBuffer.allocateDirect ( 4 * 3 );
        pixelBuffer.put ( pixels ).position ( 0 );

        // Use tightly packed data
        GLES31.glPixelStorei ( GLES31.GL_UNPACK_ALIGNMENT, 1 );

        //  Generate a texture object
        GLES31.glGenTextures ( 1, textureId, 0 );

        // Bind the texture object
        GLES31.glBindTexture ( GLES31.GL_TEXTURE_2D, textureId[0] );

        //  Load the texture
        GLES31.glTexImage2D ( GLES31.GL_TEXTURE_2D, 0, GLES31.GL_RGB, 2, 2, 0, GLES31.GL_RGB, GLES31.GL_UNSIGNED_BYTE, pixelBuffer );

        // Set the filtering mode
        GLES31.glTexParameteri ( GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_NEAREST );
        GLES31.glTexParameteri ( GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_NEAREST );

        return textureId[0];
    }

    ///
    // Initialize the shader and program object
    //
    public void onSurfaceCreated ( GL10 glUnused, EGLConfig config )
    {
        String vShaderStr =
                "#version 310 es              				\n" +
                        "layout(location = 0) in vec4 a_position;   \n" +
                        "layout(location = 1) in vec2 a_texCoord;   \n" +
                        "out vec2 v_texCoord;     	  				\n" +
                        "void main()                  				\n" +
                        "{                            				\n" +
                        "   gl_Position = a_position; 				\n" +
                        "   v_texCoord = a_texCoord;  				\n" +
                        "}                            				\n";

        String fShaderStr =
                "#version 310 es                                     \n" +
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

        IntBuffer numActiveAttribs = IntBuffer.allocate(1);

        GLES31.glGetProgramiv(mProgramObject, GLES31.GL_ACTIVE_ATTRIBUTES, numActiveAttribs);

        System.out.println("Number of Active attribute = "+ numActiveAttribs.get(0));
        // Get the sampler location
        mSamplerLoc = GLES31.glGetUniformLocation ( mProgramObject, "s_texture" );

        // Load the texture
        mTextureId = createSimpleTexture2D ();

        GLES31.glClearColor ( 1.0f, 1.0f, 1.0f, 0.0f );
    }

    // /
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
        GLES31.glVertexAttribPointer ( 0, 3, GLES31.GL_FLOAT,
                false,
                5 * 4, mVertices );
        // Load the texture coordinate
        mVertices.position ( 3 );
        GLES31.glVertexAttribPointer ( 1, 2, GLES31.GL_FLOAT,
                false,
                5 * 4,
                mVertices );

        GLES31.glEnableVertexAttribArray ( 0 );
        GLES31.glEnableVertexAttribArray ( 1 );

        // Bind the texture
        GLES31.glActiveTexture ( GLES31.GL_TEXTURE0 );
        GLES31.glBindTexture ( GLES31.GL_TEXTURE_2D, mTextureId );

        // Set the sampler texture unit to 0
        GLES31.glUniform1i ( mSamplerLoc, 0 );

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


    // Handle to a program object
    private int mProgramObject;

    // Sampler location
    private int mSamplerLoc;

    // Texture handle
    private int mTextureId;

    // Additional member variables
    private int mWidth;
    private int mHeight;
    private FloatBuffer mVertices;
    private ShortBuffer mIndices;

    private final float[] mVerticesData =
            {
                    -0.5f, 0.5f, 0.0f, // Position 0
                    0.0f, 0.0f, // TexCoord 0
                    -0.5f, -0.5f, 0.0f, // Position 1
                    0.0f, 1.0f, // TexCoord 1
                    0.5f, -0.5f, 0.0f, // Position 2
                    1.0f, 1.0f, // TexCoord 2
                    0.5f, 0.5f, 0.0f, // Position 3
                    1.0f, 0.0f // TexCoord 3
            };

    private final short[] mIndicesData =
            {
                    0, 1, 2, 0, 2, 3
            };

}