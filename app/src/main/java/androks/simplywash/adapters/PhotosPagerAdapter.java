package androks.simplywash.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import androks.simplywash.fragments.PhotoFragment;
import androks.simplywash.models.Washer;

/**
 * Created by androks on 4/1/2017.
 */

public class PhotosPagerAdapter extends FragmentStatePagerAdapter {
    private Washer mWasher;
    private int mResourceId;

    public PhotosPagerAdapter(FragmentManager fm, Washer washer, int resourceId) {
        super(fm);
        mWasher = washer;
        mResourceId = resourceId;
    }

    @Override
    public PhotoFragment getItem(int position) {
        return PhotoFragment.newInstance(
                mResourceId,
                position,
                mWasher.getId(),
                mWasher.getPhotos().get(position)
        );
    }

    @Override
    public int getCount() {
        return mWasher.getPhotos().size();
    }
}
