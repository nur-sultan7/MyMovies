package com.example.mymovies.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mymovies.pojo.Movie;

import java.util.List;

@Dao
public interface MovieDao {
    @Query("SELECT * FROM movies")
    LiveData<List<Movie>> getAllMovies();

    @Query("SELECT * FROM favourite_movie")
    LiveData<List<FavouriteMovie>> getAllFavouriteMovies();

    @Query("SELECT * FROM movies WHERE id==:movieId")
    Movie getMovieById(int movieId);

    @Query("SELECT * FROM favourite_movie WHERE id==:movieId")
    FavouriteMovie getFavouriteMovieById(int movieId);

    @Query("DELETE FROM movies")
    void deleteAllMovies();

    @Insert
    void insertMovies(List<com.example.mymovies.pojo.Movie> movies);
    @Insert
    void insertMovie(Movie movie);

    @Delete
    void deleteMovie(Movie movie);

    @Insert
    void insertFavouriteMovie(FavouriteMovie movie);

    @Delete
    void deleteFavouriteMovie(FavouriteMovie movie);

    @Query("Select * from temporary_movies")
    LiveData<List<TemporaryMovie>> getTemporaryMovies();

    @Query("Select * from temporary_movies where id==:id")
    TemporaryMovie getTemporaryMovie(int id);

    @Insert
    void insertTemporaryMovies(List<TemporaryMovie> temporaryMovieList);

    @Query("Delete from temporary_movies")
    void deleteTempMovies();
}
