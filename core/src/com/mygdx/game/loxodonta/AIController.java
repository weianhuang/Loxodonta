package com.mygdx.game.loxodonta;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.loxodonta.model.*;
import com.mygdx.game.util.Geometry;

import java.util.LinkedList;

public class AIController {

    // reference to the current roomcontroller, for occupancy testing
    private RoomController roomController;
    private ProjectileController projectileController;

    private Vector2 temp1;
    private Vector2 temp2;
    private Vector2 temp3;
    private VisionCallback callback;

    private LinkedList<Tile> queue;

    public AIController(RoomController rc, ProjectileController pc) {
        roomController = rc;
        projectileController = pc;
        temp1 = new Vector2();
        temp2 = new Vector2();
        temp3 = new Vector2();
        callback = new VisionCallback();
        queue = new LinkedList<Tile>();
    }

    public void stepEnemy(EnemyModel enemy, PlayerModel player, float dt, World world){
        if (enemy.getState() == LivingEntity.EntityState.DASH_STUN && !player.isDashing())
            enemy.endDashStun();

        if (enemy.isAlive()) {
            if (enemy.getState() != LivingEntity.EntityState.STUN && enemy.getState() != LivingEntity.EntityState.DASH_STUN) {
                if (enemy instanceof PathEnemy) {
                    stepPathEnemy((PathEnemy) enemy);
                } else if (enemy instanceof ChaseEnemy) {
                    stepChaseEnemy((ChaseEnemy) enemy, player, world);
                } else if (enemy instanceof StaticEnemy) {
                    enemy.setMoveDirection(0, 0);
                } else if (enemy instanceof KeyEnemy) {
                    stepKeyEnemy((KeyEnemy) enemy, player, dt, world);
                } else if (enemy instanceof ShootEnemy) {
                    stepShootEnemy((ShootEnemy) enemy, player, dt, world);
                }
            }
            enemy.step(dt);
        }
    }

    private void stepPathEnemy(PathEnemy enemy){
        temp1.set(roomController.roomToWorldSpace(enemy.getTargetPoint())).add(0.5f, 0.5f);
        temp2.set(enemy.getPosition());
        if (temp3.set(temp2).sub(temp1).len() <= 0.05) {
        //if (Math.round(pos.x) == enemy.getTargetPoint().x && Math.round(pos.y) == enemy.getTargetPoint().y){
            enemy.nextTarget();
        }
        temp1.set(roomController.roomToWorldSpace(enemy.getTargetPoint())).add(0.5f, 0.5f);
        Vector2 dir = temp1.sub(temp2).nor();
        enemy.setMoveDirection(dir.x, dir.y);
    }

    private void stepChaseEnemy(ChaseEnemy enemy, PlayerModel player, World world){
        boolean seen = inSight(enemy.getPosition(), player.getPosition(), enemy.getAngle(), enemy.sight_distance, (float) (Math.toRadians(45)));
        VisionCallback callback = raycast(enemy.getPosition(), player.getPosition(), world);

        if (seen && callback.playerInSight()){
            enemy.targetPoint = player.getPosition();
            enemy.chasing = true;
        }else if (enemy.chasing && enemy.getPosition() == enemy.targetPoint){
            enemy.chasing = false;
        }

        Vector2 playerPos = new Vector2(roomController.toRoomSpace(player.getPosition())).add(0.5f,0.5f);
        Vector2 dir;
        if (enemy.chasing){
            dir = moveTowardsTarget(enemy, playerPos);
        }else{
            dir = new Vector2(0,0);
        }

        enemy.setMoveDirection(dir.x,dir.y);
    }

    private void stepShootEnemy(ShootEnemy enemy, PlayerModel player, float dt, World world){
        boolean seen = inSight(enemy.getPosition(), player.getPosition(), enemy.getAngle(), enemy.sight_distance, (float) (Math.toRadians(enemy.sight_angle)));

        enemy.tracking = false;
        if (seen){
            VisionCallback callback = raycast(enemy.getPosition(), player.getPosition(), world);
            if (callback.playerInSight()) {
                enemy.target_point = player.getPosition();
                enemy.tracking = true;
            }else{
                enemy.tracking = false;
            }
        }

        if (enemy.tracking){
            if (!enemy.shoot(projectileController)) {
                enemy.rotateTowards(enemy.target_point.x, enemy.target_point.y);
            }
        }else{
            enemy.stopRotation();
        }
    }

