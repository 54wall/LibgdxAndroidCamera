完整代码已上传github，https://github.com/54wall/LibgdxAndroidCamera

## 开发背景 ##

  简单介绍下自己，目前在人脸识别从事android开发与测试，公司的产品是人脸识别SDK，成品就是各种终端上的人脸识别设备，总监大人希望最好能有一个跨平台的开发框架，能够以一种语言为主，输出各种app。于是他找到了libGDX，一个多平台开发游戏的框架， 语言为java。libGDX是一个游戏开发工具，确切的说也就是一个java框架，按他的套路进行编写即可。
    开始稍微看看游戏，有些误入歧途，然后他说要看看怎么掉摄像头。然后我又误入歧途的寻找寻找了一些有意思的东西：一个是抓幽灵的游戏ChaseWhisplyProject，来自 <https://github.com/tvbarthel/ChaseWhisplyProject> 
![ChaseWhisplyProject](http://upload-images.jianshu.io/upload_images/2467798-0baae189de4a66b3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

另外一个beyondar，来自 <https://github.com/BeyondAR/beyondar> 

![beyondar](http://upload-images.jianshu.io/upload_images/2467798-7f48fc01c83121cc.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

当然，上边两个多事在Android内实现的，而非在libgdx中实现，感谢Github，在libGDX的介绍中，有达人分享了他使用libGDX调用android摄像头的实例，他在libGDX中调用摄像头的目的简单来说就是AR，使用者可以通过APP看到设备背后的路面，或者和真实世界有交互，就像PokemonGO一样，当然全部的这些也需要算法支持。但是时间是2013年，我拷贝下来，进行了微调，发现是可以实现的，鉴于目前网络上还真没有许多的实例说明如何在LibGDX中调用android摄像头，我搜索libgdx camera 大部分给我的结果都是libgdx中的镜头，就是跟随演员的镜头，而不是设备摄像头，这里也对这个实例进行一个记录和补充，以及简单的实现。

wiki地址：[https://github.com/libgdx/libgdx/wiki/Integrating-libgdx-and-the-device-camera](https://github.com/libgdx/libgdx/wiki/Integrating-libgdx-and-the-device-camera)

  下面我也是基于这篇文章进行翻译，顺序当然有所改变，还有原代码中有包含一个类似初始页面的SplashActivity的我没有实现，目前还没有去了解libGDX的三维编程，代码的源码已上传至GitHub，再好的文字也比不上源码。
完整代码已上传github，https://github.com/54wall/LibgdxAndroidCamera
**libGDX中到底是如何实现调用摄像头**

  很简单，就是在一个透明的画布上绘制libGDX的舞台演员，在画布的后边放置摄像头的预览画面。而文章主要解决的问题就是如何显示摄像头预览画面，和如何调用摄像头。

**如何显示摄像头预览画面**

  我们知道，libGDX的代码主要全部在core项目中，其他各个平台的代码都是经过一个简单的代码启动启动然后，剩下的就去调用core项目中的代码，首先在libGDX初始化时，要进行一定的设置，才能让摄像头的预览画面显示在libGDX框架生成的app中，进入android项目组，修改AndroidLauncher.class代码如下

``` java
   AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
    cfg.r = 8;
    cfg.g = 8;
    cfg.b = 8;
    cfg.a = 8;    
    DeviceCameraControl cameraControl = new AndroidDeviceCameraController(this);
    initialize(new MyGdxGame0606(cameraControl), cfg);
```

  你可以点击进入AndroidApplicationConfiguration去看看r,g,b,a原来数值如下

```
/** number of bits per color channel **/
public int r = 5, g = 6, b = 5, a = 0;
```

  rgba的具体值指的是每种颜色的比特位数，这里改为8位，大概的意思就是每种颜色深度为8位，也就是2的8次幂，也就是256，和photoshop中意义一样。这么设置的原因是为了libGDX中的画布的后边能够正常的显示android的摄像头，不然颜色和在android上调用摄像头有出入，色彩不会同样的丰富。

  同样在AndroidLauncher.class中，将OpenGL surface 模式设置成TRANSLUCENT
   

```
if (graphics.getView() instanceof SurfaceView) {
    SurfaceView glView = (SurfaceView) graphics.getView();
    // force alpha channel - I'm not sure we need this as the GL surface
    // is already using alpha channel                
    glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
}
```

  然后是，在AndroidLauncher.class新建一个post方法，用来帮助唤起一些异步线程

```
public void post(Runnable r) {
    handler.post(r);
}
```

  以上就是AndroidLauncher.class所要完成的全部内容。
**如何让屏幕透明**

    很简单，在core项目中MyGdxGame0606.class主类中的render()渲染方法中，要注意使用glClearColor()清屏时，参数的选择必须全部是0，这样相机预览的画面才会显示在render画面的后边

```
Gdx.gl20.glClearColor(0f, 0.0f, 0.0f, 0.0f);
```

**清屏准备完毕，现在就是要调用android摄像头的时候到了**

  首先了解一下，把大象装冰箱中总共分几步？对于调用android摄像头的步骤则可以分如下几部？Android相机有特别的工作顺序必须遵守，通过application的callbacks回调函数能管理相机的状态，机器的工作状态将由AndroidDeviceCameraController.class进行管理（desktop没有实现，Android已实现）。
    相机的工作状态一次是 Ready -> Preview -> autoFocusing -> ShutterCalled -> Raw PictureData -> Postview PictureData -> Jpeg PictureData -> Ready （可以做一个表格）
    本实例代码实现的顺序也可以概括为 Ready -> Preview -> autoFocusing -> Jpeg PictureData -> Ready
**新建AndroidDeviceCameraController** 

  在core项目中，新建一个DeviceCameraController类,而为了配合core中DeviceCameraControl，在android项目中，新建一个AndroidDeviceCameraController 类，来控制设备的摄像头，它要继承DeviceCameraControl，同时还要实现Camera.PictureCallback：(android.hardware.Camera.PictureCallback)Camera.AutoFocusCallback(android.hardware.Camera.AutoFocusCallback)，共计三个接口，来实现android摄像头从准备到拍摄的过程。

```
public class AndroidDeviceCameraController implements DeviceCameraControl, Camera.PictureCallback, Camera.AutoFocusCallback {
.
.
.
}
```
AndroidDeviceCameraController 新建后，逐步实现摄像头该有的各个功能。
**1.准备显示预览信息的CameraSurface**

  我们产生一个CameraSurface类来负责管理摄像头和它收集的图像，这里我和android摄像相关的代码一致

```
    public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback {
        private Camera camera;
public CameraSurface( Context context ) {
            super( context );
            // We're implementing the Callback interface and want to get notified
            // about certain surface events.
            getHolder().addCallback( this );
            // We're changing the surface to a PUSH surface, meaning we're receiving
            // all buffer data from another component - the camera, in this case.
            getHolder().setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
        }
public void surfaceCreated( SurfaceHolder holder ) {
            // Once the surface is created, simply open a handle to the camera hardware.
            camera = Camera.open();
        }
public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
            // This method is called when the surface changes, e.g. when it's size is set.
            // We use the opportunity to initialize the camera preview display dimensions.
            Camera.Parameters p = camera.getParameters();
            p.setPreviewSize( width, height );
            camera.setParameters( p );
// We also assign the preview display to this surface...
            try {
                camera.setPreviewDisplay( holder );
            } catch( IOException e ) {
                e.printStackTrace();
            }
        }
public void surfaceDestroyed( SurfaceHolder holder ) {
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
```

**2.在android项目中增加相机预览视图**

  在android项目的AndroidDeviceController类，使用activity.addContentView，直接将cameraSurface显示在android设备的屏幕上

```
    @Override
    public void prepareCamera() {
        if (cameraSurface == null) {
            cameraSurface = new CameraSurface(activity);
        }
        activity.addContentView( cameraSurface, new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ) );
    }
```

```
@Override
public synchronized void prepareCamera() {
    if (cameraSurface == null) {
        cameraSurface = new CameraSurface(activity);
    }
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams  (680,680);
    params.rightMargin=150;//可以通过设置rightMargin控制组件的实际位置
    params.leftMargin=200;//可以通过设置rightMargin控制组件的实际位置
    params.topMargin=100;
    activity.addContentView(cameraSurface, params);
     

}
```

  prepareCamera方法应该在libgdx渲染过程中异步调用

```
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
public void prepareCameraAsync() {
    Runnable r = new Runnable() {
        public void run() {
            prepareCamera();
        }
    };
    activity.post(r);
}
```

  当CameraSurface和camera 对象准备好了的时候（通过检测cameraSurface!=null && cameraSurface.getCamera() != null），就可以让相机由准备状态进入预览模式

```
    @Override
    public boolean isReady() {
        if (cameraSurface!=null && cameraSurface.getCamera() != null) {
            return true;
        }
        return false;
    }
```

```
@Override
public boolean isReady() {
    if (cameraSurface != null && cameraSurface.getCamera() != null) {
        return true;
    }
    return false;
}
```

  异步调用开启预览

```
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
    public synchronized void startPreview() {
        // ...and start previewing. From now on, the camera keeps pushing preview
        // images to the surface.
        if (cameraSurface != null && cameraSurface.getCamera() != null) {
            cameraSurface.getCamera().startPreview();
        }
    }
```

**3.由预览模式进行拍照**

  拍照前还要AndroidDeviceCameraController类设置下相机合适的参数
    

```
public void setCameraParametersForPicture(Camera camera) {
        // Before we take the picture - we make sure all camera parameters are as we like them
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
        camera.setParameters( p );      
    }
```

  接下来，我们将通过设置相机的参数，设置聚焦为自动模式，

```
@Override
public synchronized void takePicture() {
        // the user request to take a picture - start the process by requesting focus
            setCameraParametersForPicture(cameraSurface.getCamera());
            cameraSurface.getCamera().autoFocus(this);
    }
```

  当聚焦完成后，我们就要拍照了，仅仅实现JPG回调实现
   

```
@Override
public synchronized void onAutoFocus(boolean success, Camera camera) {
        // Focus process finished, we now have focus (or not)
        if (success) {
            if (camera != null) {
                camera.stopPreview();
                // We now have focus take the actual picture
                camera.takePicture(null, null, null, this);
            }
        }
    }
@Override
public synchronized void onPictureTaken(byte[] pictureData, Camera camera) {
        this.pictureData = pictureData;
    }

@Override
public synchronized void onAutoFocus(boolean success, Camera camera) {
    // Focus process finished, we now have focus (or not)
    if (success) {
        if (camera != null) {
            camera.stopPreview();                
            /*增加三个回调函数shutterCallback, rawPictureCallback, jpegPictureCallback后，可以进行拍照，并且成功保存*/
            // We now have focus take the actual picture
            camera.takePicture(shutterCallback, rawPictureCallback, jpegPictureCallback);//54wall
            camera.startPreview();
            
        }
    }
}

@Override
public synchronized void onPictureTaken(byte[] pictureData, Camera camera) {
    // We got the picture data - keep it
    this.pictureData = pictureData;
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
//            String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
//                    .toString()
//                    + File.separator
//                    + "PicTest_" + System.currentTimeMillis() + ".jpg";
//            File file = new File(fileName);
//            if (!file.getParentFile().exists()) {
//                file.getParentFile().mkdir();
//            }
//            
//            try {
//                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
//                bos.write(arg0);
//                bos.flush();
//                bos.close();        
//                
//            } catch (Exception e) {
//                
//            }
        pictureData=arg0;
        
    };
};
```
  以上就是拍照的具体步骤，之后照片完成后，需要将图像数据进行保存到存储器上
  拍照的具体功能实现了，为了在libgdx中能够看到摄像头，当然需要在实现ApplicationListener的主类的render（）进行设置了，我这里设置了三个按钮，功能分别是开启相机，进行拍摄，和一个控制人物移动的按钮（算是证明是在libgdx框架内部的）。
原代码时触控进入相机预览，松开则进行拍照，我开始还没太理解，render中因为涉及到相机的功能切换，所以在libgdx主类中定义了相机的这几种状态
```
public enum Mode {
		normal, prepare, preview, takePicture, waitForPictureReady,
	}，
```
render（）中的代码非常长，不过就是在相机的各个状态中切换，具体代码如下：
```
	@Override
	public void render() {
		// Gdx.gl20.glClearColor(0.0f, 0f, 0.0f, 0.0f);//黑
		// Gdx.gl.glClearColor(1, 1, 1, 1);//背景为白色
		Gdx.gl.glClearColor(0.57f, 0.40f, 0.55f, 1.0f);// 紫色
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);// 清屏		
		render_preview();

	}


	public void render_preview() {
		/* 我已经将preview变为takePicture状态移动到click中，实现先预览再拍照，这样便于理解相机的运行步骤 */
		Gdx.gl20.glHint(GL20.GL_GENERATE_MIPMAP_HINT, GL20.GL_NICEST);
		if (mode == Mode.takePicture) {
			Gdx.gl20.glClearColor(0f, 0.0f, 0.0f, 0.0f);
			if (deviceCameraControl != null) {
				deviceCameraControl.takePicture();
			}
			mode = Mode.waitForPictureReady;
		} else if (mode == Mode.waitForPictureReady) {
			Gdx.gl20.glClearColor(0.0f, 0f, 0.0f, 0.0f);
		} else if (mode == Mode.prepare) {
			Gdx.gl20.glClearColor(0.0f, 0.0f, 0f, 0.6f);
			if (deviceCameraControl != null) {
				if (deviceCameraControl.isReady()) {
					deviceCameraControl.startPreviewAsync();
					mode = Mode.preview;
				}
			}
		} else if (mode == Mode.preview) {
			Gdx.gl20.glClearColor(0.0f, 0.0f, 0.0f, 0f);
		} else {
			/* mode = normal */
			Gdx.gl20.glClearColor(0.0f, 0.0f, 0.6f, 1.0f);

		}
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		/* 下边放到texture.bind();时效果一致 */
		batch.begin();	
		stage.act(); // 更新舞台逻辑
		batch.draw(texture, 0, 0, 3f*texture.getWidth(), 3f*texture.getHeight());
		Gdx.app.log("", String.valueOf(texture.getWidth()));
		//先绘制的就先出现，所以演员在texture上边，而不是被覆盖
		//batch.draw(actorTexture, firstActor.getX(), firstActor.getY());//原大小
		batch.draw(actorTexture, firstActor.getX(), firstActor.getY(),4*actorTexture.getWidth(),4*actorTexture.getWidth());//可控制绘制图像大小
		button_move.draw(batch, 1.0f);
		stage.draw();// 绘制舞台
		batch.end();
		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl20.glEnable(GL20.GL_TEXTURE);
		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
		// Gdx.gl20.glEnable(GL20.GL_LINE_SMOOTH);//old
		Gdx.gl20.glEnable(GL20.GL_LINE_LOOP);//54wall
		Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl20.glClearDepthf(1.0F);
		camera.update(true);
		// camera.apply(Gdx.gl20);//old
		texture.bind();

		if (mode == Mode.waitForPictureReady) {
			/*注意deviceCameraControl.getPictureData()得到的是byte[]，可见整体思路就是，
			 *将Android摄像头得到byte[],然后将byte[]转换为Pixmap，最后将pixmap存为jpg,这样不适用Android端图片保存模式，
			 *byte[]----Pixmap----jpg
			 */
			if (deviceCameraControl.getPictureData() != null) {
				// camera picture was actually takentake Gdx Screenshot
				Pixmap screenshotPixmap = getScreenshot(0, 0,
						Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
				Pixmap cameraPixmap = new Pixmap(
						deviceCameraControl.getPictureData(), 0,
						deviceCameraControl.getPictureData().length);
				merge2Pixmaps(cameraPixmap, screenshotPixmap);
				// we could call PixmapIO.writePNG(pngfile, cameraPixmap);
				//仅保存screenshot，对同一时间的图片进行保存然后进行比较 
				Pixmap screenshotPixmap_test = getScreenshot(0, 0,
						Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
				FileHandle jpgfile_screenshot = Gdx.files
						.external("a_SDK_fail/libGdxSnapshot" + "_" + date
								+ "_screenshot.jpg");
				deviceCameraControl.saveAsJpeg(jpgfile_screenshot,
						screenshotPixmap_test);
				//仅保存cameraPixma，对同一时间的图片进行保存然后进行比较 
				Pixmap cameraPixmap_test = new Pixmap(
						deviceCameraControl.getPictureData(), 0,
						deviceCameraControl.getPictureData().length);
				FileHandle jpgfile_cameraPixmap = Gdx.files
						.external("a_SDK_fail/libGdxSnapshot" + "_" + date
								+ "_camera.jpg");
				deviceCameraControl.saveAsJpeg(jpgfile_cameraPixmap,
						cameraPixmap_test);
				//保存混合之后的相片
				FileHandle jpgfile = Gdx.files
						.external("a_SDK_fail/libGdxSnapshot" + "_" + date
								+ ".jpg");
				Gdx.app.log("FileHandle", date);
				time_1 = System.currentTimeMillis();
				deviceCameraControl.saveAsJpeg(jpgfile, cameraPixmap);
				time_2 = System.currentTimeMillis();
				//可以得到35830ms=35s，所以非常忙，导致Mode非常缓慢的回到Mode.normal
				Gdx.app.log("cost", String.valueOf(time_2 - time_1));
				deviceCameraControl.stopPreviewAsync();
				//保存文件后，mode回到normal继续render循环，所以中间停顿和logcat长时间未动的其实是卡住了Org
				mode = Mode.normal;

			}
		}
		// 这个log将会一直出现，所以render其实是一直在执行 
		// Gdx.app.log("mode", String.valueOf(i_render++));
	}
```
**如何实现libgdx端的截图**

  因为AndroidDeviceCameraController 实现两个接口: Camera.PictureCallback，所以可以直接调用，而deviceCameraControl.getPictureData()的byte[]数据则来自AndroidDeviceCameraController,如下
```
@Override
public synchronized byte[] getPictureData() {
    // Give to picture data to whom ever requested it
    return pictureData;
}
    if (deviceCameraControl.getPictureData() != null) { // camera picture was actually taken
        Pixmap cameraPixmap = new Pixmap(deviceCameraControl.getPictureData(), 0, deviceCameraControl.getPictureData().length);
    }
```

  下面是截图的具体操作过程是保存为pixmap，libgdx中保存格式都是pixmap，而非android中的bitmap

```
    public Pixmap getScreenshot(int x, int y, int w, int h, boolean flipY) {
        Gdx.gl.glPixelStorei(GL10.GL_PACK_ALIGNMENT, 1);
final Pixmap pixmap = new Pixmap(w, h, Format.RGBA8888);
        ByteBuffer pixels = pixmap.getPixels();
        Gdx.gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pixels);
final int numBytes = w * h * 4;
        byte[] lines = new byte[numBytes];
        if (flipY) {
            final int numBytesPerLine = w * 4;
            for (int i = 0; i < h; i++) {
                pixels.position((h - i - 1) * numBytesPerLine);
                pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
            }
            pixels.clear();
            pixels.put(lines);
        } else {
            pixels.clear();
            pixels.get(lines);
        }
return pixmap;
    }
```

  接下来的操作都是需要消耗大量时间和CPU资源的，首先不应放到UI线程中，应该新开线程去执行，并且最好加一个进度条，在代码示例中，我们并没有那么做，所以屏幕在这个过程中会出现卡死的状况。我这里则直接保存了三分文件，分别是截图，android摄像头的拍摄相片，还有二者混合之后的图片，代码如下
                

```
/* 仅保存screenshot，对同一时间的图片进行保存然后进行比较 */
                Pixmap screenshotPixmap_test = getScreenshot(0, 0,
                        Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
                FileHandle jpgfile_screenshot = Gdx.files
                        .external("a_SDK_fail/libGdxSnapshot" + "_" + date
                                + "_screenshot.jpg");
                deviceCameraControl.saveAsJpeg(jpgfile_screenshot,
                        screenshotPixmap_test);
                /* 仅保存cameraPixma，对同一时间的图片进行保存然后进行比较 */
                Pixmap cameraPixmap_test = new Pixmap(
                        deviceCameraControl.getPictureData(), 0,
                        deviceCameraControl.getPictureData().length);
                FileHandle jpgfile_cameraPixmap = Gdx.files
                        .external("a_SDK_fail/libGdxSnapshot" + "_" + date
                                + "_camera.jpg");
                deviceCameraControl.saveAsJpeg(jpgfile_cameraPixmap,
                        cameraPixmap_test);
                /* 保存混合之后的相片 */
                FileHandle jpgfile = Gdx.files
                        .external("a_SDK_fail/libGdxSnapshot" + "_" + date
                                + ".jpg");
                Gdx.app.log("FileHandle", date);
                time_1 = System.currentTimeMillis();
                deviceCameraControl.saveAsJpeg(jpgfile, cameraPixmap);
                time_2 = System.currentTimeMillis();
                /* 可以得到35830ms=35s，所以非常忙，导致Mode非常缓慢的回到Mode.normal */
                Gdx.app.log("cost", String.valueOf(time_2 - time_1));
                deviceCameraControl.stopPreviewAsync();
```
**混合两个pixmap**

  接下来是整合两个PIxmap对象，LibGDX Pixmap对象可以帮助我们实现这个功能，但是因为相机的相片可能有不同的aspect ratio，所以我们也需要分别对待处理

```
    private void merge2Pixmaps(Pixmap mainPixmap, Pixmap overlayedPixmap) {
        // merge to data and Gdx screen shot - but fix Aspect Ratio issues between the screen and the camera
        Pixmap.setFilter(Filter.BiLinear);
        float mainPixmapAR = (float)mainPixmap.getWidth() / mainPixmap.getHeight();
        float overlayedPixmapAR = (float)overlayedPixmap.getWidth() / overlayedPixmap.getHeight();
        if (overlayedPixmapAR < mainPixmapAR) {
            int overlayNewWidth = (int)(((float)mainPixmap.getHeight() / overlayedPixmap.getHeight()) * overlayedPixmap.getWidth());
            int overlayStartX = (mainPixmap.getWidth() - overlayNewWidth)/2;
            mainPixmap.drawPixmap(overlayedPixmap, 
                        0, 
                        0, 
                        overlayedPixmap.getWidth(), 
                        overlayedPixmap.getHeight(), 
                        overlayStartX, 
                        0, 
                        overlayNewWidth, 
                        mainPixmap.getHeight());
        } else {
            int overlayNewHeight = (int)(((float)mainPixmap.getWidth() / overlayedPixmap.getWidth()) * overlayedPixmap.getHeight());
            int overlayStartY = (mainPixmap.getHeight() - overlayNewHeight)/2;
            mainPixmap.drawPixmap(overlayedPixmap, 
                        0, 
                        0, 
                        overlayedPixmap.getWidth(), 
                        overlayedPixmap.getHeight(), 
                        0, 
                        overlayStartY, 
                        mainPixmap.getWidth(), 
                        overlayNewHeight);                  
        }
    }
```
**将图片保存为jpg**

  所以我们选择JPG格式进行保存，一种方式就是使用Android的bitmap类对图片进行jpg格式的保存，这个功能可以在AndroidDeviceController类中实现，因为它是Android特有的功能，所以我们想不用它。
    尽量将大部分代码全部放到libgdx框架中，就是大部分实现的代码要在core中，然而libgdx的pixel格式是RGBA，而bitmap的Pixmap格式是ARGB，所以我们需要一bit一bit的将颜色转换过来
   

```
 @Override
    public void saveAsJpeg(FileHandle jpgfile, Pixmap pixmap) {
        FileOutputStream fos;
        int x=0,y=0;
        int xl=0,yl=0;
        try {
            Bitmap bmp = Bitmap.createBitmap(pixmap.getWidth(), pixmap.getHeight(), Bitmap.Config.ARGB_8888);
            // we need to switch between LibGDX RGBA format to Android ARGB format
            for (x=0,xl=pixmap.getWidth(); x<xl;x++) {
                for (y=0,yl=pixmap.getHeight(); y<yl;y++) {
                    int color = pixmap.getPixel(x, y);
                    // RGBA => ARGB
                    int RGB = color >> 8;
                    int A = (color & 0x000000ff) << 24;
                    int ARGB = A | RGB;
                    bmp.setPixel(x, y, ARGB);
                }
            }
            // Finished Color format conversion
            fos = new FileOutputStream(jpgfile.file());
            bmp.compress(CompressFormat.JPEG, 90, fos);
            // Finished Comression to JPEG file
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();            
        }
    }
```
**停止预览**

  在完成保存图片后，我们将停止预览窗口，并且从Activity窗口中移去CameraSurface，我们同样也将停止camera继续想camera surface继续发送preview，我们同样异步执行这些。
   

```
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
    public synchronized void stopPreview() {
        // stop previewing. 
        if (cameraSurface != null) {
            if (cameraSurface.getCamera() != null) {
                cameraSurface.getCamera().stopPreview();
            }
            ViewParent parentView = cameraSurface.getParent();
            if (parentView instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) parentView;
                viewGroup.removeView(cameraSurface);
            }
        }
    }
```

  注意在合成两张pixmap也就是混合相机取景和libgdx截图时分辨率有差异。在我们的Pixmaps整合过程中依然还存在一个问题，就是相机的分辨率和我们的截图分辨率也许是不同的（我在我的三星手机上，我把一个480x320屏幕截图延伸到2560x1920 大小的图片）。一个围绕它的解决方法就是扩大Libgdx视图的尺寸到更大，比实际物理设备的尺寸要大，要实现他需要使用setFixedSize()功能。真实的屏幕尺寸是根据BPU内存。
  然而，在这个时间中我设法去设置虚拟屏幕尺寸为960x640（可能因为GPU显存已经被origin的带下分配了）
   

``` 
 public void setFixedSize(int width, int height) {
        if (graphics.getView() instanceof SurfaceView) {
            SurfaceView glView = (SurfaceView) graphics.getView();
            glView.getHolder().setFixedSize(width, height);
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
    }
public void restoreFixedSize() {
        if (graphics.getView() instanceof SurfaceView) {
            SurfaceView glView = (SurfaceView) graphics.getView();
            glView.getHolder().setFixedSize(origWidth, origHeight);
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
    }
```
**一些小提示**

 1. 提示1
注意，代码仅仅适用于Android平台，并非跨平台解决方法，但是我觉得至少在桌面端，应该可以提供类似功能的代码
 2. 提示2
在整合图片，并将图片数据保存到存储器上时，会花大量的时间，是因为libgdx颜色方案是RGBA而bitmap颜色方案是ARGB
 3. 提示3
最后需要注意的
我仅仅测试了一部分Android设备，不同的GPU会产生不同的现象。
**完整代码已上传github**
[https://github.com/54wall/LibgdxAndroidCamera](https://github.com/54wall/LibgdxAndroidCamera)