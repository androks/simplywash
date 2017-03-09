package androks.simplywash;

import android.support.multidex.MultiDexApplication;

import com.digits.sdk.android.Digits;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;

/**
 * Created by androks on 3/3/2017.
 */

public class App extends MultiDexApplication {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "fmI0EobyEf3aNZk7Y4koEIffp";
    private static final String TWITTER_SECRET = "rZGOfpm0fLYNBB2wXzENLeQofMBn3LeDI2nmIBkbMf8LWqbSzy";

    @Override
    public void onCreate() {
        super.onCreate();
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().build());
    }
}
