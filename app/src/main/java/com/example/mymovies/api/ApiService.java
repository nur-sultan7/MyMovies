package com.example.mymovies.api;

import com.example.mymovies.pojo.MovieResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
   String PARAMS_API_KEY = "api_key";
   String PARAMS_LANGUAGE = "language";
   String PARAMS_SORT_BY = "sort_by";
   String PARAMS_PAGE = "page";
   String PARAMS_MIN_VOTE_COUNT = "vote_count.gte";

@GET("discover/movie")
    Observable<MovieResponse> getMovies(@Query(PARAMS_API_KEY) String api_key, @Query(PARAMS_LANGUAGE) String lang,
                                        @Query(PARAMS_SORT_BY) String sort_by,@Query(PARAMS_PAGE) String page,
                                        @Query(PARAMS_MIN_VOTE_COUNT) String min_vote_count);
@GET("movie/{id}/videos")
    Observable<MovieResponse> getVideos(@Path("id") int id,@Query(PARAMS_API_KEY) String api_key,
                                        @Query(PARAMS_LANGUAGE) String lang);
@GET("movie/{id}/reviews")
    Observable<MovieResponse> getReviews(@Path("id") int id,@Query(PARAMS_API_KEY) String api_key,
                                        @Query(PARAMS_LANGUAGE) String lang);
}
