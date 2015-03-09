package com.creativedrewy.framepicapp.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * This class and some code portions in PicTakerActivity based on dgCam:
 * https://github.com/dawidgatti/dgCam
 *
 * Copyright © 2011 Dawid Gatti.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder _surfaceHolder;
    private Camera _camera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        _camera = camera;

        // set camera attribute
        _camera.setDisplayOrientation(90);
        Camera.Parameters params = _camera.getParameters();
        params.setPictureSize(1280, 760);
//        params.setPictureSize(2560, 1920);  //This is 5mp
        params.setPictureFormat(PixelFormat.JPEG);
        params.setJpegQuality(50);
        params.set("iso", "ISO800");
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        // Test
//        params.setAutoExposureLock(true);
//        params.setAutoWhiteBalanceLock(true);
//        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);


        _camera.setParameters(params);


        _surfaceHolder = getHolder();
        _surfaceHolder.addCallback(this);
        _surfaceHolder.setFixedSize(100, 100);


    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {

            // set surface attribute
            this.setFocusable(true);

            _camera.setPreviewDisplay(_surfaceHolder);
            _camera.startPreview();
        } catch (IOException e) {
            Log.d("DG_DEBUG", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //TODO: Possibly handle device rotation here, but will have to investigate

        // For Auto Focus
        _camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if(success){
                    _camera.cancelAutoFocus();
                }
            }
        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) { }
}