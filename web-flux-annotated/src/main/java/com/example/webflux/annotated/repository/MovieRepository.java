package com.example.webflux.annotated.repository;

import com.example.webflux.annotated.model.Movie;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author lincoln.pires
 */
public interface MovieRepository extends ReactiveMongoRepository<Movie, String> {

}
