package net.osmand.plus.settings;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;

import net.osmand.AndroidUtils;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.Version;
import net.osmand.plus.download.DownloadActivity;
import net.osmand.plus.download.DownloadActivityType;
import net.osmand.plus.helpers.FileNameTranslationHelper;
import net.osmand.plus.settings.preferences.ListPreferenceEx;

import java.util.Set;

import static net.osmand.plus.activities.SettingsNavigationActivity.MORE_VALUE;

public class VoiceAnnouncesFragment extends BaseSettingsFragment {

	public static final String TAG = "VoiceAnnouncesFragment";

	@Override
	protected int getPreferencesResId() {
		return R.xml.voice_announces;
	}

	@Override
	protected int getToolbarResId() {
		return R.layout.profile_preference_toolbar;
	}

	@Override
	protected int getToolbarTitle() {
		return R.string.voice_announces;
	}

	@Override
	protected void setupPreferences() {
		Preference voiceAnnouncesInfo = findPreference("voice_announces_info");
		voiceAnnouncesInfo.setIcon(getContentIcon(R.drawable.ic_action_info_dark));

		setupSpeedLimitExceedPref();

		setupKeepInformingPref();
		setupArrivalAnnouncementPref();
		setupVoiceProviderPref();

		if (!Version.isBlackberry(app)) {
			setupAudioStreamGuidancePref();
			setupInterruptMusicPref();
		}
	}

	@Override
	protected void onBindPreferenceViewHolder(Preference preference, PreferenceViewHolder holder) {
		super.onBindPreferenceViewHolder(preference, holder);

		if (settings.SPEAK_ROUTING_ALARMS.getId().equals(preference.getKey())) {
			boolean checked = ((SwitchPreference) preference).isChecked();
			int color = checked ? getActiveProfileColor() : ContextCompat.getColor(app, R.color.preference_top_switch_off);

			AndroidUtils.setBackground(holder.itemView, new ColorDrawable(color));
		}
	}

	private void setupSpeedLimitExceedPref() {
		Float[] speedLimitValues;
		String[] speedLimitNames;

		if (settings.METRIC_SYSTEM.get() == OsmandSettings.MetricsConstants.KILOMETERS_AND_METERS) {
			speedLimitValues = new Float[] {-10f, -7f, -5f, 0f, 5f, 7f, 10f, 15f, 20f};
			speedLimitNames = new String[speedLimitValues.length];

			for (int i = 0; i < speedLimitValues.length; i++) {
				speedLimitNames[i] = speedLimitValues[i].intValue() + " " + getString(R.string.km_h);
			}
		} else {
			speedLimitValues = new Float[] {-7f, -5f, -3f, 0f, 3f, 5f, 7f, 10f, 15f};
			speedLimitNames = new String[speedLimitValues.length];

			for (int i = 0; i < speedLimitNames.length; i++) {
				speedLimitNames[i] = speedLimitValues[i].intValue() + " " + getString(R.string.mile_per_hour);
			}
		}

		ListPreferenceEx voiceProvider = (ListPreferenceEx) findPreference(settings.SPEED_LIMIT_EXCEED.getId());
		voiceProvider.setEntries(speedLimitNames);
		voiceProvider.setEntryValues(speedLimitValues);
	}

	private void setupKeepInformingPref() {
		Integer[] keepInformingValues = new Integer[] {0, 1, 2, 3, 5, 7, 10, 15, 20, 25, 30};
		String[] keepInformingNames = new String[keepInformingValues.length];
		keepInformingNames[0] = getString(R.string.keep_informing_never);
		for (int i = 1; i < keepInformingValues.length; i++) {
			keepInformingNames[i] = keepInformingValues[i] + " " + getString(R.string.int_min);
		}

		ListPreferenceEx keepInforming = (ListPreferenceEx) findPreference(settings.KEEP_INFORMING.getId());
		keepInforming.setEntries(keepInformingNames);
		keepInforming.setEntryValues(keepInformingValues);
	}

	private void setupArrivalAnnouncementPref() {
		Float[] arrivalValues = new Float[] {1.5f, 1f, 0.5f, 0.25f};
		String[] arrivalNames = new String[] {
				getString(R.string.arrival_distance_factor_early),
				getString(R.string.arrival_distance_factor_normally),
				getString(R.string.arrival_distance_factor_late),
				getString(R.string.arrival_distance_factor_at_last)
		};

		ListPreferenceEx arrivalDistanceFactor = (ListPreferenceEx) findPreference(settings.ARRIVAL_DISTANCE_FACTOR.getId());
		arrivalDistanceFactor.setEntries(arrivalNames);
		arrivalDistanceFactor.setEntryValues(arrivalValues);
	}

