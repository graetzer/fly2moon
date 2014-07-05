package de.trivago.missionmoon.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Hold an Address. Should work like a place object in Google Places
 */
public class Hotel extends DBEntity<Hotel> implements Parcelable {

	public double latitude, longitude;
	public String name, city, zipcode, street, country = "Germany";

	public Hotel() {
	}

	public static final String CREATE_TABLE = "create table Hotel ("
			+ SQL_CREATE_STATEMENT + "latitude REAL, "
			+ "longitude REAL, " + "name TEXT, " + "city TEXT, "
			+ "zipcode TEXT, " + "street TEXT, " + "country TEXT);";

	@Override
	protected void inflate(Cursor c) {
		latitude = c.getDouble(c.getColumnIndex("latitude"));
		longitude = c.getDouble(c.getColumnIndex("longitude"));
		name = c.getString(c.getColumnIndex("name"));
		city = c.getString(c.getColumnIndex("city"));
		zipcode = c.getString(c.getColumnIndex("zipcode"));
		street = c.getString(c.getColumnIndex("street"));
		country = c.getString(c.getColumnIndex("country"));
	}

	@Override
	protected void inflate(ContentValues vals, boolean remoteIds) {
		latitude = vals.getAsDouble("latitude");
		longitude = vals.getAsDouble("longitude");
		name = vals.getAsString("name");
		city = vals.getAsString("city");
		zipcode = vals.getAsString("zipcode");
		street = vals.getAsString("street");
		country = vals.getAsString("country");
	}

	@Override
	protected void deflate(ContentValues vals, boolean remoteIds) {
		vals.put("latitude", latitude);
		vals.put("longitude", longitude);
		vals.put("name", name);
		vals.put("city", city);
		vals.put("zipcode", zipcode);
		vals.put("street", street);
		vals.put("country", country);
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
