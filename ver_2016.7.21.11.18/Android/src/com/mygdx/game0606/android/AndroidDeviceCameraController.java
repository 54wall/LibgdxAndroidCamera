package com.mygdx.game0606.android;

/**
 * @author  54wall 
 * @date 创建时间：2016-7-14 上午11:45:41
 * @version 1.0 
 */
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.mygdx.game0606.DeviceCameraControl;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Environment;
import android.text.Layout;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class AndroidDeviceCameraController implements DeviceCameraControl,
		Camera.PictureCallback, Camera.AutoFocusCallback {

	private static final int ONE_SECOND_IN_MILI = 1000;
	private final AndroidLauncher activity;
	private CameraSurface cameraSurface;
	private byte[] pictureData;
	private Context context;

	public AndroidDeviceCameraController(AndroidLauncher activity) {
		this.activity = activity;
	}

	@Override
	public synchronized void prepareCamera() {
//		activity.setFixedSize(960, 640);//new
//		activity.setFixedSize(480, 320);//54wall
		if (cameraSurface == null) {
			cameraSurface = new CameraSurface(activity);
		}
		/*可以控制摄像头预览窗口大小*/
//		activity.addContentView(cameraSurface, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));	
		/*activity.addContentView只能有一个否则报错：
		 * java.lang.IllegalStateException: The specified child already has a parent. 
		 * You must call removeView() on the child's parent first.
		 * 这也是在core代码中libgdx只能在render中调用AndroidDeviceCameraController中开启相机的原因*/
//		activity.addContentView(cameraSurface, new LayoutParams(960, 640));	
		/*http://blog.csdn.net/drrlalala/article/details/38332017*/	
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams    
				 (680,680);
		//设置顶部,左边布局    
//		params.gravity=Gravity.CENTER_HORIZONTAL|Gravity.RIGHT;
//		params.gravity=Gravity.LEFT|Gravity.RIGHT;
		params.rightMargin=150;//可以通过设置rightMargin控制组件的实际位置
		params.leftMargin=200;//可以通过设置rightMargin控制组件的实际位置
		params.topMargin=100;
		activity.addContentView(cameraSurface, params);
		 

	}

	@Override
	public synchronized void startPreview() {
		// ...and start previewing. From now on, the camera keeps pushing preview images to the surface.
		if (cameraSurface != null && cameraSurface.getCamera() != null) {
			cameraSurface.getCamera().startPreview();
		}
	}

	@Override
	public synchronized void stopPreview() {
		// stop previewing.
		if (cameraSurface != null) {
			ViewParent parentView = cameraSurface.getParent();
			if (parentView instanceof ViewGroup) {
				ViewGroup viewGroup = (ViewGroup) parentView;
				viewGroup.removeView(cameraSurface);
			}
			if (cameraSurface.getCamera() != null) {
				cameraSurface.getCamera().stopPreview();
			}
		}
		activity.restoreFixedSize();
	}

	public void setCameraParametersForPicture(Camera camera) {
		// Before we take the picture - we make sure all camera parameters are
		// as we like them
		// Use max resolution and auto focus
		Camera.Parameters p = camera.getParameters();
		List<Camera.Size> supportedSizes = p.getSupportedPictureSizes();
		int maxSupportedWidth = -1;
		int maxSupportedHeight = -1;
		for (Camera.Size size : supportedSizes) {
			if (size.width > maxSupportedWidth) {
				maxSupportedWidth = size.width;
				maxSupportedHeight = size.height;
			}
		}
		p.setPictureSize(maxSupportedWidth, maxSupportedHeight);
		p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		camera.setParameters(p);
	}

	@Override
	public synchronized void takePicture() {
		// the user request to take a picture - start the process by requesting
		// focus
		setCameraParametersForPicture(cameraSurface.getCamera());
		cameraSurface.getCamera().autoFocus(this);
	}

	@Override
	public synchronized void onAutoFocus(boolean success, Camera camera) {
		// Focus process finished, we now have focus (or not)
		if (success) {
			if (camera != null) {
				camera.stopPreview();				
				/*android6.0下边都是不推荐的了，具体找下相应替代语句*/
//				camera.takePicture(null, null, null, this);//old
//				camera.takePicture(null, null, null);//54wall
				/*增加三个回调函数后，可以进行拍照，并且成功保存*/
				// We now have focus take the actual picture
				camera.takePicture(shutterCallback, rawPictureCallback, jpegPictureCallback);//54wall
				/*54wall:末尾增加重新开始预览就可以继续预览图像了：http://www.xuebuyuan.com/1982434.html */
				camera.startPreview();
				
			}
		}
	}

	
	
	
	ShutterCallback shutterCallback = new ShutterCallback() {
		@Override
		public void onShutter() {
		}
	};	
	
	PictureCallback rawPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {

		}
	};
	
	PictureCallback jpegPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {
			/*可以在Android项目中中生成图片*/
//			String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
//					.toString()
//					+ File.separator
//					+ "PicTest_" + System.currentTimeMillis() + ".jpg";
//			File file = new File(fileName);
//			if (!file.getParentFile().exists()) {
//				file.getParentFile().mkdir();
//			}
//			
//			try {
//				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
//				bos.write(arg0);
//				bos.flush();
//				bos.close();		
//				
//			} catch (Exception e) {
//				
//			}
			/*54wall:因为在libgdx合成图像时，发现pictureData=null*/
			pictureData=arg0;
			
		};
	};

	@Override
	public synchronized void onPictureTaken(byte[] pictureData, Camera camera) {
		// We got the picture data - keep it
		this.pictureData = pictureData;
	}

	@Override
	public synchronized byte[] getPictureData() {
		// Give to picture data to whom ever requested it
		return pictureData;
	}

	@Override
	public void prepareCameraAsync() {
		Runnable r = new Runnable() {
			public void run() {
				prepareCamera();
			}
		};
		activity.post(r);
	}

	@Override
	public synchronized void startPreviewAsync() {
		Runnable r = new Runnable() {
			public void run() {
				startPreview();
			}
		};
		activity.post(r);
	}

	@Override
	public synchronized void stopPreviewAsync() {
		Runnable r = new Runnable() {
			public void run() {
				stopPreview();
			}
		};
		activity.post(r);
	}

	@Override
	public synchronized byte[] takePictureAsync(long timeout) {
		timeout *= ONE_SECOND_IN_MILI;
		/*pictureData会报错*/
//		pictureData = null;//old
		Runnable r = new Runnable() {
			public void run() {
				takePicture();
			}
		};
		activity.post(r);
		while (pictureData == null && timeout > 0) {
			try {
				Thread.sleep(ONE_SECOND_IN_MILI);
				timeout -= ONE_SECOND_IN_MILI;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (pictureData == null) {
			cameraSurface.getCamera().cancelAutoFocus();
		}
		return pictureData;
	}
	
	
	
	/*在LibGDX中，图片以pixmap为存在格式，所以Android与LibGDX进行交互，必须有saveAsJpeg*/
	@Override
	public void saveAsJpeg(FileHandle jpgfile, Pixmap pixmap) {
		FileOutputStream fos;
		int x = 0, y = 0;
		int xl = 0, yl = 0;
		try {
			Bitmap bmp = Bitmap.createBitmap(pixmap.getWidth(),
					pixmap.getHeight(), Bitmap.Config.ARGB_8888);
			// we need to switch between LibGDX RGBA format to Android ARGB
			// format
			for (x = 0, xl = pixmap.getWidth(); x < xl; x++) {
				for (y = 0, yl = pixmap.getHeight(); y < yl; y++) {
					int color = pixmap.getPixel(x, y);
					// RGBA => ARGB
					int RGB = color >> 8;
					int A = (color & 0x000000ff) << 24;
					int ARGB = A | RGB;
					bmp.setPixel(x, y, ARGB);
				}
			}
			fos = new FileOutputStream(jpgfile.file());
			bmp.compress(CompressFormat.JPEG, 90, fos);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isReady() {
		if (cameraSurface != null && cameraSurface.getCamera() != null) {
			return true;
		}
		return false;
	}
}
