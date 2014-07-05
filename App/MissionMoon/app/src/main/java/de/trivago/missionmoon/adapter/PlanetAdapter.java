package de.trivago.missionmoon.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import de.trivago.missionmoon.CircleImageView;
import de.trivago.missionmoon.R;

/**
 * Created by Frederik Schweiger on 05.07.2014.
 */
public class PlanetAdapter extends ArrayAdapter{

    private LayoutInflater mLayoutInflater;

    public PlanetAdapter(Context context, int resource, ArrayList objects) {
        super(context, resource, objects);
        mLayoutInflater = LayoutInflater.from(context);
        generateObjects(objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(getItem(position) instanceof ItemStart){
            return mLayoutInflater.inflate(R.layout.item_start, parent, false);
        }

        if(getItem(position) instanceof ItemOrbit){
            return mLayoutInflater.inflate(R.layout.item_progress, parent, false);
        }

        View v = mLayoutInflater.inflate(R.layout.item_planet, parent, false);

        RelativeLayout background = (RelativeLayout) v.findViewById(R.id.relativeLayoutItem);

        if(getCount() - position > 2){
            if(position % 2 == 0){
                background.setBackgroundResource(R.drawable.bg_03);
            }else{
                background.setBackgroundResource(R.drawable.bg_04);
            }
        }
        CircleImageView image = (CircleImageView) v.findViewById(R.id.circleImageViewItem);
        TextView title = (TextView) v.findViewById(R.id.textViewItemTitle);
        TextView location = (TextView) v.findViewById(R.id.textViewItemLocation);

        return v;
    }

    private void generateObjects(ArrayList items){
        ArrayList objects = new ArrayList();

        objects.add(new ItemStart());
        objects.addAll(items);
        objects.add(2, new ItemOrbit());

        Collections.reverse(objects);

        clear();
        addAll(objects);
    }
}
