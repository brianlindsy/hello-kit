package com.brianlindsey.SlackNewsletter.repositories;

import org.springframework.data.repository.CrudRepository;

import com.brianlindsey.SlackNewsletter.models.Question;

public interface QuestionRepository extends CrudRepository<Question, Long> {

}
