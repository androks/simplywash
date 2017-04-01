package androks.simplywash.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import androks.simplywash.R;
import androks.simplywash.models.User;
import androks.simplywash.utils.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.auth_button) DigitsAuthButton mAuthButton;

    private AuthCallback authCallback;

    private String email;
    private String pass;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        implementAuthCallback();
        initializeAuthButton();
    }

    private void initializeAuthButton() {
        mAuthButton.setCallback(authCallback);
        mAuthButton.setAuthTheme(R.style.DigitsTheme);
        mAuthButton.performClick();
    }

    private void implementAuthCallback(){
        authCallback = new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                SharedPreferences sp = getSharedPreferences(Constants.AUTH_PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString(Constants.PHONE_PREF, phoneNumber);
                edit.apply();
                if(FirebaseAuth.getInstance().getCurrentUser() == null){
                    signInToFirebase(phoneNumber);
                }
                Intent intent = new Intent(LoginActivity.this, ChooseCityActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void failure(DigitsException exception) {
                Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void signInToFirebase(String phoneNumber){
        email = phoneNumber + "@example.com";
        pass = phoneNumber + "random";
        phone = phoneNumber;
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            signUp();
                        }
                    }
                });
    }

    private void signUp() {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                            writeUserToDatabase();
                        else
                            finish();
                    }
                });
    }

    private void writeUserToDatabase() {
        if(getCurrentUser() != null) {
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
}