    private void stepKeyEnemy(KeyEnemy enemy, PlayerModel player, float dt, World world){
        //boolean seen = inSight(enemy.getPosition(), player.getPosition(), enemy.getAngle(), enemy.sight_distance, (float) (Math.toRadians(45)));
        VisionCallback callback = raycast(enemy.getPosition(), player.getPosition(), world);

        enemy.targetPoint = player.getPosition();
        Vector2 dist = new Vector2(enemy.getPosition());
        float d = dist.sub(player.getPosition()).len();

        // change state
        switch (enemy.keyState){
            case IDLE:
                if (d < enemy.minDist && !enemy.cornered){
                    enemy.keyState = KeyEnemy.KeyState.RUN;
                }else if (callback.playerInSight()){
                    if (enemy.shots >= enemy.MAX_SHOTS){
                        enemy.keyState = KeyEnemy.KeyState.SUMMON;
                    }else {
                        enemy.keyState = KeyEnemy.KeyState.SHOOT;
                    }
                }
                break;

            case RUN:
                if (d > enemy.maxDist || enemy.cornered){
                    enemy.keyState = KeyEnemy.KeyState.IDLE;
                    enemy.prevDir = 0;
                }
                break;

            case SHOOT:
                if (d < enemy.minDist && !enemy.cornered){
                    enemy.keyState = KeyEnemy.KeyState.RUN;
                }else if (!callback.playerInSight()){
                    enemy.keyState = KeyEnemy.KeyState.IDLE;
                }

                if (enemy.shots >= enemy.MAX_SHOTS){
                    enemy.keyState = KeyEnemy.KeyState.SUMMON;
                }
                break;

            case SUMMON:
                if (d < enemy.minDist && !enemy.cornered){
                    enemy.keyState = KeyEnemy.KeyState.RUN;
                }else if (!callback.playerInSight()){
                    enemy.keyState = KeyEnemy.KeyState.IDLE;
                }

                if (enemy.shots < enemy.MAX_SHOTS){
                    enemy.keyState = KeyEnemy.KeyState.SHOOT;
                }

                break;
        }

        // act
        Vector2 dir = new Vector2(0,0);
        switch (enemy.keyState){
            case IDLE:
                dir = new Vector2(0,0);
                break;

            case RUN:

                Vector2 curr = new Vector2(roomController.toRoomSpace(enemy.getPosition()));
                curr.set(Math.round(curr.x),Math.round(curr.y));

                dir.set(0,0);
                Vector2[] dirs = new Vector2[3];

                dirs[0] = enemy.getPosition().sub(enemy.targetPoint).nor();
                dirs[1] = new Vector2(dirs[0].y, dirs[0].x * -1);
                dirs[2] = new Vector2(dirs[0].y * -1, dirs[0].x);

                int i = enemy.prevDir;
                int n = 0;
                while (n < dirs.length){
                    Vector2 dir2 = new Vector2(dirs[i]);

                    /*if (Math.abs(dir2.x) > Math.abs(dir2.y)){
                        dir2.scl(1/Math.abs(dir2.x), 1/Math.abs(dir2.x));
                    }else{
                        dir2.scl(1/Math.abs(dir2.y), 1/Math.abs(dir2.y));
                    }*/

                    dir2.scl(1/Math.abs(dir2.x*1.5f), 1/Math.abs(dir2.y*1.5f));

                    int x = (int)(curr.x+dir2.x);
                    int y = (int)(curr.y+dir2.y);

                    if (roomController.getOccupancy(x,y) == 0) {
                        dir = dirs[i];

                        if (i != 0 && enemy.prevDir == 0)
                            enemy.prevTime = 20;

                        enemy.prevDir = i;

                        break;
                    }else{
                        n++;
                        i = (i+1) % dirs.length;
                    }
                }

                if (dir.x == 0 && dir.y == 0){
                    enemy.cornered = true;
                    enemy.cornerTime = 260;
                }


                //enemy.keyState = KeyEnemy.KeyState.SHOOT;

                break;
            case SHOOT:
                enemy.rotateTowards(enemy.targetPoint.x, enemy.targetPoint.y);

                enemy.shoot(projectileController);
                break;

            case SUMMON:
                curr = new Vector2(roomController.toRoomSpace(enemy.getPosition()));
                Vector2 tar = new Vector2(roomController.toRoomSpace(enemy.targetPoint));

                enemy.rotateTowards(enemy.targetPoint.x, enemy.targetPoint.y);

                int x = (int)(curr.x + tar.x)/2;
                x = (int)(curr.x + x)/2;
                int y = (int)(curr.y + tar.y)/2;
                y = (int)(curr.y+y)/2;

                float angle = (float)Math.atan2(tar.y - y, tar.x - x);

                enemy.summon(x,y, angle, world, roomController);
                break;
        }

        enemy.setMoveDirection(dir.x,dir.y);
    }

