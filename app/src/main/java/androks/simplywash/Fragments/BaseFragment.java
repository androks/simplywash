package androks.simplywash.Fragments;

import android.support.v4.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
}
