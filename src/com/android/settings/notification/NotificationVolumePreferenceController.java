/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.notification;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.Utils;

public class NotificationVolumePreferenceController extends
    RingVolumePreferenceController {

    private static final String KEY_NOTIFICATION_VOLUME = "notification_volume";
    private static final String KEY_VOLUME_LINK_NOTIFICATION = "volume_link_notification";

    public NotificationVolumePreferenceController(Context context) {
        super(context, KEY_NOTIFICATION_VOLUME);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(R.bool.config_show_notification_volume)
                && !mHelper.isSingleVolume() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (getAvailabilityStatus() == UNSUPPORTED_ON_DEVICE) {
            return;
        }
        VolumeSeekBarPreference notificationVolume =
                (VolumeSeekBarPreference) screen.findPreference(KEY_NOTIFICATION_VOLUME);
        boolean linked = Settings.Secure.getInt(mContext.getContentResolver(),
                KEY_VOLUME_LINK_NOTIFICATION, 1) == 1;
        notificationVolume.setVisible(linked ? false : true);
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), KEY_NOTIFICATION_VOLUME);
    }

    @Override
    public boolean isPublicSlice() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_NOTIFICATION_VOLUME;
    }

    @Override
    public int getAudioStream() {
        return AudioManager.STREAM_NOTIFICATION;
    }

    @Override
    public int getMuteIcon() {
        return R.drawable.ic_notifications_off_24dp;
    }

}
