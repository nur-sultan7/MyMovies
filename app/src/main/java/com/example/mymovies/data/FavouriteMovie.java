package com.example.mymovies.data;

import androidx.room.Entity;
import androidx.room.Ignore;

import com.example.mymovies.pojo.Movie;

import java.util.List;

@Entity(tableName = "favourite_movie")
public class FavouriteMovie extends Movie {


    public FavouriteMovie(int uniqueId, String posterPath, boolean adult, String overview, String releaseDate,
                          List<Integer> genreIds, int id, String originalTitle, String originalLanguage, String title,
                          String backdropPath, double popularity, int voteCount, boolean video, double voteAverage) {
        super(uniqueId, posterPath, adult, overview, releaseDate, genreIds, id, originalTitle, originalLanguage, title, backdropPath, popularity, voteCount, video, voteAverage);
    }

    @Ignore
    public FavouriteMovie(Movie movie)
    {
        super(movie.getPosterPath(),movie.isAdult(),movie.getOverview(),movie.getReleaseDate(),
                movie.getGenreIds(), movie.getId(), movie.getOriginalTitle(),movie.getOriginalLanguage(),movie.getTitle(),
                movie.getBackdropPath(), movie.getPopularity(), movie.getVoteCount(),movie.isVideo(), movie.getVoteAverage());
    }
}
