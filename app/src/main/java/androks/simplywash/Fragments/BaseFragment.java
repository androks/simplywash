package androks.simplywash.Fragments;

import android.support.v4.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androks.simplywash.Models.Car;

/**
 * Created by androks on 12/11/2016.
 */

public abstract class BaseFragment extends Fragment {

    protected Car mCurrentCar;
    protected FirebaseUser mCurrentUser;


    BaseFragment(){}

    public FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static DatabaseReference getWasher(){
        return FirebaseDatabase.getInstance().getReference().child("washers");
    }

    public static DatabaseReference getWasher(String id){
        return FirebaseDatabase.getInstance().getReference().child("washers").child(id);
    }

    public static DatabaseReference getStatesOfWashers(){
        return  FirebaseDatabase.getInstance().getReference().child("states");
    }

    public static DatabaseReference getReviewsFor(String washerId){
        return  FirebaseDatabase.getInstance().getReference().child("reviews").child(washerId);
    }

    public static DatabaseReference getUserInfo(String userId){
        return  FirebaseDatabase.getInstance().getReference().child("users").child(userId);
    }

    public static DatabaseReference getPricesFor(String washerId){
        return  FirebaseDatabase.getInstance().getReference().child("prices").child(washerId);
    }
}
