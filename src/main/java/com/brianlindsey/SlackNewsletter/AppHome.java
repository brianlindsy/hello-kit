package com.brianlindsey.SlackNewsletter;

import com.brianlindsey.SlackNewsletter.models.HelloKit;
import com.brianlindsey.SlackNewsletter.models.HelloKitScheduled;
import com.brianlindsey.SlackNewsletter.models.Prompt;
import com.brianlindsey.SlackNewsletter.utils.Utils;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.methods.response.views.ViewsOpenResponse;
import com.slack.api.methods.response.views.ViewsPublishResponse;
import com.slack.api.methods.response.views.ViewsPushResponse;
import com.slack.api.methods.response.views.ViewsUpdateResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.event.AppHomeOpenedEvent;
import com.slack.api.model.view.View;
import okio.Options;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.*;
import static com.slack.api.model.view.Views.*;

public class AppHome {

	public static void publishView(EventContext ctx, String userId, View appHomeView) throws IOException, SlackApiException {
		ViewsPublishResponse resp = ctx.client().viewsPublish(r -> r
				.view(appHomeView).userId(userId)
				);
		System.out.println(resp);
	}

	public static ViewsOpenResponse openView(ActionContext ctx, View modalView, String botAccessToken) throws IOException, SlackApiException {
		ViewsOpenResponse resp = ctx.client().viewsOpen(r -> r
				.token(botAccessToken)
				.triggerId(ctx.getTriggerId())
				.view(modalView));
		System.out.println(resp);
		return resp;
	}
	
	public static ViewsPushResponse pushView(ActionContext ctx, View modalView, String botAccessToken) throws IOException, SlackApiException {
		ViewsPushResponse resp = ctx.client().viewsPush(r -> r
				.token(botAccessToken)
				.triggerId(ctx.getTriggerId())
				.view(modalView));
		System.out.println(resp);
		return resp;
	}
	
	public static ViewsUpdateResponse updateView(ActionContext ctx, View modalView, String viewId, String botAccessToken) throws IOException, SlackApiException {
		ViewsUpdateResponse resp = ctx.client().viewsUpdate(r -> r
				.token(botAccessToken)
				.viewId(viewId)
				.view(modalView));
		System.out.println(resp);
		return resp;
	}
	public static ViewsUpdateResponse updateViewWithSubContext(ViewSubmissionContext ctx, View modalView, String viewId, String botAccessToken) throws IOException, SlackApiException {
		ViewsUpdateResponse resp = ctx.client().viewsUpdate(r -> r
				.token(botAccessToken)
				.viewId(viewId)
				.view(modalView));
		System.out.println(resp);
		return resp;
	}

	public static View buildAppHome(EventContext ctx, EventsApiPayload<AppHomeOpenedEvent> payload, List<HelloKitScheduled> scheduledNotSent) throws SlackApiException, IOException {
		List<LayoutBlock> appHomeBlocks = new ArrayList<LayoutBlock>();
		AppHomeOpenedEvent event = payload.getEvent();
		String userId = event.getUser();
		UsersInfoResponse homeScreenUserInfo = ctx.client().usersInfo(i -> i.user(userId));
		System.out.println(homeScreenUserInfo);
		String name = Utils.getRealUserName(homeScreenUserInfo);
		appHomeBlocks.add(header(header -> header.text(plainText(mt -> mt.text(":wave: Welcome to Hello Kit, " + name)))));
		appHomeBlocks.add(divider());
		appHomeBlocks.add(actions(actions -> actions
				.elements(asElements(
						button(b -> b.text(plainText(pt -> pt.emoji(true).text("Build a new HelloKit"))).actionId("build_hello_kit")),
						button(b -> b.text(plainText(pt -> pt.emoji(true).text("Send a HelloKit"))).actionId("send_hello_kit"))
						))
				));
		appHomeBlocks.add(divider());
		appHomeBlocks.add(section(section -> section.text(markdownText(mt -> mt.text("*:outbox_tray: To be sent*")))));
		if(scheduledNotSent.isEmpty()) appHomeBlocks.add(section(section -> section.text(markdownText(mt -> mt.text("No Hello Kits are waiting to be sent, create a new one! :point_up_2:")))));
		for(HelloKitScheduled sched : scheduledNotSent) {
			try {
				UsersInfoResponse userInfo = ctx.client().usersInfo(i -> i.user(sched.getUserId()));
				Instant instant = sched.getSendAt().atOffset(ZoneOffset.of("-4")).toInstant();
				Date myDate = Date.from(instant);
				SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
				String formattedDate = formatter.format(myDate) + " 9AM ET";
				appHomeBlocks.add(section(section -> section.text(markdownText(mt -> mt.text("*:soon:*  " + Utils.getRealUserName(userInfo) + "  :clock1:  " + formattedDate)))));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SlackApiException e) {
				e.printStackTrace();
			}
			
		}
		View appHomeView = view(view -> view
				.type("home")
				.blocks(appHomeBlocks));

		return appHomeView;
	}

