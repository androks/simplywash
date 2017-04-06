package androks.simplywash.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androks.simplywash.R;
import androks.simplywash.activities.PhotosActivity;
import androks.simplywash.models.entity.WasherPhoto;
import androks.simplywash.utils.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PhotoFragment extends Fragment {

    @BindView(R.id.image)
    ImageView mImage;
    @BindView(R.id.progress)
    View mProgressBar;

    private Unbinder mUnbinder;

    private WasherPhoto mPhoto;
    private int mNumber;
    private int mResourceId;
    private String mWasherId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                mResourceId, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        loadPhoto();

        return rootView;
    }

    private void loadPhoto() {
        switch (mPhoto.getTypeAsEnum()) {
            case Firebase:
                StorageReference ref = FirebaseStorage.getInstance().getReference()
                        .child("washer_images")
                        .child(mWasherId)
                        .child(mPhoto.getUrl());
                Glide.with(this)
                        .using(new FirebaseImageLoader())
                        .load(ref)
                        .listener(new RequestListener<StorageReference, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, StorageReference model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                mProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(mImage);
                break;
            case External:
                Glide.with(this)
                        .load(mPhoto.getUrl())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                mProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(mImage);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @OnClick(R.id.image)
    public void viewAllImages() {
        if (mNumber >= 0
                && !(getActivity() instanceof PhotosActivity)
                && mPhoto != null) {
            Intent intent = new Intent(getActivity(), PhotosActivity.class);
            intent.putExtra(Constants.WASHER_ID, mWasherId);
            intent.putExtra(Constants.PHOTO_INDEX, mNumber);
            startActivity(intent);
        }
    }

    public static PhotoFragment newInstance(int resourceId, int number, String id, WasherPhoto photo) {
        PhotoFragment fragment = new PhotoFragment();
        fragment.setResourceId(resourceId);
        fragment.setPhoto(photo);
        fragment.setNumber(number);
        fragment.setWasherId(id);
        return fragment;
    }

    public void setResourceId(int resourceId) {
        mResourceId = resourceId;
    }

    public void setPhoto(WasherPhoto ref) {
        mPhoto = ref;
    }

    public void setNumber(int mNumber) {
        this.mNumber = mNumber;
    }

    public void setWasherId(String mWasherId) {
        this.mWasherId = mWasherId;
    }
}
