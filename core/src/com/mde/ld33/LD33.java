package com.mde.ld33;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class LD33 extends Game {
    SpriteBatch batch;
    AssetManager assetMngr;

    @Override
    public void create () {
        batch = new SpriteBatch();
        assetMngr = new AssetManager();
        assetMngr.setLoader(TiledMap.class, new TmxMapLoader());
        assetMngr.load("spritesheet.png", Texture.class);
        assetMngr.load("level1.tmx", TiledMap.class);
        assetMngr.finishLoading();
        setScreen(new PlayScreen(this));
    }
}
