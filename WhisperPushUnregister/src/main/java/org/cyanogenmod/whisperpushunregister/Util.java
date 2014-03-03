package org.cyanogenmod.whisperpushunregister;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class Util {
    private static final String TAG = Util.class.getSimpleName();
    private static final String TRUST_STORE_PASSWORD = "whisper";

    public static String findSuBinary() {
        File xbin = new File("/system/xbin/su");
        File bin = new File("/system/bin/su");

        if (xbin.exists() && !xbin.isDirectory()) {
            return xbin.getAbsolutePath();
        }

        if (bin.exists() && !bin.isDirectory()) {
            return bin.getAbsolutePath();
        }

        throw new RuntimeException("Unable to find su binary");
    }

    public static TrustManager[] getTrustManagers(Context context) {
        try {
            KeyStore keyStore = KeyStore.getInstance("BKS");
            InputStream inputStream = context.getResources().openRawResource(R.raw.whisper);
            keyStore.load(inputStream, TRUST_STORE_PASSWORD.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
            trustManagerFactory.init(keyStore);
            return trustManagerFactory.getTrustManagers();
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getSimpleName(), e);
            throw new AssertionError(e);
        }
    }
}
