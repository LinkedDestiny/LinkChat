--[[
@name 游戏操作杆
@author arthur 
--]]

local HRocker = class("HRocker",function()
    return cc.Layer:create();
end)

function HRocker.create(cpoint, radius, control, background)
    local rocker = HRocker.new(cpoint,radius,control,background);
    local eventDispatcher = rocker:getEventDispatcher();
    eventDispatcher:addEventListenerWithSceneGraphPriority(rocker:bindListener(), rocker);
    return rocker;
end

function HRocker:ctor(cpoint, radius, control, background)
    self.isActive = false;
    self.radius = radius;
    self.max_radius = radius - control:getContentSize().width * control:getScale() /2;
    self.currentPoint = cpoint;
    self.centerPoint = cpoint;
    self.controlSprite = control;
    self.controlSprite:setPosition(cpoint);
    background:setPosition(cpoint);
    
    background:setTag(88);
    self:addChild(background);
    self:addChild(self.controlSprite);
    
    self.schedulerID = nil;
    self.callbackId = nil
    
    self:setCascadeOpacityEnabled(true);
    self.bindListener();
    self:active();
end

function HRocker:setCallback(callback)
    self.callback = callback;
end

function HRocker:active()
    if( not self.isActive ) then
        self.isActive = true;
        local updatePos = function()
            local now_point = cc.p(self.controlSprite:getPosition()) ;
            local point = cc.pAdd( 
                now_point, 
                cc.pMul(
                    cc.pSub(self.currentPoint, now_point) , 
                    0.5
                ) 
            );
            self.controlSprite:setPosition(point);
        end
        self.schedulerID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(updatePos , 1.0 / 60, false);
        self.callbackId = cc.Director:getInstance():getScheduler():scheduleScriptFunc(
            function()
                if( self.callback ~= nil ) then
                    if( self:needMove() ) then
                        self.callback(self:getDirection());
                    else
                        self.callback(cc.p(0,0));
                    end
                end
            end
         , 0.1 , false);
    end
end

function HRocker:inactive()
	if( self.isActive ) then
        self.isActive = false;
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self.schedulerID);
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self.callbackId);
	end
end

function HRocker:bindListener()
    local ccTouchBegan = function(pTouch, pEvent)
        if( not self.isActive ) then
            return false;
        end

        local touchPoint = pTouch.getLocation(pTouch);
        if ( cc.pGetDistance(touchPoint, self.centerPoint)  > self.radius) then
            return false;
        end
        
        self:setOpacity(255);
        self.currentPoint = touchPoint;
        return true;
    end
    
    local ccTouchMoved = function(pTouch, pEvent)
        local touchPoint = pTouch.getLocation(pTouch);
        if (cc.pGetDistance(touchPoint, self.centerPoint) > self.radius) then
            self.currentPoint =cc.pAdd( self.centerPoint, cc.pMul(cc.pNormalize(cc.pSub(touchPoint, self.centerPoint)),( self.radius - self.max_radius )));
        else
            self.currentPoint = touchPoint;
        end
    end
    

    local ccTouchEnded = function(pTouch, pEvent)
        self.currentPoint = self.centerPoint;
        self:setOpacity(255 * 0.4);
    end
    
    local listener = cc.EventListenerTouchOneByOne:create();
    listener:registerScriptHandler(ccTouchBegan ,cc.Handler.EVENT_TOUCH_BEGAN );
    listener:registerScriptHandler(ccTouchMoved ,cc.Handler.EVENT_TOUCH_MOVED );
    listener:registerScriptHandler(ccTouchEnded ,cc.Handler.EVENT_TOUCH_ENDED );
    
    return listener;
end

function HRocker:getDirection()
    return cc.pNormalize(cc.pSub(self.currentPoint,self.centerPoint));
end

function HRocker:getVelocity()
    return cc.pGetDistance(self.centerPoint,self.currentPoint);
end

function HRocker:needMove()
    return ( self:getVelocity() > (self.radius / 10 ));
end


return HRocker;
