package de.trivago.missionmoon;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Frederik Schweiger on 06.07.2014.
 */
public class HotelDetailFragment extends DialogFragment {

    public HotelDetailFragment(){
        //default constructor
    }

    public static HotelDetailFragment newInstance(){
        return new HotelDetailFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View frag = inflater.inflate(R.layout.fragment_hotel_detail, container, false);

        return frag;
    }
}
