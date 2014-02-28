package org.cyanogenmod.whisperpushunregister;

public class PreferenceReadException extends Exception {
    public PreferenceReadException(String message) {
        super(message);
    }

    public PreferenceReadException(Throwable t) {
        super(t);
    }
}
