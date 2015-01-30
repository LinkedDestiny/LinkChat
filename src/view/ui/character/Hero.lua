--[[
@name 英雄精灵
@author arthur 
--]]

local cclog = require("Log");
local Item = require("character/Item");
local Constants = require("util/Constants");

local Hero = class("Hero",
    function(filename)
        return Item.new(filename);
    end
);

function Hero:ctor(filename, role)
    self.role = role;
    self.can_move = true;
    self.direction = cc.p(0,0);
    self.speed = 5.0;
    
    local move = function()
        local px;
        local py;
        if( self.is_moving ) then
            px , py = self:getPosition();
            local origin_x = px;
            local origin_y = py;
            px = px + self.direction.x * self.speed;
            py = py + self.direction.y * self.speed;
            self:setPosition(cc.p(px,py));
            if( not self.handler:canMove(self:getBoundingBox(), cc.p(px,py)) ) then
                self:setPosition(cc.p(origin_x,origin_y));
                return;
            end
            if( role == 1 ) then
                local json = require "json";
                self.network:sendMsg(json.encode({ x=self.direction.x ,y=self.direction.y }));
            end
        end
    end
    self.schedulerID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(move, 1.0 / 60, false);
end

function Hero:setNetwork(network)
    self.network = network;
end

function Hero:release()
    cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self.schedulerID);
end

function Hero:doTouchBegan()
    self.is_moving = true;
end

function Hero:doTouchEnd()
    self.is_moving = false;
end

function Hero:doChangeDirectionTo(direction)
    self.direction = direction;
end

return Hero;
