package com.lhobbs.lockscreen.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.lhobbs.lockscreen.LockScreenActivity;

public class LockscreenIntentReceiver extends BroadcastReceiver {

	// Handle actions and display Lockscreen
	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)
				|| intent.getAction().equals(Intent.ACTION_SCREEN_ON)
				|| intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			start_lockscreen(context);
		}
//		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
//		sendIntent.setData(Uri.parse("sms:"));
	}

	// Display lock screen
	private void start_lockscreen(Context context) {
		Intent mIntent = new Intent(context, LockScreenActivity.class);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(mIntent);
	}

}
