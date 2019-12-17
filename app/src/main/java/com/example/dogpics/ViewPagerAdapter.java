package com.example.dogpics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Use a ViewPager to display the 50 random or specific breedList/sub-breedList pictures onto the screen.
 */
public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private List<String> imageUrls;
    private List<String> breedList;
    private String breed;

    /**
     * Pass in the activity and List of URLs for the ViewPager to use.
     *
     * @param context   The activity context
     * @param imageUrls The list of URLs for the user selection
     * @param breedList The list of all the breeds
     */
    public ViewPagerAdapter(Context context, List<String> imageUrls, List<String> breedList) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.breedList = breedList;
    }

    /**
     * Pass in the activity and List of URLs for the ViewPager to use.
     *
     * @param context   The activity context
     * @param imageUrls The list of URLs for the user selection
     * @param breed     The breed and sub-breed, if applicable
     */
    public ViewPagerAdapter(Context context, List<String> imageUrls, String breed) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.breed = breed;
    }

    /**
     * Get the total number of elements in the List of Urls.
     */
    @Override
    public int getCount() {
        return imageUrls.size();
    }

    /**
     * Helps ViewPager identify which item belongs to which page.
     *
     * @param view   ImageView returned from instantiateItem()
     * @param object The object returned from instantiateItem()
     * @return True if current picture belongs to current view.
     */
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    /**
     * Create ImageView dynamically and use the Picasso library to load each image from the List of
     * Urls into the ImageView and add that to the ViewPager.
     *
     * @param container The ViewPager
     * @param position  The URL string in the list
     * @return The ImageView with the picture retrieved from the URL.
     */
    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, int position) {
        final ImageView imageView;
        TextView textView;

        final LayoutInflater inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        assert inflater != null;

        final View customerViewPager = inflater.
                inflate(R.layout.viewpager_custom, container, false);

        if (breedList != null && !breedList.isEmpty()) {
            textView = customerViewPager.findViewById(R.id.vp_breed_tv);
            // Capture positions and set to the views
            textView.setText(breedList.get(position));
        } else if (breed != null) {
            textView = customerViewPager.findViewById(R.id.vp_breed_tv);
            // Capture positions and set to the views
            textView.setText(breed);
        }

        // Locate the Views in viewpager_custom layout
        imageView = customerViewPager.findViewById(R.id.vp_image);
        Picasso
                .get()
                .load(imageUrls.get(position))
                .resize(0, 500)
                .error(R.drawable.ic_error_black_24dp)
                .into(imageView);

        // Add viewpager_custom to ViewPager
        container.addView(customerViewPager);

//        container.addView(imageView);

        return customerViewPager;
    }

    /**
     * Remove image from the current view.
     *
     * @param container The ViewPager
     * @param position  The URL string in the list
     * @param object    The ImageView
     */
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }
}
