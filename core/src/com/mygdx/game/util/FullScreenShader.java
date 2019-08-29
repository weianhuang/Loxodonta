package com.mygdx.game.util;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.mygdx.game.loxodonta.GameCanvas;

public abstract class FullScreenShader {
    public abstract void setUniforms(GameCanvas canvas);
    public abstract ShaderProgram getShader();
}
