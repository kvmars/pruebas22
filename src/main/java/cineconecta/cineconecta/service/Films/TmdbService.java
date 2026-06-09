package cineconecta.cineconecta.service.Films;

import cineconecta.cineconecta.models.tmdb.TmdbMovieDto;
import cineconecta.cineconecta.models.tmdb.TmdbMovieSearchResponse;
import cineconecta.cineconecta.models.tmdb.TmdbCreditsDto;
import cineconecta.cineconecta.models.tmdb.PersonMovieCreditsResponse;
import cineconecta.cineconecta.models.tmdb.PersonCrewCreditDto;
import cineconecta.cineconecta.models.tmdb.TmdbPersonSearchResponse;
import cineconecta.cineconecta.models.tmdb.MovieSearchResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

//C++
import org.springframework.beans.factory.annotation.Autowired;
// YA NO SE NECESITARA PORQUE LO HARA CppMovieApiClient
// import org.springframework.web.client.RestTemplate; // <--- Importante: Aquí es RestTemplate
// import org.springframework.web.util.UriComponentsBuilder; // Para construir URLs de forma segura

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Necesario para colectar listas

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
        Optional<List<MovieSearchResult>> cppResults = cppMovieApiClient.searchMoviesInCpp(query, null, null);

        if (cppResults.isPresent()) {
            // Mapea MovieSearchResult de C++ a TmdbMovieDto para mantener la compatibilidad
            // con TmdbMovieSearchResponse si es necesario, o refactoriza para usar MovieSearchResult directamente
            List<TmdbMovieDto> tmdbMovieDtos = cppResults.get().stream()
               .map(msr -> {
                    TmdbMovieDto dto = new TmdbMovieDto();
                    dto.setId(msr.getTmdbId());
                    dto.setTitle(msr.getTitle());
                    dto.setReleaseDate(msr.getYear() + "-01-01"); // Asume un formato para la fecha
                    dto.setPosterPath(msr.getImageUrl().replace("https://image.tmdb.org/t/p/w342", "")); // Quita la base para que sea relativa
                    // Otros campos si los necesitas
                    return dto;
                })
               .collect(Collectors.toList());

            TmdbMovieSearchResponse response = new TmdbMovieSearchResponse();
            response.setResults(tmdbMovieDtos);
            response.setTotalResults(tmdbMovieDtos.size());
            response.setPage(1); // Simplificado
            response.setTotalPages(1); // Simplificado
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
            dto.setReleaseDate(msr.getYear() + "-01-01"); // Asume un formato
            dto.setOverview(msr.getDescription());
            dto.setPosterPath(msr.getImageUrl().replace("https://image.tmdb.org/t/p/w342", "")); // Quita la base
            dto.setVoteAverage(msr.getGeneralRating()!= null? msr.getGeneralRating().doubleValue() : 0.0);
            // Aquí puedes seguir procesando director, escritores, actores si C++ los devuelve
            // O mantener la lógica actual de TmdbService para obtener créditos si C++ no los devuelve
            // Para géneros, necesitarías parsear el string "Ciencia Ficción, Acción" de C++ a List<GenreDto>
            // o refactorizar MovieSearchResult para que los géneros sean una lista de strings.
            return Optional.of(dto);
        }
        return Optional.empty();
    }

    // Este método ya no es necesario si C++ devuelve la URL completa
    public Optional<String> getFullPosterUrl(String posterPath) {
        // Si C++ ya devuelve la URL completa, este método podría ser obsoleto
        // O podrías usarlo para construir URLs de imágenes que no vengan de C++
        if (posterPath!= null &&!posterPath.isEmpty()) {
            // Asumiendo que posterPath ya es una URL completa de C++
            if (posterPath.startsWith("http")) {
                return Optional.of(posterPath);
            }
            // Si por alguna razón todavía recibes rutas relativas, puedes construirlas
            // return Optional.of("https://image.tmdb.org/t/p/w342" + posterPath);
        }
        return Optional.empty();
    }

    // Los métodos discoverMovies, searchPerson, getMoviesByPerson también necesitarán ser refactorizados
    // para llamar a la API C++ o ser movidos completamente a C++ si se decide.
    // Por ahora, los dejaré como estaban, asumiendo que la prioridad es la búsqueda básica y detalles.
    public Optional<TmdbMovieSearchResponse> discoverMovies(String genreId, Integer year) {
        Optional<List<MovieSearchResult>> cppResults = cppMovieApiClient.searchMoviesInCpp(null, genreId, year);
        //... mapeo similar a searchMovies
        return Optional.empty(); // Placeholder
    }

    public Optional<TmdbPersonSearchResponse> searchPerson(String personName) {
        // Esta lógica de búsqueda de persona y luego películas por persona
        // es más compleja y podría quedarse en Java o ser un endpoint dedicado en C++
        return Optional.empty(); // Placeholder
    }

    public Optional<TmdbMovieSearchResponse> getMoviesByPerson(Long personId) {
        return Optional.empty(); // Placeholder
    }
}