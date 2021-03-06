package de.trivago.missionmoon;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.trivago.missionmoon.adapter.ItemMoon;
import de.trivago.missionmoon.adapter.ItemOrbit;
import de.trivago.missionmoon.adapter.ItemReward;
import de.trivago.missionmoon.adapter.ItemStart;
import de.trivago.missionmoon.adapter.ItemTitle;
import de.trivago.missionmoon.core.Booking;
import de.trivago.missionmoon.core.BookingRequest;
import de.trivago.missionmoon.core.Hotel;
import de.trivago.missionmoon.core.HotelRequest;

/**
 * Created by Frederik Schweiger on 05.07.2014.
 */
public class MissionFragment extends Fragment {

    private ListView mListView;
    private ArrayList<Pair> mMatches;
    private RequestQueue mQueue;
    private PlanetAdapter mAdapter;

    private static class Pair implements Comparable<Pair> {
        Booking booking;
        Hotel hotel;

        public Pair(Booking b, Hotel h) {
            booking = b;
            hotel = h;
        }

        @Override
        public int compareTo(Pair pair) {
            return booking.date.compareTo(pair.booking.date);
        }
    }

    public MissionFragment() {
        //default constructor
    }

    public static MissionFragment newInstance() {
        return new MissionFragment();
    }

    @Override
    public void onStop() {
        super.onStop();
        mQueue.stop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View frag = inflater.inflate(R.layout.fragment_mission, container, false);
        mListView = (ListView) frag.findViewById(R.id.listViewFragmentMission);
        return frag;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMatches = new ArrayList<Pair>();
        mAdapter = new PlanetAdapter();
        mListView.setAdapter(mAdapter);

        mQueue = Volley.newRequestQueue(getActivity());
        BookingRequest bReq = new BookingRequest(new Response.Listener<List<Booking>>() {
            @Override
            public void onResponse(final List<Booking> bookings) {
                mMatches = new ArrayList<Pair>(bookings.size());
                for (final Booking b : bookings) {
                    HotelRequest hReq = new HotelRequest(b.hotelID, new Response.Listener<List<Hotel>>() {
                        @Override
                        public void onResponse(List<Hotel> hotels) {
                            if (hotels.size() > 0) {
                                Hotel h = hotels.get(0);
                                mMatches.add(new Pair(b, h));

                                if (mMatches.size() == bookings.size()) {
                                    mAdapter.notifyDataSetChanged();
                                }

                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(getActivity(), volleyError.getLocalizedMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    mQueue.add(hReq);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity(), volleyError.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
        mQueue.add(bReq);
    }

    private class PlanetAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;
        private Drawable bg3;

        private ArrayList items = new ArrayList();

        private void sortItems() {

            Collections.sort(mMatches);
            items = new ArrayList();
            items.addAll(mMatches);

            Date now = new Date();
            for (int i = 0; i < items.size(); i++) {
                if (mMatches.get(i).booking.date.after(now)) {
                    items.add(i, new ItemOrbit());
                    break;
                } else if (i == items.size() -1) {
                    items.add(new ItemOrbit());
                    break;
                }
            }

            Collections.reverse(items);

            items.add(new ItemStart());
            items.add(0, new ItemMoon());
            items.add(0, new ItemTitle());
            items.add(items.size() - 2, new ItemReward());
        }

        @Override
        public void notifyDataSetChanged() {
            sortItems();
            super.notifyDataSetChanged();
        }

        public PlanetAdapter() {
            mLayoutInflater = getActivity().getLayoutInflater();
            //generateObjects(objects);
            bg3 = getResources().getDrawable(R.drawable.bg_03);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (getItem(position) instanceof ItemTitle) {
                return mLayoutInflater.inflate(R.layout.item_title, parent, false);
            }

            if (getItem(position) instanceof ItemReward) {
                return mLayoutInflater.inflate(R.layout.item_reward, parent, false);
            }

            if (getItem(position) instanceof ItemMoon) {
                return mLayoutInflater.inflate(R.layout.item_moon, parent, false);
            }

            if (getItem(position) instanceof ItemStart) {
                return mLayoutInflater.inflate(R.layout.item_start, parent, false);
            }

            if (getItem(position) instanceof ItemOrbit) {
                return mLayoutInflater.inflate(R.layout.item_progress, parent, false);
            }

            View v = mLayoutInflater.inflate(R.layout.item_planet, parent, false);

            RelativeLayout background = (RelativeLayout) v.findViewById(R.id.relativeLayoutItem);

            if (getCount() - position > 2) {
                if (position % 2 == 0) {
                    background.setBackgroundResource(R.drawable.bg_03);
                } else {
                    background.setBackgroundResource(R.drawable.bg_04);
                }
            }

            final CircleImageView image = (CircleImageView) v.findViewById(R.id.circleImageViewItem);
            TextView title = (TextView) v.findViewById(R.id.textViewItemTitle);
            TextView location = (TextView) v.findViewById(R.id.textViewItemLocation);

            Object item = getItem(position);
            if (item instanceof Pair) {
                final Pair p = (Pair) item;
                title.setText(p.hotel.name);
                location.setText(p.hotel.address);

                if (!TextUtils.isEmpty(p.hotel.image)) {

                    ImageRequest req = new ImageRequest(p.hotel.image, new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            if (p.booking.date.after(new Date())) {
                                bitmap = grayScaleImage(bitmap);
                            }
                            image.setImageBitmap(bitmap);
                        }
                    }, 0, 0, null, null);
                    mQueue.add(req);
                }
            }

            return v;
        }
    }

        public static Bitmap grayScaleImage(Bitmap src) {
            // constant factors
            final double GS_RED = 0.299;
            final double GS_GREEN = 0.587;
            final double GS_BLUE = 0.114;

            // create output bitmap
            Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
            // pixel information
            int A, R, G, B;
            int pixel;

            // get image size
            int width = src.getWidth();
            int height = src.getHeight();

            // scan through every single pixel
            for(int x = 0; x < width; ++x) {
                for(int y = 0; y < height; ++y) {
                    // get one pixel color
                    pixel = src.getPixel(x, y);
                    // retrieve color of all channels
                    A = Color.alpha(pixel);
                    R = Color.red(pixel);
                    G = Color.green(pixel);
                    B = Color.blue(pixel);
                    // take conversion up to one single value
                    R = G = B = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);
                    // set new pixel color to output bitmap
                    bmOut.setPixel(x, y, Color.argb(A, R, G, B));
                }
            }

            // return final image
            return bmOut;
        }
}
