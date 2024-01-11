package com.brianlindsey.SlackNewsletter.models;

import javax.persistence.*;

@Entity
public class Prompt {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(columnDefinition = "TEXT")
	private String promptText;
	
	@Enumerated(EnumType.STRING)
	private PromptType promptType;
	
	private String teamId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPromptText() {
		return promptText;
	}

	public void setPromptText(String promptText) {
		this.promptText = promptText;
	}

	public PromptType getPromptType() {
		return promptType;
	}

	public void setPromptType(PromptType promptType) {
		this.promptType = promptType;
	}

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

}
