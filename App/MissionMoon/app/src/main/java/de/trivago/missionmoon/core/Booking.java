package de.trivago.missionmoon.core;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by simon on 14.04.14.
 */
public class Booking extends DBEntity<Booking> implements Parcelable {

	public String hotelId;
	public String userId;
    public boolean hasVisited;
	public Date date;

	public Booking() {
	}

	public Hotel getHotel() {
		return Hotel.findById(Hotel.class, hotelId);
	}

	public void setHotel(Hotel loc) {
		if (loc != null) {
			hotelId = loc.remoteId;
		} else {
			hotelId = null;
		}
	}

	public User getUser() {
		return User.findById(User.class, userId);
	}

	public void setUser(User v) {
		if (v != null) {
			userId = v.remoteId;
		} else {
			userId = null;
		}
	}

	public static final String CREATE_TABLE = "create table Booking ("
			+ SQL_CREATE_STATEMENT
			+ "hotelId INTEGER, " + "userId INTEGER, "
			+ "hasVisited INTEGER,"
			+ "date INTEGER);";

	@Override
	protected void inflate(ContentValues vals, boolean remoteIds) {

		if (remoteIds) {
            hotelId = vals.getAsString("hotelId");
			userId = vals.getAsString("userId");
		} else {
            hotelId = remoteIdByLocalId(Booking.class, vals.getAsLong("hotelId"));
            userId = remoteIdByLocalId(Booking.class, vals.getAsLong("userId"));
		}

		hasVisited = vals.getAsInteger("hasVisited") > 0;
        date = new Date(vals.getAsLong("date"));
	}

	@Override
	protected void deflate(ContentValues vals, boolean remoteIds) {
		if (remoteIds) {
			vals.put("hotelId", hotelId);
			vals.put("userId", userId);
		} else {
			vals.put("hotelId", localIdByRemoteId(Hotel.class, hotelId));
			vals.put("userId", localIdByRemoteId(User.class, userId));
		}

		vals.put("hasVisited", hasVisited ? 1 :0);
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
