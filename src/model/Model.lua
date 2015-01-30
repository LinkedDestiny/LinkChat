local Model = class("Model");

function Model.create(model)
    local model_name = require("model/" .. model .. "Model");
    
    return model_name.new(model);
end

function Model:ctor(model)
    self.model = model;
    
end

function Model:addItem(...)

end

function Model:removeItem(...)

end

return Model;