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

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.loxodonta.obstacle.*;
import com.mygdx.game.util.AnimatedTexture;

public abstract class EnemyModel extends LivingEntity {
    protected static final float DEFAULT_RADIUS = 0.475f;
    protected static final float DEFAULT_KNOCKBACK = 1.2f;
    protected static final int DEFAULT_DAMAGE = 1;

    private float radius;

    protected int damage;
    protected float knockback;

    // used by Key and Chase
    public Vector2 targetPoint;
    public Vector2 tar;
    public boolean tarV;

    public EnemyModel(AnimatedTexture idle) {
        this(DEFAULT_RADIUS, idle);
    }

    public EnemyModel(float radius, AnimatedTexture idle) {
        this(0,0, radius, idle);
    }

    public EnemyModel(float x, float y, AnimatedTexture idle) {
        this(x, y, DEFAULT_RADIUS, idle);
    }

    public EnemyModel(float x, float y, float radius, AnimatedTexture idle) {
        super(new WheelObstacle(x, y, radius), idle);
        this.radius = radius;
        obstacle.setName("enemy");
        health = 1;
        maxHealth = 1;
        moveSpeed = 2;
        knockback = DEFAULT_KNOCKBACK;
        damage = DEFAULT_DAMAGE;

        type = EntityType.ENEMY;

        setGroundAndAir();
    }

    public void setGroundAndAir(){
        obstacle.setCategoryBits(1,1, 0, 1);
    }

    public void setGround(){
        obstacle.setCategoryBits(1,0, 0, 1);
    }

    public void setAir(){
        obstacle.setCategoryBits(0,1, 0, 1);
    }

    public float getRadius(){return radius;}

    public int getDamage() {return damage;}
    public float getKnockback() {return knockback;}

    // does nothing rn, animation later?
    public void dealDamage(){
        return;
    }

    public void rotateTowards(float x, float y){
        angle = (float)Math.atan2(y - obstacle.getY(), x - obstacle.getX());
        obstacle.setAngle(angle);
    }

    // Returns true if this killed the entity
    public boolean takeDamage(int damage, Vector2 kV) {
        if (!alive) return false;
        if (curInvTime > 0) return false;

        state = EntityState.STUN;

        return super.takeDamage(damage, kV);
    }


    public void setState(EntityState s) {
        if (state == s) return;

        switch (state){
            case MOVE:
                animationStream.stopAction(walkAnim);
                break;
            case IDLE:
                animationStream.stopAction(idleAnim);
                break;
            default:
                break;
        }

        switch (s){
            case MOVE:
                animationStream.playAction(walkAnim);
                break;
            case IDLE:
                animationStream.playAction(idleAnim);
                break;
            default:
                break;
        }
        state = s;
    }
}