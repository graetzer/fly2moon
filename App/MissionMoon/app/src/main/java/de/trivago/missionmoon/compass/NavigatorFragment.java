package de.trivago.missionmoon.compass;

import java.util.Date;
import java.util.List;

import android.R.interpolator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import de.trivago.missionmoon.core.*;
import de.trivago.missionmoon.compass.LocationService;

public class NavigatorFragment extends Fragment {

	private ImageView imageViewArrow;
	private TextView textViewPlaceName, textViewPlaceDistance;
	private Button buttonMore, buttonNavigation, buttonClosePlacesList;
	private ListView listViewPlaces;
	private LinearLayout linearLayoutPlaces;
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
		listViewPlaces = (ListView) view.findViewById(R.id.listViewPlaces);
		linearLayoutPlaces = (LinearLayout) view
				.findViewById(R.id.linearLayoutPlaces);
		buttonMore = (Button) view.findViewById(R.id.buttonShowMore);
		buttonNavigation = (Button) view.findViewById(R.id.buttonNavigation);
		buttonClosePlacesList = (Button) view
				.findViewById(R.id.buttonClosePlacesList);
		textViewPlaceName = (TextView) view
				.findViewById(R.id.textViewPlaceName);
		textViewPlaceDistance = (TextView) view
				.findViewById(R.id.textViewPlaceDistance);

		mService = LocationService.getInstance(getActivity());
		Location loc = mService.currentLocation();

		WebService.loadPlaces(loc, new WebService.PlacesHandler() {

			@Override
			public void success(List<Place> places) {
				mPlaces = places;
				if (mPlaces != null && mPlaces.size() > 0) {
					listViewPlaces.setAdapter(new PlacesListAdapter());
					mSelectedPlace = mPlaces.get(0);
				}
			}

			@Override
			public void failure() {
			}
		});
		
		listViewPlaces
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						mSelectedPlace = mPlaces.get(position);
						mAlreadySucceeded = false;
						togglePlacesList();
					}
				});

		buttonMore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				togglePlacesList();
			}
		});

		buttonNavigation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mSelectedPlace == null)
					return;

				Intent navi = new Intent(Intent.ACTION_VIEW, Uri
						.parse("google.navigation:ll="
								+ mSelectedPlace.getLatitude() + ","
								+ mSelectedPlace.getLongitude()));
				PackageManager packageManager = getActivity().getPackageManager();
				List<ResolveInfo> list = packageManager.queryIntentActivities(navi,
						PackageManager.MATCH_DEFAULT_ONLY);
				if (list.size() > 0)
					startActivity(navi);
				else
					Toast.makeText(getActivity(), "You don't have navigation Software", Toast.LENGTH_SHORT).show();
			}
		});

		buttonClosePlacesList.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				togglePlacesList();
			}
		});

		return view;
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
							SuccessDialogFragment fragment = new SuccessDialogFragment();
							fragment.place = mSelectedPlace;
							fragment.show(manager, "Dialog");
						}
					}
				});

	}

	private void togglePlacesList() {
		if (linearLayoutPlaces.getVisibility() == View.VISIBLE) {
			hidePlacesList();
		} else {
			displayPlacesList();
		}
	}

	private void displayPlacesList() {

		linearLayoutPlaces.setVisibility(View.VISIBLE);
		Animation slide_in = AnimationUtils.loadAnimation(getActivity(),
				R.anim.top_down_slide);
		linearLayoutPlaces.startAnimation(slide_in);
		buttonMore.setText(R.string.less);
	}

	private void hidePlacesList() {
		Animation slide_out = AnimationUtils.loadAnimation(getActivity(),
				R.anim.bottom_up_slide);
		slide_out.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				linearLayoutPlaces.setVisibility(View.INVISIBLE);
				buttonMore.setText(R.string.more);
			}
		});

		linearLayoutPlaces.startAnimation(slide_out);
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
				newDegrees, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setFillEnabled(true);
		animation.setFillBefore(true);
		animation.setDuration(500);
		animation.setInterpolator(getActivity().getApplicationContext(),
				interpolator.accelerate_decelerate);

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

	private class PlacesListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mPlaces.size();
		}

		@Override
		public Object getItem(int position) {
			return mPlaces.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			if (convertView == null) {
				rowView = getActivity().getLayoutInflater().inflate(
						R.layout.row_places, parent, false);
			}

			TextView nameText = (TextView) rowView
					.findViewById(R.id.textViewRowPlaceName);
			TextView distanceText = (TextView) rowView
					.findViewById(R.id.textViewRowDistance);
			TextView timeText = (TextView) rowView
					.findViewById(R.id.textViewRowOpenTime);

			Place place = mPlaces.get(position);
			
			nameText.setText(place.getName());
			float distance = mService.distanceToLocation(place.getLatitude(),
					place.getLongitude());
			distanceText.setText(String.format("%d Meter", (int) distance));
			timeText.setText(getString(R.string.open_until_row) + 
					" " + DateFormat.format("kk:mm",
					place.getOpen() * 1000));

			return rowView;
		}

	}

}
