package space.celestia.mobilecelestia.utils;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import space.celestia.mobilecelestia.core.CelestiaAppCore;

public class AppStatusReporter implements CelestiaAppCore.ProgressWatcher {
    private static AppStatusReporter singleton = null;
    private ArrayList<Listener> listeners = new ArrayList<>();
    private String currentStatusString = "";

    @NonNull public static AppStatusReporter shared() {
        if (singleton == null)
            singleton = new AppStatusReporter();
        return singleton;
    }

    public interface Listener {
        void celestiaLoadingProgress(@NonNull String status);
        void celestiaLoadingSucceeded();
        void celestiaLoadingFailed();
    }

    @NonNull public String getCurrentStatusString() {
        return currentStatusString;
    }

    public void register(Listener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void unregister(Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onCelestiaProgress(@NonNull String progress) {
        updateStatus(progress);
    }

    public void celestiaLoadResult(boolean success) {
        for (Listener listener : listeners) {
            if (success) {
                listener.celestiaLoadingSucceeded();
            } else {
                listener.celestiaLoadingFailed();
            }
        }
    }

    public void updateStatus(@NonNull String status) {
        currentStatusString = status;
        for (Listener listener : listeners) {
            listener.celestiaLoadingProgress(currentStatusString);
        }
    }
}
