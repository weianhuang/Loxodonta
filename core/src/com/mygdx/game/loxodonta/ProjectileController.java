package com.mygdx.game.loxodonta;

import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.loxodonta.model.ProjectileModel;
import com.mygdx.game.util.Geometry;

import java.util.HashSet;
import java.util.LinkedList;

public class ProjectileController {
    private HashSet<ProjectileModel> activeProjectiles;
    private HashSet<ProjectileModel> inactiveProjectiles;
    private LinkedList<ProjectileModel> toAdd;
    private LinkedList<ProjectileModel> toRemove;

    public ProjectileController() {
        activeProjectiles = new HashSet<ProjectileModel>();
        inactiveProjectiles = new HashSet<ProjectileModel>();
        toAdd = new LinkedList<ProjectileModel>();
        toRemove = new LinkedList<ProjectileModel>();
    }

    // add new projectiles to physics world, delete old projectiles from physics world
    public void step(World world) {
        for (ProjectileModel p : toAdd) {
            p.activatePhysics(world);
            activeProjectiles.add(p);
        }
        toAdd.clear();

        for (ProjectileModel p : toRemove) {
            p.deactivatePhysics(world);
            inactiveProjectiles.add(p);
        }
        toRemove.clear();
    }

    public void draw(GameCanvas c, float dt) {
        for (ProjectileModel p : activeProjectiles) {
            p.draw(c, dt);
        }
    }

    // fetch free projectile (or create new one), queue it to be added next step
    public ProjectileModel newProjectile(float x, float y, float vx, float vy, ProjectileModel.ProjectileEnum type) {
        //System.out.println("New projectile, inactive = "+inactiveProjectiles.size()+" active = "+activeProjectiles.size());
        ProjectileModel free;
        if (inactiveProjectiles.isEmpty()) {
            free = new ProjectileModel();
            free.setDrawScale(Geometry.gridPixelUnit, Geometry.gridPixelUnit);
        } else {
            free = inactiveProjectiles.iterator().next();
            inactiveProjectiles.remove(free);
        }
        free.init(x, y, vx, vy, type);
        toAdd.add(free);
        return free;
    }

    // queue projectile to be deleted next step
    public boolean removeProjectile(ProjectileModel p) {
        if (!activeProjectiles.contains(p)) return false;
        //System.out.println("Remove projectile, inactive = "+inactiveProjectiles.size()+" active = "+activeProjectiles.size());
        activeProjectiles.remove(p);
        toRemove.add(p);
        p.active = false;
        return true;
    }

    public void removeAll(World world) {
        //toRemove.addAll(activeProjectiles);
        for (ProjectileModel p : activeProjectiles) {
            p.deactivatePhysics(world);
            p.active = false;
            inactiveProjectiles.add(p);
        }
        activeProjectiles.clear();
        toAdd.clear();
        toRemove.clear();
    }
}
