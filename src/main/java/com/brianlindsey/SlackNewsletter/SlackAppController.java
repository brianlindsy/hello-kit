package com.brianlindsey.SlackNewsletter;

import com.slack.api.bolt.App;
import com.slack.api.bolt.servlet.SlackAppServlet;

import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = {"/slack/events", "/slack/interactive", "/slack/options-load-endpoint"})
public class SlackAppController extends SlackAppServlet {
	public SlackAppController(App app) {
		super(app);
	}
}
