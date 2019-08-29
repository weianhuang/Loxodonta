package com.mygdx.game.loxodonta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.mygdx.game.loxodonta.data.*;
import com.mygdx.game.loxodonta.leveleditor.*;
import com.mygdx.game.loxodonta.model.*;

import java.util.ArrayList;

public class LevelEditorMode implements Screen {
    private static final float TAB_WIDTH = 300;

    private static final TileInfo[] enemyTileInfo = {
            new TileInfo("Static", StaticEnemy.getTextureStillFrame()),
            new TileInfo("Path", PathEnemy.getTextureStillFrame()),
            new TileInfo("Chase", ChaseEnemy.getTextureStillFrame()),
            new TileInfo("Key", KeyEnemy.getTextureStillFrame()),
            new TileInfo("Shoot", ShootEnemy.getTextureStillFrame())
    };

    private static final TileInfo[] itemTileInfo = {
            new TileInfo("Peanut", PeanutModel.texture),
            new TileInfo("Key", KeyModel.texture)
    };

    private static final TileInfo[] obstacleTileInfo = {
            new TileInfo("Wall", new Texture("images/WallTile50.png")),
            new TileInfo("Pit", new Texture("images/Pit50.png")),
            new TileInfo("ProjSpawn", new Texture("images/ProjectileSpawn50.png")),
            new TileInfo("Sign", new Texture("images/x_img.png")),
            new TileInfo("Portal", new Texture("images/bluePortal_100x100.png")),
            //new TileInfo("Wall?", new Texture("images/WallTile50.png")),
            //new TileInfo("Wall!", new Texture("images/WallTile50.png")),
            //new TileInfo("Wall!", new Texture("images/WallTile50.png")),
    };

    private static final TileInfo[][] tileInfoArrays = {enemyTileInfo, itemTileInfo, obstacleTileInfo};
    private static final String[] tileInfoTitles = {"Enemy", "Item", "Obstacle"};

    private Stage stage;
    //private SpriteBatch batch;
    private Skin skin;

    private Table screenTable;
    private Table leftTable;
    private Table rightTable;
    private SaveLoadMenu saveLoadMenu;
    private TileMenu tileMenu;
    private Table bottomTable;
    private EditorView editor;
    private RoomDataMenu roomMenu;
    private PropertyMenu propertyMenu;
    private LevelMenu levelMenu;

    private boolean active;

    private GameCanvas canvas;

    public LevelEditorMode(GameCanvas canvas) {
        this.canvas = canvas;
        active = false;
    }

    public void create() {
        //batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(new ScalingViewport(Scaling.stretch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera()), canvas.spriteBatch);

        /*final TextButton button = new TextButton("Click me", skin, "default");

        button.setWidth(200f);
        button.setHeight(20f);
        //button.setPosition(Gdx.graphics.getWidth() /2 - 100f, Gdx.graphics.getHeight()/2 - 10f);

        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                button.setText("You clicked the button");
            }
        });

        //stage.addActor(button);*/
        screenTable = new Table();
        screenTable.setFillParent(true);
        screenTable.left().top();

        leftTable = new Table();
        leftTable.top();
        rightTable = new Table();

        bottomTable = new Table();
        bottomTable.add(new Label("Bottom Menu", skin)).width(100).height(20);

        LevelData level = new LevelData();//LevelData.fromJSON(JSONUtil.readStringFromFile("NewLevel.json"));
        level.roomDataList.add(new RoomData());

        //new PropertyMenu().setTarget(level.roomDataList.get(0).enemyDataList.get(0));

        roomMenu = new RoomDataMenu(level, TAB_WIDTH, skin);
        roomMenu.setBackground("default-pane");

        propertyMenu = new PropertyMenu(TAB_WIDTH, skin);
        propertyMenu.setBackground("default-pane");

        tileMenu = new TileMenu(tileInfoArrays, tileInfoTitles, TAB_WIDTH, skin);
        tileMenu.setBackground("default-pane");

        levelMenu = new LevelMenu(TAB_WIDTH, skin);
        levelMenu.setBackground("default-pane");

        saveLoadMenu = new SaveLoadMenu(level,
                TAB_WIDTH,
                skin,
                new SaveLoadMenu.SaveLoadMenuCallback() {
                    public boolean onLevelLoad(LevelData l) {
                        editor.setLevel(l);
                        roomMenu.setLevel(l);
                        levelMenu.setLevel(l);
                        return true;
                    }
                }
        );
        saveLoadMenu.setBackground("default-pane");

        editor = new EditorView(stage, level, propertyMenu, roomMenu, tileMenu, levelMenu, tileInfoArrays, new EditorView.EditorViewCallback() {
            @Override
            public boolean onRoomSelected(RoomData r) {
                saveLoadMenu.setRoom(r);
                return true;
            }
        });
        stage.setScrollFocus(editor);
        rightTable.add(editor).width(canvas.getWidth()-TAB_WIDTH).height(canvas.getHeight());
        rightTable.setClip(true);

        ArrayList<String> titles = new ArrayList<String>();
        ArrayList<Actor> actors = new ArrayList<Actor>();

        titles.add("Data");
        actors.add(saveLoadMenu);

        titles.add("Tiles");
        actors.add(tileMenu);

        /*enemyTileMenu = new TileMenu(enemyTileInfo, TAB_WIDTH, skin);
        enemyTileMenu.setBackground("default-pane");
        titles.add("Enemies");
        actors.add(enemyTileMenu);*/

        titles.add("Prop");
        actors.add(propertyMenu);

        titles.add("Room");
        actors.add(roomMenu);

        titles.add("Level");
        actors.add(levelMenu);
        levelMenu.setLevel(level);

        TabbedView tabs = new TabbedView(actors, titles, TAB_WIDTH, skin);
        tabs.setBackground("default-rect");

        leftTable.add(tabs).height(canvas.getHeight());
        //leftTable.add(bottomTable);

        screenTable.add(leftTable);
        screenTable.add(rightTable).expandX();
        stage.addActor(screenTable);

        Gdx.input.setInputProcessor(stage);
        active = true;
    }

    public void dispose() {
        //batch.dispose();
        stage.dispose();
    }

    public void render(float dt) {
        if (!active) return;
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //stage.act(dt);
        //batch.begin();
        //canvas.begin();
        stage.draw();
        //canvas.end();
        //batch.end();
    }

    public void resize(int width, int height) {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void show() {
    }

    public void hide() {
    }
}
