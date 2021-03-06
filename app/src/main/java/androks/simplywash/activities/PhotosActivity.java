package androks.simplywash.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.viewpagerindicator.CirclePageIndicator;

import androks.simplywash.R;
import androks.simplywash.adapters.PhotosPagerAdapter;
import androks.simplywash.models.Washer;
import androks.simplywash.utils.Constants;
import androks.simplywash.utils.DepthPageTransformer;
import androks.simplywash.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PhotosActivity extends AppCompatActivity {
    @BindView(R.id.vp_images) ViewPager vpImages;
    @BindView(R.id.images_slideshow_indicator) CirclePageIndicator photosIndicator;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */

    private String washerId;
    private Washer washer;
    private int startId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);
        ButterKnife.bind(this);
        setUpToolbar();

        washerId = getIntent().getExtras().getString(Constants.WASHER_ID);
        startId = getIntent().getExtras().getInt(Constants.PHOTO_INDEX);

        downloadWasher();
    }

    private void downloadWasher() {
        Utils.getWasher(washerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                washer = dataSnapshot.getValue(Washer.class);
                setUpPhotoViewPager();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpPhotoViewPager() {
        vpImages.setAdapter(new PhotosPagerAdapter(
                getSupportFragmentManager(),
                washer,
                R.layout.item_image)
        );
        vpImages.setPageTransformer(true, new DepthPageTransformer());
        vpImages.setCurrentItem(startId);
        photosIndicator.setViewPager(vpImages);
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
}
