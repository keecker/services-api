/**
 * Copyright (C) 2016 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Contributors: Cyril Lugan
 */
package com.keecker.services.interfaces.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class should not be manipulated directly (use IpcPublisher/IpcSubscriber).
 *
 * Parcelable can't be directly used with AIDL, we have to provide a class with a CREATOR defined.
 * That means we should declare an AIDL interface like IInterprocessSubscriber for each message
 * for each message type. To avoid this, we use a generic Parcelable like a Bundle.
 *
 * The IpcMessage CREATOR copies the Parcel from the binder in another Parcel,
 * which is later unmarshalled by our real message CREATOR.
 *
 * Similar implementation can be found in the Bundle class. We do not use it because it seems to
 * use reflection on each received message (when using setClassLoader). In our case we can directly
 * provide the message CREATOR.
 *
 * This class is only needed because we want an AIDL interface for the subscriber.
 * We could avoid it and all the complicated Parcel copy and recyling by using a Binder object
 * see git review 2983 (Change-Id: I1e05fbdf336abdabd47c0140b5039c632882ecc5)
 */
public class IpcMessage implements Parcelable {
    private Parcel mMessageParcel = null;
    private final int mSignature;
    private final boolean mHasBeenContructedWithAParcel;

    public static final Parcelable.Creator<IpcMessage> CREATOR = new Parcelable.Creator<IpcMessage>() {
        public IpcMessage createFromParcel(Parcel in) {
            return new IpcMessage(in);
        }

        public IpcMessage[] newArray(int size) {
            return new IpcMessage[size];
        }
    };

    public IpcMessage(Parcelable payload, int signature) {
        mMessageParcel = Parcel.obtain();
        payload.writeToParcel(mMessageParcel, 0);
        mMessageParcel.setDataPosition(0);
        mSignature = signature;
        mHasBeenContructedWithAParcel = false;
    }

    public IpcMessage(Parcel parcel) {
        int length = parcel.readInt();
        if (length < 0) {
            throw new RuntimeException("Bad length in parcel: " + length);
        }
        mSignature = parcel.readInt();
        // Copied from Bundle::readFromParcelInner
        int offset = parcel.dataPosition();
        parcel.setDataPosition(offset + length);
        mMessageParcel = Parcel.obtain();
        mMessageParcel.setDataPosition(0);
        mMessageParcel.appendFrom(parcel, offset, length);
        mMessageParcel.setDataPosition(0);
        mHasBeenContructedWithAParcel = true;
    }

    public boolean hasBeenContructedWithAParcel() {
        return mHasBeenContructedWithAParcel;
    }

    /**
     * @return An ipc message, as if it was received from another process
     */
    public IpcMessage makeWithAParcel() {
        Parcel parcelledIpcMsg = Parcel.obtain();
        try {
            writeToParcel(parcelledIpcMsg, 0);
            parcelledIpcMsg.setDataPosition(0);
            return new IpcMessage(parcelledIpcMsg);
        } finally {
            parcelledIpcMsg.recycle();
        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        int length = mMessageParcel.dataSize();
        parcel.writeInt(length);
        parcel.writeInt(mSignature);
        parcel.appendFrom(mMessageParcel, 0, length);
    }

    public <T extends Parcelable> T getPayload(Parcelable.Creator<T> parcelableCreator) {
        return parcelableCreator.createFromParcel(mMessageParcel);
    }

    public int getSignature() {
        return mSignature;
    }

    static public <T extends Parcelable> int getClassSignature(Class<T> msgClass) {
        return msgClass.getCanonicalName().hashCode();
    }

    // Recycles the Parcel we obtained when no longer needed
    // IpcMessage cannot be used after calling recycle
    public void recycle() {
        mMessageParcel.recycle();
        mMessageParcel = null;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
