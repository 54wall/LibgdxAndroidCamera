package pri.weiqiang.camera;


import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;//oldGL20
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class AndroidCamera implements ApplicationListener {
	/* http://blog.csdn.net/xietansheng/article/details/50187861 */
	private Stage stage;// 舞台
	private Texture actorTexture;
	private Texture move_1_Texture;
	private Texture move_2_Texture;
	private Texture upTexture;
	private Texture downTexture;
	private Button button;
	private Button button_1;
	private Button button_move;
	// 视口世界的宽高统使用 480 * 800, 并统一使用伸展视口（StretchViewport）
	public static final float WORLD_WIDTH = 480;
	public static final float WORLD_HEIGHT = 800;
	public SpriteBatch batch;
	public Texture texture_demo;
	private SimpleDateFormat sDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss");
	private String date = sDateFormat.format(new java.util.Date());
	public enum Mode {
		normal, prepare, preview, takePicture, waitForPictureReady,
	}
	public int state_normal = 0;
	public int state_prepare = 1;
	private Texture texture;
	private PerspectiveCamera camera;
	private Mode mode = Mode.normal;
	private final DeviceCameraControl deviceCameraControl;
	private long time_1;
	private long time_2;
	private Actor firstActor;
	/*
	 * 通过this.deviceCameraControl=
	 * cameraControl获取Android端摄像头然后后续所有的deviceCameraControl实际调用的都是Android的东西
	 */
	public AndroidCamera(DeviceCameraControl cameraControl) {
		this.deviceCameraControl = cameraControl;
	}

	@Override
	public void create() {
		/* http://blog.csdn.net/xietansheng/article/details/50187861 */
		// 设置日志输出级别
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		// 使用伸展视口（StretchViewport）创建舞台
		stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));
		// 将输入处理设置到舞台（必须设置, 否则点击按钮没效果）
		Gdx.input.setInputProcessor(stage);
		firstActor = new Actor();
		firstActor.setHeight(150);
		firstActor.setWidth(150);
		firstActor.setPosition(150, 150);	
		move_1_Texture = new Texture(Gdx.files.internal("data/play.png"));
		move_2_Texture = new Texture(Gdx.files.internal("data/start.png"));
		actorTexture = new Texture(Gdx.files.internal("data/cat_0.png"));
		upTexture = new Texture(Gdx.files.internal("data/start.png"));
		downTexture = new Texture(Gdx.files.internal("data/takepictures.png"));			
		//创建 ButtonStyle
		Button.ButtonStyle style_move = new Button.ButtonStyle();
		Button.ButtonStyle style = new Button.ButtonStyle();
		style_move.up=new TextureRegionDrawable(new TextureRegion(move_1_Texture));
		style_move.down=new TextureRegionDrawable(new TextureRegion(move_2_Texture));		
		style.up = new TextureRegionDrawable(new TextureRegion(upTexture));
		style.down = new TextureRegionDrawable(new TextureRegion(downTexture));		
		button_move = new Button(style_move);
		// 设置按钮的位置
		button_move.setPosition(150, 300);
		// 给按钮添加点击监听器
		button_move.addListener(actor_move);						
		button = new Button(style);
		// 设置按钮的位置
		button.setPosition(50, 300);
		// 给按钮添加点击监听器
		button.addListener(preview_on);
		button_1 = new Button(style);
		// 设置按钮的位置
		button_1.setPosition(300, 300);
		// 给按钮添加点击监听器
		button_1.addListener(preview_on_1);
		//添加 button 到舞台 
		stage.addActor(button);
		stage.addActor(button_1);
		stage.addActor(button_move);
		stage.addActor(firstActor);
		batch = new SpriteBatch();
		// Load the Libgdx splash screen texture
		texture = new Texture(Gdx.files.internal("data/bg.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		// Create the OpenGL Camera，这里指的是视角，正交视角与设备摄像头完全不是一个东西
		camera = new PerspectiveCamera(67.0f, 2.0f * Gdx.graphics.getWidth()
				/ Gdx.graphics.getHeight(), 2.0f);
		camera.far = 100.0f;
		camera.near = 0.1f;
		camera.position.set(2.0f, 2.0f, 2.0f);
		camera.lookAt(0.0f, 0.0f, 0.0f);

	}

	/* 手动释放资源 */
	@Override
	public void dispose() {
		/* 对应button，应用退出时释放资源 */
		if (upTexture != null) {
			upTexture.dispose();
		}
		if (downTexture != null) {
			downTexture.dispose();
		}
		if (stage != null) {
			stage.dispose();
		}
		batch.dispose();
		texture.dispose();
		texture = null;
	}

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
	//注意截图与Android设备摄像传回的图像整合时并非按我所看的视角进行 
	private Pixmap merge2Pixmaps(Pixmap mainPixmap, Pixmap overlayedPixmap) {
		// merge to data and Gdx screen shot - but fix Aspect Ratio issues
		// between the screen and the camera
		Pixmap.setFilter(Filter.BiLinear);
		float mainPixmapAR = (float) mainPixmap.getWidth()
				/ mainPixmap.getHeight();
		float overlayedPixmapAR = (float) overlayedPixmap.getWidth()
				/ overlayedPixmap.getHeight();
		if (overlayedPixmapAR < mainPixmapAR) {
			int overlayNewWidth = (int) (((float) mainPixmap.getHeight() / overlayedPixmap
					.getHeight()) * overlayedPixmap.getWidth());
			int overlayStartX = (mainPixmap.getWidth() - overlayNewWidth) / 2;
			// Overlaying pixmaps
			mainPixmap.drawPixmap(overlayedPixmap, 0, 0,
					overlayedPixmap.getWidth(), overlayedPixmap.getHeight(),
					overlayStartX, 0, overlayNewWidth, mainPixmap.getHeight());
		} else {
			int overlayNewHeight = (int) (((float) mainPixmap.getWidth() / overlayedPixmap
					.getWidth()) * overlayedPixmap.getHeight());
			int overlayStartY = (mainPixmap.getHeight() - overlayNewHeight) / 2;
			// Overlaying pixmaps
			mainPixmap.drawPixmap(overlayedPixmap, 0, 0,
					overlayedPixmap.getWidth(), overlayedPixmap.getHeight(), 0,
					overlayStartY, mainPixmap.getWidth(), overlayNewHeight);
		}
		return mainPixmap;
	}

	public Pixmap getScreenshot(int x, int y, int w, int h, boolean flipY) {

		Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
		final Pixmap pixmap = new Pixmap(w, h, Format.RGBA8888);
		ByteBuffer pixels = pixmap.getPixels();
		Gdx.gl.glReadPixels(x, y, w, h, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE,
				pixels);

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

	@Override
	public void resize(int width, int height) {
		camera = new PerspectiveCamera(67.0f, 2.0f * width / height, 2.0f);
		camera.far = 100.0f;
		camera.near = 0.1f;
		camera.position.set(2.0f, 2.0f, 2.0f);
		camera.lookAt(0.0f, 0.0f, 0.0f);

	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	ClickListener preview_on = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			Gdx.app.log("preview_on", "preview_on按钮被点击了");
			if (mode == Mode.waitForPictureReady) {
				mode = Mode.normal;
			}
			if (mode == Mode.normal) {
				mode = Mode.prepare;
				if (deviceCameraControl != null) {
					deviceCameraControl.prepareCameraAsync();
				}
			}
		}

	};
	
	//控制人物移动
	ClickListener actor_move = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {			
			firstActor.setX(firstActor.getX() + 55);			
			Gdx.app.log("actor_move", "actor_move按钮被点击了");
		}

	};

	ClickListener preview_on_1 = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			Gdx.app.log("preview_on_1", "preview_on_1按钮被点击了");
			if (mode == Mode.preview) {
				mode = Mode.takePicture;
			}

		}

	};

	ClickListener preview_off = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			Gdx.app.log("preview_on", "1按钮被点击了");
			if (mode == Mode.waitForPictureReady) {
				mode = Mode.normal;
			}

			if (mode == Mode.normal) {
				mode = Mode.prepare;
				if (deviceCameraControl != null) {
					deviceCameraControl.prepareCameraAsync();
				}
			}

		}
	};

}
