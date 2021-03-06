package de.trivago.missionmoon;

import android.R.interpolator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

import de.trivago.missionmoon.compass.LocationService;
import de.trivago.missionmoon.core.Hotel;
import de.trivago.missionmoon.core.HotelRequest;

public class CompassFragment extends Fragment {

    private ImageView imageViewArrow;
    private TextView textViewPlaceName, textViewPlaceDistance;
    private AutoCompleteTextView autoCompleteTextView;
    private Button buttonMore, buttonNavigation;
    private int lastArrowDegrees;
    private boolean isArrowTurning;
    private LocationService mService;
    private List<Hotel> mPlaces;
    private Hotel mSelectedPlace;
    private boolean mAlreadySucceeded = false;

    private ArrayList<Address> searchResults;
    private ArrayAdapterNoFilter mArrayAdapterNoFilter;


    public static CompassFragment newInstance() {
        return new CompassFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastArrowDegrees = 0;
        isArrowTurning = false;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container,
                false);

        // Views, Buttons zuweisen
        imageViewArrow = (ImageView) view.findViewById(R.id.imageViewArrow);
        buttonMore = (Button) view.findViewById(R.id.buttonShowMore);
        buttonNavigation = (Button) view.findViewById(R.id.buttonNavigation);
        textViewPlaceName = (TextView) view.findViewById(R.id.textViewPlaceName);
        textViewPlaceDistance = (TextView) view.findViewById(R.id.textViewPlaceDistance);
        autoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteSearch);

        mService = LocationService.getInstance(getActivity());

        buttonMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                HotelDetailFragment frag = HotelDetailFragment.newInstance(mSelectedPlace);
                frag.show(getFragmentManager(), "dialog");
            }
        });

        buttonNavigation.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mSelectedPlace == null)
                    return;

                Intent navi = new Intent(Intent.ACTION_VIEW, Uri
                        .parse("google.navigation:ll="
                                + mSelectedPlace.lat + ","
                                + mSelectedPlace.lng));
                PackageManager packageManager = getActivity().getPackageManager();
                List<ResolveInfo> list = packageManager.queryIntentActivities(navi,
                        PackageManager.MATCH_DEFAULT_ONLY);
                if (list.size() > 0)
                    startActivity(navi);
                else
                    Toast.makeText(getActivity(), "Sorry! Du hast kein Google Maps installiert :-(", Toast.LENGTH_SHORT).show();
            }
        });

        //Autocomplete searchbar
        mArrayAdapterNoFilter = new ArrayAdapterNoFilter(getActivity(), R.layout.auto_complete_item);
        autoCompleteTextView.setAdapter(mArrayAdapterNoFilter);
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                updateLocationList(editable.toString());
            }
        });
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getActivity(), "Hier sollte jetzt ein neues Hotel angezeigt werden.", Toast.LENGTH_SHORT).show();
                Address adr = searchResults.get(i);
                HotelRequest req = new HotelRequest(adr.getLatitude(), adr.getLongitude(), new Response.Listener<List<Hotel>>() {
                    @Override
                    public void onResponse(List<Hotel> hotels) {
                        if (hotels.size() > 0) {
                            HotelDetailFragment frag = HotelDetailFragment.newInstance(hotels.get(0));
                            frag.show(getFragmentManager(), "dialog");
                        }
                    }
                }, null);
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                queue.add(req);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mService = LocationService.getInstance(getActivity());
        mService.addListener(
                new LocationService.LocationListener() {
                    @Override
                    public void onLocationUpdate() {

                        if (!isAdded()) {
                            mService.removeListener(this);
                            return;
                        }

                        if (mSelectedPlace == null) {
                            loadPlaces();
                            return;
                        }
                        int degree = mService.arrowAngleTo(mSelectedPlace.lat, mSelectedPlace.lng);
                        setArrow(degree);

                        textViewPlaceName.setText(mSelectedPlace.name.toUpperCase());
                        float distance = mService.distanceToLocation(mSelectedPlace.lat, mSelectedPlace.lng);
                        textViewPlaceDistance.setText(LocationUtils.formatDist(distance));

                        if (!mAlreadySucceeded && distance < 100) {
                            FragmentManager manager = getFragmentManager();
                            if (manager == null) return;

                            mAlreadySucceeded = true;
							/*SuccessDialogFragment fragment = new SuccessDialogFragment();
							fragment.place = mSelectedPlace;
							fragment.show(manager, "Dialog");*/
                        }
                    }
                }
        );

    }

    private RequestQueue queue;
    private void loadPlaces() {
        if (queue != null) return;

        queue = Volley.newRequestQueue(getActivity());

        Location loc = mService.currentLocation();
        HotelRequest req = new HotelRequest(loc.getLatitude(), loc.getLongitude(), new Response.Listener<List<Hotel>>() {
            @Override
            public void onResponse(List<Hotel> hotels) {
                mPlaces = hotels;
                if (mPlaces != null && mPlaces.size() > 0) {
                    //listViewPlaces.setAdapter(new PlacesListAdapter());
                    mSelectedPlace = mPlaces.get(0);
                    queue = null;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity(), volleyError.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
        queue.add(req);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocationService service = LocationService.getInstance(getActivity());
        service.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocationService service = LocationService.getInstance(getActivity());
        service.onPause();
    }

    private void setArrow(int degrees) {

        if (getActivity() == null || isArrowTurning)
            return;

        degrees = degrees % 360;
        final int newDegrees = degrees;
        if (degrees == lastArrowDegrees)
            return;
        isArrowTurning = true;

        imageViewArrow.setRotation(0);

        // Animation erstellen
        RotateAnimation animation = new RotateAnimation(lastArrowDegrees,
                newDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillEnabled(true);
        animation.setFillBefore(true);
        animation.setDuration(500);
        animation.setInterpolator(getActivity().getApplicationContext(), interpolator.accelerate_decelerate);

        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lastArrowDegrees = newDegrees;
                imageViewArrow.setRotation(newDegrees);
                isArrowTurning = false;
            }
        });

        imageViewArrow.startAnimation(animation);
    }

    private void updateLocationList(String search){
        LocationUtils.getAddresses(getActivity(), search, new LocationUtils.LocationCallback() {

            @Override
            public void onAddressesReceived(ArrayList<Address> addresses) {
                searchResults = addresses;
                mArrayAdapterNoFilter.clear();
                for(Address tmp : addresses){
                    mArrayAdapterNoFilter.add(tmp.getAddressLine(0) + ", " + tmp.getAddressLine(1));
                }
                mArrayAdapterNoFilter.notifyDataSetChanged();
            }

            @Override
            public void onError() {

            }
        });
    }

    class ArrayAdapterNoFilter extends ArrayAdapter<String> {

        public ArrayAdapterNoFilter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        private final NoFilter NO_FILTER = new NoFilter();

        @Override
        public Filter getFilter() {
            return NO_FILTER;
        }

        private class NoFilter extends Filter {
            protected FilterResults performFiltering(CharSequence prefix) {
                return new FilterResults();
            }

            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                //Do nothing
            }
        }
    }

}
