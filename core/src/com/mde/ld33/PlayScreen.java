package com.mde.ld33;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
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
    private static final int CHANGE_RIGHT = 4;
    private static final int CHANGE_LEFT = 5;
    
    private LD33 game;
    private World world;
    private OrthographicCamera cam;
    private Box2DDebugRenderer b2dr;
    private float unprocessed;
    private Body player;
    private int onGround;
    private boolean justJumped;
    private boolean justInteracted;
    private Animation wolfStand;
    private Animation wolfWalk;
    private Animation manStand;
    private Animation manWalk;
    private Animation change;
    private float animTime;
    private float lastStep;
    private int animState;
    private boolean human;
    private TiledMap level;
    private TiledMapRenderer levelRenderer;
    private OrthographicCamera levelCam;
    private OrthographicCamera bgCam;
    private TiledMapTileLayer lightLayer;
    private int lightId;
    
    public PlayScreen(LD33 game, int levelNr) {
        this.game = game;
        world = new World(new Vector2(0, -20f), true);
        world.setContactListener(this);
        cam = new OrthographicCamera();
        b2dr = new Box2DDebugRenderer();
        unprocessed = 0;
        onGround = 0;
        animTime = 0;
        lastStep = 0;
        human = true;
        game.assetMngr.load("level"+levelNr+".tmx", TiledMap.class);
        game.assetMngr.finishLoadingAsset("level"+levelNr+".tmx");
        level = game.assetMngr.get("level"+levelNr+".tmx");
        levelRenderer = new OrthogonalTiledMapRenderer(level, game.batch);
        levelCam = new OrthographicCamera();
        bgCam = new OrthographicCamera(1,1);
        
//        MapLayer objectLayer = level.getLayers().get(1);
        
        int lWidth = ((TiledMapTileLayer)level.getLayers().get(0)).getWidth();
        int lHeight = ((TiledMapTileLayer)level.getLayers().get(0)).getHeight();
        //light
        lightLayer = new TiledMapTileLayer(lWidth, lHeight, 32, 32);
        level.getLayers().add(lightLayer);
        updateLight();
        
        BodyDef bdef = new BodyDef();
        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        
        int gid1 = (Integer)level.getTileSets().getTileSet("objects").getProperties().get("firstgid");
        TiledMapTileLayer layer = (TiledMapTileLayer)level.getLayers().get(0);
        for(int y = 0; y < layer.getHeight(); y++) {
            int start = -1;
            for(int x = 0; x < layer.getWidth(); x++) {
                if(layer.getCell(x, y) != null) {
                    if(start == -1 && layer.getCell(x, y).getTile().getId() != gid1+43)
                        start = x;
                }else if(start != -1) {
                    bdef.type = BodyDef.BodyType.StaticBody;
                    bdef.position.set((start+x)/2f, y+0.5f);
                    Body b = world.createBody(bdef);
                    
                    shape.setAsBox((x-start)/2f, 0.5f);
                    fdef.shape = shape;
                    fdef.isSensor = false;
                    b.createFixture(fdef);
                    start = -1;
                }
            }
            if(start != -1) {
                bdef.type = BodyDef.BodyType.StaticBody;
                bdef.position.set((start+layer.getWidth())/2f, y+0.5f);
                Body b = world.createBody(bdef);

                shape.setAsBox((layer.getWidth()-start)/2f, 0.5f);
                fdef.shape = shape;
                fdef.isSensor = false;
                b.createFixture(fdef);
            }
        }
        
        TextureRegion[] regs = new TextureRegion[3];
        for(int i = 0; i < 3; i++) {
            regs[i] = new TextureRegion(game.assetMngr.get("spritesheet.png", Texture.class), (i+4)*32, 0, 32, 32);
        }
        wolfWalk = new Animation(0.120f, regs);
        
        regs = new TextureRegion[4];
        for(int i = 0; i < 4; i++)
        {
            regs[i] = new TextureRegion(game.assetMngr.get("spritesheet.png", Texture.class), i*32, 0, 32, 32);
        }
        wolfStand = new Animation(0.500f,regs);
        
        regs = new TextureRegion[5];
        for(int i = 0; i < 5; i++)
        {
            regs[i] = new TextureRegion(game.assetMngr.get("spritesheet.png", Texture.class), i*32, 32, 32, 32);
            
        }
        manWalk = new Animation(0.120f,regs);
        
        regs = new TextureRegion[2];
        for(int i = 0; i < 2; i++)
        {
            regs[i] = new TextureRegion(game.assetMngr.get("spritesheet.png", Texture.class), i*32, 64, 32, 32);
            
        }
        manStand = new Animation(0.500f,regs);
        
        regs = new TextureRegion[2];
        for(int i = 0; i < 2; i++)
        {
            regs[i] = new TextureRegion(game.assetMngr.get("spritesheet.png", Texture.class), i*32, 3*32, 32, 32);
            
        }
        change = new Animation(0.100f,regs);
        
        
        
        bdef.type = BodyDef.BodyType.DynamicBody;
        int sx = Integer.parseInt(level.getProperties().get("SpawnX", String.class));
        int sy = Integer.parseInt(level.getProperties().get("SpawnY", String.class));
        bdef.position.set(sx + 0.6f, sy + 0.5f);
        player = world.createBody(bdef);
        
        shape.setAsBox(0.5f, 0.45f);
        fdef.isSensor = false;
        fdef.shape = shape;
        fdef.friction = 1;
        player.createFixture(fdef).setUserData("player");
        
        shape.setAsBox(0.3f, 0.1f, new Vector2(0, -0.45f), 0);
        fdef.isSensor = true;
        player.createFixture(fdef).setUserData("foot");
        
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
        int gid1 = (Integer)level.getTileSets().getTileSet("objects").getProperties().get("firstgid");
        lightId = gid+1;
        for(int x = 0; x < lightLayer.getWidth(); x++) {
            boolean light = true;
            for(int y = lightLayer.getHeight()-1; y >= 0; y--) {
                if(lightLayer.getCell(x, y) == null) {
                    lightLayer.setCell(x, y, new TiledMapTileLayer.Cell());
                }
                if(layer.getCell(x, y) != null && layer.getCell(x, y).getTile().getId() != gid1+42) {
                    if(light && y != 0) {
                        if(layer.getCell(x, y).getTile().getId() != gid1+40)
                            lightLayer.getCell(x, y).setTile(lightSet.getTile(gid+3));
                        else
                            lightLayer.getCell(x, y).setTile(lightSet.getTile(gid+1));
                    }else {
                        lightLayer.getCell(x, y).setTile(lightSet.getTile(gid+0));
                    }
                    light = false;
                }else if(light) {
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
        
        boolean h = lightLayer.getCell((int)player.getPosition().x, (int)player.getPosition().y).getTile().getId() != lightId;
        
        if(human != h) {
            game.assetMngr.get("change.wav", Sound.class).play();
            human = h;
            animTime = 0;
            animState = animState % 2 + 4;
        }
        
        boolean interact = Gdx.input.isKeyPressed(Keys.W) || (game.controller != null && game.controller.getButton(2));
        if(!justInteracted && human && interact) {
            TiledMapTileLayer layer = (TiledMapTileLayer)level.getLayers().get(0);
            MapLayer oLayer = level.getLayers().get("objectLayer");
            int gid1 = (Integer)level.getTileSets().getTileSet("objects").getProperties().get("firstgid");
            int x = (int)player.getPosition().x;
            int y = (int)player.getPosition().y;
            TiledMapTileLayer.Cell cell = layer.getCell(x, y);
            if(cell != null && (cell.getTile().getId() == gid1+43 || cell.getTile().getId() == gid1+44)) {
                int tx = 0, ty = 0;
                for(MapObject o : oLayer.getObjects()) {
                    if(o.getProperties().get("x", Float.class)/32 == x &&
                       o.getProperties().get("y", Float.class)/32+1 == y) {
                        tx = Integer.parseInt(o.getProperties().get("TargetX", String.class));
                        ty = Integer.parseInt(o.getProperties().get("TargetY", String.class));
                    }
                }
                int lever = cell.getTile().getId();
                layer.getCell(tx, ty).setTile(level.getTileSets().getTile(lever == gid1+43 ? gid1+42 : gid1+40));
                cell.setTile(level.getTileSets().getTile(lever == gid1+43 ? gid1+44 : gid1+43));
                updateLight();
            }
        }
        justInteracted = interact;
        
        boolean right = Gdx.input.isKeyPressed(Keys.D) || (game.controller != null && game.controller.getAxis(1) > 0.5f);
        boolean left = Gdx.input.isKeyPressed(Keys.A) || (game.controller != null && game.controller.getAxis(1) < -0.5f);
        
        if(!human && (right || left) && (animTime - lastStep) > 0.360f && onGround > 0) {
            lastStep = (int)(animTime / 0.360f) * 0.360f;
            game.assetMngr.get("step.wav", Sound.class).play();
        }
        
        boolean jump = (game.controller != null && game.controller.getButton(0)) || Gdx.input.isKeyPressed(Keys.SPACE);
        if(onGround > 0)
        {
            if(jump && !justJumped)
            {
                game.assetMngr.get("Jump.wav", Sound.class).play(0.4f);
                player.applyLinearImpulse(0, human ? 10 : 12, player.getPosition().x, player.getPosition().y, true);
            }
            
            if(right || left) {
                player.getFixtureList().get(0).setFriction(0.05f);
            }else {
                player.getFixtureList().get(0).setFriction(human ? 3f : 1f);
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
            if(animState < 4)
                animState = WALK_RIGHT;
            player.applyLinearImpulse(onGround > 0 ? 2f : 1f, 0, pos.x, pos.y, true);
        }
        if(left && vel.x > -speed) {
            if(animState < 4)
                animState = WALK_LEFT;
            player.applyLinearImpulse(onGround > 0 ? -2f : -1f, 0, pos.x, pos.y, true);
        }
        
        if(onGround > 0 && !right && !left && animState < 4) {
            animState = animState % 2;
        }
        
        if(animState >= 4 && animTime > 0.2f) {
            animState = animState % 2;
        }
        
        justJumped = jump;
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
        //cams
        float maxX = lightLayer.getWidth() - cam.viewportWidth/2;
        float minX = cam.viewportWidth/2;
        float maxY = lightLayer.getHeight() - cam.viewportHeight/2;
        float minY = cam.viewportHeight/2;
        cam.position.x = Math.min(Math.max(player.getPosition().x, minX), maxX);
        cam.position.y = Math.min(Math.max(player.getPosition().y, minY), maxY);
        levelCam.position.x = cam.position.x * 32;
        levelCam.position.y = cam.position.y * 32;
        cam.update();
        bgCam.update();
        //background
        game.batch.setProjectionMatrix(bgCam.combined);
        game.batch.begin();
        Texture bg = game.assetMngr.get("caveBackround.png", Texture.class);
        game.batch.draw(bg, -0.5f, -0.5f, 1, 1);
        game.batch.end();
        //level
        levelCam.update();
        levelRenderer.setView(levelCam);
        levelRenderer.render(new int[] {0,1});
        //player
        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        float px = (animState % 2) == 0 ? player.getPosition().x-0.5f : player.getPosition().x-0.5f+1;
        int w = (animState % 2) == 0 ? 1 : -1;
        Animation a = null;
        switch(animState) {
            default:
            case STAND_RIGHT:
            case STAND_LEFT:
                a = human ? manStand : wolfStand;
                break;
            case WALK_RIGHT:
            case WALK_LEFT:
                a = human ? manWalk : wolfWalk;
                break;
            case CHANGE_RIGHT:
            case CHANGE_LEFT:
                a = change;
                break;
        }
        game.batch.draw(a.getKeyFrame(animTime, true), px, player.getPosition().y-0.5f, w, 1);
        game.batch.end();
        //light
        levelRenderer.setView(levelCam);
        levelRenderer.render(new int[] {2});
        //debug
//        b2dr.render(world, cadm.combined);
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
