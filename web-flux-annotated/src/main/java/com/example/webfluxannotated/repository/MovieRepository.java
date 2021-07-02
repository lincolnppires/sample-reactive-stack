package com.example.webfluxannotated.repository;

import com.example.webfluxannotated.model.Movie;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author lincoln.pires
 */
public interface MovieRepository extends ReactiveMongoRepository<Movie, String> {

}
