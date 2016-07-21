package com.mygdx.game0606;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;//oldGL20
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class MyGdxGame0606 implements ApplicationListener {
	/* http://blog.csdn.net/xietansheng/article/details/50187861 */
	private Stage stage;// 舞台
	private Texture upTexture;
	private Texture downTexture;
	private Button button;// 按钮
	private Button button_1;// 按钮
	// 视口世界的宽高统使用 480 * 800, 并统一使用伸展视口（StretchViewport）
	public static final float WORLD_WIDTH = 800;
	public static final float WORLD_HEIGHT = 600;
	/* 54wall */
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

	public Constant constant = new Constant();

	public static final short facesVerticesIndex[][] = { { 0, 1, 2, 3 },
			{ 4, 5, 6, 7 }, { 8, 9, 10, 11 }, { 12, 13, 14, 15 },
			{ 16, 17, 18, 19 }, { 20, 21, 22, 23 } };

	private final static VertexAttribute verticesAttributes[] = new VertexAttribute[] {
			new VertexAttribute(Usage.Position, 3, "a_position"),
			new VertexAttribute(Usage.ColorPacked, 4, "a_color"),
			new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoords"), };

	private Texture texture;

	private Mesh[] mesh = new Mesh[6];

	private PerspectiveCamera camera;

	private Mode mode = Mode.normal;

	private final DeviceCameraControl deviceCameraControl;
	private long time_1;
	private long time_2;
	private int i_render = 0;

	/*
	 * 通过this.deviceCameraControl=
	 * cameraControl获取Android端摄像头然后后续所有的deviceCameraControl实际调用的都是Android的东西
	 */
	public MyGdxGame0606(DeviceCameraControl cameraControl) {
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
		/* 第 1 步: 创建 弹起 和 按下 两种状态的纹理 */
		upTexture = new Texture(Gdx.files.internal("data/button_1.png"));
		downTexture = new Texture(Gdx.files.internal("data/button_2.png"));
		/* 第 2 步: 创建 ButtonStyle */
		Button.ButtonStyle style = new Button.ButtonStyle();
		// 设置 style 的 弹起 和 按下 状态的纹理区域
		style.up = new TextureRegionDrawable(new TextureRegion(upTexture));
		style.down = new TextureRegionDrawable(new TextureRegion(downTexture));
		/* 第 3 步: 创建 Button */
		button = new Button(style);
		// 设置按钮的位置
		button.setPosition(100, 300);
		// 给按钮添加点击监听器
		button.addListener(preview_on);

		button_1 = new Button(style);
		// 设置按钮的位置
		button_1.setPosition(550, 400);
		// 给按钮添加点击监听器
		button_1.addListener(preview_on_1);

		/* 第 4 步: 添加 button 到舞台 */
		stage.addActor(button);
		stage.addActor(button_1);
		/* 54wall */
		batch = new SpriteBatch();
		texture_demo = new Texture(Gdx.files.internal("data/1.png"));
		// Load the Libgdx splash screen texture
		texture = new Texture(Gdx.files.internal("data/libgdx.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		// Create the 6 faces of the Cube
		for (int i = 0; i < 6; i++) {
			mesh[i] = new Mesh(true, 24, 4, verticesAttributes);
			mesh[i].setVertices(Constant.vertexData);
			mesh[i].setIndices(facesVerticesIndex[i]);
		}

		// Create the OpenGL Camera，这里指的是视角，正交视角与设备摄像头完全不是一个东西
		camera = new PerspectiveCamera(67.0f, 2.0f * Gdx.graphics.getWidth()
				/ Gdx.graphics.getHeight(), 2.0f);// oldfloat
													// fieldOfViewY改为37无反应
		camera.far = 100.0f;// old 改为50，无反应

		camera.near = 0.1f;
		camera.position.set(2.0f, 2.0f, 2.0f);
		camera.lookAt(0.0f, 0.0f, 0.0f);// old

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
		/* 54wall */
		batch.dispose();
		texture_demo.dispose();

		texture.dispose();
		for (int i = 0; i < 6; i++) {
			mesh[i].dispose();
			mesh[i] = null;
		}
		texture = null;
	}

	/* 通过debug发现，每次点击运行（断点在render内）render都会一直运行，可见render作为渲染的一个覆盖函数的确是处于一直运行的状态 */
	@Override
	public void render() {
		// Gdx.gl20.glClearColor(0.0f, 0f, 0.0f, 0.0f);//黑
		// Gdx.gl.glClearColor(1, 1, 1, 1);// 设置背景为白色
		Gdx.gl.glClearColor(0.57f, 0.40f, 0.55f, 1.0f);// 紫色
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);// 清屏
		render_preview();

	}

	/*
	 * 1 发现在打开摄像头后，button全部失效，只有没有camera预览时，button才有效，是不是渲染时摄像头与绘制的button存在冲突
	 * 并且在调试button时，屏幕处于按压状态也是和button冲突了
	 * 
	 * 2 现在实现的功能是按下左按钮，开始预览，按下右侧按钮进行聚焦拍摄，这说明在mode为preview状态时，button是可以相应的之前却一直不可以
	 */
	public void render_preview() {
		/* 我已经将preview变为takePicture状态移动到click中，实现先预览再拍照 */
		Gdx.gl20.glHint(GL20.GL_GENERATE_MIPMAP_HINT, GL20.GL_NICEST);
		if (mode == Mode.takePicture) {
			/* 没有清屏，摄像头的预览功能就没有，演员和舞台在各个if中可以不加 */
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
			Gdx.gl20.glClearColor(0.0f, 0.6f, 0.0f, 0f);
		} else {
			/* mode = normal */
			Gdx.gl20.glClearColor(0.0f, 0.0f, 0.6f, 1.0f);

		}
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		/*下边放到texture.bind();时效果一致*/
		batch.begin();
		stage.act(); // 更新舞台逻辑
		button.draw(batch, 1.0F);// 仅绘制actor
		button_1.draw(batch, 1.0F);
		stage.draw();// 绘制舞台
		batch.draw(texture, 0, 0, 960, 540);
		batch.end();

		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl20.glEnable(GL20.GL_TEXTURE);
		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
		// Gdx.gl20.glEnable(GL20.GL_LINE_SMOOTH);//54wall old
		Gdx.gl20.glEnable(GL20.GL_LINE_LOOP);// new
		Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl20.glClearDepthf(1.0F);
		camera.update(true);
		// camera.apply(Gdx.gl20);//54wall old
		texture.bind();


		if (mode == Mode.waitForPictureReady) {
			/*
			 * 注意deviceCameraControl.getPictureData()得到的是byte[]，可见整体思路就是，
			 * 将Android摄像头得到byte[],然后
			 * 将byte[]转换为Pixmap，最后将pixmap存为jpg,这样不适用Android端图片保存模式，
			 * byte[]----Pixmap----jpg
			 */
			if (deviceCameraControl.getPictureData() != null) {
				// camera picture was actually takentake Gdx Screenshot
				Pixmap screenshotPixmap = getScreenshot(0, 0,
						Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
				/* 开始报错deviceCameraControl.getPictureData一直未null */
				Pixmap cameraPixmap = new Pixmap(
						deviceCameraControl.getPictureData(), 0,
						deviceCameraControl.getPictureData().length);
				merge2Pixmaps(cameraPixmap, screenshotPixmap);
				// we could call PixmapIO.writePNG(pngfile, cameraPixmap);
				/* 现在有一个问题就是每次都是拍照两次，才有一张图片 */
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
				/* 保存文件后，mode回到normal继续render循环，所以中间停顿的其实是卡住了？！ */
				mode = Mode.normal;

			}
		}
		/* 这个log将会一直出现，所以render其实是一直在执行*/
//		Gdx.app.log("mode", String.valueOf(mode));
//		Gdx.app.log("mode", String.valueOf(i_render++));
	}

	/* 随手加一个log是很重要的 */
	public void takePicture4render() {
		/*
		 * 进入app后，手按住屏幕不放，才能进入预览模式，放开就直接拍照了
		 * input.isTouched(1)可能与button点击存在冲突，所以必须屏蔽
		 */
		// if (Gdx.input.isTouched(1)) {
		//
		// mode = Mode.normal;
		// Gdx.app.log("doube", String.valueOf(mode));
		// }
		//
		//
		// if (Gdx.input.isTouched()) {
		// if (mode == Mode.normal) {
		// mode = Mode.prepare;
		// if (deviceCameraControl != null) {
		// deviceCameraControl.prepareCameraAsync();
		// }
		// }
		// } else {
		// // touch removed
		// if (mode == Mode.preview) {
		// mode = Mode.takePicture;
		// }
		// }

		/* 我想让摄像头一直处于预览状态 */
		// if (deviceCameraControl != null) {
		// deviceCameraControl.prepareCameraAsync();
		// }

		/* 仅跟进入预览后直接进入拍照模式 */
		if (mode == Mode.preview) {
			mode = Mode.takePicture;
		}

		// Gdx.gl20.glHint(GL20.GL_PERSPECTIVE_CORRECTION_HINT,
		// GL20.GL_NICEST);//old 54wall
		Gdx.gl20.glHint(GL20.GL_GENERATE_MIPMAP_HINT, GL20.GL_NICEST);
		if (mode == Mode.takePicture) {
			/* 没有清屏，摄像头的预览功能就没有 */
			Gdx.gl20.glClearColor(0f, 0.0f, 0.0f, 0.0f);
			batch.begin();
			stage.act(); // 更新舞台逻辑
			button.draw(batch, 1.0F);// 仅绘制actor
			stage.draw();// 绘制舞台
			batch.draw(texture, 0, 0, 960, 540);
			batch.end();

			if (deviceCameraControl != null) {
				deviceCameraControl.takePicture();

			}
			mode = Mode.waitForPictureReady;
		} else if (mode == Mode.waitForPictureReady) {
			Gdx.gl20.glClearColor(0.0f, 0f, 0.0f, 0.0f);
			batch.begin();
			stage.act(); // 更新舞台逻辑
			button.draw(batch, 1.0F);// 仅绘制actor
			stage.draw();// 绘制舞台
			batch.draw(texture, 0, 0, 960, 540);
			batch.end();

		} else if (mode == Mode.prepare) {
			Gdx.gl20.glClearColor(0.0f, 0.0f, 0f, 0.0f);
			batch.begin();
			stage.act(); // 更新舞台逻辑
			button.draw(batch, 1.0F);// 仅绘制actor
			stage.draw();// 绘制舞台
			batch.draw(texture, 0, 0, 960, 540);
			batch.end();
			if (deviceCameraControl != null) {
				if (deviceCameraControl.isReady()) {
					deviceCameraControl.startPreviewAsync();
					mode = Mode.preview;
				}
			}
		} else if (mode == Mode.preview) {
			Gdx.gl20.glClearColor(0.0f, 0.0f, 0.0f, 0f);
			batch.begin();
			stage.act(); // 更新舞台逻辑
			button.draw(batch, 1.0F);// 仅绘制actor
			stage.draw();// 绘制舞台
			batch.draw(texture, 0, 0, 960, 540);
			batch.end();
		} else {
			/* mode = normal */
			Gdx.gl20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			batch.begin();
			stage.act(); // 更新舞台逻辑
			button.draw(batch, 1.0F);// 仅绘制actor
			stage.draw();// 绘制舞台
			batch.draw(texture, 0, 0, 960, 540);
			batch.end();
		}
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		batch.begin();
		stage.act(); // 更新舞台逻辑
		button.draw(batch, 1.0F);// 仅绘制actor
		stage.draw();// 绘制舞台
		batch.draw(texture, 0, 0, 960, 540);
		batch.end();

		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl20.glEnable(GL20.GL_TEXTURE);
		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
		// Gdx.gl20.glEnable(GL20.GL_LINE_SMOOTH);//54wall old
		Gdx.gl20.glEnable(GL20.GL_LINE_LOOP);// new
		Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl20.glClearDepthf(1.0F);
		camera.update(true);
		// camera.apply(Gdx.gl20);//54wall old
		texture.bind();
		for (int i = 0; i < 6; i++) {
			/*
			 * 可以尝试直接使用上述代码，对一张渲染的图片进行剪切，或者使用三角形代替这里这么多的网格，然后看效果
			 * http://blog.sina.com.cn/s/blog_940dd50a0101fl4s.html
			 */
			// mesh[i].render( GL20.GL_TRIANGLE_FAN, 0 ,4);//54wall old
			// ShaderProgram shader = new ShaderProgram(1 1);//原来的数目一样，不知道会不会影响
			// ShaderProgram shader = new ShaderProgram(String.valueOf(i),
			// String.valueOf(i+1));//54new
			// mesh[i].render(shader, GL20.GL_TRIANGLE_FAN, 0, 4);//54new
		}
		if (mode == Mode.waitForPictureReady) {
			/*
			 * 注意deviceCameraControl.getPictureData()得到的是byte[]，可见整体思路就是，
			 * 将Android摄像头得到byte[],然后
			 * 将byte[]转换为Pixmap，最后将pixmap存为jpg,这样不适用Android端图片保存模式，
			 * byte[]----Pixmap----jpg
			 */
			if (deviceCameraControl.getPictureData() != null) {
				// camera picture was actually takentake Gdx Screenshot
				Pixmap screenshotPixmap = getScreenshot(0, 0,
						Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
				/* 开始报错deviceCameraControl.getPictureData一直未null */
				Pixmap cameraPixmap = new Pixmap(
						deviceCameraControl.getPictureData(), 0,
						deviceCameraControl.getPictureData().length);
				merge2Pixmaps(cameraPixmap, screenshotPixmap);
				// we could call PixmapIO.writePNG(pngfile, cameraPixmap);
				/* 现在有一个问题就是每次都是拍照两次，才有一张图片 */
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
				/* 保存文件后，mode回到normal继续render循环，所以中间停顿的其实是卡住了？！ */
				mode = Mode.normal;

			}
		}
		/* 这个log将会一直出现 */
		Gdx.app.log("mode", String.valueOf(mode));
	}

	/* 注意截图与Android设备摄像传回的图像整合时并非按我所看的视角进行 */
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
			/*
			 * 不等输入，直接写mode =
			 * Mode.takePicture;相当于deviceCameraControl.prepareCameraAsync
			 * ();没有写，会报空指针 java.lang.NullPointerException at
			 * com.mygdx.game0606.android.AndroidDeviceCameraController
			 * .takePicture(AndroidDeviceCameraController.java:122)
			 */

			/*
			 * takePicture4render不能写在这里，因为batch.draw(texture, 0, 0, 960,
			 * 540);应该在render中完成
			 */
			// takePicture4render();
			/*
			 * 增加下面语句后可以完成点击按钮进行拍照，关键就是让Mode进行一个完整的循环
			 * 程序循环成Mode.normal时，屏幕再次回到黑色，才表示循环完成一次
			 */
			if (mode == Mode.waitForPictureReady) {
				mode = Mode.normal;
			}

			if (mode == Mode.normal) {
				mode = Mode.prepare;
				if (deviceCameraControl != null) {
					/*
					 * 首先button的点击可能与Gdx.input.isTouched()输入检测有冲突，然后，
					 * 通过仅屏蔽deviceCameraControl.prepareCameraAsync();
					 * 发现代码在render后
					 * ，button依然有效，但是prepareCameraAsync后button失效，确认代码范围
					 */
					deviceCameraControl.prepareCameraAsync();
				}
			}
			/* Mode.prepare之后不是Mode.preview，所以不能直接接入下边 */
			// if (mode == Mode.preview) {
			// mode = Mode.takePicture;
			// }

		}
	};

}
