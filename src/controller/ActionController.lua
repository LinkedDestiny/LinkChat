
local MessageTable = require("controller/message/MessageTable");
local MessageCenter = require("controller/message/MessageCenter");
local Message = require("controller/message/Message");
local Network = require("src/controller/Network");

local ActionController = class("ActionController", function()

end);

local action_controller = ActionController.new();

function ActionController:ctor()
    self.data_list = {};
    MessageCenter:registeListener(self, MessageTable.MSG_PANEL_ACTION);
    
    local onReceive = function(msg)
        -- TODO 解析数据，填充进队列
        local json = require "json";
        local msg_table = json.decode(json);
        table.insert(self.data_list, msg_table);
    end
    Network:registeCallback("ActionReceive",onReceive);
    
end

function ActionController:init()
    local handleThread = function()
        for key, data in self.data_list do
            
        end
    end
    self.schedulerID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(handleThread, 0.1, false);
end

function ActionController:setModel(model, key)
    self.model = model;
    self.key = key;
end

function ActionController:handleMessage(msg)
-- TODO record hero action
    local msg_type = msg:getMsgType();
    local object = msg:getObject();
    if( msg_type == MessageTable.MSG_PANEL_ACTION ) then
        local item = self.model:getItem(self.key);
        if( object['action'] == "begin" ) then
            item:doTouchBegan();
        elseif( object['action'] == "end" ) then
            item:doTouchEnd();
        elseif( object['action'] == "move" ) then
            
            local message = Message.new(MessageTable.MSG_HERO_ACTION , "changeDirection" );
            MessageCenter:sendMessage(message);
        end
    end
end

return action_controller;