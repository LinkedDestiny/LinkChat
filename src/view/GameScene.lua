
-- cclog
local cclog = require("Log");

local GameScene = class("GameScene",function()
    return cc.Scene:create()
end)

function GameScene.create()
    local scene = GameScene.new()
    scene:addChild(scene:createLayerFarm())
    return scene
end


function GameScene:ctor()
    self.visibleSize = cc.Director:getInstance():getVisibleSize();
    self.origin = cc.Director:getInstance():getVisibleOrigin();
    self.schedulerID = nil;
    self.map = nil;
end

function GameScene:playBgMusic()
    local bgMusicPath = cc.FileUtils:getInstance():fullPathForFilename("background.mp3") 
    cc.SimpleAudioEngine:getInstance():playMusic(bgMusicPath, true)
    local effectPath = cc.FileUtils:getInstance():fullPathForFilename("effect1.wav")
    cc.SimpleAudioEngine:getInstance():preloadEffect(effectPath)
end

function GameScene:creatDog()
    local frameWidth = 105
    local frameHeight = 95

    -- create dog animate
    local textureDog = cc.Director:getInstance():getTextureCache():addImage("dog.png")
    local rect = cc.rect(0, 0, frameWidth, frameHeight)
    local frame0 = cc.SpriteFrame:createWithTexture(textureDog, rect)
    rect = cc.rect(frameWidth, 0, frameWidth, frameHeight)
    local frame1 = cc.SpriteFrame:createWithTexture(textureDog, rect)

    local spriteDog = cc.Sprite:createWithSpriteFrame(frame0)
    spriteDog:setPosition(self.origin.x, self.origin.y + self.visibleSize.height / 4 * 3)
    spriteDog.isPaused = false

    local animation = cc.Animation:createWithSpriteFrames({frame0,frame1}, 0.5)
    local animate = cc.Animate:create(animation);
    spriteDog:runAction(cc.RepeatForever:create(animate))

--    -- moving dog at every frame
--    local function tick()
--        if spriteDog.isPaused then return end
--        local x, y = spriteDog:getPosition()
--        if x > self.origin.x + self.visibleSize.width then
--            x = self.origin.x
--        else
--            x = x + 1
--        end
--
--        spriteDog:setPositionX(x)
--    end
--
--    self.schedulerID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(tick, 0, false)

    return spriteDog
end

-- create farm
function GameScene:createLayerFarm()
    local layerFarm = cc.Layer:create()
    local onRelease = function(code, event)
        if code == cc.KeyCode.KEY_BACK then
            local luaj = require("src/cocos/cocos2d/luaj");
            luaj.callStaticMethod("com/link/game/SharingPlatform","RELEASE",{});
            cc.Director:getInstance():endToLua();
        elseif code == cc.KeyCode.KEY_HOME then
            cc.Director:getInstance():endToLua();
        end
    end
    local listener = cc.EventListenerKeyboard:create();
    listener:registerScriptHandler(onRelease, cc.Handler.EVENT_KEYBOARD_RELEASED);
    local eventDispatcher = layerFarm:getEventDispatcher();
    eventDispatcher:addEventListenerWithSceneGraphPriority(listener, layerFarm);
    
    -- add in farm background
    local MapLayer = require("ui/MapLayer");
    local map_layer = MapLayer.create("background.tmx");
    layerFarm:addChild(map_layer);
   
    local spawnPoint = map_layer:getObject("spawnpoint");
    
    local x = spawnPoint['x'];
    local y = spawnPoint['y'];

    -- add moving dog
--    local spriteDog = self:creatDog()
--    spriteDog:setPosition(x, y);
--    layerFarm:addChild(spriteDog)
    
    local Network = require("src/Network");
    local network = Network.create();
    
    local HeroClass = require("character/Hero");
    local hero = HeroClass.new("hero.png", 1);
    hero:setPosition(x, y);
    hero:setHandler(map_layer);
    hero:setNetwork(network)
    layerFarm:addChild(hero);
    
    local enemy = HeroClass.new("hero.png" , 2);
    enemy:setPosition(x + 100, y);
    enemy:setHandler(map_layer);
    layerFarm:addChild(enemy);
    network:setHero(enemy);
    
    local winSize = cc.Director:getInstance():getWinSize();
    x = math.max(x, winSize.width / 2);
    y = math.max(y, winSize.height / 2);
    x = math.min(x , (map_layer:getTileMap():getMapSize().width * map_layer:getTileMap():getTileSize().width) - winSize.width/2);
    y = math.min(y , (map_layer:getTileMap():getMapSize().height * map_layer:getTileMap():getTileSize().height) - winSize.height/2);

    local actual = cc.p(x,y);
    local center = cc.p(winSize.width/2, winSize.height/2);
    layerFarm:setPosition(cc.pSub(actual,center));
    
    local CPanel = require("ui/CPanel");
    local control = { cc.Sprite:create("cpanel.png") };

    local panel = CPanel.create(cc.p(180, 180) ,43, control);
    panel:setDelegate(hero);
    
    layerFarm:addChild(panel);
    
    local luaj = require("src/cocos/cocos2d/luaj");
    luaj.callStaticMethod("com/link/game/SharingPlatform","INIT",{ 1 } ,"(I)V" );
    return layerFarm
end

return GameScene
