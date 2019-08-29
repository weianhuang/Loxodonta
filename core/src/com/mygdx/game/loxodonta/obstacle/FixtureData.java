package com.mygdx.game.loxodonta.obstacle;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class FixtureData {
    public short categoryBits;
    public short maskBits;
    public boolean sensor;
    public Object userData;
    public Shape shape;

    public Vector2 offset;

    protected Fixture fixture;

    public FixtureData(Shape shape, short categoryBits, short maskBits, boolean sensor){
        this.categoryBits = categoryBits;
        this.maskBits = maskBits;
        this.sensor = sensor;
        this.shape = shape;

        offset = new Vector2(0,0);
    }

    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }
}