package com.mygdx.game.loxodonta.model;

import com.mygdx.game.loxodonta.obstacle.FixtureData;
import com.mygdx.game.loxodonta.obstacle.SimpleObstacle;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.physics.box2d.Shape;

public class Hurtbox extends FixtureData{
    public enum HurtboxType{
        DASH, MELEE
    }

    private LivingEntity entity;
    public HurtboxType type;
    private boolean active;

    private float knockback;
    private int damage;

    public Hurtbox(Shape s, HurtboxType t, float knockback, int damage){
        super(s, SimpleObstacle.getBits(1,1,1, 0), SimpleObstacle.getBits(0,0,0, 1), true);
        userData = this;
        type = t;
        shape = s;
        this.knockback = knockback;
        this.damage = damage;

        active = false;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public void setActive(boolean a){
        active = a;
        if(!active){
            maskBits = SimpleObstacle.getBits(0,0,0,0);
            Filter f = fixture.getFilterData();
            f.maskBits = maskBits;
            fixture.setFilterData(f);
        }else{
            maskBits = SimpleObstacle.getBits(1,1,0, 1);
            Filter f = fixture.getFilterData();
            f.maskBits = maskBits;
            fixture.setFilterData(f);
        }
    }

    public boolean getActive(){ return active;}

    public void setEntity(LivingEntity e){
        entity = e;
    }

    public float getKnockback() {
        return knockback;
    }

    public int getDamage() {
        return damage;
    }
}
