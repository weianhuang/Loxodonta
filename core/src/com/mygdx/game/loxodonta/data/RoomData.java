package com.mygdx.game.loxodonta.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mygdx.game.util.Geometry;
import com.mygdx.game.util.Vec2i;

import java.util.ArrayList;
import java.util.Iterator;

public class RoomData implements Data {
    public int sizeX;
    public int sizeY;
    public boolean[] doors;
    public int[] doorIndices;
    public boolean[] doorLocks;
    public int[] doorLockIndices;

    @JsonIgnore
    public float playerPosX;
    @JsonIgnore
    public float playerPosY;
    @JsonIgnore
    public int roomIndex;

    public ArrayList<EnemyData> enemyDataList;
    public ArrayList<ObstacleData> obstacleDataList;
    public ArrayList<ItemData> itemDataList;

    public RoomData() {
        enemyDataList = new ArrayList<EnemyData>();
        obstacleDataList = new ArrayList<ObstacleData>();
        itemDataList = new ArrayList<ItemData>();
        doors = new boolean[4];
        doorIndices = new int[4];
        doorLocks = new boolean[4];
        doorLockIndices = new int[4];
        sizeX = 12;
        sizeY = 12;
        playerPosX = 0;
        playerPosY = 0;
    }

    public void getPresentKeys(ArrayList<Integer> out) {
        for (ItemData i : itemDataList) {
            if (ItemData.ItemType.values()[i.itemTypeId] == ItemData.ItemType.KEY) {
                out.add(i.itemValue);
            }
        }

        for (EnemyData e : enemyDataList) {
            if (EnemyData.EnemyType.values()[e.enemyTypeId] == EnemyData.EnemyType.KEY && e.keyCollected == 0) {
                out.add(e.key);
            }
        }
    }

    // TODO make sure pathing enemies dont path out of room?
    public void sanitize() {
        Iterator i = obstacleDataList.iterator();
        while (i.hasNext()) {
            ObstacleData o = (ObstacleData)i.next();
            if (o.posX >= sizeX || o.posX < 0 || o.posY >= sizeY || o.posY < 0) {
                i.remove();
            }
        }

        i = enemyDataList.iterator();
        while (i.hasNext()) {
            EnemyData o = (EnemyData)i.next();
            if (o.posX >= sizeX || o.posX < 0 || o.posY >= sizeY || o.posY < 0) {
                i.remove();
            }
        }
    }

    public void fragment(ObstacleData o) {
        if (!obstacleDataList.contains(o)) return;
        for (int x=0; x<o.sizeX; x++) {
            for (int y = 0; y < o.sizeY; y++) {
                if (y == 0 && x == 0) continue;
                ObstacleData copy = new ObstacleData(o);
                copy.sizeX = 1;
                copy.sizeY = 1;
                copy.posX += x;
                copy.posY += y;
                obstacleDataList.add(copy);
            }
        }
        o.sizeX = 1;
        o.sizeY = 1;
    }

    public void fragment() {
        ArrayList<ObstacleData> newObstacles = new ArrayList<ObstacleData>();
        for (ObstacleData o : obstacleDataList) {
            for (int x=0; x<o.sizeX; x++) {
                for (int y = 0; y < o.sizeY; y++) {
                    if (y == 0 && x == 0) continue;
                    ObstacleData copy = new ObstacleData(o);
                    copy.sizeX = 1;
                    copy.sizeY = 1;
                    copy.posX += x;
                    copy.posY += y;
                    newObstacles.add(copy);
                }
            }
            o.sizeX = 1;
            o.sizeY = 1;
        }

        for (ObstacleData o : newObstacles) {
            obstacleDataList.add(o);
        }
    }

    public void rotatePath(int[] pathX, int[] pathY) {
        for (int i=0; i<pathX.length; i++) {
            Vec2i v = Geometry.rotate90(pathX[i], pathY[i]);
            pathX[i] = v.x + sizeX - 1;
            pathY[i] = v.y;
        }
    }

    public void rotateDoors() {
        boolean f = doors[3];
        int fi = doorIndices[3];
        for (int i=3; i>0; i--) {
            doors[i] = doors[i-1];
            doorIndices[i] = doorIndices[i-1];
        }
        doors[0] = f;
        doorIndices[0] = fi;
    }

    public void rotate() {
        Vec2i v = Geometry.rotate90(sizeX, sizeY);
        sizeX = Math.abs(v.x);
        sizeY = Math.abs(v.y);
        for (ObstacleData o : obstacleDataList) {
            Vec2i vs = new Vec2i(Geometry.rotate90(o.sizeX, o.sizeY));
            v = Geometry.rotate90(o.posX + o.sizeX/2, o.posY + o.sizeY/2);
            o.posX = v.x + sizeX - (int)(Math.abs(vs.x)/2f + 0.5f);
            o.posY = v.y - Math.abs(vs.y)/2;
            o.sizeX = Math.abs(vs.x);
            o.sizeY = Math.abs(vs.y);
        }

        for (ItemData o : itemDataList) {
            v = Geometry.rotate90(o.posX, o.posY);
            o.posX = v.x + sizeX - 1;
            o.posY = v.y;
            o.rotation = (o.rotation + 90) % 360;
        }

        for (EnemyData o : enemyDataList) {
            v = Geometry.rotate90(o.posX, o.posY);
            o.posX = v.x + sizeX - 1;
            o.posY = v.y;
            o.rotation = (o.rotation + 90) % 360;
            if (o.pathX != null) {
                rotatePath(o.pathX, o.pathY);
            }
        }
    }

    public static RoomData fromJSON(String json) {
        return JSONUtil.autoReadJSON(json, RoomData.class);
    }

    public void setFromJSON(String json) {
        enemyDataList.clear();
        itemDataList.clear();
        obstacleDataList.clear();
        fromData(fromJSON(json));
    }

    public String toJSON() {
        return JSONUtil.autoWriteJSON(this);
    }

    public void fromData(Data d) {
        if (d instanceof RoomData) {
            RoomData o = (RoomData)d;
            sizeX = o.sizeX;
            sizeY = o.sizeY;
            //doors = o.doors;
            //doorIndices = o.doorIndices;
            playerPosX = o.playerPosX;
            playerPosY = o.playerPosY;
            enemyDataList = o.enemyDataList;
            obstacleDataList = o.obstacleDataList;
        }
    }
}
