package com.brianlindsey.SlackNewsletter.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.brianlindsey.SlackNewsletter.models.HelloKit;

public interface HelloKitRepository extends CrudRepository<HelloKit, Long>  {
	List<HelloKit> findByTeamId(String teamId);

}
