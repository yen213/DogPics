package com.example.dogpics;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Parses the 'message' JSON object retrieved from the Api call for a specific breed and sub-breed
 * user entered and puts all the picture URLs of that breed/sub-breed into a List of Strings.
 */
public class RandomSubBreed {
    @SerializedName("message")
    private List<String> subBreed;

    /** Getter */
    public List<String> getSubBreed() {
        return subBreed;
    }
}
