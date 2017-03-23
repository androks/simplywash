package androks.simplywash.Fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;

import androks.simplywash.Models.Washer;
import androks.simplywash.R;
import androks.simplywash.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteFragment extends Fragment {

    private String mUserUid;

    @BindView(R.id.recyclerLV) RecyclerView mFavoritesRecyclerView;
    @BindView(R.id.empty_list) View mEmptyListTV;


    public FavoriteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        ButterKnife.bind(this, rootView);

        mUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        setUpFavoritesRV();
        return rootView;
    }



    private void setUpFavoritesRV() {
        mFavoritesRecyclerView.setHasFixedSize(true);
        mFavoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFavoritesRecyclerView.setAdapter(new FirebaseIndexRecyclerAdapter<Washer, ViewHolder>(
                Washer.class,
                R.layout.item_favourite,
                ViewHolder.class,
                Utils.getFavourites(mUserUid),
                Utils.getWasher()
        ) {
            @Override
            protected void populateViewHolder(ViewHolder holder, Washer washer, int position) {
                holder.name.setText(washer.getName());
                holder.ratingBar.setRating(washer.getRating());
                holder.location.setText(washer.getLocation());
                holder.openStatus.setText(washer.getState());
                holder.workingTime.setText(Utils.workHoursToString(washer));
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @BindView(R.id.name) TextView name;
        @BindView(R.id.state) TextView state;
        @BindView(R.id.location) TextView location;
        @BindView(R.id.workingTime) TextView workingTime;
        @BindView(R.id.openStatus) TextView openStatus;
        @BindView(R.id.disDurToGetText) TextView resoursesToGet;
        @BindView(R.id.ratingBar) RatingBar ratingBar;
        @BindView(R.id.image) ImageView image;
    }

}
