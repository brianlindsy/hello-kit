package com.brianlindsey.SlackNewsletter.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.brianlindsey.SlackNewsletter.models.HelloKitScheduled;

public interface HelloKitScheduledRepository extends CrudRepository<HelloKitScheduled, Long>  {
	List<HelloKitScheduled> findBySendAtGreaterThanAndStatusAndHelloKit_TeamId(Instant sendAt, String status, String teamId);
	Optional<HelloKitScheduled> findByUserIdAndStatus(String id, String status);
}
