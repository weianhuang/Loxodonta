package com.mygdx.game.loxodonta.leveleditor;

import com.mygdx.game.loxodonta.data.*;
import com.mygdx.game.util.Vec2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RoomDataOccupancy {
    public static int ENEMY_LAYER = 0;
    public static int ITEM_LAYER = 1;
    public static int OBSTACLE_LAYER = 2;

    private ArrayList<HashMap<Vec2i, ObjectData>> layers;
    private HashSet<Vec2i> overlapErrors;
    private Vec2i lookup;
    private RoomData room;
    private int layerCount;

    public RoomDataOccupancy() {
        layerCount = 3;
        layers = new ArrayList<HashMap<Vec2i, ObjectData>>();
        for (int i=0; i<layerCount; i++) {
            layers.add(new HashMap<Vec2i, ObjectData>());
        }
        overlapErrors = new HashSet<Vec2i>();
        lookup = new Vec2i();
    }

    public <V> void put(HashMap<Vec2i, V> map, Vec2i key, V value) {
        if (map.get(key) != null || key.x < 0 || key.x >= room.sizeX || key.y < 0 || key.y >= room.sizeY) {
            overlapErrors.add(key);
        }
        map.put(key, value);
    }

    public <V extends ObjectData> void putAll(ArrayList<V> list, int i) {
        for (ObjectData o : list) {
            for (int x=0; x<o.sizeX; x++) {
                for (int y = 0; y<o.sizeY; y++) {
                    put(layers.get(i), new Vec2i(o.posX + x, o.posY + y), o);
                }
            }
        }
    }

    public void loadRoomData(RoomData room) {
        for (int i=0; i<layerCount; i++) {
            layers.get(i).clear();
        }
        overlapErrors.clear();
        this.room = room;

        putAll(room.enemyDataList, 0);
        putAll(room.itemDataList, 1);
        putAll(room.obstacleDataList, 2);
    }

    public ObjectData fetchFromLayer(Vec2i v, int i) {
        return layers.get(i).get(v);
    }

    public EnemyData fetchEnemy(Vec2i v) {
        return (EnemyData)fetchFromLayer(v, ENEMY_LAYER);
    }

    public EnemyData fetchEnemy(int x, int y) {
        return fetchEnemy(lookup.set(x, y));
    }

    public ObstacleData fetchObstacle(Vec2i v) {
        return (ObstacleData)fetchFromLayer(v, OBSTACLE_LAYER);
    }

    public ObstacleData fetchObstacle(int x, int y) {
        return fetchObstacle(lookup.set(x, y));
    }

    public ObjectData fetch(Vec2i v) {
        for (int i=0; i<layerCount; i++) {
            ObjectData d = fetchFromLayer(v, i);
            if (d != null) return d;
        }
        return null;
    }

    public HashSet<Vec2i> getOverlapErrors() {
        return overlapErrors;
    }
}
