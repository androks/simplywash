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
import com.google.firebase.storage.StorageReference;

import androks.simplywash.Constants;
import androks.simplywash.R;
import androks.simplywash.activities.PhotosActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by androks on 3/29/2017.
 */

public class ImageFragment extends Fragment {

    @BindView(R.id.image) ImageView image;
    @BindView(R.id.progress) View progressBar;

    private Unbinder unbinder;

    private StorageReference photoReference;
    private int id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.item_image, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(photoReference)
                .listener(new RequestListener<StorageReference, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, StorageReference model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(image);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.image)
    public void viewAllImages(){
        if(id >= 0 && !(getActivity() instanceof PhotosActivity)){
            Intent intent = new Intent(getActivity(), PhotosActivity.class);
            intent.putExtra(Constants.WASHER_ID, photoReference.getParent().getName());
            intent.putExtra(Constants.PHOTO_INDEX, id);
            startActivity(intent);
        }
    }

    public void setId(int id){
        this.id = id;
    }

    public void setImageReference(StorageReference ref){
        photoReference = ref;
    }
}
