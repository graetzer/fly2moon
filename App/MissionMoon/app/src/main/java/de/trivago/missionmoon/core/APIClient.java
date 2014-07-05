package de.trivago.missionmoon.core;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentValues;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.text.TextUtils;
import android.util.Log;

import de.trivago.missionmoon.core.Booking;

public class APIClient {
	private static final String TAG = APIClient.class.getSimpleName();
	private static final String ENDPOINT_URL = "http://lit-falls-4147.herokuapp.com/";

	public static final HttpContext LOCALCONTEXT = new BasicHttpContext();
	private final AndroidHttpClient mClient;
	private String mSessionId;

	private static APIClient instance;

	public static APIClient get(Context c) {
		if (instance == null) {
			instance = new APIClient(c);
		}
		return instance;
	}

	private APIClient(Context _ctx) {
		mClient = AndroidHttpClient.newInstance("Logbook",
				_ctx.getApplicationContext());

		CookieStore cookieStore = new BasicCookieStore();
		LOCALCONTEXT.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	public String getSessionId() {
		return mSessionId;
	}

	public boolean registerUser(String username, String password) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", password));
		String urlString = ENDPOINT_URL + "users";
		String json = post(urlString, params);

		if (json != null) {
			try {
				JSONObject object = (JSONObject) new JSONTokener(json)
						.nextValue();
				String userId = object.getString("id");
				return !TextUtils.isEmpty(userId);
			} catch (JSONException e) {
				Log.e(TAG, "Json parsing exception", e);
			}
		}
		return false;
	}

	public String loginUser(String username, String password) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", password));
		String urlString = ENDPOINT_URL + "users/login";
		String json = post(urlString, params);

		if (json != null) {
			try {
				JSONObject object = (JSONObject) new JSONTokener(json)
						.nextValue();
				mSessionId = object.getString("id");
				if (mSessionId == null) {
					Log.e(TAG, json);
				}
			} catch (JSONException e) {
				Log.e(TAG, "Json parsing exception", e);
			}
		}
		return mSessionId;
	}

	public boolean updateBookings(long revision) {
		return updateEntities("bookings", Booking.class, revision);
	}

    /**
     * Probably not a good idea
     */
	public boolean updateHotels(long revision) {
		return updateEntities("hotels", Hotel.class, revision);
	}

	//public boolean updateTours(long revision) {
