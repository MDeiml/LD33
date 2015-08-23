package com.mde.ld33;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class LD33 extends Game {
    SpriteBatch batch;
    AssetManager assetMngr;
    Controller controller;

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
        
        for(Controller c : Controllers.getControllers()) {
            if(c.getName().toLowerCase().contains("xbox") && c.getName().contains("360"))
                controller = c;
        }
        
        assetMngr.finishLoading();
        setScreen(new MainMenuScreen(this));
    }
}
