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
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.util.AnimatedTexture;

public class ChaseEnemy extends EnemyModel {
    public static Texture texture = new Texture("images/arachrat_lookaround_100x100_4.png");
    public static Texture walkTexture = new Texture("images/arachrat_walk_100x100_12.png");
    public static TextureRegion getTextureStillFrame() {
        return new TextureRegion(texture, 0, 0, 100, 100);
    }

    private static final int DEFAULT_SIGHT = 10;
    public int sight_distance;
    public boolean chasing;

    public ChaseEnemy() {
        this(DEFAULT_RADIUS);
    }

    public ChaseEnemy(float radius) {
        this(0,0,radius);
    }

    public ChaseEnemy(float x, float y) {
        this(x, y, DEFAULT_RADIUS, DEFAULT_SIGHT);
    }

    public ChaseEnemy(float x, float y, float radius) {
        this(x, y, radius, DEFAULT_SIGHT);
    }

    public ChaseEnemy(float x, float y, float radius, int sight_distance) {
        super(x,y,radius,new AnimatedTexture(texture, 100, 100, 4, 1f, true, -1));
        walkAnim = new AnimatedTexture(walkTexture, 100, 100, 12, 0.1f, true, 1);
        moveSpeed = 3;
        this.sight_distance = sight_distance;
        chasing = false;
        tarV = false;
    }
}