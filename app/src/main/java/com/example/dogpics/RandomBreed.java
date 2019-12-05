package com.example.dogpics;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Parses the 'message' JSON object retrieved from the Api call for a specific breed user entered
 * and puts all the picture URLs of that breed into a List of Strings.
 */
public class RandomBreed {
    @SerializedName("message")
    private List<String> breedList;

    // Getter
    public List<String> getBreedList() {
        return breedList;
    }
}
