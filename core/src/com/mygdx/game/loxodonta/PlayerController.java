package com.mygdx.game.loxodonta;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.loxodonta.model.PlayerModel;

public class PlayerController {
    private static final float MAP_SENSITIVITY = 500;

    public InputController input;
    public PlayerModel model;

    public boolean paused;

    public PlayerController(World world) {
        input = new InputController(KeyboardLayout.fromPresetLayout(0));//new InputController();
        model = new PlayerModel();
        model.getObstacle().activatePhysics(world);
        model.physicsActivated();
    }

    public void dispose(World world) {
        model.delete(world);
    }

    public void step(GameCanvas canvas, Minimap map, float dt) {
        input.readInput();

        if (paused) {
            Vector2 t = map.getTransform();
            map.setTransform(t.x - input.horizontal * dt * MAP_SENSITIVITY, t.y - input.vertical * dt * MAP_SENSITIVITY);
        } else {
            if (model.isAlive()) {
                if (input.primaryPressed() && model.canDash()) {
                    model.beginDash(input.horizontal, input.vertical);
                } else if (input.secondaryPressed() && model.canMelee()) {
                    model.beginMelee(input.horizontal, input.vertical);
                }
                model.step(dt, input.horizontal, input.vertical);
            }
        }

        if (input.pausePressed()) {
            paused = !paused;
            map.setTransform(0, 0);
            if (paused) {
                int x = PlayerModel.keyBindingTexture.getRegionWidth()/2 + Minimap.BORDER;
                map.setRectangle(x, 0, canvas.getWidth()-x, canvas.getHeight());
                map.setDrawScale(Minimap.DEFAULT_DRAW_SCALE * 2);
                //map.setBackgroundTransparency(0.5f);
                //map.setTransparency(0.2f);
            } else {
                map.setRectangleDefault(canvas);
                map.setDrawScale(Minimap.DEFAULT_DRAW_SCALE);
                //map.setTransparency(0.2f);
            }
        }
    }
}
