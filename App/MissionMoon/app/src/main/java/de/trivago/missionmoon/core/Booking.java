package de.trivago.missionmoon.core;

import java.util.Date;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by simon on 14.04.14.
 */
public class Booking extends DBEntity<Booking> implements Parcelable {

	public String hotelID;
	public String userID;
    public boolean washere;
	public Date date;

	public Booking() {
	}

	public Hotel getHotel() {
		return Hotel.findById(Hotel.class, hotelID);
	}

	public void setHotel(Hotel loc) {
		if (loc != null) {
			hotelID = loc.remoteId;
		} else {
			hotelID = null;
		}
	}

	public User getUser() {
		return User.findById(User.class, userID);
	}

	public void setUser(User v) {
		if (v != null) {
			userID = v.remoteId;
		} else {
			userID = null;
		}
	}

	public static final String CREATE_TABLE = "create table Booking ("
			+ SQL_CREATE_STATEMENT
			+ "hotelID INTEGER, " + "userID INTEGER, "
			+ "washere INTEGER,"
			+ "date INTEGER);";

	@Override
	protected void inflate(ContentValues vals, boolean remoteIds) {

		if (remoteIds) {
            hotelID = vals.getAsString("hotelID");
			userID = vals.getAsString("userID");
		} else {
            hotelID = remoteIdByLocalId(Booking.class, vals.getAsLong("hotelID"));
            userID = remoteIdByLocalId(Booking.class, vals.getAsLong("userID"));
		}

        washere = vals.getAsBoolean("washere");
        date = new Date(vals.getAsLong("date"));
	}

	@Override
	protected void deflate(ContentValues vals, boolean remoteIds) {
		if (remoteIds) {
			vals.put("hotelID", hotelID);
			vals.put("userID", userID);
		} else {
			vals.put("hotelID", localIdByRemoteId(Hotel.class, hotelID));
			vals.put("userID", localIdByRemoteId(User.class, userID));
		}

		vals.put("washere", washere);
		vals.put("date", date != null ? date.getTime() : 0);
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		ContentValues vals = getValues();
		vals.writeToParcel(parcel, flags);
	}

	public static final Parcelable.Creator<Booking> CREATOR = new Parcelable.Creator<Booking>() {
		public Booking createFromParcel(Parcel in) {
			ContentValues vals = ContentValues.CREATOR.createFromParcel(in);
			Booking v = new Booking();
			v.setValues(vals);
			return v;
		}

		public Booking[] newArray(int size) {
			return new Booking[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

}
