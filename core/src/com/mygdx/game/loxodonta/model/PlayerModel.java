/*
 * PlayerModel.java
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

import com.mygdx.game.loxodonta.GameCanvas;
import com.mygdx.game.loxodonta.GameplayController;
import com.mygdx.game.util.*;
import com.mygdx.game.loxodonta.obstacle.*;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.loxodonta.obstacle.MultiFixtureObstacle;

import java.util.ArrayList;

public class PlayerModel extends LivingEntity {
	public enum PlayerState{
		MOVE, DASH, DASH_FINISH, DASH_PAUSE, MELEE, FALL, WARP
	}

	private static final Texture idleTexture = new Texture("images/L427_bob_50x50_6.png");
	private static final Texture dashTexture = new Texture("images/L427_dash_200x201_4.png");
    private static final Texture dashIndicatorTexture = new Texture("images/dashIndicatorContinuous_100x100.png");
    private static final Texture fallTexture = new Texture("images/L427_fallingInPit_50x50_6.png");
	public static final TextureRegion keyBindingTexture = new TextureRegion(new Texture("ui/key_binding.png"), 0, 0, 787, 1425);
	private static final Texture meleeTexture = new Texture("images/L427_melee_50x50_7.png");
	private static final Texture warpTexture = new Texture("images/teleport_6.png");
	private static final TextureRegion keyTextureRegion = new TextureRegion(KeyModel.texture, 50, 50);

	private static final Color FULL_COLOR = new Color(119f/255f, 75f/255f, 234f/255f, 1f);
	private static final Color RECHARGE_COLOR = new Color(244f/255f, 240f/255f, 7f/255f, 1f);

	public static final float DEFAULT_RADIUS = 0.475f;
	// time required to wait between dashes
	private static final float DASH_COOLDOWN = 1.5f;
	private static final float MELEE_COOLDOWN = 0.15f;
	private static final float MELEE_DURATION = 0.25f;
	private static final float DASH_DURATION = 0.2f;
	private static final float DASH_SPEED_MULTIPLIER = 3.5f;
	private static final float DASH_HURTBOX_MULTIPLIER = 1.2f;
	private static final float REDASH_DELAY = 1f;
	private static final float REDASH_DECAY = 0.8f;
	private static final float TIME_SLOWDOWN = 0.2f;
	private static final float FALL_TIME_SLOWDOWN = 0.2f;
	private static final float TIME_SLOWDOWN_PROP = 0.33f;

	private static final float DASH_KNOCKBACK = 2f;
	private static final float MELEE_KNOCKBACK = 1f;
	private static final int DASH_DAMAGE = 1;
	private static final int MELEE_DAMAGE = 0;
	private static final int FALL_DAMAGE = 2;

	private static final int DEFAULT_MAX_HEALTH = 12;

	private static final int dashMeterSizeX = 250;
	private static final int dashMeterSizeY = 30;
	private static final int dashMeterBorderSize = 1;
	private static final int healthOffset = 60;
    private static final TextureRegion dashIndicatorFill = new TextureRegion(dashIndicatorTexture, 0, 0, 100, 100);
	private static final TextureRegion dashMeterFill = new TextureRegion(new Texture("images/dashmeter_full.png"), 0, 0, dashMeterSizeX, dashMeterSizeY);
	private static final TextureRegion dashMeterOutline = new TextureRegion(new Texture("images/emptybar.png"), 0, 0, dashMeterSizeX, dashMeterSizeY);
	private static final TextureRegion meleeFillTexture = new TextureRegion(new Texture("images/melee.png"), 35,47);
	private static final TextureRegion heartOutline = new TextureRegion(new Texture("images/empty_heart.png"), 0, 0, 50, 50);
	private static final TextureRegion halfHeart = new TextureRegion(new Texture("images/half_heart.png"), 0, 0, 50, 50);
	private static final TextureRegion fullHeart = new TextureRegion(new Texture("images/full_heart.png"), 0, 0, 50, 50);

	private AnimatedTexture dashAnimation;
	private AnimatedTexture fallAnimation;
	private AnimatedTexture meleeAnimation;
	private AnimatedTexture warpAnimation;

	//private static final Color dashMeterColor = new Color(234f/255f, 20f/255f, 140f/255f, 1f);

	// time to wait until can dash again
	private float dashCooldown = 0f;
	// time to wait until can melee again
	private float meleeCooldown = 0f;
	// time remaining in current melee
	private float meleeRemaining = 0f;
	// time remaining in current dash
	private float dashRemaining = 0f;
	// time to react after kill
	private float redashRemaining = 0f;
	// number of chain kills
	private int chain = 0;

	private float fallingTime = 0f;
	private float warpingTime = 0f;
	private float dashFinishTime;
	private float dashFinishTimeMax;

	private boolean levelEndWarp;
	private boolean warpAnimating;

	private ArrayList<Integer> keys;

	private PlayerState state;

	private PlayerState prevState;

	private Vector2 dashVector;
	private Vector2 dashTargetPos;
	private Vector2 dashLastPos;
	private Vector2 respawnPos;
	private Vector2 temp;
	private Vector2 temp2;

	public Hurtbox dashHurtbox;

	public Hurtbox meleeHurtbox;

	public boolean requestRoomReset;

	public TweenValue timeValue;
	public ShockWaveShader shockWaveShader;
	private ShaderProgram radialShader;

	public boolean hasKey(int key) {
		return keys.contains(key);
	}

	public void giveKey(int key) {
		if (hasKey(key)) return;
		keys.add(key);
	}

	private float getRedashDelay() {
		return REDASH_DELAY * (float)Math.pow(REDASH_DECAY, chain);
	}

	private float getDashSpeed(float f) {
		float fmin = 0.75f;
		if (f <= fmin) return moveSpeed * DASH_SPEED_MULTIPLIER;
		return moveSpeed + moveSpeed * (f - fmin) / (1f - fmin) * (DASH_SPEED_MULTIPLIER - 1);
	}

	public void setState(PlayerState s) {
		if (state == s)
			return;

		prevState = state;
		switch (prevState) {
			case MELEE:
				animationStream.stopAction(meleeAnimation);
				meleeRemaining = 0;
				meleeHurtbox.setActive(false);
				break;
			case DASH:
				setGroundAndAir();
				dashRemaining = 0;
				dashHurtbox.setActive(false);
				setVelocity(0, 0, 0);
				break;
			case DASH_FINISH:
				break;
			case DASH_PAUSE:
				timeValue.tweenLinear(1f, getRedashDelay() * TIME_SLOWDOWN_PROP);
				break;
			case FALL:
				animationStream.stopAction(fallAnimation);
				timeValue.tweenLinear(1f,0.5f);
				setGroundAndAir();
				break;
		}

		state = s;
		switch (state) {
			case DASH:
				animationStream.playAction(dashAnimation);
				setAir();
				dashHurtbox.setActive(true);
				break;
			case DASH_FINISH:
				dashLastPos.set(getPosition());
				dashFinishTimeMax = temp.set(dashTargetPos).sub(dashLastPos).len() / (moveSpeed * DASH_SPEED_MULTIPLIER) * 2;
				dashFinishTime = 0;
				timeValue.tweenLinear(TIME_SLOWDOWN,getRedashDelay() * TIME_SLOWDOWN_PROP);
				break;
			case DASH_PAUSE:
				setAir();
				setVelocity(0, 0, 0);
				break;
			case MELEE:
				animationStream.playAction(meleeAnimation);
				setGroundAndAir();
				meleeHurtbox.setActive(true);
				break;
			case FALL:
				knockback_vector.set(0, 0);
				animationStream.playAction(fallAnimation);
				timeValue.tweenLinear(FALL_TIME_SLOWDOWN,0.5f);
				setVelocity(0, 0, 0);
				setNoCollision();
				setInvincibility(3f);
				fallingTime = 1f;
				break;
			case WARP:
				warpingTime = 0f;
				knockback_vector.set(0, 0);
				setVelocity(0, 0, 0);
				setNoCollision();
				setInvincibility(3f);
				break;
		}
	}

	public void beginDash(float x, float y) {
		SoundController.getInstance().play("dash", GameplayController.DASH_SOUND,false, 0.8f);

		if (x == 0 && y == 0) {
			x = (float)Math.cos(angle);
			y = (float)Math.sin(angle);
		}

		dashVector.set(x, y);
		dashRemaining = DASH_DURATION;
		dashCooldown = DASH_COOLDOWN;

		setInvincibility(DASH_DURATION);

		setState(PlayerState.DASH);
	}

	public void endDash() {
		chain = 0;
		setGroundAndAir();
        animationStream.stopAction(dashAnimation);
        setState(PlayerState.MOVE);
	}

	public boolean takeDamage(int damage, Vector2 kV) {
		if (state == PlayerState.DASH || state == PlayerState.DASH_PAUSE){
			endDash();
		}

		return super.takeDamage(damage, kV);
	}

	public void beginMelee(float x, float y) {

		SoundController.getInstance().play("melee",GameplayController.LEAF_SOUND,false, 0.5f);

		meleeCooldown = MELEE_COOLDOWN;
		meleeRemaining = MELEE_DURATION;

		setState(PlayerState.MELEE);
	}

	public void fall(PitModel pit){
		if (isDashing()) return;
		setState(PlayerState.FALL);
	}

	public void portalTouched(PortalModel portal) {
		if (state == PlayerState.WARP || state == PlayerState.FALL) return;
		dashTargetPos.set(portal.getPosition());
		setState(PlayerState.WARP);
	}

	public void step(float dt, float x, float y) {
		float dt0 = dt * timeValue.getValue();
		super.step(dt0);
        shockWaveShader.step(dt0);

		// TODO
		if (remaining_stun>0){
			if (dashCooldown > 0) {
				dashCooldown = Math.max(0, dashCooldown -= dt0);
			}
			return;
		}

		switch (state){
			case DASH:
				if (dashRemaining > 0) {
					dashVector.setLength(getDashSpeed(1 - dashRemaining / DASH_DURATION));
					setVelocity(dashVector.x, dashVector.y, 1f);
					dashRemaining -= dt0;
				}else{
					endDash();
				}
				break;

			case MELEE:
				setMoveDirection(x, y);
				if (meleeRemaining > 0){
					meleeRemaining -= dt0;
				}else{
					setState(PlayerState.MOVE);
				}

				if (dashCooldown > 0) {
					dashCooldown = Math.max(0,dashCooldown -= dt0);
				}
				break;

			case MOVE:
				setMoveDirection(x, y);
				if (dashCooldown > 0) {
					dashCooldown = Math.max(0,dashCooldown -= dt0);
				}
				if (meleeCooldown > 0) {
					meleeCooldown -= dt0;
				}
				break;

			case DASH_FINISH:
				dashFinishTime = Math.min(dashFinishTimeMax, dashFinishTime + dt);
				float f = dashFinishTime / dashFinishTimeMax;
				temp.set(dashTargetPos).sub(dashLastPos).scl(f);
				setPosition(getPosition().set(dashLastPos).add(temp));
				if (dashFinishTime == dashFinishTimeMax) {
                    shockWaveShader.createShockwave(Geometry.worldToScreenSpace(dashTargetPos));
                    setState(PlayerState.DASH_PAUSE);
				}
				break;

			case DASH_PAUSE:
				setVelocity(x, y, 0);
				setAngle(x, y);

				knockback_vector.set(0,0);

				if (redashRemaining > 0){
					redashRemaining = Math.max(0,redashRemaining -= dt); // TRUE TIME
				} else{
					//force dash
					beginDash(x,y);
				}
				break;

			case FALL:
				setVelocity(0, 0, 0);
				fallingTime -= dt;
				if (fallingTime <= 0) {
					setInvincibility(0f);
					takeDamage(FALL_DAMAGE, cache.set(0,0));
					if (alive) {
						setPosition(respawnPos);
						setState(PlayerState.MOVE);
						dashCooldown = 0f;
						requestRoomReset = true;
					}
				}
				break;

			case WARP:
				if (warpingTime == 0) {
					SoundController.getInstance().play("teleport", GameplayController.TELEPORT_SOUND,false, 0.6f);
				}
				temp.set(dashTargetPos).sub(getPosition());
				if (temp.len() > moveSpeed * dt) {
					temp.nor();
					setMoveDirection(temp.x, temp.y);
				} else {
					setPosition(dashTargetPos);
					setVelocity(0, 0, 0);
					if (!warpAnimating) {
						warpAnimating = true;
						animationStream.playAction(warpAnimation);

						warpingTime = 5f;
					}
				}
				if (warpingTime > 5f && !warpAnimating) {
					warpAnimating = true;
					animationStream.playAction(warpAnimation);
					warpingTime = 5f;
				}
				warpingTime += dt;
				if (warpingTime > 6f) {
					levelEndWarp = true;
				}
		}

		timeValue.step(dt);
	}

	public void setMoveDirection(float x, float y) {
		super.setMoveDirection(x,y);
	}

	public boolean isDashing() {
		return isActionDashing() || state == PlayerState.DASH_PAUSE || state == PlayerState.DASH_FINISH;
	}

	// only when actively dashing as movement, does not include DASH_PAUSE
	public boolean isActionDashing() {
		return state == PlayerState.DASH;
	}

	public boolean isPaused() {
		return state==PlayerState.DASH_PAUSE;
	}

	public void resetRedash(SimpleObstacle hit){
		chain++;
		redashRemaining = getRedashDelay();
		Vector2 hitPos = hit.getPosition();
		dashTargetPos.set(hitPos);
		setState(PlayerState.DASH_FINISH);
	}

	public boolean canDash() {
		return dashCooldown <= 0 || state == PlayerState.DASH_PAUSE;
	}

	public void roomTransition(){
        shockWaveShader.clearShockwaves();
		dashCooldown = 0;
		endDash();
	}

	public boolean canMelee() {
		return meleeCooldown <= 0 && state == PlayerState.MOVE;
	}

	public PlayerModel() {
		this(DEFAULT_RADIUS);
	}

	public PlayerModel(float radius) {
		this(0,0,radius);
	}

	public PlayerModel(float x, float y) {
		this(x, y, DEFAULT_RADIUS);
	}

	public PlayerModel(float x, float y, float radius) {
		super(new AnimatedTexture(idleTexture, 100, 100, 6, 0.1f, true, -1));

		//new WheelObstacle(x, y, radius)
        FixtureData[] fixtureData = new FixtureData[3];
        shockWaveShader = new ShockWaveShader();
		radialShader = new ShaderProgram(Gdx.files.internal("shaders/shader.vert").readString(), Gdx.files.internal("shaders/radialshader.frag").readString());

		autoInvTime = 1f;

		Shape s = new CircleShape();
		s.setRadius(radius);

		Shape dashShape = new CircleShape();
        dashShape.setRadius(radius*DASH_HURTBOX_MULTIPLIER);

		PolygonShape meleeShape = new PolygonShape();
		float[] vertices = new float[8];
		float width = radius * 2.5f;
		float height = radius * 4f;
		float offset = radius * 2f;
        vertices[0] = -width/2.0f + offset;
        vertices[1] = -height/2.0f;
        vertices[2] = -width/2.0f+ offset;
        vertices[3] =  height/2.0f;
        vertices[4] =  width/2f+ offset;
        vertices[5] =  height/2.0f;
        vertices[6] =  width/2f+ offset;
        vertices[7] = -height/2.0f;
        meleeShape.set(vertices);

        dashHurtbox = new Hurtbox(dashShape, Hurtbox.HurtboxType.DASH, DASH_KNOCKBACK, DASH_DAMAGE);
        dashHurtbox.setEntity(this);
        meleeHurtbox = new Hurtbox(meleeShape, Hurtbox.HurtboxType.MELEE, MELEE_KNOCKBACK, MELEE_DAMAGE);
        meleeHurtbox.setEntity(this);
        meleeHurtbox.offset = new Vector2(0, radius);

        fixtureData[0] = new FixtureData(s, SimpleObstacle.getBits(1,1,0, 0),SimpleObstacle.getBits(1,1,0, 1),false);
        fixtureData[0].userData = this;
        fixtureData[1] = dashHurtbox;
        fixtureData[2] = meleeHurtbox;

        setObstacle(new MultiFixtureObstacle(x,y, fixtureData));

		dashAnimation = new AnimatedTexture(dashTexture, 200, 201, 4, 0.05f, true, 2);
		fallAnimation = new AnimatedTexture(fallTexture, 100, 100, 6, new float[]{0.075f, 0.075f, 0.075f, 0.075f, 0.075f, 10000f}, false, 3);
		meleeAnimation = new AnimatedTexture(meleeTexture, 150, 150, 7, 0.05f, false, 1);
		warpAnimation = new AnimatedTexture(warpTexture, 100, 100, 6, new float[]{0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 10000f}, false, 4);

		state = PlayerState.MOVE;
		prevState = state;

		obstacle.setName("player");

		maxHealth = DEFAULT_MAX_HEALTH;
		health = maxHealth;
		moveSpeed = 8;

		dashCooldown = 0;
		meleeCooldown = 0;
		dashRemaining = 0;
		dashVector = new Vector2();
		dashTargetPos = new Vector2();
		dashLastPos = new Vector2();
		temp = new Vector2();
		temp2 = new Vector2();
		respawnPos = new Vector2();
		type = EntityType.PLAYER;
		//state = PlayerState.MOVE;
		setState(PlayerState.MOVE);

		timeValue = new TweenValue();
		keys = new ArrayList<Integer>();

		requestRoomReset = false;
		levelEndWarp = false;
		warpAnimating = false;
	}

	public boolean didCompleteLevel() {
		return levelEndWarp;
	}

	public void setRespawnPosition(float x, float y) {
		respawnPos.set(x, y);
	}

	public void setRespawnPosition(Vector2 v) {
		setRespawnPosition(v.x, v.y);
	}

	public void setPosition(float x, float y) {
		obstacle.setPosition(x, y);
	}

	public void setPosition(Vector2 v) {
		setPosition(v.x, v.y);
	}

	public PlayerState getPlayerState() {
		return state;
	}

    // Call this function after object has been added to physics world
    public void physicsActivated() {
        obstacle.setFixedRotation(true);
        obstacle.getBody().setUserData(this);

        meleeHurtbox.setActive(false);
        dashHurtbox.setActive(false);
    }

	public void drawDebug(GameCanvas canvas){
		getObstacle().drawDebug(canvas);
	}

	public void draw(GameCanvas c, float dt) {
		float f = canDash() ? 1f : 1f - dashCooldown / DASH_COOLDOWN;
		if (state == PlayerState.DASH_PAUSE) {
			f = redashRemaining / getRedashDelay();
		}
		//drawMeter(c, dashMeterBorderSize, top-dashMeterBorderSize-dashMeterSizeY, col, f);
		if (state != PlayerState.FALL && state != PlayerState.WARP) {
			drawDashIndicator(c, f);
		}

		super.draw(c, dt);
		int top = c.getHeight();
		drawHealth(c, health);

		for (int i = 0; i < keys.size(); i++) {
			//c.draw(KeyModel.texture, KeyModel.KEY_COLORS[keys.get(i) % KeyModel.KEY_COLORS.length], 0, 0, dashMeterBorderSize + (50*i), top-dashMeterBorderSize*2-dashMeterSizeY*2-50, 0, 1, 1);
			c.draw(keyTextureRegion, KeyModel.getColor(keys.get(i)), 0, 0,
					dashMeterBorderSize + (50*i), top-dashMeterBorderSize*3-dashMeterSizeY*3-50, 0, 1, 1, false);
		}
	}

	public void drawKeyBinding(GameCanvas c) {
		c.draw(keyBindingTexture, Color.WHITE, 0, 0, 0, 0, 0, 0.5f, 0.5f,  false);
	}

	public void drawDashIndicator(GameCanvas c, float f) {
		c.spriteBatch.setShader(radialShader);
		radialShader.setUniformf("completion", f);
		c.draw(dashIndicatorFill, (f < 1f) ? RECHARGE_COLOR : FULL_COLOR, 50, 50, obstacle.getX()*obstacle.getDrawScale().x, obstacle.getY()*obstacle.getDrawScale().y, (float)Math.toDegrees(angle)*0,1,1);
		c.spriteBatch.setShader(null);
	}

	/*public void drawDashIndicator(GameCanvas c, float f) {
	    int inset_top = 11;
	    int inset_bot = 12;
	    int total = (int)((100 - inset_bot - inset_top)*f);
        dashIndicatorFill.setRegion(0, 100 - total - inset_bot, 100, total);
        c.draw(dashIndicatorFill, (f < 1f) ? Color.YELLOW : Color.WHITE, 50, (100-inset_bot-inset_top)/2f, obstacle.getX()*obstacle.getDrawScale().x, obstacle.getY()*obstacle.getDrawScale().y, (float)Math.toDegrees(angle),1,1);
    }*/

	public void drawMeter(GameCanvas c, int x, int y, Color col, float f) {
		dashMeterFill.setRegion(dashMeterBorderSize, 0, (int)((dashMeterSizeX - dashMeterBorderSize*2)*f), dashMeterSizeY);
		c.draw(dashMeterOutline, Color.WHITE,0,0,x,y,0,1,1, false);
		c.draw(dashMeterFill, col,0,0,x+dashMeterBorderSize,y,0,1,1, false);
	}

	public void drawHealth(GameCanvas c, float health) {
		int top = c.getHeight();
		int fullHearts = (int)health/2;
		int halfHearts = (int)(health % 2);

		int i = 0;
		while (i + 2 <= health) {
			float x = 0.5f + i/2f;
			c.draw(fullHeart, Color.WHITE, 0, 0, (healthOffset*x) - 15, top-3*dashMeterBorderSize-3*dashMeterSizeY, 0, 1, 1, false);
			i += 2;
		}
		while (i < maxHealth) {
			float x = 0.5f + i/2f;
			c.draw(heartOutline, Color.WHITE, 0, 0, (healthOffset*x) - 15, top-3*dashMeterBorderSize-3*dashMeterSizeY, 0, 1, 1, false);
			if (i == health - 1) {
				c.draw(halfHeart, Color.WHITE, 0, 0, (healthOffset*x) - 15, top-3*dashMeterBorderSize-3*dashMeterSizeY, 0, 1, 1, false);
			}
			i += 2;
		}

		/*if (halfHearts == 1 && health == 5) {
			c.draw(halfHeart, Color.WHITE, 0, 0, (healthOffset*3) - 15, top-3*dashMeterBorderSize-3*dashMeterSizeY, 0, 1, 1);
		}
		else if(halfHearts == 1 && health == 3) {
			c.draw(halfHeart, Color.WHITE, 0, 0, (healthOffset*2) - 15, top-3*dashMeterBorderSize-3*dashMeterSizeY, 0, 1, 1);
		}
		else if(halfHearts == 1 && health == 1) {
			c.draw(halfHeart, Color.WHITE, 0, 0, (healthOffset) - 15, top-3*dashMeterBorderSize-3*dashMeterSizeY, 0, 1, 1);
		}

		for (int i = 1; i <= fullHearts; i++) {
			c.draw(fullHeart, Color.WHITE, 0, 0, (healthOffset*i) - 15, top-3*dashMeterBorderSize-3*dashMeterSizeY, 0, 1, 1);
		}*/

		/*for (int i = 1; i <= halfHearts; i++) {
			c.draw(halfHeart, Color.WHITE, 0, 0, (healthOffset*i) - 15, top-3*dashMeterBorderSize-3*dashMeterSizeY, 0, 1, 1);
		}*/


	}

}