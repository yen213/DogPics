package com.example.dogpics;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses the 'message' JSON object retrieved from the Api call and puts the data obtained into a
 * Map containing all the dogs' breeds (key) and sub-breeds (value - List of Strings).
 */
public class BreedInfo {
    @SerializedName("message")
    private Map<String, List<String>> breedInfo = new HashMap<>();

    // Getter
    public Map<String, List<String>> getBreedInfo() {
        return breedInfo;
    }
}
