package org.cyanogenmod.whisperpushunregister.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.cyanogenmod.whisperpushunregister.PreferenceReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class UnregisterService extends IntentService {

    private static final String TAG = UnregisterService.class.getSimpleName();
    private final Binder mBinder = new UnregisterServiceBinder();
    private Handler mStateHandler;
    private PreferenceReader mPreferenceReader;

    public static void start(Context context) {
        Intent intent = new Intent(context, UnregisterService.class);
        context.startService(intent);
    }

    public UnregisterService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        readPreferences();
        unregister();
    }

    private void updateState(int state) {
        if (mStateHandler != null) {
            mStateHandler.sendEmptyMessage(state);
        }
    }

    private void unregister() {
        updateState(UnregisterState.UNREGISTERING);
        String registeredNumber = mPreferenceReader.getRegisteredNumber();
        String password = mPreferenceReader.getPassword();

        String usernamePassword = registeredNumber + ":" + password;
        Log.d(TAG, "Using username and password combo: " + usernamePassword);
        String basicAuth = Base64.encodeToString(usernamePassword.getBytes(), Base64.NO_WRAP);

        Ion.with(this).load("DELETE", "https://whisperpush.cyanogenmod.org/v1/accounts/gcm")
                .addHeader("Authorization", "Basic " + basicAuth)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception error, Response<String> stringResponse) {
                        if (error != null) {
                            Log.e(TAG, "HTTP Request Error", error);
                            updateState(UnregisterState.ERROR);
                        } else {
                            int responseCode = stringResponse.getHeaders().getResponseCode();
                            Log.d(TAG, "HTTP Result: " + responseCode);
                            if (responseCode != 204) {
                                updateState(UnregisterState.ERROR);
                                return;
                            } else {
                                wipeData();
                            }
                        }
                    }
                });
    }

    private void wipeData() {
        updateState(UnregisterState.WIPING_DATA);

        try {
            Process process = Runtime.getRuntime().exec("/system/bin/su -c /system/bin/sh");
            OutputStream stdin = process.getOutputStream();

            stdin.write(("pm clear org.whispersystems.whisperpush\n").getBytes());
            stdin.write("exit\n".getBytes());
            stdin.flush();

            StringBuilder builder = new StringBuilder();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null && line.length() > 0) {
                Log.d(TAG, "pm output: " + line);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
            throw new RuntimeException(e);
        }

        updateState(UnregisterState.FINISHED);
    }

    private void readPreferences() {
        updateState(UnregisterState.READING_PREFERENCES);
        if (mPreferenceReader == null) mPreferenceReader = new PreferenceReader();
    }

    public void setStateHandler(Handler stateHandler) {
        mStateHandler = stateHandler;
    }

    public class UnregisterServiceBinder extends Binder {
        public UnregisterService getService() {
            return UnregisterService.this;
        }
    }

    public static class UnregisterState {
        public static final int READING_PREFERENCES = 1;
        public static final int UNREGISTERING = 2;
        public static final int WIPING_DATA = 3;
        public static final int FINISHED = 4;
        public static final int ERROR = 5;
    }
}
