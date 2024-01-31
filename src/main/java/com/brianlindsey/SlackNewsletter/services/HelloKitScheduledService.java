package com.brianlindsey.SlackNewsletter.services;

import com.brianlindsey.SlackNewsletter.models.HelloKit;
import com.brianlindsey.SlackNewsletter.models.HelloKitScheduled;
import com.brianlindsey.SlackNewsletter.models.Question;
import com.brianlindsey.SlackNewsletter.repositories.HelloKitRepository;
import com.brianlindsey.SlackNewsletter.repositories.HelloKitScheduledRepository;
import com.brianlindsey.SlackNewsletter.utils.Utils;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatScheduleMessageResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.*;
import static com.slack.api.model.view.Views.*;

@Service
public class HelloKitScheduledService {

	@Autowired HelloKitScheduledRepository helloKitSchedRepo;
	@Autowired HelloKitRepository helloKitRepo;
	
	public void save(HelloKitScheduled helloKitSched) {
		helloKitSchedRepo.save(helloKitSched);
	}

	public HelloKitScheduled createNewHelloKitScheduled(ZonedDateTime postAtDate, String username, String selectedKitId, String greeting, String teamId) throws NumberFormatException, Exception {
		HelloKitScheduled helloKitScheduled = new HelloKitScheduled();
		ZonedDateTime postAtInstant = findToPostInstant(postAtDate);
		helloKitScheduled.setSendAt(postAtInstant.toInstant());
		helloKitScheduled.setUserId(username);
		helloKitScheduled.setGreeting(greeting);
		helloKitScheduled.setCreatedAt(Instant.now());
		Optional<HelloKit> found = helloKitRepo.findById(Long.parseLong(selectedKitId));
		if(found.isPresent()) {
			HelloKit helloKit = found.get();
			helloKit.setTeamId(teamId);
			helloKitScheduled.setHelloKit(helloKit);
		} else {
			throw new Exception("Could not find hello kit with id " + Long.parseLong(selectedKitId));
		}
		helloKitScheduled.setStatus("PENDING");

		HelloKitScheduled saved = helloKitSchedRepo.save(helloKitScheduled);

		return saved;
	}
	
	public List<HelloKitScheduled> getPendingNotSent(String teamId) {
		List<HelloKitScheduled> sched = helloKitSchedRepo.findBySendAtGreaterThanAndStatusAndHelloKit_TeamId(Instant.now(), "PENDING", teamId);
		
		return sched;
	}
	
	public List<LayoutBlock> createHelloMessage(ViewSubmissionContext ctx, String userId) {
		List<LayoutBlock> message = new ArrayList<LayoutBlock>();
		try {
			UsersInfoResponse userInfo = ctx.client().usersInfo(i -> i.user(userId));
			message.add(section(section -> section.text(markdownText(mt -> mt.text(createHelloKitMessageGreetingText(Utils.getRealUserName(userInfo)))))));
		} catch (IOException | SlackApiException e) {
			e.printStackTrace();
		}
		message.add(actions(actions -> actions
			      .elements(asElements(
			    	        button(b -> b.text(plainText(pt -> pt.emoji(true).text("Questionnaire!"))).actionId("open_hello_kit_questionnaire"))
			    	      ))
			      ));

		return message;
	}

	public View createHelloMessageModal(ActionContext ctx, HelloKitScheduled helloKitSched) {
		List<LayoutBlock> message = new ArrayList<LayoutBlock>();
		message.add(section(section -> section.text(markdownText(mt -> mt.text(createTeamMessageGreetingText(helloKitSched.getGreeting()))))));
		message.add(divider());
		String channelName = Utils.getChannelName(ctx, helloKitSched.getHelloKit().getChannelId());
		message.add(section(section -> section.text(markdownText(mt -> mt.text("When submitted, this will be posted to " + channelName)))));
		message.add(divider());
		List<Question> questionsToAsk = helloKitSched.getHelloKit().getQuestions();
		for(Question q : questionsToAsk) {
			message.add(input(input -> input.label(plainText(pt -> pt.text(q.getPrompt().getPromptText()))).optional(true)
					.element(plainTextInput(pti -> pti.placeholder(plainText(pt -> pt.text("Answer here!")))))));
		}
		View appHomeView = view(v -> v
				.type("modal").callbackId("submit_hello_kit_message")
				.title(viewTitle(vt -> vt.type("plain_text").text("Questionnaire")))
				.close(viewClose(vc -> vc.type("plain_text").text("Close")))
				.submit(viewSubmit(vc -> vc.type("plain_text").text("Submit!")))
				.blocks(message)
				);

		return appHomeView;

	}

	public void createScheduledMessage(ViewSubmissionContext ctx, HelloKitScheduled helloKitSched) {
		try {
			ChatScheduleMessageResponse response = ctx.client().chatScheduleMessage(r -> r
					.token(ctx.getBotToken())
					// pass username as channel ID to open direct 1:1 with user
					.channel(helloKitSched.getUserId())
					.blocks(createHelloMessage(ctx, helloKitSched.getUserId()))
					.text(createTeamMessageGreetingText(helloKitSched.getGreeting()))
					// Time to post message, in Unix Epoch timestamp format
					.postAt((int) helloKitSched.getSendAt().getEpochSecond())
					);
			helloKitSched.setScheduledMessageId(response.getScheduledMessageId());
			helloKitSchedRepo.save(helloKitSched);
		} catch (IOException | SlackApiException e) {
			System.err.print("error: " +  e.getMessage());
		}
	}
	
	//TODO: update for when start date is the next year
	private ZonedDateTime findToPostInstant(ZonedDateTime postAtDate) {
		if(postAtDate.getDayOfYear() == LocalDateTime.now().getDayOfYear()
				&& postAtDate.getYear() == LocalDateTime.now().getYear()) {
			return ZonedDateTime.now().plusSeconds(30);
		}
		
		return postAtDate.withZoneSameInstant(ZoneId.of("America/New_York"));
	}
	
	private String createHelloKitMessageGreetingText(String realName) {
		if (realName != null && realName != "") {
			return "*Hi, " + realName + "!* Your team wants to get to know you a bit more. Fill out " +
					"these questions and hit submit :point_down:. Your responses will be posted " +
					"to a channel where your teammates can learn more about you! :smile:";
		}
		return "*Hi! Your team wants to get to know you a bit more. Fill out " +
				"these questions and hit submit :point_down:. Your responses will be posted " +
				"to a channel where your teammates can learn more about you! :smile:";
	}
	
	private String createTeamMessageGreetingText(String teamGreeting) {
		if(teamGreeting == null || teamGreeting.equals("")) {
			return "Welcome to the team!";
		}
		
		return teamGreeting;
	}
	
	public HelloKitScheduled getHelloKitSchedByUserId(String id) {
		return helloKitSchedRepo.findByUserIdAndStatus(id, "PENDING").get();
	}

}
