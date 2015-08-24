package com.mde.ld33;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;


public class WinScreen implements Screen {
    
    private LD33 game;
    private Stage stage;
    private Camera bgCam;
    
    public WinScreen(LD33 game) {
        this.game = game;
        bgCam = new OrthographicCamera(1, 1);
        stage = new Stage(new FitViewport(400, 300));
        
        Skin skin = game.assetMngr.get("skin.json", Skin.class);
        
        Table table = new Table(skin);
        table.setFillParent(true);
        stage.addActor(table);
        
        Label msg = new Label("You win!", skin);
        msg.setFontScale(6);
        table.add(msg);
        table.row();
        
        TextButton backB = new TextButton("Back", skin);
        backB.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent e, float x, float y, int pointer, int button) {
                WinScreen.this.game.setScreen(new MainMenuScreen(WinScreen.this.game));
                dispose();
                return true;
            }
        });
        backB.setDisabled(true);
        table.add(backB).pad(1f);
    }

    @Override
    public void show() {
        ((InputMultiplexer)Gdx.input.getInputProcessor()).addProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.setProjectionMatrix(bgCam.combined);
        game.batch.begin();
        game.batch.draw(game.assetMngr.get("startbackground.png", Texture.class), -0.5f, -0.5f, 1, 1);
        game.batch.end();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        ((InputMultiplexer)Gdx.input.getInputProcessor()).removeProcessor(stage);
        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
    
}
