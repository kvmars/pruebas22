package cineconecta.cineconecta.controllers.Films;

import cineconecta.cineconecta.models.tmdb.TmdbMovieDto;
import cineconecta.cineconecta.models.tmdb.TmdbMovieSearchResponse;
import cineconecta.cineconecta.models.tmdb.MovieSearchResult;
import cineconecta.cineconecta.service.Films.TmdbService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cine/api/movies")
@CrossOrigin(origins = "http://localhost:3000")
public class MovieController {

    private final TmdbService tmdbService;

    private static final Map<String, String> GENRE_MAP = new HashMap<>();
    static {
        GENRE_MAP.put("accion", "28");
        GENRE_MAP.put("comedia", "35");
        GENRE_MAP.put("drama", "18");
        GENRE_MAP.put("ciencia ficcion", "878");
        GENRE_MAP.put("aventura", "12");
        GENRE_MAP.put("fantasia", "14");
        GENRE_MAP.put("familia", "10751");
        GENRE_MAP.put("romance", "10749");
        GENRE_MAP.put("terror", "27");
        GENRE_MAP.put("crimen", "80");
        GENRE_MAP.put("animacion", "16");
        GENRE_MAP.put("documental", "99");
        GENRE_MAP.put("historia", "36");
        GENRE_MAP.put("musica", "10402");
        GENRE_MAP.put("misterio", "9648");
        GENRE_MAP.put("western", "37");
        GENRE_MAP.put("thriller", "53");
        GENRE_MAP.put("guerra", "10752");
        GENRE_MAP.put("tv movie", "10770");
    }

    @Autowired
    public MovieController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieSearchResult>> searchMovies(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String director) {

        List<MovieSearchResult> results = new ArrayList<>();
        Optional<TmdbMovieSearchResponse> tmdbResponse = Optional.empty();

        if (query != null && !query.trim().isEmpty()) {
            tmdbResponse = tmdbService.searchMovies(query);
        } else if (genre != null && !genre.trim().isEmpty()) {
            System.out.println("📥 Género recibido desde frontend: " + genre);

            String genreId;
            if (genre.matches("\\d+")) {
                genreId = genre;
            } else {
                genreId = GENRE_MAP.get(genre.toLowerCase());
            }

            System.out.println("🔍 genreId mapeado: " + genreId);

            if (genreId != null) {
                tmdbResponse = tmdbService.discoverMovies(genreId, year);
            } else {
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }
        } else if (year != null) {
            tmdbResponse = tmdbService.discoverMovies(null, year);
        } else {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        if (tmdbResponse.isPresent()) {
            results = tmdbResponse.get().getResults().stream()
                    .map(tmdbMovie -> new MovieSearchResult(
                            tmdbMovie.getId(),
                            tmdbMovie.getTitle(),
                            tmdbMovie.getReleaseDate() != null && tmdbMovie.getReleaseDate().length() >= 4
                                    ? tmdbMovie.getReleaseDate().substring(0, 4)
                                    : "N/A",
                            tmdbMovie.getPosterPath()
                    ))
                    .toList();
        }

        if (results.isEmpty()) {
            return ResponseEntity.status(404).body(Collections.emptyList());
        }

        return ResponseEntity.ok(results);
    }

    @GetMapping("/{tmdbId}")
    public ResponseEntity<MovieSearchResult> getMovieById(@PathVariable Long tmdbId) {
        Optional<TmdbMovieDto> tmdbMovieDtoOptional = tmdbService.getMovieDetails(tmdbId);

        if (tmdbMovieDtoOptional.isPresent()) {
            TmdbMovieDto tmdbMovieDto = tmdbMovieDtoOptional.get();
            MovieSearchResult movieDetails = new MovieSearchResult(
                    tmdbMovieDto.getId(),
                    tmdbMovieDto.getTitle(),
                    tmdbMovieDto.getReleaseDate() != null && tmdbMovieDto.getReleaseDate().length() >= 4
                            ? tmdbMovieDto.getReleaseDate().substring(0, 4)
                            : "N/A",
                    tmdbMovieDto.getPosterPath()
            );
            movieDetails.setDescription(tmdbMovieDto.getOverview());
            movieDetails.setGenres(tmdbMovieDto.getGenres() != null
                    ? tmdbMovieDto.getGenres().stream().map(g -> g.getName()).collect(Collectors.joining(", "))
                    : "N/A");
            movieDetails.setDirector(tmdbMovieDto.getDirector());
            movieDetails.setWriters(tmdbMovieDto.getWriters());
            movieDetails.setActors(tmdbMovieDto.getActors());
            movieDetails.setGeneralRating(tmdbMovieDto.getVoteAverage() != null
                    ? tmdbMovieDto.getVoteAverage().intValue()
                    : 0);

            return ResponseEntity.ok(movieDetails);
        }

        return ResponseEntity.notFound().build();
    }
}