package com.brianlindsey.SlackNewsletter.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.brianlindsey.SlackNewsletter.models.Prompt;
import com.brianlindsey.SlackNewsletter.models.PromptType;

public interface PromptRepository extends CrudRepository<Prompt, Long> {
	
	List<Prompt> findByPromptType(PromptType promptType);
	
	List<Prompt> findByPromptTypeAndTeamId(PromptType promptType, String teamId);

}
