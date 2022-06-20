package com.brianlindsey.SlackNewsletter;

import com.brianlindsey.SlackNewsletter.models.HelloKit;

import com.brianlindsey.SlackNewsletter.models.HelloKitScheduled;
import com.brianlindsey.SlackNewsletter.models.Prompt;
import com.brianlindsey.SlackNewsletter.models.Question;
import com.brianlindsey.SlackNewsletter.services.GiphyService;
import com.brianlindsey.SlackNewsletter.services.HelloKitScheduledService;
import com.brianlindsey.SlackNewsletter.services.HelloKitService;
import com.brianlindsey.SlackNewsletter.services.PromptService;
import com.brianlindsey.SlackNewsletter.services.QuestionService;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.Context;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.model.Bot;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.service.InstallationService;
import com.slack.api.bolt.service.OAuthStateService;
import com.slack.api.bolt.service.builtin.AmazonS3InstallationService;
import com.slack.api.bolt.service.builtin.AmazonS3OAuthStateService;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatScheduleMessageResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.methods.response.views.ViewsOpenResponse;
import com.slack.api.methods.response.views.ViewsPushResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.event.AppHomeOpenedEvent;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState.SelectedOption;
import com.slack.api.model.view.ViewState.Value;

import java.util.List;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class SlackApp {

	@Autowired HelloKitService helloKitService;
	@Autowired HelloKitScheduledService helloKitSchedService;
	@Autowired PromptService promptService;
	@Autowired QuestionService questionService;
	@Autowired @Lazy InstallationService installService;
	
	public String getBotAccessToken(String teamId) {
		Bot bot = installService.findBot(null, teamId);
		
		return bot.getBotAccessToken();
	}
	
	public String getTeamId(Context ctx) {
		String teamId = ctx.getTeamId();
		
		return teamId;
	}
	
	public boolean canAddQuestion(ActionContext ctx) {
		String teamId = getTeamId(ctx);
		List<Prompt> customQuestions = promptService.getCustomPrompts(teamId);
		
		return customQuestions.size() <= 10;
	}
	
	public View createHelloKitModalWithQuestions(ActionContext ctx) {
		List<Prompt> personal = promptService.getPersonalInfoPrompts();
		List<Prompt> workStyle = promptService.getWorkStylePrompts();
		List<Prompt> fun = promptService.getFunPrompts();

		String teamId = ctx.getTeamId();
		List<Prompt> custom = promptService.getCustomPrompts(teamId);

		View modalView = AppHome.buildCreateKitModal(custom, personal, workStyle, fun);

		return modalView;
	}
	
	public View createHelloKitModalWithQuestions(ViewSubmissionContext ctx, String viewId) {
		List<Prompt> personal = promptService.getPersonalInfoPrompts();
		List<Prompt> workStyle = promptService.getWorkStylePrompts();
		List<Prompt> fun = promptService.getFunPrompts();

		String teamId = ctx.getTeamId();
		List<Prompt> custom = promptService.getCustomPrompts(teamId);

		View modalView = AppHome.buildCreateKitModal(custom, personal, workStyle, fun);

		return modalView;
	}
	
	@Bean
	public InstallationService initInstallationService() {
		InstallationService installationService = new AmazonS3InstallationService("slack-install-bucket");
		installationService.setHistoricalDataEnabled(true);
		return installationService;
	}

	@Bean
	public OAuthStateService initStateService() {
		return new CustomAmazonS3OAuthState("slack-install-bucket");
	}
	
	@Bean
	public App initSlackApp(InstallationService installationService, OAuthStateService stateService) {
		App app = new App().asOAuthApp(true);
		app.service(installationService);
	    app.service(stateService);

		app.event(AppHomeOpenedEvent.class, (payload, ctx) -> {
			List<HelloKitScheduled> schedPendingNotSent = helloKitSchedService.getPendingNotSent(payload.getTeamId());
			System.out.println(schedPendingNotSent);
			View appHomeView = AppHome.buildAppHome(ctx, schedPendingNotSent);
			try {
				AppHome.publishView(ctx, payload.getEvent().getUser(), appHomeView);
			} catch (IOException io) {
				io.printStackTrace();
			} catch (SlackApiException sae) {
				sae.printStackTrace();
			}
			return ctx.ack();
		});

		app.viewSubmission("build_new_hello_kit_submit", (req, ctx) -> {
			String kitName = "";
			String channelId = "";
			List<Long> questions = new ArrayList<Long>();
			if(req.getPayload().getView().getState().getValues() != null) {
				Map<String, Map<String,Value>> values = req.getPayload().getView().getState().getValues();
				Collection<Map<String, Value>> viewValues = values.values();
				for(Map<String, Value> entry : viewValues) {
					for(Value value : entry.values()) {
						if(value.getType().equals("plain_text_input")) {
							kitName = value.getValue();
						}
						if(value.getType().equals("multi_conversations_select")) {
							List<String> channelIds = value.getSelectedConversations();
							channelId = channelIds.get(0);
						}
						if(value.getType().equals("checkboxes")) {
							for(SelectedOption se : value.getSelectedOptions()) {
								questions.add(Long.parseLong(se.getValue()));
							}
						}
					}
				}
			}
			
			helloKitService.createNewHelloKit(kitName, req.getPayload().getTeam().getId(), channelId, questions);

			return ctx.ack();
		});
		
		app.viewSubmission("send_hello_kit_submit", (req, ctx) -> {
			String username = "";
			String postAtDate = "";
			String selectedKitId = null;
			String greeting = "";
			if(req.getPayload().getView().getState().getValues() != null) {
				Map<String, Map<String,Value>> values = req.getPayload().getView().getState().getValues();
				Collection<Map<String, Value>> viewValues = values.values();
				for(Map<String, Value> entry : viewValues) {
					for(Value value : entry.values()) {
						if(value.getType().equals("multi_users_select")) {
							List<String> users = value.getSelectedUsers();
							username = users.get(0);
						}
						if(value.getType().equals("datepicker")) {
							postAtDate = value.getSelectedDate();
						}
						if(value.getType().equals("static_select")) {
							selectedKitId = value.getSelectedOption().getValue();
						}
						if(value.getType().equals("plain_text_input")) {
							greeting = value.getValue();
						}
					}
				}
			}
			
			try {
				
				LocalDate date = LocalDate.parse(postAtDate);
				String teamId = req.getPayload().getTeam().getId();
				// TODO: check this is doing what I think its doing
				HelloKitScheduled scheduled = helloKitSchedService.createNewHelloKitScheduled(date, username, selectedKitId, greeting, teamId);
				helloKitSchedService.createScheduledMessage(ctx, scheduled);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return ctx.ack();
		});

		app.blockAction("build_hello_kit", (req, ctx) -> {
			View modalView = createHelloKitModalWithQuestions(ctx);
			try {
				AppHome.openView(ctx, modalView, getBotAccessToken(getTeamId(ctx)));
			} catch (IOException io) {
				io.printStackTrace();
			} catch (SlackApiException sae) {
				sae.printStackTrace();
			}
			return ctx.ack();
		});
		
		app.blockAction("send_hello_kit", (req, ctx) -> {
			String teamId = req.getPayload().getTeam().getId();
			List<HelloKit> helloKits = helloKitService.getHelloKits(teamId);
			View modalView = AppHome.buildSendKitModal(helloKits);
			try {
				AppHome.openView(ctx, modalView, getBotAccessToken(getTeamId(ctx)));
			} catch (IOException io) {
				io.printStackTrace();
			} catch (SlackApiException sae) {
				sae.printStackTrace();
			}
			return ctx.ack();
		});
		
		app.viewSubmission("submit_hello_kit_message", (req, ctx) -> {
			String username = req.getPayload().getUser().getId();
			HelloKitScheduled helloKitSched = helloKitSchedService.getHelloKitSchedByUserId(username);
			Map<String, String> questionsAnswers = new HashMap<String, String>();
			System.out.println("Questionnaire submit: " + req.getPayload().getView().getState().getValues());
			if(req.getPayload().getView().getState().getValues() != null) {
				Map<String, Map<String,Value>> values = req.getPayload().getView().getState().getValues();
				List<Question> questionsToAsk = helloKitSched.getHelloKit().getQuestions();
				for(Question q : questionsToAsk) {
					String[] blockInfo = q.getBlockId().split("-");
					String blockId = blockInfo[1];
					
					Map<String, Value> answer = values.get(blockId);
					questionsAnswers.put(q.getPrompt().getPromptText(), answer.values().stream().findFirst().get().getValue());
				}
			}
			
			UsersInfoResponse userInfo = null;
			try {
				userInfo = ctx.client().usersInfo(i -> i.user(helloKitSched.getUserId()));
			} catch (IOException | SlackApiException e) {
				e.printStackTrace();
			}
			String realName = userInfo.getUser().getName();
			
			String giphyUrl = GiphyService.getGiphyUrl("welcome to the team");
			
			List<LayoutBlock> welcomeMessage = helloKitService.postWelcomeMessage(questionsAnswers, realName, giphyUrl);
			
			HelloKit helloKit = helloKitSched.getHelloKit();
			try {
				ChatScheduleMessageResponse response = ctx.client().chatScheduleMessage(r -> r
						.token(ctx.getBotToken())
						.channel(helloKit.getChannelId())
						.text("Welcome!")
						.blocks(welcomeMessage)
						.postAt((int) Instant.now().getEpochSecond() + 15)
						);
				System.out.println("submit_hello_kit_message submit response: " + response);
				helloKitSched.setStatus("POSTED");
				helloKitSchedService.save(helloKitSched);
			} catch (IOException | SlackApiException e) {
				System.err.print("error: " +  e.getMessage());
			}
			return ctx.ack();
		});
		
		app.blockAction("open_hello_kit_questionnaire", (req, ctx) -> {
			String username = req.getPayload().getUser().getId();
			HelloKitScheduled helloKitSched = helloKitSchedService.getHelloKitSchedByUserId(username);
			View modalView = helloKitSchedService.createHelloMessageModal(ctx, helloKitSched);
			try {
				ViewsOpenResponse vor = AppHome.openView(ctx, modalView, getBotAccessToken(getTeamId(ctx)));
				questionService.updateBlockIds(vor, helloKitSched);
			} catch (IOException io) {
				io.printStackTrace();
			} catch (SlackApiException sae) {
				sae.printStackTrace();
			}
			return ctx.ack();
		});
		
		app.viewSubmission("submit_custom_question", (req, ctx) -> {
			String newQuestion = "";
			String teamId = ctx.getTeamId();
			if(req.getPayload().getView().getState().getValues() != null) {
				Map<String, Map<String,Value>> values = req.getPayload().getView().getState().getValues();
				Collection<Map<String, Value>> viewValues = values.values();
				for(Map<String, Value> entry : viewValues) {
					for(Value value : entry.values()) {
						if(value.getType().equals("plain_text_input")) {
							newQuestion = value.getValue();
						}
					}
				}
			}
			promptService.addNewQuestion(newQuestion, teamId);
			String viewId = req.getPayload().getView().getPrivateMetadata();
			View modalView = createHelloKitModalWithQuestions(ctx, viewId);
			AppHome.updateViewWithSubContext(ctx, modalView, viewId, getBotAccessToken(getTeamId(ctx)));
			return ctx.ack();
		});
		
		app.blockAction("add_new_question", (req, ctx) -> {
			String viewToUpdate = req.getPayload().getView().getId();
			if(canAddQuestion(ctx)) {
				View modalView = helloKitService.addCustomQuestionModal(viewToUpdate);
				try {
					ViewsPushResponse vor = AppHome.pushView(ctx, modalView, getBotAccessToken(getTeamId(ctx)));
					System.out.println(vor);
				} catch (IOException io) {
					io.printStackTrace();
				} catch (SlackApiException sae) {
					sae.printStackTrace();
				}
			}
			
			return ctx.ack();
		});
		app.blockAction("question-group-personal", (req, ctx) -> {
			return ctx.ack();
		});
		app.blockAction("question-group-workStyle", (req, ctx) -> {
			return ctx.ack();
		});
		app.blockAction("question-group-fun", (req, ctx) -> {
			return ctx.ack();
		});
		app.blockAction("question-group-custom", (req, ctx) -> {
			return ctx.ack();
		});

		return app;
	}
}