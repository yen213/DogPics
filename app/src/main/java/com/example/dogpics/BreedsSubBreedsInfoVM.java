package com.example.dogpics;

import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Map;

/**
 * Persistent storage class of the list of available dog breeds and sub-breeds obtained from
 * https://dog.ceo/api/breeds/list/all. Class used to save the list data so that only one Api call
 * is made to retrieve this information during the apps lifecycle.
 */
public class BreedsSubBreedsInfoVM extends ViewModel {
    private Map<String, List<String>> breedInfoMap;

    // Getter and setter
    public Map<String, List<String>> getBreedInfoMap() {
        return breedInfoMap;
    }

    public void setBreedInfoMap(Map<String, List<String>> breedInfoMap) {
        this.breedInfoMap = breedInfoMap;
    }
}
