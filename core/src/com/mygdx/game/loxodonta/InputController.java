/*
 * InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package com.mygdx.game.loxodonta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.util.*;

/**
 * Class for reading player input. 
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only 
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputController {
	private static float XBOX_TRIGGER_ACTIVE_BOUND = 0.5f;
	private static float DEAD_ZONE = 0.22f;
	private static boolean XBOX_NORMALIZE_MOVE = false;
	private static boolean XBOX_SNAP_DIRECTION = true;

    // Fields to manage buttons
	/** Whether the debug toggle was pressed. */
	private boolean debugHolding;
	private boolean debugPrevious;
	/** Whether the exit button was pressed. */
	private boolean exitHolding;
	private boolean exitPrevious;
	/** Whether the button to advanced worlds was pressed. */
	private boolean nextPressed;
	private boolean nextPrevious;

	/** Whether menu button pressed*/
	private boolean menuPressed;
	private boolean menuPrevious;

	/** Whether space pressed */
	private boolean spacePressed;
	private boolean spacePrevious;

	public boolean primaryHolding;
	private boolean primaryPrevious;

	public boolean secondaryHolding;
	private boolean secondaryPrevious;

	public boolean pauseHolding;
	private boolean pausePrevious;

	public boolean restartHolding;
	private boolean restartPrevious;

	private KeyboardLayout controls;

	/** How much did we move horizontally? */
	public float horizontal;
	/** How much did we move vertically? */
	public float vertical;
	
	/** An X-Box controller (if it is connected) */
	XBox360Controller xbox;
	Vector2 temp;

	public InputController() {
		this(new KeyboardLayout());
	}


	/**
	 * Creates a new input controller
	 *
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputController(KeyboardLayout c) {
		// If we have a game-pad for id, then use it.
		xbox = new XBox360Controller(0);
		horizontal = 0;
		vertical = 0;
		primaryHolding = false;
		primaryPrevious = false;
		secondaryHolding = false;
		secondaryPrevious = false;
		pauseHolding = false;
		pausePrevious = false;


		nextPressed = false;
		nextPrevious = false;
		menuPrevious = false;
		menuPressed = false;
		spacePressed = false;

		restartHolding = false;
		restartPrevious = false;

		controls = c;

		temp = new Vector2();
	}

	/**
	 * Returns the amount of sideways movement.
	 *
	 * -1 = left, 1 = right, 0 = still
	 *
	 * @return the amount of sideways movement.
	 */
	public float getHorizontal() {
		return horizontal;
	}
	
	/**
	 * Returns the amount of vertical movement. 
	 *
	 * -1 = down, 1 = up, 0 = still
	 *
	 * @return the amount of vertical movement. 
	 */
	public float getVertical() {
		return vertical;
	}

	/**
	 * Returns true if the primary action button was pressed.
	 *
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * @return true if the primary action button was pressed.
	 */
	public boolean primaryPressed() {
		return primaryHolding && !primaryPrevious;
	}

	/**
	 * Returns true if the secondary action button was pressed.
	 *
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * @return true if the secondary action button was pressed.
	 */
	public boolean secondaryPressed() {
		return secondaryHolding && !secondaryPrevious;
	}

	/**
	 * Returns true if the player wants to restart.
	 *
	 * @return true if the player wants to restart.
	 */
	public boolean restartPressed() {
		return restartHolding && !restartPrevious;
	}

	/**
	 * Returns true if the player wants to go toggle the pause mode.
	 *
	 * @return true if the player wants to go toggle the pause mode.
	 */
	public boolean pausePressed() {
		return pauseHolding && !pausePrevious;
	}

	/**
	 * Returns true if the player wants to go toggle the menu mode.
	 *
	 * @return true if the player wants to go toggle the menu mode.
	 */
	public boolean menuPressed() {
		return menuPressed && !menuPrevious;
	}

	public boolean nextPressed() {
		return nextPressed && !nextPrevious;
	}

	/**
	 * Returns true if the player hits space
	 *
	 * @return true if the player hits space
	 */
	public boolean spacePressed() {
		return spacePressed && !spacePrevious;
	}

	/**
	 * Returns true if the player wants to go toggle the debug mode.
	 *
	 * @return true if the player wants to go toggle the debug mode.
	 */
	public boolean didDebug() {
		return debugHolding && !debugPrevious;
	}
	
	/**
	 * Returns true if the exit button was pressed.
	 *
	 * @return true if the exit button was pressed.
	 */
	public boolean didExit() {
		return exitHolding && !exitPrevious;
	}

	/**
	 * Returns true if the player wants to go to the next level.
	 *
	 * @return true if the player wants to go to the next level.
	 */
	public boolean didAdvance() {
		return nextPressed && !nextPrevious;
	}

	/**
	 * Reads the input for the player and converts the result into game logic.
	 */
	public void readInput() {
		// Copy state from last animation frame
		// Helps us ignore buttons that are held down
		primaryPrevious = primaryHolding;
		secondaryPrevious = secondaryHolding;
		pausePrevious = pauseHolding;
		restartPrevious = restartHolding;
		nextPrevious = nextPressed;
		menuPrevious = menuPressed;
		spacePrevious = spacePressed;
		
		// Check to see if a GamePad is connected
		if (xbox.isConnected()) {
			readGamepad();
			readKeyboard(true); // Read as a back-up
		} else {
			readKeyboard(false);
		}
	}

	/**
	 * Reads input from an X-Box controller connected to this computer.
	 */
	private void readGamepad() {
		//primaryHolding = (xbox.getRightTrigger() >= XBOX_TRIGGER_ACTIVE_BOUND);
		primaryHolding = xbox.getRB() || xbox.getY();
		secondaryHolding = xbox.getLB() || xbox.getA();
		pauseHolding = xbox.getStart();
		restartHolding = xbox.getBack();

		// Increase animation frame, but only if trying to move
		horizontal = xbox.getLeftX();
		vertical = -xbox.getLeftY();
		if (Math.abs(horizontal) <= DEAD_ZONE) {
            horizontal = 0;
        } else {
		    horizontal *= (Math.abs(horizontal) - DEAD_ZONE) / (1f - DEAD_ZONE);
        }
		if (Math.abs(vertical) <= DEAD_ZONE) {
		    vertical = 0;
        } else {
            vertical *= (Math.abs(vertical) - DEAD_ZONE) / (1f - DEAD_ZONE);
        }
		float nor = (float)Math.sqrt(horizontal*horizontal + vertical*vertical);
		if (nor != 0) {
			if (XBOX_NORMALIZE_MOVE) {
				horizontal /= nor;
				vertical /= nor;
			}
			if (XBOX_SNAP_DIRECTION) {
				int k = Geometry.vectorToNearestDirection(horizontal, vertical, 8);
				Vector2 v = Geometry.directionToVector(k, 8);
				horizontal = v.x;
				vertical = v.y;
			}
		}
	}

	/**
	 * Reads input from the keyboard.
	 *
	 * This controller reads from the keyboard regardless of whether or not an X-Box
	 * controller is connected.  However, if a controller is connected, this method
	 * gives priority to the X-Box controller.
	 *
	 * @param secondary true if the keyboard should give priority to a gamepad
	 */
	private void readKeyboard(boolean secondary) {
		// Give priority to gamepad results
		primaryHolding = (secondary && primaryHolding) || (Gdx.input.isKeyPressed(controls.PrimaryKey));
		secondaryHolding = (secondary && secondaryHolding) || (Gdx.input.isKeyPressed(controls.SecondaryKey));
		pauseHolding  = (secondary && pauseHolding) || (Gdx.input.isKeyPressed(controls.PauseKey));
		restartHolding = (secondary && restartHolding) || (Gdx.input.isKeyPressed(controls.RestartKey));
		nextPressed = (secondary && nextPressed) || (Gdx.input.isKeyPressed(Input.Keys.N));
		menuPressed = (secondary && menuPressed) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));
		spacePressed = (secondary && spacePressed) || (Gdx.input.isKeyPressed(Input.Keys.SPACE));
		
		// Directional controls
		if (!secondary || (horizontal == 0 && vertical == 0)) {
			horizontal = 0;
			if (Gdx.input.isKeyPressed(controls.RightKey)) horizontal += 1;
			if (Gdx.input.isKeyPressed(controls.LeftKey)) horizontal -= 1;

			vertical = 0;
			if (Gdx.input.isKeyPressed(controls.UpKey)) vertical += 1;
			if (Gdx.input.isKeyPressed(controls.DownKey)) vertical -= 1;
		}

        float len2 = horizontal*horizontal + vertical*vertical;
        if (len2 > 1) {
            len2 = (float)Math.sqrt(len2);
            horizontal /= len2;
            vertical /= len2;
        }
	}
}