local Item = require("model/item/Item");

local Bullet = class("Bullet", function()
    return Item.new();
end);

Bullet.TYPE = {
    
};

function Bullet.create(type)
    local bullet = Bullet.new(type);
    
    return bullet();
end

function Bullet:ctor(type)
    self.type = type;
    
    self.can_move=true;
    self.can_touch=false;
    self.can_hit=true;
end

return Bullet;