package com.mygdx.game.loxodonta.model;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.loxodonta.GameCanvas;
import com.mygdx.game.loxodonta.ProjectileController;
import com.mygdx.game.loxodonta.RoomController;
import com.mygdx.game.util.AnimatedTexture;

import java.util.ArrayList;

public class KeyEnemy extends EnemyModel {
    public static Texture texture = new Texture("images/keyholder_inPlace_40x50_9.png");
    public static TextureRegion getTextureStillFrame() {
        return new TextureRegion(texture, 0, 0, 75, 75);
    }

    public enum KeyState{
        IDLE, RUN, SHOOT, SUMMON
    }

    private static final int DEFAULT_SIGHT = 10;
    private static final float DEFAULT_DIST = 5;
    private static final int DEFAULT_FIRERATE = 60;
    public static final int MAX_SHOTS = 3;

    public int sight_distance;
    public KeyState keyState;

    // Shooting
    private int fireRate; // 60 = 1 second
    private float leftoverT;
    private int cooldown;

    public float minDist = DEFAULT_DIST;
    public float maxDist = DEFAULT_DIST + 1;

    public int shots;
    public int prevDir;
    public int prevTime;

    public boolean cornered;
    public int cornerTime;

    public boolean keyCollected;
    public int keyId;

    private KeyModel key;
    public ArrayList<EnemyModel> summonedEnemies; // TODO: cap?

    public KeyEnemy(float x, float y, int keyId) {
        super(x,y,DEFAULT_RADIUS,new AnimatedTexture(texture, 75, 75, 9, 0.1f, true, -1));

        moveSpeed = 6;
        this.keyId = keyId;

        this.sight_distance = DEFAULT_SIGHT;
        keyState = KeyState.IDLE;
        tarV = false;

        this.fireRate = DEFAULT_FIRERATE;
        cooldown = fireRate;
        leftoverT = 0;

        shots = 0;
        prevDir = 0;
        prevTime = 40;

        cornered = false;
        cornerTime = 120;

        summonedEnemies = new ArrayList<EnemyModel>();
    }

    public void addKey(KeyModel k){
        key = k;

        if (!isAlive())
            dropKey();
    }

    public void shoot(ProjectileController projectileController) {
        if (cooldown <= 0) {
            projectileController.newProjectile(obstacle.getX(), obstacle.getY(), targetPoint.x - obstacle.getX(), targetPoint.y - obstacle.getY(), ProjectileModel.ProjectileEnum.SPIT);
            cooldown = fireRate;
            shots ++;
        }
    }

    public void summon(int x, int y, float angle, World world, RoomController roomController){
        if (cooldown <= 0) {
            ChaseEnemy e = new ChaseEnemy(x, y);
            e.setAngle(angle);
            //e.setMoveSpeed(2);
            roomController.addLivingEntity(e, world);
            cooldown = fireRate;
            shots = 0;

            summonedEnemies.add(e);
        }
    }

    public void step(float dt){
        leftoverT += dt*60;
        cooldown -= (int)leftoverT;
        leftoverT = leftoverT % 1;
        prevTime -= dt;

        if (cornered)
            cornerTime -= dt;

        if (prevTime <= 0)
            prevDir = 0;

        if (cornerTime <= 0)
            cornered = false;

        super.step(dt);
    }

    public boolean kill(){
        dropKey();
        return super.kill();
    }

    public void dropKey(){
        if (key != null){
            key.setActive(true);
        }
    }

    public void draw(GameCanvas canvas, float dt) {
        if (alive) {
            AnimatedTexture tex = animationStream.step(dt);
            super.draw(canvas, dt);
            //canvas.draw(tex, Color.WHITE, tex.getRegionWidth() / 2, tex.getRegionHeight() / 2, obstacle.getX() * obstacle.getDrawScale().x, obstacle.getY() * obstacle.getDrawScale().y, obstacle.getAngle(), 1, 1);
        }else if (key != null && !key.getCollected()) {
            key.draw(canvas);
        }

        for (EnemyModel e : summonedEnemies){
            if (e.isAlive())
                e.draw(canvas, dt);
        }
    }

    public void delete(World world){
        super.delete(world);
        if (key != null)
            key.deactivatePhysics(world);
    }
}
