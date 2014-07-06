package de.trivago.missionmoon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.location.Location;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.List;

import de.trivago.missionmoon.core.Hotel;
import de.trivago.missionmoon.core.HotelRequest;
import de.trivago.missionmoon.dummy.DummyContent;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class HotelDialogFragment extends DialogFragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_LAT = "lat";
    private static final String ARG_LNG = "lng";

    private double mLat;
    private double mLng;

    private OnFragmentInteractionListener mListener;
    private ListView mListView;
    private List<Hotel> mHotels;
    private

    public static HotelDialogFragment newInstance(double lat, double lng) {
        HotelDialogFragment fragment = new HotelDialogFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, lat);
        args.putDouble(ARG_LNG, lng);
        fragment.setArguments(args);
        return fragment;
    }
    public HotelDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mLat = getArguments().getDouble(ARG_LAT);
            mLng = getArguments().getDouble(ARG_LNG);
        }

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        HotelRequest req = new HotelRequest(mLat, mLng, new Response.Listener<List<Hotel>>() {
            @Override
            public void onResponse(List<Hotel> hotels) {
                mHotels = hotels;
                m
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity(), volleyError.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
        queue.add(req);

        // TODO: Change Adapter to display your content
        /*setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS));*/
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle("Best Rated Hotels")
                .setView(createContentView(getActivity().getLayoutInflater(), savedInstanceState))
                .setNegativeButton("Cancel", null)
                .create();
    }

    private View createContentView(LayoutInflater inflater, Bundle savedInstanceState) {
        ListView
        return null;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
    }

}
