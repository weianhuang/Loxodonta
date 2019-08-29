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
import com.mygdx.game.loxodonta.GameplayController;
import com.mygdx.game.loxodonta.ProjectileController;
import com.mygdx.game.loxodonta.obstacle.BoxObstacle;
import com.mygdx.game.util.AnimatedTexture;
import com.mygdx.game.util.AnimationStream;
import com.mygdx.game.util.Geometry;
import com.mygdx.game.util.SoundController;

public class ProjectileSpawnModel extends BoxObstacle {
    public static Texture texture = new Texture("images/turret_atRest_50x50.png");
    public static Texture fireTexture = new Texture("images/turret_firing_50x50_4.png");
    private static final int DEFAULT_FIRERATE = 140;

    private int fireRate; // 60 = 1 second
    private float leftoverT;
    private int cooldown;

    private AnimatedTexture idleAnim;
    private AnimatedTexture fireAnim;
    private AnimationStream animationStream;

    public ProjectileSpawnModel(int cooldown) {
        this(DEFAULT_FIRERATE, cooldown);
    }

    public ProjectileSpawnModel(int fireRate, int cooldown) {
        super(1,1);

        setBodyType(BodyDef.BodyType.StaticBody);
        setCategoryBits(0,0,0);
        setMaskBits(0,0,0);
//        setTexture(texture);

        idleAnim = new AnimatedTexture(texture, 50, 50, 1, 10f, true, -1);
        fireAnim = new AnimatedTexture(fireTexture, 50, 50, 4, 0.1f, false, 1);
        animationStream = new AnimationStream(idleAnim);

        if (fireRate == 0) fireRate = DEFAULT_FIRERATE;

        this.fireRate = fireRate;
        this.cooldown = cooldown;

        leftoverT = 0;
    }

    public ProjectileSpawnModel() {
        this(DEFAULT_FIRERATE, 0);
    }

    public void step(ProjectileController projectileController, float dt) {
        leftoverT += dt*60;
        cooldown -= (int)leftoverT;
        leftoverT = leftoverT % 1;
        if (cooldown <= 0) {
            animationStream.playAction(fireAnim);
            SoundController.getInstance().play("turret", GameplayController.TURRET_SOUND,false, 0.1f);
            Vector2 ang = Geometry.angleToVector(getAngle());
            projectileController.newProjectile(getX(), getY(), ang.x, ang.y, ProjectileModel.ProjectileEnum.BASIC);
            cooldown += fireRate;
        }
        animationStream.step(dt);
    }

    public void draw(GameCanvas canvas) {
        Vector2 v = getDimension();
        canvas.draw(animationStream.step(0), Color.WHITE, v.x/2*drawScale.x, v.y/2*drawScale.y, getX()*drawScale.x, getY()*drawScale.y, (float)Math.toDegrees(getAngle()), 1, 1);
        //WALL_NINEPATCH.draw(canvas.spriteBatch, (getX() - v.x/2)*drawScale.x, (getY() - v.y/2)*drawScale.y, v.x*drawScale.x, v.y*drawScale.y);
    }
}