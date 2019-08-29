/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter. 
 * There must be some undocumented OpenGL code in setScreen.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
 package com.mygdx.game.loxodonta;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.mygdx.game.loxodonta.data.SaveData;
import com.mygdx.game.util.*;

/**
 * Root class for a LibGDX.  
 * 
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However, 
 * those classes are unique to each platform, while this class is the same across all 
 * plaforms. In addition, this functions as the root class all intents and purposes, 
 * and you would draw it as a root class in an architecture specification.  
 */
public class GDXRoot extends Game implements ScreenListener {
	public static final int EXIT_CODE_DEFAULT = 0;
	public static final int EXIT_CODE_NEXT = 1;
	public static final int EXIT_CODE_QUIT = 2;
	public static final int EXIT_CODE_ABOUT = 3;

	/** AssetManager to load game assets (textures, sounds, etc.) */
	private AssetManager manager;
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas; 
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;
	private LevelEditorMode levelEditor;
	private LevelSelectMode levelSelect;
	private NextLevelMode nextLevel;
	private AboutMode about;

	private Music gameMusic;
	private Music mainMusic;
	private Music levelSelectMusic;

	private SaveData saveData;

	/** Player mode for the the game proper (CONTROLLER CLASS) */
	private int current;
	private GameplayController gameplayController;

	private int width;
	private int height;

	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public GDXRoot(int width, int height) {
		this.width = width;
		this.height = height;

		// Start loading with the asset manager
		manager = new AssetManager();
		
		// Add font support to the asset manager
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		saveData = new SaveData();
	}

	public int getCurrent() {return current;}

	public void setCurrent(int c) {
		current = c;
	}

	/** 
	 * Called when the Application is first created.
	 * 
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas  = new GameCanvas();
		loading = new LoadingMode(canvas,manager,1);

		gameMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/gamePlayMusic.mp3"));
		levelSelectMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/levelSelect.mp3"));
		mainMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/main.mp3"));

		gameMusic.setVolume(0.3f);
		levelSelectMusic.setVolume(0.3f);
		mainMusic.setVolume(0.3f);

		mainMusic.setLooping(true);
		mainMusic.play();

		// Initialize the game worlds
		gameplayController = new GameplayController(width, height);
		gameplayController.preLoadContent(manager);

		setScreen(createLoadingMode());
	}

	/** 
	 * Called when the Application is destroyed. 
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		setScreen(null);
		gameplayController.unloadContent(manager);
		gameplayController.dispose();

		canvas.dispose();
		canvas = null;
	
		// Unload all of the resources
		manager.clear();
		manager.dispose();
		super.dispose();
	}
	
	/**
	 * Called when the Application is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
	}

	private LevelEditorMode createLevelEditor() {
		levelEditor = new LevelEditorMode(canvas);
		levelEditor.create();
		return levelEditor;
	}

	private LevelSelectMode createLevelSelect() {
		levelSelect = new LevelSelectMode(canvas, manager, 1);
		levelSelect.setScreenListener(this);
		for (int i : saveData.completedLevels) {
			levelSelect.setLevelCompleted(i, true);
		}
		levelSelectMusic.setLooping(true);
		levelSelectMusic.play();
		return levelSelect;
	}

	private AboutMode createAbout() {
		about = new AboutMode(canvas,manager,1);
		about.setScreenListener(this);
		return about;
	}

	private LoadingMode createLoadingMode() {
		loading = new LoadingMode(canvas,manager,1);
		loading.setScreenListener(this);
		return loading;
	}

	private NextLevelMode createNextLevel() {
		nextLevel = new NextLevelMode(canvas,manager,1);
		nextLevel.setScreenListener(this);
		return nextLevel;
	}

	private boolean checkVictory(){
		int j = 0;
		for (int i: saveData.completedLevels){
			j++;
		}
		return (j == 10);
	}
	
	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loading) {
			loading.dispose();
			loading = null;
			if (exitCode != EXIT_CODE_ABOUT) {
				mainMusic.stop();
				mainMusic.dispose();
			}
			if (exitCode == 1) {
				setScreen(createLevelEditor());
			} else if (exitCode == EXIT_CODE_ABOUT){
				setScreen(createAbout());
			}else {
				gameplayController.loadContent(manager);
				gameplayController.setScreenListener(this);
				gameplayController.setCanvas(canvas);
				setScreen(createLevelSelect());
			}
		} else if (screen == about) {
			//if (exitCode == EXIT_CODE_QUIT){
				setScreen(createLoadingMode());
			//}
			about.dispose();
			about = null;
		}
		else if (screen == levelSelect) {
			levelSelectMusic.stop();
			levelSelectMusic.dispose();
			if (exitCode == EXIT_CODE_QUIT){
				mainMusic.setLooping(true);
				mainMusic.play();
				setScreen(createLoadingMode());
			} else {
				gameplayController.setLevelIndex(levelSelect.getSelectedLevel());
				setScreen(gameplayController);
				gameMusic.setLooping(true);
				gameMusic.play();
			}
			levelSelect.dispose();
			levelSelect = null;
		} else if (screen == levelEditor){
			levelEditor.dispose();
			levelEditor = null;
			setScreen(createLoadingMode());
		} else if (screen == gameplayController) {
			gameMusic.stop();
			gameMusic.dispose();
			if (gameplayController.isComplete()) {
				saveData.setLevelComplete(gameplayController.getLevelIndex(), true);
			}
			if (exitCode == EXIT_CODE_NEXT) {
				gameMusic.play();
				if (checkVictory()) {
					System.out.println("victory");
					//setScreen(createVictoryMode());
				}
				setScreen(createNextLevel());
			} else {
				mainMusic.play();
				setScreen(createLoadingMode());
			}
		}
		else if (screen == nextLevel){
			if (exitCode == WorldController.EXIT_CONTINUE){
				gameMusic.play();
				gameplayController.setLevelIndex((gameplayController.getLevelIndex()+1) % LevelSelectMode.NUM_LEVELS);
				setScreen(gameplayController);
				nextLevel.dispose();
				nextLevel = null;
			} else if (exitCode == WorldController.EXIT_LEVELS){
				gameMusic.stop();
				gameMusic.dispose();
				levelSelectMusic.setLooping(true);
				levelSelectMusic.play();
				nextLevel.dispose();
				nextLevel = null;
				setScreen(createLevelSelect());
			} else if (exitCode == WorldController.EXIT_RESTART){
				gameMusic.play();
				gameplayController.reset();
				setScreen(gameplayController);
				nextLevel.dispose();
				nextLevel = null;
			}
		} else if (exitCode == EXIT_CODE_QUIT) {
			// We quit the main application
			gameMusic.dispose();
			mainMusic.dispose();
			levelSelectMusic.dispose();
			Gdx.app.exit();
		} else {
			setScreen(createLoadingMode());
		}
	}

}
