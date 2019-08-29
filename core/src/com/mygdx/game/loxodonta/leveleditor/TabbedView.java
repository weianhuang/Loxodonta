package com.mygdx.game.loxodonta.leveleditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;

public class TabbedView extends Table {
    private ArrayList<Actor> actors;
    private ArrayList<String> titles;
    private Table[] backings;
    private int tabIndex;

    public TabbedView(ArrayList<Actor> actors, ArrayList<String> titles, float width, Skin skin) {
        super(skin);
        this.actors = actors;
        this.titles = titles;
        backings = new Table[actors.size()];

        top();
        Stack stack = new Stack();
        for (int i=0; i<actors.size(); i++) {
            Button b = new Button(skin);
            b.setColor(new Color(0,0,0,0f));
            final int index = i;
            b.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    setTab(index);
                }
            });
            Label label = new Label(titles.get(i), skin);
            label.setAlignment(1);
            Table back = new Table(skin);
            backings[i] = back;
            Stack s = new Stack();
            s.add(back);
            s.add(label);
            s.add(b);
            add(s).height(30).width(width / actors.size());
            stack.add(actors.get(i));
        }
        row();
        add(stack).colspan(actors.size());
        setTab(0);
    }

    public void setTab(int index) {
        tabIndex = index;
        for (int i=0; i<actors.size(); i++) {
            actors.get(i).setVisible(i == tabIndex);
        }
    }

    public void setBackground(String s) {
        for (Table t : backings) {
            t.setBackground(s);
        }
    }
}
