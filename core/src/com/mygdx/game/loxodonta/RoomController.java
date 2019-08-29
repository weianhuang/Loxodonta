package com.mygdx.game.loxodonta;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.loxodonta.data.*;
import com.mygdx.game.loxodonta.model.*;
import com.mygdx.game.loxodonta.obstacle.Obstacle;
import com.mygdx.game.loxodonta.obstacle.PolygonObstacle;
import com.mygdx.game.util.Geometry;
import com.mygdx.game.util.Vec2i;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.util.ArrayList;

public class RoomController {
    private static final float WALL_DENSITY = 0.0f;
    /**
     * Friction of non-crate objects
     */
    private static final float WALL_FRICTION = 0.1f;
    /**
     * Collision restitution for all objects
     */
    private static final float WALL_RESTITUTION = 0.1f;

    private static final Color FLOOR_COLOR = new Color(0.9f, 0.9f, 0.9f, 1f);

    private static TextureRegion wallSideTexture = new TextureRegion(new Texture("images/WallSide50x50.png"), 0, 0, 50, 50);
    private static TextureRegion wallCornerTexture = new TextureRegion(new Texture("images/WallCorner50x50.png"), 0, 0, 50, 50);
    private static TextureRegion floorTexture = new TextureRegion(new Texture("images/floor_texture.png"), 0, 0, 50, 50);

    private static final TextureRegion[] pitTextures = new TextureRegion[]{
            new TextureRegion(new Texture("images/shadowFull_50x50.png"), 0, 0, 50, 50),
            new TextureRegion(new Texture("images/shadowU_50x50.png"), 0, 0, 50, 50),
            new TextureRegion(new Texture("images/shadowTunnel_50x50.png"), 0, 0, 50, 50),
            new TextureRegion(new Texture("images/shadowL_50x50.png"), 0, 0, 50, 50),
            new TextureRegion(new Texture("images/shadowSingle_50x50.png"), 0, 0, 50, 50),
            new TextureRegion(new Texture("images/pitMiddle50x50.png"), 0, 0, 50, 50),
            new TextureRegion(new Texture("images/shadowCorner_50x50.png"), 0, 0, 50, 50),
    };

    private static final TextureRegion[] floorTextures = new TextureRegion[]{
            new TextureRegion(new Texture("images/borderFull_50x50.png"), 0, 0, 50, 50),
            new TextureRegion(new Texture("images/borderU_50x50.png"), 0, 0, 50, 50),
            new TextureRegion(new Texture("images/borderTunnel_50x50.png"), 0, 0, 50, 50),
            new TextureRegion(new Texture("images/borderL_50x50.png"), 0, 0, 50, 50),
            new TextureRegion(new Texture("images/borderSingle_50x50.png"), 0, 0, 50, 50),
            new TextureRegion(new Texture("images/pitMiddle50x50.png"), 0, 0, 50, 50),
            new TextureRegion(new Texture("images/borderCorner_50x50.png"), 0, 0, 50, 50),
    };

    private static float WALL_THICKNESS = 1.0f;
    private static float DOOR_THICKNESS = 1.5f;
    private static float DOOR_THICKNESS_VISUAL = 2.0f;

    private Vector2 drawScale;

    private static float[] createCornerVertexArray(float xMin, float xMax, float yMin, float yMax) {
        return new float[]{
                xMax, yMax,
                xMin, yMax,
                xMin, yMin,
                xMin + WALL_THICKNESS, yMin,
                xMin + WALL_THICKNESS, yMax - WALL_THICKNESS,
                xMax, yMax - WALL_THICKNESS
        };
    }

    private static float[] createCornerVertexArrayOuter(float xMin, float xMax, float yMin, float yMax) {
        return createCornerVertexArray(xMin - WALL_THICKNESS, xMax, yMin, yMax + WALL_THICKNESS);
    }

