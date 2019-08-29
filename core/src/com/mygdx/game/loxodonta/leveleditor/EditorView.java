package com.mygdx.game.loxodonta.leveleditor;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.mygdx.game.loxodonta.LevelController;
import com.mygdx.game.loxodonta.data.*;
import com.mygdx.game.loxodonta.model.KeyModel;
import com.mygdx.game.util.Geometry;
import com.mygdx.game.util.Vec2i;

import java.util.HashSet;

public class EditorView extends Widget {
    private static final float TILE_SIZE = 50f;
    private static final float ZOOM_AMOUNT = 1.05f;
    private static final float PAN_LIMIT = 2000;
    private static final float ZOOM_MIN = 0.05f;
    private static final float ZOOM_MAX = 10f;
    private static TextureRegion gridTexture = new TextureRegion(new Texture("images/TileOutline50.png"), 0, 0, 50, 50);
    private static TextureRegion wallTexture = new TextureRegion(new Texture("images/WallTile50.png"), 0, 0, 50, 50);
    private static TextureRegion floorTexture = new TextureRegion(new Texture("images/Tileset100x100.png"), 0, 0, 100, 100);
    private static TextureRegion doorTexture = new TextureRegion(new Texture("images/WhiteTile50.png"), 0, 0, 50, 50);
    private static TextureRegion selectTexture = new TextureRegion(new Texture("images/WhiteTile50.png"), 0, 0, 50, 50);
    private static TextureRegion plusTexture = new TextureRegion(new Texture("images/PlusIcon50.png"), 0, 0, 50, 50);

    private Stage stage;
    private LevelData level;
    private RoomData room;
    private RoomDataOccupancy occupancy;
    private TileInfo[][] tileInfoArrays;
    private Vector2 translation;
    private float zoom;

    private Vector2 dragPrev;
    private Vector2 renderOrigin;
    private HashSet<RoomData> visited;
    private Rectangle selection;
    private boolean selectionValid;
    private Vec2i tempi;
    private Vector2 temp;
    private Color tempCol;

    private boolean fullRender;
    private ObjectData target;
    private boolean targetDragging;
    private PropertyMenu propertyMenu;
    private RoomDataMenu roomDataMenu;
    private TileMenu tileMenu;
    private LevelMenu levelMenu;
    private EditorViewCallback callback;

    public interface EditorViewCallback {
        public boolean onRoomSelected(RoomData r);
    }

    public EditorView(Stage stage, final LevelData level, final PropertyMenu pMenu, RoomDataMenu rMenu, TileMenu tMenu, final LevelMenu lMenu, TileInfo[][] tileInfoArrays, EditorViewCallback c) {
        super();
        /*for (RoomData r : level.roomDataList) {
            r.fragment();
        }*/
        this.stage = stage;
        this.propertyMenu = pMenu;
        this.roomDataMenu = rMenu;
        this.tileMenu = tMenu;
        this.levelMenu = lMenu;
        this.callback = c;
        occupancy = new RoomDataOccupancy();
        setLevel(level);
        this.tileInfoArrays = tileInfoArrays;
        selection = new Rectangle();
        selectionValid = false;
        translation = new Vector2();
        renderOrigin = new Vector2();
        dragPrev = new Vector2();
        visited = new HashSet<RoomData>();
        tempi = new Vec2i();
        temp = new Vector2();
        tempCol = new Color();
        zoom = 1;
        targetDragging = false;

        fullRender = true;

        rMenu.setCallback(new RoomDataMenu.RoomDataMenuCallback() {
            @Override
            public void onRoomModified() {
                occupancy.loadRoomData(room);
                if ((target instanceof EnemyData && !room.enemyDataList.contains(target)) ||
                        (target instanceof ObstacleData && !room.obstacleDataList.contains(target))) {
                    setTarget(null);
                    return;
                }
                setTarget(target);
            }
        });

        pMenu.setCallback(new PropertyMenu.DataMenuCallback() {
            @Override
            public void propertyChanged(String p) {
                occupancy.loadRoomData(room);
                setSelectionFromTarget(target);
            }

            public void onButtonPressed(String b) {
                if (b.equals("Delete")) {
                    deleteTarget();
                } else if (b.equals("Clone")) {
                    cloneTarget();
                } else if (b.equals("Fragment")) {
                    fragmentTarget();
                }
            }
        });

        tMenu.setCallback(new TileMenu.TileMenuCallback() {
            @Override
            public void onTileSelected(int group, int index) {
                if (group == 0) { // enemy
                    EnemyData e = new EnemyData(0, 0, index, new int[]{}, new int[]{});
                    room.enemyDataList.add(e);
                    setTarget(e);
                    occupancy.loadRoomData(room);
                } else if (group == 1) {
                    ItemData i = new ItemData(0, 0, index);
                    room.itemDataList.add(i);
                    setTarget(i);
                    occupancy.loadRoomData(room);
                } else if (group == 2) {
                    ObstacleData o = new ObstacleData(0, 0, 1, 1, index);
                    room.obstacleDataList.add(o);
                    setTarget(o);
                    occupancy.loadRoomData(room);
                }
            }
        });

        lMenu.setCallback(new LevelMenu.LevelMenuCallback() {
            @Override
            public void buttonClicked(int i) {
                if (i < level.roomDataList.size()) {
                    setRoom(level.roomDataList.get(i));
                } else {
                    RoomData rNew = new RoomData();
                    rNew.roomIndex = level.roomDataList.size();
                    level.roomDataList.add(rNew);
                    setRoom(rNew);
                    lMenu.setLevel(level);
                }
            }
        });

        DragListener drag = new DragListener(){
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                dragPrev.set(x, y);
            }

            public void dragStop(InputEvent event, float x, float y, int pointer) {
                pan(x, y, dragPrev);
                dragPrev.set(x, y);
            }

            public void drag(InputEvent event, float x, float y, int pointer) {
                pan(x, y, dragPrev);
                dragPrev.set(x, y);
            }
        };
        drag.setButton(Input.Buttons.RIGHT);
        addListener(drag);

