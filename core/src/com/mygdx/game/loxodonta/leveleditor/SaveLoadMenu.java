package com.mygdx.game.loxodonta.leveleditor;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.game.loxodonta.data.Data;
import com.mygdx.game.loxodonta.data.JSONUtil;
import com.mygdx.game.loxodonta.data.LevelData;
import com.mygdx.game.loxodonta.data.RoomData;

public class SaveLoadMenu extends Table {
    private LevelData level;
    private RoomData room;

    private static final float PADDING = 8;

    public interface SaveLoadMenuCallback {
        public boolean onLevelLoad(LevelData l);
    }

    private void saveLoadSection(String title, final boolean isRoom, float width, Skin skin, final SaveLoadMenuCallback callback) {
        Label t = new Label(title, skin);
        t.setAlignment(1);
        add(t).width(width-PADDING*2f).colspan(2).spaceBottom(PADDING);
        row();
        final TextField textField = new TextField(title+".json", skin);
        add(textField).width(width-PADDING*2f).colspan(2).spaceBottom(PADDING);
        row();
        TextButton saveButton = new TextButton("Save", skin);
        add(saveButton).width((width-PADDING*3f)/2f).space(PADDING);
        TextButton loadButton = new TextButton("Load", skin);
        add(loadButton).width((width-PADDING*3f)/2f).space(PADDING);
        saveButton.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                Data d = isRoom ? room : level;
                try {
                    JSONUtil.writeStringToFile(d.toJSON(), textField.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        loadButton.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                Data d = isRoom ? room : level;
                try {
                    d.setFromJSON(JSONUtil.readStringFromFile(textField.getText()));
                    if (!isRoom) {
                        callback.onLevelLoad((LevelData) d);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public SaveLoadMenu(LevelData l, float width, Skin skin, SaveLoadMenuCallback callback) {
        super(skin);
        level = l;
        room = l.getStartRoom();
        center();
        top();
        pad(PADDING);
        saveLoadSection("Room", true, width, skin, callback);
        row();
        saveLoadSection("Level", false, width, skin, callback);
    }

    public void setRoom(RoomData r) {
        room = r;
    }
}
