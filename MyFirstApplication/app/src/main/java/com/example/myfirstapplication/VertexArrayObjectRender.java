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

public class VertexArrayObjectRender {
    // Handle to a program object
    private int mProgramObject;

    // Additional member variables
    private int mWidth;
    private int mHeight;
    private FloatBuffer mVertices;
    private ShortBuffer mIndices;

    // VertexBufferObject Ids
    private int[] mVBOIds = new int[2];

    // VertexArrayObject Id
    private int[] mVAOId = new int[1];

    // 3 vertices, with (x,y,z) ,(r, g, b, a) per-vertex
    private final float[] mVerticesData =
            {
                    0.0f, 0.5f, 0.0f,        // v0
                    1.0f, 0.0f, 0.0f, 1.0f,  // c0
                    -0.5f, -0.5f, 0.0f,        // v1
                    0.0f, 1.0f, 0.0f, 1.0f,  // c1
                    0.5f, -0.5f, 0.0f,        // v2
                    0.0f, 0.0f, 1.0f, 1.0f,  // c2
            };

    private final short[] mIndicesData =
            {
                    0, 1, 2
            };

    final int VERTEX_POS_SIZE = 3; // x, y and z
    final int VERTEX_COLOR_SIZE = 4; // r, g, b, and a

    final int VERTEX_POS_INDX = 0;
    final int VERTEX_COLOR_INDX = 1;

    final int VERTEX_STRIDE = (4 * (VERTEX_POS_SIZE + VERTEX_COLOR_SIZE));

    // Constructor
    //
    public VertexArrayObjectRender(Context context) {
        mVertices = ByteBuffer.allocateDirect(mVerticesData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(mVerticesData).position(0);

        mIndices = ByteBuffer.allocateDirect(mIndicesData.length * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndices.put(mIndicesData).position(0);
    }

    ///
    // Initialize the shader and program object
    //
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        String vShaderStr =
                "#version 310 es                            \n" +
                        "layout(location = 0) in vec4 a_position;   \n" +
                        "layout(location = 1) in vec4 a_color;      \n" +
                        "out vec4 v_color;                          \n" +
                        "void main()                                \n" +
                        "{                                          \n" +
                        "    v_color = a_color;                     \n" +
                        "    gl_Position = a_position;              \n" +
                        "}";


        String fShaderStr =
                "#version 310 es            \n" +
                        "precision mediump float;   \n" +
                        "in vec4 v_color;           \n" +
                        "out vec4 o_fragColor;      \n" +
                        "void main()                \n" +
                        "{                          \n" +
                        "    o_fragColor = v_color; \n" +
                        "}";

        // Load the shaders and get a linked program object
        mProgramObject = ESShader.loadProgram(vShaderStr, fShaderStr);

        // Generate VBO Ids and load the VBOs with data
        GLES31.glGenBuffers(2, mVBOIds, 0);

        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, mVBOIds[0]);

        mVertices.position(0);
        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, mVerticesData.length * 4,
                mVertices, GLES31.GL_STATIC_DRAW);

        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, mVBOIds[1]);

        mIndices.position(0);
        GLES31.glBufferData(GLES31.GL_ELEMENT_ARRAY_BUFFER, 2 * mIndicesData.length,
                mIndices, GLES31.GL_STATIC_DRAW);

        // Generate VAO Id
        GLES31.glGenVertexArrays(1, mVAOId, 0);
        // Bind the VAO and then setup the vertex
        // attributes
        GLES31.glBindVertexArray(mVAOId[0]);

        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, mVBOIds[0]);
        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, mVBOIds[1]);

        GLES31.glEnableVertexAttribArray(VERTEX_POS_INDX);
        GLES31.glEnableVertexAttribArray(VERTEX_COLOR_INDX);

        GLES31.glVertexAttribPointer(VERTEX_POS_INDX, VERTEX_POS_SIZE,
                GLES31.GL_FLOAT, false, VERTEX_STRIDE,
                0);

        GLES31.glVertexAttribPointer(VERTEX_COLOR_INDX, VERTEX_COLOR_SIZE,
                GLES31.GL_FLOAT, false, VERTEX_STRIDE,
                (VERTEX_POS_SIZE * 4));

        // Reset to the default VAO
        GLES31.glBindVertexArray(0);

        GLES31.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
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

        // Bind the VAO
        GLES31.glBindVertexArray ( mVAOId[0] );

        // Draw with the VAO settings
        GLES31.glDrawElements ( GLES31.GL_TRIANGLES, mIndicesData.length, GLES31.GL_UNSIGNED_SHORT, 0 );

        // Return to the default VAO
        GLES31.glBindVertexArray ( 0 );
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
