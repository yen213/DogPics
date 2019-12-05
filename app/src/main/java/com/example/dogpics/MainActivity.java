package com.example.dogpics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Main screen where user enter the breed/sub-breed of a dog they want to see pictures of or of
 * random breeds/sub-breeds. Passes the user selected option(s) to the ImageScreenActivity class
 * through intent. Class makes only one Api call during the app lifecycle to fetch the available
 * breeds and sub-breeds.
 */
public class MainActivity extends AppCompatActivity {
    // Tag used for logging messages to Logcat
    private static final String TAG = MainActivity.class.getName();

    // Api call related data and user inputs
    private BreedsSubBreedsInfoVM breedsSubBreedsInfoVM;
    private List<String> breeds = new ArrayList<>();
    private List<String> subBreeds = new ArrayList<>();
    private ApiCalls apiCalls;
    private String userInputBreed;
    private String userInputSubBreed;
    private Intent intent;

    // Views on the activity screen
    private AutoCompleteTextView breedACTxtView;
    private AutoCompleteTextView subACTxtView;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the views to member variables
        breedACTxtView = findViewById(R.id.breed_ac);
        searchButton = findViewById(R.id.search);
        subACTxtView = findViewById(R.id.sub_breed_ac);
        Button randomButton = findViewById(R.id.random_search);

        intent = new Intent(this, ImageScreenActivity.class);

        // Instantiate a Retrofit object with the base URL and generate an implementation of the
        // ApiCalls interface with it
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dog.ceo/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiCalls = retrofit.create(ApiCalls.class);

        breedsSubBreedsInfoVM = ViewModelProviders.of(this).get(BreedsSubBreedsInfoVM.class);

        // Start ImageScreenActivity with random dog pictures
        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("RANDOM", true);
                startActivity(intent);
            }
        });

        // Get all the available breeds and sub-breeds
        getJsonBreedInfo();
    }

    /**
     * Gets all the breeds and sub-breeds from the JSON Object returned by the api call and stores
     * the data in the persistent BreedsSubBreedsInfoVM object. The JSON object returned by this
     * call contains multiple key value pairs where the values are JSON arrays of Strings.
     */
    private void getJsonBreedInfo() {
        // Only make the call to retrieve the breed information once during the lifecycle of the app
        if (breedsSubBreedsInfoVM.getBreedInfoMap() != null) {
            getUserBreedInfo();
            return;
        }

        // Api call link: https://dog.ceo/api/breeds/list/all
        Call<BreedInfo> call = apiCalls.getAllBreed();

        // Asynchronous call to be performed on the background thread
        call.enqueue(new Callback<BreedInfo>() {
            @Override
            public void onResponse(@NonNull Call<BreedInfo> call,
                                   @NonNull Response<BreedInfo> response) {
                // Log and exit function if Api call was unsuccessful
                if (!response.isSuccessful()) {
                    Log.v(TAG, "getJsonBreedInfo() Response Code: " + response.code());
                    return;
                }

                BreedInfo breedInfo = response.body();
                Map<String, List<String>> breedInfoMap = new HashMap<>();

                if (breedInfo != null) {
                    for (String key : (breedInfo.getBreedInfo()).keySet()) {
                        breedInfoMap.put(key, (breedInfo.getBreedInfo()).get(key));
                        Log.v(TAG, "Key: " + key + "\tValue: " + breedInfoMap.get(key));
                    }

                    breedsSubBreedsInfoVM.setBreedInfoMap(breedInfoMap);
                    getUserBreedInfo();

                    // Start the activity to show the images for a breed/sub-breed if user entered
                    // the information correctly, otherwise inform them of incorrect spelling.
                    searchButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            subACTxtView.setEnabled(false);
                            userInputSubBreed = subACTxtView.getText().toString().trim();
                            Log.v(TAG, userInputBreed + "   " + userInputSubBreed);

                            if (breeds.contains(userInputBreed) && (subBreeds.contains(userInputSubBreed) ||
                                    userInputSubBreed.equals(getResources().getString(R.string.no_sub_breed)))) {
                                intent.putExtra("RANDOM", false);
                                intent.putExtra("BREED", userInputBreed);
                                intent.putExtra("SUB_BREED", userInputSubBreed);
                                startActivity(intent);
                            } else {
                                Log.v(TAG, "sub: " + userInputSubBreed);
                                subACTxtView.setError(getResources().getString(R.string.error_message));
                                subACTxtView.setEnabled(true);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<BreedInfo> call, @NonNull Throwable t) {
                Log.d(TAG, "getJsonBreedInfo() onFailure: " + t.getMessage());
            }
        });
    }

    /**
     * Populate the AutoCompleteTextView with the available breeds retrieved from the Api call
     * earlier.
     */
    private void getUserBreedInfo() {
        final Map<String, List<String>> breedsMap = breedsSubBreedsInfoVM.getBreedInfoMap();
        breeds.addAll(breedsMap.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                breeds);

        breedACTxtView.setAdapter(adapter);

        // Check if user entered breed information correctly. Change View states depending on user
        // input.
        breedACTxtView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    searchButton.setEnabled(false);
                    subACTxtView.setEnabled(true);
                } else {
                    userInputBreed = breedACTxtView.getText().toString().trim();

                    if (userInputBreed.length() > 2 && breedsMap.containsKey(userInputBreed)) {
                        getUserSubBreedInfo();
                    } else if (userInputBreed.length() > 2) {
                        breedACTxtView.setError(getResources().getString(R.string.error_message));
                    }
                }
            }
        });

    }

    /**
     * Populate the AutoCompleteTextView with the list of sub-breeds for the breed user selected
     * previously, set the sub-breed selection to none if no sub-breeds available.
     */
    private void getUserSubBreedInfo() {
        subBreeds = breedsSubBreedsInfoVM.getBreedInfoMap().get(userInputBreed);
        String none = getResources().getString(R.string.no_sub_breed);

        // Show dropdown of the sub-breeds selection
        subACTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subACTxtView.showDropDown();
            }
        });

        if (subBreeds != null && subBreeds.isEmpty()) {
            subACTxtView.setText(none);
            userInputSubBreed = none;
            searchButton.setEnabled(true);
            subACTxtView.setEnabled(false);
            return;
        }

        Log.v(TAG, "Outside Empty");

        if (!subBreeds.contains(none)) {
            subBreeds.add(none);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                subBreeds);

        subACTxtView.setAdapter(adapter);
        searchButton.setEnabled(true);
        subACTxtView.showDropDown();

        Log.v(TAG, "Sub: " + subBreeds);
    }
}