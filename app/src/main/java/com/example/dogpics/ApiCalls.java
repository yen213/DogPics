package com.example.dogpics;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Interface class for all the Api calls the application is going to make
 */
public interface ApiCalls {
    // Gets all the available breeds and sub-breeds
    @GET("breeds/list/all")
    Call<BreedInfo> getAllBreed();

    // Gets 50 random images
    @GET("breeds/image/random/50")
    Call<RandomImage> getRandom();

    // Gets 50 pictures of a particular breed that is provided at runtime
    @GET("breed/{breed}/images/random/50")
    Call<RandomBreed> getByBreed(@Path("breed") String breed);

    // Gets 50 pictures of a particular breed and sub-breed that is provided at runtime
    @GET("breed/{breed}/{subBreed}/images/random/50")
    Call<RandomSubBreed> getBySubBreed(@Path("breed") String breed, @Path("subBreed") String sBreed);
}
