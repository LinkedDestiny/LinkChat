
local Constants = {};

Constants.CreatEnumTable = function(tbl, index) 
    local enumtbl = {} 
    local enumindex = index or 0 
    for i, v in ipairs(tbl) do 
        enumtbl[v] = enumindex + i 
    end 
    return enumtbl 
end 

local Direction = 
{ 
    'UP',
    'DOWN',
    'LEFT',
    'RIGHT',
    'CENTER'
};

Constants.Direction = Constants.CreatEnumTable(Direction);

Constants.getIntPart = function (x)
    if x <= 0 then
        return math.ceil(x);
    end

    if math.ceil(x) == x then
        x = math.ceil(x);
    else
        x = math.ceil(x) - 1;
    end
    return x;
end
return Constants;