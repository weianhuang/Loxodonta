/*
 * BoxObject.java
 *
 * Given the name Box2D, this is your primary model class.  Most of the time,
 * unless it is a player controlled avatar, you do not even need to subclass
 * BoxObject.  Look through the code and see how many times we use this class.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package com.mygdx.game.loxodonta.obstacle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.loxodonta.GameCanvas;

/**
 * Box-shaped model to support collisions.
 *
 * Unless otherwise specified, the center of mass is as the center.
 */
public class MultiFixtureObstacle extends SimpleObstacle {
	/** Shape information for this box */
	protected FixtureData fixtureData[];
	private Fixture mainFix;
	private Shape shapes[];

	public void setTexture(TextureRegion value) {
		super.setTexture(value);
	}

	public Fixture getMainFix(){
		return mainFix;
	}

	/**
	 * Creates a new multifixture object, with fixtures of the specified shapes.
	 *
	 * The size is expressed in physics units NOT pixels.  In order for
	 * drawing to work properly, you MUST set the drawScale. The drawScale
	 * converts the physics units to pixels.
	 *
	 * @param x  		Initial x position of the box center
	 * @param y  		Initial y position of the box center
	 */
	public MultiFixtureObstacle(float x, float y, FixtureData[] fixtureData) {
		super(x,y);
		this.fixtureData = fixtureData;

		shapes = new Shape[fixtureData.length];
		for (int i=0; i<fixtureData.length; i++){
			shapes[i] = fixtureData[i].shape;
		}
	}

	/**
	 * Create new fixtures for this body, defining the shape
	 *
	 * This is the primary method to override for custom physics objects
	 */
	protected void createFixtures() {
		if (body == null) {
			return;
		} 
		
	    releaseFixtures();

		// Create the fixtures
		for (int i=0; i<fixtureData.length; i++){
			FixtureDef f = new FixtureDef();
			FixtureData fix = fixtureData[i];
			f.shape = fix.shape;
			f.filter.categoryBits = fix.categoryBits;
			f.filter.maskBits = fix.maskBits;
			f.isSensor = fix.sensor;
			Fixture fixture = body.createFixture(f);
			fixture.setUserData(fix.userData);
			fix.setFixture(fixture);

			if (i==0)
				mainFix = fixture;
		}
	}
	
	/**
	 * Release the fixtures for this body, resetting the shape
	 *
	 * This is the primary method to override for custom physics objects
	 */
	protected void releaseFixtures() {
	    for (Fixture f :body.getFixtureList()){
	    	body.destroyFixture(f);
		}
	}

	public void draw(GameCanvas canvas) {
		if (texture != null) {
			canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,(float)Math.toDegrees(getAngle()),shapes[0].getRadius());
		}
		else
		{
			canvas.drawPhysics(shapes[0], Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
		}
	}
	
	/**
	 * Draws the outline of the physics body.
	 *
	 * This method can be helpful for understanding issues with collisions.
	 *
	 * @param canvas Drawing context
	 */
	public void drawDebug(GameCanvas canvas) {
		for (Shape s : shapes)
			canvas.drawPhysics(s, Color.YELLOW,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
	}

	public void setCategoryBits(int ground, int air, int hurtbox, int enemy){
		Filter filter = mainFix.getFilterData();
		filter.categoryBits = getBits(ground, air, hurtbox, enemy);
		mainFix.setFilterData(filter);
	}

	public void setMaskBits(int ground, int air, int hurtbox, int enemy){
		Filter filter = mainFix.getFilterData();
		filter.maskBits = getBits(ground, air, hurtbox, enemy);
		mainFix.setFilterData(filter);
	}


}