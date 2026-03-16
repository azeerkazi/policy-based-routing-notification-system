package com.notification.channel;

import com.notification.enums.ChannelType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ChannelRegistry {
    private final Map<ChannelType, Channel> channelMap = new EnumMap<>(ChannelType.class);
    
    @Autowired
    private List<Channel> channels;

    @PostConstruct
    public void init() {
        channels.forEach(channel -> channelMap.put(channel.getChannelType(), channel));
    }

    public Channel getChannel(ChannelType type) {
        Channel channel = channelMap.get(type);
        if (channel == null) {
            throw new IllegalArgumentException("No channel found for type: " + type);
        }
        return channel;
    }

    public boolean containsChannel(ChannelType type) {
        return channelMap.containsKey(type);
    }
}