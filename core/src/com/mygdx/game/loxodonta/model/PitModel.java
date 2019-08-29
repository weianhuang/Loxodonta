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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.game.loxodonta.GameCanvas;
import com.mygdx.game.loxodonta.obstacle.BoxObstacle;
import com.mygdx.game.util.AnimatedTexture;
import com.mygdx.game.util.AnimationStream;

public class PitModel extends BoxObstacle {
    public static Texture texture = new Texture("images/WhitePixel.png");//"images/maroongoo_50x50.png");
    private static final AnimatedTexture[] anims = {
        new AnimatedTexture(new Texture("images/bubble1_50x50_9.png"), 50, 50, 9, 0.5f, true, 0),
        new AnimatedTexture(new Texture("images/bubble2_50x50_4.png"), 50, 50, 4, 0.5f, true, 0),
        new AnimatedTexture(new Texture("images/bubble3_50x50_4.png"),  50, 50, 4, 0.5f, true, 0),
        new AnimatedTexture(new Texture("images/goo_50x50.png"),  50, 50, 1, 10000f, true, 0)
    };

    private static final Color COLOR = Color.WHITE;//new Color(0.8f, 0.1f, 0.1f, 1f);
    private static final double inset = PlayerModel.DEFAULT_RADIUS;

    private TextureRegion textureRegion;

    public int x;
    public int width;
    public int y;
    public int height;

    private AnimationStream[][] animations;

    public PitModel(int width, int height) {
        this(0, 0, width, height);
    }

    public PitModel(int x, int y, int width, int height) {
        super(x + (float)inset, y + (float)inset, width - (float)inset * 2, height - (float)inset * 2);
        setName("pit");

        setSensor(true);
        setBodyType(BodyDef.BodyType.StaticBody);

        setMaskBits(1,0,0, 0);
        textureRegion = new TextureRegion(texture, 0, 0, 50*width, 50*height);
        textureRegion.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        this.x = x;
        this.width = width;
        this.y = y;
        this.height = height;

        int seed = width * height + (int)Math.sqrt(x + y) + width % 2 + height % 3 + (width / (height + 1));
        animations = new AnimationStream[width][height];
        for (int xx=0; xx<width; xx++) {
            for (int yy=0; yy<height; yy++) {
                int pseed = seed;
                seed = (seed * 1103515245 + 12345) & 0x7fffffff - x * x;
                seed = seed / 10;
                int x0 = x + xx;
                int y0 = y + yy;
                animations[xx][yy] = new AnimationStream(anims[seed % anims.length].duplicate());
                animations[xx][yy].step((pseed % 100) / 100f);
            }
        }
    }

    public void draw(GameCanvas canvas, float dt) {
        float start_x = (float)(getX() - inset - (float)width/2f + 1);
        float start_y = (float)(getY() - inset - (float)height/2f + 1);
        for (int xx=0; xx<width; xx++) {
            for (int yy=0; yy<height; yy++) {
                canvas.draw(animations[xx][yy].step(dt), COLOR, drawScale.x/2f, drawScale.y/2f, (start_x + xx) * drawScale.x - 1, (start_y + yy) * drawScale.y - 1, 0, 1, 1);
            }
        }
    }
}