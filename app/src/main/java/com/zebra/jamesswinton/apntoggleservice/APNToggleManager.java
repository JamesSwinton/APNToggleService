package com.zebra.jamesswinton.apntoggleservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;

import static com.zebra.jamesswinton.apntoggleservice.App.INTERNET_APN_PROFILE_NAME;
import static com.zebra.jamesswinton.apntoggleservice.App.INTERNET_APN_PROFILE_XML;
import static com.zebra.jamesswinton.apntoggleservice.App.MOBILEDADE_APN_PROFILE_NAME;
import static com.zebra.jamesswinton.apntoggleservice.App.MOBILEDADE_APN_PROFILE_XML;

public class APNToggleManager implements EMDKManager.EMDKListener, ProcessProfile.OnProfileApplied {

    // Debugging
    private static final String TAG = "APNToggleManager";

    // APN Enum
    public enum ApnType { MOBILEDADE, INTERNET }

    // Preference Constants
    private static final String MAPPING_STATE_PREFS = "mapping-state-prefs";
    private static final String IS_CUSTOM_STATE = "is-custom-state";

    // EMDK Variables
    private EMDKManager mEmdkManager = null;
    private ProfileManager mProfileManager = null;

    // Type Holder
    private ApnType mApnTypeToApply = ApnType.INTERNET;

    // Context
    private Context mContext;
    private OnApnToggledListener mOnApnToggledListener;

    public APNToggleManager(Context context) {
        this.mContext = context;
    }

    public void toggleApn(ApnType apnTypeToApply, OnApnToggledListener onApnToggledListener) {
        this.mApnTypeToApply = apnTypeToApply;
        this.mOnApnToggledListener = onApnToggledListener;
        initEMDK();
    }

    private void initEMDK() {
        EMDKResults emdkManagerResults = EMDKManager.getEMDKManager(mContext, this);
        if (emdkManagerResults == null || emdkManagerResults.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            Log.e(TAG, "onCreate: Failed to get EMDK Manager -> " +
                    (emdkManagerResults == null ? "No Results Returned"
                            : emdkManagerResults.statusCode));
            Toast.makeText(mContext, "Failed to get EMDK Manager!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        // Assign EMDK Reference
        mEmdkManager = emdkManager;

        // Get Profile & Version Manager Instances
        mProfileManager = (ProfileManager) mEmdkManager.getInstance(EMDKManager.FEATURE_TYPE.PROFILE);

        // Apply Profile
        if (mProfileManager != null) {
            applyCustomXml();
        } else {
            Log.e(TAG, "Error Obtaining ProfileManager!");
            Toast.makeText(mContext, "Error Obtaining ProfileManager!", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onClosed() {
        // Release EMDK Manager Instance
        if (mEmdkManager != null) {
            mEmdkManager.release();
            mEmdkManager = null;
        }
    }

    private void applyCustomXml() {
        String[] params = new String[1];
        if (mApnTypeToApply == ApnType.INTERNET) {
            params[0] = INTERNET_APN_PROFILE_XML;
            new ProcessProfile(INTERNET_APN_PROFILE_NAME, mProfileManager, this)
                    .execute(params);
        } else {
            params[0] = MOBILEDADE_APN_PROFILE_XML;
            new ProcessProfile(MOBILEDADE_APN_PROFILE_NAME, mProfileManager, this)
                    .execute(params);
        }
    }

    @Override
    public void profileApplied() {
        // Notify
        mOnApnToggledListener.onApnToggled(mApnTypeToApply);

        // Notify User
        Toast.makeText(mContext, "APN Applied: " + mApnTypeToApply.name(), Toast.LENGTH_SHORT).show();

        // Release EMDK
        mEmdkManager.release();
        mEmdkManager = null;
    }

    @Override
    public void profileError() {
        Log.e(TAG, "Error Processing APN Profile!");
        Toast.makeText(mContext, "Error Applying Profile!", Toast.LENGTH_SHORT).show();

        // Release EMDK
        mEmdkManager.release();
        mEmdkManager = null;
    }

    public interface OnApnToggledListener {
        void onApnToggled(ApnType apnType);
        void onApnToggleFailed();
    }

}
