package com.example.webflux.annotated;

import com.example.webflux.annotated.controller.MovieController;
import com.example.webflux.annotated.model.Movie;
import com.example.webflux.annotated.model.MovieEvent;
import com.example.webflux.annotated.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author lincoln.pires
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MovieControllerTest{

    private WebTestClient client;

    private List<Movie> expectedList;

    @Autowired
    private MovieRepository repository;

    @BeforeEach
    void beforeEach() {
        this.client =
                WebTestClient
                        .bindToController(new MovieController(repository))
                        .configureClient()
                        .baseUrl("/movies")
                        .build();

        this.expectedList =
                repository.findAll().collectList().block();
    }

    @Test
    void testGetAllMovies() {
        client
                .get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Movie.class)
                .isEqualTo(expectedList);
    }

    @Test
    void testMovieInvalidIdNotFound() {
        client
                .get()
                .uri("/aaa")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void testMovieIdFound() {
        Movie expectedMovie = expectedList.get(0);
        client
                .get()
                .uri("/{id}", expectedMovie.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .isEqualTo(expectedMovie);
    }

    @Test
    void testMovieEvents() {
        MovieEvent expectedEvent =
                new MovieEvent(0L, "Movie Event");

        FluxExchangeResult<MovieEvent> result =
                client.get().uri("/events")
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(MovieEvent.class);

        StepVerifier.create(result.getResponseBody())
                .expectNext(expectedEvent)
                .expectNextCount(2)
                .consumeNextWith(event ->
                        assertEquals(Long.valueOf(3), event.getEventId()))
                .thenCancel()
                .verify();
    }
}
