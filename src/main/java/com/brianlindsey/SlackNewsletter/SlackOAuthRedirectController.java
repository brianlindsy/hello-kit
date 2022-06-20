package com.brianlindsey.SlackNewsletter;

import javax.servlet.annotation.WebServlet;

import com.slack.api.bolt.App;
import com.slack.api.bolt.servlet.SlackOAuthAppServlet;

@WebServlet("/slack/oauth/callback")
public class SlackOAuthRedirectController extends SlackOAuthAppServlet {
  public SlackOAuthRedirectController(App app) { super(app); }
}
