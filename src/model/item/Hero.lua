local Item = require("model/item/Item");

local Hero = class("Hero", function()
    return Item.new();
end);

Hero.ROLE = {
    HERO=1,
    ENEMY=2
}

function Hero.create(role)
    local hero = Hero.new(role);
    return hero;
end

function Hero:ctor(role)
    self.role=role;
    
    self.can_move=true;
    self.can_touch=false;
    self.can_hit=true;
    
    self.direction = nil;
end

function Hero:getRole()
    return self.role;
end

function Hero:setDirection(direction)
	self.direction = direction;
end

function Hero:getDirection()
	return self.direction;
end

return Hero;