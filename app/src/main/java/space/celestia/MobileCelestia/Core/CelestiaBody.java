package space.celestia.MobileCelestia.Core;

import androidx.annotation.NonNull;

public class CelestiaBody extends CelestiaAstroObject {
    protected CelestiaBody(long ptr) {
        super(ptr);
    }

    @NonNull
    public String getName() {
        return c_getName();
    }
    @NonNull
    public String getWebInfoURL() {
        return c_getWebInfoURL();
    }

    // C functions
    private native String c_getName();
    private native String c_getWebInfoURL();
}
