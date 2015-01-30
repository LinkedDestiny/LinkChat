local Network = class("Network");

local cclog = require("src/Log");

function Network.create()
    local network = Network.new();
    
    return network;
end

function Network:ctor()
    
    self.luaj = require("src/cocos/cocos2d/luaj");
    self.callback_table = {};
end

function Network:sendMsg(msg)
    self.luaj.callStaticMethod("com/link/game/SharingPlatform","SendMsg",{ msg });
end

function Network:registeCallback(name, callback)
    if( not self.callback_table[name] ) then
        self.callback_table[name] = true;
        self.luaj.callStaticMethod("com/link/game/SharingPlatform","RegisteCallback",{ name , callback });
    end
end

return Network.create();