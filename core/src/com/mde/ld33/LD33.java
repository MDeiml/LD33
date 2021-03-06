package com.mde.ld33;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class LD33 extends Game {
    SpriteBatch batch;
    AssetManager assetMngr;
    Controller controller;

    @Override
    public void create () {
        if(Gdx.app.getType() == Application.ApplicationType.Desktop)
            Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode());
        batch = new SpriteBatch();
        assetMngr = new AssetManager();
        assetMngr.setLoader(TiledMap.class, new TmxMapLoader());
        assetMngr.load("spritesheet.png", Texture.class);
        assetMngr.load("caveBackround.png", Texture.class);
        assetMngr.load("ruinbackground.png", Texture.class);
        assetMngr.load("Jump.wav", Sound.class);
        assetMngr.load("step.wav", Sound.class);
        assetMngr.load("change.wav", Sound.class);
        assetMngr.load("explosion.wav", Sound.class);
        assetMngr.load("skin.atlas", TextureAtlas.class);
        assetMngr.load("music.ogg", Music.class);
        assetMngr.load("startbackground.png", Texture.class);
        assetMngr.load("ascii.fnt", BitmapFont.class);
        assetMngr.finishLoadingAsset("skin.atlas");
        assetMngr.load("skin.json", Skin.class, new SkinLoader.SkinParameter("skin.atlas"));
        
        for(Controller c : Controllers.getControllers()) {
            if(c.getName().toLowerCase().contains("xbox") && c.getName().contains("360"))
                controller = c;
        }
        
        InputAdapter webGlfullscreen = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if(Gdx.app.getType() == Application.ApplicationType.WebGL && keycode == Keys.F) {
                    if (!Gdx.graphics.isFullscreen()) {
                        Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode());
                    }else {
                        Gdx.graphics.setDisplayMode(800, 600, false);
                    }
                    return true;
                }
                return false;
            }
        };
        Gdx.input.setInputProcessor(new InputMultiplexer(webGlfullscreen));
        
        assetMngr.finishLoading();
        assetMngr.get("music.ogg", Music.class).setLooping(true);
        assetMngr.get("music.ogg", Music.class).play();
        setScreen(new MainMenuScreen(this));
    }
    
    @Override
    public void dispose() {
        assetMngr.dispose();
        batch.dispose();
    }
}