    // targetPos in room coordinates!
    public Vector2 moveTowardsTarget(EnemyModel enemy, Vector2 targetPos){
        Vector2 curr = new Vector2(roomController.toRoomSpace(enemy.getPosition()));
        curr.set(Math.round(curr.x),Math.round(curr.y));

        Vector2 dir = enemy.targetPoint.sub(enemy.getPosition()).nor();

        Vector2 dir2 = new Vector2(dir);

        if (Math.abs(dir2.x) > Math.abs(dir2.y)){
            dir2.scl(1/Math.abs(dir2.x), 1/Math.abs(dir2.x));
        }else{
            dir2.scl(1/Math.abs(dir2.y), 1/Math.abs(dir2.y));
        }

        int x = (int)(curr.x+dir2.x);
        x = Math.min(Math.max(x, 0), roomController.getGridSizeX()-1);
        int y = (int)(curr.y+dir2.y);
        y = Math.min(Math.max(y, 0), roomController.getGridSizeY()-1);

        if (roomController.getAdjOccupancy(x,y) !=0 || roomController.getOccupancy(x,y) != 0){

            Vector2 pos = new Vector2(roomController.toRoomSpace(enemy.getPosition()));

            pos.x = Geometry.clamp((int)pos.x, 0, roomController.getGridSizeX()-1);
            pos.y = Geometry.clamp((int)pos.y, 0, roomController.getGridSizeY()-1);

            if (enemy.tarV){
                temp1.set(roomController.roomToWorldSpace(enemy.tar)).add(0.5f, 0.5f);
                temp2.set(enemy.getPosition());
                Vector2 temp3 = new Vector2(temp2);
                if (temp3.sub(temp1).len() <= 0.05) {
                    enemy.tar = bfs(enemy.tar, targetPos);
                }
            }else{
                enemy.tar = bfs(pos, targetPos);
                enemy.tarV = true;
            }

            if (enemy.tar.x == -1 && enemy.tar.y == -1){
                dir = new Vector2(0,0);
                enemy.tarV = false;
            }else {
                temp1.set(roomController.roomToWorldSpace(enemy.tar)).add(0.5f, 0.5f);
                dir = temp1.sub(enemy.getPosition()).nor();
            }
        }else{
            enemy.tarV=false;
        }

        return dir;
    }

    class Tile{
        /** The tile's x position */
        public int x;
        /** The tile's y position */
        public int y;
        /** The direction the ship must go as a first step towards this tile */
        public Vector2 dir;
        public float distance;

        public boolean valid;

        /**
         * Creates a Tile object associated with the given coordinates
         * @param x		The x position of the tile on the board
         * @param y		The y position of the tile on the board
         */
        public Tile(int x, int y, float d) {
            this.x = x;
            this.y = y;
            this.dir = new Vector2(0,0);
            distance=d;
            valid=true;
        }

        public Tile(boolean valid){
            this.valid = valid;
        }

        /**
         * Creates a Tile object associated with the given coordinates
         * and a direction for the ship to move to reach it
         *
         * @param x		The x position of the tile on the board
         * @param y		The y position of the tile on the board
         * @param dir 	The direction of the shortest path to the tile
         */
        public Tile(int x, int y, Vector2 dir, float d) {
            this.x = x;
            this.y = y;
            this.dir = dir;
            distance=d;
            valid=true;
        }
    }

