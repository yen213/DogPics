package com.example.dogpics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

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
 * Main screen where user enters the breed/sub-breed of a dog they want to see pictures of or of
 * random dog pictures. Passes the user selected option(s) to the ImageScreenActivity class
 * through Intent. This class makes only one Api call during the app lifecycle to fetch the
 * available breeds and sub-breeds from the website.
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

    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver receiver;

    // Connection flag and no connection message
    private static boolean isConnected = false;
    private static String no_internet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the views to member variables
        breedACTxtView = findViewById(R.id.breed_ac);
        searchButton = findViewById(R.id.search);
        subACTxtView = findViewById(R.id.sub_breed_ac);

        breedsSubBreedsInfoVM = ViewModelProviders.of(this).get(BreedsSubBreedsInfoVM.class);

        no_internet = getResources().getString(R.string.no_internet);

        // Register BroadcastReceiver to track connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);
        intent = new Intent(this, ImageScreenActivity.class);

        checkInternetConnection();

        // Instantiate a Retrofit object with the base URL and generate an implementation of the
        // ApiCalls interface with it
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dog.ceo/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiCalls = retrofit.create(ApiCalls.class);

        Button randomButton = findViewById(R.id.random_search);

        // Start ImageScreenActivity with random dog pictures
        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    intent.putExtra("RANDOM", true);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), no_internet, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Unregister the receiver when the activity gets destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    /**
     * Checks to see if user is connected to the internet when app first starts up and sets the
     * connection flag appropriately.
     */
    private void checkInternetConnection() {
        ConnectivityManager cManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;

        if (cManager != null) {
            networkInfo = cManager.getActiveNetworkInfo();
        }

        isConnected = (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Inner class which extends BroadcastReceiver and intercepts the
     * android.net.ConnectivityManager.CONNECTIVITY_ACTION, which indicates a connection change.
     * Sets the global connection flag based on connection status and starts the api call when
     * internet connection is found.
     */
    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connMgr =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;

            if (connMgr != null) {
                networkInfo = connMgr.getActiveNetworkInfo();
            }

            // Checks to see if the device has an internet connection
            if (networkInfo != null) {
                isConnected = true;

                if (breedsSubBreedsInfoVM.getBreedInfoMap() == null) {
                    getJsonBreedInfo();
                }
            } else {
                isConnected = false;
                Toast.makeText(context, no_internet, Toast.LENGTH_SHORT).show();
            }
        }
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
                    Log.d(TAG, "getJsonBreedInfo() Response Code: " + response.code());
                    return;
                }

                BreedInfo breedInfo = response.body();
                Map<String, List<String>> breedInfoMap = new HashMap<>();

                if (breedInfo != null) {
                    for (String key : (breedInfo.getBreedInfo()).keySet()) {
                        breedInfoMap.put(key, (breedInfo.getBreedInfo()).get(key));
                        Log.d(TAG, "Key: " + key + "\tValue: " + breedInfoMap.get(key));
                    }

                    breedsSubBreedsInfoVM.setBreedInfoMap(breedInfoMap);
                    getUserBreedInfo();

                    // Start the activity to show the images for a breed/sub-breed if user entered
                    // the information correctly, otherwise inform them of incorrect spelling.
                    searchButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isConnected) {
                                subACTxtView.setEnabled(false);
                                userInputSubBreed = subACTxtView.getText().toString().trim();
                                Log.d(TAG, userInputBreed + "   " + userInputSubBreed);

                                if (breeds.contains(userInputBreed) && (subBreeds.contains(userInputSubBreed) ||
                                        userInputSubBreed.equals(getResources().getString(R.string.no_sub_breed)))) {
                                    intent.putExtra("RANDOM", false);
                                    intent.putExtra("BREED", userInputBreed);
                                    intent.putExtra("SUB_BREED", userInputSubBreed);
                                    startActivity(intent);
                                } else {
                                    Log.d(TAG, "sub: " + userInputSubBreed);
                                    subACTxtView.setError(getResources().getString(R.string.error_message));
                                    subACTxtView.setEnabled(true);
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        no_internet, Toast.LENGTH_LONG).show();
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
     * Populate the Breed AutoCompleteTextView with the available breeds retrieved from the Api call
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
     * Populate the Sub-Breed AutoCompleteTextView, if applicable, set the sub-breed selection to
     * none if no sub-breeds available.
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

        // For no available sub-breeds
        if (subBreeds != null && subBreeds.isEmpty()) {
            subACTxtView.setText(none);
            userInputSubBreed = none;
            searchButton.setEnabled(true);
            subACTxtView.setEnabled(false);
            return;
        }

        if (subBreeds != null) {
            // Add the none option to list suggestion list only once
            if (!subBreeds.contains(none)) {
                subBreeds.add(none);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    subBreeds);

            subACTxtView.setAdapter(adapter);
            searchButton.setEnabled(true);
            subACTxtView.showDropDown();

            Log.d(TAG, "Sub: " + subBreeds);
        }
    }
}