    private static PolygonObstacle createCornerObstacle(float xMin, float xMax, float yMin, float yMax, boolean xReflect, boolean yReflect) {
        float xCenter = (xMin + xMax) / 2;
        float yCenter = (yMin + yMax) / 2;
        float[] verts = Geometry.reflectVertexArray(
                createCornerVertexArrayOuter(xMin - xCenter, xMax - xCenter, yMin - yCenter, yMax - yCenter),
                0, 0,
                xReflect, yReflect
        );

        PolygonObstacle obj = new PolygonObstacle(verts, xCenter, yCenter);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(WALL_DENSITY);
        obj.setFriction(WALL_FRICTION);
        obj.setRestitution(WALL_RESTITUTION);
        obj.setName("wall");

        obj.setCategoryBits(1, 1, 0);
        obj.setMaskBits(1, 1, 0);

        return obj;
    }

    // doors is {R, U, L, D}
    public static void generateWallObstacles(ArrayList<Obstacle> out, Rectangle bounds, boolean[] doors) {
        // Top-Left
        PolygonObstacle obj = createCornerObstacle(
                bounds.x, bounds.x + bounds.width / 2 - (doors[1] ? DOOR_THICKNESS / 2 : 0),
                bounds.y + bounds.height / 2 + (doors[2] ? DOOR_THICKNESS / 2 : 0), bounds.y + bounds.height,
                false, false
        );
        out.add(obj);

        // Bottom-Left
        obj = createCornerObstacle(
                bounds.x, bounds.x + bounds.width / 2 - (doors[3] ? DOOR_THICKNESS / 2 : 0),
                bounds.y, bounds.y + bounds.height / 2 - (doors[2] ? DOOR_THICKNESS / 2 : 0),
                false, true
        );
        out.add(obj);

        // Bottom-Right
        obj = createCornerObstacle(
                bounds.x + bounds.width / 2 + (doors[3] ? DOOR_THICKNESS / 2 : 0), bounds.x + bounds.width,
                bounds.y, bounds.y + bounds.height / 2 - (doors[0] ? DOOR_THICKNESS / 2 : 0),
                true, true
        );
        out.add(obj);

        // Top-Right
        obj = createCornerObstacle(
                bounds.x + bounds.width / 2 + (doors[1] ? DOOR_THICKNESS / 2 : 0), bounds.x + bounds.width,
                bounds.y + bounds.height / 2 + (doors[0] ? DOOR_THICKNESS / 2 : 0), bounds.y + bounds.height,
                true, false
        );
        out.add(obj);
    }

    private Rectangle gridScreen;
    /* bottom-left corner of room grid in world space */
    private Vector2 gridOrigin;
    /* grid size in units */
    private Vec2i gridSize;

    private final Vector2 temp;

    /* list of enemies in room */
    private ArrayList<LivingEntity> enemies;
    private ArrayList<Obstacle> obstacles;
    private ArrayList<ItemModel> items;

    private GameCanvas canvas;

    private int[][] gridOccupancy;
    private int[][] adjOccupancy;
    private int[][] pitRender;

    private RoomData currentRoomData;
    private ArrayList<DoorModel> doorModels;

    private BorderModel pitBorderModel;
    private BorderModel floorBorderModel;

    public RoomController(GameCanvas c) {
        canvas = c;

        gridScreen = new Rectangle();
        gridOrigin = new Vector2();
        gridSize = new Vec2i();
        enemies = new ArrayList<LivingEntity>();
        obstacles = new ArrayList<Obstacle>();
        items = new ArrayList<ItemModel>();
        temp = new Vector2();
        doorModels = new ArrayList<DoorModel>();

        pitBorderModel = new BorderModel(pitTextures);
        floorBorderModel = new BorderModel(floorTextures);
        pitBorderModel.drawMiddles = false;
        floorBorderModel.drawMiddles = false;
    }

    public RoomData getCurrentRoomData() {
        return currentRoomData;
    }

