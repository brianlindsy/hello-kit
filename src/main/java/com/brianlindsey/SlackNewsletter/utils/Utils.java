package com.brianlindsey.SlackNewsletter.utils;

import com.slack.api.bolt.context.Context;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsInfoResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;

import java.io.IOException;

public class Utils {

    public static String getRealUserName(UsersInfoResponse userInfo) {
        if (userInfo.getUser() != null && userInfo.getUser().getRealName() != null) {
            return userInfo.getUser().getRealName();
        }

        return "";
    }

    public static String getSlackUserName(UsersInfoResponse userInfo) {
        if (userInfo.getUser() != null && userInfo.getUser().getName() != null) {
            return userInfo.getUser().getName();
        }

        return "";
    }

    public static String getChannelName(Context ctx, String channelId) {
        try {
            ConversationsInfoResponse infoResponse = ctx.client().conversationsInfo(r -> r
                    .channel(channelId)
            );
            if (infoResponse.isOk()) {
                String channelName = infoResponse.getChannel().getName();
                return "#" + channelName;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return "a channel chosen by your manager.";
    }
}
