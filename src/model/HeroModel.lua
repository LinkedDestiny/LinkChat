local Model = require("model/Model");
local MessageTable = require("controller/message/MessageTable");
local MessageCenter = require("controller/message/MessageCenter");
local Hero = require("model/item/Hero");

local HeroModel = class("HeroModel", function()
    return Model.new("Hero");
end);

function HeroModel:ctor() 

end

function HeroModel:addItem(key, role)
    self.item_table[key] = Hero.create( role);
end

function HeroModel:removeItem(key)
    self.item_table[key] = nil;
end

function HeroModel:getItem(key)

    return self.item_table[key];
end



return Model.create("Hero");