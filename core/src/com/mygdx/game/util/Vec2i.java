package com.mygdx.game.util;

import com.badlogic.gdx.utils.NumberUtils;

public class Vec2i {
    public int x;
    public int y;

    public Vec2i() {
        this(0,0);
    }

    public Vec2i(int x, int y) {
        this.x=x;
        this.y=y;
    }


    public Vec2i(Vec2i v) {
        this(v.x, v.y);
    }

    public Vec2i set(int x, int y) {
        this.x=x;
        this.y=y;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vec2i) {
            Vec2i other = (Vec2i)obj;
            return other.x == x && other.y == y;
        }
        return false;
    }

    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + NumberUtils.floatToIntBits(x);
        result = prime * result + NumberUtils.floatToIntBits(y);
        return result;
    }

    public String toString() {
        return "("+x+", "+y+")";
    }
}
