package androks.simplywash.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;

import androks.simplywash.Constants;
import androks.simplywash.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private AuthCallback authCallback;

    @BindView(R.id.auth_button) DigitsAuthButton mAuthButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        implementAuthCallback();
        mAuthButton.setCallback(authCallback);
        mAuthButton.setAuthTheme(R.style.DigitsTheme);
        mAuthButton.performClick();
    }

    private void implementAuthCallback(){
        authCallback = new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                Toast.makeText(getApplicationContext(), "Authentication successful for \n"
                        + phoneNumber, Toast.LENGTH_LONG).show();
                SharedPreferences sp = getSharedPreferences(Constants.AUTH_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString(Constants.AUTH_UUID, session.getPhoneNumber());
                edit.apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void failure(DigitsException exception) {
                Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }
}
