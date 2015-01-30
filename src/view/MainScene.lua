
-- cclog
local cclog = require("util/Log");
local ViewController = require("controller/ViewController");

local MainScene = class("MainScene",function()
    return cc.Scene:create()
end)

function MainScene.create()
    local scene = MainScene.new()
    scene:addChild(scene:createLayerFarm())
    return scene
end


function MainScene:ctor()
    self.visibleSize = cc.Director:getInstance():getVisibleSize();
    self.origin = cc.Director:getInstance():getVisibleOrigin();
    self.schedulerID = nil;
    self.map = nil;
end

-- create farm
function MainScene:createLayerFarm()
    local layerFarm = cc.Layer:create()

    local onRelease = function(code, event)
        if code == cc.KeyCode.KEY_BACK then
            cc.Director:getInstance():endToLua();
        elseif code == cc.KeyCode.KEY_HOME then
            cc.Director:getInstance():endToLua();
        end
    end
    local listener = cc.EventListenerKeyboard:create();
    listener:registerScriptHandler(onRelease, cc.Handler.EVENT_KEYBOARD_RELEASED);
    local eventDispatcher = layerFarm:getEventDispatcher();
    eventDispatcher:addEventListenerWithSceneGraphPriority(listener, layerFarm);
    
    local background = cc.Sprite:create("main_scene_background.png");
    background:setAnchorPoint(cc.p(0,0));
    background:setPosition(0,0);
    background:setScale(2, 2);
    layerFarm:addChild(background);
    
    
    local normal = cc.SpriteFrame:create("start_game.png" , cc.rect(0,0,214,88));
    local press = cc.SpriteFrame:create("start_game_normal.png" , cc.rect(0,0,214,88));
    local startGame = cc.MenuItemImage:create();
    startGame:setNormalSpriteFrame(normal);
    startGame:setSelectedSpriteFrame(press);
    startGame:activate();
    local function onQuit(sender)
        cclog("onQuit item is clicked.");
        ViewController:replaceScene("BattleScene");
    end
    startGame:registerScriptTapHandler(onQuit);

    local menu = cc.Menu:create();
    --menu:setAnchorPoint(cc.p(0,0));
    menu:setPosition(450,200);
    menu:addChild(startGame);
    layerFarm:addChild(menu);
    
    return layerFarm;
end

return MainScene
