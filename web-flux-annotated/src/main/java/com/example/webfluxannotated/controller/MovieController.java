package com.example.webfluxannotated.controller;

import com.example.webfluxannotated.model.Movie;
import com.example.webfluxannotated.repository.MovieRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
}
