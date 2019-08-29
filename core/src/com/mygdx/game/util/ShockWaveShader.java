package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.loxodonta.GameCanvas;

public class ShockWaveShader extends FullScreenShader {
    private static final float DEFAULT_WAVE_SPEED = 2000f;
    private static final float DEFAULT_WAVE_RADIUS = 30f;
    private static final int MAX_WAVES = 20;

    private ShaderProgram shader;
    private Vector2 temp;

    private float wave_speed; // pixels/second
    private float wave_radius; // pixels

    private Shockwave[] shockwaves;

    private class Shockwave {
        public float x;
        public float y;
        public float t;
        public boolean active;

        public Shockwave() {
            active = false;
        }

        public Shockwave(float x, float y) {
            init(x, y);
        }

        public void init(float x, float y) {
            this.x = x;
            this.y = y;
            t = 0;
            active = true;
        }
    }

    public void setWaveSpeed(float waveSpeed) {
        wave_speed = waveSpeed;
    }

    public void setWaveRadius(float waveRadius) {
        wave_radius = waveRadius;
    }

    public ShockWaveShader() {
        this(DEFAULT_WAVE_SPEED, DEFAULT_WAVE_RADIUS);
    }

    public ShockWaveShader(float waveSpeed, float waveRadius) {
        shader = new ShaderProgram(Gdx.files.internal("shaders/shader.vert").readString(), Gdx.files.internal("shaders/shockwaveshader.frag").readString());
        temp = new Vector2();

        shockwaves = new Shockwave[MAX_WAVES];
        for (int i=0; i<MAX_WAVES; i++) {
            shockwaves[i] = new Shockwave();
        }

        setWaveSpeed(waveSpeed);
        setWaveRadius(waveRadius);
    }

    public void clearShockwaves() {
        for (int i = 0; i < shockwaves.length; i++) {
            shockwaves[i].active = false;
        }
    }

    public void createShockwave(Vector2 v) {
        createShockwave(v.x, v.y);
    }

    public void createShockwave(float x, float y) {
        float oldestT = 0;
        int oldest = 0;
        for (int i=0; i<shockwaves.length; i++) {
            if (!shockwaves[i].active) {
                shockwaves[i].init(x, y);
                return;
            }
            if (shockwaves[i].t > oldestT) {
                oldestT = shockwaves[i].t;
                oldest = i;
            }
        }
        shockwaves[oldest].init(x, y);
    }

    public void step(float dt) {
        float maxDur = (Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) + wave_radius) / wave_speed;

        for (int i=0; i<shockwaves.length; i++) {
            shockwaves[i].t += dt;
            if (shockwaves[i].t > maxDur) {
                shockwaves[i].active = false;
            }
        }
    }

    public void setUniforms(GameCanvas canvas) {
        shader.setUniformf("wave_radius", wave_radius);
        shader.setUniformf("wave_speed", wave_speed);
        Affine2 transform = canvas.getTransform();
        int active = 0;
        for (int i=0; i<shockwaves.length; i++) {
            if (shockwaves[i].active) {
                transform.applyTo(temp.set(shockwaves[i].x, shockwaves[i].y));
                shader.setUniformf("wave_position[" + active + "]", temp);
                shader.setUniformf("wave_time[" + active + "]", shockwaves[i].t);
                active++;
            }
        }
        shader.setUniformi("wave_count", active);
        shader.setUniformf("resolution", temp.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
    }

    public ShaderProgram getShader() {
        return shader;
    }
}
