package com.mde.ld33;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;


public class ControlsScreen implements Screen {
    
    private LD33 game;
    private Stage stage;
    private Camera bgCam;
    
    public ControlsScreen(LD33 game, final Screen before) {
        this.game = game;
        bgCam = new OrthographicCamera(1, 1);
        stage = new Stage(new FitViewport(400, 300));
        
        Skin skin = game.assetMngr.get("skin.json", Skin.class);
        
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        
        Table table1 = new Table(skin);
        table1.add("A/D");
        table1.add(" - ");
        table1.add("Walk left/right");
        table1.row();
        table1.add("SPACE");
        table1.add(" - ");
        table1.add("Jump");
        table1.row();
        table1.add("W");
        table1.add(" - ");
        table1.add("Interact/Climb up");
        table1.row();
        table1.add("S");
        table1.add(" - ");
        table1.add("Climb down");
        table1.row();
        table1.add("ESC");
        table1.add(" - ");
        table1.add("Main menu");
        table.add(table1);
        table.row();
        
        TextButton backB = new TextButton("Back", skin);
        backB.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent e, float x, float y, int pointer, int button) {
                ControlsScreen.this.game.setScreen(before);
                dispose();
                return true;
            }
        });
        backB.setDisabled(true);
        table.add(backB).pad(1f);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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
        Gdx.input.setInputProcessor(null);
        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
    
}
