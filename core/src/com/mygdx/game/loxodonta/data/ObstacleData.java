package com.mygdx.game.loxodonta.data;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.game.loxodonta.model.*;
import com.mygdx.game.loxodonta.obstacle.BoxObstacle;
import com.mygdx.game.loxodonta.obstacle.Obstacle;

public class ObstacleData extends ObjectData {
    public int cooldown;
    public int fireRate;
    public int obstacleTypeId;
    public String imgPath;

    public ObstacleData() {
        this(0, 0, 1, 1, 0);
    }

    public ObstacleData(int pX, int pY, int sX, int sY, int id) {
        super(pX, pY);
        sizeX = sX;
        sizeY = sY;
        obstacleTypeId = id;
        rotation = 0;
    }

    public ObstacleData(ObstacleData o) {
        this(o.posX, o.posY, o.sizeX, o.sizeY, o.obstacleTypeId);
    }

    public static ObstacleData fromJSON(String json) {
        return JSONUtil.autoReadJSON(json, ObstacleData.class);
    }

    public void setFromJSON(String json) {
        fromData(fromJSON(json));
    }

    public String toJSON() {
        return JSONUtil.autoWriteJSON(this);
    }

    public void fromData(Data d) {
        if (d instanceof ObstacleData) {
            ObstacleData o = (ObstacleData)d;
            sizeX = o.sizeX;
            sizeY = o.sizeY;
            posX = o.posX;
            posY = o.posY;
            obstacleTypeId = o.obstacleTypeId;
            rotation = o.rotation;
            cooldown = o.cooldown;
            fireRate = o.fireRate;
            imgPath = o.imgPath;
        }
    }

    public Obstacle toObject() {
        Obstacle o;
        switch (obstacleTypeId) {
            case 1:
                o = new PitModel(posX, posY, sizeX, sizeY);
                break;
            case 2:
                o = new ProjectileSpawnModel(fireRate, cooldown);
                break;
            case 3:
                o = new SignModel(posX, posY, sizeX, sizeY, imgPath);
                break;
            case 4:
                o = new PortalModel(posX, posY);
                break;
            default:
                o = new WallModel(sizeX, sizeY);
                break;
        }
        o.setPosition(posX + (float)sizeX/2f, posY + (float)sizeY/2f);
        o.setAngle(rotation/180f * (float)Math.PI);
        return o;
    }
}
