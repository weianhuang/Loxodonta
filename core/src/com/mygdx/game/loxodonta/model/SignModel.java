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
import com.mygdx.game.loxodonta.obstacle.BoxObstacle;

public class SignModel extends BoxObstacle {
    public TextureRegion texture;
    public int x;
    public int width;
    public int y;
    public int height;

    public SignModel(int x, int y, int width, int height, String imgPath) {
        super(x , y , width , height );

        try {
            texture = new TextureRegion(new Texture("images/" + imgPath), 0, 0, width * 50, height * 50);
        }catch (NullPointerException n){
            texture = new TextureRegion(new Texture("images/x_img.png"), 0, 0, 50, 50);
        }
        setName("sign");

        setSensor(true);
        setBodyType(BodyDef.BodyType.StaticBody);

        setMaskBits(0,0,0);
        setTexture(texture);

        this.x = x;
        this.width = width;
        this.y = y;
        this.height = height;
    }
}