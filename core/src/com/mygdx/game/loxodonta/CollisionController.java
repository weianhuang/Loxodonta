package com.mygdx.game.loxodonta;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.loxodonta.model.*;
import com.mygdx.game.util.Geometry;
import com.mygdx.game.util.SoundController;

public class CollisionController implements ContactListener {
    private RoomController roomController;
    private ProjectileController projectileController;
    private Vector2 temp1;
    private Vector2 temp2;

    public CollisionController(RoomController rC, ProjectileController pC){
        roomController = rC;
        projectileController = pC;
        temp1 = new Vector2();
        temp2 = new Vector2();
    }

    private void exertOutwardForce(Vector2 point, float radius, float knockback) {
        temp1.set(point);
        for (LivingEntity le : roomController.getEnemies()) {
            if (!le.isAlive()) continue;
            temp2.set(le.getPosition()).sub(temp1);
            float dist = temp2.len();
            if (dist < radius) {
                le.knockback(temp2.nor().scl(knockback * (1 - dist/radius)));
            }
        }
    }

    /// CONTACT LISTENER METHODS
    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Object ob1 = contact.getFixtureA().getUserData();
        Object ob2 = contact.getFixtureB().getUserData();

        // both living
        if (ob1 instanceof LivingEntity && ob2 instanceof LivingEntity) {
            handleCollision((LivingEntity) ob1, (LivingEntity) ob2);
            return;
        }

        // either projectile
        if (ob1 instanceof ProjectileModel || ob2 instanceof ProjectileModel) {
            if (ob1 instanceof ProjectileModel) {
                handleCollision(ob2, (ProjectileModel)ob1);
            } else {
                handleCollision(ob1, (ProjectileModel)ob2);
            }
            return;
        }

        // one living
        if (ob1 instanceof LivingEntity || ob2 instanceof LivingEntity) {
            LivingEntity alive;
            Object other;
            if (ob2 instanceof LivingEntity) {
                other = ob1;
                alive = (LivingEntity)ob2;
            } else {
                other = ob2;
                alive = (LivingEntity)ob1;
            }

            if (!alive.isAlive()) return; // dont even process collisions for dead things

            if (other instanceof Hurtbox) {
                handleCollision((Hurtbox)other, alive);
            } else if (other instanceof PitModel) {
                handleCollision(alive, (PitModel)other);
            } else if (other instanceof ItemModel) {
                handleCollision(alive, (ItemModel)other);
            } else if (other instanceof DoorModel) {
                handleCollision(alive, (DoorModel)other);
            } else if (other instanceof PortalModel) {
                handleCollision(alive, (PortalModel)other);
            }
            return;
        }

