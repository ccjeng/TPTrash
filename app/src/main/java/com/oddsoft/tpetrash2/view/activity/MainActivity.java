package com.oddsoft.tpetrash2.view.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.oddsoft.tpetrash2.BuildConfig;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.utils.Utils;
import com.oddsoft.tpetrash2.view.adapter.MainAdapter;
import com.oddsoft.tpetrash2.view.base.BaseActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.functions.Consumer;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.navigation)
    NavigationView navigation;

    @BindView(R.id.drawerlayout)
    DrawerLayout drawerLayout;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private ActionBar actionbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.cvMessage)
    CardView cvMessage;
    @BindView(R.id.message)
    TextView messageView;

    private Analytics ga;

    private static final int DIALOG_WELCOME = 1;
    private static final int DIALOG_UPDATE = 2;

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

        //show remote message if it is enabled
        showMessage();

        if (Utils.isNewInstallation(this)) {
            this.showDialog(DIALOG_WELCOME);
        } else
        if (Utils.newVersionInstalled(this)) {
            this.showDialog(DIALOG_UPDATE);
        }

        getPermission();
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
        mainItems.add(getString(R.string.tpcloth));
        mainItems.add(getString(R.string.recycle));

        MainAdapter adapter = new MainAdapter(mainItems);

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
                        gotoCustomMapActivity("tpfix");
                        break;
                    case 3:
                        startActivity(new Intent(MainActivity.this, NTFixActivity.class));
                        break;
                    case 4:
                        gotoCustomMapActivity("tpfood");
                        break;
                    case 5:
                        gotoCustomMapActivity("ntrecycle");
                        break;
                    case 6:
                        gotoCustomMapActivity("tpcloth");
                        break;
                    case 7:
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

        builder.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_exclamation_sign));

                builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, null);

        //final Context context = this;

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

    private void gotoCustomMapActivity(String mapType){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, CustomMapActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("mapType", mapType);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void showMessage() {

        //get Firebase Remote Config data
        final FirebaseRemoteConfig mRemoteConfig = FirebaseRemoteConfig.getInstance();

        // cache expiration in seconds
        long cacheExpiration = 0; //3600 * 3; //3 hour

        //Settings
        if(BuildConfig.DEBUG) {
            cacheExpiration = 0;
        }

        mRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Make the values available to your app
                            mRemoteConfig.activateFetched();
                            //get value from remote config
                            String messageText = mRemoteConfig.getString("message");
                            Boolean messageEnabled = mRemoteConfig.getBoolean("message_enabled");

                            cvMessage.setVisibility(messageEnabled ? View.VISIBLE : View.GONE);

                            if (messageEnabled) {
                                messageView.setText(messageText.replace("\\n", System.getProperty("line.separator")));
                            }
                        }
                    }
                });
    }

    public void getPermission() {

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION) // ask single or multiple permission once
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            //申請的權限全部允許
                            Log.d(TAG,"PERMISSION GRANT");
                        } else {
                            //只要有一個權限被拒絕，就會執行
                            Log.d(TAG,"PERMISSION DENY");
                            Toast.makeText(MainActivity.this, "未授權權限，地圖功能不能使用", Toast.LENGTH_SHORT).show();

                        }
                    }

                });
    }
}
