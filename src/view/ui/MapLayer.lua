--[[
@name 游戏地图层
@author arthur 
--]]
local cclog=require("util/Log");
local Constants=require("util/Constants");

local MapLayer = class("MapLayer",function()
    return cc.Layer:create();
end)

function MapLayer.create(filename)
	local map_layer = MapLayer.new(filename);
	
	return map_layer;
end

function MapLayer:ctor(filename)
    self.tile_map = cc.TMXTiledMap:create(filename);
    self.item_layer = self.tile_map:getLayer("background");
    self.meta_layer = self.tile_map:getLayer("Meta");
    self.objectGroup = cc.TMXTiledMap.getObjectGroup(self.tile_map,"object");
    self.meta_layer:setVisible(false);
    self.tile_map:setPosition(0,32);
    self.tile_map:setAnchorPoint(cc.p(0,0));
    --self:addChild(self.meta_layer);
end

function MapLayer:canMove(boundingbox, point)
    point = self:tilePosition(point);
    local gid = self.meta_layer:getTileGIDAt(point); 
    if( gid == 0 ) then
        return true;
    end
    local properties = self.tile_map:getPropertiesForGID(gid);

    if( not properties ) then
        return true;
    end
    if( properties.Collidable == "true" ) then
        local tile_boundingbox = self.meta_layer:getTileAt(point):getBoundingBox();
        local test = cc.rectIntersectsRect(boundingbox,tile_boundingbox);
        return not test;
    end 
    return true;
end

function MapLayer:getMetaLayer()
    return self.meta_layer;
end

function MapLayer:getTileMap()
    return self.tile_map;
end

function MapLayer:getObject(name)
    return self.objectGroup:getObject(name);
end

function MapLayer:tilePosition(point)
    local x = Constants.getIntPart( point.x / self.tile_map:getTileSize().width );
    local y = Constants.getIntPart( ( (self.tile_map:getMapSize().height * self.tile_map:getTileSize().height) - point.y) / self.tile_map:getTileSize().height );
    return cc.p(x,y);
end

return MapLayer;