package com.oddsoft.tpetrash2.view.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.adapter.MainAdapter;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Utils;
import com.oddsoft.tpetrash2.view.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.navigation)
    NavigationView navigation;

    @Bind(R.id.drawerlayout)
    DrawerLayout drawerLayout;

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    private ActionBar actionbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private Analytics ga;

    private static final int DIALOG_WELCOME = 1;
    private static final int DIALOG_UPDATE = 2;

    private MainAdapter adapter;

    //private ArrayItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        initActionBar();
        initDrawer();
        initRecyclerView();

        if (Utils.isNewInstallation(this)) {
            this.showDialog(DIALOG_WELCOME);
        } else
        if (Utils.newVersionInstalled(this)) {
            this.showDialog(DIALOG_UPDATE);
        }


    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initDrawer() {
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Checking if the item is in checked state or not, if not make it in checked state
                if(menuItem.isChecked())
                    menuItem.setChecked(false);
                else
                    menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.navSetting:
                        startActivity(new Intent(MainActivity.this, Prefs.class));
                        break;
                    case R.id.navAbout:
                        new LibsBuilder()
                                //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                .withAboutIconShown(true)
                                .withAboutVersionShown(true)
                                .withAboutAppName(getString(R.string.app_name))
                                .withActivityTitle(getString(R.string.about))
                                .withAboutDescription(getString(R.string.license))
                                        //start the activity
                                .start(MainActivity.this);
                        break;
                    case R.id.navSuggest:
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.oddsoft.tpetrash2")));
                        break;
                    case R.id.navFacebook:
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.facebook.com/TaipeiTrash")));
                        break;
                }
                return false;
            }
        });

        //change navigation drawer item icons
        navigation.getMenu().findItem(R.id.navSetting).setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_cog)
                .color(Color.GRAY)
                .sizeDp(24));

        navigation.getMenu().findItem(R.id.navAbout).setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_info_circle)
                .color(Color.GRAY)
                .sizeDp(24));

        navigation.getMenu().findItem(R.id.navSuggest).setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_thumbs_up)
                .color(Color.GRAY)
                .sizeDp(24));

        navigation.getMenu().findItem(R.id.navFacebook).setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_facebook_official)
                .color(Color.GRAY)
                .sizeDp(24));

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar
                ,R.string.app_name, R.string.app_name){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        List<String> mainItems = new ArrayList<String>();
        mainItems.add(getString(R.string.lbs));
        mainItems.add(getString(R.string.query));
        mainItems.add(getString(R.string.tpfix));
        mainItems.add(getString(R.string.ntfix));
        mainItems.add(getString(R.string.tpfood));
        mainItems.add(getString(R.string.ntrecycle));
        mainItems.add(getString(R.string.recycle));

        adapter = new MainAdapter(mainItems);

        adapter.setOnItemClickListener(new MainAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(int position, String name) {

                switch (position) {
                    case 0:
                        startActivity(new Intent(MainActivity.this, LBSActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(MainActivity.this, QueryActivity.class));
                        break;
                    case 2:
                        gotoTaipeiFixActivity("tpfix");
                        break;
                    case 3:
                        startActivity(new Intent(MainActivity.this, NTFixActivity.class));
                        break;
                    case 4:
                        gotoTaipeiFixActivity("tpfood");
                        break;
                    case 5:
                        gotoTaipeiFixActivity("ntrecycle");
                        break;
                    case 6:
                        startActivity(new Intent(MainActivity.this, RecycleActivity.class));
                        break;
                }
            }

        });

        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    protected final Dialog onCreateDialog(final int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon(android.R.drawable.ic_dialog_info);

        builder.setIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_info_circle)
                        .color(Color.GRAY)
                        .sizeDp(24));

                builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, null);

        final Context context = this;

        switch (id) {
            case DIALOG_WELCOME:
                builder.setTitle(getResources().getString(R.string.welcome_title));
                builder.setMessage(getResources().getString(R.string.welcome_message));
                break;
            case DIALOG_UPDATE:
                builder.setTitle(getString(R.string.changelog_title));
                final String[] changes = getResources().getStringArray(R.array.updates);
                final StringBuilder buf = new StringBuilder();
                for (int i = 0; i < changes.length; i++) {
                    buf.append("\n\n");
                    buf.append(changes[i]);
                }
                builder.setMessage(buf.toString().trim());
                break;
        }
        return builder.create();
    }

    private void gotoTaipeiFixActivity(String mapType){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, TPFixActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("mapType", mapType);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
