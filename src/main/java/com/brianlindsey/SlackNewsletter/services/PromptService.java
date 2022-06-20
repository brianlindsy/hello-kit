package com.brianlindsey.SlackNewsletter.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.brianlindsey.SlackNewsletter.models.Prompt;
import com.brianlindsey.SlackNewsletter.models.PromptType;
import com.brianlindsey.SlackNewsletter.repositories.PromptRepository;

@Service
public class PromptService {
	
	@Autowired PromptRepository promptRepo;

	public List<Prompt> getPersonalInfoPrompts() {
		List<Prompt> personal = promptRepo.findByPromptType(PromptType.PERSONAL);
		
		return personal;
	}
	
	public List<Prompt> getWorkStylePrompts() {
		List<Prompt> workStyle = promptRepo.findByPromptType(PromptType.WORKSTYLE);
		
		return workStyle;
	}
	
	public List<Prompt> getFunPrompts() {
		List<Prompt> fun = promptRepo.findByPromptType(PromptType.FUN);
		
		return fun;
	}
	
	public List<Prompt> getCustomPrompts(String teamId) {
		List<Prompt> personal = promptRepo.findByPromptTypeAndTeamId(PromptType.CUSTOM, teamId);
		
		return personal;
	}
	
	public void addNewQuestion(String question, String teamId) {
		Prompt prompt = new Prompt();
		prompt.setPromptText(question);
		prompt.setTeamId(teamId);
		prompt.setPromptType(PromptType.CUSTOM);
		promptRepo.save(prompt);
	}
	
	public Prompt getById(Long id) {
		Prompt prompt = promptRepo.findById(id).get();
		
		return prompt;
	}
}
