/*
* Copyright (C) 2013 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.omnirom.device;

import android.os.Bundle;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.TwoStatePreference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.MenuItem;

import android.preference.MultiSelectListPreference;

import android.preference.ListPreference;

import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;


public class DeviceSettings extends PreferenceActivity implements OnPreferenceChangeListener {

    public static final String KEY_DISABLER = "key_disabler";
    public static final String KEY_CAMERA_SWITCH = "camera";
    public static final String KEY_MUSIC_SWITCH = "music";
    public static final String KEY_TORCH_SWITCH = "torch";
    public static final String KEY_VIBSTRENGTH = "vib_strength";

    private static final String KEY_HAPTIC_FEEDBACK = "touchscreen_gesture_haptic_feedback";

    private static final String KEY_TORCH_LAUNCH_INTENT = "touchscreen_gesture_torch_launch_intent";  
    private static final String KEY_PLAY_PAUSE_LAUNCH_INTENT = 
			"touchscreen_gesture_play_pause_launch_intent";  
    private static final String KEY_PREVIOUS_LAUNCH_INTENT = 
			"touchscreen_gesture_previous_launch_intent";  
    private static final String KEY_NEXT_LAUNCH_INTENT = "touchscreen_gesture_next_launch_intent";

    private TwoStatePreference mCameraSwitch;
    private TwoStatePreference mMusicSwitch;
    private TwoStatePreference mTorchSwitch;
    private VibratorStrengthPreference mVibratorStrength;

    private MultiSelectListPreference mHapticFeedback;
    private ListPreference mTorchLaunchIntent;
    private ListPreference mPlayPauseLaunchIntent;
    private ListPreference mPreviousLaunchIntent;
    private ListPreference mNextLaunchIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.main);

        mHapticFeedback = (MultiSelectListPreference) findPreference(KEY_HAPTIC_FEEDBACK);
        mHapticFeedback.setOnPreferenceChangeListener(this);

        mCameraSwitch = (TwoStatePreference) findPreference(KEY_CAMERA_SWITCH);
        mCameraSwitch.setEnabled(CameraGestureSwitch.isSupported());
        mCameraSwitch.setChecked(CameraGestureSwitch.isEnabled(this));
        mCameraSwitch.setOnPreferenceChangeListener(new CameraGestureSwitch());

	mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        mVibratorStrength.setEnabled(VibratorStrengthPreference.isSupported());

        mMusicSwitch = (TwoStatePreference) findPreference(KEY_MUSIC_SWITCH);
        mMusicSwitch.setEnabled(MusicGestureSwitch.isSupported());
        mMusicSwitch.setChecked(MusicGestureSwitch.isEnabled(this));
        mMusicSwitch.setOnPreferenceChangeListener(new MusicGestureSwitch());

        mTorchSwitch = (TwoStatePreference) findPreference(KEY_TORCH_SWITCH);
        mTorchSwitch.setEnabled(TorchGestureSwitch.isSupported());
        mTorchSwitch.setChecked(TorchGestureSwitch.isEnabled(this));
        mTorchSwitch.setOnPreferenceChangeListener(new TorchGestureSwitch());

        mTorchLaunchIntent = (ListPreference) findPreference(KEY_TORCH_LAUNCH_INTENT); 
        mTorchLaunchIntent.setOnPreferenceChangeListener(this);
        
        mPlayPauseLaunchIntent = (ListPreference) findPreference(KEY_PLAY_PAUSE_LAUNCH_INTENT); 
        mPlayPauseLaunchIntent.setOnPreferenceChangeListener(this);
        
        mPreviousLaunchIntent = (ListPreference) findPreference(KEY_PREVIOUS_LAUNCH_INTENT); 
        mPreviousLaunchIntent.setOnPreferenceChangeListener(this);
        
        mNextLaunchIntent = (ListPreference) findPreference(KEY_NEXT_LAUNCH_INTENT);   
        mNextLaunchIntent.setOnPreferenceChangeListener(this);
        
        new InitListTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        if (KEY_HAPTIC_FEEDBACK.equals(key)) {
            final Set<String> value = (Set<String>) newValue;
            final CharSequence[] valueOptions = mHapticFeedback.getEntryValues();
            if(!value.isEmpty()){
			Settings.System.putInt(getContentResolver(), KEY_HAPTIC_FEEDBACK, 1);
			for(int i = 0; i < valueOptions.length; i++){
				if(value.contains(valueOptions[i].toString())){
					Settings.System.putInt(getContentResolver(), valueOptions[i].toString(), 1);
 				}
				else{
					Settings.System.putInt(getContentResolver(), valueOptions[i].toString(), 0);
				}
			}
		}
		else{
                    Settings.System.putInt(getContentResolver(), KEY_HAPTIC_FEEDBACK, 0);
		}
            return true;
        }
        if(KEY_TORCH_LAUNCH_INTENT.equals(key)){
            final String value = (String) newValue;
            findPreference(KEY_TORCH_LAUNCH_INTENT).setSummary(getAppnameFromPackagename(value));
            Settings.System.putString(getContentResolver(), KEY_TORCH_LAUNCH_INTENT, value);
            return true;
        }
        if(KEY_PLAY_PAUSE_LAUNCH_INTENT.equals(key)){
            final String value = (String) newValue;
            findPreference(KEY_PLAY_PAUSE_LAUNCH_INTENT).setSummary(
            getAppnameFromPackagename(value));
            Settings.System.putString(getContentResolver(), KEY_PLAY_PAUSE_LAUNCH_INTENT, value);
            return true;
        }
        if(KEY_PREVIOUS_LAUNCH_INTENT.equals(key)){
            final String value = (String) newValue;
            findPreference(KEY_PREVIOUS_LAUNCH_INTENT).setSummary(getAppnameFromPackagename(value));
            Settings.System.putString(getContentResolver(), KEY_PREVIOUS_LAUNCH_INTENT, value);
            return true;
        }
        if(KEY_NEXT_LAUNCH_INTENT.equals(key)){
            final String value = (String) newValue;
            findPreference(KEY_NEXT_LAUNCH_INTENT).setSummary(getAppnameFromPackagename(value));
            Settings.System.putString(getContentResolver(), KEY_NEXT_LAUNCH_INTENT, value);
            return true;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private List<String> getPackageNames(){
		List<String> packageNameList = new ArrayList<String>();
		List<PackageInfo> packs = 
			getApplicationContext().getPackageManager().getInstalledPackages(0);
		packageNameList.add("");
		for(int i = 0; i < packs.size(); i++){
			String packageName = packs.get(i).packageName;
			Intent launchIntent = getApplicationContext().getPackageManager()
					.getLaunchIntentForPackage(packageName);
			if(launchIntent != null){
				packageNameList.add(packageName);
			}
		}
		return packageNameList;
	}
	
	private String getAppnameFromPackagename(String packagename){
		if(packagename == null || "".equals(packagename)){
			 return getResources().getString(R.string.touchscreen_action_default);
		}
		final PackageManager pm = getApplicationContext().getPackageManager();
		ApplicationInfo ai;
		try {
			ai = pm.getApplicationInfo(packagename, 0);
		} catch (final Exception e) {
			ai = null;
		}
		return (String) (ai != null ? pm.getApplicationLabel(ai) : 
				getResources().getString(R.string.touchscreen_action_unkownappforpackagename));
	}
	
	private String getSummary(String key){
		String summary = Settings.System.getString(getContentResolver(), key);
		if(summary != null){
			return getAppnameFromPackagename(summary);
		}
		return getResources().getString(R.string.touchscreen_action_unkownappforpackagename);
	}
	
	private class InitListTask extends AsyncTask<Void, Void, Void> {
 	
		@Override
		protected Void doInBackground(Void... voids) {
			List<String> listPackageNames = getPackageNames();        
			final CharSequence[] packageNames = 
					listPackageNames.toArray(new CharSequence[listPackageNames.size()]);
			final CharSequence[] hrblPackageNames = new CharSequence[listPackageNames.size()];
			hrblPackageNames[0] = "Default action";
			
			for(int i = 1; i < listPackageNames.size(); i++){
				 hrblPackageNames[i] = getAppnameFromPackagename(listPackageNames.get(i));
			}

			mTorchLaunchIntent.setEntries(hrblPackageNames);
			mTorchLaunchIntent.setEntryValues(packageNames);

			mPlayPauseLaunchIntent.setEntries(hrblPackageNames);
			mPlayPauseLaunchIntent.setEntryValues(packageNames);

			mPreviousLaunchIntent.setEntries(hrblPackageNames);
			mPreviousLaunchIntent.setEntryValues(packageNames);

			mNextLaunchIntent.setEntries(hrblPackageNames);
			mNextLaunchIntent.setEntryValues(packageNames);
			
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			mTorchLaunchIntent.setSummary(getSummary(KEY_TORCH_LAUNCH_INTENT));
			mTorchLaunchIntent.setEnabled(true);
			
			mPlayPauseLaunchIntent.setSummary(getSummary(KEY_PLAY_PAUSE_LAUNCH_INTENT));  
			mPlayPauseLaunchIntent.setEnabled(true);
			
			mPreviousLaunchIntent.setSummary(getSummary(KEY_PREVIOUS_LAUNCH_INTENT));   
			mPreviousLaunchIntent.setEnabled(true);
			
			mNextLaunchIntent.setSummary(getSummary(KEY_NEXT_LAUNCH_INTENT));
			mNextLaunchIntent.setEnabled(true);
		}
	}
}
