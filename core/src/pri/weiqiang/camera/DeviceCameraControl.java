package pri.weiqiang.camera;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;

/**
 * @author 54wall
 * @date 创建时间：2016-7-14 下午12:38:29
 * @version 1.0
 */
public interface DeviceCameraControl {

	// Synchronous interface
	void prepareCamera();
	void startPreview();
	void stopPreview();
	void takePicture();
	byte[] getPictureData();
	// Asynchronous interface - need when called from a non platform thread (GDXOpenGl thread)
	void startPreviewAsync();
	void stopPreviewAsync();
	byte[] takePictureAsync(long timeout);
	void saveAsJpeg(FileHandle jpgfile, Pixmap cameraPixmap);
	boolean isReady();
	void prepareCameraAsync();
}