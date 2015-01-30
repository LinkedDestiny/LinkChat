local Message = class("Message", function()

end);

function Message:ctor(message_type, object)
    self.message_type = message_type and "";
    self.object = object and nil;
end

function Message:setMsgType(message_type)
    self.message_type = message_type;
end

function Message:getMsgType()
    return self.message_type;
end

function Message:setObject(object)
    self.object = object;
end

function Message:getObject(object)
	return self.object;
end

return Message;