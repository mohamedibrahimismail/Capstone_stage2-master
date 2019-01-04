package com.example.mieib.capstone_stage2;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mieib.capstone_stage2.Models.Movie;
import com.example.mieib.capstone_stage2.Models.MoviesResponse;
import com.example.mieib.capstone_stage2.adapter.MovieAdatpter;
import com.example.mieib.capstone_stage2.contentprovider.MovieProvider;
import com.example.mieib.capstone_stage2.database.MovieContruct;
import com.example.mieib.capstone_stage2.Models.Movie;
import com.example.mieib.capstone_stage2.Models.MoviesResponse;
import com.example.mieib.capstone_stage2.Network.BaseUrls;
import com.example.mieib.capstone_stage2.Network.CheckInternetConnection;
import com.example.mieib.capstone_stage2.Network.rest.ApiClient;
import com.example.mieib.capstone_stage2.Network.rest.ApiInteface;
import com.example.mieib.capstone_stage2.adapter.MovieAdatpter;
import com.example.mieib.capstone_stage2.constants.Constants;
import com.example.mieib.capstone_stage2.contentprovider.MovieProvider;
import com.example.mieib.capstone_stage2.database.MovieContruct;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mieib.capstone_stage2.constants.Constants.ADMOB_APP_ID;
import static com.example.mieib.capstone_stage2.constants.Constants.API_KEY;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "aaaaaaaaaaa";
//    private static final String TAG = MainActivity.class.getSimpleName();
    private BaseUrls baseUrls;
    private ApiInteface apiService;
    private List<Movie> movies = new ArrayList<>();
    private MovieAdatpter movieAdatpter;


    @BindView(R.id.adView) AdView mAdView;
    @BindView(R.id.recyclerview_popular)
    RecyclerView popularRecyclerView;
    @BindView(R.id.recyclerview_top_rated)
    RecyclerView topRatedRecyclerView;
    @BindView(R.id.recyclerview_playing_now)
    RecyclerView playingNowRecyclerView;
    @BindView(R.id.recyclerview_upcoming)
    RecyclerView upcomingRecyclerView;
    @BindView(R.id.recyclerview_favorite)
    RecyclerView favoriteRecyclerView;
    @BindView(R.id.main_layout)
    LinearLayout linearLayout;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.my_toolbar)
    Toolbar myToolbar ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        baseUrls = new BaseUrls();
        apiService = ApiClient.getClient().create(ApiInteface.class);


        initUi();
        if (CheckInternetConnection.isConnected(getApplicationContext())){
        getPopularMovies();
        getTopRatedMovies();
        getNowPlayingMovies();
        getUpcomingMovies();
        initAdMob();
        }else {
            Toasty.error(getApplicationContext(), getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onResume() {
        getSupportLoaderManager().restartLoader(0, null, this);
        super.onResume();
    }

    private void initAdMob(){

        Log.e(TAG,"call ads");
        MobileAds.initialize(this, ADMOB_APP_ID);
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.e(TAG,"error");
                if (errorCode == AdRequest.ERROR_CODE_NETWORK_ERROR){
                    Log.e(TAG,"Check NetworkConntion");
                }else if (errorCode == AdRequest.ERROR_CODE_INVALID_REQUEST){
                    Log.e(TAG,"ad request was invalid");

                }else if (errorCode == AdRequest.ERROR_CODE_INTERNAL_ERROR){
                    Log.e(TAG,"invalid response");

                }else if (errorCode == AdRequest.ERROR_CODE_NO_FILL){
                    Log.e(TAG,"lack of ad inventory");

                }else if (errorCode == AdRequest.GENDER_UNKNOWN){
                    Log.e(TAG,"Unknown Error");
                }
                Log.e(TAG,"errorCode: "+errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.

            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.


            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.


            }
        });
    }

    private void initUi(){
        LinearLayoutManager linearLayoutManagerpopularRecyclerView = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        LinearLayoutManager linearLayoutManagertopRatedRecyclerView = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        LinearLayoutManager linearLayoutManagerplayingNowRecyclerView = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        LinearLayoutManager linearLayoutManagerupcomingRecyclerView = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        LinearLayoutManager linearLayoutManagerfavoriteRecyclerView = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        popularRecyclerView.setLayoutManager(linearLayoutManagerpopularRecyclerView);
        topRatedRecyclerView.setLayoutManager(linearLayoutManagertopRatedRecyclerView);
        playingNowRecyclerView.setLayoutManager(linearLayoutManagerplayingNowRecyclerView);
        upcomingRecyclerView.setLayoutManager(linearLayoutManagerupcomingRecyclerView);
        favoriteRecyclerView.setLayoutManager(linearLayoutManagerfavoriteRecyclerView);
        myToolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(myToolbar);
    }


    public void getPopularMovies(){
        Call<MoviesResponse> call = apiService.getPopularMovies(API_KEY);
        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                movies = response.body().getResults();
                movieAdatpter = new MovieAdatpter(MainActivity.this,movies);
                popularRecyclerView.setAdapter(movieAdatpter);

            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e(TAG,t.getMessage());
            }
        });
    }


    public void getTopRatedMovies(){
        Call<MoviesResponse> call = apiService.getTopRatedMovies(API_KEY);
        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                movies = response.body().getResults();
                movieAdatpter = new MovieAdatpter(MainActivity.this,movies);
                topRatedRecyclerView.setAdapter(movieAdatpter);
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e(TAG,t.getMessage());
            }
        });
    }


    public void getNowPlayingMovies(){
        Call<MoviesResponse> call = apiService.getNowPlayingMovies(API_KEY);
        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                movies = response.body().getResults();
                movieAdatpter = new MovieAdatpter(MainActivity.this,movies);
                playingNowRecyclerView.setAdapter(movieAdatpter);
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e(TAG,t.getMessage()+"");
            }
        });
    }


    public void getUpcomingMovies(){
        Call<MoviesResponse> call = apiService.getUpcomingMovies(API_KEY);
        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                movies = response.body().getResults();
                movieAdatpter = new MovieAdatpter(MainActivity.this,movies);
                upcomingRecyclerView.setAdapter(movieAdatpter);
                progressBar.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e(TAG,t.getMessage());
            }
        });
    }



    @Override
    protected void onStart()
    {
        Log.e("onStart","----------------------------------------");
        super.onStart();
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        Log.e("onCreateLoader","----------------------------------------");


        return new CursorLoader(this,
                MovieProvider.CONTENT_URI,
                null,
                null,
                null,
                null);
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        Log.e(TAG,"Loader Finished------------------------------------------");
        if (data != null && data.getCount() > 0) {
            Log.e("data.getCount()",data.getCount()+"------------------------------------------");
            List<Movie> movies = new ArrayList<>();
            data.moveToFirst();
            do {
                int movieID = data.getInt(data.getColumnIndex(MovieContruct.Favorite.MOVIE_ID));
                String movieTitle = data.getString(data.getColumnIndex(MovieContruct.Favorite.TITLE));
                String moviePosterPath = data.getString(data.getColumnIndex(MovieContruct.Favorite.POSTER_PATH));
                int movieRating = data.getInt(data.getColumnIndex(MovieContruct.Favorite.VOTE_AVERAGE));
                Movie movie = new Movie();
                movie.setId(movieID);
                movie.setPoster_path(moviePosterPath);
                movie.setTitle(movieTitle);
                movie.setVote_average(movieRating);
                movies.add(movie);
            } while (data.moveToNext());
            movieAdatpter = new MovieAdatpter(this, movies);
            favoriteRecyclerView.setAdapter(movieAdatpter);

        } else {

            favoriteRecyclerView.setAdapter(null);


             }

    }


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        Log.e(TAG,"onLoaderReset------------------------------------------");
    }


}
