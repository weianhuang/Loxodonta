package com.mygdx.game.loxodonta.leveleditor;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.game.loxodonta.data.LevelData;
import com.mygdx.game.loxodonta.data.RoomData;
import com.mygdx.game.loxodonta.model.KeyModel;

public class RoomDataMenu extends Table {
    private TextField sizeFieldX;
    private TextField sizeFieldY;
    private DoorMenu[] doorMenus;
    private LevelData level;
    private RoomData room;
    private RoomDataMenuCallback callback;

    private static final float PADDING = 8;

    public interface RoomDataMenuCallback {
        public void onRoomModified();
    }

    private class DoorMenu extends Table {
        private CheckBox check;
        private CheckBox lockCheck;
        private TextField field;
        private TextField lockField;
        private int index;

        public DoorMenu(final int i, float width, String s, Skin skin) {
            index = i;
            check = new CheckBox(s, skin);
            field = new TextField("", skin);
            lockCheck = new CheckBox("Lock", skin);
            lockField = new TextField("", skin);
            check.left();
            check.addListener(new ChangeListener() {
                public void changed (ChangeEvent event, Actor actor) {
                    room.doors[i] = check.isChecked();
                    field.setDisabled(!room.doors[i]);
                }
            });
            add(check).width((width - PADDING*2) * 0.3f);
            field.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
            field.setMaxLength(3);
            field.setTextFieldListener(new TextField.TextFieldListener() {
                public void keyTyped(TextField textField, char key) {
                    if (key == 13) {
                        int v = Integer.parseInt(field.getText());
                        if (v >= 0 && v < level.roomDataList.size()) {
                            room.doorIndices[i] = v;
                        } else {
                            field.setText(Integer.toString(room.doorIndices[index]));
                        }
                    }
                }
            });
            add(field).width((width - PADDING*2) * 0.2f);
            lockField.setMaxLength(3);
            lockField.setTextFieldListener(new TextField.TextFieldListener() {
                public void keyTyped(TextField textField, char key) {
                    if (key == 13) {
                        try {
                            int v = Integer.parseInt(lockField.getText());
                            if (v < KeyModel.ENEMY_KILL_KEY_ID) throw new Exception();
                            room.doorLockIndices[i] = v;
                        } catch (Exception e) {
                            lockField.setText(Integer.toString(room.doorLockIndices[index]));
                        }
                    }
                }
            });
            lockCheck.addListener(new ChangeListener() {
                public void changed (ChangeEvent event, Actor actor) {
                    room.doorLocks[i] = lockCheck.isChecked();
                    lockField.setDisabled(!room.doorLocks[i]);
                }
            });
            add(lockCheck).width((width - PADDING*2) * 0.3f);
            add(lockField).width((width - PADDING*2) * 0.2f);
            refresh();
        }

        public void refresh() {
            check.setChecked(room.doors[index]);
            field.setDisabled(!room.doors[index]);
            field.setText(Integer.toString(room.doorIndices[index]));
            lockCheck.setChecked(room.doorLocks[index]);
            lockField.setDisabled(!room.doorLocks[index]);
            lockField.setText(Integer.toString(room.doorLockIndices[index]));
        }
    }

    public void connectDoors(RoomData r) {
        for (int i=0; i<4; i++) {
            if (r.doors[i]) {
                int flip = i - 2;
                if (flip < 0) flip += 4;
                int v = r.doorIndices[i];
                level.roomDataList.get(v).doors[flip] = true;
                level.roomDataList.get(v).doorIndices[flip] = r.roomIndex;
                level.roomDataList.get(v).doorLocks[flip] = r.doorLocks[i];
                level.roomDataList.get(v).doorLockIndices[flip] = r.doorLockIndices[i];
            }
        }
    }

    public RoomDataMenu(LevelData l, float width, Skin skin) {
        super(skin);
        level = l;
        room = l.getStartRoom();

        center();
        top();
        pad(PADDING);

        Table sizeTable = new Table();
        Label size = new Label("Size: ", skin);
        sizeTable.add(size).width((width - PADDING * 4)/3f).space(PADDING);
        sizeFieldX = new TextField(Integer.toString(room.sizeX), skin);
        sizeFieldX.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        sizeFieldX.setMaxLength(3);
        sizeFieldX.setTextFieldListener(new TextField.TextFieldListener() {
            public void keyTyped(TextField textField, char key) {
                if (key == 13) {
                    int i = Integer.parseInt(sizeFieldX.getText());
                    if (i != -1) room.sizeX = i;
                }
            }
        });
        sizeTable.add(sizeFieldX).width((width - PADDING * 4)/3f).space(PADDING);
        sizeFieldY = new TextField(Integer.toString(room.sizeY), skin);
        sizeFieldY.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        sizeFieldY.setMaxLength(3);
        sizeFieldY.setTextFieldListener(new TextField.TextFieldListener() {
            public void keyTyped(TextField textField, char key) {
                if (key == 13) {
                    int i = Integer.parseInt(sizeFieldY.getText());
                    if (i != -1) room.sizeY = i;
                }
            }
        });
        sizeTable.add(sizeFieldY).width((width - PADDING * 4)/3f).space(PADDING);
        add(sizeTable);
        String[] labels = new String[]{"Right", "Top", "Left", "Bottom"};
        doorMenus = new DoorMenu[labels.length];
        for (int i=0; i<labels.length; i++) {
            row();
            DoorMenu d = new DoorMenu(i, width, labels[i], skin);
            doorMenus[i] = d;
            add(d).space(PADDING);
        }

        TextButton sanitize = new TextButton("Sanitize", skin);
        sanitize.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                room.sanitize();
                if (callback!=null) callback.onRoomModified();
            }
        });
        row();
        add(sanitize).width(width - PADDING*2).space(PADDING);

        TextButton rotate = new TextButton("Rotate", skin);
        rotate.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                room.rotate();
                setRoom(room);
                if (callback!=null) callback.onRoomModified();
            }
        });
        row();
        add(rotate).width(width - PADDING*2).space(PADDING);

        TextButton rotateD = new TextButton("Rotate Doors", skin);
        rotateD.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                room.rotateDoors();
                setRoom(room);
            }
        });
        row();
        add(rotateD).width(width - PADDING*2).space(PADDING);

        TextButton doorConnect = new TextButton("Connect Doors", skin);
        doorConnect.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                connectDoors(room);
            }
        });
        row();
        add(doorConnect).width(width - PADDING*2).space(PADDING);
    }

    public void setRoom(RoomData r) {
        room = r;
        sizeFieldX.setText(Integer.toString(room.sizeX));
        sizeFieldY.setText(Integer.toString(room.sizeY));
        for (DoorMenu d : doorMenus) {
            d.refresh();
        }
    }

    public void setLevel(LevelData l) {
        level = l;
        setRoom(l.getStartRoom());
    }

    public void setCallback(RoomDataMenuCallback c) {
        callback = c;
    }
}
