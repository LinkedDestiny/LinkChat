local ViewController = class("ViewController");

local view_controller = ViewController.new();

function ViewController:ctor()
    
end

function ViewController:replaceScene(scene_name)
    local name = "view/" .. scene_name;
    local Scene = require(name);
    local scene = Scene:create();
    cc.Director:getInstance():replaceScene(scene);
end

return view_controller;