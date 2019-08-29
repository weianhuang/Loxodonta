package com.mygdx.game.loxodonta.data;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mygdx.game.loxodonta.model.ItemModel;
import com.mygdx.game.loxodonta.model.KeyModel;
import com.mygdx.game.loxodonta.model.PeanutModel;
import com.mygdx.game.loxodonta.model.PitModel;
import com.mygdx.game.loxodonta.obstacle.BoxObstacle;
import com.mygdx.game.loxodonta.obstacle.Obstacle;

public class ItemData extends ObjectData {
    public enum ItemType {
        PEANUT, KEY
    }

    public int itemTypeId;
    public int itemValue;

    @JsonIgnore
    public boolean collected;

    public ItemData() {
        this(0, 0, 0, 0);
    }

    public ItemData(int pX, int pY, int id) {
        this(pX, pY, 0, id);
    }

    public ItemData(int pX, int pY, int value, int id) {
        super(pX, pY);
        itemTypeId = id;
        itemValue = value;
        rotation = 0;
        collected = false;
    }

    public ItemData(ItemData o) {
        this(o.posX, o.posY, o.itemValue, o.itemTypeId);
    }

    public static ItemData fromJSON(String json) {
        return JSONUtil.autoReadJSON(json, ItemData.class);
    }

    public void setFromJSON(String json) {
        fromData(fromJSON(json));
    }

    public String toJSON() {
        return JSONUtil.autoWriteJSON(this);
    }

    public void fromData(Data d) {
        if (d instanceof ItemData) {
            ItemData o = (ItemData)d;
            posX = o.posX;
            posY = o.posY;
            itemTypeId = o.itemTypeId;
            itemValue = o.itemValue;
            rotation = o.rotation;
        }
    }

    public ItemModel toObject() {
        ItemModel o;
        switch (ItemType.values()[itemTypeId]) {
            case KEY:
                o = new KeyModel(itemValue);
                break;
            default:
                o = new PeanutModel(itemValue);
                break;
        }
        o.setPosition(posX + (float)sizeX/2f, posY + (float)sizeY/2f);
        o.setAngle(rotation/180f * (float)Math.PI);
        return o;
    }
}
