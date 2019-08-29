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
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.game.loxodonta.obstacle.WheelObstacle;

public class PortalModel extends WheelObstacle {
    public static Texture texture = new Texture("images/bluePortal_100x100.png");

    public PortalModel(float x, float y) {
        super(x+0.5f, y+0.5f, 1);

        setSensor(true);
        setBodyType(BodyDef.BodyType.StaticBody);
        setMaskBits(1,1,0, 0);

        setName("portal");
        setTexture(new TextureRegion(texture, 0, 0, 100, 100));
    }
}