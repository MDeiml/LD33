package com.mde.ld33;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.*;


public class MainMenuScreen implements Screen {
    
    private LD33 game;
    private Stage stage;
    
    public MainMenuScreen(LD33 game) {
        this.game = game;
        stage = new Stage(new FitViewport(400, 300));
        
        Skin skin = game.assetMngr.get("skin.json", Skin.class);
        
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        Label title = new Label("Where-Wolf", skin);
        title.setFontScale(4);
        table.add(title).pad(50);
        table.row();
        
        TextButton startB = new TextButton("Start", skin);
        startB.setDisabled(true);
        startB.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent e, float x, float y, int pointer, int button) {
                MainMenuScreen.this.game.setScreen(new PlayScreen(MainMenuScreen.this.game, 1));
                return true;
            }
        });
        table.add(startB).pad(1f).width(96);
        table.row();
        
        TextButton levelB = new TextButton("Select level", skin);
        levelB.setDisabled(true);
        table.add(levelB).pad(1f).width(96);
        table.row();
        
        TextButton controlB = new TextButton("Controls", skin);
        controlB.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent e, float x, float y, int pointer, int button) {
                MainMenuScreen.this.game.setScreen(new ControlsScreen(MainMenuScreen.this.game, MainMenuScreen.this));
                return true;
            }
        });
        controlB.setDisabled(true);
        table.add(controlB).pad(1f).width(96);
        table.row();
        
        TextButton exitB = new TextButton("Exit", skin);
        exitB.setDisabled(true);
        exitB.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent e, float x, float y, int pointer, int button) {
                Gdx.app.exit();
                return true;
            }
        });
        if(Gdx.app.getType() != Application.ApplicationType.WebGL)
            table.add(exitB).pad(1f).width(96);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void dispose() {
    }
    
}
