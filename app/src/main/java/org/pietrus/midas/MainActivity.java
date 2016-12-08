package org.pietrus.midas;

import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private String[] mItems;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle drawerToggle;
    private LinearLayout mNavDrawer;
    private Fragment frMain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        final TextView toolbarSubtitle = (TextView) findViewById(R.id.toolbar_subtitle);

        mItems = getResources().getStringArray(R.array.menu);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavDrawer = (LinearLayout) findViewById(R.id.navdrawer);
        mDrawerList = (ListView) findViewById(R.id.navlist);
        frMain = new FrMain();

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_activated_1, mItems));

        //Fragment inicial
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, frMain)
                .commit();

        mDrawerList.setItemChecked(0, true);
        toolbarTitle.setText(mItems[0]);

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Fragment fragment = null;

                switch (pos) {
                    case 0:
                        fragment = new FrMain();
                        break;
                    case 1:
                        fragment = new FrBudget();
                        break;
                    case 2:
                        fragment = new FrStats();
                        break;
                    case 3:
                        fragment = new FrCat();
                        break;
                    case 4:
                        fragment = new FrSinc();
                        break;
                    case 5:
                        fragment = new FrOpt();
                        break;
                    case 6:
                        fragment = new FrHelp();
                        break;
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, fragment)
                        .commit();

                mDrawerList.setItemChecked(pos, true);

                toolbarSubtitle.setText(null);
                toolbarTitle.setText(mItems[pos]);

                mDrawerLayout.closeDrawer(mNavDrawer);
            }
        });

        mDrawerLayout.setStatusBarBackgroundColor(
                getResources().getColor(R.color.color_primary_dark));

        drawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
}

