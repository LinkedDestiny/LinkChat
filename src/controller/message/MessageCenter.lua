
local message_center;

local MessageCenter = class("MessageCenter", function()

end);

function MessageCenter:ctor()
    self.listener_list = {};
    self.listener_set = {};
end

function MessageCenter:registeListener(listener, message_type)
    local sub_listener_list = self.listener_list[message_type];
    local sub_listener_set = self.listener_set[message_type];
    if( not sub_listener_set[listener] ) then
        table.insert(sub_listener_list, listener);
        sub_listener_set[listener]=table.getn(sub_listener_list);
    end
end

function MessageCenter:removeListener(listener)
	for key , sub_listener_list in self.listener_list do
	   for skey, _listener in sub_listener_list do
	       if( _listener == listener ) then
                table.remove(sub_listener_list, skey);
                break;
	       end
	   end
	end
    for key , sub_listener_set in self.listener_set do
        sub_listener_set[listener]=nil;
    end
end

function MessageCenter:sendMessage(msg)
    local message_type = msg:getMsgType();
    local sub_listener_list = self.listener_list[message_type];
    for key , listener in sub_listener_list do
        listener:handleMessage(msg);
    end
end



message_center = MessageCenter.new();
return message_center;