package com.mde.ld33.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mde.ld33.LD33;
import java.awt.Toolkit;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                config.width = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
                config.height = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
                config.useGL30 = false;
                config.fullscreen = true;
		new LwjglApplication(new LD33(), config);
	}
}
