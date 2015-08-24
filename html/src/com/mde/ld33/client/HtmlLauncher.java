package com.mde.ld33.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.mde.ld33.LD33;

public class HtmlLauncher extends GwtApplication {
    
//    @Override
//    public void onModuleLoad () {
//        super.onModuleLoad();
//        com.google.gwt.user.client.Window.addResizeHandler(new ResizeHandler() {
//            @Override
//            public void onResize(ResizeEvent ev) {
//                Gdx.graphics.setDisplayMode(ev.getWidth(),ev.getHeight(), false);
//            }
//        });
//    }

    @Override
    public GwtApplicationConfiguration getConfig () {
        return new GwtApplicationConfiguration(800, 600);
    }

    @Override
    public ApplicationListener getApplicationListener () {
        return new LD33();
    }
}