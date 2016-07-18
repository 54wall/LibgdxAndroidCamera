package com.mygdx.game0606.android;

/**
 * @author  54wall 
 * @date 创建时间：2016-7-14 上午11:44:40
 * @version 1.0 
 */

import java.io.IOException;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurface extends SurfaceView implements
		SurfaceHolder.Callback {
	private Camera camera;

	public CameraSurface(Context context) {
		super(context);
		// We're implementing the Callback interface and want to get notified
		// about certain surface events.
		getHolder().addCallback(this);
		// We're changing the surface to a PUSH surface, meaning we're receiving
		// all buffer data from another component - the camera, in this case.
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD) 
	public void surfaceCreated(SurfaceHolder holder) {
		// Once the surface is created, simply open a handle to the camera
		// hardware.
//		camera = Camera.open();//old
		camera = Camera.open(0);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// This method is called when the surface changes, e.g. when it's size
		// is set.
		// We use the opportunity to initialize the camera preview display
		// dimensions.
		Camera.Parameters p = camera.getParameters();
//		p.setPreviewSize(width, height);//old 会报 setParameters failed 
		camera.setParameters(p);

		// We also assign the preview display to this surface...
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Once the surface gets destroyed, we stop the preview mode and release
		// the whole camera since we no longer need it.
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	public Camera getCamera() {
		return camera;
	}

}
