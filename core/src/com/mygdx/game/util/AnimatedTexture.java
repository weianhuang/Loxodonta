package com.mygdx.game.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.loxodonta.GameCanvas;

public class AnimatedTexture extends TextureRegion {
    private int sizeX;
    private int sizeY;
    private int frames;
    private float[] frameLengths;
    private boolean repeatable;
    private int priority;

    private int cols;
    private int rows;
    private float curTime;
    private int curIndex;
    private boolean completed;

    private AnimationCallback callback;

    public interface AnimationCallback {
        public void frameReached(int frame);
        public void completed();
    }

    public AnimatedTexture(Texture texture, int sizeX, int sizeY, int frames, float frameLength, boolean repeatable, int priority) {
        this(texture, sizeX, sizeY, frames, new float[frames], repeatable, priority);
        for (int i=0; i<frames; i++) {
            frameLengths[i] = frameLength;
        }
    }

    public AnimatedTexture(Texture texture, int sizeX, int sizeY, int frames, float[] frameLengths, boolean repeatable, int priority) {
        super(texture);
        if (frames > 1 && frameLengths.length != frames) System.err.println("frameLengths length must match frame count");
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.frames = frames;
        this.frameLengths = frameLengths;
        this.repeatable = repeatable;
        this.priority = priority;

        cols = Math.round((float)texture.getWidth() / sizeX); // i really dont like rounding this but the image sizes are wrong
        rows = Math.round((float)texture.getHeight() / sizeY);

        curTime = 0;
        curIndex = 0;
        completed = false;
        setTexture();
    }

    private void setTexture() {
        setRegion((curIndex % cols) * sizeX, (curIndex / cols) * sizeY, sizeX, sizeY);
    }

    public void setFrame(int f) {
        curIndex = f;
        curTime = 0;
        setTexture();
    }

    // returns true if completed
    public AnimatedTexture step(float dt) {
        if (dt == 0) return this;
        if (frames > 1 && curIndex < frames) {
            curTime += dt;
            boolean changed = false;
            while (curTime >= frameLengths[curIndex]) { // dont make frameLengths all zero or this will yield forever
                changed = true;
                curTime -= frameLengths[curIndex];
                curIndex++;
                if (curIndex >= frames) {
                    if (repeatable) {
                        curTime = 0;
                        curIndex = 0;
                        if (callback != null) callback.frameReached(curIndex);
                    } else {
                        completed = true;
                        changed = false;
                        if (callback != null) callback.completed();
                        return this;
                    }
                } else {
                    if (callback != null) callback.frameReached(curIndex);
                }
            }
            if (changed) {
                setTexture();
            }
        }
        return this;
    }

    public void reset() {
        setFrame(0);
        completed = false;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCallback(AnimationCallback callback) {
        this.callback = callback;
    }

    public int getPriority() {
        return priority;
    }

    public AnimatedTexture duplicate() {
        return new AnimatedTexture(getTexture(), sizeX, sizeY, frames, frameLengths, repeatable, priority);
    }
}
