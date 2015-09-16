package com.rcarvalho.tabletcomparison;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class TabletComparison extends ActionBarActivity {

    @Override
    public void onBackPressed()
    //Override the back button to share intent data with the TabletSelection Fragment
    {
        TabletComparisonFragment fragment = (TabletComparisonFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment);
        fragment.closeFragment(false);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tablet_comparison);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tablet_comparison, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == android.R.id.home)
        //Override the action bar button to provide intent data to the TabletSelection Fragment
        {
            TabletComparisonFragment fragment = (TabletComparisonFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment);
            fragment.closeFragment(false);
            return true;
        }

        else
            return super.onOptionsItemSelected(item);
    }
}
