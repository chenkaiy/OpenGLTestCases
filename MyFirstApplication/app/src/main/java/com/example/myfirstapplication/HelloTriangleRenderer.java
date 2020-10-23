// The MIT License (MIT)
//
// Copyright (c) 2013 Dan Ginsburg, Budirijanto Purnomo
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

//
// Book:      OpenGL(R) ES 3.0 Programming Guide, 2nd Edition
// Authors:   Dan Ginsburg, Budirijanto Purnomo, Dave Shreiner, Aaftab Munshi
// ISBN-10:   0-321-93388-5
// ISBN-13:   978-0-321-93388-1
// Publisher: Addison-Wesley Professional
// URLs:      http://www.opengles-book.com
//            http://my.safaribooksonline.com/book/animation-and-3d/9780133440133
//

// Hello_Triangle
//
//    This is a simple example that draws a single triangle with
//    a minimal vertex/fragment shader.  The purpose of this
//    example is to demonstrate the basic concepts of
//    OpenGL ES 3.0 rendering.

package com.example.myfirstapplication;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class HelloTriangleRenderer implements GLSurfaceView.Renderer
{

    ///
    // Constructor
    //
    public HelloTriangleRenderer ( Context context )
    {
        mVertices = ByteBuffer.allocateDirect ( mVerticesData.length * 4 )
                .order ( ByteOrder.nativeOrder() ).asFloatBuffer();
        mVertices.put ( mVerticesData ).position ( 0 );
    }

    ///
    // Create a shader object, load the shader source, and
    // compile the shader.
    //
    private int LoadShader ( int type, String shaderSrc )
    {
        int shader;
        int[] compiled = new int[1];

        // Create the shader object
        shader = GLES31.glCreateShader ( type );

        if ( shader == 0 )
        {
            return 0;
        }

        // Load the shader source
        GLES31.glShaderSource ( shader, shaderSrc );

        // Compile the shader
        GLES31.glCompileShader ( shader );

        // Check the compile status
        GLES31.glGetShaderiv ( shader, GLES31.GL_COMPILE_STATUS, compiled, 0 );

        if ( compiled[0] == 0 )
        {
            Log.e ( TAG, GLES31.glGetShaderInfoLog ( shader ) );
            GLES31.glDeleteShader ( shader );
            return 0;
        }

        return shader;
    }

    ///
    // Initialize the shader and program object
    //
    public void onSurfaceCreated ( GL10 glUnused, EGLConfig config )
    {
        String vShaderStr =
                "#version 300 es 			  \n"
                        +   "in vec4 vPosition;           \n"
                        + "void main()                  \n"
                        + "{                            \n"
                        + "   gl_Position = vPosition;  \n"
                        + "}                            \n";

        String fShaderStr =
                "#version 300 es		 			          	\n"
                        + "precision mediump float;					  	\n"
                        + "out vec4 fragColor;	 			 		  	\n"
                        + "void main()                                  \n"
                        + "{                                            \n"
                        + "  fragColor = vec4 ( 1.0, 0.0, 0.0, 1.0 );	\n"
                        + "}                                            \n";

        int vertexShader;
        int fragmentShader;
        int programObject;
        int[] linked = new int[1];

        // Load the vertex/fragment shaders
        vertexShader = LoadShader ( GLES31.GL_VERTEX_SHADER, vShaderStr );
        fragmentShader = LoadShader ( GLES31.GL_FRAGMENT_SHADER, fShaderStr );

        // Create the program object
        programObject = GLES31.glCreateProgram();

        if ( programObject == 0 )
        {
            return;
        }

        GLES31.glAttachShader ( programObject, vertexShader );
        GLES31.glAttachShader ( programObject, fragmentShader );

        // Bind vPosition to attribute 0
        GLES31.glBindAttribLocation ( programObject, 0, "vPosition" );

        // Link the program
        GLES31.glLinkProgram ( programObject );

        // Check the link status
        GLES31.glGetProgramiv ( programObject, GLES31.GL_LINK_STATUS, linked, 0 );

        if ( linked[0] == 0 )
        {
            Log.e ( TAG, "Error linking program:" );
            Log.e ( TAG, GLES31.glGetProgramInfoLog ( programObject ) );
            GLES31.glDeleteProgram ( programObject );
            return;
        }

        // Store the program object
        mProgramObject = programObject;

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

        // Load the vertex data
        GLES31.glVertexAttribPointer ( 0, 3, GLES31.GL_FLOAT, false, 0, mVertices );
        GLES31.glEnableVertexAttribArray ( 0 );

        GLES31.glDrawArrays ( GLES31.GL_TRIANGLES, 0, 3 );
    }

    // /
    // Handle surface changes
    //
    public void onSurfaceChanged ( GL10 glUnused, int width, int height )
    {
        mWidth = width;
        mHeight = height;
    }

    // Member variables
    private int mProgramObject;
    private int mWidth;
    private int mHeight;
    private FloatBuffer mVertices;
    private static String TAG = "HelloTriangleRenderer";

    private final float[] mVerticesData =
            { 0.0f, 0.5f, 0.0f, -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f };

}
