package de.trivago.missionmoon.core;

import com.android.volley.Response;

import java.util.List;
import java.util.Locale;

/**
 * Created by simon on 05.07.14.
 */
public class HotelRequest extends APIRequest<Hotel> {
    private Response.Listener<List<Hotel>> listener;

    public HotelRequest(String hotelId, Response.Listener<List<Hotel>> listener, Response.ErrorListener errorListener) {
        super(Hotel.class, "hotel?hotelID=" + hotelId, errorListener);
        this.listener = listener;
    }

    public HotelRequest(double lat, double log, Response.Listener<List<Hotel>> listener, Response.ErrorListener errorListener) {
        super(Hotel.class, String.format(Locale.ENGLISH, "hotel?lat=%f&lng=%f", lat,  log), errorListener);
        this.listener = listener;
    }

    @Override
    protected void deliverResponse(List<Hotel> hotels) {
        if (listener != null) {
            listener.onResponse(hotels);
        }
    }
}
