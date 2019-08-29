package com.mygdx.game.loxodonta.leveleditor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TileInfo {
    public TextureRegion texture;
    public String name;
    public int sizeX;
    public int sizeY;

    public TileInfo(String name, TextureRegion texture) {
        this.texture = texture;
        this.name = name;
        sizeX = texture.getRegionWidth();
        sizeY = texture.getRegionHeight();
    }

    public TileInfo(String name, Texture texture) {
        this(name, new TextureRegion(texture));
    }
}
