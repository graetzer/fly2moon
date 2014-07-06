package de.trivago.missionmoon.compass;

import java.lang.ref.WeakReference;
import java.util.HashSet;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LocationService implements LocationListener {
	private final static String TAG = "LocationListener";
	private static LocationService instance;

	public static LocationService getInstance(Context ctx) {
		if (instance == null)
			instance = new LocationService(ctx);
		return instance;
	}

	private Context mCtx;
	private final String mLocationProvider = LocationManager.GPS_PROVIDER;
	private CompassSensor compass;
	private LocationManager locationManager;
	private Location mCurrentLocation;
	static private final int duration = 1000;

	private LocationService(Context ctx) {
		mCtx = ctx;

		locationManager = (LocationManager) mCtx
				.getSystemService(Context.LOCATION_SERVICE);

		mCurrentLocation = locationManager
				.getLastKnownLocation(mLocationProvider);
		compass = CompassSensor.getInstance(mCtx);
		
		if (mCurrentLocation == null) {
			mCurrentLocation = new Location("tmp");
			mCurrentLocation.setLatitude(51.221481);
			mCurrentLocation.setLongitude(6.789897);
		}
	}

	public Location currentLocation() {
		return mCurrentLocation;
	}

	public void onResume() {
		locationManager.requestLocationUpdates(mLocationProvider, 0, 0, this);
		compass.onResume();
	    //Restart the timer only if we have listeners
	    if(listeners.size()>0){
	        handler.sendMessageDelayed(Message.obtain(handler, 1), duration);
	    }
	}

	public void onPause() {
		locationManager.removeUpdates(this);
		compass.onPause();
		handler.removeMessages(1);
	}

	public float distanceToLocation(double latitude, double longitude) {
		if (mCurrentLocation != null) {
			float[] results = new float[3];
			Location.distanceBetween(mCurrentLocation.getLatitude(),
					mCurrentLocation.getLongitude(), latitude, longitude,
					results);

			return results[0];
		}

		return 0;
	}

	public int arrowAngleTo(double latitude, double longitude) {
		//return -compass.getLastDirection();
		if (mCurrentLocation != null) {
			float[] results = new float[3];
			Location.distanceBetween(mCurrentLocation.getLatitude(),
					mCurrentLocation.getLongitude(), latitude, longitude,
					results);
			double bearing = results[2];
			double degree = 0;

			GeomagneticField geoField = new GeomagneticField(
					Double.valueOf(mCurrentLocation.getLatitude()).floatValue(),
					Double.valueOf(mCurrentLocation.getLongitude())
							.floatValue(),
					Double.valueOf(mCurrentLocation.getAltitude()).floatValue(),
					System.currentTimeMillis());
			degree -= compass.getLastDirection();
			degree += geoField.getDeclination();
			degree += bearing;

			return normalizeDegrees(degree);
		}
		return 0;
	}

	@Override
	public void onLocationChanged(Location location) {
		// Called when a new location is found by the network location provider.
		// makeUseOfNewLocation(location);
		if (isBetterLocation(location, mCurrentLocation))
			mCurrentLocation = location;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	protected boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
	
	private int normalizeDegrees(double rads){
	    return (int)((rads+360)%360);
	}
	
	public void addListener(LocationListener listener){
	    if(listeners.size() == 0){
	        //Start the timer on first listener
	        handler.sendMessageDelayed(Message.obtain(handler, 1), duration);
	    }
	    listeners.add(listener);
	}

	public void removeListener(LocationListener listener){
	    listeners.remove(listener);
	    if(listeners.size() == 0){
	        handler.removeMessages(1);
	    }

	}

	private void callListeners(){
	    for(LocationListener listener: listeners){
	        listener.onLocationUpdate();
	    }             

	}

	//This handler is run every 1s, and updates the listeners
	//Static class because otherwise we leak, Eclipse told me so
	static class IncomingHandler extends Handler {
	    private final WeakReference<LocationService> locationService; 

	    IncomingHandler(LocationService sensor) {
	        locationService = new WeakReference<LocationService>(sensor);
	    }
	    @Override
	    public void handleMessage(Message msg)
	    {
	    	LocationService sensor = locationService.get();
	         if (sensor != null) {
	        	 try {
	              sensor.callListeners();
	        	 } catch (Exception e) {
	        		 Log.e(TAG, "Fuck them", e);
	        	 }
	         }
	        sendMessageDelayed(Message.obtain(this, 1), duration);
	    }
	}

	private HashSet<LocationListener> listeners = new HashSet<LocationListener>();
	private IncomingHandler handler = new IncomingHandler(this);

	public interface LocationListener {
	    void onLocationUpdate();
	}
	
}
