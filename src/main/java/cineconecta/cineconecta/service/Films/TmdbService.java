package cineconecta.cineconecta.service.Films;

import cineconecta.cineconecta.models.tmdb.TmdbMovieDto;
import cineconecta.cineconecta.models.tmdb.TmdbMovieSearchResponse;
import cineconecta.cineconecta.models.tmdb.MovieSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TmdbService {

    // Ahora inyectas el cliente de la API C++
    private final CppMovieApiClient cppMovieApiClient;

    //private final RestTemplate restTemplate; // <--- Cambio clave: Ahora es RestTemplate

    /*
    @Value("33d6f9d20e39e104025fe947df34c197")
    private String apiKey;

    @Value("https://api.themoviedb.org/3")
    private String tmdbBaseUrl;

    @Value("https://image.tmdb.org/t/p/")
    private String tmdbImageBaseUrl;

    @Value("w342")
    private String tmdbPosterSize;
     */

    @Autowired
    public TmdbService(CppMovieApiClient cppMovieApiClient) { // <--- Constructor simple sin inyección de WebClient.Builder
        this.cppMovieApiClient = cppMovieApiClient; // Inicializa RestTemplate
    }

    // Este metodo ahora llama a la API C++
    public Optional<TmdbMovieSearchResponse> searchMovies(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Optional.empty(); // Protección adicional
        }

        String genreId = mapGenreToId(query.trim());

        Optional<List<MovieSearchResult>> cppResults;

        // Si es un género válido, busca por genreId, si no, busca por título (query)
        if (genreId != null) {
            cppResults = cppMovieApiClient.searchMoviesInCpp(null, genreId, null);
        } else {
            cppResults = cppMovieApiClient.searchMoviesInCpp(query, null, null);
        }

        if (cppResults.isPresent()) {
            List<TmdbMovieDto> tmdbMovieDtos = cppResults.get().stream()
                    .map(msr -> {
                        TmdbMovieDto dto = new TmdbMovieDto();
                        dto.setId(msr.getTmdbId());
                        dto.setTitle(msr.getTitle());
                        dto.setReleaseDate(msr.getYear() + "-01-01");
                        dto.setPosterPath(msr.getImageUrl()); // Ya es URL completa desde C++
                        return dto;
                    })
                    .collect(Collectors.toList());

            TmdbMovieSearchResponse response = new TmdbMovieSearchResponse();
            response.setResults(tmdbMovieDtos);
            response.setTotalResults(tmdbMovieDtos.size());
            response.setPage(1);
            response.setTotalPages(1);
            return Optional.of(response);
        }

        return Optional.empty();
    }

    // Este metodo ahora llama a la API C++
    public Optional<TmdbMovieDto> getMovieDetails(Long tmdbId) {
        Optional<MovieSearchResult> movieDetails = cppMovieApiClient.getMovieDetailsInCpp(tmdbId);

        if (movieDetails.isPresent()) {
            MovieSearchResult msr = movieDetails.get();
            TmdbMovieDto dto = new TmdbMovieDto();
            dto.setId(msr.getTmdbId());
            dto.setTitle(msr.getTitle());
            dto.setReleaseDate(msr.getYear() + "-01-01");
            dto.setOverview(msr.getDescription());
            dto.setPosterPath(msr.getImageUrl().replace("https://image.tmdb.org/t/p/w342", ""));
            dto.setVoteAverage(msr.getGeneralRating() != null ? msr.getGeneralRating().doubleValue() : 0.0);
            dto.setDirector(msr.getDirector());
            dto.setWriters(msr.getWriters());
            dto.setActors(msr.getActors());

            // ✅ Mapea "Drama, Aventura" a List<GenreDto>
            if (msr.getGenres() != null && !msr.getGenres().isEmpty()) {
                dto.setGenres(Arrays.stream(msr.getGenres().split(","))
                        .map(String::trim)
                        .map(name -> new TmdbMovieDto.GenreDto(name))
                        .collect(Collectors.toList()));
            }

            return Optional.of(dto);
        }
        return Optional.empty();
    }

    // Los métodos discoverMovies, searchPerson, getMoviesByPerson también necesitarán ser refactorizados
    // para llamar a la API C++ o ser movidos completamente a C++ si se decide.
    // Por ahora, los dejaré como estaban, asumiendo que la prioridad es la búsqueda básica y detalles.
    public Optional<TmdbMovieSearchResponse> discoverMovies(String genreId, Integer year) {
        System.out.println("🟣 Buscando películas por género ID: " + genreId + " y año: " + year);

        Optional<List<MovieSearchResult>> cppResults = cppMovieApiClient.searchMoviesInCpp(null, genreId, year);

        if (cppResults.isPresent()) {
            System.out.println("✅ Resultados encontrados: " + cppResults.get().size());

            List<TmdbMovieDto> tmdbMovieDtos = cppResults.get().stream()
                    .map(msr -> {
                        TmdbMovieDto dto = new TmdbMovieDto();
                        dto.setId(msr.getTmdbId());
                        dto.setTitle(msr.getTitle());
                        dto.setReleaseDate(msr.getYear() + "-01-01");
                        dto.setPosterPath(msr.getImageUrl()); // Ya es completa
                        return dto;
                    })
                    .toList();

            TmdbMovieSearchResponse response = new TmdbMovieSearchResponse();
            response.setResults(tmdbMovieDtos);
            response.setTotalResults(tmdbMovieDtos.size());
            response.setPage(1);
            response.setTotalPages(1);

            return Optional.of(response);
        }

        System.out.println("❌ No se encontraron resultados desde C++.");
        return Optional.empty();
    }

    private String mapGenreToId(String genre) {
        return switch (genre.toLowerCase()) {
            case "accion", "acción" -> "28";
            case "aventura" -> "12";
            case "animación" -> "16";
            case "comedia" -> "35";
            case "crimen" -> "80";
            case "documental" -> "99";
            case "drama" -> "18";
            case "familia" -> "10751";
            case "fantasía" -> "14";
            case "historia" -> "36";
            case "terror", "horror" -> "27";
            case "musical" -> "10402";
            case "misterio" -> "9648";
            case "romance" -> "10749";
            case "ciencia ficción", "sci-fi" -> "878";
            case "película de tv", "tv movie" -> "10770";
            case "thriller" -> "53";
            case "bélico", "guerra" -> "10752";
            case "western" -> "37";
            default -> null;
        };
    }
}