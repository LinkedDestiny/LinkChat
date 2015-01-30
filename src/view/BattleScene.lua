
-- cclog
local cclog = require("util/Log");

local BattleScene = class("BattleScene",function()
    return cc.Scene:create()
end)

function BattleScene.create()
    local scene = BattleScene.new()
    scene:addChild(scene:createLayerFarm())
    return scene
end


function BattleScene:ctor()
    self.visibleSize = cc.Director:getInstance():getVisibleSize();
    self.origin = cc.Director:getInstance():getVisibleOrigin();
    self.schedulerID = nil;
    self.map = nil;
end

-- create farm
function BattleScene:createLayerFarm()
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

    local MapLayer = require("view/ui/MapLayer");
    local map_layer = MapLayer.create("background.tmx");

    layerFarm:addChild(map_layer:getTileMap());

    return layerFarm;
end

return BattleScene
