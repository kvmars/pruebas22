package cineconecta.cineconecta.models.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TmdbMovieDto {
    private Long id;
    private String title;

    @JsonProperty("release_date")
    private String releaseDate;

    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("vote_average")
    private Double voteAverage;

    private List<GenreDto> genres;

    private String director;
    private String writers;
    private String actors;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }

    public List<GenreDto> getGenres() { return genres; }
    public void setGenres(List<GenreDto> genres) { this.genres = genres; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getWriters() { return writers; }
    public void setWriters(String writers) { this.writers = writers; }

    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }

    public String getReleaseYear() {
        if (releaseDate != null && releaseDate.length() >= 4) {
            return releaseDate.substring(0, 4);
        }
        return null;
    }

    // ✅ Clase anidada para géneros
    public static class GenreDto {
        private String name;

        public GenreDto() {}

        public GenreDto(String name) {
            this.name = name;
        }

        public String getName() { return name; }

        public void setName(String name) { this.name = name; }
    }
}