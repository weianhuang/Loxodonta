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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.util.AnimatedTexture;

public class StaticEnemy extends EnemyModel {
    public static Texture texture = new Texture("images/ratbatross_fly_126x126_11.png");//new Texture("images/arachrat_lookaround_100x100_4.png");
    public static TextureRegion getTextureStillFrame() {
        return new TextureRegion(texture, 0, 0, 126, 126);
    }

    public StaticEnemy() {
        this(DEFAULT_RADIUS);
    }

    public StaticEnemy(float radius) {
        this(0,0,radius);
    }

    public StaticEnemy(float x, float y) {
        this(x, y, DEFAULT_RADIUS);
    }

    public StaticEnemy(float x, float y, float radius) {
        super(x,y,radius,new AnimatedTexture(texture, 125, 125, 11, 0.1f, true, -1));
        //obstacle.setTexture(texture);
        setAir();
    }
}