    private Vector2 bfs(Vector2 curr, Vector2 target){
        //#region PUT YOUR CODE HERE

        int sizeX = roomController.getGridSizeX();
        int sizeY = roomController.getGridSizeY();

        // Queue of tiles to search
        queue.clear();
        Tile[][] visited = new Tile[sizeX][sizeY];
        //boolean[][] visited = new boolean[roomController.getGridSizeX()][roomController.getGridSizeY()];

        target.set(Geometry.clamp((int)target.x, 0, sizeX-1), Geometry.clamp((int)target.y, 0, sizeY-1));
        int sx = (int)curr.x;
        int sy= (int)curr.y;

        // Add current position to the queue
        Tile t = new Tile(sx, sy, 0);
        queue.add(t);
        visited[sx][sy] = t;

        // Search for the closest goal tile
        while(queue.size() > 0){
            t = queue.removeFirst();

            if ((t.x == target.x) && (t.y == target.y)) {
                return t.dir;
            }

            // add tiles adjacent to t to the queue, if safe
            for (int i = -1; i <= 1; i++){
                if (t.x+i >= sizeX || t.x+i < 0)
                    continue;

                for (int j = -1; j <= 1; j++){
                    float d;
                    if (i!=0 && j !=0)
                        d= 1.75f;
                    else
                        d=1;

                    // ignore the starting tile
                    if (i==0 && j==0)
                        continue;

                    if (t.y +j >= sizeY || t.y+j < 0)
                        continue;

                    // If tile was visited, check if this is closer
                    if (visited[t.x +i][t.y+j] != null){
                        if (visited[t.x +i][t.y+j].distance > t.distance + d){
                            visited[t.x +i][t.y+j].distance = t.distance + d;
                            visited[t.x +i][t.y+j].dir = t.dir;
                        }
                    }

                    boolean safe = (roomController.getOccupancy(t.x + i, t.y + j) & 1) == 0;
                    safe = safe || (t.x+i==target.x && t.y+j == target.y && sx != t.x && sy != t.y);

                    // don't allow diagonals next to obstacles
                    if (i!=0 && j!=0)
                        safe = safe && (roomController.getAdjOccupancy(t.x + i, t.y + j) & 1) == 0;

                    // If tile is safe and has not been visited, add it to the queue
                    if (safe && visited[t.x +i][t.y+j] == null)
                    {
                        Vector2 dir;
                        float dist;

                        // If added from the initial location, set direction
                        if (sx == t.x && sy == t.y) {
                            dir = new Vector2(i+sx,j+sy);
                            dist = d;
                        }
                        // Otherwise, inherit the direction from the previous tile in the BFS
                        else{
                            dir = t.dir;
                            dist = t.distance + d;
                        }

                        // Add the tile to the queue and visit it
                        Tile s = new Tile(t.x + i, t.y + j, dir, dist);
                        queue.add(s);
                        visited[t.x+i][t.y+j] = s;
                    }
                }
            }
        }
        return new Vector2(-1,-1);
    }


    /** Inner class deals with vision*/
    private class VisionCallback implements RayCastCallback {
        private float playerFrac;
        private float sightFrac;

        private VisionCallback(){
            reset();
        }

        private void reset(){
            playerFrac = 1;
            sightFrac = 1;
        }

        // can see
        public boolean playerInSight() {
            return playerFrac < 1 && playerFrac < sightFrac;
        }

        public float reportRayFixture(Fixture f, Vector2 s, Vector2 t, float fraction){
            if (fraction < playerFrac){
                if (f.getBody().getUserData() instanceof PlayerModel) {
                    playerFrac = fraction;
                }else if (!f.isSensor() && !(f.getBody().getUserData() instanceof EnemyModel)) {
                    sightFrac = fraction;
                }
            }

            return -1;
        }
    }

    /** Returns true if source can see target, false otherwise */
    private VisionCallback raycast(Vector2 source, Vector2 target, World world) {
        callback.reset();
        world.rayCast(callback, source, target);

        return callback;
    }


    /**
     * Returns whether the target is in sight of source, regardless of obstacles blocking sight.
     *
     * @param source is the observer
     * @param target is the target of observer
     * @param angle is the angle the observer is facing, in radians
     * @param sightDist is the maximum distance an observer can see
     * @param sightAng is the maximum angle of field of vision of observer
     *
     * */
    public boolean inSight(Vector2 source, Vector2 target, float angle, int sightDist, float sightAng) {
        if (source.dst2(target) > sightDist * sightDist) {
            return false;
        }

        float angleDiff = Math.abs(target.sub(source).angleRad() - angle);
        if (angleDiff > Math.PI) angleDiff = Math.abs((float)Math.PI*2 - angleDiff);

        if (angleDiff > sightAng) {
            return false;
        }
        return true;
    }

}
