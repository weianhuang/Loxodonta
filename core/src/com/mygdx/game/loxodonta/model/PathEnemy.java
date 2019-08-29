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
import com.mygdx.game.loxodonta.model.EnemyModel;
import com.mygdx.game.util.AnimatedTexture;

public class PathEnemy extends EnemyModel {
    public static Texture texture = new Texture("images/ratbatross_fly_126x126_11.png");
    public static Texture walkTexture = new Texture("images/ratbatross_fly_126x126_11.png");
    public static TextureRegion getTextureStillFrame() {
        return new TextureRegion(texture, 0, 0, 126, 126);
    }

    private int[] pathPointsX;
    private int[] pathPointsY;
    private int curr_index;

    public Vector2 getTargetPoint(){
        return new Vector2(pathPointsX[curr_index], pathPointsY[curr_index]);
    }

    public void nextTarget(){
        curr_index = (curr_index + 1) % pathPointsY.length;
    }

    public PathEnemy(int[] pathX, int[] pathY) {
        this(DEFAULT_RADIUS, pathX, pathY);
    }

    public PathEnemy(float radius, int[] pathX, int[] pathY) {
        this(0,0,radius, pathX, pathY);
    }

    public PathEnemy(float x, float y, int[] pathX, int[] pathY) {
        this(x, y, DEFAULT_RADIUS, pathX, pathY);
    }

    public PathEnemy(float x, float y, float radius, int[] pathX, int[] pathY) {
        super(x,y,radius,new AnimatedTexture(texture, 125, 125, 11, 0.1f, true, -1));
        pathPointsX = pathX;
        pathPointsY = pathY;
        curr_index = 0;
        walkAnim = new AnimatedTexture(walkTexture, 125, 125, 11, 0.1f, true, 1);
        setAir();
    }

    public PathEnemy(float x, float y) {
        this(x, y, DEFAULT_RADIUS, new int[] {(int)x}, new int[] {(int)y});
    }
}