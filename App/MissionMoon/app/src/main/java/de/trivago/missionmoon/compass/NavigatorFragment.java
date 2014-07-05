package de.trivago.missionmoon.compass;

import android.R.interpolator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.List;

import de.trivago.missionmoon.R;
import de.trivago.missionmoon.core.Hotel;
import de.trivago.missionmoon.core.HotelRequest;

public class NavigatorFragment extends Fragment {

	private ImageView imageViewArrow;
	private TextView textViewPlaceName, textViewPlaceDistance;
	private Button buttonMore, buttonNavigation;
	private int lastArrowDegrees;
	private boolean isArrowTurning;
	private LocationService mService;
	private List<Hotel> mPlaces;
	private Hotel mSelectedPlace;
	private boolean mAlreadySucceeded = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_navigation, container,
				false);

		// Views, Buttons zuweisen
		imageViewArrow = (ImageView) view.findViewById(R.id.imageViewArrow);
		buttonMore = (Button) view.findViewById(R.id.buttonShowMore);
		buttonNavigation = (Button) view.findViewById(R.id.buttonNavigation);
		textViewPlaceName = (TextView) view
				.findViewById(R.id.textViewPlaceName);
		textViewPlaceDistance = (TextView) view
				.findViewById(R.id.textViewPlaceDistance);

		mService = LocationService.getInstance(getActivity());
		Location loc = mService.currentLocation();

		buttonNavigation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mSelectedPlace == null)
					return;

				Intent navi = new Intent(Intent.ACTION_VIEW, Uri
						.parse("google.navigation:ll="
								+ mSelectedPlace.latitude + ","
								+ mSelectedPlace.longitude));
				PackageManager packageManager = getActivity().getPackageManager();
				List<ResolveInfo> list = packageManager.queryIntentActivities(navi,
						PackageManager.MATCH_DEFAULT_ONLY);
				if (list.size() > 0)
					startActivity(navi);
				else
					Toast.makeText(getActivity(), "Sorry! Du hast kein Google Maps installiert :-(", Toast.LENGTH_SHORT).show();
			}
		});

		return view;
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        HotelRequest req = new HotelRequest(0,0, new Response.Listener<List<Hotel>>() {
            @Override
            public void onResponse(List<Hotel> hotels) {
                mPlaces = hotels;
                if (mPlaces != null && mPlaces.size() > 0) {
                    //listViewPlaces.setAdapter(new PlacesListAdapter());
                    mSelectedPlace = mPlaces.get(0);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        queue.add(req);
    }

    @Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		final LocationService service = LocationService.getInstance(getActivity());
		service.addListener(
				new LocationService.LocationListener() {
					@Override
					public void onLocationUpdate() {
						if (!isAdded()) {
							service.removeListener(this);
							return;
						}
						if (mSelectedPlace == null) return;
						
						int degree = mService.arrowAngleTo(
								mSelectedPlace.latitude,
								mSelectedPlace.longitude);
						setArrow(degree);

                        /*
						String dateStr;
						Date d = new Date(mSelectedPlace.gtOpen()e*1000);
						if (d.after(new Date())) {
							dateStr = mSelectedPlace.getName()
									+ getString(R.string.open_from) + " "
									+ DateFormat.format("kk:mm",
											mSelectedPlace.getOpen() * 1000) + " "
											+ getString(R.string.clock);
						} else {
							dateStr = mSelectedPlace.getName()
									+ getString(R.string.open_until) + " "
									+ DateFormat.format("kk:mm",
											mSelectedPlace.getClosed() * 1000) + " "
											+ getString(R.string.clock);
						}*/

						textViewPlaceName.setText(mSelectedPlace.name);
						float distance = mService.distanceToLocation(
								mSelectedPlace.latitude,
								mSelectedPlace.longitude);
						textViewPlaceDistance.setText(String.format("%d m",
								(int) distance));

						if (!mAlreadySucceeded && distance < 100) {
							FragmentManager manager = getFragmentManager();
							if (manager == null) return;

							mAlreadySucceeded = true;
							/*SuccessDialogFragment fragment = new SuccessDialogFragment();
							fragment.place = mSelectedPlace;
							fragment.show(manager, "Dialog");*/
						}
					}
				});

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		init();
		super.onCreate(savedInstanceState);
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

	private void init() {
		lastArrowDegrees = 0;
		isArrowTurning = false;
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

}
