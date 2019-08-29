package com.mygdx.game.loxodonta.leveleditor;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.game.loxodonta.data.LevelData;
import com.mygdx.game.loxodonta.data.RoomData;

public class LevelMenu extends Table {
    private static final float PADDING = 8;

    private LevelData level;
    private Skin skin;
    private LevelMenuCallback callback;
    private float width;

    public interface LevelMenuCallback {
        public void buttonClicked(int i);
    }

    public LevelMenu(float width, Skin skin) {
        super(skin);
        pad(PADDING);
        top();
        this.skin = skin;
        this.width = width;
    }

    public void addButton(String s, final int i) {
        TextButton b = new TextButton(s, skin);
        b.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                if (callback != null) {
                    callback.buttonClicked(i);
                }
            }
        });
        add(b).width(width - PADDING*2).space(PADDING);
        row();
    }

    public void setLevel(LevelData l) {
        level = l;
        clearChildren();
        if (level == null) return;
        for (int i=0; i<l.roomDataList.size(); i++) {
            addButton("Room "+i, i);
        }
        addButton("New Room ", l.roomDataList.size());
    }

    public void setCallback(LevelMenuCallback c) {
        callback = c;
    }
}
