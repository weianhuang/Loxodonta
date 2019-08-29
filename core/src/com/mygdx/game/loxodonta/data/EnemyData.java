package com.mygdx.game.loxodonta.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mygdx.game.loxodonta.model.*;

public class EnemyData extends ObjectData {
    public enum EnemyType {
        STATIC, PATH, CHASE, KEY, SHOOT
    }

    private int hp;

    public int enemyTypeId;
    public int[] pathX;
    public int[] pathY;
    public boolean flying;
    public int respawn;
    public int keyCollected;
    public int key;

    public EnemyData() {
        this(0, 0, 0);
    }

    public EnemyData(int pX, int pY, int id) {
        this(pX, pY, id, 0);
    }

    public EnemyData(int pX, int pY, int id, int respawn) {
        super(pX, pY);
        enemyTypeId = id;
        hp = 1;
        this.respawn = respawn;
        key = 0;
    }

    public EnemyData(int pX, int pY, int id, int[] pathX, int[] pathY) {
        this(pX, pY, id, pathX, pathY, 1);
    }

    public EnemyData(int pX, int pY, int id, int[] pathX, int[] pathY, int hp) {
        this(pX, pY, id, pathX, pathY, hp, 0);
    }

    public EnemyData(int pX, int pY, int id, int[] pathX, int[] pathY, int hp, int respawn) {
        this(pX, pY, id, pathX, pathY, hp, 0, 0);
    }

    public EnemyData(int pX, int pY, int id, int[] pathX, int[] pathY, int hp, int respawn, int collected) {
        this(pX, pY, id);
        this.pathX = pathX;
        this.pathY = pathY;
        this.hp = hp;

        this.respawn = respawn;
        this.keyCollected = collected;
    }

    @JsonIgnore
    public int getHp() {
        return hp;
    }

    @JsonProperty
    public void setHp(int hp) {
        this.hp = hp;
    }

    public static EnemyData fromJSON(String json) {
        return JSONUtil.autoReadJSON(json, EnemyData.class);
    }

    public void setFromJSON(String json) {
        fromData(fromJSON(json));
    }

    public String toJSON() {
        return JSONUtil.autoWriteJSON(this);
    }

    public void fromData(Data d) {
        if (d instanceof EnemyData) {
            EnemyData o = (EnemyData)d;
            posX = o.posX;
            posY = o.posY;
            enemyTypeId = o.enemyTypeId;
            pathX = o.pathX;
            pathY = o.pathY;
            hp = o.getHp();
            rotation = o.rotation;
            respawn = o.respawn;
            key = o.key;
            keyCollected = o.keyCollected;
        }
    }

    public void updateFromObject(LivingEntity le) {
        hp = le.getHealth();
        if (le instanceof KeyEnemy && ((KeyEnemy) le).keyCollected)
            keyCollected = 1;
        else
            keyCollected = 0;
    }

    public LivingEntity toObject() {
        LivingEntity le;
        switch (EnemyType.values()[enemyTypeId]) {
            case PATH:
                le = new PathEnemy(posX, posY, pathX, pathY);
                break;
            case CHASE:
                le = new ChaseEnemy(posX, posY);
                break;
            case KEY:
                le = new KeyEnemy(posX, posY, key);
                ((KeyEnemy) le).keyCollected = (keyCollected > 0);
                break;

            case SHOOT:
                le = new ShootEnemy(posX, posY);
                break;

            default:
                le = new StaticEnemy(posX, posY);
        }
        le.setAngle(rotation/180f * (float)Math.PI);
        le.setHealth(hp);
        le.respawn = (respawn > 0);

        return le;
    }

    public String toString() {
        return "EnemyModel {posX = "+posX+"; posY = "+posY+"; hp = "+hp+"; enemyTypeId = "+enemyTypeId+"}";
    }
}