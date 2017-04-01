package androks.simplywash.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.google.firebase.storage.StorageReference;

import java.util.List;

import androks.simplywash.fragments.ImageFragment;

/**
 * Created by androks on 4/1/2017.
 */

public class PhotosPagerAdapter extends FragmentStatePagerAdapter {
    private List<StorageReference> mReferences;

    public PhotosPagerAdapter(FragmentManager fm, List<StorageReference> references) {
        super(fm);
        mReferences = references;
    }

    @Override
    public ImageFragment getItem(int position) {
        ImageFragment fragment = new ImageFragment();
        fragment.setImageReference(mReferences.get(position));
        fragment.setId(position);
        return fragment;
    }

    @Override
    public int getCount() {
        return mReferences.size();
    }
}
