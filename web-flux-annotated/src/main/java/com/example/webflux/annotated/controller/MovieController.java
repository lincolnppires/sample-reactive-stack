package com.example.webflux.annotated.controller;

import com.example.webflux.annotated.model.MovieEvent;
import com.example.webflux.annotated.repository.MovieRepository;
import com.example.webflux.annotated.model.Movie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author lincoln.pires
 */
@RestController
@RequestMapping("/movies")
public class MovieController {

    private MovieRepository movieRepository;

    public MovieController(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @GetMapping
    public Flux<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Movie>> getMovie(@PathVariable String id) {
        return movieRepository.findById(id)
                .map(movie -> ResponseEntity.ok(movie))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Movie> saveMovie(@RequestBody Movie movie) {
        return movieRepository.save(movie);
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Movie>> updateMovie(@PathVariable String id, @RequestBody Movie movie) {
        return movieRepository.findById(id)
                .flatMap(existingMovie -> {
                    existingMovie.setName(movie.getName());
                    existingMovie.setPrice(movie.getPrice());
                    return movieRepository.save(existingMovie);
                })
                .map(updateMovie -> ResponseEntity.ok(updateMovie))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> deleteMovie(@PathVariable String id) {
        return movieRepository.findById(id)
                .flatMap(existingMovie ->
                        movieRepository.delete(existingMovie)
                                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping
    public Mono<Void> deleteAllMovies() {
        return movieRepository.deleteAll();
    }

    //test in browser (http://localhost:8080/movies/events)
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MovieEvent> getMovieEvents(){
        return Flux.interval(Duration.ofSeconds(2))
                .map(val ->
                        new MovieEvent(val, "Movie Event"));
    }
}
