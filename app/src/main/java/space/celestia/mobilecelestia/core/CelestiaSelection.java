package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CelestiaSelection {
    private final static int SELECTION_TYPE_NIL             = 0;
    private final static int SELECTION_TYPE_STAR            = 1;
    private final static int SELECTION_TYPE_BODY            = 2;
    private final static int SELECTION_TYPE_DEEP_SKY        = 3;
    private final static int SELECTION_TYPE_LOCATION        = 4;
    private final static int SELECTION_TYPE_GENERIC         = 5;

    protected long pointer;

    protected CelestiaSelection(long ptr) {
        pointer = ptr;
    }

    @Nullable
    public static CelestiaSelection create(@NonNull CelestiaAstroObject object) {
        if (object instanceof CelestiaStar) {
            return new CelestiaSelection(c_createSelection(SELECTION_TYPE_STAR, object.pointer));
        } else if (object instanceof CelestiaBody) {
            return new CelestiaSelection(c_createSelection(SELECTION_TYPE_BODY, object.pointer));
        } else if (object instanceof CelestiaDSO) {
            return new CelestiaSelection(c_createSelection(SELECTION_TYPE_DEEP_SKY, object.pointer));
        } else if (object instanceof CelestiaLocation) {
            return new CelestiaSelection(c_createSelection(SELECTION_TYPE_LOCATION, object.pointer));
        }
        return null;
    }

    public boolean isEmpty() {
        return c_isEmpty();
    }

    @Nullable
    public CelestiaAstroObject getObject() {
        int type = c_getSelectionType();
        switch (type) {
            case SELECTION_TYPE_STAR:
                return new CelestiaStar(c_getSelectionPtr());
            case SELECTION_TYPE_LOCATION:
                return new CelestiaLocation(c_getSelectionPtr());
            case SELECTION_TYPE_DEEP_SKY:
                return new CelestiaDSO(c_getSelectionPtr());
            case SELECTION_TYPE_BODY:
                return new CelestiaBody(c_getSelectionPtr());
        }
        return null;
    }

    @Nullable
    public CelestiaStar getStar() {
        CelestiaAstroObject obj = getObject();
        if (obj instanceof CelestiaStar)
            return (CelestiaStar) obj;
        return null;
    }

    @Nullable
    public CelestiaLocation getLocation() {
        CelestiaAstroObject obj = getObject();
        if (obj instanceof CelestiaLocation)
            return (CelestiaLocation) obj;
        return null;
    }

    @Nullable
    public CelestiaDSO getDSO() {
        CelestiaAstroObject obj = getObject();
        if (obj instanceof CelestiaDSO)
            return (CelestiaDSO) obj;
        return null;
    }

    @Nullable
    public CelestiaBody getBody() {
        CelestiaAstroObject obj = getObject();
        if (obj instanceof CelestiaBody)
            return (CelestiaBody) obj;
        return null;
    }

    @Nullable
    public String getWebInfoURL() {
        CelestiaAstroObject object = getObject();
        if (object instanceof CelestiaBody)
            return ((CelestiaBody) object).getWebInfoURL();
        if (object instanceof CelestiaStar)
            return ((CelestiaStar) object).getWebInfoURL();
        if (object instanceof CelestiaDSO)
            return ((CelestiaDSO) object).getWebInfoURL();
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }

    // C functions
    private native boolean c_isEmpty();
    private native int c_getSelectionType();
    private native long c_getSelectionPtr();
    private native String c_getName();
    private native void destroy();

    private static native long c_createSelection(int type, long pointer);
}
