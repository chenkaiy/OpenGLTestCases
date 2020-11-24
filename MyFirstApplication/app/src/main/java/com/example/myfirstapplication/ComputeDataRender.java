package com.example.myfirstapplication;

import android.content.Context;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

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

public class ComputeDataRender implements GLSurfaceView.Renderer {

    private static final String TAG = "ComputeRender";

    private Context mContext;
    private int mWidth = 32;
    private int mHeight = 32;
    private int mSize = mWidth * mHeight * 4;
    private FloatBuffer mInputBuffer;
    private int[] fTexture = new int[3];

    private int[] fFrame = new int[1];

    private int mComputeProg;
    private FloatBuffer mValueBuffer;
    private int mValueSize = 1000;

    public ComputeDataRender(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initGLSL();
        mValueBuffer = createValueBuffer();
        mInputBuffer = createInputBuffer();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        createEnvi();
        transferToTexture(mInputBuffer, fTexture[0]);
        FloatBuffer a0 = FloatBuffer.allocate(mSize);
        FloatBuffer a1 = FloatBuffer.allocate(mSize);
        FloatBuffer a2 = FloatBuffer.allocate(mSize);

        long begin = System.currentTimeMillis();

        performCompute(fTexture[0], fTexture[1]);
        performCompute(fTexture[1], fTexture[2]);

        Log.w(TAG, "total compute spent:" + (System.currentTimeMillis() - begin));
        glReadBuffer(GLES31.GL_COLOR_ATTACHMENT0);
        glReadPixels(0, 0, mWidth, mHeight, GLES31.GL_RGBA, GLES31.GL_FLOAT, a0);
        glReadBuffer(GLES31.GL_COLOR_ATTACHMENT1);
        glReadPixels(0, 0, mWidth, mHeight, GLES31.GL_RGBA, GLES31.GL_FLOAT, a1);
        glReadBuffer(GLES31.GL_COLOR_ATTACHMENT2);
        glReadPixels(0, 0, mWidth, mHeight, GLES31.GL_RGBA, GLES31.GL_FLOAT, a2);
        float[] o1 = a0.array();
        float[] o2 = a1.array();
        float[] o3 = a2.array();
    }

    private void initGLSL() {
        mComputeProg = GLES31.glCreateProgram();
        String source = ShaderUtils.loadFromAssetsFile("compute.cs", mContext.getResources());
        ShaderUtils.vglAttachShaderSource(mComputeProg, GLES31.GL_COMPUTE_SHADER, source);
        glLinkProgram(mComputeProg);
    }

    private FloatBuffer createInputBuffer() {
        FloatBuffer floatBuffer = FloatBuffer.allocate(mSize);
        for (int i = 0; i < mSize; i++) {
            floatBuffer.put(i);
        }
        floatBuffer.position(0);
        return floatBuffer;
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
        glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0,
                GLES31.GL_TEXTURE_2D, fTexture[0], 0);
        glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT1,
                GLES31.GL_TEXTURE_2D, fTexture[1], 0);
        glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT2,
                GLES31.GL_TEXTURE_2D, fTexture[2], 0);
    }

    private void performCompute(int inputTeture, int outputTexture) {
        glUseProgram(mComputeProg);
        glUniform1fv(GLES31.glGetUniformLocation(mComputeProg, "v"), mValueSize, mValueBuffer);

        glBindImageTexture(0, inputTeture, 0, false, 0, GLES31.GL_READ_ONLY, GLES31.GL_RGBA32F);
        glBindImageTexture(1, outputTexture, 0, false, 0, GLES31.GL_WRITE_ONLY, GLES31.GL_RGBA32F);

        glDispatchCompute(1, 1, 1);
        glMemoryBarrier(GLES31.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GLES31.GL_PIXEL_BUFFER_BARRIER_BIT);
    }

    private void transferFromTexture(Buffer data) {
        glReadBuffer(GLES31.GL_COLOR_ATTACHMENT2);
        glReadPixels(0, 0, mWidth, mHeight, GLES31.GL_RGBA, GLES31.GL_FLOAT, data);
    }

    private void transferToTexture(Buffer data, int texID) {
        glBindTexture(GLES31.GL_TEXTURE_2D, texID);
        glTexSubImage2D(GLES31.GL_TEXTURE_2D, 0, 0, 0, mWidth, mHeight, GLES31.GL_RGBA, GLES31.GL_FLOAT, data);
    }
}
