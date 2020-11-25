package com.example.myfirstapplication;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES31.glBindTexture;
import static android.opengl.GLES31.glBindFramebuffer;
import static android.opengl.GLES31.glFramebufferTexture2D;
import static android.opengl.GLES31.glGenFramebuffers;
import static android.opengl.GLES31.glGenTextures;
import static android.opengl.GLES31.glLinkProgram;
import static android.opengl.GLES31.glTexSubImage2D;
import static android.opengl.GLES31.glUniform1fv;
import static android.opengl.GLES31.glUseProgram;
import static android.opengl.GLES31.glTexStorage2D;
import static android.opengl.GLES31.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES31.GL_LINEAR;
import static android.opengl.GLES31.GL_TEXTURE_2D;
import static android.opengl.GLES31.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES31.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES31.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES31.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES31.glBindImageTexture;
import static android.opengl.GLES31.glDispatchCompute;
import static android.opengl.GLES31.glMemoryBarrier;
import static android.opengl.GLES31.glReadPixels;
import static android.opengl.GLES31.glTexParameteri;
import static android.opengl.GLES31.glReadBuffer;

public class ComputeRender implements GLSurfaceView.Renderer {
    private static final String TAG = "ComputeRender";

    private Context mContext;
    private int mWidth = 32;
    private int mHeight = 32;
    private int mSize = mWidth * mHeight * 4;
    private FloatBuffer mInputBuffer;

    private int mViewWidth;
    private int mViewHeight;
    // VB and IB
    private FloatBuffer mVertices;
    private ShortBuffer mIndices;

    // Sampler location
    private int mSamplerLoc;

    private final float[] mVerticesData =
            {
                    -0.25f, 0.25f, 0.0f, // Position 0
                    0.0f, 0.0f, // TexCoord 0
                    -0.25f, -0.25f, 0.0f, // Position 1
                    0.0f, 1.0f, // TexCoord 1
                    0.25f, -0.25f, 0.0f, // Position 2
                    1.0f, 1.0f, // TexCoord 2
                    0.25f, 0.25f, 0.0f, // Position 3
                    1.0f, 0.0f // TexCoord 3
            };

    private PointF[] posOffset;

    private final short[] mIndicesData =
            {
                    0, 1, 2, 0, 2, 3
            };

    private int[] fTexture = new int[3];

    private int[] fTextureRGBA8 = new int[1];

    private int[] fFrame = new int[1];

    private int mComputeProg;
    private int mVSPSProg;


    private FloatBuffer mValueBuffer;
    private int mValueSize = 1000;

    public ComputeRender(Context context) {

        mContext = context;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;

        setPosOffset();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        createEnvi();
        transferToTexture(mInputBuffer, fTexture[0]);
        FloatBuffer a0 = FloatBuffer.allocate(mSize);
        FloatBuffer a1 = FloatBuffer.allocate(mSize);
        FloatBuffer a2 = FloatBuffer.allocate(mSize);

        long begin = System.currentTimeMillis();

        performCompute(fTexture[0], fTexture[1], fTextureRGBA8[0]);
        //performCompute(fTexture[1], fTexture[2]);

        //Log.w(TAG, "total compute spent:" + (System.currentTimeMillis() - begin));
        //glReadBuffer(GLES31.GL_COLOR_ATTACHMENT0);
        //glReadPixels(0, 0, mWidth, mHeight, GLES31.GL_RGBA, GLES31.GL_FLOAT, a0);
        //glReadBuffer(GLES31.GL_COLOR_ATTACHMENT1);
        //glReadPixels(0, 0, mWidth, mHeight, GLES31.GL_RGBA, GLES31.GL_FLOAT, a1);
        //glReadBuffer(GLES31.GL_COLOR_ATTACHMENT2);
        //glReadPixels(0, 0, mWidth, mHeight, GLES31.GL_RGBA, GLES31.GL_FLOAT, a2);
        //float[] o1 = a0.array();
        //float[] o2 = a1.array();
        //float[] o3 = a2.array();
        //destroyEnvi();
        performRendering();
    }

    private void initResources()
    {
        mVertices = ByteBuffer.allocateDirect ( mVerticesData.length * 4 )
                .order ( ByteOrder.nativeOrder() ).asFloatBuffer();
        mVertices.put ( mVerticesData ).position ( 0 );
        mIndices = ByteBuffer.allocateDirect ( mIndicesData.length * 2 )
                .order ( ByteOrder.nativeOrder() ).asShortBuffer();
        mIndices.put ( mIndicesData ).position ( 0 );

        mValueBuffer = createValueBuffer();
        mInputBuffer = createInputBuffer();
    }

    private void initGLSL() {
        mComputeProg = GLES31.glCreateProgram();
        String source = ShaderUtils.loadFromAssetsFile("compute.cs", mContext.getResources());
        ShaderUtils.vglAttachShaderSource(mComputeProg, GLES31.GL_COMPUTE_SHADER, source);
        glLinkProgram(mComputeProg);

        mVSPSProg = ESShader.loadProgramFromAsset(mContext,"computeRenderVS.vert", "computeRenderPS.frag");
        mSamplerLoc = GLES31.glGetUniformLocation (mVSPSProg, "s_texture" );
    }

    private FloatBuffer createInputBuffer() {
        FloatBuffer floatBuffer = FloatBuffer.allocate(mSize);
        for (int i = 0; i < mSize; i++) {
            floatBuffer.put((float)i / mSize);
        }
        floatBuffer.position(0);
        return floatBuffer;
    }