//		return updateEntities("tours", Tour.class, revision);
//	}

	private <T extends DBEntity<?>> boolean updateEntities(String collection,
			Class<T> clzz, long revision) {

		String urlString = ENDPOINT_URL + collection
				+ "?%7B%22revision%22:%7B%22%24gt%22:" + revision + "%7D%7D";
		Log.i(TAG, urlString);

		String json = get(urlString);
		if (json == null) {
			return true;
		}

		try {
			JSONArray array = (JSONArray) new JSONTokener(json).nextValue();
			for (int i = 0; i < array.length(); i++) {
				JSONObject jObj = array.getJSONObject(i);
				ContentValues values = new ContentValues(jObj.length());

				String remoteId = jObj.getString("id");
				long remoteRevision = jObj.getLong(DBEntity.REVISION_FIELD);
				boolean remoteDeleted = jObj.getBoolean("deleted");

				T object = DBEntity.findById(clzz, remoteId);
				if (object == null) {
					if (remoteDeleted) {
						continue;// Ignore this object
					}

					object = clzz.newInstance();
					object.remoteId = jObj.getString("id");
					Log.i(TAG, "Creating new local object " + remoteId);
				} else if (remoteRevision < object.revision) {
					Log.i(TAG, "Skipping outdated remote object " + remoteId);
					continue;
				}
				if (remoteDeleted) {
					object.delete();
				}

				Log.i(TAG, "Updating object to rev " + remoteRevision);
				object.revision = remoteRevision;

				@SuppressWarnings("unchecked")
				Iterator<String> iter = jObj.keys();
				while (iter.hasNext()) {
					String key = iter.next();
					String value = jObj.getString(key);
					values.put(key, value);
				}
				object.inflate(values, true);
				object.save();
			}

		} catch (JSONException e) {
			Log.e(TAG, "Json parsing exception", e);
		} catch (InstantiationException e) {
			Log.e(TAG, "Class need a default constructor", e);
		} catch (IllegalAccessException e) {
			Log.e(TAG, "Class needs a public constructor", e);
		}
		return true;
	}

	public boolean push(Hotel loc) {
		return pushEntity("hotels", loc);
	}

	public boolean push(Booking veh) {
		return pushEntity("bookings", veh);
	}

	public boolean push(User tour) {
		return pushEntity("users", tour);
	}

	private boolean pushEntity(String collection, DBEntity<?> entity) {
		ContentValues vals = new ContentValues();
		entity.deflate(vals, true);

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(
				vals.size());

		params.add(new BasicNameValuePair(DBEntity.REVISION_FIELD, String
				.valueOf(entity.revision)));
		params.add(new BasicNameValuePair(DBEntity.DELETED_FIELD, String
				.valueOf(entity.deleted)));

		// Add all other stuff
		Set<String> keys = vals.keySet();
		for (String key : keys) {
			String value = vals.getAsString(key);
			if (!TextUtils.isEmpty(value)) {
				params.add(new BasicNameValuePair(key, value));
			}
		}

		boolean success = true;
		// Determine if this is in fact a new object
		if (TextUtils.isEmpty(entity.remoteId)) {

			if (entity.deleted) {
				entity.purge();
			} else {
				String json = post(ENDPOINT_URL + collection, params);
				if (success = (json != null)) {
					try {
						Log.d(TAG, json);
						JSONObject object = (JSONObject) new JSONTokener(json)
								.nextValue();
						entity.remoteId = object.getString("id");
						entity.save();
					} catch (JSONException e) {
						Log.e(TAG, "Json parsing exception", e);
						success = false;
					}
				}
			}

		} else {// Upload an existing object
			params.add(new BasicNameValuePair("id", String
					.valueOf(entity.remoteId)));

			String json = put(ENDPOINT_URL + collection, params);
			if (success = (json != null) && entity.deleted) {
				entity.purge();
			}
		}

		return success;
	}

	public static long requestRevision() {
		return new Date().getTime();
	}

	public String get(String urlString) {
		HttpGet request = new HttpGet(urlString);
		try {

			HttpResponse resp = mClient.execute(request, LOCALCONTEXT);
			if (resp.getStatusLine().getStatusCode() != 200) {
				return null;
			}

			StringWriter writer = new StringWriter();
			IOUtils.copy(resp.getEntity().getContent(), writer,
					Charset.defaultCharset());
			return writer.toString();
		} catch (IOException e) {
			Log.e(TAG, "Networking error", e);
		}
		return null;
	}

	public String post(String urlString, List<NameValuePair> nameValuePairs) {
		HttpPost request = new HttpPost(urlString);

		try {
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
			HttpResponse resp = mClient.execute(request, LOCALCONTEXT);
			/*
			 * if (resp.getStatusLine().getStatusCode() != 200) { return null; }
			 */

			StringWriter writer = new StringWriter();
			IOUtils.copy(resp.getEntity().getContent(), writer,
					Charset.defaultCharset());
			return writer.toString();
		} catch (IOException e) {
			Log.e(TAG, "Networking error", e);
		}
		return null;
	}

	public String put(String urlString, List<NameValuePair> nameValuePairs) {
		HttpPut request = new HttpPut(urlString);

		try {
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

			HttpResponse resp = mClient.execute(request, LOCALCONTEXT);
			if (resp.getStatusLine().getStatusCode() != 200) {
				return null;
			}

			StringWriter writer = new StringWriter();
			IOUtils.copy(resp.getEntity().getContent(), writer,
					Charset.defaultCharset());
			return writer.toString();
		} catch (IOException e) {
			Log.e(TAG, "Networking error", e);
		}
		return null;
	}
}