    public int getOccupancy(int x, int y) {
        if (x < 0 || x >= gridSize.x || y < 0 || y >= gridSize.y)
            return 3;
        return gridOccupancy[x][y];
    }

    public int getAdjOccupancy(int x, int y) {
        if (x < 0 || x >= gridSize.x || y < 0 || y >= gridSize.y)
            return 3;
        return adjOccupancy[x][y];
    }

    public int getGridSizeX() {
        return gridSize.x;
    }

    public int getGridSizeY() {
        return gridSize.y;
    }

    public Vector2 screenToRoomSpace(Vector2 p) {
        return screenToRoomSpace(p.x, p.y);
    }

    public Vector2 screenToRoomSpace(float x, float y) {
        return Geometry.screenToRoomSpace(gridOrigin, x, y);
    }

    public Vector2 toRoomSpace(Vector2 p) {
        return toRoomSpace(p.x, p.y);
    }

    public Vector2 toRoomSpace(float x, float y) {
        return Geometry.worldToRoomSpace(gridOrigin, x, y);
    }

    public Vector2 roomToWorldSpace(Vector2 p) {
        return roomToWorldSpace(p.x, p.y);
    }

    public Vector2 roomToWorldSpace(float x, float y) {
        return Geometry.roomToWorldSpace(gridOrigin, x, y);
    }

    public Vector2 roomToScreenSpace(float x, float y) {
        return Geometry.worldToScreenSpace(Geometry.roomToWorldSpace(gridOrigin, x, y));
    }

    // a more generic method for adding new obstacle datas to the current room
    public void addObstacleData(ObstacleData o) {
        currentRoomData.obstacleDataList.add(o);
        obstacles.add(o.toObject());
    }

    /*public Obstacle createKey(PlayerModel p) {
        Obstacle key = new KeyModel(p.keyX, p.keyY);
        key.setDrawScale(Geometry.gridPixelUnit, Geometry.gridPixelUnit);
        obstacles.add(key);
        return key;
    }*/

    public void unloadCurrentRoom(World world) {
        if (currentRoomData == null) return;

        for (Obstacle o : obstacles) {
            o.deactivatePhysics(world);
        }
        for (int i = items.size() - 1; i >= 0; i--) {
            ItemModel item = items.get(i);
            if (item.getCollected()) {
                currentRoomData.itemDataList.remove(i);
            }
            item.deactivatePhysics(world);
        }
        for (int i = enemies.size() - 1; i >= 0; i--) {
            LivingEntity le = enemies.get(i);
            if (i < currentRoomData.enemyDataList.size()){
                if (!le.respawn) {
                    if (!le.isAlive() && !(le instanceof KeyEnemy) ) {
                        currentRoomData.enemyDataList.remove(i);
                    } else {
                        currentRoomData.enemyDataList.get(i).updateFromObject(le);
                    }
                }
            }

            le.delete(world);
        }

        for (int i=0; i<doorModels.size(); i++) {
            if (currentRoomData.doors[i]) {
                DoorModel d = doorModels.get(i);
                d.deactivatePhysics(world);
            }
        }

        enemies.clear();
        obstacles.clear();
        items.clear();
        currentRoomData = null;
    }

    public void playerTouchedDoor(PlayerModel p, DoorModel d) {
        if (!d.getLocked()) return;
        boolean canOpen = false;
        canOpen = p.hasKey(d.getKeyId());
        if (canOpen) {
            d.setLocked(false);
            currentRoomData.doorLocks[d.index] = false;
        }
    }

    public void loadRoomData(RoomData rd, World world, PlayerModel p) {
        loadRoomData(rd, world, p, -1);
    }