    private void setPosOffset()
    {
        int Width = 3;
        int Height = 3;
        posOffset = new PointF[Height * Width];
        float Offset = 0.6f;
        float GlobalOffsetX = 0.5f;
        float GlobalOffsetY = 0.5f;
        int counter = 0;
        for(int i = 0; i < Width; i++)
        {
            for(int j = 0; j < Height; j++)
            {
                posOffset[counter] = new PointF( -GlobalOffsetX + i * Offset, -GlobalOffsetX + j * Offset);
                counter++;
            }
        }
    }

    private FloatBuffer createValueBuffer() {
        FloatBuffer floatBuffer = FloatBuffer.allocate(mValueSize);
        for (int i = 0; i < mValueSize; i++) {
            floatBuffer.put(i);
        }
        floatBuffer.position(0);
        return floatBuffer;
    }

    public void createEnvi() {
        glGenFramebuffers(1, fFrame, 0);
        glBindFramebuffer(GLES31.GL_FRAMEBUFFER, fFrame[0]);

        glGenTextures(3, fTexture, 0);
        for (int i = 0; i < 3; i++) {
            glBindTexture(GLES31.GL_TEXTURE_2D, fTexture[i]);
            glTexStorage2D(GLES31.GL_TEXTURE_2D, 1, GLES31.GL_RGBA32F, mWidth, mHeight);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            GLES31.glBindTexture(GL_TEXTURE_2D, 0);
        }

        glGenTextures(1, fTextureRGBA8, 0);
        glBindTexture(GLES31.GL_TEXTURE_2D, fTextureRGBA8[0]);
        glTexStorage2D(GLES31.GL_TEXTURE_2D, 1, GLES31.GL_RGBA8, mWidth, mHeight);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        GLES31.glBindTexture(GL_TEXTURE_2D, 0);

        glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0,
                GLES31.GL_TEXTURE_2D, fTexture[0], 0);
        glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT1,
                GLES31.GL_TEXTURE_2D, fTexture[1], 0);
        glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT2,
                GLES31.GL_TEXTURE_2D, fTexture[2], 0);
    }

    public void destroyEnvi(){
        glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0,
                GLES31.GL_TEXTURE_2D, 0, 0);
        glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT1,
                GLES31.GL_TEXTURE_2D, 0, 0);
        glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT2,
                GLES31.GL_TEXTURE_2D, 0, 0);
    }

    private void performCompute(int inputTeture, int outputTexture1, int outputTexture2) {
        glUseProgram(mComputeProg);
        glUniform1fv(GLES31.glGetUniformLocation(mComputeProg, "v"), mValueSize, mValueBuffer);

        glBindImageTexture(0, inputTeture, 0, false, 0, GLES31.GL_READ_ONLY, GLES31.GL_RGBA32F);
        glBindImageTexture(1, outputTexture1, 0, false, 0, GLES31.GL_WRITE_ONLY, GLES31.GL_RGBA32F);
        glBindImageTexture(2, outputTexture2, 0, false, 0, GLES31.GL_WRITE_ONLY, GLES31.GL_RGBA8);

        glDispatchCompute(1, 1, 1);
        glMemoryBarrier(GLES31.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
    }

    private void performRendering()
    {
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0);

        // GLES31.glMemoryBarrier(GLES31.GL_ALL_BARRIER_BITS);
        // Set the viewport
        GLES31.glViewport ( 0, 0, mViewWidth, mViewHeight );
        // Clear the color buffer
        GLES31.glClear ( GLES31.GL_COLOR_BUFFER_BIT );
        // Use the program object

        GLES31.glUseProgram ( mVSPSProg );


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

        GLES31.glActiveTexture ( GLES31.GL_TEXTURE0 );
        GLES31.glBindTexture ( GLES31.GL_TEXTURE_2D, fTexture[1]);

        GLES31.glUniform1f(GLES31.glGetUniformLocation(mVSPSProg, "OffsetX"), posOffset[0].x);
        GLES31.glUniform1f(GLES31.glGetUniformLocation(mVSPSProg, "OffsetY"), posOffset[0].y);

        GLES31.glUniform1f(mSamplerLoc, 0);

        GLES31.glDrawElements ( GLES31.GL_TRIANGLES, 6, GLES31.GL_UNSIGNED_SHORT, mIndices );

        GLES31.glUniform1f(GLES31.glGetUniformLocation(mVSPSProg, "OffsetX"), posOffset[1].x);
        GLES31.glUniform1f(GLES31.glGetUniformLocation(mVSPSProg, "OffsetY"), posOffset[1].y);
        GLES31.glActiveTexture ( GLES31.GL_TEXTURE0 );
        GLES31.glBindTexture ( GLES31.GL_TEXTURE_2D, fTextureRGBA8[0]);
        GLES31.glDrawElements ( GLES31.GL_TRIANGLES, 6, GLES31.GL_UNSIGNED_SHORT, mIndices );
    }


    private void transferFromTexture(Buffer data) {
        glReadBuffer(GLES31.GL_COLOR_ATTACHMENT2);
        glReadPixels(0, 0, mWidth, mHeight, GLES31.GL_RGBA, GLES31.GL_FLOAT, data);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initGLSL();
        initResources();
    }

    private void transferToTexture(Buffer data, int texID) {
        glBindTexture(GLES31.GL_TEXTURE_2D, texID);
        glTexSubImage2D(GLES31.GL_TEXTURE_2D, 0, 0, 0, mWidth, mHeight, GLES31.GL_RGBA, GLES31.GL_FLOAT, data);
    }
}
