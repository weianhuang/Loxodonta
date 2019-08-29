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

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.game.loxodonta.obstacle.WheelObstacle;

public abstract class ItemModel extends WheelObstacle {
    protected static final float DEFAULT_RADIUS = 0.475f;

    protected boolean collected;

    public ItemModel(float x, float y) {
        super(x, y, DEFAULT_RADIUS);

        setName("item");

        setSensor(true);
        setBodyType(BodyDef.BodyType.StaticBody);

        setCategoryBits(1,1,0);
        setMaskBits(1,1,0);
        collected = false;
    }

    public void setCollected(boolean b) {
        collected = b;
    }

    public boolean getCollected() {
        return collected;
    }
}