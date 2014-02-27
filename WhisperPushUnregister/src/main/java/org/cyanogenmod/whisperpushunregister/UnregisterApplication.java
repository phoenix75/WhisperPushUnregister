package org.cyanogenmod.whisperpushunregister;

import android.app.Application;
import android.util.Log;

import com.koushikdutta.ion.Ion;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class UnregisterApplication extends Application {

    private static final String TAG = UnregisterApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Setting up Ion trust manager");
        TrustManager[] trustManagers = Util.getTrustManagers(this);
        Ion.getDefault(this)
                .getHttpClient()
                .getSSLSocketMiddleware()
                .setTrustManagers(trustManagers);

        Log.d(TAG, "Setting up Ion SSL Context");
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);
            Ion.getDefault(this)
                    .getHttpClient()
                    .getSSLSocketMiddleware()
                    .setSSLContext(sslContext);
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getSimpleName(), e);
            throw new AssertionError(e);
        }
    }

}
