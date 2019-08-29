/*
 * RocketWorldController.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package com.mygdx.game.loxodonta;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.loxodonta.data.*;
import com.mygdx.game.loxodonta.model.EnemyModel;
import com.mygdx.game.loxodonta.model.KeyEnemy;
import com.mygdx.game.loxodonta.model.LivingEntity;
import com.mygdx.game.loxodonta.obstacle.*;
import com.mygdx.game.util.*;

import java.util.ArrayList;

/**
 * Gameplay specific controller for the rocket lander game.
 *
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class GameplayController extends WorldController {
	/** Sound Assets*/
    public static final String DASH_SOUND = "sound/dash2.wav";
    public static final String HIT_BY_ENEMY_SOUND = "sound/enemyMelee2.mp3";
    public static final String ENEMY_SPIT_SOUND = "sound/enemySpit.mp3";
    public static final String LEAF_SOUND = "sound/leaf.wav";
    public static final String TURRET_SOUND = "sound/turret.mp3";
    public static final String DEATH_SOUND = "sound/death2.mp3";
    public static final String TELEPORT_SOUND = "sound/teleport.mp3";

    private AssetState assetState = AssetState.EMPTY;

    private PlayerController plyrController;
    private AIController aiController;
    private RoomController roomController;
    private LevelController levelController;
	private CollisionController collisionController;
	private ProjectileController projectileController;
	private Minimap minimap;
	private int level;
	private Vector2 temp;

	private ScreenListener listener;
	private boolean didExit;

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    /**
	 * Preloads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void preLoadContent(AssetManager manager) {
		if (assetState != AssetState.EMPTY) {
			return;
		}

		assetState = AssetState.LOADING;

        // Ship sounds
        manager.load(DASH_SOUND, Sound.class);
        assets.add(DASH_SOUND);
        manager.load(HIT_BY_ENEMY_SOUND, Sound.class);
        assets.add(HIT_BY_ENEMY_SOUND);
        manager.load(ENEMY_SPIT_SOUND, Sound.class);
        assets.add(ENEMY_SPIT_SOUND);
        manager.load(LEAF_SOUND, Sound.class);
        assets.add(LEAF_SOUND);
        manager.load(TURRET_SOUND, Sound.class);
        assets.add(TURRET_SOUND);
        manager.load(DEATH_SOUND, Sound.class);
        assets.add(DEATH_SOUND);
        manager.load(TELEPORT_SOUND, Sound.class);
        assets.add(TELEPORT_SOUND);

        super.preLoadContent(manager);
    }

    public int getLevelIndex(){
	    return level;
    }

    public void setLevelIndex(int l){
        level = l;
        reset();
    }

    private String levelToFilename(int l){
	    switch(l){
            case 0:
                return "Tutorial1.json";
            case 1:
                return "KeyLevel.json";
            case 2:
                return "Tutorial2.json";
            case 3:
                return "CamProgress.json";
            case 4:
                return "PlaytestLevel.json";
            case 5:
                return "PuzzleLevel.json";
            case 6:
                return "LevelHard2.json";
            case 7:
                return "MidLevel.json";
            case 8:
                return "AbyssLevel.json";
            case 9:
                return "PanningLevel.json";
            default:
                return "MidLevel.json";
        }
    }

    /**
     * Loads the assets for this controller.
     * <p>
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void loadContent(AssetManager manager) {
        if (assetState != AssetState.LOADING) {
            return;
        }

		SoundController sounds = SoundController.getInstance();
        sounds.allocate(manager, DASH_SOUND);
        sounds.allocate(manager, HIT_BY_ENEMY_SOUND);
        sounds.allocate(manager, ENEMY_SPIT_SOUND);
        sounds.allocate(manager, LEAF_SOUND);
        sounds.allocate(manager, TURRET_SOUND);
        sounds.allocate(manager, DEATH_SOUND);
        sounds.allocate(manager, TELEPORT_SOUND);
        super.loadContent(manager);
        assetState = AssetState.COMPLETE;
    }

  	private void loadPlayerController() {
        // TODO: move to models as much as possible
        plyrController = new PlayerController(world);
    }

    public void resetAllInstances() {
        setDebug(false);
        setComplete(false);
        setFailure(false);

        projectileController = new ProjectileController();
        roomController = new RoomController(canvas);
        levelController = new LevelController();
        collisionController = new CollisionController(roomController, projectileController);
        world.setContactListener(collisionController);
        aiController = new AIController(roomController, projectileController);
        minimap = new Minimap();
        minimap.setBackgroundTransparency(0.4f);
        minimap.setTransparency(0.2f);
        temp = new Vector2();
        didExit = false;
    }

    public GameplayController(int width, int height) {
        super(width, height);
        resetAllInstances();
    }

    /**
     * Resets the status of the game so that we can play again.
     * <p>
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        Vector2 gravity = new Vector2(world.getGravity());
        didExit = false;

        roomController.unloadCurrentRoom(world);
        projectileController.removeAll(world);
        if (plyrController != null) {
            plyrController.dispose(world);
        }

        /*for (Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }*/
        objects.clear();
        addQueue.clear();
        world.dispose();

        world = new World(gravity, false);
        world.setContactListener(collisionController);
        setComplete(false);
        setFailure(false);

        populateLevel();
    }
