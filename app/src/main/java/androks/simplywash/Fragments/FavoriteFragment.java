package androks.simplywash.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;

import androks.simplywash.Activities.WasherActivity;
import androks.simplywash.Constants;
import androks.simplywash.Models.Washer;
import androks.simplywash.R;
import androks.simplywash.Utils;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteFragment extends Fragment{

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
        setHasOptionsMenu(true);
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
            protected void populateViewHolder(ViewHolder holder, final Washer washer, int position) {
                hideProgress();
                holder.name.setText(washer.getName());
                holder.ratingBar.setRating(washer.getRating());
                holder.location.setText(washer.getLocation());
                holder.openStatus.setText(washer.getState());
                holder.state.setText(washer.getState());
                holder.workingTime.setText(Utils.workHoursToString(washer));
                if (Utils.isWasherOpenAtTheTime(washer)) {
                    holder.openStatus.setText("Open");
                    holder.openStatus.setTextColor(holder.green);
                } else {
                    holder.openStatus.setText("Closed");
                    holder.openStatus.setTextColor(holder.red);
                }
                holder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), WasherActivity.class);
                        intent.putExtra(Constants.WASHER_ID, washer.getId());
                        startActivity(intent);
                    }
                });
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @BindView(R.id.container) View container;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.state) TextView state;
        @BindView(R.id.location) TextView location;
        @BindView(R.id.workingTime) TextView workingTime;
        @BindView(R.id.openStatus) TextView openStatus;
        @BindView(R.id.ratingBar) RatingBar ratingBar;
        @BindView(R.id.image) ImageView image;

        @BindColor(R.color.green) int green;
        @BindColor(R.color.red) int red;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }

    private void showProgress(){
        mEmptyListTV.setVisibility(View.VISIBLE);
    }

    private void hideProgress(){
        mEmptyListTV.setVisibility(View.GONE);
    }
}
