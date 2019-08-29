package com.mygdx.game.loxodonta.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.loxodonta.GameCanvas;
import com.mygdx.game.loxodonta.obstacle.*;
import com.mygdx.game.util.AnimatedTexture;
import com.mygdx.game.util.AnimationStream;
import com.mygdx.game.util.Geometry;
import com.mygdx.game.util.TweenValue;

public abstract class LivingEntity {
    public enum EntityType {
        PLAYER, ENEMY
    }

    public enum EntityState {
        IDLE, MOVE, ATTACK, STUN, DASH_STUN
    }

    // Default physics values
    private static final float DEFAULT_DENSITY  =  1.0f;
    private static final float DEFAULT_FRICTION = 0.1f;
    private static final float DEFAULT_RESTITUTION = 0.4f;

    private static final float ANGLE_TRANSITION_RATE = (float)Math.PI*4; // rot / s
    private static final float STUN_TIME = 0.2f;
    private static final float KNOCKBACK_SCALE = 26f;
    private static final float KNOCKBACK_EXP = 0.85f; // exponential decay per 1/60 seconds

    protected float remaining_stun;
    protected Vector2 knockback_vector;

    protected int health;
    protected int maxHealth;
    protected float moveSpeed;
    protected boolean alive;

    public boolean respawn;

    protected float angle;
    protected TweenValue tweenAngle;

    protected SimpleObstacle obstacle;

    protected Vector2 cache;
    private Vector2 moveVector;

    protected EntityState state;
    protected EntityState nextState;

    protected float autoInvTime;
    protected float curInvTime;

    protected AnimatedTexture idleAnim;
    protected AnimatedTexture walkAnim;
    protected AnimationStream animationStream;

    public EntityType type;

    public LivingEntity(AnimatedTexture idleAnim) {
        obstacle = null;            // NOTE!
        angle = 0;

        maxHealth = 1;
        health = maxHealth;
        moveSpeed = 3;
        alive = true;

        state = EntityState.IDLE;

        respawn = true;

        cache = new Vector2();
        moveVector = new Vector2();

        autoInvTime = 1f * 0f;
        curInvTime = 0;

        remaining_stun = 0f;
        knockback_vector = new Vector2();
        animationStream = new AnimationStream(idleAnim);
        this.idleAnim = idleAnim;

        tweenAngle = new TweenValue();
        tweenAngle.setMod((float)Math.PI*2);
    }

    public void setObstacle(SimpleObstacle obs){
        obstacle = obs;
        angle = obs.getAngle();

        obstacle.setDensity(DEFAULT_DENSITY);
        obstacle.setFriction(DEFAULT_FRICTION);
        obstacle.setRestitution(DEFAULT_RESTITUTION);
    }

    public LivingEntity(SimpleObstacle obs, AnimatedTexture idleAnim) {
        obstacle = obs;
        maxHealth = 1;
        health = maxHealth;
        moveSpeed = 3;
        alive = true;
        angle = obs.getAngle();

        obstacle.setDensity(DEFAULT_DENSITY);
        obstacle.setFriction(DEFAULT_FRICTION);
        obstacle.setRestitution(DEFAULT_RESTITUTION);

        state = EntityState.IDLE;

        respawn = true;

        cache = new Vector2();
        moveVector = new Vector2();

        autoInvTime = 1f;
        curInvTime = 0;

        remaining_stun = 0f;
        knockback_vector = new Vector2();
        animationStream = new AnimationStream(idleAnim);
        this.idleAnim = idleAnim;

        tweenAngle = new TweenValue();
        tweenAngle.setMod((float)Math.PI*2);
    }

    public void delete(World world) {
        obstacle.deactivatePhysics(world);
        obstacle = null;
    }

    public void setHealth(int h){
        health = h;
        if (h<= 0)
            kill();
    }

    public int getCategoryBits(){
        return obstacle.getFilterData().categoryBits;
    }

    public void setGroundAndAir(){
        obstacle.setCategoryBits(1,1, 0);
    }

    public void setGround(){
        obstacle.setCategoryBits(1,0, 0);
    }

    public void setAir(){
        obstacle.setCategoryBits(0,1, 0);
    }

    public void setNoCollision(){
        obstacle.setCategoryBits(0, 0, 0);
    }

    public SimpleObstacle getObstacle() {
        return obstacle;
    }

    public Body getBody() {
        return obstacle.getBody();
    }

    /*public boolean activatePhysics(World world) {
        return physicsBody.activatePhysics(world);
    }*/

    // Call this function after object has been added to physics world
    public void physicsActivated() {
        obstacle.setFixedRotation(true);
        obstacle.setUserData(this);
    }

    public void setMoveDirection(float x, float y) {
        setVelocity(x, y, moveSpeed);
    }

