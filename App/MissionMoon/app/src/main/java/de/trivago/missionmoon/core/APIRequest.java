package de.trivago.missionmoon.core;

import android.content.ContentValues;
import android.util.JsonReader;
import android.util.Log;

import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.trivago.missionmoon.core.DBEntity;

/**
 * Created by simon on 05.07.14.
 */
abstract class APIRequest<T extends DBEntity> extends Request<List<T>> {
    private static final String TAG = APIClient.class.getSimpleName();
    protected static final String ENDPOINT_URL = "http://worlddraws.com/";

    private final Class<T> clazz;

    protected APIRequest(Class<T> clazz, String query,
                         Response.ErrorListener errorListener) {
        super(Method.GET, ENDPOINT_URL + query, errorListener);

        this.clazz = clazz;
    }

    @Override
    protected Response<List<T>> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            return Response.success(parseEntities(json),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            Log.e(TAG, "JSON error", e);
            return Response.error(new ParseError(e));
        }
    }

    protected List<T> parseEntities(String json) throws JSONException, IllegalAccessException, InstantiationException {
        JSONArray array = (JSONArray) new JSONTokener(json).nextValue();
        ArrayList<T> results = new ArrayList<T>(array.length());

        for (int i = 0; i < array.length(); i++) {
            JSONObject jObj = array.getJSONObject(i);
            ContentValues values = new ContentValues(jObj.length());

            String remoteId = jObj.getString("_id");
            //long remoteRevision = jObj.getLong(DBEntity.REVISION_FIELD);
            //boolean remoteDeleted = jObj.getBoolean("deleted");

            T object = DBEntity.findById(clazz, remoteId);
            if (object == null) {
                //if (remoteDeleted) {
                //    continue;// Ignore this object
                //}

                object = clazz.newInstance();
                object.remoteId = remoteId;
                Log.i(TAG, "Creating new local object " + remoteId);
            } //else if (remoteRevision < object.revision) {
              //  Log.i(TAG, "Skipping outdated remote object " + remoteId);
              //  continue;
            //}
            //if (remoteDeleted) {
            //    object.delete();
            //}

            //Log.i(TAG, "Updating object to rev " + remoteRevision);
            //object.revision = remoteRevision;

            @SuppressWarnings("unchecked")
            Iterator<String> iter = jObj.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                String value = jObj.getString(key);
                values.put(key, value);
            }
            object.inflate(values, true);
            //object.save();

            results.add(object);
        }

        return results;
    }

}