/*
    public void addEntity(LivingEntity le) {
        addObject(le.getObstacle());
        le.physicsActivated();

        if (le.type == LivingEntity.EntityType.ENEMY) {
            enemies.add(le);
        }
    }
*/
    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        loadPlayerController();

        levelController.loadLevelData(LevelData.fromJSON(JSONUtil.readStringFromFile(levelToFilename(level))));
        roomController.loadRoomData(levelController.getStartRoomData(), world, plyrController.model);
        minimap.setLevel(levelController.getLevelData());
        minimap.setRectangleDefault(canvas);
        minimap.setDrawScale(Minimap.DEFAULT_DRAW_SCALE);
    }

    private void exit(int code) {
        didExit = true;
        canvas.setTransformXY(0, 0);
        listener.exitScreen(this, code);
    }

    /**
     * The core gameplay loop of this world.
     * <p>
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        plyrController.step(canvas, minimap, dt);
        dt *= plyrController.model.timeValue.getValue();

        if (plyrController.model.requestRoomReset){
            resetRoom();
            plyrController.model.requestRoomReset = false;
        }

        if (plyrController.input.restartPressed()) {
            reset();
            return;
        } else if (plyrController.input.menuPressed() && plyrController.paused) {
            exit(GDXRoot.EXIT_CODE_DEFAULT);
            return;
        } else if (plyrController.input.nextPressed()) {
            exit(GDXRoot.EXIT_CODE_NEXT);
            return;
        } else if (isFailure() || isComplete()) {
            if (countdown > 0) {
                countdown--;
            } else {
                if (isComplete()) {
                    exit(EXIT_NEXT);
                    return;
                }
                reset();
            }
            return;
        }

        if (plyrController.paused) return;

        if (!isFailure()) {
            // check if player is dead
            if (!plyrController.model.isAlive()) {
                // Game over
                setFailure(true);
            }
        }

        int living_enemies = 0;

        for (Object enemy_obj : roomController.getEnemies().toArray()){
            LivingEntity enemy = (LivingEntity)enemy_obj; 
            aiController.stepEnemy((EnemyModel)enemy, plyrController.model, dt, world);
            if (enemy.isAlive())
                living_enemies ++;

            if (enemy.isAlive() && enemy instanceof KeyEnemy){
                for (EnemyModel e : ((KeyEnemy) enemy).summonedEnemies)
                    aiController.stepEnemy(e, plyrController.model, dt, world);
            }
            /*if ((enemy instanceof KeyEnemy) && (!enemy.isAlive())) {
                plyrController.model.keyX = enemy.getPosition().x;
                plyrController.model.keyY = enemy.getPosition().y;
                plyrController.model.killedKeyEnemy = true;
            }*/
        }

        roomController.step(projectileController, living_enemies, dt);

        if (roomController.playerDidExit(plyrController.model)) {
            LevelController.ExitDirection exitDir = roomController.playerExitDirection(plyrController.model);
            RoomData nextRoom = levelController.getConnectingRoomData(roomController.getCurrentRoomData(), exitDir);
            roomController.unloadCurrentRoom(world);
            projectileController.removeAll(world);
            roomController.loadRoomData(nextRoom, world, plyrController.model, exitDir.reverse().value);
            minimap.setFocusRoomIndex(nextRoom.roomIndex);
            /*if (nextRoom.roomIndex == levelController.getLevelData().winRoomIndex) {
                setComplete(true);
            }*/

            plyrController.model.roomTransition();
        }

        // If we use sound, we must remember this.
        projectileController.step(world);
        SoundController.getInstance().update();

        if (plyrController.model.didCompleteLevel()) {
            setComplete(true);
        }
    }

    public void resetRoom(){
        RoomData nextRoom = roomController.getCurrentRoomData();
        roomController.unloadCurrentRoom(world);
        projectileController.removeAll(world);
        roomController.loadRoomData(nextRoom, world, plyrController.model);
    }

    public void postUpdate(float dt) {
        dt *= plyrController.model.timeValue.getValue();
        if (!plyrController.paused) {
            world.step(dt, WorldController.WORLD_VELOC, WorldController.WORLD_POSIT);
        }
    }

    public void setCanvas(GameCanvas canvas) {
        super.setCanvas(canvas);
        roomController.setCanvas(canvas);
        minimap.setRectangleDefault(canvas);
    }

    public void draw(float delta) {
        if (didExit) return;
        canvas.begin(); // DO NOT SCALE
        canvas.clear();

        delta *= plyrController.model.timeValue.getValue();
        if (plyrController.paused) delta = 0;

        temp.set(plyrController.model.getPosition()).scl(Geometry.gridPixelUnit);
        canvas.setTransformFocus(temp, (roomController.getGridSizeX()+2)*Geometry.gridPixelUnit, (roomController.getGridSizeY()+2)*Geometry.gridPixelUnit);

        roomController.draw(delta, plyrController.model, projectileController);

        if (isDebug()) {
            roomController.drawDebug(plyrController.model);
        }

        canvas.renderFullscreenShader(plyrController.model.shockWaveShader);

        // Final message
        if (isComplete() && !isFailure()) {
            displayFont.setColor(Color.PURPLE);
            //canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
        } else if (isFailure()) {
            displayFont.setColor(Color.RED);
            canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
        }

        if (plyrController.paused) {
            plyrController.model.drawKeyBinding(canvas);
        }

        canvas.end();

        // give minimap its own draw pass
        minimap.draw(canvas);
    }
}
