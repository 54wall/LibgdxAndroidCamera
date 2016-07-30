package com.mygdx.game0606.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

/**
 * @author 54wall
 * @date 创建时间：2016-7-21 下午3:51:09
 * @version 1.0
 * http://veikr.com/201203/libgdx_load_texture_network_async.html
 * libgdx中异步从网络加载图片
 */
public class GetUrlImg {

	public void getImg() throws MalformedURLException, IOException {
		/* 从url读取byte[] */
		InputStream is = new URL("").openStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int length = 0;
		byte[] bytes = new byte[1024];
		while ((length = is.read(bytes)) != -1) {
			out.write(bytes, 0, length);
		}
		is.close();
		out.flush();
		byte[] rtn = out.toByteArray();

		/* 使用byte[]生成一个pixmap */
		Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);

		/* 将pixmap画到texture上 */
		int width = pixmap.getWidth();
		int height = pixmap.getHeight();
		int preferWidth = MathUtils.nextPowerOfTwo(width);
		int preferHeight = MathUtils.nextPowerOfTwo(height);
		Texture texture = new Texture(preferWidth, preferHeight,
				pixmap.getFormat());
		texture.draw(pixmap, 0, 0);
		pixmap.dispose();
		/* 构造textureRegion */
		TextureRegion region = new TextureRegion(texture, 0, 0, width, height);
	}

	public void name() {
		TextureRegion region = new TextureRegion(new Texture(1, 1,
				Format.RGBA8888));
		loadTextureRegionFromUrl(region, "http://sss.com/asdf.png");
	}

	public static void loadTextureRegionFromUrl(final TextureRegion region,
			final String url) {
		if (region == null) {
			throw new NullPointerException("region不能为空");
		}
		new Thread() {
			@Override
			public void run() {
				/* 还没有新建ReaderHelper */
				// final byte[] bytes = ReaderHelper.getBytesFromUrl(url);
				final byte[] bytes = null;
				System.out.println(url + " 图片获取成功：" + bytes.length);
				Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run() {
						Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
						int width = pixmap.getWidth();
						int height = pixmap.getHeight();
						int preferWidth = MathUtils.nextPowerOfTwo(width);
						int preferHeight = MathUtils.nextPowerOfTwo(height);
						Texture texture = new Texture(preferWidth,
								preferHeight, pixmap.getFormat());
						texture.draw(pixmap, 0, 0);
						pixmap.dispose();
						region.setTexture(texture);
						region.setRegion(0, 0, width, height);
						System.out.println(region.getRegionWidth());
						System.out.println(region.getRegionHeight());
						System.out.println(region.getTexture().getHeight());
					}
				});
			};
		}.start();
	}

}