    public void loadRoomData(RoomData rd, World world, PlayerModel p, int entryDirection) {
        if (currentRoomData != null || enemies.size() > 0 || obstacles.size() > 0)
            throw new Error("Unload current room before loading");

        currentRoomData = rd;

        gridSize.set(rd.sizeX, rd.sizeY);
        gridScreen.set(Geometry.getRoomWorldRect(canvas, rd.sizeX, rd.sizeY));
        //gridScreen.set(0, 0, canvas.getWidth(), canvas.getHeight());
        gridOrigin.set(gridScreen.x, gridScreen.y);
        floorBorderModel.gridOrigin.set(gridOrigin);
        pitBorderModel.gridOrigin.set(gridOrigin);

        gridOccupancy = new int[rd.sizeX][rd.sizeY];
        adjOccupancy = new int[rd.sizeX][rd.sizeY];

        generateWallObstacles(obstacles, gridScreen, rd.doors);

        drawScale = new Vector2(gridScreen.width / rd.sizeX, gridScreen.height / rd.sizeY);
        drawScale.set(Geometry.gridPixelUnit, Geometry.gridPixelUnit);

        p.getObstacle().setDrawScale(drawScale);
        if (entryDirection != -1) {
            Vector2 v = Geometry.directionToVector(entryDirection, 4).scl(0.5f).add(0.5f, 0.5f);
            rd.playerPosX = (rd.sizeX + 1f) * v.x - 1f;
            rd.playerPosY = (rd.sizeY + 1f) * v.y - 1f;
        }
        //System.out.println(rd.playerPosX+","+rd.playerPosY+"-"+temp+"-"+gridOrigin+"-"+p.getObstacle().getPosition()+p.getObstacle());

        pitBorderModel.reset(rd.sizeX, rd.sizeY);
        floorBorderModel.reset(rd.sizeX, rd.sizeY);

        for (int x=0; x<rd.sizeX; x++) {
            for (int y=0; y<rd.sizeY; y++) {
                pitBorderModel.data[x][y] = 0;
                floorBorderModel.data[x][y] = 1; // all starts as floor
            }
        }

        for (ObstacleData o : rd.obstacleDataList) {
            Obstacle obs = o.toObject();

            //int isAir = obs instanceof PitModel ? 0 : 2;
            for (int x = o.posX; x < o.posX + o.sizeX; x++) {
                if (x >= rd.sizeX)
                    continue;

                for (int y = o.posY; y < o.posY + o.sizeY; y++) {
                    if (y >= rd.sizeY)
                        continue;

                    if (obs instanceof PitModel) {
                        pitBorderModel.data[x][y] = 1;
                        floorBorderModel.data[x][y] = 0;
                    }

                    gridOccupancy[x][y] = 3; // everything is an air-ground obstacle until we implement flying enemies

                    // don't allow diagonals next to obstacles
                    for (int i = -1; i <= 1; i++) {
                        if (x + i >= rd.sizeX || x + i < 0)
                            continue;

                        for (int j = -1; j <= 1; j++) {
                            if (i == 0 && j == 0 || !(i == 0 || j == 0))
                                continue;

                            if (y + j >= rd.sizeY || y + j < 0)
                                continue;

                            adjOccupancy[x + i][y + j] = 3;
                        }
                    }
                }
            }
            obs.setPosition(obs.getPosition().add(gridOrigin));
            obstacles.add(obs);
        }

        pitBorderModel.computeRender();
        floorBorderModel.computeRender();

        for (int i = 0; i < rd.enemyDataList.size(); i++) {
            EnemyData e = rd.enemyDataList.get(i);
            LivingEntity le = e.toObject();
            le.getObstacle().setPosition(le.getObstacle().getPosition().add(gridOrigin).add(0.5f, 0.5f));
            le.setHealth(e.getHp());
            enemies.add(le);

            if (le instanceof KeyEnemy && !((KeyEnemy) le).keyCollected){
                KeyModel k = new KeyModel(e.posX, e.posY, ((KeyEnemy) le).keyId, false);
                k.setPosition(k.getPosition().add(gridOrigin).add(0.5f, 0.5f));
                k.setSpawner((KeyEnemy)le);
                //items.add(k);
                k.setDrawScale(drawScale);
                k.activatePhysics(world);
                ((KeyEnemy)le).addKey(k);
            }
        }

        for (ItemData i : rd.itemDataList) {
            ItemModel obs = i.toObject();
            obs.setPosition(obs.getPosition().add(gridOrigin));
            items.add(obs);
        }

        for (Obstacle o : obstacles) {
            o.setDrawScale(drawScale);
            o.activatePhysics(world);
        }

        for (ItemModel o : items) {
            o.setDrawScale(drawScale);
            o.activatePhysics(world);
        }

        for (LivingEntity le : enemies) {
            le.getObstacle().setDrawScale(drawScale);
            le.getObstacle().activatePhysics(world);
            le.physicsActivated();
        }

        float sX = currentRoomData.sizeX;
        float sY = currentRoomData.sizeY;
        for (int i=0; i<currentRoomData.doors.length; i++) {
            if (doorModels.size() <= i) {
                doorModels.add(new DoorModel(WALL_THICKNESS, DOOR_THICKNESS));
            }
            if (currentRoomData.doors[i]) {
                DoorModel d = doorModels.get(i);
                d.index = i;
                d.setDrawScale(drawScale);
                d.setLocked(currentRoomData.doorLocks[i]);
                d.setKeyId(currentRoomData.doorLockIndices[i]);
                Vector2 dir = Geometry.directionToVector(i, 4).scl(0.5f);
                float x0 = sX / 2f + (sX + 1) * dir.x;
                float y0 = sY / 2f + (sY + 1) * dir.y;
                d.setPosition(gridOrigin.x + x0, gridOrigin.y + y0);
                d.setAngle((float) (i * Math.PI / 2));
                d.activatePhysics(world);
                if (i == entryDirection) {
                    playerTouchedDoor(p, d);
                    if (d.getLocked()) {
                        temp.set(Geometry.directionToVector(i + 2, 4));
                        currentRoomData.playerPosX += temp.x;
                        currentRoomData.playerPosY += temp.y;
                    }
                }
            }
        }

        temp.set(rd.playerPosX, rd.playerPosY).add(gridOrigin).add(0.5f, 0.5f);
        p.setPosition(temp);
        p.setRespawnPosition(temp);
    }