        addListener(new InputListener(){
            public boolean keyDown (InputEvent event, int keycode) {
                if (target != null) {
                    if (keycode == Input.Keys.R) {
                        target.rotate();
                        pMenu.refresh();
                    } else if (keycode == Input.Keys.C) {
                        cloneTarget();
                    } else if (keycode == Input.Keys.BACKSPACE) {
                        deleteTarget();
                    }
                }
                return true;
            }
        });

        ClickListener click = new ClickListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int b) {
                if (b != Input.Buttons.LEFT) return false;

                Vec2i v = screenToRoomSpace(renderOrigin,room,x,y);
                ObjectData d = occupancy.fetch(v);
                if (d != null) {
                    setTarget(d);
                    return true;
                }

                RoomData sel = findSelectedRoom(x, y);
                if (sel == room) {
                    setTarget(null);
                } else if (sel != null) { // click in room tile
                    translation.set(0, 0);
                    setRoom(sel);
                }
                return true;
            }
        };
        addListener(click);

        DragListener leftDrag = new DragListener(){
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                if (target != null) {
                    targetDragging = true;
                }
            }

            public void drag(InputEvent event, float x, float y, int pointer) {
                if (!targetDragging || target == null) return;
                Vec2i v = screenToRoomSpace(renderOrigin,room,x,y);
                if (target.setPos(v.x, v.y)) {
                    selection.set(v.x, v.y, selection.width, selection.height);
                    occupancy.loadRoomData(room);
                    propertyMenu.refresh();
                }
            }

            public void dragStop(InputEvent event, float x, float y, int pointer) {
                targetDragging = false;
            }

            public boolean mouseMoved(InputEvent event, float x, float y) {
                if (target == null) {
                    Vec2i v = screenToRoomSpace(renderOrigin,room,x,y);
                    Data d = occupancy.fetch(v);
                    if (d != null) {
                        setSelectionFromTarget(d);
                        selectionValid = true;
                    } else {
                        findSelectedRoom(x, y);
                    }
                    return true;
                }
                return false;
            }

            public boolean scrolled(InputEvent event, float x, float y, int amount) {
                if (amount > 0) {
                    setZoom(zoom / ZOOM_AMOUNT, x/getWidth(), y/getHeight());
                    zoom /= ZOOM_AMOUNT;
                } else {
                    setZoom(zoom * ZOOM_AMOUNT, x/getWidth(), y/getHeight());
                }
                return true;
            }
        };
        leftDrag.setButton(Input.Buttons.LEFT);
        addListener(leftDrag);
    }

    public void setSelectionFromTarget(Data o) {
        if (o == null) {
            selectionValid = false;
            return;
        }
        boolean posFound = false;
        int x = 0;
        int y = 0;
        int sx = 1;
        int sy = 1;
        try {
            x = o.getClass().getField("posX").getInt(o);
            y = o.getClass().getField("posY").getInt(o);
            posFound = true;
            sx = o.getClass().getField("sizeX").getInt(o);
            sy = o.getClass().getField("sizeY").getInt(o);
        } catch (Exception e) {

        }
        if (posFound) {
            selection.set(x, y, sx, sy);
        }
    }

    private Vec2i screenToRoomSpace(Vector2 origin, RoomData r, float x, float y) {
        x += getX();
        y += getY();
        return tempi.set((int)Math.floor((x - origin.x)/(zoom*TILE_SIZE)), (int)Math.floor((y - origin.y)/(zoom*TILE_SIZE)));
    }

    private float clamp(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }

    public void setZoom(float zoom, float anchorX, float anchorY) {
        float finalZoom = clamp(zoom, ZOOM_MIN, ZOOM_MAX);
        float prevZoom = this.zoom;
        this.zoom = finalZoom;
        panPixels((anchorX-0.5f) * getWidth() * (prevZoom - finalZoom), (anchorY-0.5f) * getHeight() * (prevZoom - finalZoom));
    }

    public void panPixels(float x, float y) {
        //float panLimit = zoom <= 1 ? 0 : (zoom - 1f)/2f * getWidth();
        //float panLimitX = Math.max(0, (room.sizeX * TILE_SIZE * zoom) - getWidth()) / 2 + TILE_SIZE;
        //float panLimitY = Math.max(0, (room.sizeY * TILE_SIZE * zoom) - getHeight()) / 2 + TILE_SIZE;
        float panLimit = PAN_LIMIT * level.roomDataList.size();
        translation.set(
                clamp(translation.x + x, -panLimit*zoom, panLimit*zoom),
                clamp(translation.y + y, -panLimit*zoom, panLimit*zoom)
        );
    }

    public void pan(float x, float y, Vector2 start) {
        panPixels((x - start.x), (y - start.y));
    }

    private void drawTile(Batch batch, TextureRegion tex, float x, float y, float sx, float sy) {
        drawTile(batch, tex, x, y, sx, sy, 0);
    }

    private void drawTexture(Batch batch, TextureRegion tex, float x, float y, float rx, float ry, float rot) {
        float s = TILE_SIZE* zoom;
        batch.draw(tex, renderOrigin.x + x*s - TILE_SIZE/2 + TILE_SIZE/2*zoom, renderOrigin.y + y*s - TILE_SIZE/2 + TILE_SIZE/2*zoom, (TILE_SIZE/2), (TILE_SIZE/2), TILE_SIZE, TILE_SIZE, rx/TILE_SIZE*zoom, ry/TILE_SIZE*zoom, rot);
    }

    private void drawTile(Batch batch, TextureRegion tex, float x, float y, float sx, float sy, float rot) {
        float s = TILE_SIZE* zoom;

        tex.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        tex.setRegion(0, 0, (int)(TILE_SIZE*sx), (int)(TILE_SIZE*sy));

        batch.draw(tex, renderOrigin.x + x*s, renderOrigin.y + y*s, sx*s/2, sy*s/2, sx*s, sy*s, 1, 1, rot);
    }

    private void setDoorColor(Batch batch, RoomData room, int door) {
        if (room.doorLocks[door]) {
            batch.setColor(KeyModel.getColor(room.doorLockIndices[door]));
        } else {
            batch.setColor(Color.WHITE);
        }
    }

    public void renderWalls(Batch batch, RoomData room) {
        float gridX = room.sizeX;
        float gridY = room.sizeY;
        drawTile(batch, wallTexture, -1, -1, gridX+2, 1);
        drawTile(batch, wallTexture, -1, gridY, gridX+2, 1);
        drawTile(batch, wallTexture, -1, 0, 1, gridY);
        drawTile(batch, wallTexture, gridX, 0, 1, gridY);
        if (room.doors[0]) {
            setDoorColor(batch, room,0);
            drawTile(batch, doorTexture, gridX, gridY/2-1, 1, 2);
        }
        if (room.doors[2]) {
            setDoorColor(batch, room, 2);
            drawTile(batch, doorTexture, -1, gridY/2-1, 1, 2);
        }
        if (room.doors[3]) {
            setDoorColor(batch, room, 3);
            drawTile(batch, doorTexture, gridX/2-1, -1, 2, 1);
        }
        if (room.doors[1]) {
            setDoorColor(batch, room, 1);
            drawTile(batch, doorTexture, gridX/2-1, gridY, 2, 1);
        }
        batch.setColor(Color.WHITE);
        /*levelMin.set(
                Math.min(levelMin.x, renderOrigin.x - gridX*TILE_SIZE),
                Math.min(levelMin.y, renderOrigin.y - gridY*TILE_SIZE)
        );
        levelMax.set(
                Math.max(levelMax.x, renderOrigin.x + gridX*TILE_SIZE),
                Math.max(levelMax.y, renderOrigin.y + gridY*TILE_SIZE)
        );*/
    }

    public void renderEnemy(Batch batch, RoomData room, EnemyData e) {
        TileInfo t = tileInfoArrays[0][e.enemyTypeId];
        drawTexture(batch, t.texture, e.posX, e.posY, t.sizeX, t.sizeY, e.rotation);
        switch (EnemyData.EnemyType.values()[e.enemyTypeId]) {
            case PATH:
                int m = Math.min(e.pathX.length, e.pathY.length);
                for (int i=0; i<m; i++) {
                    int next = (i + 1) % m;
                    drawLine(batch, room, 4, e.pathX[i]+0.5f, e.pathY[i]+0.5f, e.pathX[next]+0.5f, e.pathY[next]+0.5f);
                }
                break;
            case CHASE:
                break;
            case KEY:
                break;
            default:
                break;
        }
    }

    public void renderRoomContents(Batch batch, RoomData room) {
        drawTile(batch, floorTexture, 0, 0, room.sizeX, room.sizeY);

        for (ObstacleData o : room.obstacleDataList) {
            drawTile(batch, tileInfoArrays[2][o.obstacleTypeId].texture, o.posX, o.posY, o.sizeX, o.sizeY, o.rotation);
        }

        for (ItemData o : room.itemDataList) {
            if (o.itemTypeId == 1) { // key id
                batch.setColor(KeyModel.getColor(o.itemValue));
            }
            drawTile(batch, tileInfoArrays[1][o.itemTypeId].texture, o.posX, o.posY, o.sizeX, o.sizeY, o.rotation);
            batch.setColor(Color.WHITE);
        }
        for (EnemyData o : room.enemyDataList) {
            renderEnemy(batch, room, o);
        }
    }

    public void resetRenderOrigin() {
        renderOrigin.set(getWidth()/2f+getX()+translation.x-(float)room.sizeX/2f*TILE_SIZE*zoom, getHeight()/2f+getY()+ translation.y-(float)room.sizeY/2f*TILE_SIZE*zoom);
    }

    public boolean isInRoom(RoomData room, int x, int y) {
        return x >= 0 && x < room.sizeX && y >= 0 && y < room.sizeY;
    }

    public RoomData checkAdjacentRooms(RoomData room, float x, float y) {
        Vec2i v = screenToRoomSpace(renderOrigin,room,x,y);
        if (isInRoom(room, v.x, v.y)) {
            if (room == this.room) {
                selection.set(v.x, v.y, 1, 1);
            } else {
                Vector2 holder = new Vector2(renderOrigin);
                resetRenderOrigin();
                selection.set((holder.x - renderOrigin.x)/(TILE_SIZE*zoom) - 1, (holder.y - renderOrigin.y)/(TILE_SIZE*zoom) - 1, room.sizeX + 2, room.sizeY + 2);
            }
            selectionValid = true;
            return room;
        }
        Vector2 holder = new Vector2(renderOrigin);
        for (int d=3; d>=0; d--) {
            if (room.doors[d]) {
                RoomData adjacent = level.roomDataList.get(room.doorIndices[d]);
                if (!visited.contains(adjacent)) {
                    visited.add(adjacent);
                    renderOrigin.set(holder);
                    adjustRenderOrigin(room, adjacent, d);
                    RoomData out = checkAdjacentRooms(adjacent, x, y);
                    if (out != null) {
                        return out;
                    }
                }
            }
        }
        return null;
    }

    public RoomData findSelectedRoom(float x, float y) {
        selectionValid = false;
        resetRenderOrigin();
        Vec2i v = screenToRoomSpace(renderOrigin,room,x,y);
        if (isInRoom(room, v.x, v.y)) {
            selection.set(v.x, v.y, 1, 1);
            selectionValid = true;
            return room;
        }
        visited.clear();
        visited.add(room);
        return checkAdjacentRooms(room, x, y);
    }

    public void drawText(Batch batch, String text, BitmapFont font, float x, float y) {
        GlyphLayout layout = new GlyphLayout(font,text);
        x *= TILE_SIZE*zoom;
        y *= TILE_SIZE*zoom;
        x -= layout.width / 2.0f;
        y -= layout.height / 2.0f;
        font.draw(batch, layout, renderOrigin.x+x, renderOrigin.y+y);
    }

    public void drawLine(Batch batch, RoomData room, float thickness, float x1, float y1, float x2, float y2) {
        tempCol.set(batch.getColor());
        batch.setColor(1, 0, 0, 1);
        float len = (float)Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1))*TILE_SIZE*zoom;
        thickness = Math.min(thickness, TILE_SIZE*zoom*0.15f); // static size, but don't exceed 20% of a tile
        batch.draw(selectTexture, renderOrigin.x+x1*TILE_SIZE*zoom, renderOrigin.y+y1*TILE_SIZE*zoom-thickness/2, 0, thickness/2, len, thickness, 1, 1, (float)(180f/Math.PI*Math.atan2(y2-y1,x2-x1)));
        batch.setColor(tempCol);
    }

    public void adjustRenderOrigin(RoomData room, RoomData adjacent, int door) {
        LevelController.ExitDirection e = LevelController.ExitDirection.values()[door];
        Vector2 dir = Geometry.exitEnumToDirection(e);
        renderOrigin.add((room.sizeX-adjacent.sizeX)/2f*TILE_SIZE*zoom+dir.x*(room.sizeX+adjacent.sizeX+4)/2f*TILE_SIZE*zoom, (room.sizeY-adjacent.sizeY)/2f*TILE_SIZE*zoom+dir.y*(room.sizeY+adjacent.sizeY+4)/2f*TILE_SIZE*zoom);
    }

    public void renderAdjacentRooms(Batch batch, RoomData room, boolean fullRender) {
        Vector2 holder = new Vector2(renderOrigin);
        for (int d=3; d>=0; d--) {
            if (room.doors[d]) {
                RoomData adjacent = level.roomDataList.get(room.doorIndices[d]);
                if (!visited.contains(adjacent)) {
                    visited.add(adjacent);
                    renderOrigin.set(holder);
                    adjustRenderOrigin(room, adjacent, d);
                    renderWalls(batch, adjacent);
                    if (fullRender) renderRoomContents(batch, adjacent);
                    renderAdjacentRooms(batch, adjacent, fullRender);
                }
            }
        }
    }

    public void renderOverlapErrors(Batch batch) {
        HashSet<Vec2i> e = occupancy.getOverlapErrors();
        tempCol.set(batch.getColor());
        batch.setColor(1, 0, 0, 0.5f);
        for (Vec2i v : e) {
            drawTile(batch, selectTexture, v.x, v.y, 1, 1);
        }
        batch.setColor(tempCol);
    }

    public void draw(Batch batch, float parentAlpha) {
        resetRenderOrigin();
        Vector2 holder = new Vector2(renderOrigin);
        renderWalls(batch, room);
        renderRoomContents(batch, room);
        drawTile(batch, gridTexture, 0, 0, room.sizeX, room.sizeY);
        visited.clear();
        visited.add(room);
        renderAdjacentRooms(batch, room, fullRender);
        renderOrigin.set(holder);
        if (selectionValid) {
            tempCol.set(batch.getColor());
            batch.setColor(1, 1, 0, 0.5f);
            drawTile(batch, selectTexture, selection.x, selection.y, selection.width, selection.height);
            batch.setColor(tempCol);
        }
        renderOverlapErrors(batch);
    }

    public void cloneTarget() {
        if (target == null) return;
        ObjectData d = target;

        if (target instanceof EnemyData) {
            target = new EnemyData();
            target.fromData(d);
            room.enemyDataList.add((EnemyData)target);
        } else if (target instanceof ObstacleData) {
            target = new ObstacleData();
            target.fromData(d);
            room.obstacleDataList.add((ObstacleData) target);
        } else if (target instanceof ItemData) {
            target = new ItemData();
            target.fromData(d);
            room.itemDataList.add((ItemData) target);
        }
        target.posX += 1;
        target.posY += 1;
        occupancy.loadRoomData(room);
        setTarget(target);
    }

    public void fragmentTarget() {
        if (target == null) return;
        if (!(target instanceof ObstacleData)) return;
        room.fragment((ObstacleData)target);
        setTarget(target);
        occupancy.loadRoomData(room);
    }

    public void deleteTarget() {
        if (target == null) return;

        room.enemyDataList.remove(target);
        room.obstacleDataList.remove(target);
        room.itemDataList.remove(target);
        setTarget(null);
    }

    public void setTarget(ObjectData o) {
        target = o;
        setSelectionFromTarget(o);
        selectionValid = target != null;
        propertyMenu.setTarget(target);
        if (selectionValid) {
            stage.setKeyboardFocus(this);
        }
    }

    public void setRoom(RoomData r) {
        room = r;
        occupancy.loadRoomData(room);
        roomDataMenu.setRoom(room);
        callback.onRoomSelected(room);
        setTarget(null);
    }

    public void setLevel(LevelData l) {
        level = l;
        setRoom(l.getStartRoom());
    }

    public RoomData getRoom() {
        return room;
    }

    public void setFullRender(boolean b) {
        fullRender = b;
    }
}
