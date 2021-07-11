package com.example.webflux.functional.handler;

import com.example.webflux.functional.model.Movie;
import com.example.webflux.functional.model.MovieEvent;
import com.example.webflux.functional.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

/**
 * @author lincoln.pires
 */
@Component
public class MovieHandler {


    private MovieRepository movieRepository;

    public MovieHandler() {
    }

    @Autowired
    public MovieHandler(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Mono<ServerResponse> getAllMovies(ServerRequest serverRequest){
        Flux<Movie> movies = this.movieRepository.findAll();

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(movies, Movie.class);
    }

    public Mono<ServerResponse> getMovie(ServerRequest request) {
        String id = request.pathVariable("id");

        Mono<Movie> movieMono = this.movieRepository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return movieMono
                .flatMap(movie ->
                        ServerResponse.ok()
                                .contentType(APPLICATION_JSON)
                                .body(fromValue(movie)))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> saveMovie(ServerRequest request) {
        Mono<Movie> movieMono = request.bodyToMono(Movie.class);

        return movieMono.flatMap(movie ->
                ServerResponse.status(HttpStatus.CREATED)
                        .contentType(APPLICATION_JSON)
                        .body(this.movieRepository.save(movie), Movie.class));
    }

    public Mono<ServerResponse> updateMovie(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Movie> existingMovieMono = this.movieRepository.findById(id);
        Mono<Movie> movieMono = request.bodyToMono(Movie.class);

        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return movieMono.zipWith(existingMovieMono,
                (movie, existingMovie) ->
                        new Movie(existingMovie.getId(), movie.getName(), movie.getPrice())
        )
                .flatMap(movie ->
                        ServerResponse.ok()
                                .contentType(APPLICATION_JSON)
                                .body(this.movieRepository.save(movie), Movie.class)
                ).switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> deleteMovie(ServerRequest request) {
        String id = request.pathVariable("id");

        Mono<Movie> movieMono = this.movieRepository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return movieMono
                .flatMap(existingMovie ->
                        ServerResponse.ok()
                                .build(this.movieRepository.delete(existingMovie))
                )
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> deleteAllMovies(ServerRequest request) {
        return ServerResponse.ok()
                .build(this.movieRepository.deleteAll());
    }

    public Mono<ServerResponse> getMovieEvents(ServerRequest request) {
        Flux<MovieEvent> eventsFlux = Flux.interval(Duration.ofSeconds(1)).map(val ->
                new MovieEvent(val, "Movie Event")
        );

        return ServerResponse.ok()
                .contentType(TEXT_EVENT_STREAM)
                .body(eventsFlux, MovieEvent.class);
    }


}
