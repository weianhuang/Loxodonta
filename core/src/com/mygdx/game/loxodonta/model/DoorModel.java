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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.game.loxodonta.GameCanvas;
import com.mygdx.game.loxodonta.obstacle.BoxObstacle;

public class DoorModel extends BoxObstacle {
    public static TextureRegion texture = new TextureRegion(new Texture("images/door_50x150.png"), 0, 0, 50, 150);
    public static TextureRegion lowTexture = new TextureRegion(new Texture("images/door_bottom_50x150.png"), 0, 0, 50, 150);
    public static TextureRegion highTexture = new TextureRegion(new Texture("images/door_top_50x150.png"), 0, 0, 50, 150);
    public static TextureRegion underlayTexture = new TextureRegion(new Texture("images/doorKeyLocked_50x100.png"), 0, 0, 50, 100);
    public static TextureRegion underlayEnemyTexture = new TextureRegion(new Texture("images/doorEnemyLocked_50x100.png"), 0, 0, 50, 100);

    private int keyId;
    private boolean locked;
    public int index; // for roomcontroller

    public DoorModel(float x, float y) {
        this(x, y, false, KeyModel.ENEMY_KILL_KEY_ID);
    }

    public void setLocked(boolean b) {
        locked = b;
        if (locked) {
            setMaskBits(1, 1, 0, 1);
        } else {
            setMaskBits(0, 0, 0, 1);
        }
    }

    public boolean isEnemyLocked() {
        return locked && keyId == KeyModel.ENEMY_KILL_KEY_ID;
    }

    public DoorModel(float x, float y, boolean locked, int keyId) {
        super(x, y);
        setKeyId(keyId);
        setLocked(locked);

        setName("door");
        setBodyType(BodyDef.BodyType.StaticBody);
        setTexture(texture);
        setCategoryBits(1,1,0,0);
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getKeyId() {
        return keyId;
    }

    public boolean getLocked() {
        return locked;
    }

    public void drawBottom(GameCanvas canvas){
        float ang = (float)Math.toDegrees(getAngle());
        if (locked) {
            TextureRegion underlay = (keyId == KeyModel.ENEMY_KILL_KEY_ID) ? underlayEnemyTexture : underlayTexture;
            canvas.draw(underlay, KeyModel.getColor(keyId), origin.x, origin.y - 25, getX() * drawScale.x, getY() * drawScale.y, ang, 1, 1);
        }
        canvas.draw(lowTexture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, ang, 1, 1);
    }
    public void drawTop(GameCanvas canvas) {
        float ang = (float)Math.toDegrees(getAngle());
        canvas.draw(highTexture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, ang, 1, 1);
    }
}