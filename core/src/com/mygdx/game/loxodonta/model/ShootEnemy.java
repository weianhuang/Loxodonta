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
import com.mygdx.game.loxodonta.GameCanvas;
import com.mygdx.game.loxodonta.GameplayController;
import com.mygdx.game.loxodonta.ProjectileController;
import com.mygdx.game.util.AnimatedTexture;
import com.mygdx.game.util.SoundController;

/**
 * Player avatar for the rocket lander game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class ShootEnemy extends EnemyModel {
    public static Texture texture = new Texture("images/arachrat2_lookaround_100x100_6.png");
    private static Texture walkTexture = new Texture("images/arachrat2_walk_50x50_12.png");
    private static Texture attackTexture = new Texture("images/arachrat2_attack_50x50_6.png");

    public static TextureRegion getTextureStillFrame() {
        return new TextureRegion(texture, 0, 0, 100, 100);
    }

    private static final int DEFAULT_SIGHT_DIST = 20;
    private static final int DEFAULT_SIGHT_ANGLE = 360;
    private static final int DEFAULT_FIRERATE = 120;

    public int sight_distance;
    public int sight_angle;
    public Vector2 target_point;
    public boolean tracking;

    private int fireRate; // 60 = 1 second
    private float leftoverT;
    private int cooldown;

    private boolean turning;
    private boolean start;

    private AnimatedTexture attackAnim;

    public ShootEnemy() {
        this(DEFAULT_RADIUS);
    }

    public ShootEnemy(float radius) {
        this(0,0,radius);
    }

    public ShootEnemy(float x, float y) {
        this(x, y, DEFAULT_RADIUS, DEFAULT_SIGHT_DIST, DEFAULT_SIGHT_ANGLE, DEFAULT_FIRERATE);
    }

    public ShootEnemy(float x, float y, float radius) {
        this(x, y, radius, DEFAULT_SIGHT_DIST, DEFAULT_SIGHT_ANGLE, DEFAULT_FIRERATE);
    }

    public ShootEnemy(float x, float y, float radius, int sight_distance, int sight_angle, int fireRate) {
        super(x,y,radius,new AnimatedTexture(texture, 100, 100, 6, 1f, true, -1));
        walkAnim = new AnimatedTexture(walkTexture, 100, 100, 12, 0.1f, true, 1);
        attackAnim = new AnimatedTexture(attackTexture, 100, 100, 6, 0.1f, false, 1);
        moveSpeed = 4;
        this.sight_distance = sight_distance;
        this.sight_angle = sight_angle;
        target_point = new Vector2(x,y);
        tracking = false;

        this.fireRate = fireRate;
        cooldown = fireRate;
        leftoverT = 0;

        turning = false;
        start = true;
    }

    public boolean shoot(ProjectileController projectileController) {
        if (cooldown <= 0) {
            SoundController.getInstance().play("shootEnemy", GameplayController.ENEMY_SPIT_SOUND,false, 0.8f);
            animationStream.playAction(attackAnim);
            projectileController.newProjectile(obstacle.getX(), obstacle.getY(), target_point.x - obstacle.getX(), target_point.y - obstacle.getY(), ProjectileModel.ProjectileEnum.SPIT);
            cooldown += fireRate;
            return true;
        }else if (fireRate - cooldown < 30 && !start){
            start = false;
            return true;
        }

        return false;
    }

    public void step(float dt){
        leftoverT += dt*60;
        if (cooldown >0)
            cooldown -= (int)leftoverT;
        leftoverT = leftoverT % 1;
        super.step(dt);
    }

    public void rotateTowards(float x, float y){
        angle = (float)Math.atan2(y - obstacle.getY(), x - obstacle.getX());
        if (obstacle.getAngle() != angle){
            obstacle.setAngle(angle);
            if (!turning) {
                animationStream.playAction(walkAnim);
                turning = true;
            }
        }else{
            if (turning){
                animationStream.stopAction(walkAnim);
                turning = false;
            }
        }

    }

    public void stopRotation(){
        if (turning){
            animationStream.stopAction(walkAnim);
        }
        turning= false;
    }

    public void draw(GameCanvas canvas) {
        obstacle.draw(canvas, Color.RED);
    }

}