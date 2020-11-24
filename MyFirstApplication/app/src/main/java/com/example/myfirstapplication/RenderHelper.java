package com.example.myfirstapplication;

import android.opengl.GLES31;
import android.util.Log;

public class RenderHelper {
    public static void ErrorLoger()
    {
        int ErrorCode = GLES31.glGetError();
        if(ErrorCode != GLES31.GL_NO_ERROR)
        {
            System.out.println("OpenGLES Error code " + ErrorCode);
        }
    }
}
