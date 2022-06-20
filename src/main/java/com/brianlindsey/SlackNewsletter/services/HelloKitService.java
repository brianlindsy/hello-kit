package com.brianlindsey.SlackNewsletter.services;

import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.input;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.plainTextInput;
import static com.slack.api.model.view.Views.view;
import static com.slack.api.model.view.Views.viewClose;
import static com.slack.api.model.view.Views.viewSubmit;
import static com.slack.api.model.view.Views.viewTitle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.brianlindsey.SlackNewsletter.models.HelloKit;
import com.brianlindsey.SlackNewsletter.models.Prompt;
import com.brianlindsey.SlackNewsletter.models.Question;
import com.brianlindsey.SlackNewsletter.repositories.HelloKitRepository;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.View;

@Service
public class HelloKitService {
	
	@Autowired HelloKitRepository helloKitRepo;
	@Autowired PromptService promptService;
	@Autowired QuestionService questionService;
	
	public HelloKit createNewHelloKit(String name, String teamId, String channelId, List<Long> questionIds) {
		HelloKit helloKit = new HelloKit();
		helloKit.setChannelId(channelId);
		helloKit.setName(name);
		helloKit.setTeamId(teamId);
		helloKit.setCreatedAt(Instant.now());
		helloKit.setStatus("ACTIVE");
		
		ArrayList<Question> questionsToInsert = new ArrayList<Question>();
		
		for(Long id : questionIds) {
			Question question = new Question();
			question.setCreatedAt(Instant.now());
			Prompt prompt = promptService.getById(id);
			question.setPrompt(prompt);
			questionsToInsert.add(question);
		}
		
		questionService.saveAllQuestion(questionsToInsert);
		
		helloKit.setQuestions(questionsToInsert);
		
		HelloKit saved = helloKitRepo.save(helloKit);
		
		return saved;
	}
	
	public List<HelloKit> getHelloKits(String teamId) {
		List<HelloKit> found = helloKitRepo.findByTeamId(teamId);
		
		return found;
	}
	
	public List<LayoutBlock> postWelcomeMessage(Map<String, String> questionAnswers, String realName, String giphyUrl) {
		List<LayoutBlock> message = new ArrayList<LayoutBlock>();
		message.add(section(section -> section.text(markdownText(mt -> mt.text("Hello all!  :wave:  Please give a warm welcome to :tada: @"
		+ realName +" :tada: \nHere is a little about them. :point_down:" + giphyUrl)))));
		message.add(divider());
		for(Map.Entry<String, String> entry : questionAnswers.entrySet()) {
			if(entry.getValue() != null && !entry.getValue().isBlank()) {
				message.add(section(section -> section.text(markdownText(mt -> mt.text("*" + entry.getKey() + "*" + "\n" + entry.getValue())))));
			}
		}
		return message;
	}
	
	public View addCustomQuestionModal(String viewToUpdate) {
		List<LayoutBlock> message = new ArrayList<LayoutBlock>();
		message.add(input(input -> input.hint(plainText("Ask a question for you and your team to get to know the new hire. Make it fun, informative or just silly!")).label(plainText(pt -> pt.text("New Question:")))
				.element(plainTextInput(pti -> pti))));
		
		View customQuestion = view(v -> v
				.type("modal").callbackId("submit_custom_question")
				.privateMetadata(viewToUpdate)
				.title(viewTitle(vt -> vt.type("plain_text").text("Add New Question")))
				.close(viewClose(vc -> vc.type("plain_text").text("Close")))
				.submit(viewSubmit(vc -> vc.type("plain_text").text("Add")))
				.blocks(message)
				);
		return customQuestion;
	}

}
