package com.example.mymovies.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mymovies.api.ApiFactory;
import com.example.mymovies.api.ApiService;
import com.example.mymovies.pojo.Movie;
import com.example.mymovies.pojo.MovieResponse;
import com.example.mymovies.pojo.Review;
import com.example.mymovies.pojo.ReviewsResponse;
import com.example.mymovies.pojo.Video;
import com.example.mymovies.pojo.VideosResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends AndroidViewModel {


    private static final String API_KEY = "93583a10883c1e30212173ba58e9d57b";

    private static final String SORT_BY_POPULARITY = "popularity.desc";
    private static final String SORT_BY_TOP_RATED = "vote_average.desc";
    private static final String MIN_VOTE_COUNT_VALUE = "1000";
    private static MovieDatabase database;
    private LiveData<List<Movie>> movies;
    private LiveData<List<FavouriteMovie>> favouriteMovies;

    private static MutableLiveData<List<Review>> reviewList;
    private static MutableLiveData<List<Video>> videosList;
    private static MutableLiveData<Boolean> isLoading;
    private static MutableLiveData<List<Movie>> listMoreVideos;

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<List<Movie>> getListMoreVideos() {
        return listMoreVideos;
    }

    public MainViewModel(@NonNull Application application) {
        super(application);
        database = MovieDatabase.getInstance(getApplication());
        movies = database.movieDao().getAllMovies();
        favouriteMovies = database.movieDao().getAllFavouriteMovies();
        reviewList = new MutableLiveData<>();
        videosList = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        listMoreVideos = new MutableLiveData<>();
    }

    public LiveData<List<Video>> getVideosList() {
        return videosList;
    }

    public LiveData<List<Review>> getReviewList() {
        return reviewList;
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }


    public void loadData(final int sortBy, final int page, String lang, final boolean isFirstLoading) {
        isLoading.setValue(true);
        String methodOfSort;
        if (sortBy == 0) {
            methodOfSort = SORT_BY_POPULARITY;
        } else {
            methodOfSort = SORT_BY_TOP_RATED;
        }
        ApiFactory apiFactory = ApiFactory.getInstance();
        ApiService apiService = apiFactory.getApiService();
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        Disposable disposable = apiService.getMovies(API_KEY, lang, methodOfSort, String.valueOf(page), MIN_VOTE_COUNT_VALUE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MovieResponse>() {
                    @Override
                    public void accept(MovieResponse moviewsResponse) throws Exception {
                        isLoading.setValue(false);

                        if (page == 1 && listMoreVideos.getValue() != null && listMoreVideos.getValue().size() == 0) {
                            deleteTempMovies();
                        }
                        if (page == 1 && movies.getValue() != null && movies.getValue().size() == 0 && sortBy == 0) {

                            insertMovies(moviewsResponse.getResults());

                        } else if (page == 1 && movies.getValue() != null && movies.getValue().size() > 0 && sortBy == 0 && !isFirstLoading) {
                            deleteAllMovies();
                            insertMovies(moviewsResponse.getResults());

                        } else if (page > 1 | (page == 1 && sortBy == 1)) {
                            insertTemporaryMovies(moviewsResponse.getResults());
                            listMoreVideos.postValue(moviewsResponse.getResults());
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
        compositeDisposable.add(disposable);
    }

    public void loadVideosOfMovie(int movieId, String lang) {

        ApiFactory apiFactory = ApiFactory.getInstance();
        ApiService apiService = apiFactory.getApiService();
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        Disposable disposable = apiService.getVideos(movieId, API_KEY, lang)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<VideosResponse>() {
                    @Override
                    public void accept(VideosResponse videosResponse) throws Exception {
                        videosList.setValue(videosResponse.getResults());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
        compositeDisposable.add(disposable);

    }

    public void loadReviewsOfMovie(int movieId, String lang) {

        ApiFactory apiFactory = ApiFactory.getInstance();
        ApiService apiService = apiFactory.getApiService();
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        Disposable disposable = apiService.getReviews(movieId, API_KEY, lang)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ReviewsResponse>() {
                    @Override
                    public void accept(ReviewsResponse reviewsResponse) throws Exception {
                        reviewList.setValue(reviewsResponse.getResults());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
        compositeDisposable.add(disposable);

    }

    public TemporaryMovie getTemporaryMovieById(int id) {
        TemporaryMovie movie = null;
        try {
            movie = new GetMovieById().execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return movie;
    }

    private static class GetMovieById extends AsyncTask<Integer, Void, TemporaryMovie> {

        @Override
        protected TemporaryMovie doInBackground(Integer... integers) {
            TemporaryMovie temporaryMovie = null;
            if (integers[0] != null)
                temporaryMovie = database.movieDao().getTemporaryMovie(integers[0]);
            return temporaryMovie;
        }
    }

    @SuppressWarnings("unchecked")
    public void insertTemporaryMovies(List<Movie> movies) {
        new InsertTempMoviesTask().execute(movies);
    }

    private static class InsertTempMoviesTask extends AsyncTask<List<Movie>, Void, Void> {

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<Movie>... lists) {

            if (lists[0] != null && lists[0].size() > 0) {
                List<TemporaryMovie> temporaryMovies = new ArrayList<>();
                for (Movie movie : lists[0]) {
                    temporaryMovies.add(new TemporaryMovie(movie));
                }
                database.movieDao().insertTemporaryMovies(temporaryMovies);
            }
            return null;
        }
    }

    public void deleteTempMovies() {
        new DeleteTempMoviesTask().execute();
    }

    private static class DeleteTempMoviesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            database.movieDao().deleteTempMovies();
            return null;
        }
    }

    public LiveData<List<FavouriteMovie>> getFavouriteMovies() {
        return favouriteMovies;
    }

    public Movie getMovieById(int id) {
        try {
            return new GetMovieTask().execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }

    public FavouriteMovie getFavouriteMovieById(int id) {
        try {
            return new GetFavouriteMovieTask().execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void deleteAllMovies() {
        new DeleteAllMovieTask().execute();
    }

    public void insertMovie(Movie movie) {
        new InsertMovieTask().execute(movie);
    }

    @SuppressWarnings("unchecked")
    private void insertMovies(List<com.example.mymovies.pojo.Movie> movies) {
        new InsertMoviesTask().execute(movies);
    }

    public void deleteMovie(Movie movie) {
        new DeleteMovieTask().execute(movie);
    }

    private static class InsertMoviesTask extends AsyncTask<List<com.example.mymovies.pojo.Movie>, Void, Void> {


        @Override
        protected Void doInBackground(List<com.example.mymovies.pojo.Movie>... lists) {
            if (lists[0] != null && lists[0].size() > 0)
                database.movieDao().insertMovies(lists[0]);
            return null;
        }
    }

    private static class DeleteMovieTask extends AsyncTask<Movie, Void, Void> {

        @Override
        protected Void doInBackground(Movie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().deleteMovie(movies[0]);
            }
            return null;
        }
    }

    private static class InsertMovieTask extends AsyncTask<Movie, Void, Void> {

        @Override
        protected Void doInBackground(Movie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().insertMovie(movies[0]);
            }
            return null;
        }
    }

    private static class GetMovieTask extends AsyncTask<Integer, Void, Movie> {

        @Override
        protected Movie doInBackground(Integer... integers) {
            if (integers != null && integers.length > 0) {
                return database.movieDao().getMovieById(integers[0]);
            }
            return null;
        }
    }

    private static class DeleteAllMovieTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            database.movieDao().deleteAllMovies();
            return null;
        }
    }

    public void insertFavouriteMovie(FavouriteMovie movie) {
        new InsertFavouriteMovieTask().execute(movie);
    }

    public void deleteFavouriteMovie(FavouriteMovie movie) {
        new DeleteFavouriteMovieTask().execute(movie);
    }

    private static class DeleteFavouriteMovieTask extends AsyncTask<FavouriteMovie, Void, Void> {

        @Override
        protected Void doInBackground(FavouriteMovie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().deleteFavouriteMovie(movies[0]);
            }
            return null;
        }
    }

    private static class InsertFavouriteMovieTask extends AsyncTask<FavouriteMovie, Void, Void> {

        @Override
        protected Void doInBackground(FavouriteMovie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().insertFavouriteMovie(movies[0]);
            }
            return null;
        }
    }

    private static class GetFavouriteMovieTask extends AsyncTask<Integer, Void, FavouriteMovie> {

        @Override
        protected FavouriteMovie doInBackground(Integer... integers) {
            if (integers != null && integers.length > 0) {
                return database.movieDao().getFavouriteMovieById(integers[0]);
            }
            return null;
        }
    }
}

