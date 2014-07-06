package de.trivago.missionmoon;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import de.trivago.missionmoon.adapter.PlanetAdapter;

/**
 * Created by Frederik Schweiger on 05.07.2014.
 */
public class MissionFragment extends Fragment {

    private ListView mListView;

    public MissionFragment(){
        //default constructor
    }

    public static MissionFragment newInstance(){
        return new MissionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        ArrayList test = new ArrayList();
        test.add(1);
        test.add(2);
        test.add(2);
        test.add(2);

        mListView.setAdapter(new PlanetAdapter(getActivity(), 0 , test));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListView.smoothScrollToPosition(7);
            }
        });
    }
}
