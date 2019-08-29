package com.mygdx.game.loxodonta.data;

import java.util.HashSet;

public class SaveData {
    public HashSet<Integer> completedLevels;

    public SaveData() {
        completedLevels = new HashSet<Integer>();
    }

    public boolean setLevelComplete(int index, boolean b) {
        boolean isComplete = completedLevels.contains(index);
        if (isComplete == b) return false;
        if (b) {
            completedLevels.add(index);
        } else {
            completedLevels.remove(index);
        }
        return true;
    }
}
