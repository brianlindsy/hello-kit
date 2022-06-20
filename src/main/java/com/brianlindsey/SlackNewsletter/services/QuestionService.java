package com.brianlindsey.SlackNewsletter.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.brianlindsey.SlackNewsletter.models.HelloKitScheduled;
import com.brianlindsey.SlackNewsletter.models.Question;
import com.brianlindsey.SlackNewsletter.repositories.QuestionRepository;
import com.slack.api.methods.response.views.ViewsOpenResponse;
import com.slack.api.model.block.LayoutBlock;

@Service
public class QuestionService {
	
	@Autowired QuestionRepository questionRepo;

	public void saveAllQuestion(List<Question> questions) {
		questionRepo.saveAll(questions);
	}
	
	public void updateBlockIds(ViewsOpenResponse vor, HelloKitScheduled helloKitSched) {
		List<Question> questionsToAsk = helloKitSched.getHelloKit().getQuestions();
		for(LayoutBlock block : vor.getView().getBlocks()) {
			if(block.getType().equals("input")){
				for(Question q : questionsToAsk) {
					if(block.toString().contains(q.getPrompt().getPromptText())) {
						q.setBlockId(helloKitSched.getUserId() + "-" + block.getBlockId());
					}
				}
			}
		}
		questionRepo.saveAll(questionsToAsk);
	}
}
