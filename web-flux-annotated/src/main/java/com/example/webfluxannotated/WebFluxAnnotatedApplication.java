package com.example.webfluxannotated;

import com.example.webfluxannotated.model.Movie;
import com.example.webfluxannotated.repository.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class WebFluxAnnotatedApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebFluxAnnotatedApplication.class, args);
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
}
