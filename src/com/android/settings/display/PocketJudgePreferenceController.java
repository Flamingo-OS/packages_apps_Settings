/*
 * Copyright (C) 2018 Paranoid Android
 * Copyright (C) 2022 FlamingoOS Preoject
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.settings.display;

import static android.provider.Settings.System.POCKET_JUDGE;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.TogglePreferenceController;

public final class PocketJudgePreferenceController extends TogglePreferenceController
        implements PreferenceControllerMixin {

    public PocketJudgePreferenceController(
        final Context context,
        final String preferenceKey
    ) {
        super(context, preferenceKey);
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getIntForUser(
            mContext.getContentResolver(), POCKET_JUDGE,
            0, UserHandle.USER_CURRENT) == 1;
    }

    @Override
    public boolean setChecked(final boolean isChecked) {
        return Settings.System.putIntForUser(
            mContext.getContentResolver(), POCKET_JUDGE,
            isChecked ? 1 : 0, UserHandle.USER_CURRENT);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(
            com.android.internal.R.bool.config_pocketModeSupported
        ) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_display;
    }
}
