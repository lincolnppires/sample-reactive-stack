package com.example.webflux.functional.repository;


import com.example.webflux.functional.model.Movie;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author lincoln.pires
 */
public interface MovieRepository extends ReactiveMongoRepository<Movie, String> {

}
