package com.mygdx.game.loxodonta.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.loxodonta.GameCanvas;
import com.mygdx.game.util.Geometry;

public class BorderModel {
    private static boolean checkValue(int[][] array, int x, int y) {
        if (x < 0 || y < 0 || x >= array.length || y >= array[x].length) return true;
        return array[x][y] != 0;
    }

    public int[][] data;
    public boolean drawMiddles;
    public TextureRegion[] textures;
    public Vector2 gridOrigin;

    public BorderModel(TextureRegion[] textures) {
        drawMiddles = true;
        this.textures = textures;
        gridOrigin = new Vector2();
    }

    public void reset(int sizeX, int sizeY) {
        data = new int[sizeX][sizeY];
    }

    // key
    // first digit = type, second digit = rotation
    // 0 = full
    // 1 = U
    // 2 = tunnel
    // 3 = L
    // 4 = single
    // 5 = middle
    public void computeRender() {
        for (int x=0; x<data.length; x++) {
            for (int y=0; y<data[x].length; y++) {
                if (data[x][y] == 1) {
                    boolean right = checkValue(data, x+1, y);
                    boolean left = checkValue(data, x-1, y);
                    boolean up = checkValue(data, x, y+1);
                    boolean down = checkValue(data, x, y-1);
                    boolean up_right = checkValue(data, x+1, y+1);
                    boolean up_left = checkValue(data, x-1, y+1);
                    boolean down_right = checkValue(data, x+1, y-1);
                    boolean down_left = checkValue(data, x-1, y-1);
                    int total = (right?1:0) + (left?1:0) + (up?1:0) + (down?1:0);

                    int pType;
                    int pRot = 0;
                    switch(total) {
                        case 0:
                            pType = 0;
                            break;
                        case 1:
                            pType = 1;
                            if (left) {
                                pRot = 1;
                            } else if (down) {
                                pRot = 2;
                            } else if (right) {
                                pRot = 3;
                            }
                            break;
                        case 2:
                            if (right && left || up && down) {
                                pType = 2;
                                if (up) {
                                    pRot = 1;
                                }
                                break;
                            }
                            pType = 3;
                            if (up && left) {
                                pRot = 1;
                            } else if (left && down) {
                                pRot = 2;
                            } else if (down && right) {
                                pRot = 3;
                            }
                            break;
                        case 3:
                            pType = 4;
                            if (!down) {
                                pRot = 1;
                            } else if (!right) {
                                pRot = 2;
                            } else if (!up) {
                                pRot = 3;
                            }
                            break;
                        default:
                            pType = 5;
                    }

                    int corners = 0;
                    if (up && right && !up_right) corners += 1;
                    if (up && left && !up_left) corners += 10;
                    if (down && left && !down_left) corners += 100;
                    if (down && right && !down_right) corners += 1000;

                    data[x][y] = 1 + pRot + pType * 10 + corners * 100;
                }
            }
        }
    }

    private void renderTexture(GameCanvas c, int x, int y, TextureRegion t, int rot) {
        int scale = Geometry.gridPixelUnit;
        c.draw(t, Color.WHITE, scale/2, scale/2, (gridOrigin.x + x + 0.5f)*scale, (gridOrigin.y + y + 0.5f)*scale, rot, 1, 1);
    }

    public void drawTile(GameCanvas c, int x, int y) {
        int pit = data[x][y] - 1;
        int pRot = pit % 10;
        int pType = (pit / 10) % 10;
        if (drawMiddles || pType != 5) {
            renderTexture(c, x, y, textures[pType], pRot * 90);
        }
        pit /= 100;
        for (int i=0; i<4; i++) {
            if (pit % 10 != 0) {
                renderTexture(c, x, y, textures[textures.length-1], i * 90);
            }
            pit /= 10;
        }
    }

    public void drawAll(GameCanvas c) {
        for (int x=0; x<data.length; x++) {
            for (int y=0; y<data[x].length; y++) {
                drawTile(c, x, y);
            }
        }
    }
}