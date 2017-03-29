package androks.simplywash.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import androks.simplywash.Constants;
import androks.simplywash.R;
import androks.simplywash.Utils;
import androks.simplywash.fragments.ImageFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PhotosActivity extends AppCompatActivity {
    @BindView(R.id.image_slideshow) ViewPager mPhotosViewPager;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */

    private String mWasherId;
    private List<StorageReference> mPhotoReferences = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);
        ButterKnife.bind(this);
        setUpToolbar();

        mWasherId = getIntent().getExtras().getString(Constants.WASHER_ID);

        downloadPhotoReferences();
    }

    private void downloadPhotoReferences() {
        Utils.getPhotos(mWasherId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> mUrls = dataSnapshot.getValue(
                        new GenericTypeIndicator<List<String>>() {}
                );
                for(String url: mUrls){
                    mPhotoReferences.add(Utils.getPhotoStorageRef(mWasherId).child(url));
                }
                setUpViewPager();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpViewPager() {
        mPhotosViewPager.setAdapter(new PhotosPagerAdapter(getSupportFragmentManager(), mPhotoReferences));
        mPhotosViewPager.setPageTransformer(true, new DepthPageTransformer());
    }

    private void setUpToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class PhotosPagerAdapter extends FragmentStatePagerAdapter {
        private List<StorageReference> mReferences;

        public PhotosPagerAdapter(FragmentManager fm, List<StorageReference> references) {
            super(fm);
            mReferences = references;
        }

        @Override
        public ImageFragment getItem(int position) {
            ImageFragment fragment = new ImageFragment();
            fragment.setImageReference(mReferences.get(position));
            return fragment;
        }

        @Override
        public int getCount() {
            return mReferences.size();
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
