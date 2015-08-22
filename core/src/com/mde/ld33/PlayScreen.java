package com.mde.ld33;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;


public class PlayScreen implements Screen, ContactListener {

    private static final float SPEED = 3f;
    
    private static final int STAND_RIGHT = 0;
    private static final int STAND_LEFT = 1;
    private static final int WALK_RIGHT = 2;
    private static final int WALK_LEFT = 3;
    
    private LD33 game;
    private World world;
    private OrthographicCamera cam;
    private Box2DDebugRenderer b2dr;
    private float unprocessed;
    private Body player;
    private int onGround;
    private boolean justJumped;
    private Animation wolfStand;
    private Animation wolfWalk;
    private Animation manStand;
    private Animation manWalk;
    private float animTime;
    private int animState;
    private boolean human;
    private TiledMap level;
    private TiledMapRenderer levelRenderer;
    private OrthographicCamera levelCam;
    private TiledMapTileLayer lightLayer;
    
    public PlayScreen(LD33 game) {
        this.game = game;
        world = new World(new Vector2(0, -20f), true);
        world.setContactListener(this);
        cam = new OrthographicCamera();
        b2dr = new Box2DDebugRenderer();
        unprocessed = 0;
        onGround = 0;
        animTime = 0;
        human = false;
        level = game.assetMngr.get("level1.tmx");
        levelRenderer = new OrthogonalTiledMapRenderer(level, game.batch);
        levelCam = new OrthographicCamera();
        
        int lWidth = ((TiledMapTileLayer)level.getLayers().get(0)).getWidth();
        int lHeight = ((TiledMapTileLayer)level.getLayers().get(0)).getHeight();
        //light
        lightLayer = new TiledMapTileLayer(lWidth, lHeight, 32, 32);
        level.getLayers().add(lightLayer);
        updateLight();
        
        TiledMapTileLayer layer = (TiledMapTileLayer)level.getLayers().get(0);
        
        TextureRegion[] regs = new TextureRegion[3];
        for(int i = 0; i < 3; i++) {
            regs[i] = new TextureRegion(game.assetMngr.get("spritesheet.png", Texture.class), (i+2)*32, 0, 32, 32);
        }
        wolfWalk = new Animation(0.120f, regs);
        
        regs = new TextureRegion[2];
        for(int i = 0; i < 2; i++)
        {
            regs[i] = new TextureRegion(game.assetMngr.get("spritesheet.png", Texture.class), i*32, 0, 32, 32);
        }
        wolfStand = new Animation(0.500f,regs);
        
        regs = new TextureRegion[3];
        for(int i = 0; i < 3; i++)
        {
            regs[i] = new TextureRegion(game.assetMngr.get("spritesheet.png", Texture.class), (i+2)*32, 32, 32, 32);
            
        }
        manWalk = new Animation(0.120f,regs);
        
        regs = new TextureRegion[2];
        for(int i = 0; i < 2; i++)
        {
            regs[i] = new TextureRegion(game.assetMngr.get("spritesheet.png", Texture.class), i*32, 32, 32, 32);
            
        }
        manStand = new Animation(0.500f,regs);
        
        
        
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        player = world.createBody(bdef);
        
        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);
        fdef.shape = shape;
        fdef.friction = 1;
        player.createFixture(fdef).setUserData("player");
        
        shape.setAsBox(0.3f, 0.1f, new Vector2(0, -0.5f), 0);
        fdef.isSensor = true;
        player.createFixture(fdef).setUserData("foot");
        
        //TEMP
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(0, -2);
        Body temp = world.createBody(bdef);
        
