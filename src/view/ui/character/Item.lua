--[[
@name 所有精灵的基类
@author arthur 
--]]

local cclog = require("Log");

local Item = class("Item",
    function(filename)
        return cc.Sprite:create(filename);
    end
);

function Item:ctor(filename)
    
    self.can_move=false;
    self.can_touch=false;
    self.can_hit=true;
    
    self.is_moving=false;
    
    self.position=nil;
    self.handler = nil;
end

function Item:setHandler(handler)
    self.handler = handler;
end

function Item:canMove()
    return self.can_move;
end

function Item:canTouch()
    return self.can_touch;
end

function Item:canHit()
    return self.can_hit;
end

function Item:isMoving()
    return self.is_moving;
end


return Item;


