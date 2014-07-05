package de.trivago.missionmoon.core;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class User extends DBEntity<User> implements Parcelable {
	public String email, image;
    public double miles, lat, lng;

	public User() {
	}

	public static final String CREATE_TABLE = "create table User ("
			+ SQL_CREATE_STATEMENT + "email TEXT, " + "image TEXT, miles REAL, lat REAL, lng REAL);";

	@Override
	protected void inflate(Cursor c) {
		email = c.getString(c.getColumnIndex("email"));
		image = c.getString(c.getColumnIndex("image"));
        miles = c.getDouble(c.getColumnIndex("miles"));
        lat = c.getDouble(c.getColumnIndex("lat"));
        lng = c.getDouble(c.getColumnIndex("lng"));
	}

	@Override
	protected void inflate(ContentValues vals, boolean remoteIds) {
		email = vals.getAsString("email");
		image = vals.getAsString("image");
        miles = vals.getAsDouble("miles");
        lat = vals.getAsDouble("lat");
        lng = vals.getAsDouble("lng");
	}

	@Override
	protected void deflate(ContentValues vals, boolean remoteIds) {
		vals.put("email", email);
		vals.put("image", image);
        vals.put("miles", miles);
        vals.put("lat", lat);
        vals.put("lng", lng);
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		ContentValues vals = getValues();
		vals.writeToParcel(parcel, flags);
	}

	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
		public User createFromParcel(Parcel in) {
			ContentValues vals = ContentValues.CREATOR.createFromParcel(in);
			User v = new User();
			v.setValues(vals);
			return v;
		}

		public User[] newArray(int size) {
			return new User[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
		return String.valueOf(email);
	}

}
