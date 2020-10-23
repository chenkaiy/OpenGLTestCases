package com.example.myfirstapplication;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.EGL14.EGL_ALPHA_SIZE;
import static android.opengl.EGL14.EGL_BLUE_SIZE;
import static android.opengl.EGL14.EGL_BUFFER_SIZE;
import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;
import static android.opengl.EGL14.EGL_DEFAULT_DISPLAY;
import static android.opengl.EGL14.EGL_GREEN_SIZE;
import static android.opengl.EGL14.EGL_NONE;
import static android.opengl.EGL14.EGL_NO_CONTEXT;
import static android.opengl.EGL14.EGL_NO_DISPLAY;
import static android.opengl.EGL14.EGL_OPENGL_ES2_BIT;
import static android.opengl.EGL14.EGL_RED_SIZE;
import static android.opengl.EGL14.EGL_RENDERABLE_TYPE;
import static android.opengl.EGL14.EGL_SURFACE_TYPE;
import static android.opengl.EGL14.EGL_WINDOW_BIT;
import static android.opengl.EGL14.eglChooseConfig;
import static android.opengl.EGL14.eglCreateContext;
import static android.opengl.EGL14.eglGetDisplay;
import static android.opengl.EGL14.eglGetError;
import static android.opengl.EGL14.eglInitialize;
import static android.opengl.GLES20.glGenTextures;

public class DemoRender implements GLSurfaceView.Renderer {
    private static final float[] vertices = {
            0.0f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f};

    private static final float[] verticeColors = {
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };

    public static final float[] VERTEX = {
            0.0f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f};

    public static final float[] TEXTURE_COORD = {
            0.5f,0.0f,
            0.0f,1.0f,
            1.0f,1.0f
    };

    private FloatBuffer verticesBuffer;
    private FloatBuffer verticeColorsBuffer;

    private static int BYTES_PER_FLOAT = 4;
    private String TAG = "ShaderCompile";
    private int program;
    private EGLDisplay eglDisplay;
    private EGLConfig eglConfig;
    private EGLContext eglContext;

    private static final String vertextShaderSource =
            "#version 300 es\n"
                    + "layout (location = 0) in vec4 vPosition;\n"
                    + "layout (location = 1) in vec2 a_texCoord; \n"
                    + "layout (location = 2) in vec4 aColor;\n"

                    + "out vec4 vColor;\n"
                    + "out vec2 v_texCoord; \n"

                    + "void main()\n"
                    + "{\n"
                    + "    gl_Position = vPosition;\n"
                    + "    vColor = aColor;\n"
                    + "}\n";

    private static final String fragmentShaderSource =
            "#version 300 es		 			          	        \n"
                    + "precision mediump float;					  	\n"
                    + "out vec4 fragColor;	 			 		  	\n"
                    + "in vec4 vColor;					  	        \n"
                    + "in vec2 v_texCoord;                          \n"
                    + "uniform sampler2D s_texture;                 \n"

                    + "void main()                                  \n"
                    + "{                                            \n"
                    + "  fragColor = vColor;	\n"
                    + "}                                            \n";

    @Override
    public void onSurfaceCreated(GL10 var1, javax.microedition.khronos.egl.EGLConfig var2) {
        //获取顶点着色器
        int vertextShader = loadShader(GLES31.GL_VERTEX_SHADER, vertextShaderSource);
        //获取片段着色器
        int fragmentShader =loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderSource);
        //创建程序
        int tmpProgram = GLES31.glCreateProgram();
        if (tmpProgram == 0) return;//创建失败
        //绑定着色器到程序
        GLES31.glAttachShader(tmpProgram, vertextShader);
        GLES31.glAttachShader(tmpProgram, fragmentShader);

        //绑定属性位置 vPosition ：0 着色器中没有设定属性位置时使用
//        GLES31.glBindAttribLocation(tmpProgram, 0, "vPosition");

        //连接程序
        GLES31.glLinkProgram(tmpProgram);
        //检查连接状态
        int[] linked = new int[1];
        GLES31.glGetProgramiv(tmpProgram,GLES31.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0){
            Log.e(TAG, "tmpProgram linked error");
            Log.e(TAG, GLES31.glGetProgramInfoLog(tmpProgram));
            GLES31.glDeleteProgram(tmpProgram);
            return;//连接失败
        }
        //保存程序，后面使用
        program = tmpProgram;

        //设置清除渲染时的颜色
        GLES31.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置适口尺寸
        GLES31.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //擦除屏幕
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);
        //使用程序
        GLES31.glUseProgram(program);

        TriangleRender();
        //获取 vPosition 属性的位置
        int vposition = GLES31.glGetAttribLocation(program, "vPosition");
        //加载顶点数据到 vPosition 属性位置
        GLES31.glVertexAttribPointer(vposition,3,GLES31.GL_FLOAT,false,0,verticesBuffer);
        GLES31.glEnableVertexAttribArray(vposition);
        //获取 vColor 属性位置
        int vColor = GLES31.glGetAttribLocation(program, "aColor");
        GLES31.glVertexAttribPointer(vColor, 4, GLES31.GL_FLOAT, false, 0, verticeColorsBuffer);
        GLES31.glEnableVertexAttribArray(vColor);
        //绘制
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES,0,3);
    }

    private int loadShader(int type, String shaderSource) {
        //创建着色器对象
        int shader = GLES31.glCreateShader(type);
        if (shader == 0) return 0;//创建失败
        //加载着色器源
        GLES31.glShaderSource(shader, shaderSource);
        //编译着色器
        GLES31.glCompileShader(shader);
        //检查编译状态
        int[] compiled = new int[1];
        GLES31.glGetShaderiv(shader, GLES31.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
            Log.e(TAG, GLES31.glGetShaderInfoLog(shader));
            GLES31.glDeleteShader(shader);
            return 0;//编译失败
        }
        return shader;
    }

    public void TriangleRender() {
        //将顶点数据拷贝映射到 native 内存中，以便opengl能够访问
        verticesBuffer = ByteBuffer
                .allocateDirect(vertices.length * BYTES_PER_FLOAT)//直接分配 native 内存，不会被gc
                .order(ByteOrder.nativeOrder())//和本地平台保持一致的字节序（大/小头）
                .asFloatBuffer();//将底层字节映射到FloatBuffer实例，方便使用
        verticesBuffer
                .put(vertices)//将顶点拷贝到 native 内存中
                .position(0);//每次 put position 都会 + 1，

        //将顶点颜色数据拷贝映射到 native 内存中，以便opengl能够访问
        verticeColorsBuffer = ByteBuffer
                .allocateDirect(verticeColors.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        verticeColorsBuffer
                .put(verticeColors)
                .position(0);
    }
}