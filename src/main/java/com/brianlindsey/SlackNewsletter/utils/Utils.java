package com.brianlindsey.SlackNewsletter.utils;

import com.slack.api.methods.response.users.UsersInfoResponse;

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
}
