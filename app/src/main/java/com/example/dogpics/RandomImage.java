package com.example.dogpics;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Parses the 'message' JSON object retrieved from the Api call for random picture URLs into a List
 * of Strings.
 */
public class RandomImage {
    @SerializedName("message")
    private List<String> randomList;

    // Getter
    public List<String> getRandomList() {
        return randomList;
    }
}
