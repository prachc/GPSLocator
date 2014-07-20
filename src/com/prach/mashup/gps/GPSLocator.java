package com.prach.mashup.gps;

import java.text.NumberFormat;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class GPSLocator extends Activity implements LocationListener{
	/** Called when the activity is first created. */
	private final int UPDATE_LOCATION = 0xff;
	
	//private String TAG = "GPSLocation";
	private TextView tblatdec,tblngdec,tblatdeg,tblngdeg,tbprovider; 
	private Button refresh,finish;
	private LocationManager mLocationManager;
	private double mLatitude,mLongitude;
	private Thread mThread;
	private String provider;
	private NumberFormat nf = NumberFormat.getInstance();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.main);
		nf.setMaximumFractionDigits(6);
		
		tblatdec = (TextView) findViewById(R.id.textview_latdec);
		tblatdeg = (TextView) findViewById(R.id.textview_latdeg);
		tblngdec = (TextView) findViewById(R.id.textview_lngdec);
		tblngdeg = (TextView) findViewById(R.id.textview_lngdeg);
		tbprovider = (TextView) findViewById(R.id.textview_provider);
		initializeLocationAndStartGpsThread();
		refreshLocation();

		refresh = (Button) findViewById(R.id.button_refresh);

		refresh.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				refreshLocation();
			}
		});

		finish = (Button) findViewById(R.id.button_finish);

		finish.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mLocationManager.removeUpdates(GPSLocator.this);
				stopThread(mThread);
				intentFinish();
			}
		});	
	}

	private void intentFinish(){
		Intent intent = getIntent();
		if(intent!=null){
			intent.putExtra("LATITUDE", nf.format(mLatitude));
			intent.putExtra("LONGITUDE", nf.format(mLongitude));
			//String[] coor = {nf.format(mLatitude),nf.format(mLongitude)};
			//intent.putExtra("COOR", coor);
			intent.putExtra("PROVIDER", provider);
			this.setResult(Activity.RESULT_OK, intent);
		}
		GPSLocator.this.finish();
	}
	
	/*private String[] getLocation(){
    	Location loc;

    	mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    	List<String> providers = mLocationManager.getProviders(true);
    	Log.i("GPSLocator", "Enabled providers = " + providers.toString());

    	String provider = mLocationManager.getBestProvider(new Criteria(),true);
    	Log.i("GPSLocator", "Best provider = " + provider);
     	loc = mLocationManager.getLastKnownLocation(provider);

    	float Lat =  (float)loc.getLatitude();
    	float Lon =  (float)loc.getLongitude();

    	return new String[] {""+Lat,""+Lon,provider};
    }*/

	private void refreshLocation(){
		//String[] location = getLocation();
		//lat.setText(location[0]);
		//lng.setText(location[1]);
		//provider.setText(location[2]);
		Message msg = Message.obtain();
		msg.what = UPDATE_LOCATION;
		GPSLocator.this.updateHandler.sendMessage(msg);
		
		tblatdec.setText(nf.format(mLatitude));
		tblatdeg.setText(toDegree(Coordinates.convert(mLatitude, Coordinates.DD_MM_SS)));
		tblngdec.setText(nf.format(mLongitude));
		tblngdeg.setText(toDegree(Coordinates.convert(mLongitude, Coordinates.DD_MM_SS)));
		tbprovider.setText(provider);
	}
	
	private String toDegree(String rawdegree){
		String[] temp = rawdegree.split(":");
		return temp[0]+"\u00b0 "+temp[1]+"\' "+temp[2].substring(0, 2)+"\"";
		
	}

	private void initializeLocationAndStartGpsThread() {
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		List<String> providers = mLocationManager.getProviders(true);
		Log.i("GPSLocator", "Enabled providers = " + providers.toString());
		provider = mLocationManager.getBestProvider(new Criteria(),true);
		Log.i("GPSLocator", "Best provider = " + provider);

		setCurrentGpsLocation(null);   
		mThread = new Thread(new MyThreadRunner());
		mThread.start();
	}

	private void setCurrentGpsLocation(Location location) {
		//Log.i("","here");
		
		//String bestProvider = "";
		if (location == null) {
			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 10000, 0, this); // Every 30000 msecs	
			location = mLocationManager.getLastKnownLocation(provider);	
			
		}
		try {
			mLongitude = location.getLongitude();
			mLatitude = location.getLatitude();
			Log.i("GPSLocator", "<long,lat> = <" + mLongitude + "," + mLatitude+">");
			Message msg = Message.obtain();
			msg.what = UPDATE_LOCATION;
			GPSLocator.this.updateHandler.sendMessage(msg);
		} catch (NullPointerException e) {
			Log.i("GPSLocator", "Null pointer exception " + mLongitude + "," + mLatitude);
		}
	}

	public synchronized void stopThread(Thread t) {
		if (t != null) {
			Thread moribund = t;
			t = null;
			moribund.interrupt();
		}
	}

	Handler updateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case UPDATE_LOCATION:
					Log.i("GPSLocator", "Updated location = " + mLatitude + " " + mLongitude);
					mLocationManager.removeUpdates(GPSLocator.this);
					break;
				default:
					super.handleMessage(msg);
			}
		}
	};

	class MyThreadRunner implements Runnable {
		// @Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				Message m = Message.obtain();
				m.what = 0;
				GPSLocator.this.updateHandler.sendMessage(m);
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		setCurrentGpsLocation(location);
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		//setCurrentGpsLocation(null);

	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		//setCurrentGpsLocation(null);

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		//setCurrentGpsLocation(null);

	}

	@Override
	protected void onPause(){
		stopThread(mThread);
		mLocationManager.removeUpdates(this);
		super.onPause();
		//mThread.stop();
	}
	
	@Override
	protected void onDestroy(){
		stopThread(mThread);
		mLocationManager.removeUpdates(this);
		super.onDestroy();
		//mThread.stop();
	}

	@Override
	public void onResume(){
		super.onResume();
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		List<String> providers = mLocationManager.getProviders(true);
		Log.i("GPSLocator", "Enabled providers = " + providers.toString());
		provider = mLocationManager.getBestProvider(new Criteria(),true);
		Log.i("GPSLocator", "Best provider = " + provider);

		setCurrentGpsLocation(null);   
		mThread = new Thread(new MyThreadRunner());
		mThread.start();
		
		Intent intent = getIntent();
		String mode = intent.getStringExtra("MODE");
		String type = intent.getStringExtra("TYPE");
		
		if(intent!=null){
			if(mode!=null){
				if(mode.equals("PASSIVE")&&type.equals("null")){
					//intent.putExtra("LAT", nf.format(mLatitude));
					//intent.putExtra("LNG", nf.format(mLongitude));
					//String[] coor = {nf.format(mLatitude),nf.format(mLongitude)};
					intent.putExtra("LATITUDE", nf.format(mLatitude));
					intent.putExtra("LONGITUDE", nf.format(mLongitude));
					//intent.putExtra("COOR", coor);
					intent.putExtra("PROVIDER", provider);
					this.setResult(Activity.RESULT_OK, intent);
					mLocationManager.removeUpdates(GPSLocator.this);
					stopThread(mThread);
					GPSLocator.this.finish();
				}else if(mode.equals("PASSIVE")&&type.equals("JSON")){
					intent.putExtra("JSON_RESULT", "{\"latitude\":\""+nf.format(mLatitude)+"\",\"longitude\":\""+nf.format(mLongitude)+"\"}");
					this.setResult(Activity.RESULT_OK, intent);
					mLocationManager.removeUpdates(GPSLocator.this);
					stopThread(mThread);
					GPSLocator.this.finish();
				}else if(mode.equals("ACTIVE")){
					
				}else{
					this.setResult(Activity.RESULT_CANCELED, intent);
					mLocationManager.removeUpdates(GPSLocator.this);
					stopThread(mThread);
					GPSLocator.this.finish();
				}
			}
		}
	}


}