package com.vidyo.vidyosample.activity;


//import com.MyMobile.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.ichatxvidyo.R;
import com.vidyo.LmiDeviceManager.LmiDeviceManagerView;
import com.vidyo.VidyoClientLib.LmiAndroidJniChatCallbacks;
import com.vidyo.VidyoClientLib.LmiAndroidJniConferenceCallbacks;
import com.vidyo.VidyoClientLib.LmiAndroidJniLoginCallbacks;
import com.vidyo.VidyoClientLib.LmiAndroidJniMessageCallbacks;
//import com.vidyo.vidyosample.R;
import com.vidyo.vidyosample.app.ApplicationJni;
import com.vidyo.vidyosample.entities.VidyoInfo;
import com.vidyo.vidyosample.entities.VidyoResponse;
import com.vidyo.vidyosample.fragment.VidyoJoinConferenceResponderFragment;
import com.vidyo.vidyosample.fragment.VidyoJoinConferenceResponderFragment.OnVidyoJoinConferenceUpdatedListener;
import com.vidyo.vidyosample.fragment.VidyoMyAccountResponderFragment;
import com.vidyo.vidyosample.fragment.VidyoMyAccountResponderFragment.OnVidyoMyAccountUpdatedListener;
import com.vidyo.vidyosample.util.Utils;
import com.vidyo.vidyosample.entities.UserInfo;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Base64;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class VidyoSampleActivity extends Activity implements
	LmiDeviceManagerView.Callback, SensorEventListener, 
	OnVidyoJoinConferenceUpdatedListener, OnVidyoMyAccountUpdatedListener {

	private static Vector _eventListeners = new Vector();



	private static final String TAG = "VidyoSampleActivity";
	
	private final String VIDYO_MY_ACCOUNT_RESPONDER_TAG = "VidyoMyAccountResponder";
	private final String VIDYO_JOIN_CONFERENCE_RESPONDER_TAG = "VidyoJoinConferenceResponder";
	
	// member status for vidyo myAccount call
	private final String MEMBER_STATUS_ONLINE = "Online";
	
	// Offsets to place the video window below the top buttons.
	private final int landscapeOffset = 70;
	private final int portraitOffset = 70;
	
	// Time between engagement status requests
	private final long statusUpdateInterval = 2000;
	
	// Device managers
	private LmiDeviceManagerView bcView;
	
	// MESSAGES
	public static final int MSG_BOX = 1;
	public static final int CALL_RECEIVED = 2;
	public static final int GET_ENGAGEMENT_STATUS = 3;
	public static final int MEMBER_CONNECTED = 4;
	public static final int END_ENGAGEMENT = 5;
	public static final int JOIN_CONFERENCE = 6;
	public static final int CONFERENCE_ENDED = 7;
	public static final int CONFERENCE_ERROR = 8;
	public static final int CONFERENCE_STARTED = 9;
	public static final int SHOW_MSG = 10;
	public static final int MSG_CALL = 11;
	public static final int MSG_SEARCH = 12;
	public static final int MSG_BYE = 13;

	// CAMERA ORIENTATION VARIABLES
	final float degreePerRadian = (float) (180.0f / Math.PI);
	final int ORIENTATION_UP = 0;
	final int ORIENTATION_DOWN = 1;
	final int ORIENTATION_LEFT = 2;
	final int ORIENTATION_RIGHT = 3;



	private int currentRotation;
	private SensorManager sensorManager;
	private AudioManager audioManager;
	private TelephonyManager telephonyManager; 
	private boolean sensorListenerStarted = false;
	private boolean telephonyListenerStarted = false;
	private boolean audioReceiverRegistered = false;
	private boolean blockingCallReceiverRegistered = false;
	private boolean timeoutHandlerRegistered = false;

	static Handler message_handler;
	static Handler timeoutHandler;
	Timer engagementTimer;
	
	private VidyoResponse vidyoResponse;
	private VidyoInfo vidyoInfo;
	private int vidyoRetryCount;
	private int vidyoRetryAttempts = 100;


	private String CallType = "GUEST_JOIN";
	private int lastConfStatus = -1;

	Button refreshVideoBtn;
	Button endBtn;

	// Application
	static ApplicationJni app;

		
	// Engagement flags
	private boolean loginStatus = false;
	private boolean engagementStarted = false;
	private boolean conferenceStarted = false;
	private boolean conferenceEnded = false;
	private boolean conferenceEnding = false;
	private boolean memberConnected = false;
	private boolean engagementComplete = false;
	private boolean confirmCancel = false;
	private boolean endNearAlertShown = false;
	private boolean joinedRoom = false;
	private boolean refreshVideo = false;

	public static void SendMsg(int msgid)
	{
		Message msg = new Message();
		msg.what = msgid;
		Bundle data = new Bundle();

		message_handler.sendMessage(msg);
	}

	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "VidyoSampleActivity.onNewIntent...Start...");

		super.onNewIntent(intent);

		setIntent(intent);//must store the new intent unless getIntent() will return the old one

		final Bundle bundle = getIntent().getExtras();
		String Cmd = bundle.getString("Cmd");
		Log.d(TAG, "VidyoSampleActivity.onNewIntent...Cmd->" + Cmd);

		if(Cmd.equals("VideoQuit")){
			sendEndEngagement();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "VidyoSampleActivity.onCreate...Start...");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.conference);
		
		message_handler = new MessageHandler(this);

		if (savedInstanceState == null) {
			Object object = (Object)getApplication();
			if (object instanceof ApplicationJni) {
				app = (ApplicationJni)object;


				// THIS MUST BE CALLED TO INITIALIZE THE VIDYOCLIENT JNI LIBRARY
				Log.d(TAG, "Initializing JNI");
				app.LmiAndroidJniInitialize();
			}
		}

		/*
		Object object = (Object)getApplication();
		if (object instanceof ApplicationJni) {
			app = (ApplicationJni)object;

		} else {
			app = null;
		}
		*/

		if (app != null) {
			Log.d(TAG, "VidyoSampleActivity.onCreate...ApplicationJni has been set correct!!!");
			constructJniInterface();
		}
		
		final Bundle bundle = getIntent().getExtras();

		/*
		CALLDIRECT
		JOIN
		GUEST_JOIN
		 */
		CallType = bundle.getString("CallType");
		if(CallType == null)
		{
			Log.v(TAG,"VidyoSampleActivity.onCreate...CallType->null...ignore and return...");
			sendEndEngagement();
			return;
		}

		if(CallType.equals("CALLDIRECT") || CallType.equals("JOIN")) {
			final String server = bundle.getString("server");
			final String username = bundle.getString("username");
			final String password = bundle.getString("password");
			final String dest = bundle.getString("Dest");


			vidyoInfo = new VidyoInfo();
			vidyoInfo.setVidyoHost(server);
			vidyoInfo.setVidyoUsername(username);
			vidyoInfo.setVidyoPassword(password);
			vidyoInfo.setCalledDestUser(dest);

			Log.d(TAG, "VidyoSampleActivity.onCreate...server: " + server);
			Log.d(TAG, "VidyoSampleActivity.onCreate...username: " + username);
			Log.d(TAG, "VidyoSampleActivity.onCreate...password: " + password);
			Log.d(TAG, "VidyoSampleActivity.onCreate...dest: " + dest);
		}
		else if(CallType.equals("GUEST_JOIN")){
			final String host = bundle.getString("host");
			final String port = bundle.getString("port");
			final String key = bundle.getString("key");
			final String userName = bundle.getString("userName");
			final String pin = bundle.getString("pin");

			vidyoInfo = new VidyoInfo();
			vidyoInfo.setGuest_host(host);
			vidyoInfo.setGuest_port(Integer.parseInt(port));
			vidyoInfo.setGuest_key(key);
			vidyoInfo.setGuest_userName(userName);
			vidyoInfo.setGuest_pin(pin);

			Log.d(TAG, "VidyoSampleActivity.onCreate...Guest_host: " + host);
			Log.d(TAG, "VidyoSampleActivity.onCreate...Guest_port: " + port);
			Log.d(TAG, "VidyoSampleActivity.onCreate...Guest_key: " + key);
			Log.d(TAG, "VidyoSampleActivity.onCreate...Guest_userName: " + userName);
			Log.d(TAG, "VidyoSampleActivity.onCreate...Guest_pin: " + pin);
		}
				
		// Hook-up exit button
		final Button btnEnd = (Button) findViewById(R.id.button_cancel);
		btnEnd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View view) {
				confirmCancelButton();
			}
		});
		
		final View view = (View) findViewById(R.id.engagement_layout);
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmCancel = false;
				btnEnd.setText(R.string.misc_cancel);
			}	
		});
		
		final Display display = getWindowManager().getDefaultDisplay();
		currentRotation = display.getRotation();
		
		
		setupVideo();
		startEngagmentTimer();



	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "onResume called");
		super.onResume();

		app.EnableAllVideoStreams();

		app.LmiAndroidJniSetCameraDevice(1);
		app.LmiAndroidJniMuteCamera(false);
		resizeVideo();

		if (joinedRoom) {
			Log.d(TAG, "onResume called...joinedRoom->true...refreshing video from background mode");
			refreshVideo();
		}
		else{
			Log.d(TAG, "onResume called...joinedRoom->false...will not refreshing video from background mode");
		}

		Log.d(TAG, "onResume called...will call sensorManager.registerListener....");
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    Sensor gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorListenerStarted = sensorManager.registerListener(this,
				gSensor, SensorManager.SENSOR_DELAY_NORMAL);
		Log.d(TAG, "onResume called...will call sensorManager.registerListener....sensorListenerStarted->" + sensorListenerStarted);

		audioReceiverRegistered = true;
		registerReceiver(audioReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		
		if (!blockingCallReceiverRegistered) {
			registerReceiver(blockingCallReceiver, new IntentFilter("android.intent.action.PHONE_STATE"));
			blockingCallReceiverRegistered = true;
		}



	}
	
	@Override
	public void onStop() {
		Log.d(TAG, "onStop called");
		super.onStop();
		


		if (sensorListenerStarted) {
			Log.d(TAG, "onStop called...sensorListenerStarted->true...will call unregisterListener...");
			sensorManager.unregisterListener(this);
			sensorListenerStarted = false;
		}
		else{
			Log.d(TAG, "onStop called...sensorListenerStarted->false...will not call unregisterListener...");
		}
		
		if (audioReceiverRegistered) {
			Log.d(TAG, "onStop called...audioReceiverRegistered->true...will call unregisterReceiver...");
			unregisterReceiver(audioReceiver);
			audioReceiverRegistered = false;
			audioManager = null;
		}
		else{
			Log.d(TAG, "onStop called...audioReceiverRegistered->false...will not call unregisterReceiver...");

		}


	}
		
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy start...");

		runOnUiThread(new Runnable()
		{ public void run() {
			Iterator iterator = ((Vector) _eventListeners.clone()).iterator();

			while (iterator.hasNext()) {
				VidyoEventListener listener = (VidyoEventListener) iterator.next();
				listener.OnEvtVideoQuit();
			}
		}});

		stopDevices();
		killEngagmentTimer();
		app.uninitialize();
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		Log.d(TAG, "onKeyDown start...");
		// User should not be able to click back button on negative use cases or during the conversation.
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Log.d(TAG, "onKeyDown Called");
			return false; 
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "Configuration changed being handled.");
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		//Log.d(TAG, "onSensorChanged start...");
		final Display display = getWindowManager().getDefaultDisplay();
		final int rotation = display.getRotation();
		if (rotation == currentRotation) {
			return;
		}
		rotateScreen(rotation);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }
	
	private void setupVideo() {
		Log.d(TAG, "setupVideo start...");
	    bcView = new LmiDeviceManagerView(this, this);
	    
		final String caFileName = writeCaCertificates();
		
		app.initialize(caFileName, this);
	}
	
	private void startEngagement() {

		Log.d(TAG, "startEngagement");

		switchToVideoView();
		
		setupAudioForEngagement();
		
		// Start listening for sensor events...
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    Sensor gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorListenerStarted = sensorManager.registerListener(this,
				gSensor, SensorManager.SENSOR_DELAY_NORMAL);
		
		if (!loginStatus) {

			if (!Utils.isWifiConnected(this)) {
				Log.d(TAG, "Setting Vidyo Config to LIMITED_BANDWIDTH");
				app.SetLimitedBandwidth(true);
			}
			else {
				Log.d(TAG, "Setting Vidyo Config to BEST_QUALITY");
				app.SetLimitedBandwidth(false);
			}

			if(CallType.equals("GUEST_JOIN")){
				Log.d(TAG, "startEngagement...loginStatus is false...will try to call LmiAndroidJniHandleGuestLink...");
				app.HideToolBar(false);
				//SetEchoCancellation(true);

				Message msg = new Message();
				msg.what = SHOW_MSG;
				Bundle data = new Bundle();
				data.putString("text", "视频访客呼叫中...");
				msg.setData(data);

				message_handler.sendMessage(msg);

				app.LmiAndroidJniHandleGuestLink(vidyoInfo.getGuest_host(),vidyoInfo.getGuest_port(),vidyoInfo.getGuest_key(),vidyoInfo.getGuest_userName(),vidyoInfo.getGuest_pin(),false);
				loginStatus = true;
			}
			else {
				Log.d(TAG, "startEngagement...loginStatus is false...will try to call LmiAndroidJniLogin...");
				app.HideToolBar(false);
				//SetEchoCancellation(true);

				Message msg = new Message();
				msg.what = SHOW_MSG;
				Bundle data = new Bundle();
				data.putString("text", "视频账号注册中...");
				msg.setData(data);

				message_handler.sendMessage(msg);

				app.LmiAndroidJniLogin(vidyoInfo.getVidyoHost(), vidyoInfo.getVidyoUsername(), vidyoInfo.getVidyoPassword());
				loginStatus = true;
			}

		}
		else{
			Log.d(TAG, "startEngagement...loginStatus is true...will do nothing...");
		}
		engagementStarted = true;
	}
	
	private void startEngagmentTimer() {
		Log.d(TAG, "startEngagmentTimer start...");
		engagementTimer = new Timer();
		engagementTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				message_handler.sendEmptyMessage(GET_ENGAGEMENT_STATUS);
			}
		}, 0L, statusUpdateInterval);
	}
	
	private void killEngagmentTimer() {
		Log.d(TAG, "killEngagmentTimer start...");
		if (engagementTimer != null) {
			engagementTimer.cancel();
			engagementTimer.purge();
			engagementTimer = null;
		}
	}
	
	private String writeCaCertificates() {
		try {
			Log.d(TAG, "writeCaCertificates start...");
			final InputStream caCertStream = getResources().openRawResource(R.raw.ca_certificates);
			
			File caCertDirectory = null;
			try {
				String pathDir = Utils.getAndroidInternalMemDir(this);
				caCertDirectory = new File(pathDir);
			} catch (Exception e) {
				Log.e(TAG, "Something went wrong getting the pathDir");
				return null;
			}
			
 			File caFile = new File(caCertDirectory,"ca-certificates.crt");
			
			final FileOutputStream caCertFile = new FileOutputStream(caFile);
			final byte buf[] = new byte[1024];
			int len;
			while ((len = caCertStream.read(buf)) != -1) {
				caCertFile.write(buf, 0, len);
			}
			caCertStream.close();
			caCertFile.close();

			return caFile.getPath();
		}
		catch (final Exception e) {
			return null;
		}
	}
	
	private void confirmCancelButton() {
		Log.d(TAG, "confirmCancelButton start...");
		final Button btnEnd = (Button) findViewById(R.id.button_cancel);
		if (confirmCancel) {
			btnEnd.setEnabled(false);
			finish();
		}
		else {
			confirmCancel = true;
			btnEnd.setText(R.string.confirm_cancel);
		}
	}
		
	private void cancelConfirmCancelButton() {
		Log.d(TAG, "cancelConfirmCancelButton start...");
		if (confirmCancel) {
			confirmCancel = false;
			final Button btnEnd = (Button) findViewById(R.id.button_cancel);
			btnEnd.setText(R.string.misc_cancel);
			final ViewGroup.LayoutParams params = btnEnd.getLayoutParams();
			params.width = 110;
			btnEnd.setLayoutParams(params);
		}
	}
	
	private void refreshVideo() {
		Log.d(TAG, "Refresh Video button pushed");
		
		final RelativeLayout layout = (RelativeLayout) findViewById(R.id.refresh_video_content);
		layout.setVisibility(View.VISIBLE);
				
		vidyoRetryCount = 0;
		
		refreshVideoBtn.setEnabled(false);
		refreshVideo = true;
		conferenceEnding = true;
		
		app.LmiAndroidJniLeave();
		return;
	}

		
	private void stopDevices() {
		Log.d(TAG, "Stopping devices");
		loginStatus = false;
		
		if (timeoutHandlerRegistered) {
			timeoutHandler.removeCallbacks(engagementTimeoutRunnable);
		}
		
		if (blockingCallReceiverRegistered) {
			unregisterReceiver(blockingCallReceiver);
			blockingCallReceiverRegistered = false;
		}
	}
	
	private void rotateScreen(final int rotation) {
		Log.d(TAG, "rotateScreen start...");
		switch (rotation) {
		case Surface.ROTATION_0:
			app.LmiAndroidJniSetOrientation(ORIENTATION_UP);
			break;
		case Surface.ROTATION_90:
			app.LmiAndroidJniSetOrientation(ORIENTATION_RIGHT);
			break;
		case Surface.ROTATION_180:
			app.LmiAndroidJniSetOrientation(ORIENTATION_DOWN);
			break;
		case Surface.ROTATION_270:
			app.LmiAndroidJniSetOrientation(ORIENTATION_LEFT);
			break;
		}

		currentRotation = rotation;
		return;
	}
	
	private void switchToWaitingRoom() {
		Log.d(TAG, "switchToWaitingRoom start...");

		final TextView message = (TextView) findViewById(R.id.just_relax);
		message.setText(String.format(getString(R.string.be_there_shortly)));
		
		final ImageView image = (ImageView) findViewById(R.id.connecting_image);
		//image.setImageResource(R.drawable.img_video_engagement_waiting);
	}

	private void switchToVideoView() {

		Log.d(TAG, "switchToVideoView start");

		final ViewFlipper vf = (ViewFlipper) findViewById(R.id.ViewFlipper01);
		vf.showNext();

		final RelativeLayout layout = (RelativeLayout) findViewById(R.id.engagement_layout);
		final RelativeLayout layout2 = (RelativeLayout) findViewById(R.id.engagement_layout2);

		layout.removeAllViews();
		layout2.addView(bcView, 0);
		
		resizeVideo();
		
		// fire off a timer and time out after one minute if the conversation hasn't started
		timeoutHandler = new Handler();
		Log.d(TAG, "The engagement timer has started");
		timeoutHandler.postDelayed(engagementTimeoutRunnable, 120000);	
		timeoutHandlerRegistered = true;
		
		endBtn = (Button) findViewById(R.id.button_end);
		//endBtn.setEnabled(false);
		endBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View view) {
				sendEndEngagement();
			}
		});

		// Hook-up refresh video button
		refreshVideoBtn = (Button) findViewById(R.id.button_refresh_video);
		refreshVideoBtn.setEnabled(false);
		refreshVideoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View view) {
				refreshVideo();
			}
		});
	}
	
	private void engagementTimeoutDialog() {
		Log.d(TAG, "engagement has timed out");
	}
	
	private void sendMemberConnected() {
		// code to send the member connected message to server would go here
		Log.d(TAG, "sendMemberConnected start...");
		onMemberConnectedResponse();
	}
	
	private void sendEndEngagement() {
		Log.d(TAG, "sendEndEngagement start...");
		final RelativeLayout wrapUpLayout = (RelativeLayout) findViewById(R.id.wrapup_content);
		wrapUpLayout.setVisibility(View.VISIBLE);
		
		// code to request the status of the engagement would go here
		onEndEngagement();	
	}
	
	private void sendCancelEngagement() {
		// code to request to cancel the engagement would go here
		Log.d(TAG, "sendCancelEngagement start...");
		onCancelEngagement();
	}	
	
	private void joinConference() {
		Log.d(TAG, "joinConference start...");


		if(CallType.equals("GUEST_JOIN")){
			Log.d(TAG, "joinConference start...CallType->GUEST_JOIN...will do nothing here...");
		}
		else {
			if (vidyoRetryCount < vidyoRetryAttempts) {
				if (vidyoResponse == null || vidyoResponse.getRequestEid() == null) {
					getVidyoAccountInfo();
					return;
				}

				Log.d(TAG, "request id is: " + vidyoResponse.getRequestEid());
				Log.d(TAG, "status is: " + vidyoResponse.getMemberStatus());


				// THIS CODE IS IN OUR REAL APP, ITS COMMENTED OUT BECAUSE ON
				// SUBSEQUENT RE-ENTRY THE VIDPORTAL RESPONDS WITH A SUCCESSFUL
				// LOGIN AND REQUESTEID BUT SAYS THE MEMBERSTATUS IS OFFLINE...
				// we have an eid and the member is online, join the room

				//if (vidyoResponse.getRequestEid() != null && MEMBER_STATUS_ONLINE.equals
				//		(vidyoResponse.getMemberStatus())) {
				if (vidyoResponse.getRequestEid() != null) {
					Log.e(TAG, "joinConference...request id is not null...will call joinRoom...");
					joinRoom();

				}
				// the member is not online, attempt to login again
				/*
				else if (!MEMBER_STATUS_ONLINE.equals(vidyoResponse.getMemberStatus())) {
					Log.e(TAG, "Retrying attempt " + vidyoRetryCount + " of " + vidyoRetryAttempts +
							" : memberStatus is not Online...now will call LmiAndroidJniLogin...");
					vidyoRetryCount++;
					app.LmiAndroidJniLogin(vidyoInfo.getVidyoHost(), vidyoInfo.getVidyoUsername(), vidyoInfo.getVidyoPassword());
				}
				*/
				// the eid may not have been retrieved, try again
				else {
					Log.e(TAG, "joinConference...request id is null...Retrying attempt " + vidyoRetryCount + " of " + vidyoRetryAttempts +
							" to get eid...will call joinConference");
					vidyoRetryCount++;
					joinConference();
				}
			}
			// max retries have been hit, send error.
			else {
				Log.d(TAG, "joinConference...vidyoRetryCount->" + vidyoRetryCount + "...vidyoRetryAttempts->" + vidyoRetryAttempts + "...will send CONFERENCE_ERROR");
				message_handler.sendEmptyMessage(CONFERENCE_ERROR);
			}
		}

	}
	
	private void conferenceEnded() {
		Log.d(TAG, "conferenceEnded");
		if (!refreshVideo) {
			loginStatus = false;
			conferenceEnded = true;
			stopDevices();
		
			final RelativeLayout wrapUpLayout = (RelativeLayout) findViewById(R.id.wrapup_content);
			wrapUpLayout.setVisibility(View.GONE);
			
			finish();
		}
		else {
			conferenceEnding = false;
		}
	}
	
	private void requestEngagmentStatus() {
		Log.d(TAG, "requestEngagmentStatus start...");
		if (!this.isFinishing()) {
			// code to request the status of the engagement would go here
			onEngagementStatus();
		}
	}
	
	private void conferenceError() {
		Log.d(TAG, "conferenceError");
		killEngagmentTimer();
		sendCancelEngagement();
	}
	
	private void conferenceStarted() {
		Log.d(TAG, "conferenceStarted");


		final RelativeLayout textContent = (RelativeLayout) findViewById(R.id.engagement_text_content);
		textContent.setVisibility(View.GONE);

		if (conferenceStarted) {
			Log.d(TAG, "Resetting audio state");
			setupAudioForEngagement();
		}	
		app.LmiAndroidJniStartMedia();
		app.LmiAndroidJniSetPreviewModeON(false);
		app.LmiAndroidJniSetCameraDevice(1);
		app.LmiAndroidJniMuteCamera(false);
		resizeVideo();
		
		// Set the screen's pixel density
		double density = getResources().getDisplayMetrics().density;
		app.setPixelDensity(density);
		
		conferenceStarted = true;

		if(CallType.equals("CALLDIRECT") == true){
			Log.d(TAG, "conferenceStarted...CallType->CALLDIRECT...will not search Member to invite...");
		}
		else {
			Log.d(TAG, "conferenceStarted...CallType->" + CallType + "...will search Member to invite...");
			Message msg = new Message();
			msg.what = SHOW_MSG;
			Bundle data = new Bundle();
			data.putString("text", "加入对方会议室成功!");
			msg.setData(data);

			message_handler.sendMessage(msg);


		}

	}

	private void searchMembers(final String searchString) {
		Log.i(TAG, "searchMembers...searchString->" + searchString);
		new Thread(new Runnable() {

			@Override
			public void run() {


				String wsdlURL = vidyoInfo.getVidyoHost()+"/services/v1_1/VidyoPortalUserService";
				HttpClient client = new DefaultHttpClient();

				HttpPost postRequest = new HttpPost(wsdlURL);

				String soapBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v1=\"http://portal.vidyo.com/user/v1_1\">"
						+ "<env:Body>" + " <v1:SearchRequest>" + "<v1:Filter>"
						+ "<v1:query>" + searchString + "</v1:query>"
						+ "</v1:Filter>" + "</v1:SearchRequest>"
						+ "</env:Body>" + "</env:Envelope>";

				//try {
				StringEntity se = new StringEntity(soapBody, "UTF-8");
				postRequest.setEntity(se);
				//} catch (UnsupportedEncodingException e1) {
				//	e1.printStackTrace();
				//}

				String soapAction = "search";

				postRequest.setHeader(
						"Authorization",
						"Basic "
								+ Base64.encodeToString((vidyoInfo.getVidyoUsername()
								+ ":" + vidyoInfo.getVidyoPassword())
								.getBytes(), Base64.NO_WRAP));
				postRequest.setHeader("SOAPAction", soapAction);
				postRequest.setHeader("Content-Type",
						"application/soap+xml;charset=UTF-8");

				try {
					HttpResponse response = client.execute(postRequest);

					Log.i(TAG, response.getStatusLine().getStatusCode()
							+ "");

					if (response.getStatusLine().getStatusCode() == 200) {

						HttpEntity entity = response.getEntity();
						InputStream inputStream = entity.getContent();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(inputStream));
						String result = "";
						String line = "";
						while (null != (line = reader.readLine())) {
							result += line;
						}

						Message message = Message.obtain();
						message.what = MSG_SEARCH;
						message.obj = result;
						message_handler.sendMessage(message);

					} else {
						Log.i(TAG, "Search Member Failed...");

						Message msg = new Message();
						msg.what = SHOW_MSG;
						Bundle data = new Bundle();
						data.putString("text", "查询对方的信息失败!");
						msg.setData(data);

						message_handler.sendMessage(msg);

						msg = new Message();
						msg.what = MSG_BYE;

						message_handler.sendMessage(msg);


					}

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();

	}

	public  void setDestEntityId(String entityID)
	{
		Message msg = new Message();
		msg.what = SHOW_MSG;
		Bundle data = new Bundle();
		data.putString("text", "查询对方的信息成功，下面邀请对方加入会议!");
		msg.setData(data);

		message_handler.sendMessage(msg);

		Log.d(TAG, "setDestEntityId...entityID->" + entityID);
		vidyoInfo.setCalledDestEntityID(entityID);

		if(CallType.equals("CALLDIRECT")){
			Log.d(TAG, "setDestEntityId...entityID->" + entityID + "...CallType->CALLDIRECT...will directCall...");
			doCallByNameDirect(vidyoInfo.getCalledDestUser());
		}
		else {
			Log.d(TAG, "setDestEntityId...entityID->" + entityID + "...CallType->" + CallType + "...will invite Dest User...");
			inviteDestUser();
		}
	}

	private void inviteDestUser() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				Log.i(TAG, "DoingActivity.inviteDestUser Start");


				String wsdlURL = vidyoInfo.getVidyoHost() +"/services/v1_1/VidyoPortalUserService";
				HttpClient client = new DefaultHttpClient();

				HttpPost postRequest = new HttpPost(wsdlURL);

				String soapBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v1=\"http://portal.vidyo.com/user/v1_1\">"
						+ "<env:Body>" + "<v1:InviteToConferenceRequest>"
						+ " <v1:conferenceID>" + vidyoResponse.getRequestEid()
						+ "</v1:conferenceID>" + "<v1:entityID>" + vidyoInfo.getCalledDestEntityID()
						+ "</v1:entityID>" + "</v1:InviteToConferenceRequest>"
						+ "</env:Body>" + "</env:Envelope>";

				Log.i(TAG, "DoingActivity.inviteDestUser soapBody->" + soapBody);

				//try {
				StringEntity se = new StringEntity(soapBody, "UTF-8");
				postRequest.setEntity(se);
				//} catch (UnsupportedEncodingException e1) {
				//	e1.printStackTrace();
				//}

				String soapAction = "inviteToConference";

				postRequest.setHeader(
						"Authorization",
						"Basic "
								+ Base64.encodeToString((vidyoInfo.getVidyoUsername()
								+ ":" + vidyoInfo.getVidyoPassword())
								.getBytes(), Base64.NO_WRAP));
				postRequest.setHeader("SOAPAction", soapAction);
				postRequest.setHeader("Content-Type",
						"application/soap+xml;charset=UTF-8");

				try {
					HttpResponse response = client.execute(postRequest);

					Log.i(TAG, response.getStatusLine().getStatusCode()
							+ "");



					if (response.getStatusLine().getStatusCode() == 200) {

						HttpEntity entity = response.getEntity();
						InputStream inputStream = entity.getContent();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(inputStream));
						String result = "";
						String line = "";
						while (null != (line = reader.readLine())) {
							result += line;
						}

						Message message = Message.obtain();
						message.what = MSG_CALL;
						message.obj = result;
						message_handler.sendMessage(message);

						Message msg = new Message();
						msg.what = SHOW_MSG;
						Bundle data = new Bundle();
						data.putString("text", "邀请对方加入会议成功!");
						msg.setData(data);

						message_handler.sendMessage(msg);

					} else {
						Log.i(TAG, "inviteDestUser Failed...");
						Message msg = new Message();
						msg.what = SHOW_MSG;
						Bundle data = new Bundle();
						data.putString("text", "邀请对方加入会议失败!");
						msg.setData(data);

						message_handler.sendMessage(msg);

						msg = new Message();
						msg.what = MSG_BYE;

						message_handler.sendMessage(msg);



					}

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();

	}

	
	public void onEndEngagement() {
		Log.d(TAG, "Engagement ended on server.");
		
		final RelativeLayout wrapUpLayout = (RelativeLayout) findViewById(R.id.wrapup_content);
		wrapUpLayout.setVisibility(View.VISIBLE);
		
		conferenceEnded = true;
		
	}
	
	public void onCancelEngagement() {
		Log.i(TAG, "Engagement canceled by member on server.");
		finish();
	}
	
	private void onEngagementStatus() {
		Log.d(TAG, "onEngagementStatus start...");
		// Much simplified version here

		final RelativeLayout refreshVideoLayout = (RelativeLayout) findViewById(R.id.refresh_video_content);
		refreshVideoLayout.setVisibility(View.GONE);
		
		Log.d(TAG, "onEngagementStatus...Engagement started: " + engagementStarted);
		
		if (conferenceEnded) {
			Log.d(TAG, "onEngagementStatus..." + engagementStarted + "...conferenceEnded->true...now will call finish...");
			finish();

		}
		else if (!conferenceEnded && !conferenceEnding && refreshVideo) {
			Log.d(TAG, "onEngagementStatus..." + "..refreshVideo->true...now will call joinRoom...");
			refreshVideoBtn.setEnabled(true);
			joinRoom();
		}
		else if (!conferenceEnded && conferenceEnding && refreshVideo) {
			Log.d(TAG, "refreshVideo->true and conferenceEnding->true...will waiting for conference to end on refresh");
		}
		else if (!engagementStarted){
			Log.d(TAG, "onEngagementStatus..." + "..engagementStarted->false...now will call startEngagement...");
			startEngagement();
		}
		else{
			Log.d(TAG, "onEngagementStatus...do nothing...");
		}
	}
	
	private void onMemberConnectedResponse() {
		
		Log.d(TAG, "Application acknowledged that we are connected.  Converstaion should be starting up.");
		// Do some Vidyo type stuff here? Disable button, echo cancellation etc...
		rotateScreen(currentRotation);
		
		endBtn.setEnabled(true);
		refreshVideoBtn.setEnabled(true);
		memberConnected = true;
	}
	
	private Runnable engagementTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "The engagement timer has timeout out.");
			if (!joinedRoom) {
				engagementTimeoutDialog();
			}
		}
	};
	
	// //////////////////////////////////////////////////////////////////
	// RESPONDER FRAGMENTS and RESPONDER FRAGMENT CALLBACKS
	// //////////////////////////////////////////////////////////////////

	private LmiAndroidJniLoginCallbacks loginCallbacks;
	private LmiAndroidJniConferenceCallbacks conferenceCallbacks;
	private LmiAndroidJniChatCallbacks chatCallbacks;
	private LmiAndroidJniMessageCallbacks messageCallbacks;
	
	public void constructJniInterface() {
		Log.d(TAG, "constructJniInterface start...");

		loginCallbacks = new LmiAndroidJniLoginCallbacks("com/vidyo/vidyosample/activity/VidyoSampleActivity", "vidyoLoginStatusCallback");
		app.LmiAndroidJniLoginSetCallbacks(loginCallbacks);
		
		conferenceCallbacks = new LmiAndroidJniConferenceCallbacks("com/vidyo/vidyosample/activity/VidyoSampleActivity",
				"vidyoConferenceStatusCallback",
				"vidyoConferenceEventCallback",
				"vidyoConferenceShareEventCallback",
				"vidyoFeccCameraControl",
				"vidyoCameraSwitchCallback",
				"vidyoNotifyParticipantsChanged");
		app.LmiAndroidJniConferenceSetCallbacks(conferenceCallbacks);

		chatCallbacks = new LmiAndroidJniChatCallbacks("com/vidyo/vidyosample/activity/VidyoSampleActivity", "vidyoChatMsgRcvCallback");
		app.LmiAndroidJniChatSetCallbacks(chatCallbacks);
		
		messageCallbacks = new LmiAndroidJniMessageCallbacks("com/vidyo/vidyosample/activity/VidyoSampleActivity", "vidyoMessageOutMsgCallback");
		app.LmiAndroidJniMessageSetCallbacks(messageCallbacks);
	}
	
	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Login Related Definitions and Methods
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/
	private String GetLoginStatusCH(int loginStatus)
	{
		switch (loginStatus) {
			case LmiAndroidJniLoginCallbacks.STATUS_LOGIN_COMPLETE:
				return "STATUS_LOGIN_COMPLETE";
			case LmiAndroidJniLoginCallbacks.STATUS_LOGGING_IN:
				return "STATUS_LOGGING_IN";
			case LmiAndroidJniLoginCallbacks.STATUS_LOGGED_OUT:
				return "STATUS_LOGGED_OUT";
			case LmiAndroidJniLoginCallbacks.STATUS_PORTAL_PREFIX:
				return "STATUS_PORTAL_PREFIX";
			case LmiAndroidJniLoginCallbacks.STATUS_DISCONNECT_FROM_GUESTLINK:
				return "STATUS_DISCONNECT_FROM_GUESTLINK";
			case LmiAndroidJniLoginCallbacks.STATUS_GUEST_LOGIN_CONFERENCE_ENDED:
				return "STATUS_GUEST_LOGIN_CONFERENCE_ENDED";
			default:
				return "UNKNOWN";
		}
	}

	public void vidyoLoginStatusCallback(int loginStatus, int loginError, String loginMsg) {
		Log.d(TAG, "applicationJniLoginStatusCallback: loginStatus=" + GetLoginStatusCH(loginStatus) + ", loginError="+loginError);
		switch (loginStatus) {
		case LmiAndroidJniLoginCallbacks.STATUS_LOGIN_COMPLETE:
			vidyoSignedInCallback(loginError, loginMsg);
			break;
		case LmiAndroidJniLoginCallbacks.STATUS_LOGGING_IN:
			break;
		case LmiAndroidJniLoginCallbacks.STATUS_LOGGED_OUT:
			signedOutCallback(loginMsg);
			break;
		case LmiAndroidJniLoginCallbacks.STATUS_PORTAL_PREFIX:
			break;
		case LmiAndroidJniLoginCallbacks.STATUS_DISCONNECT_FROM_GUESTLINK:
			break;
		case LmiAndroidJniLoginCallbacks.STATUS_GUEST_LOGIN_CONFERENCE_ENDED:
			break;
		}
	}

	private void vidyoSignedInCallback(int loginStatus, String loginMsg) {
		Log.d(TAG, "Signed into Vidyo Portal...loginStatus->" + loginStatus + "...loginMsg->" + loginMsg);

		Message msg = new Message();
		msg.what = SHOW_MSG;
		Bundle data = new Bundle();
		if(loginStatus == 0) {
			if(CallType.equals("CALLDIRECT")) {
				data.putString("text", "签入成功!下面呼叫对方");
			}
			else if(CallType.equals("JOIN")){
				data.putString("text", "签入成功!下面加入会议室");
			}

		}
		else{
			data.putString("text", "签入失敗!");
		}
		msg.setData(data);

		message_handler.sendMessage(msg);

		// reset the vidyoResponse
		if(loginStatus == 0) {
			vidyoResponse = null;
			//暂时不调用Join Conference

			message_handler.sendEmptyMessage(JOIN_CONFERENCE);
		}

	}
	
	private void signedOutCallback(String loginMsg) {
		Log.d(TAG, "Signed Out received!...will call finish...");
		loginStatus = false;

		Message msg = new Message();
		msg.what = MSG_BYE;

		message_handler.sendMessage(msg);

		//sendEndEngagement();
	}
	
	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Conference Related Definitions and Methods
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/
	private String GetConferenceStatusCH(int status)
	{
		switch (status) {
			case LmiAndroidJniConferenceCallbacks.STATUS_JOIN_COMPLETE:
				return "STATUS_JOIN_COMPLETE";

			case LmiAndroidJniConferenceCallbacks.STATUS_JOIN_PROGRESS:
				return "STATUS_JOIN_PROGRESS";
			case LmiAndroidJniConferenceCallbacks.STATUS_GUEST_JOIN_ERROR:
				return "STATUS_GUEST_JOIN_ERROR";
			case LmiAndroidJniConferenceCallbacks.STATUS_CALL_ENDED:
				return "STATUS_CALL_ENDED";
			case LmiAndroidJniConferenceCallbacks.STATUS_INCOMING_CALL_REQUEST:
				return "STATUS_INCOMING_CALL_REQUEST";
			case LmiAndroidJniConferenceCallbacks.STATUS_INCOMING_CALL_CANCELLED:
				return "STATUS_INCOMING_CALL_CANCELLED";
			case LmiAndroidJniConferenceCallbacks.STATUS_INCOMING_END_CALLING:
				return "STATUS_INCOMING_END_CALLING";
			default:
				return "UNKNOWN";
		}
	}
	public void vidyoConferenceStatusCallback(int status, int error, String message) {

		Log.d(TAG, "applicationJniConferenceStatusCallback: Status->" + GetConferenceStatusCH(status) + ", error="+error + ", message->" + message);
		switch (status) {
		case LmiAndroidJniConferenceCallbacks.STATUS_JOIN_COMPLETE:
			callStartedCallback(error);
			break;
		case LmiAndroidJniConferenceCallbacks.STATUS_JOIN_PROGRESS:
			callStartingCallback();
			break;
		case LmiAndroidJniConferenceCallbacks.STATUS_GUEST_JOIN_ERROR:
			break;
		case LmiAndroidJniConferenceCallbacks.STATUS_CALL_ENDED:
			vidyoConferenceEnded();
			break;
		case LmiAndroidJniConferenceCallbacks.STATUS_INCOMING_CALL_REQUEST:
			vidyoIncomingCallRequest(message);
			break;
		case LmiAndroidJniConferenceCallbacks.STATUS_INCOMING_CALL_CANCELLED:
			break;
		case LmiAndroidJniConferenceCallbacks.STATUS_INCOMING_END_CALLING:
			if(LmiAndroidJniConferenceCallbacks.STATUS_JOIN_PROGRESS == lastConfStatus){

				/*
				Log.d(TAG, "applicationJniConferenceStatusCallback: Status from STATUS_JOIN_PROGRESS->STATUS_INCOMING_END_CALLING...remote does not answer call...");

				Message msg = new Message();
				msg.what = SHOW_MSG;
				Bundle data = new Bundle();
				data.putString("text", "视频对方未应答");
				msg.setData(data);

				message_handler.sendMessage(msg);

				msg = new Message();
				msg.what = MSG_BYE;

				message_handler.sendMessage(msg);
				*/

			}

			break;
		}

		lastConfStatus = status;
	}



	private void callStartedCallback(int error) {	
		Log.d(TAG, "Call started received!");
		if(error != 0){
			Log.d(TAG, "Call started received。。。there is error...will not send CONFERENCE_STARTED and quit...");
			Message msg = new Message();
			msg.what = SHOW_MSG;
			Bundle data = new Bundle();
			data.putString("text", "呼叫失败");
			msg.setData(data);

			message_handler.sendMessage(msg);

			msg = new Message();
			msg.what = MSG_BYE;

			message_handler.sendMessage(msg);



		}
		else {
			Log.d(TAG, "Call started received。。。there is error...will send CONFERENCE_STARTED and go on...");
			message_handler.sendEmptyMessage(CONFERENCE_STARTED);
		}

	}
	
	private void callStartingCallback() {
		Log.d(TAG, "callStartingCallback received!");
	}
	
	private void vidyoIncomingCallRequest(String caller) {
		Log.d(TAG, "GOT INCOMING CALL FROM "+caller);
	}
	
	private void vidyoConferenceEnded() {
		Log.d(TAG, "vidyoConferenceEnded");
		message_handler.sendEmptyMessage(CONFERENCE_ENDED);
	}

	private String GetConferenceEventCH(int event)
	{

		switch (event) {
			case LmiAndroidJniConferenceCallbacks.EVENT_RECORDING_STATUS:
				return "EVENT_RECORDING_STATUS";
			case LmiAndroidJniConferenceCallbacks.EVENT_WEBCASTING_STATUS:
				return "EVENT_WEBCASTING_STATUS";
			case LmiAndroidJniConferenceCallbacks.EVENT_SERVER_VIDEO_MUTE:
				return "EVENT_SERVER_VIDEO_MUTE";
			case LmiAndroidJniConferenceCallbacks.EVENT_CAMERA_ENABLED:
				return "EVENT_CAMERA_ENABLED";
			case LmiAndroidJniConferenceCallbacks.EVENT_MIC_ENABLED:
				return "EVENT_MIC_ENABLED";
			case LmiAndroidJniConferenceCallbacks.EVENT_SPEAKER_ENABLED:
				return "EVENT_SPEAKER_ENABLED";
			case LmiAndroidJniConferenceCallbacks.EVENT_GUI_CHANGED:
				return "EVENT_GUI_CHANGED";
			case LmiAndroidJniConferenceCallbacks.EVENT_FECC_BUTTON_CLICK:
				return "EVENT_FECC_BUTTON_CLICK";
			default:
				return "UNKNOWN";
		}
	}

	public void vidyoConferenceEventCallback(int event, boolean state) {
		Log.d(TAG, "applicationJniConferenceEventCallback: event="+GetConferenceEventCH(event)+", state="+state);
		switch (event) {
		case LmiAndroidJniConferenceCallbacks.EVENT_RECORDING_STATUS:
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_WEBCASTING_STATUS:
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_SERVER_VIDEO_MUTE:
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_CAMERA_ENABLED:
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_MIC_ENABLED:
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_SPEAKER_ENABLED:
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_GUI_CHANGED:
			resizeVideo();
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_FECC_BUTTON_CLICK:
			break;
		}
	}
	
	public void vidyoConferenceShareEventCallback(int eventType, String shareURI) {
		Log.d(TAG, "vidyoConferenceShareEventCallback start...shareURI->" + shareURI);
		switch (eventType) {
			case LmiAndroidJniConferenceCallbacks.EVENT_SHARE_ADDED:

				app.setWindowShares(0,shareURI);
				break;

			case LmiAndroidJniConferenceCallbacks.EVENT_SHARE_REMOVED:
				resizeVideo();
				break;

			default: {
			}
		}
	}
	
	private void vidyoFeccCameraControl(String commandId, int cameraCommand) {
		Log.d(TAG, "vidyoFeccCameraControl start...");
	}

	private void vidyoCameraSwitchCallback(String name) {
		Log.d(TAG, "Switch camera: " + name);
	}
	
	private void vidyoNotifyParticipantsChanged(int numOfParticipants) {
		Log.d(TAG, "notifyParticipantsChanged...numOfParticipants->" + numOfParticipants);

		if(numOfParticipants <= 1){
			Log.d(TAG, "notifyParticipantsChanged...numOfParticipants->" + numOfParticipants + "...will quit conference...");
			//sendEndEngagement();
			Message msg = new Message();
			msg.what = MSG_BYE;

			message_handler.sendMessage(msg);

			//finish();
		}

	}

	
	
	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Chat Related Definitions and Methods
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	private void vidyoChatMsgRcvCallback(boolean groupChat, String uri, String name, String message) {
		Log.d(TAG, "Got chat message from: "+name);
		Log.d(TAG, "Chat msg: "+message);
	}

	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Message Related Definitions and Methods
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	public void vidyoMessageOutMsgCallback(String s) {
		Log.d(TAG, "Got Message: "+s);


		Message msg = new Message();
		msg.what = SHOW_MSG;
		Bundle data = new Bundle();
		data.putString("text", s);
		msg.setData(data);

		message_handler.sendMessage(msg);

		if(s.contains("declined the call")){
			msg = new Message();
			msg.what = MSG_BYE;

			message_handler.sendMessage(msg);


		}

	}

	
	
	
	private void resizeVideo() {
		Log.d(TAG, "resizeVideo start...");
		if (bcView != null) {
			int width = bcView.getWidth();
			int height = bcView.getHeight();
			app.LmiAndroidJniResize(width, height);
		}
//		final Display display = getWindowManager().getDefaultDisplay();
//		LmiDeviceManagerViewResize(display.getWidth(), display.getHeight());
	}
	
	
	
	

	private void getVidyoAccountInfo() {
		Log.d(TAG, "getVidyoAccountInfo Start");
		final FragmentManager fm = getFragmentManager();
		final FragmentTransaction ft = fm.beginTransaction();
		
		VidyoMyAccountResponderFragment responder = VidyoMyAccountResponderFragment.newInstance(vidyoInfo);
		ft.add(responder, VIDYO_MY_ACCOUNT_RESPONDER_TAG);
		ft.commit();
	}	
	
	private void joinRoom() {
		Log.d(TAG, "Attempting to join room with eid: " + vidyoResponse.getRequestEid());

		if(CallType.equals("CALLDIRECT")){
			Log.d(TAG, "joinConference start...CallType->CALLDIRECT");
			doCallByNameDirect(vidyoInfo.getCalledDestUser());
			return;
		}
		else if(CallType.equals("JOIN")) {
			Log.d(TAG, "joinConference start...CallType->JOIN...will join dest...");
			app.doJoinByName(vidyoInfo.getCalledDestUser(),"");

			/*
			final FragmentManager fm = getFragmentManager();
			final FragmentTransaction ft = fm.beginTransaction();

			Fragment responder = VidyoJoinConferenceResponderFragment.newInstance(vidyoInfo, vidyoResponse.getRequestEid());
			ft.add(responder, VIDYO_JOIN_CONFERENCE_RESPONDER_TAG);
			ft.commit();
			*/

		}
		else if(CallType.equals("GUEST_JOIN")){
			Log.d(TAG, "joinConference start...CallType->GUEST_JOIN...will do nothing here...");
			//app.LmiAndroidJniHandleGuestLink(vidyoInfo.getGuest_host(),vidyoInfo.getGuest_port(),vidyoInfo.getGuest_key(),vidyoInfo.getGuest_userName(),vidyoInfo.getGuest_pin(),false);
		}

	}

	private void doCallByNameDirect(String sName){
		Log.d(TAG, "doCallByNameDirect Start...sName->" + sName);
		app.doCallByNameDirect(sName);
	}




	/**
	 * Callback for VidPortal Soap request 'myAccount'
	 */
	@Override
	public void onVidyoMyAccountUpdated(final VidyoResponse vidyoResponse) {
		Log.d(TAG, "onVidyoMyAccountUpdated called");
		this.vidyoResponse = vidyoResponse;
		joinConference();
	}
	
	/**
	 * Callback for VidPortal Soap request 'joinConference' with 200 status code
	 */
	@Override
	public void onVidyoJoinConferenceUpdated() {
		Log.d(TAG, "onVidyoJoinConferenceUpdated Called.");
		joinedRoom = true;
		refreshVideo = false;
		 
		Log.d(TAG, "The engagement timer has been stopped.");
		timeoutHandler.removeCallbacks(engagementTimeoutRunnable);
		timeoutHandlerRegistered = false;
		 								 
		// need to get rid of the 'please waiting text' since the provider has just joined the room
		final RelativeLayout textContent = (RelativeLayout) findViewById(R.id.engagement_text_content);
		textContent.setVisibility(View.GONE); 
		
		if (!memberConnected) {
			message_handler.sendEmptyMessage(MEMBER_CONNECTED);
		}
	}
	
	/**
	 * Callback for VidPortal Soap request 'joinConference' with failed status
	 */
	@Override
	public void onVidyoJoinConferenceError(final int statusCode, final String resultData) {
		Log.d(TAG, "onVidyoJoinConferenceError called");
		Log.e(TAG, "Soap response = " + resultData);
		Log.e(TAG, "Attempting to join room with eid: " + vidyoResponse.getRequestEid() + 
				" failed with status code " + statusCode);
		vidyoRetryCount++;
		joinConference();
	}
	
	
	
	// NATIVE LAYER
	//////////////////////////////////////////////////////////////////
	
	public void LmiDeviceManagerViewRender() {
		app.LmiAndroidJniRender();
	}

	public void LmiDeviceManagerViewResize(final int width, final int height) {
		app.LmiAndroidJniResize(width, height);
	}

	public void LmiDeviceManagerViewRenderRelease() {
		app.LmiAndroidJniRenderRelease();
		resizeVideo();
	}

	public void LmiDeviceManagerViewTouchEvent(final int id, final int type, final int x, final int y) {
		if (!engagementStarted) {
			cancelConfirmCancelButton();
		}
		app.LmiAndroidJniTouchEvent(id, type, x, y);
	}

	public int LmiDeviceManagerCameraNewFrame(final byte[] frame, final String fourcc, final int width,
			final int height, final int orientation, final boolean mirrored) {
		return app.SendVideoFrame(frame, fourcc, width, height, orientation, mirrored);
	}

	public int LmiDeviceManagerMicNewFrame(final byte[] frame, final int numSamples, final int sampleRate,
			final int numChannels, final int bitsPerSample) {
		return app.SendAudioFrame(frame, numSamples, sampleRate, numChannels, bitsPerSample);
	}

	public int LmiDeviceManagerSpeakerNewFrame(final byte[] frame, final int numSamples, final int sampleRate,
			final int numChannels, final int bitsPerSample) {
		return app.GetAudioFrame(frame, numSamples, sampleRate, numChannels, bitsPerSample);
	}

	// AUDIO MANAGEMENT
	// //////////////////////////////////////////////////////////////////
			
	private void setupAudioForEngagement() {
		Log.d(TAG, "setupAudioForEngagement start...");
		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		
		final int mode = audioManager.getMode();
		
		if (mode == AudioManager.MODE_NORMAL) {			
			final int volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
			audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, 0);
			audioManager.setMode(AudioManager.MODE_NORMAL);
			audioManager.setSpeakerphoneOn(true);
		}
		else {
			audioManager.setMode(AudioManager.MODE_IN_CALL);
			audioManager.setSpeakerphoneOn(false);
			audioManager.setMicrophoneMute(false);
		}  
	}
	
	// A receiver that detects when the headphones have been plugged/unplugged
	// and diverts the audio to the correct speaker
	private BroadcastReceiver audioReceiver = new BroadcastReceiver() {
	
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
								
				// 0 = unplugged, 1 = plugged
				if (intent.getIntExtra("state", 0) == 0) {
					
					// audio stream during the waiting room video needs to change
					if (!engagementStarted) {
						final int volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2;
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
					}
					// audio stream during the engagement needs to change
					else {
						final int volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
						audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, 0);
					}
					
					audioManager.setMode(AudioManager.MODE_NORMAL);
					audioManager.setSpeakerphoneOn(true);
				}
				else {
					audioManager.setMode(AudioManager.MODE_IN_CALL);
					audioManager.setSpeakerphoneOn(false);
					audioManager.setMicrophoneMute(false);
				}
			}
		}	  
	};
	
	// This receiver will block all incoming calls from interrupting an engagement
	private BroadcastReceiver blockingCallReceiver = new BroadcastReceiver() {
	
		@Override
		public void onReceive(Context context, Intent intent) {	
			try {
			
				if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
					String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
					
					if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
						
						String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
						Log.d(TAG, "Incoming Phone Call Ignored: " + incomingNumber);
						
						TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
					
						Class<?> classTelephony = Class.forName(telephonyManager.getClass().getName());
	                    Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");
	                    methodGetITelephony.setAccessible(true);
	                      
	                    Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);
	                    Class<?> telephonyInterfaceClass = Class.forName(telephonyInterface.getClass().getName());
	                    Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");
	                    methodEndCall.invoke(telephonyInterface);
					}
				}
			}
			catch (Exception e) {
				Log.d(TAG, "An error occurred: " + e.getMessage());
			}	
		}
	};
	
	private static final class MessageHandler extends Handler {
		VidyoSampleActivity activity;

		public MessageHandler(final VidyoSampleActivity activity) {
			super();
			this.activity = activity;
		}

		private String GetMessageCN(int what)
		{
			switch (what) {
				case GET_ENGAGEMENT_STATUS:
					return "GET_ENGAGEMENT_STATUS";
				case MEMBER_CONNECTED:
					return "MEMBER_CONNECTED";
				case END_ENGAGEMENT:
					return "END_ENGAGEMENT";
				case JOIN_CONFERENCE:
					return "JOIN_CONFERENCE";
				case CONFERENCE_ENDED:
					return "CONFERENCE_ENDED";
				case CONFERENCE_ERROR:
					return "CONFERENCE_ERROR";
				case CONFERENCE_STARTED:
					return "CONFERENCE_STARTED";
				case SHOW_MSG:
					return "SHOW_MSG";
				case MSG_SEARCH:
					return "MSG_SEARCH";
				case MSG_BYE:
					return "MSG_BYE";
				default:
					return "UNKNOWN";
			}
		}
		@Override
		public void handleMessage(final Message msg) {
			Log.d(TAG, "handleMessage..." + GetMessageCN(msg.what));

			if (activity.isFinishing()) {
				Log.d(TAG, "handleMessage..." + GetMessageCN(msg.what) + "..isFinishing->true...ignore and return...");
				return;
			}
			switch (msg.what) {
			case GET_ENGAGEMENT_STATUS:
				activity.requestEngagmentStatus();
				break;
			case MEMBER_CONNECTED:
				activity.sendMemberConnected();
				break;
			case END_ENGAGEMENT:
				activity.sendEndEngagement();
				break;
			case JOIN_CONFERENCE:
				activity.joinConference();
				break;
			case CONFERENCE_ENDED:
				activity.conferenceEnded();
				break;
			case CONFERENCE_ERROR:
				activity.conferenceError();
				break;
			case CONFERENCE_STARTED:
				activity.conferenceStarted();
				break;

			case SHOW_MSG:
				MyShowMsg(msg);
				break;

			case MSG_SEARCH:

				String resultSearch = (String) msg.obj;
				Log.i(TAG, "Search result->" + resultSearch);

				// 解析结果
				parseXMLWithDom(resultSearch);

				break;
			case MSG_BYE:
				activity.sendEndEngagement();
				break;
			}
		}


		private void MyShowMsg(Message msg)
		{
			Bundle bundle = msg.getData();
			String data = bundle.getString("text");

			try {
				Toast.makeText(activity, data, Toast.LENGTH_SHORT).show();
			}
			catch(Exception ex){
				Log.d(TAG,"Toast.makeText..." + ex.getMessage());
			}

		}

		private void parseXMLWithDom(String response) {

			String total = "";

			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				InputSource is = new InputSource(new StringReader(response));

				Document doc = builder.parse(is);

				NodeList totalNodes = doc.getElementsByTagName("ns1:total");
				NodeList memberNameNodes = doc.getElementsByTagName("ns1:Entity");

				if (totalNodes.getLength() > 0) {
					Element element = (Element) totalNodes.item(0);
					NodeList entityIDs = element.getChildNodes();
					Node entityID = entityIDs.item(0);
					total = entityID.getNodeValue();
					Log.i(TAG, " 搜索到的总数： " + total);
				}

				if (memberNameNodes.getLength() > 0) {

					for (int i = 0; i < memberNameNodes.getLength(); i++) {
						Element element = (Element) memberNameNodes.item(i);

						String name = element
								.getElementsByTagName("ns1:displayName").item(0)
								.getFirstChild().getNodeValue();
						String entityID = element
								.getElementsByTagName("ns1:entityID").item(0)
								.getFirstChild().getNodeValue();

						String isContacts = element
								.getElementsByTagName("ns1:isInMyContacts").item(0)
								.getFirstChild().getNodeValue();

						Log.i(TAG, " name->" + name + "...entityID->" + entityID + "...isContacts->" + isContacts);
						activity.setDestEntityId(entityID);
						break;

					}

				}



			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}



	public static synchronized void addEventListener(VidyoEventListener l){
		_eventListeners.addElement(l);

	}

	public static synchronized void removeEventListener(VidyoEventListener l){
		_eventListeners.removeElement(l);

	}

}