    public void addLivingEntity(LivingEntity le, World world){
        le.getObstacle().setPosition(le.getObstacle().getPosition().add(gridOrigin).add(0.5f, 0.5f));
        //enemies.add(le);

        le.getObstacle().setDrawScale(drawScale);
        le.getObstacle().activatePhysics(world);
        le.physicsActivated();
    }

    private int isOutOfBounds(Vector2 p) {
        return isOutOfBounds(p.x, p.y);
    }

    private int isOutOfBounds(float x, float y) {
        float l = 0.25f;
        if (x > gridSize.x + 1 + l) return 0;
        if (y > gridSize.y + 1 + l) return 1;
        if (x < -l) return 2;
        if (y < -l) return 3;
        return -1;
    }

    private int isOutOfDoor(Vector2 p) {
        return isOutOfDoor(p.x, p.y);
    }

    private int isOutOfDoor(float x, float y) {
        float l = 0.25f;
        float midY = ((float)gridSize.y)/2 + 0.5f;
        float midX = ((float)gridSize.x)/2 + 0.5f;
        if (x > gridSize.x + 1 + l && Math.abs(y - midY) <= DOOR_THICKNESS/2 + l) return 0;
        if (y > gridSize.y + 1 + l && Math.abs(x - midX) <= DOOR_THICKNESS/2 + l) return 1;
        if (x < -l && Math.abs(y - midY) <= DOOR_THICKNESS/2 + l) return 2;
        if (y < -l && Math.abs(x - midX) <= DOOR_THICKNESS/2 + l) return 3;
        return -1;
    }

