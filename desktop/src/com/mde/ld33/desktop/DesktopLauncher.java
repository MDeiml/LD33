package com.mde.ld33.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mde.ld33.LD33;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                config.width = 800;
                config.height = 600;
                config.fullscreen = true;
                config.useGL30 = false;
                config.title = "Where-Wolf";
		new LwjglApplication(new LD33(), config);
	}
}
