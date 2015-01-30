local Item = class("Item");

function Item.create()
    local item = Item.new();
    return item;
end

function Item:ctor()
    self.name = "";
    self.x, self.y = 0, 0;
    self.sprite = nil;
    
    self.can_move=false;
    self.can_touch=false;
    self.can_hit=true;
    
    self.is_moving=false;
end

function Item:setName(name)
	self.name = name;
end

function Item:getName()
	return self.name;
end

function Item:getX()
    return self.x;
end

function Item:setX(x)
    self.x = x;
end

function Item:setY(y)
	self.y = y;
end

function Item:getY()
	return self.y;
end

function Item:setSprite(sprite)
	self.sprite = sprite;
end

function Item:getSprite()
	return self.sprite;
end

function Item:setCanMove(can_move)
	self.can_move = can_move;
end

function Item:canMove()
	return self.can_move;
end

function Item:setCanTouch(can_touch)
	self.can_touch = can_touch;
end

function Item:canTouch()
	return self.can_touch;
end

function Item:setCanHit(can_hit)
	self.can_hit = can_hit;
end

function Item:canHit()
	return self.can_hit;
end


return Item;