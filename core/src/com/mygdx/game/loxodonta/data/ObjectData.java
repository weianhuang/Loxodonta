package com.mygdx.game.loxodonta.data;

public abstract class ObjectData implements Data {
    public int posX;
    public int posY;
    public int sizeX;
    public int sizeY;
    public int rotation;

    public ObjectData() {
        this(0, 0);
    }

    public ObjectData(int x, int y) {
        posX = x;
        posY = y;
        sizeX = 1;
        sizeY = 1;
        rotation = 0;
    }

    public void rotate() {
        rotation += 90;
        rotation %= 360;
    }

    public boolean setPos(int x, int y) {
        if (posX != x || posY != y) {
            posX = x;
            posY = y;
            return true;
        }
        return false;
    }

    public void fromData(Data d) {
        if (d instanceof ObjectData) {
            ObjectData o = (ObjectData)d;
            posX = o.posX;
            posY = o.posY;
            sizeX = o.sizeX;
            sizeY = o.sizeY;
            rotation = o.rotation;
        }
    }
}
