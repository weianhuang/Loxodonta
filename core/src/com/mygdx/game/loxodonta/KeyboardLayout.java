package com.mygdx.game.loxodonta;

import com.badlogic.gdx.Input;

public class KeyboardLayout {
    public int PrimaryKey;
    public int SecondaryKey;
    public int PauseKey;
    public int RestartKey;

    public int LeftKey, RightKey, UpKey, DownKey;

    public static KeyboardLayout fromPresetLayout(int i) {
        switch (i) {
            case 1:
                return new KeyboardLayout(
                        Input.Keys.SPACE,
                        Input.Keys.SHIFT_LEFT,
                        Input.Keys.P,
                        Input.Keys.R,
                        Input.Keys.A,
                        Input.Keys.D,
                        Input.Keys.W,
                        Input.Keys.S
                );
            default:
                return new KeyboardLayout();
        }
    }

    public KeyboardLayout() {
        PrimaryKey = Input.Keys.Z;
        SecondaryKey = Input.Keys.X;
        PauseKey = Input.Keys.P;
        RestartKey = Input.Keys.R;


        LeftKey = Input.Keys.LEFT;
        RightKey = Input.Keys.RIGHT;
        UpKey = Input.Keys.UP;
        DownKey = Input.Keys.DOWN;
    }

    public KeyboardLayout(int prim, int sec, int pause, int restart, int l, int r, int u, int d) {
        PrimaryKey = prim;
        SecondaryKey = sec;
        PauseKey = pause;
        RestartKey = restart;

        LeftKey = l;
        RightKey = r;
        UpKey = u;
        DownKey = d;
    }
}
