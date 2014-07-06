package de.trivago.missionmoon.core;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

/**
 * Implement a few CRUD operations, subclasses need to implement the store and
 * inflate methods. Mostly works without reflection.
 * 
 * @param <T>
 *            The Datatype to store
 */
public abstract class DBEntity<T> {
	private static final String TAG = "DBEntity";
	public static final long UNDEFINED_ID = -1;
	public static final String LOCAL_ID_FIELD = "_id";
	public static final String REMOTE_ID_FIELD = "_remoteId";
	public static final String REVISION_FIELD = "revision";
	public static final String DELETED_FIELD = "deleted";
	public static final String SQL_CREATE_STATEMENT = LOCAL_ID_FIELD
			+ " INTEGER primary key autoincrement, " + REMOTE_ID_FIELD
			+ " TEXT, " + REVISION_FIELD + " INTEGER, " + DELETED_FIELD
			+ " INTEGER, ";

	protected long localId = UNDEFINED_ID;

	public long getLocalId() {
		return localId;
	}
    public String getRemoteId() {return remoteId;}

	// Important to synchronize data
	public long revision = 0;
	protected String remoteId;
	protected boolean deleted;

	public DBEntity() {
	}

	public static <T extends DBEntity<?>> T findById(Class<T> clzz, long id) {

		// Sqlite autoincrement id's should start with 1
		if (id <= 0)
			return null;

		String table = clzz.getSimpleName();
		SQLiteDatabase db = DAO.inst.getDB();
		T obj = null;
		Cursor c = db.query(table, null, LOCAL_ID_FIELD + "=?",
				new String[] { String.valueOf(id) }, null, null, null, "1");

		try {
			if (c.moveToFirst()) {
				obj = clzz.newInstance();
				obj.setValues(c);
			}
		} catch (Exception e) {
			Log.e(TAG, "Error in findById", e);
		} finally {
			c.close();
		}
		return obj;
	}

	public static <T extends DBEntity<?>> T findById(Class<T> clzz,
			String remoteId) {
		if (!TextUtils.isEmpty(remoteId)) {
			List<T> list = find(clzz, REMOTE_ID_FIELD + "=?",
					new String[] { remoteId });
			if (list != null && list.size() > 0) {
				return list.get(0);
			}
		}
		return null;
	}

	public static <T extends DBEntity<?>> long localIdByRemoteId(Class<T> clzz,
			String remoteId) {
		if (remoteId == null)
			return UNDEFINED_ID;

		String table = clzz.getSimpleName();
		SQLiteDatabase db = DAO.inst.getDB();
		Cursor c = db.query(table, new String[] { LOCAL_ID_FIELD },
				REMOTE_ID_FIELD + "=?", new String[] { remoteId }, null, null,
				null, "1");

		long localId = UNDEFINED_ID;
		if (c.moveToFirst()) {
			localId = c.getLong(c.getColumnIndex(LOCAL_ID_FIELD));
		}
		c.close();

		return localId;
	}

	public static <T extends DBEntity<?>> String remoteIdByLocalId(
			Class<T> clzz, long localId) {
		if (localId <= 0)
			return null;

		String table = clzz.getSimpleName();
		SQLiteDatabase db = DAO.inst.getDB();
		Cursor c = db
				.query(table, new String[] { REMOTE_ID_FIELD }, LOCAL_ID_FIELD
						+ "=?", new String[] { String.valueOf(localId) }, null,
						null, null, "1");

		String remoteId = null;
		if (c.moveToFirst()) {
			remoteId = c.getString(c.getColumnIndex(REMOTE_ID_FIELD));
		}
		c.close();
		return remoteId;
	}

	public static <T extends DBEntity<?>> List<T> newerRevision(Class<T> clzz,
			long revision) {
		return find(clzz, REVISION_FIELD + " > ?",
				new String[] { String.valueOf(revision) });
	}

	/**
	 * Includes the deleted objects
	 * 
	 * @param clzz
	 * @return
	 */
	public static <T extends DBEntity<?>> List<T> allInstances(Class<T> clzz) {
		return find(clzz, null, null);
	}

	public static <T extends DBEntity<?>> List<T> all(Class<T> clzz) {
		return find(clzz, "deleted=0", null);
	}

	public static <T extends DBEntity<?>> List<T> find(Class<T> clzz,
			String where, String[] whereArgs) {

		SQLiteDatabase db = DAO.inst.getDB();
		String tableName = clzz.getSimpleName();

		Cursor c = db.query(tableName, null, where, whereArgs, null, null,
				null, null);
		ArrayList<T> list = null;
		try {
			list = new ArrayList<T>(c.getCount());
			while (c.moveToNext()) {
				T obj = clzz.newInstance();
				obj.setValues(c);
				list.add(obj);
			}
		} catch (Exception e) {
			Log.e(TAG, "Error in find", e);
		} finally {
			c.close();
		}
		return list;
	}