	public static List<OptionObject> createPersonalInfoCheckboxes(List<Prompt> personal) {
		List<OptionObject> intro = new ArrayList<OptionObject>();

		for(Prompt question : personal) {
			OptionObject oo = new OptionObject();
			oo.setText(plainText(question.getPromptText()));
			oo.setValue(question.getId().toString());
			intro.add(oo);
		}

		return intro;
	}
	
	public static List<OptionObject> createCustomQuestionCheckboxes(List<Prompt> custom) {
		List<OptionObject> intro = new ArrayList<OptionObject>();

		for(Prompt question : custom) {
			OptionObject oo = new OptionObject();
			oo.setText(plainText(question.getPromptText()));
			oo.setValue(question.getId().toString());
			intro.add(oo);
		}

		return intro;
	}

	public static List<OptionObject> createWorkHabitsCheckboxes(List<Prompt> workStyle) {
		List<OptionObject> intro = new ArrayList<OptionObject>();


		for(Prompt question : workStyle) {
			OptionObject oo = new OptionObject();
			oo.setText(plainText(question.getPromptText()));
			oo.setValue(question.getId().toString());
			intro.add(oo);
		}

		return intro;
	}

	public static List<OptionObject> createFunQuestionsCheckboxes(List<Prompt> fun) {
		List<OptionObject> intro = new ArrayList<OptionObject>();


		for(Prompt question : fun) {
			OptionObject oo = new OptionObject();
			oo.setText(plainText(question.getPromptText()));
			oo.setValue(question.getId().toString());
			intro.add(oo);
		}

		return intro;
	}

	public static View buildCreateKitModal(List<Prompt> custom, List<Prompt> personal, List<Prompt> workStyle, List<Prompt> fun) {
		List<LayoutBlock> modalFirstSection = new ArrayList<LayoutBlock>();
		modalFirstSection.add(divider());
		modalFirstSection.add(input(input -> input.hint(plainText("Ex. Engineering Welcome, Sales Welcome, New Hire Team Welcome")).label(plainText(pt -> pt.text("Name your new Hello Kit:")))
								.element(plainTextInput(pti -> pti))));
		modalFirstSection.add(input(input -> input.hint(plainText("This is the channel that will be posted to when the new team members answers the questions.")).label(plainText(pt -> pt.text("Channel to post Hello to:")))
								.element(multiConversationsSelect(mcs -> mcs.placeholder(plainText("Select a channel"))))));
		modalFirstSection.add(divider());
		modalFirstSection.add(section(section -> section.text(markdownText(mt -> mt.text("*Choose questions for the new team member.*")))));
		
		modalFirstSection.add(actions(actions -> actions
				.elements(asElements(
						button(b -> b.text(plainText(pt -> pt.emoji(true).text("Add custom question     :heavy_plus_sign:"))).actionId("add_new_question")))
				)));
		if(!custom.isEmpty()) {
			modalFirstSection.add(section(section -> section.text(markdownText("Custom Questions (Maximum 10 custom questions per workspace)"))
					.accessory(checkboxes(cb -> cb.actionId("question-group-custom").options(createCustomQuestionCheckboxes(custom))))));
		}
		modalFirstSection.add(section(section -> section.text(markdownText("Personal Introduction"))
								.accessory(checkboxes(cb -> cb.actionId("question-group-personal").options(createPersonalInfoCheckboxes(personal))))));
		modalFirstSection.add(section(section -> section.text(markdownText("Work Style / Habits"))
								.accessory(checkboxes(cb -> cb.actionId("question-group-workStyle").options(createWorkHabitsCheckboxes(workStyle))))));
		modalFirstSection.add(section(section -> section.text(markdownText("Fun Questions"))
								.accessory(checkboxes(cb -> cb.actionId("question-group-fun").options(createFunQuestionsCheckboxes(fun))))));
		
		View appHomeView = view(v -> v
				.type("modal").callbackId("build_new_hello_kit_submit")
				.title(viewTitle(vt -> vt.type("plain_text").text("Build a new Hello Kit")))
				.close(viewClose(vc -> vc.type("plain_text").text("Close")))
				.submit(viewSubmit(vc -> vc.type("plain_text").text("Create")))
				.blocks(modalFirstSection));

		return appHomeView;
	}