        // none living
    }

    private void handleCollision(LivingEntity p1, LivingEntity p2){
        if (!p1.isAlive() || !p2.isAlive()) return; // dont collide dead things
        if(p1 instanceof PlayerModel || p2 instanceof PlayerModel){
            if (p1 instanceof PlayerModel)
                handleCollision((PlayerModel)p1, (EnemyModel)p2);
            else
                handleCollision((PlayerModel)p2, (EnemyModel)p1);
        }
    }

    private void handleCollision(PlayerModel player, EnemyModel enemy){
        // if enemy alive, take damage
        if (enemy.isAlive() && !player.isDashing()) {
            SoundController.getInstance().play("hitbyEnemy", GameplayController.HIT_BY_ENEMY_SOUND,false, 0.8f);
            temp1.set(player.getObstacle().getPosition()).sub(enemy.getObstacle().getPosition()).nor().scl(enemy.getKnockback());
            player.takeDamage(enemy.getDamage(), temp1);
            enemy.dealDamage();
        }
    }

    private void handleCollision(Hurtbox hurtbox, LivingEntity p){
        if (hurtbox.getEntity() == p) return; // dont hit yourself
        if (!p.isAlive() || !hurtbox.getActive())
            return;

        temp1.set(Geometry.angleToVector(hurtbox.getEntity().getAngle())).nor().scl(hurtbox.getKnockback());
        /*if (hurtbox.type == Hurtbox.HurtboxType.DASH) {
            p.setState(LivingEntity.EntityState.DASH_STUN);
        }*/
        boolean kill = p.takeDamage(hurtbox.getDamage(), temp1);

        if (hurtbox.getEntity() instanceof PlayerModel) {
            PlayerModel player = (PlayerModel)hurtbox.getEntity();
            //temp2.set(temp1.scl(-1,-1));
            if (hurtbox.type == Hurtbox.HurtboxType.DASH && kill) {
                exertOutwardForce(player.getPosition(), 3f, 1f); // bad magic numbers
                SoundController.getInstance().play("death", GameplayController.DEATH_SOUND,false, 1f);
                player.resetRedash(p.getObstacle());
            }
        }
    }

    private void handleCollision(LivingEntity p1, PitModel pit){
        p1.fall(pit);
        /*if (p1 instanceof PlayerModel){
            handleCollision((PlayerModel) p1, pit);
        }else{
            p1.fall();
        }*/
    }

    private void handleCollision(LivingEntity alive, PortalModel portal){
        if (!(alive instanceof PlayerModel)) return; // enemy touched portal, do nothing
        ((PlayerModel)alive).portalTouched(portal);
    }

    /*private void handleCollision(PlayerModel p1, PitModel pit){
        float ang = p1.getObstacle().getAngle();
        temp1.set((float) Math.cos(ang),(float) Math.sin(ang)).nor().scl(-1.2f);
        if (!p1.pitVul){
            p1.takeDamage(1, temp1);
        } else{
            temp1.scl(4);
            p1.takeDamage(1, temp1);
            //p1.fall();
        }
    }*/

    public void handleCollision(LivingEntity alive, ItemModel item) {
        if (item.getCollected()) return; // item was already collected, do nothing

        if (!(alive instanceof PlayerModel)) return; // enemy touched item, do nothing

        PlayerModel player = (PlayerModel)alive;
        item.setCollected(true);
        if (item instanceof KeyModel) {
            player.giveKey(((KeyModel) item).getKeyId());
        } else if (item instanceof PeanutModel) {
            player.heal(((PeanutModel)item).getHealing());
        }
    }

    public void handleCollision(LivingEntity alive, DoorModel door) {
        if (!(alive instanceof PlayerModel)) return; // enemy touched door, do nothing
        PlayerModel player = (PlayerModel)alive;

        roomController.playerTouchedDoor(player, door);
    }

    public void handleCollision(Object obj, ProjectileModel proj) {
        if (obj instanceof ProjectileModel) return; // projectiles collide, do nothing
        if (obj instanceof ItemModel) return;

        if (obj instanceof LivingEntity) {
            handleCollision((LivingEntity)obj, proj);
        }else if (obj instanceof Hurtbox){
            if (proj.projectileType == ProjectileModel.ProjectileEnum.SPIT){
                projectileController.removeProjectile(proj);
            }
        }
        else {
            projectileController.removeProjectile(proj);
        }
    }

    public void handleCollision(LivingEntity alive, ProjectileModel proj) {
        if (!proj.active) return; // projectile already collided, do nothing
        if (!(alive instanceof PlayerModel)) return; // enemy touched projectile, do nothing

        projectileController.removeProjectile(proj);

        //PlayerModel player = (PlayerModel)alive;
        //temp1.set(alive.getPosition()).sub(proj.getPosition()).nor();


        if (proj.projectileType == ProjectileModel.ProjectileEnum.SPIT){
            PlayerModel player = (PlayerModel)alive;
            if (player.getPlayerState() == PlayerModel.PlayerState.DASH_PAUSE ||
                player.getPlayerState() == PlayerModel.PlayerState.DASH)
                return;
        }

        temp1.set(proj.getLinearVelocity()).nor().scl(proj.knockback);
        alive.takeDamage(proj.damage, temp1);
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.  We do not use it.
     */
    public void endContact(Contact contact) {}

    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}

    /**
     * Handles any modifications necessary before collision resolution
     *
     * This method is called just before Box2D resolves a collision.  We use this method
     * to implement sound on contact, using the algorithms outlined similar to those in
     * Ian Parberry's "Introduction to Game Physics with Box2D".
     *
     * However, we cannot use the proper algorithms, because LibGDX does not implement
     * b2GetPointStates from Box2D.  The danger with our approximation is that we may
     * get a collision over multiple frames (instead of detecting the first frame), and
     * so play a sound repeatedly.  Fortunately, the cooldown hack in SoundController
     * prevents this from happening.
     *
     * @param  contact  	The two bodies that collided
     * @param  oldManifold  	The collision manifold before contact
     */

    public void preSolve(Contact contact, Manifold oldManifold) {
        //float speed = 0;

        // Use Ian Parberry's method to compute a speed threshold
		/*Body body1 = contact.getFixtureA().getBody();
		Body body2 = contact.getFixtureB().getBody();
		WorldManifold worldManifold = contact.getWorldManifold();
		Vector2 wp = worldManifold.getPoints()[0];
		cache.set(body1.getLinearVelocityFromWorldPoint(wp));
		cache.sub(body2.getLinearVelocityFromWorldPoint(wp));
		speed = cache.dot(worldManifold.getNormal());

		// Play a sound if above threshold
		if (speed > SOUND_THRESHOLD) {
			String s1 = ((Obstacle)body1.getUserData()).getName();
			String s2 = ((Obstacle)body2.getUserData()).getName();
			if (s1.equals("rocket") || s1.startsWith("crate")) {
				SoundController.getInstance().play(s1, COLLISION_SOUND, false, 0.5f);
			}
			if (s2.equals("rocket") || s2.startsWith("crate")) {
				SoundController.getInstance().play(s2, COLLISION_SOUND, false, 0.5f);
			}
		}*/

    }
}
