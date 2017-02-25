package androks.simplywash;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by androks on 2/24/2017.
 */

public class FirebaseReferences {
    public static DatabaseReference getWasherReference(){
        return FirebaseDatabase.getInstance().getReference().child("washers");
    }

    public static DatabaseReference getWasherReference(String id){
        return FirebaseDatabase.getInstance().getReference().child("washers").child(id);
    }

    public static DatabaseReference getFreeWasherReference(){
        return  FirebaseDatabase.getInstance().getReference().child("free-washers");
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