	public static List<OptionObject> createKitDropdown(List<HelloKit> helloKits) {
		List<OptionObject> helloKitChoices = new ArrayList<OptionObject>();
		for(HelloKit hello : helloKits) {
			OptionObject oo = new OptionObject();
			oo.setText(new PlainTextObject(hello.getName(), true));
			oo.setValue(hello.getId().toString());
			helloKitChoices.add(oo);
		}

		return helloKitChoices;
	}

	public static View buildSendKitModal(List<HelloKit> helloKits) {
		List<OptionObject> helloKitChoices = createKitDropdown(helloKits);
		OptionObject oo = new OptionObject();
		oo.setText(plainText("Notify Team Members"));
		List<OptionObject> notifyTeamMembers = new ArrayList<OptionObject>();
		notifyTeamMembers.add(oo);
		View appHomeView = view(v -> v
				.type("modal").callbackId("send_hello_kit_submit")
				.title(viewTitle(vt -> vt.type("plain_text").text("Send a Hello Kit")))
				.close(viewClose(vc -> vc.type("plain_text").text("Close")))
				.submit(viewSubmit(vc -> vc.type("plain_text").text("Send")))
				.blocks(asBlocks(
						divider(),
						input(input -> input.label(plainText(pt -> pt.text("Send to:"))).optional(false)
								.element(multiUsersSelect(mus -> mus.placeholder(plainText(pt -> pt.text("Person to welcome with a Hello Kit:")))))),
						input(input -> input.hint(plainText("If set to today, will send immediately. Otherwise will send at 9AM Eastern time.")).label(plainText(pt -> pt.text("Send on:"))).optional(false)
								.element(datePicker(dp -> dp))),
						input(input -> input.label(plainText(pt -> pt.text("Hello Kit to send:"))).optional(false)
								.element(staticSelect(sl -> sl.options(helloKitChoices)))),
						input(input -> input.hint(plainText("Please identify the team members your new hire will be collaborating with closely. Simply enter the Slack usernames.")).label(plainText(pt -> pt.text("Team Collaboration Circle"))).optional(true)
								.element(multiUsersSelect(mus -> mus.placeholder(plainText(pt -> pt.text("Team Collaboration Circle")))))),
						checkboxes(cb -> cb.initialOptions(notifyTeamMembers)), // TODO: finsih this
						input(input -> input.hint(plainText("Add a team member this person can reach out to for questions during their onboarding.")).label(plainText(pt -> pt.text("Onboarding buddy"))).optional(true)
								.element(multiUsersSelect(mus -> mus.placeholder(plainText(pt -> pt.text("Onboarding buddy")))))),
						input(input -> input.label(plainText(pt -> pt.text("Initial Greeting:"))).optional(true)
								.element(plainTextInput(pti -> pti.multiline(true).placeholder(plainText(pt -> pt.text("Give a warm welcome for the new teammate here! This will be shown to the new teammate only when we message them to answer the questions."))))))
						))
				);

		return appHomeView;
	}

}
