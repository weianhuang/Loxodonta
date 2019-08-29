package com.mygdx.game.util;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.loxodonta.GameCanvas;
import com.mygdx.game.loxodonta.LevelController;

public class Geometry {
    public static final int gridPixelUnit = 50;

    private static Vector2 out = new Vector2();
    private static Vector2 temp = new Vector2();
    private static Rectangle outRect = new Rectangle();
    private static Vec2i out2i = new Vec2i();

    public static float lerp(float a, float b, float p) {
        return (b-a)*p + a;
    }

    public static int clamp(int val, int min, int max) {
        return val > max ? max : (val < min ? min : val);
    }

    public static float clamp(float val, float min, float max) {
        return val > max ? max : (val < min ? min : val);
    }

    public static Vector2 rotate90(float x, float y) {
        return out.set(-y, x);
    }

    public static Vec2i rotate90(int x, int y) {
        return out2i.set(-y, x);
    }

    public static Vector2 exitEnumToVector(LevelController.ExitDirection e) {
        return exitEnumToDirection(e).scl(0.5f).add(0.5f, 0.5f);
    }

    public static Vector2 exitEnumToDirection(LevelController.ExitDirection e) {
        return directionToVector(e.value, 4);
    }

    public static Vector2 screenToRoomSpace(Vector2 gridOrigin, Vector2 p) {
        return screenToRoomSpace(gridOrigin, p.x, p.y);
    }

    public static Vector2 screenToRoomSpace(Vector2 gridOrigin, float x, float y) {
        return worldToRoomSpace(gridOrigin,x/gridPixelUnit, y/gridPixelUnit);
    }

    public static Vector2 worldToRoomSpace(Vector2 gridOrigin, Vector2 p) {
        return worldToRoomSpace(gridOrigin, p.x, p.y);
    }

    public static Vector2 worldToRoomSpace(Vector2 gridOrigin, float x, float y) {
        return out.set(x, y).sub(gridOrigin);
    }

    public static Vector2 roomToWorldSpace(Vector2 gridOrigin, float x, float y) {
        return out.set(x, y).add(gridOrigin);
    }

    public static Vector2 worldToScreenSpace(Vector2 p) {
        return worldToScreenSpace(p.x, p.y);
    }

    public static Vector2 worldToScreenSpace(float x, float y) {
        return out.set(x, y).scl(gridPixelUnit);
    }

    public static Rectangle gridSpaceToScreenSpace(Vector2 gridScreenOrigin, float x, float y, float sx, float sy) {
        temp.set(gridSpaceToScreenSpace(gridScreenOrigin, x, y));
        out.set(gridSpaceToScreenSpace(gridScreenOrigin, sx, sy));
        return outRect.set(temp.x, temp.y, out.x, out.y);
    }

    public static Vector2 gridSpaceToScreenSpace(Vector2 gridScreenOrigin, float x, float y) {
        return out.set(x, y).scl(gridPixelUnit).sub(gridScreenOrigin);
    }

    public static Vector2 gridSpaceToScreenSpace(Vector2 gridScreenOrigin, Vector2 p) {
        return gridSpaceToScreenSpace(gridScreenOrigin, p.x, p.y);
    }

    public static boolean pointInRect(float x, float y, float xMin, float xMax, float yMin, float yMax) {
        return x >= xMin && x <= xMax && y >= yMin && y <= yMax;
    }

    public static boolean pointInRect(float x, float y, Rectangle rect) {
        return pointInRect(x, y, rect.x, rect.x+rect.width, rect.y, rect.y+rect.height);
    }

    public static boolean pointInRect(Vector2 p, Rectangle rect) {
        return pointInRect(p.x, p.y, rect);
    }

    public static boolean circleInRect(float r, Vector2 p, Rectangle rect) {
        return pointInRect(p.x, p.y,
                rect.x + r,
                rect.x + rect.width - r,
                rect.y + r,
                rect.y + rect.height - r
        );
    }

    public static Rectangle getRoomScreenRect(GameCanvas canvas, int roomX, int roomY) {
        int insetX = (canvas.getWidth() - roomX*gridPixelUnit) / 2;
        int insetY = (canvas.getHeight() - roomY*gridPixelUnit) / 2;
        return outRect.set(insetX, insetY, canvas.getWidth() - insetX*2, canvas.getHeight() - insetY*2);
    }

    public static Rectangle getRoomWorldRect(GameCanvas canvas, int roomX, int roomY) {
        float screenX = canvas.getWidth()/(float)gridPixelUnit;
        float screenY = canvas.getHeight()/(float)gridPixelUnit;
        float insetX = (screenX - roomX) / 2f;
        float insetY = (screenY - roomY) / 2f;
        return outRect.set(insetX, insetY, screenX - insetX*2, screenY - insetY*2);
    }

    // angle in range [-pi .. pi]
    public static int angleToNearestDirection(float theta, int divisions) {
        return Math.round((float)(theta / (Math.PI * 2) + 0.5) * divisions + divisions/2) % divisions;
    }

    // Rounds a given vector (x, y) to the nearest direction among [divisions] of a circle
    // (1, 0) is direction 0, progressing counterclockwise
    public static int vectorToNearestDirection(float x, float y, int divisions) {
        return angleToNearestDirection((float)Math.atan2(y, x), divisions);
    }

    public static Vector2 directionToVector(int direction, int divisions) {
        float theta = (float)Math.PI*2 / divisions * direction;
        return angleToVector(theta);
    }

    public static Vector2 angleToVector(float theta) {
        return out.set((float)Math.cos(theta), (float)Math.sin(theta));
    }

    public static float [] rotateVertexArray(float [] p, float xCenter, float yCenter, float theta) {
        float[] out = new float[p.length];

        float s = (float)Math.sin(theta);
        float c = (float)Math.cos(theta);

        for (int i=0; i<p.length; i+=2) {
            float x = p[i] - xCenter;
            float y = p[i+1] - yCenter;
            out[i] = x*c - y*s + xCenter;
            out[i+1] = x*s + y*c + yCenter;
        }

        return out;
    }

    public static float [] reflectVertexArray(float [] p, float xCenter, float yCenter, boolean xReflect, boolean yReflect) {
        float[] out = new float[p.length];

        int xFlip = xReflect ? -1 : 1;
        int yFlip = yReflect ? -1 : 1;

        for (int i=0; i<p.length; i+=2) {
            float x = p[i] - xCenter;
            float y = p[i+1] - yCenter;
            out[i] = x*xFlip + xCenter;
            out[i+1] = y*yFlip + yCenter;
        }

        return out;
    }
}
