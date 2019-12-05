package com.example.dogpics;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Use a ViewPager to display the 50 random or specific breed/sub-breed pictures onto the screen.
 */
public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private List<String> imageUrls;

    /**
     * Pass in the activity and List of URLs for the ViewPager to use.
     *
     * @param context   The activity context
     * @param imageUrls The list of URLs for the user selection
     */
    public ViewPagerAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
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
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        Picasso.get()
                .load(imageUrls.get(position))
                .fit()
                .centerInside()
                .into(imageView);

        container.addView(imageView);

        return imageView;
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
        container.removeView((View) object);
    }
}
