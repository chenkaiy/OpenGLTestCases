package com.example.myfirstapplication;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.content.Context;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class MainActivity extends AppCompatActivity {

    private final int CONTEXT_CLIENT_VERSION = 3;

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate ( savedInstanceState );
        mGLSurfaceView = new GLSurfaceView ( this );

        if ( detectOpenGLES31() )
        {
            // Tell the surface view we want to create an OpenGL ES 3.0-compatible
            // context, and set an OpenGL ES 3.0-compatible renderer.
            mGLSurfaceView.setEGLContextClientVersion ( CONTEXT_CLIENT_VERSION );
            //mGLSurfaceView.setRenderer ( new VertexBufferObjectRender ( this ) );
            //mGLSurfaceView.setRenderer ( new SimpleTexture2DRender ( this ) );
            //mGLSurfaceView.setRenderer ( new ComputeShaderRender( this));
            //mGLSurfaceView.setRenderer ( new SimpleTexture3DRender ( this ) );
            //mGLSurfaceView.setRenderer ( new SimpleTextureCubemapRenderer ( this ) );
            //mGLSurfaceView.setRenderer( new MipMap2DRenderer(this));
            //mGLSurfaceView.setRenderer( new HelloTriangleRenderer(this));
            mGLSurfaceView.setRenderer ( new ComputeRender( this));
        }
        else
        {
            Log.e ( "HelloTriangle", "OpenGL ES 3.0 not supported on device.  Exiting..." );
            finish();

        }

        setContentView ( mGLSurfaceView );
    }

    private boolean detectOpenGLES31()
    {
        ActivityManager am = ( ActivityManager ) getSystemService ( Context.ACTIVITY_SERVICE );
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return ( info.reqGlEsVersion >= 0x30000 );
    }

    @Override
    protected void onResume()
    {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause()
    {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
    }

    private GLSurfaceView mGLSurfaceView;
}