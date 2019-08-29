package com.mygdx.game.loxodonta.leveleditor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.ArrayList;

public class TileMenu extends Table {
    private TextField textField;
    private TextButton saveButton;
    private TextButton loadButton;
    private TileMenuCallback callback;

    private static final float PADDING = 8;
    private static final int TILES_PER_ROW = 3;

    public interface TileMenuCallback {
        void onTileSelected(int group, int index);
    }

    public TileMenu(TileInfo[][] tileArrays, String[] titles, float width, Skin skin) {
        super(skin);
        top();
        left();
        pad(PADDING);
        for (int a=0; a<tileArrays.length; a++) {
            final int group = a;
            TileInfo[] tiles = tileArrays[a];
            Label title = new Label(titles[a], skin);
            //title.setAlignment(1);
            add(title).width(width-PADDING*2f).space(PADDING).colspan(TILES_PER_ROW);
            row();
            for (int i = 0; i < tiles.length; i++) {
                final int index = i;
                if (i > 0 && i % TILES_PER_ROW == 0) row();
                Stack s = new Stack();
                Button b = new Button(skin);
                b.add(new Image(new TextureRegionDrawable(new TextureRegion(tiles[i].texture))));
                b.addListener(new ClickListener(){
                    public void clicked(InputEvent event, float x, float y){
                        if (callback != null) {
                            callback.onTileSelected(group, index);
                        }
                    }
                });
                float f = (width - PADDING * (1 + TILES_PER_ROW)) / TILES_PER_ROW;
                s.add(b);
                Table t = new Table();
                t.bottom().add(new Label(tiles[i].name, skin));
                s.add(t);
                add(s).space(PADDING).width(f).height(f);
            }
            row();
        }
    }

    public void setCallback(TileMenuCallback c) {
        callback = c;
    }
}
