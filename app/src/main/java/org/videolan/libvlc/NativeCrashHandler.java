/*****************************************************************************
 * NativeCrashHandler.java
 *****************************************************************************
 * Copyright © 2014 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package org.videolan.libvlc;

import android.os.Build;
import android.util.Log;

public class NativeCrashHandler {
    public final static String TAG = "VLC/NativeCrashHandler";

    /** Native crash handler */
    private OnNativeCrashListener mOnNativeCrashListener;

    private static NativeCrashHandler sInstance;

    private NativeCrashHandler() {
    }

    public static interface OnNativeCrashListener {
        public void onNativeCrash();
    }

    public static NativeCrashHandler getInstance() {
        synchronized (NativeCrashHandler.class) {
            if (sInstance == null) {
                /* First call */
                sInstance = new NativeCrashHandler();
                if (LibVlcUtil.isLibraryLoaded())
                    sInstance.nativeInit();
            }
        }

        return sInstance;
    }

    public void setOnNativeCrashListener(OnNativeCrashListener l) {
        mOnNativeCrashListener = l;
    }

    public void onNativeCrash() {
        if (mOnNativeCrashListener != null)
            mOnNativeCrashListener.onNativeCrash();
    }

    private native void nativeInit();
}