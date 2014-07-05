package de.trivago.missionmoon;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

import de.trivago.missionmoon.adapter.PlanetAdapter;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList test = new ArrayList();
        test.add(1);
        test.add(2);
        test.add(2);
        test.add(2);

        ListView planets = (ListView) findViewById(R.id.listViewPlanets);
        planets.setAdapter(new PlanetAdapter(getApplicationContext(), 0, test));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
