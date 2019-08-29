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

public class PeanutModel extends ItemModel {
    public static Texture texture = new Texture("images/peanut.png");

    public PeanutModel(int value) {
        this(0,0);
    }

    public PeanutModel(float x, float y) {
        super(x, y);

        setName("peanut");
        setTexture(new TextureRegion(texture, 0, 0, 100, 100));
    }

    public int getHealing() {
        return 3;
    }
}