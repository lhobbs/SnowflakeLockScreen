package com.lhobbs.lockscreen;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;

import com.lhobbs.lockscreen.utils.LockscreenService;
import com.lhobbs.lockscreen.utils.LockscreenUtils;

public class LockScreenActivity extends Activity implements
		LockscreenUtils.OnLockStatusChangedListener {

	// User-interface
	private ImageButton btnUnlock;
	private ImageButton btnMessages;
	private ImageButton btnSnap;
	private ImageButton btnCamera;
	// Member variables
	private LockscreenUtils mLockscreenUtils;

	private GestureDetectorCompat mGestureDetector;
	private ScaleGestureDetector mScaleDetector;
	DisplayMetrics displaymetrics;

	// Set appropriate flags to make the screen appear over the keyguard
	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(
				WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						);

		super.onAttachedToWindow();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_lockscreen);

		displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//		int height = displaymetrics.heightPixels;

		init();

		// unlock screen in case of app get killed by system
		if (getIntent() != null && getIntent().hasExtra("kill")
				&& getIntent().getExtras().getInt("kill") == 1) {
			enableKeyguard();
			unlockHomeButton();
		} else {

			try {
				// disable keyguard
				disableKeyguard();

				// lock home button
				lockHomeButton();

				// start service for observing intents
				startService(new Intent(this, LockscreenService.class));

				// listen the events get fired during the call
				StateListener phoneStateListener = new StateListener();
				TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
				telephonyManager.listen(phoneStateListener,
						PhoneStateListener.LISTEN_CALL_STATE);

			} catch (Exception e) {
			}

		}
	}

	private void init() {
		mLockscreenUtils = new LockscreenUtils();
		btnUnlock = (ImageButton) findViewById(R.id.btnUnlock);
		btnMessages = (ImageButton) findViewById(R.id.btnMessages);
		btnSnap = (ImageButton) findViewById(R.id.btnSnap);
		btnCamera = (ImageButton) findViewById(R.id.btnCamera);

		btnMessages.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.setType("vnd.android-dir/mms-sms");
				startActivity(intent);
			}
		});

		btnCamera.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivity(intent);
			}
		});



		btnSnap.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("*/*");
				intent.setPackage("com.snapchat.android");
				startActivity(intent);
			}
		});
//		btnUnlock.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// unlock home button and then screen on button press
//				unlockHomeButton();
//			}
//		});

		btnUnlock.setOnTouchListener( new View.OnTouchListener() {
			private int INVALID_POINTER_ID = 0;
			private int mActivePointerId = INVALID_POINTER_ID;

			@Override
			public boolean onTouch(View v, MotionEvent ev) {
				//Log.d("test gesture", "in on touch event");
				float mLastTouchX = 0;
				float mLastTouchY = 0;
				float mPosX = 0;
				float mPosY = 0;
				// Let the ScaleGestureDetector inspect all events.
				//mScaleDetector.onTouchEvent(ev);

				final int action = MotionEventCompat.getActionMasked(ev);

//				WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) btnUnlock.getLayoutParams();
//				Log.d("layout params", layoutParams.toString());

				switch (action) {
					case MotionEvent.ACTION_DOWN: {
						final int pointerIndex = MotionEventCompat.getActionIndex(ev);
						final float x = MotionEventCompat.getX(ev, pointerIndex);
						final float y = MotionEventCompat.getY(ev, pointerIndex);

						// Remember where we started (for dragging)
						mLastTouchX = x;
						mLastTouchY = y;
						// Save the ID of this pointer (for dragging)
						mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
						break;
					}

					case MotionEvent.ACTION_MOVE: {
						int height = displaymetrics.heightPixels;
						int currentY = (int) btnUnlock.getY();
						float YPerct = (float)currentY/height;
						Log.d("Y", Float.toString(YPerct) + " " + Integer.toString(height) + " " + Integer.toString(currentY));
						if (YPerct > .2) {
							btnUnlock.setBackgroundResource(R.drawable.snowflake_highlighted);
						}
						if (YPerct > .30) {
							//unlockHomeButton();
							Intent startMain = new Intent(Intent.ACTION_MAIN);
							startMain.addCategory(Intent.CATEGORY_HOME);
							startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(startMain);
						}
						else {
							// Find the index of the active pointer and fetch its position
							final int pointerIndex =
									MotionEventCompat.findPointerIndex(ev, mActivePointerId);

							final float x = MotionEventCompat.getX(ev, pointerIndex);
							final float y = MotionEventCompat.getY(ev, pointerIndex);

							// Calculate the distance moved
							final float dx = x - mLastTouchX;
							final float dy = y - mLastTouchY;

							mPosX += dx;
							mPosY += dy;

							//invalidate();

							// Remember this touch position for the next move event
							mLastTouchX = x;
							mLastTouchY = y;

//						layoutParams.x = (int)mPosX;
//						layoutParams.y = (int)mPosY;
							btnUnlock.setX(x);
							btnUnlock.setY(y);
							break;
						}
					}

					case MotionEvent.ACTION_UP: {
						mActivePointerId = INVALID_POINTER_ID;
						break;
					}

					case MotionEvent.ACTION_CANCEL: {
						mActivePointerId = INVALID_POINTER_ID;
						break;
					}

					case MotionEvent.ACTION_POINTER_UP: {

						final int pointerIndex = MotionEventCompat.getActionIndex(ev);
						final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

						if (pointerId == mActivePointerId) {
							// This was our active pointer going up. Choose a new
							// active pointer and adjust accordingly.
							final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
							mLastTouchX = MotionEventCompat.getX(ev, newPointerIndex);
							mLastTouchY = MotionEventCompat.getY(ev, newPointerIndex);
							mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
						}
						break;
					}
				}
				return true;
			}
		});
	}

	// Handle events of calls and unlock screen if necessary
	private class StateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				unlockHomeButton();
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			}
		}
	};

	// Don't finish Activity on Back press
	@Override
	public void onBackPressed() {
		return;
	}

	// Handle button clicks
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

		if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
				|| (keyCode == KeyEvent.KEYCODE_POWER)
				|| (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
				|| (keyCode == KeyEvent.KEYCODE_CAMERA)) {
			return true;
		}
		if ((keyCode == KeyEvent.KEYCODE_HOME)) {

			return true;
		}

		return false;

	}

	// handle the key press events here itself
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
				|| (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
				|| (event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
			return false;
		}
		if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {

			return true;
		}
		return false;
	}

	// Lock home button
	public void lockHomeButton() {
		mLockscreenUtils.lock(LockScreenActivity.this);
	}

	// Unlock home button and wait for its callback
	public void unlockHomeButton() {
		mLockscreenUtils.unlock();
	}

	// Simply unlock device when home button is successfully unlocked
	@Override
	public void onLockStatusChanged(boolean isLocked) {
		if (!isLocked) {
			unlockDevice();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		unlockHomeButton();
	}

	@SuppressWarnings("deprecation")
	private void disableKeyguard() {
		KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
		mKL.disableKeyguard();
	}

	@SuppressWarnings("deprecation")
	private void enableKeyguard() {
		KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
		mKL.reenableKeyguard();
	}
	
	//Simply unlock device by finishing the activity
	private void unlockDevice()
	{
		finish();
	}



}