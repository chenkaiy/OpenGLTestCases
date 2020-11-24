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
import android.util.Log;

public class ComputeShaderRender implements GLSurfaceView.Renderer
{

    ///
    // Constructor
    //
    public ComputeShaderRender ( Context context )
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
                        "layout(location = 1) in vec4 a_fillcolor;   \n" +
                        "out vec4 v_fillcolor;                  \n" +
                        "void main()                  				\n" +
                        "{                            				\n" +
                        "   gl_Position = a_position; 				\n" +
                        "   v_fillcolor = a_fillcolor;  			\n" +
                        "}                            				\n";

        String fShaderStr =
                "#version 310 es                                     \n" +
                        "precision mediump float;                            \n" +
                        "in vec4 v_fillcolor;                            	 \n" +
                        "layout(location = 0) out vec4 outColor;             \n" +
                        "void main()                                         \n" +
                        "{                                                   \n" +
                        "  outColor = v_fillcolor;                           \n" +
                        "}                                                   \n";

        String fComputeShader =
                "#version 310 es\n" +
                "// The uniform parameters which is passed from application for every frame.\n" +
                "layout(location = 2) uniform float radius;\n" +
                "// Declare custom data struct, which represents either vertex or colour.\n" +
                "struct Vector3f\n" +
                "{\n" +
                "      float x;\n" +
                "      float y;\n" +
                "      float z;\n" +
                "      float w;\n" +
                "};\n" +
                "\n" +
                "// Declare the custom data type, which represents one point of a circle.\n" +
                "// And this is vertex position and colour respectively.\n" +
                "// As you may already noticed that will define the interleaved data within\n" +
                "// buffer which is Vertex|Colour|Vertex|Colour|…\n" +
                "\n" +
                "struct AttribData\n" +
                "{\n" +
                "      Vector3f v;\n" +
                "      Vector3f c;\n" +
                "};\n" +
                "\n" +
                "layout(std140, binding = 0) buffer destBuffer\n" +
                "{\n" +
                "\tAttribData data[];\n" +
                "} outBuffer;\n" +
                "\n" +
                "layout (local_size_x = 8, local_size_y = 8, local_size_z = 1) in;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "\n" +
                "      // Read current global position for this thread\n" +
                "      ivec2 storePos = ivec2(gl_GlobalInvocationID.xy);\n" +
                "      // Calculate the global number of threads (size) for this\n" +
                "      uint gWidth = gl_WorkGroupSize.x * gl_NumWorkGroups.x;\n" +
                "      uint gHeight = gl_WorkGroupSize.y * gl_NumWorkGroups.y;\n" +
                "      uint gSize = gWidth * gHeight;\n" +
                "      // Since we have 1D array we need to calculate offset.\n" +
                "      uint offset = storePos.y * gWidth + storePos.x;\n" +
                "      // Calculate an angle for the current thread\n" +
                "      float alpha = 2.0 * 3.14159265359 * (float(offset) / float(gSize));\n" +
                "      // Calculate vertex position based on the already calculate angle\n" +
                "      // and radius, which is given by application\n" +
                "      outBuffer.data[offset].v.x = sin(alpha) * radius;\n" +
                "      outBuffer.data[offset].v.y = cos(alpha) * radius;\n" +
                "      outBuffer.data[offset].v.z = 0.0;\n" +
                "      outBuffer.data[offset].v.w = 1.0;\n" +
                "      // Assign colour for the vertex\n" +
                "      outBuffer.data[offset].c.x = storePos.x / float(gWidth);\n" +
                "      outBuffer.data[offset].c.y = 0.0;\n" +
                "      outBuffer.data[offset].c.z = 1.0;\n" +
                "      outBuffer.data[offset].c.w = 1.0;\n" +
                "}";

        // Load the shaders and get a linked program object
        mProgramObject = ESShader.loadProgram ( vShaderStr, fShaderStr );
        GLES31.glBindAttribLocation ( mProgramObject, iLocPosition, "a_position" );
        GLES31.glBindAttribLocation( mProgramObject, iLocFillColor, "a_fillcolor");

        iLocPosition = GLES31.glGetAttribLocation( mProgramObject, "a_position");
        iLocFillColor = GLES31.glGetAttribLocation( mProgramObject, "a_fillcolor");

        mComputeShaderProgramObject = ESShader.loadComputeShader(fComputeShader);
        int iLocRadius = GLES31.glGetUniformLocation(mComputeShaderProgramObject, "radius");

        RenderHelper.ErrorLoger();
        IntBuffer numActiveAttribs = IntBuffer.allocate(1);

        GLES31.glGetProgramiv(mProgramObject, GLES31.GL_ACTIVE_ATTRIBUTES, numActiveAttribs);
        RenderHelper.ErrorLoger();

        System.out.println("Number of Active attribute Of Normal Shader= "+ numActiveAttribs.get(0));
        // Get the sampler location
        GLES31.glGetProgramiv(mComputeShaderProgramObject, GLES31.GL_ACTIVE_ATTRIBUTES, numActiveAttribs);

        System.out.println("Number of Active attribute Of Compute Shader= "+ numActiveAttribs.get(0));
        // Load the texture
        mTextureId = createSimpleTexture2D ();

        // Compute Shader Init
        IndexBufferBinding = 0;
        mVBO = IntBuffer.allocate(1);
        GLES31.glGenBuffers(1, mVBO);


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
        GLES31.glUseProgram( mComputeShaderProgramObject);
        int iLocRadius = GLES31.glGetUniformLocation(mComputeShaderProgramObject, "radius");
        // Use the program object
        GLES31.glUniform1f(iLocRadius, (float) mFrameNumber);

        GLES31.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, IndexBufferBinding, mVBO.get(0));

        GLES31.glDispatchCompute( (NUM_VERTS_H % GROUP_SIZE_WIDTH + NUM_VERTS_H) / GROUP_SIZE_WIDTH,

                (NUM_VERTS_V % GROUP_SIZE_HEIGHT + NUM_VERTS_V) / GROUP_SIZE_HEIGHT,

                1);

        GLES31.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, IndexBufferBinding, 0);

        GLES31.glMemoryBarrier(GLES31.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);

        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, mVBO.get(0));
        GLES31.glUseProgram ( mProgramObject );

        GLES31.glEnableVertexAttribArray ( 0 );
        GLES31.glEnableVertexAttribArray ( 1 );

        GLES31.glDrawArrays(GLES31.GL_POINTS, 0, NUM_VERTS);
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
    private int mComputeShaderProgramObject;
    private int mFrameNumber;

    // Parameter location
    private int mSamplerLoc;
    private int iLocPosition = 0;
    private int iLocFillColor = 1;

    // Texture handle
    private int mTextureId;

    // Additional member variables
    private int mWidth;
    private int mHeight;
    private FloatBuffer mVertices;
    private ShortBuffer mIndices;

    // ComputeShader vbo Ids
    private IntBuffer mVBO;
    private int IndexBufferBinding;

    private int GROUP_SIZE_HEIGHT = 8;
    private int GROUP_SIZE_WIDTH = 8;

    private int NUM_VERTS_H = 16;
    private int NUM_VERTS_V = 16;

    private int NUM_VERTS = 256;

    // Error Code
    private int ErrCode;

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