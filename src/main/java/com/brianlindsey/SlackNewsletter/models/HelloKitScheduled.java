package com.brianlindsey.SlackNewsletter.models;

import java.time.Instant;

import javax.persistence.*;

@Entity
public class HelloKitScheduled {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
	private HelloKit helloKit;
	
	private String userId;
	
	private Instant sendAt;
	
	private String scheduledMessageId;
	
	@Column(columnDefinition = "TEXT")
	private String greeting;
	
	private Instant createdAt;
	
	private String status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public HelloKit getHelloKit() {
		return helloKit;
	}

	public void setHelloKit(HelloKit helloKit) {
		this.helloKit = helloKit;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Instant getSendAt() {
		return sendAt;
	}

	public void setSendAt(Instant sendAt) {
		this.sendAt = sendAt;
	}

	public String getScheduledMessageId() {
		return scheduledMessageId;
	}

	public void setScheduledMessageId(String scheduledMessageId) {
		this.scheduledMessageId = scheduledMessageId;
	}

	public String getGreeting() {
		return greeting;
	}

	public void setGreeting(String greeting) {
		this.greeting = greeting;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
