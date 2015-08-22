package com.mde.ld33;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;


public class PlayScreen implements Screen {

    private LD33 game;
    private World world;
    private OrthographicCamera cam;
    private Box2DDebugRenderer b2dr;
    private float unprocessed;
    
    public PlayScreen(LD33 game) {
        this.game = game;
        world = new World(new Vector2(0, 0), true);
        cam = new OrthographicCamera(Gdx.graphics.getWidth() / 32, Gdx.graphics.getWidth() / 32);
        b2dr = new Box2DDebugRenderer();
        unprocessed = 0;
    }
    
    @Override
    public void show() {
        
    }
    
    public void update(float delta) {
        world.step(delta, 8, 6);
    }

    @Override
    public void render(float delta) {
        unprocessed += delta;
        if(unprocessed >= 1/60f) {
            unprocessed -= 1/60f;
            update(1/60f);
        }
        
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        cam.update();
        b2dr.render(world, cam.combined);
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
    
}
