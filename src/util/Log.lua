
local isLog = true;
--local luaj = require("src/cocos/cocos2d/luaj");

local cclog = function(...)
    if( isLog ) then
        --luaj.callStaticMethod("com/link/game/SharingPlatform","Log",{ (string.format(...)) } );
        print((string.format(...)));
    end
end

return cclog;