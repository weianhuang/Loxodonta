package com.mygdx.game.loxodonta.leveleditor;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.game.loxodonta.data.*;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class PropertyMenu extends Table {
    private static final float PADDING = 8;

    private ObjectData target;
    private ArrayList<Row> rows;
    private float width;
    private Skin skin;
    private DataMenuCallback parentCallback;

    public interface DataMenuCallback {
        void propertyChanged(String p);
        void onButtonPressed(String b);
    }

    private interface TextInputHandler {
        public void handle(Field field, String input) throws Exception;
        public TextField.TextFieldFilter getTextFieldFilter();
        public String display(Object o);
    }

    private TextInputHandler intHandler = new TextInputHandler() {
        @Override
        public void handle(Field field, String input) throws Exception {
            field.set(target, Integer.parseInt(input));
        }

        @Override
        public TextField.TextFieldFilter getTextFieldFilter() {
            return new TextField.TextFieldFilter.DigitsOnlyFilter();
        }

        @Override
        public String display(Object o) {
            return o.toString();
        }
    };

    private TextInputHandler intArrayHandler = new TextInputHandler() {
        @Override
        public void handle(Field field, String input) throws Exception {
            String[] strings = input.split(",");
            int[] out = new int[strings.length];
            for (int i=0; i<strings.length; i++) {
                out[i] = Integer.parseInt(strings[i]);
            }
            field.set(target, out);
        }

        @Override
        public TextField.TextFieldFilter getTextFieldFilter() {
            return null;
        }

        @Override
        public String display(Object o) {
            if (o == null) return "null";
            int[] arr = (int[])o; // java is weird
            String out = "";
            for (int i=0; i<arr.length; i++) {
                out += arr[i];
                if (i < arr.length-1) out += ",";
            }
            return out;
        }
    };

    private TextInputHandler stringHandler = new TextInputHandler() {
        @Override
        public void handle(Field field, String input) throws Exception {
            field.set(target, input);
        }

        @Override
        public TextField.TextFieldFilter getTextFieldFilter() {
            return null;
        }

        @Override
        public String display(Object o) {
            return o.toString();
        }
    };

    private class Row extends Table {
        public void refresh() {}
    }

    private class Property extends Row {
        private Field field;
        private TextField input;
        private TextInputHandler callback;

        public Property(final String name, Field fi, final TextInputHandler c) {
            this.field = fi;
            this.callback = c;
            Label l = new Label(name, skin);
            input = new TextField("", skin);
            add(l).width((width - PADDING*3) * 0.5f).space(PADDING);
            TextField.TextFieldFilter t = callback.getTextFieldFilter();
            if (t != null) input.setTextFieldFilter(t);
            input.setTextFieldListener(new TextField.TextFieldListener() {
                public void keyTyped(TextField textField, char key) {
                    if (key == 13) {
                        try {
                            callback.handle(field, input.getText());
                            if (parentCallback != null) {
                                parentCallback.propertyChanged(name);
                            }
                        } catch (Exception e) {
                            refresh();
                        }
                    }
                }
            });
            add(input).width((width - PADDING*3) * 0.5f).space(PADDING);
            refresh();
        }

        public void refresh() {
            try {
                input.setText(callback.display(field.get(target)));
            } catch (Exception e) {
                //e.printStackTrace();
                //System.out.println("Failed refresh");
            }
        }
    }

    private class Button extends Row {
        private TextButton button;

        public Button(final String name) {
            button = new TextButton(name, skin);
            button.addListener(new ClickListener(){
                public void clicked(InputEvent event, float x, float y){
                    if (parentCallback != null) {
                        parentCallback.onButtonPressed(name);
                    }
                }
            });
            add(button).width(width - PADDING*2).space(PADDING);
        }
    }

    private Field getField(String name) {
        try {
            return target.getClass().getDeclaredField(name);
        } catch (Exception e) {

        }
        try {
            return target.getClass().getSuperclass().getDeclaredField(name);
        } catch (Exception e) {

        }
        return null;
    }

    private void addProperty(String name, TextInputHandler callback) {
        Field field = getField(name);
        if (field == null) return;

        Row r = new Property(name, field, callback);
        rows.add(r);
        add(r).spaceBottom(PADDING);
        row();
    }

    private void addButton(String name) {
        Row r = new Button(name);
        rows.add(r);
        add(r).spaceBottom(PADDING);
        row();
    }

    public PropertyMenu(float width, Skin skin) {
        super(skin);
        top();
        pad(PADDING);
        this.width = width;
        this.skin = skin;
        rows = new ArrayList<Row>();
    }

    public void setTarget(ObjectData o) {
        clearChildren();
        rows.clear();
        target = o;
        if (target == null) return;
        String[] s = target.getClass().toString().split("\\.");
        //add(new Label(s[s.length-1], skin)).width(width - PADDING*2).space(PADDING);
        addProperty("posX", intHandler);
        addProperty("posY", intHandler);
        addProperty("pathX", intArrayHandler);
        addProperty("pathY", intArrayHandler);
        addProperty("sizeX", intHandler);
        addProperty("sizeY", intHandler);
        addProperty("rotation", intHandler);
        addProperty("fireRate", intHandler);
        addProperty("cooldown", intHandler);
        addProperty("imgPath", stringHandler);
        addProperty("itemValue", intHandler);
        addProperty("flying", intHandler);
        addProperty("hp", intHandler);
        addProperty("key", intHandler);
        addProperty("respawn", intHandler);
        addButton("Fragment");
        addButton("Copy");
        addButton("Delete");
        //addProperty("enemyTypeId", intHandler);
        //addProperty("obstacleTypeId", intHandler);
    }

    public void refresh() {
        for (Row p : rows) {
            p.refresh();
        }
    }

    public void setCallback(DataMenuCallback c) {
        parentCallback = c;
    }
}
