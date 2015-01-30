
local MessageTable = require("controller/message/MessageTable");
local MessageCenter = require("controller/message/MessageCenter");

local DataController = class("DataController", function()

end);

local data_controller = DataController.new();

function DataController:ctor()
    self.data_list = {};
    
    MessageCenter:registeListener(self, MessageTable.MSG_HERO_ACTION);
end

function DataController:release()
    MessageCenter:removeListener(self);
    self:stopCollecting();
end

function DataController:startCollecting()
    local collection_function = function()
      
        local item = self.model:getItem(self.key);
        local data_table = {};
        data_table['name'] = item.getName();
        data_table['action']="move";
        data_table['x'] = item.getX();
        data_table['y'] = item.getY();
        data_table['direction'] = item.getDirection();
        
        table.insert(self.data_list, data_table);
    end 
    
    self.schedulerID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(collection_function, 0.04, false);
    
    local send_function = function()
        local json = require "json";
        local network = require("controller/Network");
        network:sendMsg(json.encode(self.data_list));
        self.data_list = {};
    end
    
    self.sendID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(collection_function, 0.1, false);
end

function DataController:stopCollecting()
    cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self.schedulerID);
end

function DataController:handleMessage(msg)
    -- TODO record hero action
    local msg_type = msg:getMsgType();
    local object = msg:getObject();
    if( msg_type == MessageTable.MSG_PANEL_ACTION ) then
        local item = self.model:getItem(self.key);
        local data_table = {};
        if( object == "useSkill" ) then
            data_table['action']="skill";
        elseif( object == "changeDirection" ) then
            data_table['action']="move";
        end
        data_table['name'] = item.getName();
        data_table['x'] = item.getX();
        data_table['y'] = item.getY();
        data_table['direction'] = item.getDirection();
        table.insert(self.data_list, data_table);
    end
end

function DataController:registeModel(model, key)
    self.model = model;
    self.key = key;
end

return data_controller;