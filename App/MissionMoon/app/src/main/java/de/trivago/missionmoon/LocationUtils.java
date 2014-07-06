package de.trivago.missionmoon;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;

import java.util.ArrayList;

/**
 * Created by Frederik Schweiger on 06.07.2014.
 */
public class LocationUtils {

    private static Handler mHandler = new Handler();

    public interface LocationCallback{
        public void onAddressesReceived(ArrayList<Address> addresses);
        public void onError();
    }

    public interface MapCallback{
        public void onMapReceived(Bitmap map);
        public void onError();
    }

    public static void getAddresses(final Context ctx, final String search, final LocationCallback callback){
        if(!Geocoder.isPresent()){
            callback.onError();
            return;
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Geocoder g = new Geocoder(ctx);
                final ArrayList<Address> addresses = new ArrayList<Address>();

                try {
                    addresses.addAll(g.getFromLocationName(search, 4));
                } catch(Exception e){
                    //Something is wrong with the Geocoder
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError();
                        }
                    });
                    return;
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onAddressesReceived(addresses);
                    }
                });
            }
        });
        t.start();
    }

//    public static void downloadMap(double latitude, double longitude, final MapCallback callback){
//        AsyncHttpClient client = new AsyncHttpClient();
//        String[] allowedContentTypes = new String[] { "image/png", "image/jpeg" };
//        client.get("http://maps.googleapis.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=15&size=500x180&maptype=roadmap&sensor=false&scale=2", new BinaryHttpResponseHandler(allowedContentTypes) {
//            @Override
//            public void onSuccess(byte[] fileData) {
//                Bitmap result = BitmapFactory.decodeByteArray(fileData, 0, fileData.length);
//                callback.onMapReceived(result);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
//                callback.onError();
//            }
//        });
//    }

    public static String formatDist(float meters) {
        if (meters < 1000) {
            return ((int) meters) + "m";
        } else if (meters < 10000) {
            return formatDec(meters / 1000f, 1) + "km";
        } else {
            return ((int) (meters / 1000f)) + "km";
        }
    }

    private static String formatDec(float val, int dec) {
        int factor = (int) Math.pow(10, dec);

        int front = (int) (val);
        int back = (int) Math.abs(val * (factor)) % factor;

        return front + "." + back;
    }

}