    public void setVelocity(float x, float y, float multiplier) {
        if (!obstacle.isActive()) {
            return;
        }
        /*cache.set(x, y).scl(multiplier).sub(obstacle.getLinearVelocity()).scl(obstacle.getBody().getMass());
        obstacle.getBody().applyLinearImpulse(cache, obstacle.getBody().getPosition(), true);*/
        moveVector.set(x, y).scl(multiplier);
        /*cache.set(x,y).scl(multiplier).add(knockback_vector);
        obstacle.getBody().setLinearVelocity(cache);

        // set facing direction if player is moving
        // we'll need this for drawing at very least.
        if ((x != 0 || y != 0) && updateAngle) {
            angle = (float)Math.atan2(y, x);
            obstacle.setAngle(angle);
        }*/
    }

    public void step (float dt){
        if (!alive) return;
         switch (state){
            case STUN:
                if (remaining_stun <= 0) {
                    setState(EntityState.IDLE);
                }else{
                    remaining_stun -= dt;
                }
                break;

            case DASH_STUN:
                break;
        }

        cache.set(moveVector);
        if (state == EntityState.STUN || state == EntityState.DASH_STUN) {
            cache.scl(0);
        } else {
            setAngle(cache.x, cache.y);
            if (state == EntityState.IDLE && moveVector.len2() > 0) {
                setState(EntityState.MOVE);
            } else if (state == EntityState.MOVE && moveVector.len2() == 0) {
                setState(EntityState.IDLE);
            }
        }
        cache.add(knockback_vector);
        obstacle.getBody().setLinearVelocity(cache);

        if (knockback_vector.len2() > 0) {
            knockback_vector.scl((float)Math.pow(KNOCKBACK_EXP, dt*60));
            if (knockback_vector.len2() <= 0.005) {
                knockback_vector.set(0, 0);
            }
        }

        if (curInvTime > 0) {
            curInvTime = Math.max(0, curInvTime-dt);
        }

        tweenAngle.step(dt);
    }

    public void setInvincibility(float t){
        curInvTime = t;
    }
    public boolean isInvincible(){
        return curInvTime > 0;
    }

    public void knockback(float x, float y) {
        knockback_vector.add(x * KNOCKBACK_SCALE, y * KNOCKBACK_SCALE);
    }

    public void knockback(Vector2 kV){
        knockback(kV.x, kV.y);
    }

    public boolean kill() {
        if (!alive) return false;

        alive = false;
        health = 0;
        obstacle.setSensor(true);
        knockback_vector.set(0,0);
        setVelocity(0, 0, 0);
        obstacle.setLinearVelocity(0, 0);

        return true;
    }

    // Returns true if this killed the entity
    public boolean takeDamage(int damage, Vector2 kV) {
        if (!alive) return false;
        if (curInvTime > 0) return false;

        if (damage > 0) {
            curInvTime = autoInvTime;
        }

        health -= damage;
        if (health <= 0) {
            kill();
            return true;
        }else{
            if (kV.len2() > 0){
                knockback(kV);
            }
        }

        return false;
    }

    public void heal(int h) {
        if (!alive) return;

        health += h;
        if (health > maxHealth) health = maxHealth;
    }

    public void heal() {
        heal(maxHealth);
    }

    // fall into pit, instantly killing entity
    public void fall(PitModel pit){
        knockback_vector.set(0,0);
        kill();
    }

    public boolean isAlive() {
        return alive;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public float getHealthFraction() {
        return (float)health / maxHealth;
    }

    public int getFacingDirection8() {
        return Geometry.angleToNearestDirection(angle, 8);
    }

    public Vector2 getPosition(){ return obstacle.getPosition();}

    public float getAngle() {
        return angle;
    }

    public void setAngle(float x, float y) {
        if (x != 0 || y != 0) {
            setAngle((float)Math.atan2(y, x));
        }
    }

    public void setAngle(float f) {
        obstacle.setAngle(f);
        angle = f;
        tweenAngle.tweenConstantRate(angle, ANGLE_TRANSITION_RATE);
    }

    public float getRemainingStun() {
        return remaining_stun;
    }

    public void draw(GameCanvas canvas, float dt) {
        AnimatedTexture tex = animationStream.step(dt);
        canvas.draw(tex, Color.WHITE, tex.getRegionWidth()/2, tex.getRegionHeight()/2, obstacle.getX()*obstacle.getDrawScale().x, obstacle.getY()*obstacle.getDrawScale().y, (float)Math.toDegrees(angle),1,1);
        //obstacle.draw(canvas);
    }

    public EntityState getState() {
        return state;
    }

    public void setState(EntityState s) {
        state = s;
    }

    public void endDashStun() {
        if (state == EntityState.DASH_STUN){
            setState(EntityState.IDLE);
            obstacle.setMaskBits(1,1,1);
        }else if (nextState == EntityState.DASH_STUN){
            nextState = EntityState.STUN;
            remaining_stun = STUN_TIME;
        }
    }

    public void setMoveSpeed(int s){
        moveSpeed = s;
    }

}
