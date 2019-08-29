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
import com.mygdx.game.loxodonta.GameCanvas;

public class KeyModel extends ItemModel {
    public static Texture texture = new Texture("images/key.png");
    private static final Color[] KEY_COLORS = new Color[]{
            new Color(238f/255f, 18f/255f, 24f/255f, 1f),
            new Color(238f/255f, 94f/255f, 18f/255f, 1f),
            new Color(224f/255f, 225f/255f, 19f/255f, 1f),
            new Color(88f/255f, 165f/255f, 29/255f, 1f),
            new Color(29f/255f, 165f/255f, 164f/255f, 1f),
            new Color(29f/255f, 86f/255f, 165f/255f, 1f),
            new Color(165f/255f, 29f/255f, 128f/255f, 1f),
            //new Color(145f/255f, 30f/255f, 180f/255f, 1f),
    };
    private static final Color ENEMY_KILL_KEY_COLOR = new Color(213f/255f, 156f/255f, 158f/255f, 1f);
    public static final int ENEMY_KILL_KEY_ID = -1;

    private int keyId;
    private boolean active;
    private KeyEnemy spawner;

    public static Color getColor(int i) {
        if (i == ENEMY_KILL_KEY_ID) return ENEMY_KILL_KEY_COLOR;
        return KEY_COLORS[i % KEY_COLORS.length];
    }

    public KeyModel(int keyId) {
        this(0, 0, keyId, true);
    }

    public KeyModel(float x, float y, int keyId, boolean a) {
        super(x, y);
        this.keyId = keyId;

        setName("key");
        setTexture(new TextureRegion(texture, 0, 0, 50, 50));

        active = a;

        if (!active){
            setMaskBits(0,0,0);
        }

        spawner = null;
    }

    public void draw(GameCanvas canvas) {
        if (!active)
            return;

        if (texture != null) {
            //canvas.draw(texture, KEY_COLORS[keyId % KEY_COLORS.length], origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,(float)Math.toDegrees(getAngle()),1,1);
            canvas.draw(texture, getColor(keyId), origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,(float)Math.toDegrees(getAngle()),1,1);
        }
    }

    public int getKeyId() {
        return keyId;
    }

    public void setActive(boolean a){
        active = a;

        if (active){
            setMaskBits(1,1,0);
        }else{
            setMaskBits(0,0,0);
        }
    }

    public void setCollected(boolean b) {
        collected = b;
        if (spawner != null)
            spawner.keyCollected = true;
    }

    public void setSpawner(KeyEnemy k){
        spawner = k;
    }
}