package com.mygdx.game.loxodonta.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.game.loxodonta.GameCanvas;
import com.mygdx.game.loxodonta.obstacle.WheelObstacle;
import com.mygdx.game.util.AnimatedTexture;
import com.mygdx.game.util.AnimationStream;

public class ProjectileModel extends WheelObstacle {
    protected static final float DEFAULT_RADIUS = 0.2f;
    public static TextureRegion texture = new TextureRegion(new Texture("images/Projectile50.png"), 0, 0, 50, 50);
    public static Texture spitTexture = new Texture("images/arachratSpit50x50_5.png");
    public static Texture basicTexture = new Texture("images/projectile_50x50_4.png");

    public boolean active;
    public int damage;
    public float knockback;

    public AnimationStream animationStream;
    public AnimatedTexture anims[];

    public ProjectileEnum projectileType;

    public enum ProjectileEnum {
        BASIC, SPIT
    }

    public ProjectileModel() {
        this(ProjectileEnum.BASIC);
    }

    public ProjectileModel(ProjectileEnum type) {
        super(0, 0, DEFAULT_RADIUS);

        setName("projectile");
        setSensor(true);
        //setBodyType(BodyDef.BodyType.StaticBody);

        setCategoryBits(0,1,0, 1);
        projectileType = type;
        anims = new AnimatedTexture[]{
            new AnimatedTexture(basicTexture, 50, 50, 4, 0.05f, true, -1),
            new AnimatedTexture(spitTexture, 50, 50, 5, 0.05f, true, -1)
        };
        animationStream = new AnimationStream(anims[0]);
    }

    public ProjectileModel init(float x, float y, float vx, float vy, ProjectileEnum type) {
        float nor = (float)Math.sqrt(vx*vx + vy*vy);
        if (nor == 0) {
            vx = 1;
            nor = 1;
        }
        vx /= nor;
        vy /= nor;

        float speed;
        switch (type) {
            case SPIT:
                damage = 1;
                knockback = 0.5f;
                speed = 6f;
                setMaskBits(0,1,1, 1);
                animationStream.reset(anims[1]);
                break;
            default:
                damage = 1;
                knockback = 0.5f;
                speed = 6f;
                setMaskBits(0,1,0, 1);
                animationStream.reset(anims[0]);
                break;
        }

        vx *= speed;
        vy *= speed;

        active = true;
        setPosition(x, y);
        setLinearVelocity(vx, vy);
        setAngle((float)Math.atan2(vy, vx));

        projectileType = type;

        return this;
    }

    public void draw(GameCanvas canvas, float dt) {
        AnimatedTexture tex = animationStream.step(dt);
        canvas.draw(tex, Color.WHITE, tex.getRegionWidth()/2, tex.getRegionHeight()/2, getX()*getDrawScale().x, getY()*getDrawScale().y, (float)Math.toDegrees(getAngle()),1,1);
        //obstacle.draw(canvas);
    }
}