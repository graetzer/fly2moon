package de.trivago.missionmoon.core;

import com.android.volley.Response;

import java.util.List;
import java.util.Locale;

/**
 * Created by simon on 05.07.14.
 */
public class BookingRequest extends APIRequest<Booking> {
    private Response.Listener<List<Booking>> listener;

    public BookingRequest(Response.Listener<List<Booking>> listener, Response.ErrorListener errorListener) {
        super(Booking.class, "booking", errorListener);
        this.listener = listener;
    }


    @Override
    protected void deliverResponse(List<Booking> bookings) {
        if (listener != null) {
            listener.onResponse(bookings);
        }
    }
}
