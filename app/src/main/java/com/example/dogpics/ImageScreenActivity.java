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
 * Class to display images of dogs on the screen based on the information user chose on the Main
 * Screen.
 */
public class ImageScreenActivity extends AppCompatActivity {
    // Constant for Logging and constants for parsing the URL
    private static final String TAG = MainActivity.class.getName();
    private static final String PREFIX = "breeds/";
    private static final String DELIM = "/";

    // The lists of URLS for the different search types
    private List<String> randomImgUrls = new ArrayList<>();
    private List<String> randomBreedUrls = new ArrayList<>();
    private List<String> randomSubBreedUrls = new ArrayList<>();

    private List<String> breedNameToDisplay = new ArrayList<>();
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

        makeCalls();
    }

    /**
     * Gets the information user chose in the Main Screen and begins the appropriate API calls to
     * fetch and display the images on the screen.
     */
    private void makeCalls() {
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
                // Start breeds search with a specific sub-breed
                getJsonSubBreeds();
            }
        }
    }

    /**
     * Gets a List of 70 random image URLs from the JSON Object returned by the Api call.
     */
    private void getJsonRandom() {
        // Api call link: https://dog.ceo/api/breeds/image/random/70
        Call<RandomImage> call = apiCalls.getRandom();

        // Asynchronous call to be performed on the background thread
        call.enqueue(new Callback<RandomImage>() {
            @Override
            public void onResponse(@NonNull Call<RandomImage> call,
                                   @NonNull Response<RandomImage> response) {
                // Log and exit function if Api call was unsuccessful
                if (!response.isSuccessful()) {
                    Log.d(TAG, "getJsonRandom() Response Code: " + response.code());
                    return;
                }

                RandomImage randomImage = response.body();

                if (randomImage != null) {
                    for (int i = 0; i < randomImage.getRandomList().size(); i++) {
                        randomImgUrls.add(randomImage.getRandomList().get(i));
                        Log.d(TAG, "Random Image Search URL: " + randomImgUrls.get(i));
                    }

                    // Get the name of the breeds/sub-breeds, then provide the URL links for the
                    // ViewPager to use
                    parseUrl();
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
     * Gets a List of 70 random image URLs for a particular breed from the JSON Object returned by
     * the Api call.
     */
    private void getJsonBreeds() {
        // Api call link: https://dog.ceo/api/breed/{breed}/images/random/70
        Call<RandomBreed> call = apiCalls.getByBreed(breed);

        // Asynchronous call to be performed on the background thread
        call.enqueue(new Callback<RandomBreed>() {
            @Override
            public void onResponse(@NonNull Call<RandomBreed> call,
                                   @NonNull Response<RandomBreed> response) {
                // Log and exit function if Api call was unsuccessful
                if (!response.isSuccessful()) {
                    Log.d(TAG, "getJsonBreeds() Response Code: " + response.code());
                    return;
                }

                RandomBreed randomBreed = response.body();

                if (randomBreed != null) {
                    for (int i = 0; i < randomBreed.getBreedList().size(); i++) {
                        randomBreedUrls.add(randomBreed.getBreedList().get(i));
                        Log.d(TAG, "Random Breed Search URL: " + randomBreedUrls.get(i));
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
     * Gets a List of 70 random image URLs for a particular breed and sub-breed from the JSON Object
     * returned by the Api call.
     */
    private void getJsonSubBreeds() {
        // Api call link: https://dog.ceo/api/breed/{breed}/{subBreed}/images/random/70
        Call<RandomSubBreed> call = apiCalls.getBySubBreed(breed, subBreed);

        // Asynchronous call to be performed on the background thread
        call.enqueue(new Callback<RandomSubBreed>() {
            @Override
            public void onResponse(@NonNull Call<RandomSubBreed> call,
                                   @NonNull Response<RandomSubBreed> response) {
                // Log and exit function if Api call was unsuccessful
                if (!response.isSuccessful()) {
                    Log.d(TAG, "getJsonSubBreeds() Response Code: " + response.code());
                    return;
                }

                RandomSubBreed randomSubBreed = response.body();

                if (randomSubBreed != null) {
                    for (int i = 0; i < randomSubBreed.getSubBreed().size(); i++) {
                        randomSubBreedUrls.add(randomSubBreed.getSubBreed().get(i));
                        Log.d(TAG, "Random Sub-Breed URL: " + randomSubBreedUrls.get(i));
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
     * Function takes in the list of Urls passed in and and sets an appropriate ViewPagerAdapter to
     * the ViewPager based on the information user chose on the Main Screen.
     *
     * @param urls List of image URL links.
     */
    private void setImagesToScreen(List<String> urls) {
        // Show user the dialog box
        ImageScreenDialog dialog = new ImageScreenDialog();
        dialog.show(getSupportFragmentManager(), "Dialog");

        ViewPager viewPager = findViewById(R.id.view_pager);
        ViewPagerAdapter adapter;

        if (breed != null && subBreed != null) {
            // Capitalize first letter and pass in the breed name
            if (subBreed.equals(getResources().getString(R.string.no_sub_breed))) {
                breed = breed.substring(0, 1).toUpperCase() + breed.substring(1);

                adapter = new ViewPagerAdapter(ImageScreenActivity.this, urls, breed);
            } else {
                // Capitalize first letters and pass in the breed & sub-breed name
                breed = breed.substring(0, 1).toUpperCase() + breed.substring(1);
                subBreed = subBreed.substring(0, 1).toUpperCase() + subBreed.substring(1);

                adapter = new ViewPagerAdapter
                        (ImageScreenActivity.this, urls, subBreed + " " + breed);
            }
        } else {
            // Pass in the list of random breeds
            adapter = new ViewPagerAdapter
                    (ImageScreenActivity.this, urls, breedNameToDisplay);
        }

        viewPager.setAdapter(adapter);
    }

    /**
     * Parses the random image URLs to get the dog's breed/sub-breed information and store that
     * data in the global variable, which then passes it to the ViewPagerAdapter class to display on
     * the screen.
     */
    private void parseUrl() {
        String dogBreed;

        for (int i = 0; i < randomImgUrls.size(); i++) {
            dogBreed = randomImgUrls.get(i);
            int indexBegin = dogBreed.lastIndexOf(PREFIX) + PREFIX.length();
            int indexEnd = dogBreed.indexOf(DELIM, indexBegin);
            dogBreed = dogBreed.substring(indexBegin, indexEnd);

            if (dogBreed.contains("-")) {
                int splitIndex = dogBreed.indexOf('-');
                String parsedBreed = dogBreed
                        .substring(0, 1)
                        .toUpperCase() + dogBreed.substring(1, splitIndex);
                String parsedSubBreed = dogBreed
                        .substring(splitIndex + 1, splitIndex + 2)
                        .toUpperCase() + dogBreed.substring(splitIndex + 2);

                breedNameToDisplay.add(parsedSubBreed + " " + parsedBreed);
                Log.d(TAG, parsedSubBreed + " " + parsedBreed);
            } else {
                dogBreed = dogBreed.substring(0, 1).toUpperCase() + dogBreed.substring(1);

                breedNameToDisplay.add(dogBreed);
                Log.d(TAG, dogBreed);
            }
        }
    }
}