	public static <T extends DBEntity<?>> long count(Class<T> clzz) {
		return count(clzz, null, null);
	}

	public static <T extends DBEntity<?>> long count(Class<T> clzz,
			String where, String[] whereArgs) {
		SQLiteDatabase db = DAO.inst.getDB();
		String tableName = clzz.getSimpleName();
		return DatabaseUtils.queryNumEntries(db, tableName, where, whereArgs);
	}

	/**
	 * Test if this instance exist in the db
	 * 
	 * @return id != UNDEFINED_ID
	 */
	public boolean isPersistant() {
		return localId != UNDEFINED_ID;
	}

	/**
	 * Load the current data from the underlying db. Overwrites current data
	 */
	public void sync() {

		if (localId != UNDEFINED_ID) {

			String table = getClass().getSimpleName();
			SQLiteDatabase db = DAO.inst.getDB();
			Cursor c = db.query(table, null, LOCAL_ID_FIELD + "=?",
					new String[] { String.valueOf(localId) }, null, null, null,
					"1");
			try {
				setValues(c);
			} catch (Exception e) {
				Log.e(TAG, "Error in findById", e);
			} finally {
				c.close();
			}
		}
	}

	public void save() {
		String table = getClass().getSimpleName();
		SQLiteDatabase db = DAO.inst.getWritableDatabase();
		revision = DAO.inst.nextRevison();

		ContentValues vals = getValues();
		vals.remove(LOCAL_ID_FIELD);
		if (localId == UNDEFINED_ID) {
			localId = db.insert(table, null, vals);
		} else {
			db.update(table, vals, LOCAL_ID_FIELD + " = ?",
					new String[] { String.valueOf(localId) });
		}
	}

	/**
	 * Mark an object as deleted. The sync client should call {@link #purge()
	 * purge()} next time the db is synced
	 */
	public void delete() {
		deleted = true;
		if (localId != UNDEFINED_ID) {
			save();
		}
	}

	/**
	 * Actually delete an object
	 */
	public void purge() {
		SQLiteDatabase db = DAO.inst.getDB();

		if (localId != UNDEFINED_ID) {
			db.delete(getClass().getSimpleName(), LOCAL_ID_FIELD + "=?",
					new String[] { String.valueOf(localId) });
			localId = UNDEFINED_ID;
		}
	}

	protected ContentValues getValues() {
		ContentValues vals = new ContentValues(5);
		vals.put(LOCAL_ID_FIELD, localId);
		vals.put(REMOTE_ID_FIELD, remoteId);
		vals.put(REVISION_FIELD, revision);
		vals.put(DELETED_FIELD, deleted ? 1 : 0);
		deflate(vals, true);
		return vals;
	}

	protected void setValues(ContentValues vals) {
		localId = vals.getAsLong(LOCAL_ID_FIELD);
		remoteId = vals.getAsString(REMOTE_ID_FIELD);
		revision = vals.getAsLong(REVISION_FIELD);
		deleted = vals.getAsBoolean(DELETED_FIELD);
		inflate(vals, true);
	}

	protected void setValues(Cursor c) {
		localId = c.getLong(c.getColumnIndex(LOCAL_ID_FIELD));
		remoteId = c.getString(c.getColumnIndex(REMOTE_ID_FIELD));
		revision = c.getLong(c.getColumnIndex(REVISION_FIELD));
		deleted = c.getInt(c.getColumnIndex(DELETED_FIELD)) > 0;
		inflate(c);
	}

	/**
	 * Put values in the map
	 * 
	 * @param vals
	 */
	abstract protected void deflate(ContentValues vals, boolean remoteIds);

	/**
	 * Set the local values from the keys in the map
	 * 
	 * @param vals
	 */
	abstract protected void inflate(ContentValues vals, boolean remoteIds);

	/**
	 * Overwrite this for better performance. Default implementation copies data
	 * in map and then calls the {@link #inflate(android.content.ContentValues, boolean)}  inflate} method
	 * 
	 * @param c
	 *            The db cursor, don't call moveNext()
	 */
	protected void inflate(Cursor c) {
		ContentValues vals = new ContentValues(c.getColumnCount());
		DatabaseUtils.cursorRowToContentValues(c, vals);
		inflate(vals, false);
	}
}
