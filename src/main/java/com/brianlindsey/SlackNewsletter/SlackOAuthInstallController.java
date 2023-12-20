package com.brianlindsey.SlackNewsletter;

import javax.servlet.annotation.WebServlet;

import com.slack.api.bolt.App;
import com.slack.api.bolt.servlet.SlackOAuthAppServlet;

@WebServlet("/slack/install")
public class SlackOAuthInstallController extends SlackOAuthAppServlet {
  public SlackOAuthInstallController(App app) { super(app); }
}
