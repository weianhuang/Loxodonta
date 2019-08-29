package com.mygdx.game.loxodonta;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.mygdx.game.loxodonta.data.LevelData;
import com.mygdx.game.loxodonta.data.RoomData;
import com.mygdx.game.loxodonta.model.KeyModel;
import com.mygdx.game.util.Geometry;
import com.mygdx.game.util.Vec2i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class Minimap {
    private static final int SPACING = 1; // in room units, not pixels
    public static final int BORDER = 3; // in pixels
    private static final NinePatch ROOM_TEXTURE = new NinePatch(new Texture("images/RoundedRect20x20inset3.png"), BORDER, BORDER, BORDER, BORDER);
    private static final NinePatchDrawable ROOM_NINEPATCH = new NinePatchDrawable(ROOM_TEXTURE);
    private static final NinePatchDrawable MINIMAP_NINEPATCH = new NinePatchDrawable(new NinePatch(new Texture("images/RoundedRect20x20inset3filled.png"), BORDER, BORDER, BORDER, BORDER));
    private static final Texture DOOR_TEXTURE = new Texture("images/WhitePixel.png");
    private static final Texture YOU_ARE_HERE_TEXTURE = new Texture("ui/ui_buttons/level_not_complete.png");
    private static final TextureRegion DOOR_REGION = new TextureRegion(DOOR_TEXTURE);
    private static final Vec2i DEFAULT_SIZE = new Vec2i(200, 150);
    public static final int DEFAULT_INSET = 20;

    public static final int DEFAULT_DRAW_SCALE = 4;

    private LevelData level;
    private ArrayList<MinimapRoom> rooms;
    private int roomIndex;
    private Rectangle rectangle; // on screen
    private float drawScale; // one room unit = how many pixels?
    private Vector2 transform;
    private Vector2 out;
    private float transparency;
    private float backgroundTransparency;
    private Color drawCol;
    //private Rectangle worldBounds;

    private HashSet<RoomData> visited;
    private LinkedList<MinimapRoom> queued;

    private class MinimapRoom {
        public boolean visible;
        public RoomData room;
        public float posX;
        public float posY;

        public ArrayList<Integer> keys;

        public MinimapRoom(RoomData r, float x, float y) {
            room = r;
            visible = false;
            posX = x;
            posY = y;
            keys = new ArrayList<Integer>();
            updateKeys();
        }

        public void updateKeys() {
            keys.clear();
            room.getPresentKeys(keys);
        }
    }

    public Minimap() {
        rooms = new ArrayList<MinimapRoom>();
        visited = new HashSet<RoomData>();
        queued = new LinkedList<MinimapRoom>();
        rectangle = new Rectangle();
        transform = new Vector2();
        out = new Vector2();
        drawScale = DEFAULT_DRAW_SCALE;
        roomIndex = 0;
        transparency = 0;
        backgroundTransparency = 0;
        drawCol = new Color();
        roomIndex = -1;
        //worldBounds = new Rectangle();
    }

    private void enqueue(MinimapRoom from, int door, int targetIndex) {
        RoomData target = level.roomDataList.get(targetIndex);
        if (visited.contains(target)) return;
        visited.add(target);
        Vector2 dir = Geometry.exitEnumToDirection(LevelController.ExitDirection.values()[door]);
        float x = from.posX + (from.room.sizeX + target.sizeX + SPACING*2) * dir.x/2;
        float y = from.posY + (from.room.sizeY + target.sizeY + SPACING*2) * dir.y/2;
        MinimapRoom mRoom = rooms.get(targetIndex);
        mRoom.posX = x;
        mRoom.posY = y;
        queued.add(mRoom);
    }

    public void setLevel(LevelData l) {
        roomIndex = -1;
        level = l;
        rooms.clear();
        queued.clear();
        visited.clear();
        for (int i=0; i<l.roomDataList.size(); i++) {
            rooms.add(new MinimapRoom(l.roomDataList.get(i), 0, 0));
        }
        queued.add(rooms.get(l.startRoomIndex));
        while (queued.size() > 0) {
            MinimapRoom mRoom = queued.pop();
            for (int i=0; i<4; i++) {
                if (mRoom.room.doors[i]) {
                    enqueue(mRoom, i, mRoom.room.doorIndices[i]);
                }
            }
        }
        visited.clear(); // dont hold refs
        setFocusRoomIndex(l.startRoomIndex);
    }

    public void setFocusRoomIndex(int i) {
        if (roomIndex != -1) {
            rooms.get(roomIndex).updateKeys();
        }
        roomIndex = i;
        rooms.get(roomIndex).visible = true;
    }

    public void setRectangle(float x, float y, float width, float height) {
        rectangle.set(x, y, width, height);
    }

    public void setRectangle(Rectangle r) {
        rectangle.set(r);
    }

    public void setRectangleDefault(GameCanvas c) {
        rectangle.set(c.getWidth() - DEFAULT_INSET - DEFAULT_SIZE.x, c.getHeight() - DEFAULT_INSET - DEFAULT_SIZE.y, DEFAULT_SIZE.x, DEFAULT_SIZE.y);
    }

    public void setTransform(float x, float y) {
        transform.set(x, y);
    }

    public void setTransform(Vector2 v) {
        transform.set(v);
    }

    public void setDrawScale(float f) {
        drawScale = f;
    }

    public Vector2 getTransform() {
        return out.set(transform);
    }

    public void setTransparency(float t) {
        transparency = t;
    }

    public void setBackgroundTransparency(float t) {
        backgroundTransparency = t;
    }

    private void drawRoom(GameCanvas c, MinimapRoom m, float x0, float y0) {
        if (m.visible) {
            ROOM_NINEPATCH.draw(c.spriteBatch, x0 + (m.posX - m.room.sizeX / 2f) * drawScale, y0 + (m.posY - m.room.sizeY / 2f) * drawScale, m.room.sizeX * drawScale, m.room.sizeY * drawScale);
        }

        if (m.room.roomIndex == roomIndex) {
            c.spriteBatch.draw(YOU_ARE_HERE_TEXTURE, x0 + (m.posX - 2) * drawScale, y0 + (m.posY - 2) * drawScale, 4 * drawScale, 4 * drawScale);
        }

        int keyCount = m.keys.size();
        for (int i=0; i<m.keys.size(); i++) {
            int key = m.keys.get(i);
            c.spriteBatch.setColor(KeyModel.getColor(key));
            float s = 4;
            c.spriteBatch.draw(KeyModel.texture, x0 + (m.posX - s*keyCount/2 + i*s) * drawScale, y0 + (m.posY - s/2) * drawScale, s * drawScale, s * drawScale);
        }
        c.spriteBatch.setColor(drawCol);

        if (m.visible) {
            for (int i = 0; i < 4; i++) {
                if (m.room.doors[i]) {
                    float x = drawScale;
                    float y = 2 * drawScale * 2;
                    if (i % 2 == 1) {
                        float q = x;
                        x = y;
                        y = q;
                    }
                    Vector2 dir = Geometry.directionToVector(i, 4).scl(0.5f);
                    DOOR_REGION.setRegion(0, 0, (int) x, (int) y);
                    if (m.room.doorLocks[i]) {
                        c.spriteBatch.setColor(KeyModel.getColor(m.room.doorLockIndices[i]));
                    }
                    c.spriteBatch.draw(DOOR_REGION, x0 + (m.posX + m.room.sizeX * dir.x) * drawScale - x / 2f, y0 + (m.posY + m.room.sizeY * dir.y) * drawScale - y / 2f, x, y);
                    c.spriteBatch.setColor(drawCol);
                }
            }
        }
    }

    public void draw(GameCanvas c) {
        drawCol.set(1, 1, 1, 1 - backgroundTransparency);
        c.spriteBatch.begin();
        c.spriteBatch.setColor(drawCol);
        MINIMAP_NINEPATCH.draw(c.spriteBatch, rectangle.x-BORDER, rectangle.y-BORDER, rectangle.width+BORDER*2, rectangle.height+BORDER*2);
        c.spriteBatch.end();
        if (!ScissorStack.pushScissors(rectangle)) return;
        drawCol.set(1, 1, 1, 1 - transparency);
        c.spriteBatch.setColor(drawCol);
        c.spriteBatch.begin();
        float x0 = rectangle.x + rectangle.width / 2f - rooms.get(roomIndex).posX*drawScale + transform.x;
        float y0 = rectangle.y + rectangle.height / 2f - rooms.get(roomIndex).posY*drawScale + transform.y;
        for (MinimapRoom m : rooms) {
            //if (!m.visible) continue;
            drawRoom(c, m, x0, y0);
        }
        c.spriteBatch.setColor(Color.WHITE);
        c.spriteBatch.end();
        ScissorStack.popScissors();
    }
}
