package com.mygdx.game0606.actor;

/**
 * @author  54wall 
 * @date 创建时间：2016-7-21 下午1:16:58
 * @version 1.0 
 */
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class FirstActor extends Actor {
	Texture texture;

	public void draw(SpriteBatch batch, float parentAlpha) {
		batch.draw(texture, this.getX(), this.getY());
	}

	public Actor hit(float x, float y) {
		if (x > 0 && y > 0 && this.getHeight() > y && this.getWidth() > x) {
			return this;
		} else {
			return null;
		}
	}

	public boolean touchDown(float x, float y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	public void touchDragged(float x, float y, int pointer) {
		// TODO Auto-generated method stub
	}

	public void touchUp(float x, float y, int pointer) {
		// TODO Auto-generated method stub
	}

	public FirstActor(String name) {
		super();
		texture = new Texture(Gdx.files.internal("data/james.png"));
//		this.height = texture.getHeight();
//		this.width = texture.getWidth();
	}
}
