package com.example.webflux.functional;

import com.example.webflux.functional.handler.MovieHandler;
import com.example.webflux.functional.model.Movie;
import com.example.webflux.functional.repository.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class WebFluxFunctionalApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebFluxFunctionalApplication.class, args);
    }

    @Bean
    CommandLineRunner init(MovieRepository movieRepository) {

        return args -> {
            Flux<Movie> movieFlux = Flux.just(
                    new Movie(null, "A Clockwork Orange", 9.99),
                    new Movie(null, "The Shining", 7.99),
                    new Movie(null, "Lolita", 6.99))
                    .flatMap(movieRepository::save);

            movieFlux
                    .thenMany(movieRepository.findAll())
                    .subscribe(System.out::println);

        };
    }

    @Bean
    public RouterFunction<ServerResponse>  routes(MovieHandler handler) {
		return route(GET("/movies").and(accept(APPLICATION_JSON)), handler::getAllMovies)
				.andRoute(POST("/movies").and(contentType(APPLICATION_JSON)), handler::saveMovie)
				.andRoute(DELETE("/movies").and(accept(APPLICATION_JSON)), handler::deleteAllMovies)
				.andRoute(GET("/movies/events").and(accept(TEXT_EVENT_STREAM)), handler::getMovieEvents)
				.andRoute(GET("/movies/{id}").and(accept(APPLICATION_JSON)), handler::getMovie)
				.andRoute(PUT("/movies/{id}").and(contentType(APPLICATION_JSON)), handler::updateMovie)
				.andRoute(DELETE("/movies/{id}").and(accept(APPLICATION_JSON)), handler::deleteMovie);

//        return nest(path("/movies"),
//                nest(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON)).or(accept(TEXT_EVENT_STREAM)),
//                        route(GET("/"), handler::getAllMovies)
//                                .andRoute(method(HttpMethod.POST), handler::saveMovie)
//                                .andRoute(DELETE("/"), handler::deleteAllMovies)
//                                .andRoute(GET("/events"), handler::getMovieEvents)
//                                .andNest(path("/{id}"),
//                                        route(method(HttpMethod.GET), handler::getMovie)
//                                                .andRoute(method(HttpMethod.PUT), handler::updateMovie)
//                                                .andRoute(method(HttpMethod.DELETE), handler::deleteMovie)
//                                )
//                )
//        );
    }

}
