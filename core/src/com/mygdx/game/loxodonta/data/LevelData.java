package com.mygdx.game.loxodonta.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;

public class LevelData implements Data {
    public ArrayList<RoomData> roomDataList;
    public int startRoomIndex;
    public int winRoomIndex;

    public LevelData() {
        roomDataList = new ArrayList<RoomData>();
        startRoomIndex = 0;
        winRoomIndex = 0;
    }

    @JsonIgnore
    public RoomData getStartRoom() {
        return roomDataList.get(startRoomIndex);
    }

    public static LevelData fromJSON(String json) {
        LevelData level = JSONUtil.autoReadJSON(json, LevelData.class);
        for (int i=0; i<level.roomDataList.size(); i++) {
            level.roomDataList.get(i).roomIndex = i;
        }
        return level;
    }

    public void setFromJSON(String json) {
        fromData(fromJSON(json));
    }

    public String toJSON() {
        return JSONUtil.autoWriteJSON(this);
    }

    public void fromData(Data d) {
        if (d instanceof LevelData) {
            LevelData o = (LevelData)d;
            roomDataList = o.roomDataList;
            startRoomIndex = o.startRoomIndex;
            winRoomIndex = o.winRoomIndex;
        }
    }

    public void rotate() {}
}
