package com.mygdx.game.util;

import com.mygdx.game.util.Geometry;

public class TweenValue {
    public static final float DEFAULT_TRANSITION_TIME = 0.25f;

    private float transitionT;
    private float mod;

    private int tweenFunction;

    private float tFinish;
    private float t;

    private float a;
    private float b;

    private float value;

    public void TweenValue(float transitionTime) {
        this.transitionT = transitionTime;

        tweenFunction = 0;

        tFinish = 0f;
        t = 0f;
        value = 1f;
        a = 1f;
        b = 1f;
        mod = 0;
    }

    public TweenValue() {
        TweenValue(DEFAULT_TRANSITION_TIME);
    }

    public void tweenConstantRate(float b, float rate) {
        tweenLinear(getValue(), b, 1);
        tFinish = Math.min(Math.abs(b - a), Math.abs(a - b)) / rate;
    }

    public void tweenLinear(float a, float b, float duration) {
        tFinish = duration;
        t = 0;
        tweenFunction = 0;


        if (mod != 0) {
            b %= mod;
            if (b < 0) b += mod;
            if (Math.abs(b - a) > mod/2) { // it's faster to go the other way
                a += mod;
            }
        }

        this.a = a;
        this.b = b;
    }

    public void tweenLinear(float b, float duration) {
        tweenLinear(getValue(), b, duration);
    }

    public void tweenLinear(float b) {
        tweenLinear(getValue(), b, tFinish);
    }

    public void tweenSpecial(float a, float b, float duration) {
        tFinish = duration;
        t = 0;
        tweenFunction = 1;
        this.a = a;
        this.b = b;
    }

    public float getValue() {
        return mod == 0 ? value : value % mod;
    }

    private float linearTween(float a, float b, float p) {
        if (p <= 0) return a;
        if (p >= 1) return b;
        return Geometry.lerp(a, b, p);
    }

    private float specialTween(float a, float b, float p) {
        if (p <= 0) return a;
        if (p >= 1) return a;
        if (p < transitionT) return Geometry.lerp(a, b, p/transitionT);
        if (p > 1 - transitionT) return Geometry.lerp(b, a, (p - (1 - transitionT))/transitionT);
        return b;
    }

    public void setMod(float m) {
        mod = m;
    }

    public float step(float dt) {
        t += dt;
        if (tFinish != 0) {
            switch (tweenFunction) {
                case 1:
                    value = specialTween(a, b, t / tFinish);
                    break;
                default:
                    value = linearTween(a, b, t / tFinish);
            }
        }
        return getValue();
    }
}
