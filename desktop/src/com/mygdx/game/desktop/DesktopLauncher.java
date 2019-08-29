package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.loxodonta.GDXRoot;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;//1024;
		config.height = 720;//640;
		config.resizable = false;
		config.vSyncEnabled = false;
		new LwjglApplication(new GDXRoot(config.width, config.height), config);
	}
}
