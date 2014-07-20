package com.prach.mashup.gps;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

public class GPSService extends Service implements LocationListener{
	private static final String TAG = "com.prach.mashup.GPSService";
	private static final int GPS_SERVICE_CODE = 0x67707301; //GPS1
	//private NumberFormat nf = NumberFormat.getInstance();
	private double mLatitude,mLongitude;
	private String provider;
	private LocationManager mLocationManager;
	
	private class GPSServiceBinder extends Binder {
		@Override
		protected synchronized boolean onTransact(int code, Parcel data, Parcel reply,int flags) {
			//Init locman and List all providers
			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			List<String> providers = mLocationManager.getProviders(true);
			Log.i("GPSLocator", "Enabled providers = " + providers.toString());
			
			provider = mLocationManager.getBestProvider(new Criteria(),true);
			Log.i("GPSLocator", "Best provider = " + provider);
			
			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			Location location = mLocationManager.getLastKnownLocation(provider);	
			
			mLongitude = location.getLongitude();
			mLatitude = location.getLatitude();
			//Log.i("GPSService", "<long,lat> = <" + mLongitude + "," + mLatitude+">");
			
			if(code==GPS_SERVICE_CODE){
				Bundle bundle = data.readBundle();
				String type = bundle.getString("TYPE");
				Log.i(TAG,"TYPE="+type);
				
				if(type.equals("null")){
					Log.i("GPSService", "<long,lat> = <" + mLongitude + "," + mLatitude+">");
					//String[] coor = {nf.format(mLatitude),nf.format(mLongitude)};
					
					bundle = new Bundle();
					bundle.putString("LATITUDE", Double.toString(mLatitude));
					bundle.putString("LONGITUDE", Double.toString(mLongitude));
					//bundle.putStringArray("COOR", coor);
					bundle.putString("PROVIDER", provider);
					reply.writeBundle(bundle);
				}else if(type.equals("JSON")){
					bundle.putString("JSON_RESULT", "{\"latitude\":\""+Double.toString(mLatitude)+"\",\"longitude\":\""+Double.toString(mLongitude)+"\"}");
					reply.writeBundle(bundle);
				}
				return true;
			}else{ 
				Log.e(getClass().getSimpleName(),"Transaction code should be "+ LOCATION_SERVICE + ";"	+ " received instead " + code);
				return false;
			}
		}
	}
	
	public void debug(String msg){
		Log.d(TAG,msg);
	}
	
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return new GPSServiceBinder();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
	}
}
