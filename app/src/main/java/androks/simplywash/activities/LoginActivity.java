package androks.simplywash.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import androks.simplywash.R;
import androks.simplywash.models.User;
import androks.simplywash.utils.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity {

    private static final int CHECK_PERMISSIONS_REQUEST = 125;

    @BindView(R.id.auth_button)
    DigitsAuthButton mAuthButton;

    private AuthCallback authCallback;

    private String email;
    private String pass;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        checkIfUserLoggedIn();
        checkPermissions();

        implementAuthCallback();
        initializeAuthButton();
    }

    private void checkPermissions() {
        List<String> permissionNeeded = new ArrayList<>();
        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.RECEIVE_SMS))
            permissionNeeded.add("android.permission.RECEIVE_SMS");
        if (!addPermission(permissionsList, Manifest.permission.READ_PHONE_STATE))
            permissionNeeded.add("android.permission.READ_PHONE_STATE");

        if (!permissionNeeded.isEmpty())
            ActivityCompat.requestPermissions(LoginActivity.this,
                    permissionNeeded.toArray(new String[permissionNeeded.size()]),
                    CHECK_PERMISSIONS_REQUEST);
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(LoginActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permission))
                return false;
        }
        return true;
    }

    private void initializeAuthButton() {
        mAuthButton.setCallback(authCallback);
        mAuthButton.setAuthTheme(R.style.DigitsTheme);
    }

    private void implementAuthCallback() {
        authCallback = new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                SharedPreferences sp = getSharedPreferences(Constants.AUTH_PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString(Constants.PHONE_PREF, phoneNumber);
                edit.apply();
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    signInToFirebase(phoneNumber);
                }
                goToChooseCityAct();
            }

            @Override
            public void failure(DigitsException exception) {
                Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void signInToFirebase(String phoneNumber) {
        email = phoneNumber + "@example.com";
        pass = phoneNumber + "random";
        phone = phoneNumber;
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //If task is to successful try to sign up
                        if (!task.isSuccessful()) {
                            signUpToFirebase();
                        }
                    }
                });
    }

    private void signUpToFirebase() {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                            writeUserToFirebaseDatabase();
                        else {
                            Toast.makeText(LoginActivity.this, R.string.error_while_sign_up, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    private void writeUserToFirebaseDatabase() {
        if (getCurrentUser() != null) {
            String uid = getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                    .setValue(new User(phone)).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(
                            LoginActivity.this,
                            R.string.login_successful,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkIfUserLoggedIn() {
        SharedPreferences sp = getSharedPreferences(Constants.AUTH_PREFERENCES, MODE_PRIVATE);
        String phone = sp.getString(Constants.PHONE_PREF, null);
        String city = sp.getString(Constants.CITY_PREF, null);
        if (FirebaseAuth.getInstance().getCurrentUser() != null && phone != null) {
            if (city != null)
                goToMainAct();
            else
                goToChooseCityAct();
        }
    }

    public void goToChooseCityAct() {
        Intent intent = new Intent(LoginActivity.this, ChooseCityActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToMainAct() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
