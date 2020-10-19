package com.example.mymovies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymovies.adapters.ReviewAdapter;
import com.example.mymovies.adapters.TrailerAdapter;
import com.example.mymovies.data.FavouriteMovie;
import com.example.mymovies.data.MainViewModel;

import com.example.mymovies.data.TemporaryMovie;
import com.example.mymovies.data.Trailer;
import com.example.mymovies.pojo.Movie;
import com.example.mymovies.pojo.Review;
import com.example.mymovies.pojo.Video;
import com.example.mymovies.utils.JSONUtils;
import com.example.mymovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class DetailActivity extends AppCompatActivity {
    private static final String BASE_YOUTUBE_URL ="https://www.youtube.com/watch?v=";
    private ImageView imageViewBigPoster;
    private TextView textViewTitle;
    private TextView textViewOriginalTitle;
    private TextView textViewRating;
    private TextView textViewReLeaseDate;
    private TextView textViewOverview;
    private ImageView imageViewAddToFavourite;

    private RecyclerView recyclerViewTrailers;
    private RecyclerView recyclerViewReviews;
    private ReviewAdapter reviewAdapter;
    private TrailerAdapter trailerAdapter;

    private int id;
    private String fromParentActivity;
    private Movie movie;
    private FavouriteMovie favouriteMovie;

    private MainViewModel viewModel;

    private static String lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        lang = Locale.getDefault().getLanguage();
        imageViewBigPoster = findViewById(R.id.imageViewBigPoster);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewOriginalTitle = findViewById(R.id.textViewOriginalTitle);
        textViewRating = findViewById(R.id.textViewRating);
        textViewReLeaseDate = findViewById(R.id.textViewReleaseDate);
        textViewOverview = findViewById(R.id.textViewOverView);
        imageViewAddToFavourite = findViewById(R.id.imageViewAddToFavorite);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id") && intent.hasExtra("from"))
        {
            id = intent.getIntExtra("id",-1);
            fromParentActivity = intent.getStringExtra("from");
        }
        else
        {
            finish();
        }
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        if (fromParentActivity.equals("main"))
        {
            movie = viewModel.getMovieById(id);
            if (movie==null)
                    movie=viewModel.getTemporaryMovieById(id);
       //     List<TemporaryMovie> temporaryMovies = viewModel.getListMoreVideos();
        }
        else
        {
            movie = viewModel.getFavouriteMovieById(id);
        }
        Picasso.get()
                .load(movie.getBigPosterPath())
                .placeholder(R.drawable.ic_movie)
                .into(imageViewBigPoster);
        textViewTitle.setText(movie.getTitle());
        textViewOriginalTitle.setText(movie.getOriginalTitle());
        textViewOverview.setText(movie.getOverview());
        textViewReLeaseDate.setText(movie.getReleaseDate());
        textViewRating.setText(String.valueOf(movie.getVoteAverage()));
        setFavorite();
        recyclerViewTrailers = findViewById(R.id.recyclerViewTrailers);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        reviewAdapter = new ReviewAdapter();
        trailerAdapter = new TrailerAdapter();
        trailerAdapter.setOnTrailerClickListener(new TrailerAdapter.OnTrailerClickListener() {
            @Override
            public void onTrailerClick(String url) {
                Intent intentToTrailer = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_YOUTUBE_URL+url));
                startActivity(intentToTrailer);
            }
        });
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTrailers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTrailers.setAdapter(trailerAdapter);
        recyclerViewReviews.setAdapter(reviewAdapter);
//        JSONObject jsonObjectTrailers = NetworkUtils.getJSONForVideos(movie.getId(),lang);
//        JSONObject jsonObjectReviews = NetworkUtils.getJSONForReviews(movie.getId(), lang);
//        ArrayList<Trailer> trailers = JSONUtils.getTrailersFromJSON(jsonObjectTrailers);
//        ArrayList<Review> reviews = JSONUtils.getReviewsFromJSON(jsonObjectReviews);
     //   List<Video> trailers =  viewModel.loadVideosOfMovie(movie.getId(),lang);
        viewModel.getReviewList().observe(this, new Observer<List<Review>>() {
            @Override
            public void onChanged(List<Review> reviews) {
                reviewAdapter.setReviews(reviews);
            }
        });
        viewModel.getVideosList().observe(this, new Observer<List<Video>>() {
            @Override
            public void onChanged(List<Video> videos) {
                trailerAdapter.setTrailers(videos);
            }
        });
       viewModel.loadReviewsOfMovie(movie.getId(),lang);
       viewModel.loadVideosOfMovie(movie.getId(),lang);

    }


    public void onClickChangeFavourite(View view) {

        if (favouriteMovie==null)
        {
            viewModel.insertFavouriteMovie(new FavouriteMovie(movie));
            Toast.makeText(this, R.string.add_favourite, Toast.LENGTH_SHORT).show();
        }
        else
        {
            viewModel.deleteFavouriteMovie(favouriteMovie);
            Toast.makeText(this, R.string.delete_favourite, Toast.LENGTH_SHORT).show();

        }
        setFavorite();

    }

    private void setFavorite()
    {
        favouriteMovie = viewModel.getFavouriteMovieById(id);
        if (favouriteMovie==null)
        {
            imageViewAddToFavourite.setImageResource(android.R.drawable.btn_star_big_off);
        }
        else
        {
            imageViewAddToFavourite.setImageResource(android.R.drawable.btn_star_big_on);
        }
    }
}
