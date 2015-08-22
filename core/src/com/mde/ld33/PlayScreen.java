package com.mde.ld33;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;


public class PlayScreen implements Screen, ContactListener {

    private static final float SPEED = 3f;
    
    private LD33 game;
    private World world;
    private OrthographicCamera cam;
    private Box2DDebugRenderer b2dr;
    private float unprocessed;
    private Body player;
    private Fixture foot;
    private int onGround;
    private boolean justJumped;
    
    
    public PlayScreen(LD33 game) {
        this.game = game;
        world = new World(new Vector2(0, -9.81f), true);
        world.setContactListener(this);
        cam = new OrthographicCamera(Gdx.graphics.getWidth() / 32, Gdx.graphics.getHeight()/ 32);
        b2dr = new Box2DDebugRenderer();
        unprocessed = 0;
        onGround = 0;
        
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        player = world.createBody(bdef);
        
        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);
        fdef.shape = shape;
        player.createFixture(fdef);
        
        shape.setAsBox(0.3f, 0.1f, new Vector2(0, -0.5f), 0);
        fdef.isSensor = true;
        player.createFixture(fdef).setUserData("foot");
        
        //TEMP
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(0, -2);
        Body temp = world.createBody(bdef);
        
        fdef.isSensor = false;
        fdef.friction = 1f;
        shape.setAsBox(5, 0.5f);
        temp.createFixture(fdef);
    }
    
    @Override
    public void show() {
        
    }
    
    public void update(float delta) {
        if(onGround > 0 && !justJumped)
        {
            if(Gdx.input.isKeyPressed(Keys.SPACE))
            {
                player.applyLinearImpulse(0, 10, player.getPosition().x, player.getPosition().y, true);
            }
            
            Vector2 vel = player.getLinearVelocity();
            Vector2 pos = player.getPosition();
            
            if(Math.abs(vel.x) > SPEED) {
                vel.x = SPEED * Math.signum(vel.x);
                player.setLinearVelocity(vel);
            }
            
            boolean right = Gdx.input.isKeyPressed(Keys.D);
            boolean left = Gdx.input.isKeyPressed(Keys.A);
            
            if(right || left) {
                player.getFixtureList().get(0).setFriction(0.2f);
            }else {
                player.getFixtureList().get(0).setFriction(3f);
            }
            
            if(right && vel.x < SPEED) {
                player.applyLinearImpulse(2f, 0, pos.x, pos.y, true);
            }
            if(left && vel.y > -SPEED) {
                player.applyLinearImpulse(-2f, 0, pos.x, pos.y, true);
            }
        }else {
            player.getFixtureList().get(0).setFriction(0);
        }
        justJumped = Gdx.input.isKeyPressed(Keys.SPACE);
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

    @Override
    public void beginContact(Contact contact) {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        
        if("foot".equals(a.getUserData()) || "foot".equals(b.getUserData())) {
            onGround++;
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        
        if("foot".equals(a.getUserData()) || "foot".equals(b.getUserData())) {
            onGround--;
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {}

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    
}
