package com.mde.ld33;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class LD33 extends Game {
    SpriteBatch batch;
    AssetManager assetMngr;

    @Override
    public void create () {
        batch = new SpriteBatch();
        assetMngr = new AssetManager();
        assetMngr.setLoader(TiledMap.class, new TmxMapLoader());
        assetMngr.load("spritesheet.png", Texture.class);
        assetMngr.load("caveBackround.png", Texture.class);
        assetMngr.load("Jump.wav", Sound.class);
        assetMngr.load("step.wav", Sound.class);
        assetMngr.load("change.wav", Sound.class);
        assetMngr.load("skin.atlas", TextureAtlas.class);
        assetMngr.finishLoadingAsset("skin.atlas");
        assetMngr.load("skin.json", Skin.class, new SkinLoader.SkinParameter("skin.atlas"));
        assetMngr.finishLoading();
        setScreen(new PlayScreen(this, 3));
    }
}
