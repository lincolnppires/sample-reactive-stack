package com.example.webflux.annotated;

import com.example.webflux.annotated.controller.MovieController;
import com.example.webflux.annotated.model.Movie;
import com.example.webflux.annotated.model.MovieEvent;
import com.example.webflux.annotated.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author lincoln.pires
 */
@ExtendWith(SpringExtension.class)
@WebFluxTest(MovieController.class)
public class MovieControllerMockAnnotationTest {

    @Autowired
    private WebTestClient client;

    private List<Movie> expectedList;

    @MockBean
    private MovieRepository repository;


    @BeforeEach
    void beforeEach() {
        this.expectedList = Arrays.asList(
                new Movie("01", "dolor y gloria", 9.99)
        );
    }

    @Test
    void testGetAllMovies() {
        when(repository.findAll()).thenReturn(Flux.fromIterable(this.expectedList));

        client
                .get()
                .uri("/movies")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Movie.class)
                .isEqualTo(expectedList);
    }

    @Test
    void testMovieInvalidIdNotFound() {
        String id = "aaa";
        when(repository.findById(id)).thenReturn(Mono.empty());

        client
                .get()
                .uri("/movies/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void testMovieIdFound() {
        Movie expectedMovie = expectedList.get(0);
        when(repository.findById(expectedMovie.getId())).thenReturn(Mono.just(expectedMovie));

        client
                .get()
                .uri("/movies/{id}", expectedMovie.getId())
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
                client.get().uri("/movies/events")
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
