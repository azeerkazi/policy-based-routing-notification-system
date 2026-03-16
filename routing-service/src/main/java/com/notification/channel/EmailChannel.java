package com.notification.channel;

import org.springframework.stereotype.Component;

import com.notification.enums.ChannelType;

@Component
public class EmailChannel implements Channel {

    @Override
    public ChannelType getChannelType() {
        return ChannelType.EMAIL;
    }
}
