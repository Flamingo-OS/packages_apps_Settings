/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.settings.network;

import static com.google.common.truth.Truth.assertThat;

import static junit.framework.Assert.assertEquals;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.slice.Slice;
import androidx.slice.builders.GridRowBuilder;
import androidx.slice.builders.GridRowBuilder.CellBuilder;
import androidx.slice.builders.ListBuilder;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.settings.Utils;
import com.android.settings.slices.CustomSliceable;
import com.android.settings.testutils.ResourcesUtils;
import com.android.settings.wifi.slice.WifiSliceItem;
import com.android.wifitrackerlib.WifiEntry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ProviderModelSliceHelperTest {
    private static final int DEFAULT_SUBID = 1;

    private Context mContext;
    private MockProviderModelSliceHelper mProviderModelSliceHelper;
    private PersistableBundle mBundle;
    private Network mNetwork;
    private NetworkCapabilities mNetworkCapabilities;

    @Mock
    private SubscriptionManager mSubscriptionManager;
    @Mock
    private CarrierConfigManager mCarrierConfigManager;
    @Mock
    private ConnectivityManager mConnectivityManager;
    @Mock
    private TelephonyManager mTelephonyManager;
    @Mock
    private ServiceState mServiceState;
    @Mock
    private WifiSliceItem mWifiSliceItem1;
    @Mock
    private WifiSliceItem mWifiSliceItem2;
    @Mock
    private SubscriptionInfo mDefaultDataSubscriptionInfo;
    @Mock
    private Drawable mDrawableWithSignalStrength;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mContext = spy(ApplicationProvider.getApplicationContext());
        mBundle = new PersistableBundle();
        mNetwork = new Network(anyInt());

        when(mContext.getSystemService(SubscriptionManager.class)).thenReturn(mSubscriptionManager);
        when(mContext.getSystemService(CarrierConfigManager.class)).thenReturn(
                mCarrierConfigManager);
        when(mCarrierConfigManager.getConfigForSubId(anyInt())).thenReturn(mBundle);
        when(mContext.getSystemService(ConnectivityManager.class)).thenReturn(mConnectivityManager);
        when(mConnectivityManager.getActiveNetwork()).thenReturn(mNetwork);
        when(mContext.getSystemService(TelephonyManager.class)).thenReturn(mTelephonyManager);
        when(mTelephonyManager.createForSubscriptionId(anyInt())).thenReturn(mTelephonyManager);

        TestCustomSliceable testCustomSliceable = new TestCustomSliceable();
        mProviderModelSliceHelper = new MockProviderModelSliceHelper(mContext, testCustomSliceable);
    }

    @Test
    public void createMessageGridRow_inputTheResourceId_verifyTitle() {
        int messageResId = ResourcesUtils.getResourcesId(mContext, "string",
                "non_carrier_network_unavailable");
        CharSequence title = ResourcesUtils.getResourcesString(mContext,
                "non_carrier_network_unavailable");

        GridRowBuilder testGridRow = mProviderModelSliceHelper.createMessageGridRow(messageResId,
                Settings.ACTION_AIRPLANE_MODE_SETTINGS);
        List<CellBuilder> cellItem = testGridRow.getCells();

        assertThat(cellItem.get(0).getTitle()).isEqualTo(title);
    }

    @Test
    public void getConnectedWifiItem_inputListInvolveOneConnectedWifiItem_verifyReturnItem() {
        when(mWifiSliceItem1.getConnectedState()).thenReturn(WifiEntry.CONNECTED_STATE_CONNECTED);
        when(mWifiSliceItem2.getConnectedState()).thenReturn(
                WifiEntry.CONNECTED_STATE_DISCONNECTED);
        List<WifiSliceItem> wifiList = new ArrayList<>();
        wifiList.add(mWifiSliceItem1);
        wifiList.add(mWifiSliceItem2);

        WifiSliceItem testItem = mProviderModelSliceHelper.getConnectedWifiItem(wifiList);

        assertThat(testItem).isNotNull();
        assertEquals(mWifiSliceItem1, testItem);
    }

    @Test
    public void getConnectedWifiItem_inputListInvolveNoConnectedWifiItem_verifyReturnItem() {
        when(mWifiSliceItem1.getConnectedState()).thenReturn(
                WifiEntry.CONNECTED_STATE_DISCONNECTED);
        when(mWifiSliceItem2.getConnectedState()).thenReturn(
                WifiEntry.CONNECTED_STATE_DISCONNECTED);
        List<WifiSliceItem> wifiList = new ArrayList<>();
        wifiList.add(mWifiSliceItem1);
        wifiList.add(mWifiSliceItem2);

        WifiSliceItem testItem = mProviderModelSliceHelper.getConnectedWifiItem(wifiList);

        assertThat(testItem).isNull();
    }

    @Test
    public void getConnectedWifiItem_inputNull_verifyReturnItem() {
        List<WifiSliceItem> wifiList = null;

        WifiSliceItem testItem = mProviderModelSliceHelper.getConnectedWifiItem(wifiList);

        assertThat(testItem).isNull();
    }

    @Test
    public void createCarrierRow_hasDdsAndActiveNetworkIsNotCellular_verifyTitleAndSummary() {
        String expectDisplayName = "Name1";
        String expectedSubtitle = "5G";
        String networkType = "5G";

        final int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        when(mDefaultDataSubscriptionInfo.getSubscriptionId()).thenReturn(defaultDataSubId);
        when(mDefaultDataSubscriptionInfo.getDisplayName()).thenReturn(expectDisplayName);
        when(mSubscriptionManager.getActiveSubscriptionInfo(defaultDataSubId)).thenReturn(
                mDefaultDataSubscriptionInfo);
        when(mSubscriptionManager.getAvailableSubscriptionInfoList()).thenReturn(
                Arrays.asList(mDefaultDataSubscriptionInfo));

        when(mServiceState.getState()).thenReturn(ServiceState.STATE_IN_SERVICE);
        mBundle.putBoolean(CarrierConfigManager.KEY_INFLATE_SIGNAL_STRENGTH_BOOL, false);
        addNetworkTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        when(mTelephonyManager.isDataEnabled()).thenReturn(true);


        ListBuilder.RowBuilder testRowBuild = mProviderModelSliceHelper.createCarrierRow(
                networkType);

        assertThat(testRowBuild.getTitle()).isEqualTo(expectDisplayName);
        assertThat(testRowBuild.getSubtitle()).isEqualTo(expectedSubtitle);
    }

    @Test
    public void createCarrierRow_hasDdsAndActiveNetworkIsCellular_verifyTitleAndSummary() {
        String expectDisplayName = "Name1";
        String networkType = "5G";
        String connectedText = ResourcesUtils.getResourcesString(mContext,
                "mobile_data_connection_active");
        String expectedSubtitle = ResourcesUtils.getResourcesString(mContext,
                "preference_summary_default_combination", connectedText, networkType);

        final int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        when(mDefaultDataSubscriptionInfo.getSubscriptionId()).thenReturn(defaultDataSubId);
        when(mDefaultDataSubscriptionInfo.getDisplayName()).thenReturn(expectDisplayName);
        when(mSubscriptionManager.getActiveSubscriptionInfo(defaultDataSubId)).thenReturn(
                mDefaultDataSubscriptionInfo);
        when(mSubscriptionManager.getAvailableSubscriptionInfoList()).thenReturn(
                Arrays.asList(mDefaultDataSubscriptionInfo));
        when(mServiceState.getState()).thenReturn(ServiceState.STATE_IN_SERVICE);
        mBundle.putBoolean(CarrierConfigManager.KEY_INFLATE_SIGNAL_STRENGTH_BOOL, false);
        addNetworkTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);

        ListBuilder.RowBuilder testRowBuild = mProviderModelSliceHelper.createCarrierRow(
                networkType);

        assertThat(testRowBuild.getTitle()).isEqualTo(expectDisplayName);
        assertThat(testRowBuild.getSubtitle()).isEqualTo(expectedSubtitle);
    }

    @Test
    public void isNoCarrierData_mobileDataOnAndNoData_returnTrue() {
        when(mTelephonyManager.isDataEnabled()).thenReturn(true);
        when(mTelephonyManager.getDataState()).thenReturn(mTelephonyManager.DATA_DISCONNECTED);
        when(mTelephonyManager.getServiceState()).thenReturn(mServiceState);
        when(mServiceState.getState()).thenReturn(ServiceState.STATE_IN_SERVICE);

        assertThat(mProviderModelSliceHelper.isNoCarrierData()).isTrue();
    }

    @Test
    public void isNoCarrierData_mobileDataOffAndOutOfService_returnTrue() {
        when(mTelephonyManager.isDataEnabled()).thenReturn(false);
        when(mTelephonyManager.getDataState()).thenReturn(mTelephonyManager.DATA_DISCONNECTED);
        when(mTelephonyManager.getServiceState()).thenReturn(mServiceState);
        when(mServiceState.getState()).thenReturn(ServiceState.STATE_OUT_OF_SERVICE);

        assertThat(mProviderModelSliceHelper.isNoCarrierData()).isTrue();
    }

    @Test
    public void isNoCarrierData_mobileDataOnAndDataConnected_returnFalse() {
        when(mTelephonyManager.isDataEnabled()).thenReturn(true);
        when(mTelephonyManager.getDataState()).thenReturn(mTelephonyManager.DATA_CONNECTED);
        when(mTelephonyManager.getServiceState()).thenReturn(mServiceState);
        when(mServiceState.getState()).thenReturn(ServiceState.STATE_IN_SERVICE);

        assertThat(mProviderModelSliceHelper.isNoCarrierData()).isFalse();
    }

    @Test
    public void isNoCarrierData_mobileDataOffAndVoiceIsInService_returnFalse() {
        when(mTelephonyManager.isDataEnabled()).thenReturn(false);
        when(mTelephonyManager.getDataState()).thenReturn(mTelephonyManager.DATA_DISCONNECTED);
        when(mTelephonyManager.getServiceState()).thenReturn(mServiceState);
        when(mServiceState.getState()).thenReturn(ServiceState.STATE_IN_SERVICE);

        assertThat(mProviderModelSliceHelper.isNoCarrierData()).isFalse();
    }

    @Test
    public void getMobileDrawable_noCarrierData_getMobileDrawable() throws Throwable {
        when(mTelephonyManager.isDataEnabled()).thenReturn(false);
        when(mTelephonyManager.getDataState()).thenReturn(mTelephonyManager.DATA_DISCONNECTED);
        when(mTelephonyManager.getServiceState()).thenReturn(mServiceState);
        when(mServiceState.getState()).thenReturn(ServiceState.STATE_OUT_OF_SERVICE);
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        when(mSubscriptionManager.getActiveSubscriptionInfo(defaultDataSubId)).thenReturn(
                mDefaultDataSubscriptionInfo);
        when(mConnectivityManager.getActiveNetwork()).thenReturn(null);
        Drawable expectDrawable = mock(Drawable.class);

        assertThat(mProviderModelSliceHelper.getMobileDrawable(expectDrawable)).isEqualTo(
                expectDrawable);
    }

    @Test
    public void getMobileDrawable_hasCarrierDataAndDataIsOnCellular_getMobileDrawable()
            throws Throwable {
        when(mTelephonyManager.isDataEnabled()).thenReturn(true);
        when(mTelephonyManager.getDataState()).thenReturn(mTelephonyManager.DATA_CONNECTED);
        when(mTelephonyManager.getServiceState()).thenReturn(mServiceState);
        when(mServiceState.getState()).thenReturn(ServiceState.STATE_IN_SERVICE);
        Drawable drawable = mock(Drawable.class);
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        when(mSubscriptionManager.getActiveSubscriptionInfo(defaultDataSubId)).thenReturn(
                mDefaultDataSubscriptionInfo);
        addNetworkTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);

        assertThat(mProviderModelSliceHelper.getMobileDrawable(drawable)).isEqualTo(
                mDrawableWithSignalStrength);

        verify(mDrawableWithSignalStrength).setTint(Utils.getColorAccentDefaultColor(mContext));
    }

    @Test
    public void getMobileDrawable_hasCarrierDataAndDataIsOnWifi_getMobileDrawable()
            throws Throwable {
        when(mTelephonyManager.isDataEnabled()).thenReturn(true);
        when(mTelephonyManager.getDataState()).thenReturn(mTelephonyManager.DATA_CONNECTED);
        when(mTelephonyManager.getServiceState()).thenReturn(mServiceState);
        when(mServiceState.getState()).thenReturn(ServiceState.STATE_IN_SERVICE);
        Drawable drawable = mock(Drawable.class);
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        when(mSubscriptionManager.getActiveSubscriptionInfo(defaultDataSubId)).thenReturn(
                mDefaultDataSubscriptionInfo);
        addNetworkTransportType(NetworkCapabilities.TRANSPORT_WIFI);

        assertThat(mProviderModelSliceHelper.getMobileDrawable(drawable)).isEqualTo(
                mDrawableWithSignalStrength);
    }

    private void addNetworkTransportType(int networkType) {
        mNetworkCapabilities = new NetworkCapabilities().addTransportType(networkType);
        when(mConnectivityManager.getNetworkCapabilities(mNetwork)).thenReturn(
                mNetworkCapabilities);
    }

    private class TestCustomSliceable implements CustomSliceable {
        TestCustomSliceable() {
        }

        @Override
        public Slice getSlice() {
            return null;
        }

        @Override
        public Uri getUri() {
            return Uri.parse("content://android.settings.slices/action/provider_model");
        }

        @Override
        public Intent getIntent() {
            return new Intent();
        }
    }

    private class MockProviderModelSliceHelper extends ProviderModelSliceHelper {
        MockProviderModelSliceHelper(Context context, CustomSliceable sliceable) {
            super(context, sliceable);
        }

        @Override
        public Drawable getDrawableWithSignalStrength() {
            return mDrawableWithSignalStrength;
        }
    }
}