	private void setupVoiceProviderPref() {
		Activity activity = getActivity();
		if (activity == null) {
			return;
		}
		Set<String> voiceFiles = app.getRoutingOptionsHelper().getVoiceFiles(activity);
		String[] entries = new String[voiceFiles.size() + 2];
		String[] entryValues = new String[voiceFiles.size() + 2];

		int k = 0;
		// entries[k++] = getString(R.string.shared_string_none);
		entryValues[k] = OsmandSettings.VOICE_PROVIDER_NOT_USE;
		entries[k++] = getString(R.string.shared_string_do_not_use);
		for (String s : voiceFiles) {
			entries[k] = (s.contains("tts") ? getString(R.string.ttsvoice) + " " : "") + FileNameTranslationHelper.getVoiceName(activity, s);
			entryValues[k] = s;
			k++;
		}
		entryValues[k] = MORE_VALUE;
		entries[k] = getString(R.string.install_more);

		ListPreferenceEx voiceProvider = (ListPreferenceEx) findPreference(settings.VOICE_PROVIDER.getId());
		voiceProvider.setEntries(entries);
		voiceProvider.setEntryValues(entryValues);
		voiceProvider.setIcon(getContentIcon(R.drawable.ic_action_volume_up));
	}

	private void setupAudioStreamGuidancePref() {
		String[] streamTypes = new String[] {
				getString(R.string.voice_stream_music),
				getString(R.string.voice_stream_notification),
				getString(R.string.voice_stream_voice_call)
		};
		//getString(R.string.shared_string_default)};

		Integer[] streamIntTypes = new Integer[] {
				AudioManager.STREAM_MUSIC,
				AudioManager.STREAM_NOTIFICATION,
				AudioManager.STREAM_VOICE_CALL
		};
		//AudioManager.USE_DEFAULT_STREAM_TYPE};

		ListPreferenceEx audioStreamGuidance = createListPreferenceEx(settings.AUDIO_STREAM_GUIDANCE.getId(), streamTypes, streamIntTypes, R.string.choose_audio_stream, R.layout.preference_with_descr);
		audioStreamGuidance.setIconSpaceReserved(true);

		getPreferenceScreen().addPreference(audioStreamGuidance);

		audioStreamGuidance.setDependency(settings.SPEAK_ROUTING_ALARMS.getId());
	}

	private void setupInterruptMusicPref() {
		Preference interruptMusicPref = createSwitchPreference(settings.INTERRUPT_MUSIC, R.string.interrupt_music, R.string.interrupt_music_descr, R.layout.preference_switch);
		interruptMusicPref.setIconSpaceReserved(true);
		getPreferenceScreen().addPreference(interruptMusicPref);

		interruptMusicPref.setDependency(settings.SPEAK_ROUTING_ALARMS.getId());
	}

	public void confirmSpeedCamerasDlg() {
		Context ctx = getContext();
		if (ctx == null) {
			return;
		}
		AlertDialog.Builder bld = new AlertDialog.Builder(ctx);
		bld.setMessage(R.string.confirm_usage_speed_cameras);
		bld.setPositiveButton(R.string.shared_string_yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				settings.SPEAK_SPEED_CAMERA.set(true);
				SwitchPreference speakSpeedCamera = (SwitchPreference) findPreference(settings.SPEAK_SPEED_CAMERA.getId());
				if (speakSpeedCamera != null) {
					speakSpeedCamera.setChecked(true);
				}
			}
		});
		bld.setNegativeButton(R.string.shared_string_cancel, null);
		bld.show();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String prefId = preference.getKey();

		if (prefId.equals(settings.VOICE_PROVIDER.getId())) {
			if (MORE_VALUE.equals(newValue)) {
				// listPref.set(oldValue); // revert the change..
				final Intent intent = new Intent(getContext(), DownloadActivity.class);
				intent.putExtra(DownloadActivity.TAB_TO_OPEN, DownloadActivity.DOWNLOAD_TAB);
				intent.putExtra(DownloadActivity.FILTER_CAT, DownloadActivityType.VOICE_FILE.getTag());
				startActivity(intent);
			} else if (newValue instanceof String) {
				settings.VOICE_PROVIDER.set((String) newValue);
				app.initVoiceCommandPlayer(getActivity(), getSelectedAppMode(), false, null, true, false);
			}
			return true;
		}
		if (prefId.equals(settings.SPEAK_SPEED_CAMERA.getId())) {
			if (!settings.SPEAK_SPEED_CAMERA.get()) {
				confirmSpeedCamerasDlg();
				return false;
			} else {
				return true;
			}
		}
		if (prefId.equals(settings.AUDIO_STREAM_GUIDANCE.getId())) {
			// Sync DEFAULT value with CAR value, as we have other way to set it for now

			if (getSelectedAppMode().equals(ApplicationMode.CAR) && newValue instanceof Integer) {
				settings.AUDIO_STREAM_GUIDANCE.setModeValue(ApplicationMode.DEFAULT, (Integer) newValue);
			} else {
				settings.AUDIO_STREAM_GUIDANCE.setModeValue(ApplicationMode.DEFAULT, settings.AUDIO_STREAM_GUIDANCE.getModeValue(ApplicationMode.CAR));
			}
			settings.AUDIO_USAGE.setModeValue(ApplicationMode.DEFAULT, settings.AUDIO_USAGE.getModeValue(ApplicationMode.CAR));

			return true;
		}

		return super.onPreferenceChange(preference, newValue);
	}
}