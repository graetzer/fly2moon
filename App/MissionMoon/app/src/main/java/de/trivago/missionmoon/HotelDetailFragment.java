package de.trivago.missionmoon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.util.Date;

import de.trivago.missionmoon.core.Hotel;
import de.trivago.missionmoon.core.HotelRequest;

/**
 * Created by Frederik Schweiger on 06.07.2014.
 */
public class HotelDetailFragment extends DialogFragment {

    public HotelDetailFragment(){
        //default constructor
    }

    public static HotelDetailFragment newInstance(Hotel h){

        Bundle args = new Bundle();
        args.putParcelable("hotel", h);
        HotelDetailFragment frag =  new HotelDetailFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity()).setTitle("Hotel Buchen")
                .setView(createView(getActivity().getLayoutInflater()))
                .setNegativeButton("Abbrechen", null)
                .create();
    }

    private View createView(LayoutInflater inflater) {
        View frag = inflater.inflate(R.layout.fragment_hotel_detail, null);

        Hotel hotel = getArguments().getParcelable("hotel");
        TextView tv = (TextView)frag.findViewById(R.id.title);

        tv.setText(hotel.name);

        final ImageView image = (ImageView)frag.findViewById(R.id.hotelImage);
        if (!TextUtils.isEmpty(hotel.image)) {
            RequestQueue mQueue = Volley.newRequestQueue(getActivity());
            ImageRequest req = new ImageRequest(hotel.image, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {

                    image.setImageBitmap(bitmap);
                }
            }, 0, 0, null, null);
            mQueue.add(req);
        }

        return frag;
    }
}
