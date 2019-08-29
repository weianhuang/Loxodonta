package com.mygdx.game.util;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.*;

public class AnimationStream {
    private AnimatedTexture idle;
    private AnimatedTexture current;
    private int currentPriority;
    private LinkedList<AnimatedTexture> actions;

    private class Action {
        public int priority;
        public AnimatedTexture action;
        public boolean active;

        public Action(AnimatedTexture a, int p) {
            action = a;
            priority = p;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Action)) return false;
            Action o = (Action)obj;
            return o.action == action && o.priority == priority;
        }
    }

    public AnimationStream(AnimatedTexture idle) {
        this.idle = idle;
        actions = new LinkedList<AnimatedTexture>();
        nextAction();
    }

    public void playAction(AnimatedTexture action) {
        if (action == null) return;
        action.reset();
        if (current == action) return;
        if (action.getPriority() >= currentPriority) {
            actions.remove(action);
            actions.add(action);
            current = action;
            return;
        }
        int index = 0;
        for (AnimatedTexture a : actions) {
            if (a.getPriority() > action.getPriority()) break;
            index++;
        }
        actions.add(index, action);
    }

    private void nextAction() {
        if (actions.size() == 0) {
            current = idle;
            currentPriority = -1;
        } else {
            current = actions.getLast();
            currentPriority = current.getPriority();
        }
    }

    public void reset(AnimatedTexture idle) {
        this.idle = idle;
        stopAllActions();
    }

    public void stopAllActions() {
        actions.clear();
        nextAction();
    }

    public boolean stopCurrentAction() {
        if (current == idle) return false;
        actions.remove(current);
        nextAction();
        return true;
    }

    public boolean stopAction(AnimatedTexture action) {
        if (action == null) return false;
        if (!actions.contains(action)) return false;
        actions.remove(action);
        if (current == action) {
            nextAction();
        }
        return true;
    }

    public AnimatedTexture step(float dt) {
        if (dt == 0) return current;
        current.step(dt);
        if (current.isCompleted()) {
            AnimatedTexture a = current;
            stopCurrentAction();
            return a;
        }
        return current;
    }

    public AnimatedTexture getTexture() {
        return current;
    }
}
