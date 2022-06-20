package com.brianlindsey.SlackNewsletter;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slack.api.bolt.response.Response;

@RestController
public class CompleteCancelController {
	
	@RequestMapping("/slack/oauth/completion")
	public String complete() {
		return "Thanks for installing Hello Kit!";
	}
	
	@RequestMapping("/slack/oauth/cancellation")
	public String cancel() {
		return "Hello Kit installation has been cancelled.";
	}

}
