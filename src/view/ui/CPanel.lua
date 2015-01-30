--[[
@name 游戏操作按键
@author arthur 
--]]

local cclog=require("util/Log");
local MessageTable = require("controller/message/MessageTable");
local MessageCenter = require("controller/message/MessageCenter");
local Message = require("controller/message/Message");

local CPanel = class("CPanel",function()
    return cc.Layer:create();
end)

local MIN_CTRL_AREA = 3;


function CPanel.create(cpoint, radius, control)
    local panel = CPanel.new(cpoint,radius,control);
    local eventDispatcher = panel:getEventDispatcher();
    eventDispatcher:addEventListenerWithSceneGraphPriority(panel:bindListener(), panel);
    return panel;
end

function CPanel:ctor(cpoint, radius, control)
    self.isActive = false;
    self.radius = radius;
    
    self.center = cpoint;
    self.controlSprite = control;
    for i, v in ipairs(self.controlSprite) do
        v:setPosition(cpoint);
        v:setCascadeOpacityEnabled(true);
    end
    self:setCascadeOpacityEnabled(true);
    self:setOpacity(255 * 0.4);
    self:addChild(self.controlSprite[1]);
    self.schedulerID = nil;
    self.callbackId = nil

    self:setCascadeOpacityEnabled(true);
    self.bindListener();
    self:active();
end

function CPanel:setDelegate(delegate)
    self.delegate = delegate;
end

function CPanel:bindListener()
    local ccTouchBegan = function(pTouch, pEvent)
        if( not self.isActive ) then
            return false;
        end

        local touchPoint = pTouch:getLocation();
        local d = cc.pGetDistance(touchPoint, self.center);
        if ( d <= self.radius ) then
            self:setOpacity(255);
            self.isHeld = true;
            
            local message = Message.new(MessageTable.MSG_PANEL_ACTION , {action="begin"});
            MessageCenter:sendMessage(message);
            
            if( d >= self.radius / MIN_CTRL_AREA ) then
                self:updateDirection(touchPoint);
            end
        end
        return true;
    end

    local ccTouchMoved = function(pTouch, pEvent)
        local touchPoint = pTouch:getLocation();
        local d = cc.pGetDistance(touchPoint, self.center);
        if ( d <= self.radius ) then
            if( not self.isHeld ) then
                self:setOpacity(255);
                self.isHeld = true;
                local message = Message.new(MessageTable.MSG_PANEL_ACTION , {action="begin"});
                MessageCenter:sendMessage(message);
            end
            if( d >= self.radius / MIN_CTRL_AREA ) then
                self:updateDirection(touchPoint);
            end
        elseif( self.isHeld ) then
            self:setOpacity(255 * 0.4);
            self.isHeld = false;
            local message = Message.new(MessageTable.MSG_PANEL_ACTION , {action="end"});
            MessageCenter:sendMessage(message);
        end
    end


    local ccTouchEnded = function(pTouch, pEvent)
        self:setOpacity(255 * 0.4);
        self.isHeld = false;
        local message = Message.new(MessageTable.MSG_PANEL_ACTION , {action="end"});
        MessageCenter:sendMessage(message);
    end

    local listener = cc.EventListenerTouchOneByOne:create();
    listener:registerScriptHandler(ccTouchBegan ,cc.Handler.EVENT_TOUCH_BEGAN );
    listener:registerScriptHandler(ccTouchMoved ,cc.Handler.EVENT_TOUCH_MOVED );
    listener:registerScriptHandler(ccTouchEnded ,cc.Handler.EVENT_TOUCH_ENDED );

    return listener;
end

function CPanel:updateDirection(location)
    local pt = cc.pSub(location, self.center);
    local degrees = cc.pToAngleSelf(pt) * 57.29577951;
    local new_direction;
    if (degrees <= 22.5 and degrees >= -22.5) then
        --right
        new_direction = cc.p(1.0, 0.0);
        
    elseif (degrees >= 67.5 and degrees <= 112.5) then
        --top
        new_direction = cc.p(0.0, 1.0);
    elseif (degrees >= 157.5 or degrees <= -157.5) then
        --left
        new_direction = cc.p(-1.0, 0.0);
    elseif (degrees <= -67.5 and degrees >= -112.5) then
        --bottom
        new_direction = cc.p(0.0, -1.0);
    else
        new_direction = cc.p(0.0, 0.0);
    end
    if( new_direction ~= self.direction ) then
        local object = {
            action = "move",
            direction = self.direction;
        };
        local message = Message.new(MessageTable.MSG_PANEL_ACTION , object );
        MessageCenter:sendMessage(message);
    end
end

function CPanel:active()
    if( not self.isActive ) then
        self.isActive = true;
    end
end

function CPanel:inActive()
    if( self.isActive ) then
        self.isActive = false;
    end
end

return CPanel;