package com.example.dogpics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class to show images for a particular breed/sub-breed or random breeds/sub-breeds on the screen
 * by passing in the images' Urls to the ViewPagerAdapter, which then uses the Urls and the Picasso
 * library to fetch the images.
 */
public class ImageScreenActivity extends AppCompatActivity {
    // Tag used for logging messages to Logcat
    private static final String TAG = MainActivity.class.getName();

    // Member variables for the list of Urls
    private List<String> randomImgUrls = new ArrayList<>();
    private List<String> randomBreedUrls = new ArrayList<>();
    private List<String> randomSubBreedUrls = new ArrayList<>();

    // Data obtained from Intent and ApiCalls object
    private String breed;
    private String subBreed;
    private ApiCalls apiCalls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_screen);

        // Instantiate a Retrofit object with the base URL and generate an implementation of the
        // ApiCalls interface with it
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dog.ceo/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiCalls = retrofit.create(ApiCalls.class);

        // Get the data passed in to the intent from previous screen
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            // Start random image search
            if (intent.getBooleanExtra("RANDOM", false)) {
                getJsonRandom();
            } else {
                breed = bundle.getString("BREED");
                subBreed = bundle.getString("SUB_BREED");
            }
        }

        if (breed != null && subBreed != null) {
            // Start breeds only search
            if (subBreed.equals(getResources().getString(R.string.no_sub_breed))) {
                getJsonBreeds();
            } else {
                // Start breeds and sub-breeds search
                getJsonSubBreeds();
            }
        }
    }

    /**
     * Gets a List of 50 random image URLs from the JSON Object returned by the Api call.
     */
    private void getJsonRandom() {
        // Api call link: https://dog.ceo/api/breeds/image/random/50
        Call<RandomImage> call = apiCalls.getRandom();

        // Asynchronous call to be performed on the background thread
        call.enqueue(new Callback<RandomImage>() {
            @Override
            public void onResponse(@NonNull Call<RandomImage> call,
                                   @NonNull Response<RandomImage> response) {
                // Log and exit function if Api call was unsuccessful
                if (!response.isSuccessful()) {
                    Log.v(TAG, "getJsonRandom() Response Code: " + response.code());
                    return;
                }

                RandomImage randomImage = response.body();

                if (randomImage != null) {
                    for (int i = 0; i < randomImage.getRandomList().size(); i++) {
                        randomImgUrls.add(randomImage.getRandomList().get(i));
                        Log.v(TAG, "Random Image URL: " + randomImgUrls.get(i));
                    }

                    // Provide the URL links for the  ViewPager to use
                    setImagesToScreen(randomImgUrls);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RandomImage> call, @NonNull Throwable t) {
                Log.d(TAG, "getJsonRandom() onFailure: " + t.getMessage());
            }
        });
    }

    /**
     * Gets a List of 50 random image URLs for a particular breed from the JSON Object returned by
     * the Api call.
     */
    public void getJsonBreeds() {
        // Api call link: https://dog.ceo/api/breed/{breed}/images/random/50
        Call<RandomBreed> call = apiCalls.getByBreed(breed);

        // Asynchronous call to be performed on the background thread
        call.enqueue(new Callback<RandomBreed>() {
            @Override
            public void onResponse(@NonNull Call<RandomBreed> call,
                                   @NonNull Response<RandomBreed> response) {
                // Log and exit function if Api call was unsuccessful
                if (!response.isSuccessful()) {
                    Log.v(TAG, "getJsonBreeds() Response Code: " + response.code());
                    return;
                }

                RandomBreed randomBreed = response.body();

                if (randomBreed != null) {
                    for (int i = 0; i < randomBreed.getBreedList().size(); i++) {
                        randomBreedUrls.add(randomBreed.getBreedList().get(i));
                        Log.v(TAG, "Random Breed URL: " + randomBreedUrls.get(i));
                    }
                }

                // Provide the URL links for the  ViewPager to use
                setImagesToScreen(randomBreedUrls);
            }

            @Override
            public void onFailure(@NonNull Call<RandomBreed> call, @NonNull Throwable t) {
                Log.d(TAG, "getJsonBreeds() onFailure: " + t.getMessage());
            }
        });
    }

    /**
     * Gets a List of 50 random image URLs for a particular breed and sub-breed from the JSON Object
     * returned by the Api call.
     */
    public void getJsonSubBreeds() {
        // Api call link: https://dog.ceo/api/breed/{breed}/{subBreed}/images/random/50
        Call<RandomSubBreed> call = apiCalls.getBySubBreed(breed, subBreed);

        // Asynchronous call to be performed on the background thread
        call.enqueue(new Callback<RandomSubBreed>() {
            @Override
            public void onResponse(@NonNull Call<RandomSubBreed> call,
                                   @NonNull Response<RandomSubBreed> response) {
                // Log and exit function if Api call was unsuccessful
                if (!response.isSuccessful()) {
                    Log.v(TAG, "getJsonSubBreeds() Response Code: " + response.code());
                    return;
                }

                RandomSubBreed randomSubBreed = response.body();

                if (randomSubBreed != null) {
                    for (int i = 0; i < randomSubBreed.getSubBreed().size(); i++) {
                        randomSubBreedUrls.add(randomSubBreed.getSubBreed().get(i));
                        Log.v(TAG, "Random Sub-Breed URL: " + randomSubBreedUrls.get(i));
                    }
                }

                // Provide the URL links for the  ViewPager to use
                setImagesToScreen(randomSubBreedUrls);
            }

            @Override
            public void onFailure(@NonNull Call<RandomSubBreed> call, @NonNull Throwable t) {
                Log.d(TAG, "getJsonSubBreeds() onFailure: " + t.getMessage());
            }
        });
    }

    /**
     * Function takes in the list of Urls passed and gets their images and sets them to the
     * ViewPager so that it can show them on the screen.
     *
     * @param urls The list of Urls that was obtained by parsing an appropriate JSON object
     */
    public void setImagesToScreen(List<String> urls) {
        // Shows user dialog informing them they cal swipe left and right
        ImageScreenDialog dialog = new ImageScreenDialog();
        dialog.show(getSupportFragmentManager(), "Dialog");

        ViewPager viewPager = findViewById(R.id.view_pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, urls);
        viewPager.setAdapter(adapter);
    }
}
