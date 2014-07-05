package de.trivago.missionmoon.core;

import java.util.Date;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Class to manage the sqlite instance. Should handle upgrades of the db-scheme.
 */
public class DAO extends SQLiteOpenHelper {
	private static final String TAG = "DAO";
	protected static DAO inst = null;

	private static final String DATABASE_NAME = "moon.db";
	private static final int DATABASE_VERSION = 1;

	public static DAO initInstance(Context ctx) {
		if (inst == null) {
			assert ctx != null;
			inst = new DAO(ctx.getApplicationContext());
		}
		return inst;
	}

	private SQLiteDatabase mDatabase;

	private DAO(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public SQLiteDatabase getDB() {
		if (mDatabase == null) {
			mDatabase = getWritableDatabase();
		}
		return mDatabase;
	}

	public long nextRevison() {
		return System.currentTimeMillis();
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(Hotel.CREATE_TABLE);
		database.execSQL(User.CREATE_TABLE);
		database.execSQL(Booking.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + Booking.class.getSimpleName());
		db.execSQL("DROP TABLE IF EXISTS " + Hotel.class.getSimpleName());
		db.execSQL("DROP TABLE IF EXISTS " + User.class.getSimpleName());
		onCreate(db);
	}

	public void createDemoData() {
		/*Location ad1 = new Location(), ad2 = new Location();

		ad1.latitude = 50.831119;
		ad1.longitude = 6.082579;
		ad1.name = "ADIS Technology";
		ad1.street = "Kaiserstraße 100";
		ad1.zipcode = "52134";
		ad1.city = "Herzogenrath";
		ad1.country = "Deutschland";
		ad1.save();

		ad2.latitude = 50.713213;
		ad2.longitude = 6.144916;
		ad2.name = "DSA Daten- und Systemtechnik GmbH‎";
		ad2.street = "Pascalstraße 28";
		ad2.zipcode = "52076";
		ad2.city = "Aachen";
		ad2.country = "Deutschland";
		ad2.save();

		long x = 2000;
		for (int i = 0; i < 10; i++) {
			Tour t = new Tour();

			int rand = ((int) (Math.random() * 10.0)) % 3;
			if (rand == 0)
				t.tourType = Tour.PRIVATE_TOUR;
			if (rand == 1)
				t.tourType = Tour.HOME_TOUR;
			if (rand == 2)
				t.tourType = Tour.BUSINESS_TOUR;

			t.startMileage = x;
			x += (int) (Math.random() * 1000);
			t.endMileage = x;
			t.setStartLocation(ad1);
			t.setEndLocation(ad2);
			t.visited = "ABCD";
			t.reason = "Projekt A, Besprechung " + (i + 1);
			t.startDate = new Date(i * 24 * 60 * 60 * 1000L + 1201020212023L);
			t.startDate = new Date(i * 24 * 60 * 60 * 1000L + 1201021720023L);
			t.save();
		}*/
	}
}
