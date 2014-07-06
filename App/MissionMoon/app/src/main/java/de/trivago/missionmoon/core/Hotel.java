package de.trivago.missionmoon.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Hold an Address. Should work like a place object in Google Places
 */
public class Hotel extends DBEntity<Hotel> implements Parcelable {

    public double lat, lng;
    public String name, address, image;
    int rating = 0;

    public Hotel() {
    }

    public static final String CREATE_TABLE = "create table Hotel ("
            + SQL_CREATE_STATEMENT + "lat REAL, "
            + "lng REAL, " + "name TEXT, " + "address TEXT, "
            + "image TEXT, " + "rating INTEGER);";

    @Override
    protected void inflate(ContentValues vals, boolean remoteIds) {
        lat = vals.getAsDouble("lat");
        lng = vals.getAsDouble("lng");
        name = vals.getAsString("name");
        address = vals.getAsString("address");
        image = vals.getAsString("image");
        if (vals.containsKey("rating")) {
            rating = vals.getAsInteger("rating");
        } else {
            rating = 4;
        }

    }

    @Override
    protected void deflate(ContentValues vals, boolean remoteIds) {
        vals.put("lat", lat);
        vals.put("lng", lng);
        vals.put("name", name);
        vals.put("address", address);
        vals.put("image", image);
        vals.put("rating", rating);
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        ContentValues vals = getValues();
        vals.writeToParcel(parcel, flags);
    }

    public static final Parcelable.Creator<Hotel> CREATOR = new Parcelable.Creator<Hotel>() {
        public Hotel createFromParcel(Parcel in) {
            ContentValues vals = ContentValues.CREATOR.createFromParcel(in);
            Hotel v = new Hotel();
            v.setValues(vals);
            return v;
        }

        public Hotel[] newArray(int size) {
            return new Hotel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Change this to LocationHelper";
    }
}
