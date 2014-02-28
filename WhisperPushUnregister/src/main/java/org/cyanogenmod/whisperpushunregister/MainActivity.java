package org.cyanogenmod.whisperpushunregister;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.cyanogenmod.whisperpushunregister.service.UnregisterService;
import org.cyanogenmod.whisperpushunregister.service.UnregisterService.UnregisterState;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ServiceConnection mServiceConnection = new UnregisterServiceConnection();
    private UnregisterService mService;
    private Handler mStateHandler = new StateHandler();

    private Button mStartButton;
    private TextView mStateText;
    private ProgressBar mProgressBar;
    private TextView mInstructionsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupResources();
        setupServiceBinding();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        teardownServiceBinding();
    }

    private void setupResources() {
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mStateText = (TextView) findViewById(R.id.state_text);
        mInstructionsText = (TextView) findViewById(R.id.instructions);
        mInstructionsText.setMovementMethod(LinkMovementMethod.getInstance());

        mStartButton = (Button) findViewById(R.id.button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartButton.setVisibility(View.GONE);
                mInstructionsText.setVisibility(View.GONE);
                mStateText.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                UnregisterService.start(MainActivity.this);
            }
        });
    }

    private void setupServiceBinding() {
        Intent intent = new Intent(this, UnregisterService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void teardownServiceBinding() {
        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
    }

    private class UnregisterServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = ((UnregisterService.UnregisterServiceBinder)iBinder).getService();
            mService.setStateHandler(mStateHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService.setStateHandler(null);
        }
    }

    private class StateHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            Log.d(TAG, "Unregister State = " + message.what);
            switch (message.what) {
                case UnregisterState.READING_PREFERENCES:
                    mStateText.setText(getString(R.string.state_reading_preferences));
                    break;
                case UnregisterState.UNREGISTERING:
                    mStateText.setText(getString(R.string.state_unregistering));
                    break;
                case UnregisterState.WIPING_DATA:
                    mStateText.setText(getString(R.string.state_wiping));
                    break;
                case UnregisterState.FINISHED:
                    mStateText.setText(getString(R.string.state_finished));
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case UnregisterState.ERROR:
                    mStateText.setText(getString(R.string.state_error));
                    mProgressBar.setVisibility(View.GONE);
                    mStartButton.setVisibility(View.VISIBLE);
                    mInstructionsText.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
