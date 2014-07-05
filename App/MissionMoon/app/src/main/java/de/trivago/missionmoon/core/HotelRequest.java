package de.trivago.missionmoon.core;

import com.android.volley.Response;

import java.util.List;

/**
 * Created by simon on 05.07.14.
 */
public class HotelRequest extends APIRequest<Hotel> {
    private Response.Listener<List<Hotel>> listener;

    public HotelRequest(double lat, double log, Response.Listener<List<Hotel>> listener, Response.ErrorListener errorListener) {
        super(Hotel.class, String.format("hotels?lat=%f&log=%f", lat,  log), errorListener);
        this.listener = listener;
    }

    @Override
    protected void deliverResponse(List<Hotel> hotels) {
        if (listener != null) {
            listener.onResponse(hotels);
        }
    }
}
