package com.mygdx.game.loxodonta;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.loxodonta.data.EnemyData;
import com.mygdx.game.loxodonta.data.JSONUtil;
import com.mygdx.game.loxodonta.data.LevelData;
import com.mygdx.game.loxodonta.data.RoomData;
import com.mygdx.game.util.Geometry;
import com.mygdx.game.util.RandomController;

public class LevelController {
    private LevelData levelData;

    public enum ExitDirection {
        RIGHT(0), UP(1), LEFT(2), DOWN(3);

        public ExitDirection reverse() {
            return ExitDirection.values()[(value + 2) % 4];
        }

        public final int value;

        ExitDirection(int value) {
            this.value = value;
        }
    }

    public void loadLevelData(LevelData ld) {
        levelData = ld;
        for (int i=0; i<ld.roomDataList.size(); i++) {
            ld.roomDataList.get(i).roomIndex = i;
        }
    }

    public LevelData getLevelData() {
        return levelData;
    }

    public RoomData getStartRoomData() {
        assert levelData != null : "Load level data first";
        RoomData rd = levelData.roomDataList.get(levelData.startRoomIndex);
        rd.playerPosX = 0;
        rd.playerPosY = 0;
        return rd;
    }

    public RoomData getConnectingRoomData(RoomData r, ExitDirection e) {
        return levelData.roomDataList.get(r.doorIndices[e.value]);
    }

    private static RoomData genRandomRoom() {
        RoomData rd = new RoomData();
        int roomX = RandomController.rollInt(10, 24);
        int roomY = RandomController.rollInt(8, 13);
        rd.sizeX = roomX;
        rd.sizeY = roomY;
        for (int j=0; j<RandomController.rollInt(1, 10); j++) {
            rd.enemyDataList.add(new EnemyData(
                    RandomController.rollInt(1, roomX-1),
                    RandomController.rollInt(1, roomY-1),
                    EnemyData.EnemyType.STATIC.ordinal()
            ));
        }
        return rd;
    }

    public static LevelData genRandomLevel() {
        LevelData lvl = new LevelData();
        lvl.startRoomIndex = 0;

        int numRooms = 10;

        for (int i=0; i<numRooms; i++) {
            RoomData rd = genRandomRoom();
            if (i < numRooms-1) {
                rd.doors[0] = true;
                rd.doorIndices[0] = i+1;
            }
            if (i > 0) {
                rd.doors[2] = true;
                rd.doorIndices[2] = i-1;
            } else {
                rd.doors[1] = true;
                rd.doors[3] = true;
                rd.doorIndices[1] = numRooms;
                rd.doorIndices[3] = numRooms+1;
            }
            lvl.roomDataList.add(rd);
        }

        RoomData rd = genRandomRoom();
        rd.doors[3] = true;
        rd.doorIndices[3] = 0;
        lvl.roomDataList.add(rd);

        rd = RoomData.fromJSON(JSONUtil.readStringFromFile("TestRoom.json")); //genRandomRoom();
        rd.doors[1] = true;
        rd.doorIndices[1] = 0;
        lvl.roomDataList.add(rd);
        lvl.startRoomIndex = numRooms + 1;

        JSONUtil.writeStringToFile(lvl.toJSON(), "RandomLevel.json");
        return lvl;
    }

}
