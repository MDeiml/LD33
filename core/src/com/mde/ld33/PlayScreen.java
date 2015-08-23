package com.mde.ld33;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import java.util.ArrayList;
import java.util.Arrays;


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
    private TextureRegion wolfJump;
    private TextureRegion manJump;
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
    private int levelNr;
    private ArrayList<String> monolog;
    private float monologTimer = 0;
    private float intro;
    
    public PlayScreen(LD33 game, int levelNr) {
        this.game = game;
        this.levelNr = levelNr;
        world = new World(new Vector2(0, -20f), true);
        world.setContactListener(this);
        cam = new OrthographicCamera();
        b2dr = new Box2DDebugRenderer();
        intro = levelNr == 1 ? 2 : 0;
        unprocessed = 0;
        onGround = 0;
        animTime = 0;
        lastStep = 0;
        monologTimer = 0;
        monolog = new ArrayList<String>();
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
        
        int gid0 = (Integer)level.getTileSets().getTileSet(0).getProperties().get("firstgid");
        int gid1 = 0;
        if(level.getTileSets().getTileSet("objects") != null)
            gid1 = (Integer)level.getTileSets().getTileSet("objects").getProperties().get("firstgid");
        TiledMapTileLayer layer = (TiledMapTileLayer)level.getLayers().get(0);
        for(int y = 0; y < layer.getHeight(); y++) {
            int start = -1;
            for(int x = 0; x < layer.getWidth(); x++) {
                if(layer.getCell(x, y) != null && layer.getCell(x, y).getTile().getId() != gid1+43
                                               && (layer.getCell(x, y).getTile().getId() < gid1+32
                                               || layer.getCell(x, y).getTile().getId()  > gid1+34)
                                               && layer.getCell(x, y).getTile().getId() != gid0+0
                                               && layer.getCell(x, y).getTile().getId() != gid0+2
                                               && layer.getCell(x, y).getTile().getId() != gid0+8
                                               && layer.getCell(x, y).getTile().getId() != gid0+10
                                               && layer.getCell(x, y).getTile().getId() != gid0+3
                                               && layer.getCell(x, y).getTile().getId() != gid1+48) {
                    if(start == -1)
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
                if(layer.getCell(x, y) != null) {
                    if(layer.getCell(x, y).getTile().getId() == gid0 + 0) {
                        bdef.type = BodyDef.BodyType.StaticBody;
                        bdef.position.set(x+0.5f, y+0.5f);
                        Body b = world.createBody(bdef);

                        shape.set(new float[] {-0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f});
                        fdef.shape = shape;
                        fdef.isSensor = false;
                        b.createFixture(fdef);
                    }
                    if(layer.getCell(x, y).getTile().getId() == gid0 + 2) {
                        bdef.type = BodyDef.BodyType.StaticBody;
                        bdef.position.set(x+0.5f, y+0.5f);
                        Body b = world.createBody(bdef);

                        shape.set(new float[] {-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f});
                        fdef.shape = shape;
                        fdef.isSensor = false;
                        b.createFixture(fdef);
                    }
                    if(layer.getCell(x, y).getTile().getId() == gid0 + 8) {
                        bdef.type = BodyDef.BodyType.StaticBody;
                        bdef.position.set(x+0.5f, y+0.5f);
                        Body b = world.createBody(bdef);

                        shape.set(new float[] {-0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f});
                        fdef.shape = shape;
                        fdef.isSensor = false;
                        b.createFixture(fdef);
                    }
                    if(layer.getCell(x, y).getTile().getId() == gid0 + 10) {
                        bdef.type = BodyDef.BodyType.StaticBody;
                        bdef.position.set(x+0.5f, y+0.5f);
                        Body b = world.createBody(bdef);

                        shape.set(new float[] {-0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f});
                        fdef.shape = shape;
                        fdef.isSensor = false;
                        b.createFixture(fdef);
                    }
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
        
        wolfJump = new TextureRegion(game.assetMngr.get("spritesheet.png", Texture.class), 4*32, 0, 32, 32);
        manJump = new TextureRegion(game.assetMngr.get("spritesheet.png", Texture.class), 3*32, 2*32, 32, 32);
        
        bdef.type = BodyDef.BodyType.DynamicBody;
        int sx = Integer.parseInt(level.getProperties().get("SpawnX", String.class));
        int sy = Integer.parseInt(level.getProperties().get("SpawnY", String.class));
        bdef.position.set(sx + 0.6f, sy + 0.5f);
        player = world.createBody(bdef);
        
        shape.setAsBox(0.45f, 0.45f);
        fdef.isSensor = false;
        fdef.shape = shape;
        fdef.friction = 1;
        player.createFixture(fdef).setUserData("player");
        
        shape.setAsBox(0.3f, 0.1f, new Vector2(0, -0.45f), 0);
        fdef.isSensor = true;
        player.createFixture(fdef).setUserData("foot");
        
        if(levelNr == 1) {
            playerSay(new String[] {"Finally I'm free!", "After 10,000 years of being imprisoned..."
                                   ,"Man that was boring!"});
        }
    }
    
    private void playerSay(String[] texts) {
        monolog.addAll(Arrays.asList(texts));
        monologTimer = 3;
    }
    
    private void updateLight() {
        TiledMapTileSet lightSet = level.getTileSets().getTileSet("light");
        int gid = (Integer)lightSet.getProperties().get("firstgid");
        lightId = gid+1;
        for(int x = 0; x < lightLayer.getWidth(); x++) {
            for(int y = 0; y < lightLayer.getHeight(); y++) {
                if(lightLayer.getCell(x, y) == null) {
                    lightLayer.setCell(x, y, new TiledMapTileLayer.Cell());
                }
                lightLayer.getCell(x, y).setRotation(0);
                lightLayer.getCell(x, y).setTile(lightSet.getTile(gid));
            }
        }
        for(int x = 0; x < lightLayer.getWidth(); x++) {
            addRay(x, lightLayer.getHeight()-1, 0, -1);
        }
    }
    
    private void addRay(int x, int y, int dx, int dy) {
        TiledMapTileLayer layer = (TiledMapTileLayer)level.getLayers().get(0);
        TiledMapTileSet lightSet = level.getTileSets().getTileSet("light");
        int gid = (Integer)lightSet.getProperties().get("firstgid");
        int gid1 = 0;
        if(level.getTileSets().getTileSet("objects") != null)
            gid1 = (Integer)level.getTileSets().getTileSet("objects").getProperties().get("firstgid");
        boolean light = true;
        while(light && x >= 0 && y >= 0 && x < lightLayer.getWidth() && y < lightLayer.getHeight()) {
            TiledMapTileLayer.Cell cell = lightLayer.getCell(x, y);
            int r;
            if(dx == 1) {
                r = TiledMapTileLayer.Cell.ROTATE_90;
            }else if(dx == -1) {
                r = TiledMapTileLayer.Cell.ROTATE_270;
            }else if(dy == 1) {
                r = TiledMapTileLayer.Cell.ROTATE_180;
            }else {
                r = TiledMapTileLayer.Cell.ROTATE_0;
            }
            cell.setRotation(r);
            if(layer.getCell(x, y) != null && layer.getCell(x, y).getTile().getId() != gid1+(dx == 0 ? 42 : 40)
                                           && layer.getCell(x, y).getTile().getId() != gid1+43
                                           && layer.getCell(x, y).getTile().getId() != gid1+44) {
                int tid = layer.getCell(x, y).getTile().getId();
                if(tid >= gid1+32 && tid <= gid1+35) {
                    cell.setTile(lightSet.getTile(gid+1));
                    if(dx == 1) {
                        if(tid == gid1 + 33) {
                            addRay(x, y+1, 0, 1);
                        }else if(tid == gid1+34) {
                            addRay(x, y-1, 0, -1);
                        }
                    }else if(dx == -1) {
                        if(tid == gid1 + 32) {
                            addRay(x, y+1, 0, 1);
                        }else if(tid == gid1+35) {
                            addRay(x, y-1, 0, -1);
                        }
                    }else if(dy == -1) {
                        if(tid == gid1 + 32) {
                            addRay(x+1, y, 1, 0);
                        }else if(tid == gid1+33) {
                            addRay(x-1, y, -1, 0);
                        }
                    }else if(dy == 1) {
                        if(tid == gid1 + 35) {
                            addRay(x+1, y, 1, 0);
                        }else if(tid == gid1+34) {
                            addRay(x-1, y, -1, 0);
                        }
                    }
                }else if(light && y != lightLayer.getHeight()-1) {
                    if(layer.getCell(x, y).getTile().getId() != gid1+(dy == -1 ? 40 : (dx == -1 ? 42 : 10000)))
                        cell.setTile(lightSet.getTile(gid+3));
                    else
                        cell.setTile(lightSet.getTile(gid+1));
                }else {
                    cell.setTile(lightSet.getTile(gid+0));
                }
                light = false;
            }else {
                cell.setTile(lightSet.getTile(gid+1));
            }
            x += dx;
            y += dy;
        }
    }
    
    @Override
    public void show() {
        
    }
    
    private int monologCounter = 0;
    
    public void update(float delta) {
        
        if(monologCounter == 0 && levelNr == 1 && player.getPosition().x > 12) {
            monologCounter++;
            playerSay(new String[] {"That next jump looks difficult.", "Maybe my werewolf form is\nable to jump high enough..."});
        }
        if(monologCounter == 0 && levelNr == 3 && player.getPosition().x > 22) {
            monologCounter++;
            playerSay(new String[] {"I need to be a werewolf to make this jump.", "I should try to open that trapdoor up there."});
        }
        if(monologCounter == 0 && levelNr == 4 && player.getPosition().x > 18) {
            monologCounter++;
            playerSay(new String[] {"That mirror is pointing in the wrong direction."});
        }
        if(monologCounter == 0 && levelNr == 5 && player.getPosition().y > 10) {
            monologCounter++;
            playerSay(new String[] {"I can't switch that lever as a wolf!"});
        }
        
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
            int gid1 = 0;
            if(level.getTileSets().getTileSet("objects") != null)
                gid1 = (Integer)level.getTileSets().getTileSet("objects").getProperties().get("firstgid");
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
                int tid = layer.getCell(tx, ty).getTile().getId()-gid1;
                int tid1 = tid;
                switch(tid) {
                    case 40:
                        tid1 = 42;
                        break;
                    case 42:
                        tid1 = 40;
                        break;
                    case 32:
                        tid1 = 33;
                        break;
                    case 33:
                        tid1 = 32;
                        break;
                    case 34:
                        tid1 = 35;
                        break;
                    case 35:
                        tid1 = 34;
                }
                layer.getCell(tx, ty).setTile(level.getTileSets().getTile(tid1+gid1));
                cell.setTile(level.getTileSets().getTile(lever == gid1+43 ? gid1+44 : gid1+43));
                updateLight();
            }
        }
        justInteracted = interact;
        
        TiledMapTileLayer layer = (TiledMapTileLayer)level.getLayers().get(0);
        int gid = (Integer)level.getTileSets().getTileSet("objects").getProperties().get("firstgid");
        TiledMapTileLayer.Cell cell = layer.getCell((int)player.getPosition().x, (int)player.getPosition().y);
        boolean climbing = human && cell != null && cell.getTile().getId() == gid + 48;
        System.out.println(climbing);
        
        boolean right = Gdx.input.isKeyPressed(Keys.D) || (game.controller != null && game.controller.getAxis(1) > 0.5f);
        boolean left = Gdx.input.isKeyPressed(Keys.A) || (game.controller != null && game.controller.getAxis(1) < -0.5f);
        
        if(!human && (right || left) && (animTime - lastStep) > 0.360f && onGround > 0) {
            lastStep = (int)(animTime / 0.360f) * 0.360f;
            game.assetMngr.get("step.wav", Sound.class).play();
        }
        
        boolean jump = (game.controller != null && game.controller.getButton(0)) || Gdx.input.isKeyPressed(Keys.SPACE);
        if(climbing) {
            Vector2 vel = player.getLinearVelocity();
            player.setLinearVelocity(vel.x * 0.9f, vel.y * 0.5f);
            player.setGravityScale(0);
            if(Gdx.input.isKeyPressed(Keys.W) || (game.controller != null && game.controller.getAxis(0) < -0.5f)) {
                player.applyLinearImpulse(0, 1, player.getPosition().x, player.getPosition().y, true);
            }
            System.out.println(game.controller.getAxis(0));
            if(Gdx.input.isKeyPressed(Keys.S) || (game.controller != null && game.controller.getAxis(0) > 0.5f)) {
                player.applyLinearImpulse(0, -1, player.getPosition().x, player.getPosition().y, true);
            }
        }else if(onGround > 0) {
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
            player.setGravityScale(1);
            player.getFixtureList().get(0).setFriction(0);
        }
        
        Vector2 vel = player.getLinearVelocity();
        Vector2 pos = player.getPosition();
        
        float speed = SPEED * (human ? 1 : 2f) * (climbing ? 0.5f : 1);
        
        if(onGround == 0 && !climbing) {
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
        
        if(player.getPosition().x > lightLayer.getWidth()-1)
            game.setScreen(new PlayScreen(game, levelNr+1));
    }

    @Override
    public void render(float delta) {
        if(intro > 0) {
            boolean b = intro > 1;
            intro -= delta;
            if(b && intro <= 1) {
                int gid = (Integer)level.getTileSets().getTileSet(0).getProperties().get("firstgid");
                TiledMapTileLayer layer = (TiledMapTileLayer)level.getLayers().get(0);
                layer.setCell(1, 10, new TiledMapTileLayer.Cell());
                layer.getCell(1, 10).setTile(level.getTileSets().getTile(gid + 14));
                game.assetMngr.get("explosion.wav", Sound.class).play();
                updateLight();
            }
            if(intro < 0) {
                int gid = (Integer)level.getTileSets().getTileSet(0).getProperties().get("firstgid");
                TiledMapTileLayer layer = (TiledMapTileLayer)level.getLayers().get(0);
                layer.setCell(2, 10, new TiledMapTileLayer.Cell());
                layer.getCell(2, 10).setTile(level.getTileSets().getTile(gid + 14));
                game.assetMngr.get("explosion.wav", Sound.class).play();
                updateLight();
            }
        }else {
            unprocessed += delta;
            if(unprocessed >= 1/60f) {
                unprocessed -= 1/60f;
                update(1/60f);
            }
        }
        
        animTime += delta;
        if(intro <= 0)
            monologTimer = Math.max(monologTimer-delta, 0);
        
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //cams
        float maxX = lightLayer.getWidth() - cam.viewportWidth/2;
        float minX = cam.viewportWidth/2;
        float maxY = lightLayer.getHeight() - cam.viewportHeight/2;
        float minY = cam.viewportHeight/2;
        cam.position.x = Math.max(Math.min(player.getPosition().x, maxX), minX);
        cam.position.y = Math.max(Math.min(player.getPosition().y, maxY), minY);
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
        TextureRegion p;
        switch(animState) {
            default:
            case STAND_RIGHT:
            case STAND_LEFT:
                if(onGround == 0)
                    p = human ? manJump : wolfJump;
                else
                    p = (human ? manStand : wolfStand).getKeyFrame(animTime, true);
                break;
            case WALK_RIGHT:
            case WALK_LEFT:
                if(onGround == 0)
                    p = human ? manJump : wolfJump;
                else
                    p = (human ? manWalk : wolfWalk).getKeyFrame(animTime, true);
                break;
            case CHANGE_RIGHT:
            case CHANGE_LEFT:
                p = change.getKeyFrame(animTime, false);
                break;
        }
        game.batch.draw(p, px, player.getPosition().y-0.5f, w, 1);
        game.batch.end();
        //light
        levelRenderer.setView(levelCam);
        levelRenderer.render(new int[] {2});
        //debug
        b2dr.render(world, cam.combined);
        //monologs
        if(monologTimer <= 0 && !monolog.isEmpty() && intro <= 0) {
            monolog.remove(0);
            monologTimer = 3;
        }
        if(!monolog.isEmpty() && intro <= 0) {
            game.batch.begin();
            GlyphLayout gl = new GlyphLayout(game.assetMngr.get("ascii.fnt", BitmapFont.class), monolog.get(0));
            float px1 = player.getPosition().x * 32;
            float py1 = (player.getPosition().y+1) * 32;
            game.assetMngr.get("ascii.fnt", BitmapFont.class).draw(game.batch, gl, px1-gl.width/2, py1+gl.height);
            game.batch.end();
        }
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