        fdef.isSensor = false;
        shape.setAsBox(5, 0.5f);
        temp.createFixture(fdef);
    }
    
    private void updateLight() {
        TiledMapTileLayer layer = (TiledMapTileLayer)level.getLayers().get(0);
        TiledMapTileSet lightSet = level.getTileSets().getTileSet("light");
        int gid = (Integer)lightSet.getProperties().get("firstgid");
        for(int x = 0; x < lightLayer.getWidth(); x++) {
            boolean light = true;
            for(int y = lightLayer.getHeight()-1; y >= 0; y--) {
                if(lightLayer.getCell(x, y) == null) {
                    lightLayer.setCell(x, y, new TiledMapTileLayer.Cell());
                }
                if(layer.getCell(x, y) != null) {
                    if(light && y != 0) {
                        lightLayer.getCell(x, y).setTile(lightSet.getTile(gid+3));
                    }else {
                        lightLayer.getCell(x, y).setTile(lightSet.getTile(gid+0));
                    }
                    light = false;
                }else if(light) {
                    System.out.println(x);
                    lightLayer.getCell(x, y).setTile(lightSet.getTile(gid+1));
                }else {
                    lightLayer.getCell(x, y).setTile(lightSet.getTile(gid+0));
                }
            }
        }
    }
    
    @Override
    public void show() {
        
    }
    
    public void update(float delta) {
        
        boolean right = Gdx.input.isKeyPressed(Keys.D);
        boolean left = Gdx.input.isKeyPressed(Keys.A);
        
        
        if(onGround > 0)
        {
            if(Gdx.input.isKeyPressed(Keys.SPACE) && !justJumped)
            {
                player.applyLinearImpulse(0, human ? 10 : 12, player.getPosition().x, player.getPosition().y, true);
            }
            
            if(right || left) {
                System.out.println("geh");
                player.getFixtureList().get(0).setFriction(0.05f);
            }else {
                player.getFixtureList().get(0).setFriction(human ? 3f : 0.1f);
            }
        }else {
            player.getFixtureList().get(0).setFriction(0);
        }
        
        Vector2 vel = player.getLinearVelocity();
        Vector2 pos = player.getPosition();
        
        float speed = SPEED * (human ? 1 : 2f);
        
        if(onGround == 0) {
            if(vel.x < 0 && !left) {
                speed = SPEED / 2;
            }else if(vel.x > 0 && !right) {
                speed = SPEED / 2;
            }
        }
        
        if(Math.abs(vel.x) > speed) {
            vel.x = speed * Math.signum(vel.x);
            player.setLinearVelocity(vel);
        }

        if(right && vel.x < speed) {
            animState = WALK_RIGHT;
            player.applyLinearImpulse(onGround > 0 ? 2f : 1f, 0, pos.x, pos.y, true);
        }
        if(left && vel.x > -speed) {
            animState = WALK_LEFT;
            player.applyLinearImpulse(onGround > 0 ? -2f : -1f, 0, pos.x, pos.y, true);
        }
        
        if(onGround > 0 && !right && !left) {
            animState = animState % 2;
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
        
        animTime += delta;
        
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        cam.update();
        //background
        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        Texture bg = game.assetMngr.get("caveBackround.png", Texture.class);
        game.batch.draw(bg, -cam.viewportWidth/2, -cam.viewportHeight/2, cam.viewportWidth, cam.viewportHeight);
        game.batch.end();
        //level
        levelCam.update();
        levelRenderer.setView(levelCam);
        levelRenderer.render(new int[] {0});
        //player
        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        float px = (animState % 2) == 0 ? player.getPosition().x-0.5f : player.getPosition().x-0.5f+1;
        int w = (animState % 2) == 0 ? 1 : -1;
        game.batch.draw((animState < 2 ? (human ? manStand :wolfStand) :( human ?  manWalk : wolfWalk)).getKeyFrame(animTime, true), px, player.getPosition().y-0.5f, w, 1);
        game.batch.end();
        //light
        levelRenderer.setView(levelCam);
        levelRenderer.render(new int[] {1});
        //debug
        b2dr.render(world, cam.combined);
    }
    
   

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = width / 32f / 2f;
        cam.viewportHeight = height / 32f / 2f;
        levelCam.viewportWidth = width / 2f;
        levelCam.viewportHeight = height / 2f;
    }

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
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        
        if("player".equals(b.getUserData()) || "player".equals(a.getUserData())) {
            contact.resetFriction();
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    
}
