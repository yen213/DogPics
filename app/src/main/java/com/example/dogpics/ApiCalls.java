package com.example.dogpics;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Interface class for all the Api calls this application is going to make
 */
public interface ApiCalls {
    // Gets the names of all the available breeds and sub-breeds
    @GET("breeds/list/all")
    Call<BreedInfo> getAllBreed();

    // Gets 70 random images
    @GET("breeds/image/random/70")
    Call<RandomImage> getRandom();

    // Gets 70 pictures of a particular breed that is provided by user at runtime
    @GET("breed/{breed}/images/random/70")
    Call<RandomBreed> getByBreed(@Path("breed") String breed);

    // Gets 70 pictures of a particular breed and sub-breed that is provided by user at runtime
    @GET("breed/{breed}/{subBreed}/images/random/70")
    Call<RandomSubBreed> getBySubBreed(@Path("breed") String breed, @Path("subBreed") String sBreed);
}
