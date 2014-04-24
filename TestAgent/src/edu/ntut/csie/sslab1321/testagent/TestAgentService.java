/**
 * 
 */
package edu.ntut.csie.sslab1321.testagent;

import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * @author Reverof
 */
public class TestAgentService extends Service {
	private CommandReceiver mCommandReceiver;
	
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(TestIntent.ACTION_STOP_TESTAGENT);
		intentFilter.addAction(TestIntent.ACTION_CONNECT_TO_WIFI);
		mCommandReceiver = new CommandReceiver();
		registerReceiver(mCommandReceiver, intentFilter);
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		setNotification(intent);
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mCommandReceiver);
		super.onDestroy();
	}
	
	// Make this service wouldn't stop when device becomes sleep.
	private void setNotification(Intent intent) {
		PendingIntent pendingIntnet = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle("Test Agent Service");
		builder.setTicker("Test Agent Service started...");
		builder.setContentText("Test Agent Service is running...");
		builder.setContentIntent(pendingIntnet);
		builder.setOngoing(true);
		Notification notification = builder.build();
		notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
		startForeground(1, notification);
	}
	
	private void connectToWiFi(Bundle bundle) {
		// You need to create WifiConfiguration instance like this:
		String networkSSID = "IAT";
		String networkPass = "smart@inhon";
		WifiConfiguration conf = new WifiConfiguration();
		conf.SSID = "\"" + networkSSID + "\""; // Please note the quotes. String should contain ssid in quotes
		// Then, for WEP network you need to do this:
//		conf.wepKeys[0] = "\"" + networkPass + "\"";
//		conf.wepTxKeyIndex = 0;
//		conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//		conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
		// For WPA network you need to add passphrase like this:
		conf.preSharedKey = "\"" + networkPass + "\"";
		// For Open network you need to do this:
//		conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		// Then, you need to add it to Android wifi manager settings:
		WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		wifiManager.addNetwork(conf);
		// And finally, you might need to enable it, so Android conntects to it:
		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration i : list) {
			if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
				wifiManager.disconnect();
				wifiManager.enableNetwork(i.networkId, true);
				wifiManager.reconnect();
				break;
			}
		}
		Log.i(TestIntent.RESULT_CONNECT_TO_WIFI, "Connect to WiFi");
	}
	
	// Wait for command received
	private class CommandReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Bundle bundle = intent.getExtras();
			if (action.equals(TestIntent.ACTION_STOP_TESTAGENT)) {
				context.stopService(new Intent(context, TestAgentService.class));
			} else if (action.equals(TestIntent.ACTION_CONNECT_TO_WIFI)) {
				connectToWiFi(bundle);
			}
		}

	}
}
