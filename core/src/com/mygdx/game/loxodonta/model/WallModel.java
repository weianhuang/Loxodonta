/*
 * EnemyModel.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Note how this class combines physics and animation.  This is a good template
 * for models in your game.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package com.mygdx.game.loxodonta.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.mygdx.game.loxodonta.GameCanvas;
import com.mygdx.game.loxodonta.obstacle.BoxObstacle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class WallModel extends BoxObstacle {
    public static final Texture texture = new Texture("images/innerWall_single_50x50.png");
    private static final NinePatch WALL_TEXTURE = new NinePatch(new Texture("images/innerWall.png"), 4, 4, 4, 4);
    private static final NinePatchDrawable WALL_NINEPATCH = new NinePatchDrawable(WALL_TEXTURE);

    private TextureRegion textureRegion;

    public WallModel(int sizeX, int sizeY) {
        super(sizeX, sizeY);
        setBodyType(BodyDef.BodyType.StaticBody);
        setCategoryBits(1,1,0);
        setMaskBits(1,1,0);
        textureRegion = new TextureRegion(texture, 0, 0, 50*sizeX, 50*sizeY);
        textureRegion.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        //setTexture(texture);
    }

    public void draw(GameCanvas canvas) {
        Vector2 v = getDimension();
        canvas.draw(textureRegion, Color.WHITE, v.x/2*drawScale.x, v.y/2*drawScale.y, getX()*drawScale.x, getY()*drawScale.y, 0, 1, 1);
        //WALL_NINEPATCH.draw(canvas.spriteBatch, (getX() - v.x/2)*drawScale.x, (getY() - v.y/2)*drawScale.y, v.x*drawScale.x, v.y*drawScale.y);
    }
}