    public int obstacleIsOutOfBounds(Obstacle o) {
        return isOutOfBounds(toRoomSpace(o.getPosition()).add(0.5f, 0.5f));
    }

    public boolean playerDidExit(PlayerModel p) {
        int dir = isOutOfDoor(toRoomSpace(p.getPosition()).add(0.5f, 0.5f));
        return dir != -1 && currentRoomData.doors[dir] && (!currentRoomData.doorLocks[dir] || p.hasKey(currentRoomData.doorLockIndices[dir]));
    }

    public LevelController.ExitDirection playerExitDirection(PlayerModel p) {
        return LevelController.ExitDirection.values()[obstacleIsOutOfBounds(p.getObstacle())];
    }

    public void setCanvas(GameCanvas c) {
        canvas = c;
    }

    public void drawTileStream(float xMin, float yMin, float xMax, float yMax, TextureRegion texture) {
        drawTileStream(xMin, yMin, xMax, yMax, texture, 0);
    }

    public void drawTileStream(float xMin, float yMin, float xMax, float yMax, TextureRegion texture, int rotation) {
        if (xMin>=xMax || yMin>=yMax) return;
        texture.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        float spanX = Geometry.gridPixelUnit*(xMax-xMin);
        float spanY = Geometry.gridPixelUnit*(yMax-yMin);
        temp.set(spanX, spanY).rotate(rotation);
        temp.x = Math.round(Math.abs(temp.x)*100)/100f;
        temp.y = Math.round(Math.abs(temp.y)*100)/100f;
        texture.setRegion(0, 0, (int)temp.x, (int)temp.y);
        Vector2 v = roomToScreenSpace(xMin, yMin);
        canvas.draw(texture, canvas.spriteBatch.getColor(), temp.x/2, temp.y/2, v.x+spanX/2, v.y+spanY/2, rotation, 1, 1);
    }

    private void renderRoom() {
        float gridX = gridSize.x;
        float gridY = gridSize.y;
        float pi = (float)Math.PI;
        // bottom
        float div = currentRoomData.doors[3] ? gridX/2f-DOOR_THICKNESS_VISUAL/2f : gridX+1;
        drawTileStream(0, -WALL_THICKNESS, div, 0, wallSideTexture, 180);
        drawTileStream(div+DOOR_THICKNESS_VISUAL, -WALL_THICKNESS, gridX, 0, wallSideTexture, 180);
        // top
        div = currentRoomData.doors[1] ? gridX/2f-DOOR_THICKNESS_VISUAL/2f : gridX;
        drawTileStream(0, gridY, div, gridY+WALL_THICKNESS, wallSideTexture);
        drawTileStream(div+DOOR_THICKNESS_VISUAL, gridY, gridX, gridY+WALL_THICKNESS, wallSideTexture);
        // left
        div = currentRoomData.doors[2] ? gridY/2f-DOOR_THICKNESS_VISUAL/2f : gridY;
        drawTileStream(-WALL_THICKNESS, 0, 0, div, wallSideTexture, 90);
        drawTileStream(-WALL_THICKNESS, div+DOOR_THICKNESS_VISUAL, 0, gridY, wallSideTexture, 90);
        // right
        div = currentRoomData.doors[0] ? gridY/2f-DOOR_THICKNESS_VISUAL/2f : gridY;
        drawTileStream(gridX, 0, gridX+WALL_THICKNESS, div, wallSideTexture, 270);
        drawTileStream(gridX, div+DOOR_THICKNESS_VISUAL, gridX+WALL_THICKNESS, gridY, wallSideTexture, 270);

        drawTileStream(-WALL_THICKNESS, -WALL_THICKNESS, 0, 0, wallCornerTexture, 90);
        drawTileStream(-WALL_THICKNESS, gridY, 0, gridY+WALL_THICKNESS, wallCornerTexture, 0);
        drawTileStream(gridX, gridY, gridX+WALL_THICKNESS, gridY+WALL_THICKNESS, wallCornerTexture, 270);
        drawTileStream(gridX, -WALL_THICKNESS, gridX+WALL_THICKNESS, 0, wallCornerTexture, 180);

        // middle
        canvas.spriteBatch.setColor(FLOOR_COLOR);
        drawTileStream(0, 0, gridX, gridY, floorTexture);
        canvas.spriteBatch.setColor(Color.WHITE);
    }

