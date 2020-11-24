package com.example.myfirstapplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES31;
import android.util.Log;

public class ESShader {
    //
    ///
    /// \brief Read a shader source into a String
    /// \param context Application context
    /// \param fileName Name of shader file
    /// \return A String object containing shader source, otherwise null
    //
    private static String readShader(Context context, String fileName) {
        String shaderSource = null;

        if (fileName == null) {
            return shaderSource;
        }

        // Read the shader file from assets
        InputStream is = null;
        byte[] buffer;

        try {
            is = context.getAssets().open(fileName);

            // Create a buffer that has the same size as the InputStream
            buffer = new byte[is.available()];

            // Read the text file as a stream, into the buffer
            is.read(buffer);

            ByteArrayOutputStream os = new ByteArrayOutputStream();

            // Write this buffer to the output stream
            os.write(buffer);

            // Close input and output streams
            os.close();
            is.close();

            shaderSource = os.toString();
        } catch (IOException ioe) {
            is = null;
        }

        if (is == null) {
            return shaderSource;
        }

        return shaderSource;
    }

    public static int loadShader(int type, String shaderSrc) {
        int shader;
        int[] compiled = new int[1];

        // Create the shader object
        shader = GLES31.glCreateShader(type);

        if (shader == 0) {
            return 0;
        }

        // Load the shader source
        GLES31.glShaderSource(shader, shaderSrc);

        // Compile the shader
        GLES31.glCompileShader(shader);

        // Check the compile status
        GLES31.glGetShaderiv(shader, GLES31.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
            Log.e("ESShader", GLES31.glGetShaderInfoLog(shader));
            GLES31.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    //
    ///
    /// \brief Load a vertex and fragment shader, create a program object, link
    ///    program.
    /// Errors output to log.
    /// \param vertShaderSrc Vertex shader source code
    /// \param fragShaderSrc Fragment shader source code
    /// \return A new program object linked with the vertex/fragment shader
    ///    pair, 0 on failure
    //
    public static int loadProgram(String vertShaderSrc, String fragShaderSrc) {
        int vertexShader;
        int fragmentShader;
        int programObject;
        int[] linked = new int[1];

        // Load the vertex/fragment shaders
        vertexShader = loadShader(GLES31.GL_VERTEX_SHADER, vertShaderSrc);

        if (vertexShader == 0) {
            return 0;
        }

        fragmentShader = loadShader(GLES31.GL_FRAGMENT_SHADER, fragShaderSrc);

        if (fragmentShader == 0) {
            GLES31.glDeleteShader(vertexShader);
            return 0;
        }

        // Create the program object
        programObject = GLES31.glCreateProgram();

        if (programObject == 0) {
            return 0;
        }


        GLES31.glAttachShader(programObject, vertexShader);
        GLES31.glAttachShader(programObject, fragmentShader);

        // Link the program
        GLES31.glLinkProgram(programObject);

        // Check the link status
        GLES31.glGetProgramiv(programObject, GLES31.GL_LINK_STATUS, linked, 0);

        if (linked[0] == 0) {
            Log.e("ESShader", "Error linking program:");
            Log.e("ESShader", GLES31.glGetProgramInfoLog(programObject));
            GLES31.glDeleteProgram(programObject);
            return 0;
        }

        // Free up no longer needed shader resources
        GLES31.glDeleteShader(vertexShader);
        GLES31.glDeleteShader(fragmentShader);

        return programObject;
    }

    public static int loadComputeShader(String computeShaderSrc)
    {
        int programObject = GLES31.glCreateProgram();
        int mComputeShader = GLES31.glCreateShader(GLES31.GL_COMPUTE_SHADER);
        GLES31.glShaderSource(mComputeShader, computeShaderSrc);
        GLES31.glCompileShader(mComputeShader);

        IntBuffer StatusBuffer = IntBuffer.allocate(1);
        GLES31.glGetShaderiv(mComputeShader, GLES31.GL_COMPILE_STATUS, StatusBuffer);

        if(StatusBuffer.get(0) != 0)
        {
            Log.e("ESShader", "Compute Shader Compile Error:");
            Log.e("ESShader", GLES31.glGetShaderInfoLog(mComputeShader));

            return -1;
        }

        GLES31.glAttachShader(programObject, mComputeShader);
        GLES31.glLinkProgram(programObject);
        GLES31.glGetProgramiv(programObject, GLES31.GL_LINK_STATUS, StatusBuffer);
        if(StatusBuffer.get(0) != 0)
        {
            Log.e("ESShader", "Compute Shader Link Error:");
            Log.e("ESShader", GLES31.glGetProgramInfoLog(programObject));

            return -1;
        }
        return programObject;
    }

    public static int loadProgramFromAsset ( Context context, String vertexShaderFileName, String fragShaderFileName )
    {
        int vertexShader;
        int fragmentShader;
        int programObject;
        int[] linked = new int[1];

        String vertShaderSrc = null;
        String fragShaderSrc = null;

        // Read vertex shader from assets
        vertShaderSrc = readShader ( context, vertexShaderFileName );

        if ( vertShaderSrc == null )
        {
            return 0;
        }

        // Read fragment shader from assets
        fragShaderSrc = readShader ( context, fragShaderFileName );

        if ( fragShaderSrc == null )
        {
            return 0;
        }

        // Load the vertex shader
        vertexShader = loadShader ( GLES31.GL_VERTEX_SHADER, vertShaderSrc );

        if ( vertexShader == 0 )
        {
            return 0;
        }

        // Load the fragment shader
        fragmentShader = loadShader ( GLES31.GL_FRAGMENT_SHADER, fragShaderSrc );

        if ( fragmentShader == 0 )
        {
            GLES31.glDeleteShader ( vertexShader );
            return 0;
        }

        // Create the program object
        programObject = GLES31.glCreateProgram();

        if ( programObject == 0 )
        {
            return 0;
        }

        GLES31.glAttachShader ( programObject, vertexShader );
        GLES31.glAttachShader ( programObject, fragmentShader );

        // Link the program
        GLES31.glLinkProgram ( programObject );

        // Check the link status
        GLES31.glGetProgramiv ( programObject, GLES31.GL_LINK_STATUS, linked, 0 );

        if ( linked[0] == 0 )
        {
            Log.e ( "ESShader", "Error linking program:" );
            Log.e ( "ESShader", GLES31.glGetProgramInfoLog ( programObject ) );
            GLES31.glDeleteProgram ( programObject );
            return 0;
        }

        // Free up no longer needed shader resources
        GLES31.glDeleteShader ( vertexShader );
        GLES31.glDeleteShader ( fragmentShader );

        return programObject;
    }
}