    public void step(ProjectileController projectileController, int living_enemies, float dt) {
        for (Obstacle o : obstacles) {
            if (o instanceof ProjectileSpawnModel) {
                ((ProjectileSpawnModel)o).step(projectileController, dt);
            }
        }

        if (living_enemies == 0){
            for (DoorModel door : doorModels){
                if (door.isEnemyLocked()) {
                    door.setLocked(false);
                    currentRoomData.doorLocks[door.index] = false;
                }
            }
        }
    }

    public void draw(float dt, PlayerModel p, ProjectileController projectileController) {
        renderRoom();
        floorBorderModel.drawAll(canvas);

        for (int i=0; i<currentRoomData.doors.length; i++) {
            if (currentRoomData.doors[i]) {
                DoorModel d = doorModels.get(i);
                d.drawBottom(canvas);
            }
        }

        for (Obstacle o : obstacles) {
            if (o instanceof PitModel) {
                PitModel pit = (PitModel)o;
                pit.draw(canvas, dt);
                for (int x=pit.x; x<pit.x+pit.width; x++) {
                    for (int y=pit.y; y<pit.y+pit.height; y++) {
                        pitBorderModel.drawTile(canvas, x, y);
                    }
                }
            } else {
                o.draw(canvas);
            }
        }

        for (ItemModel i : items) {
            if (!i.getCollected()) {
                i.draw(canvas);
            }
        }

        for (LivingEntity le : enemies) {
            if (le.isAlive() || le instanceof KeyEnemy) {
                le.draw(canvas, dt);
            }
        }
        projectileController.draw(canvas, dt);

        p.draw(canvas, dt);

        for (int i=0; i<currentRoomData.doors.length; i++) {
            if (currentRoomData.doors[i]) {
                DoorModel d = doorModels.get(i);
                d.drawTop(canvas);
            }
        }
    }

    public void drawDebug(PlayerModel p) {
        canvas.beginDebug();
        for (Obstacle o : obstacles) {
            o.drawDebug(canvas);
        }
        for (LivingEntity le : enemies) {
            le.getObstacle().drawDebug(canvas);
        }
        p.drawDebug(canvas);

        canvas.endDebug();
    }

    /*public void deleteEnemy(LivingEntity le) {
        int i = enemies.indexOf(le);
        if (i != -1) {
            enemies.remove(i);
            currentRoomData.enemyDataList.remove(i);
            le.delete(world);
        }
    }

    public void deleteItem(ItemModel item) {
        int i = items.indexOf(item);
        if (i != -1) {
            items.remove(i);
            currentRoomData.itemDataList.remove(i);
            item.deactivatePhysics(world);
        }
    }*/

    public ArrayList<LivingEntity> getEnemies() {
        return enemies;
    }

    // todo get rid of eventually
    public static RoomData test() {
        RoomData rd = new RoomData();
        //rd.enemyDataList.add(new EnemyData(3,3,EnemyData.EnemyType.CHASE.ordinal()));

        rd.obstacleDataList.add(new ObstacleData(1,1,1,1,1));
        rd.obstacleDataList.add(new ObstacleData(10,8,4,1,1));


        rd.doors= new boolean[] {false, true, false, false};
        rd.doorIndices = new int[] {0,0,0,0};
        rd.sizeX = 16;
        rd.sizeY = 12;

        JSONUtil.writeStringToFile(rd.toJSON(), "TestRoom.json");

        return rd;
    }
}