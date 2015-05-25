package com.oddsoft.tpetrash2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class InfoActivity extends Activity {

    private String strFrom = "";
    private String strTo = "";
    private String address;
    private String carno;
    private String carnumber;
    private String time;
    private String memo;
    private Boolean garbage;
    private Boolean food;
    private Boolean recycling;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView timeView = (TextView) findViewById(R.id.time);
        TextView addressView = (TextView) findViewById(R.id.address);
        TextView carNoView = (TextView) findViewById(R.id.carno);
        TextView carNumberView = (TextView) findViewById(R.id.carnumber);
        TextView memoView = (TextView) findViewById(R.id.memo);

        ImageView garbageView = (ImageView) findViewById(R.id.garbageImageView);
        ImageView foodView = (ImageView) findViewById(R.id.foodImageView);
        ImageView recyclingView = (ImageView) findViewById(R.id.recyclingImageView);

        Button mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                goBrowser();
            }
        });

        Bundle bunde = this.getIntent().getExtras();
        strFrom = bunde.getString("from");
        strTo = bunde.getString("to");
        address = bunde.getString("address");
        carno = bunde.getString("carno");
        carnumber = bunde.getString("carnumber");
        time = bunde.getString("time");
        memo = bunde.getString("memo");
        garbage = bunde.getBoolean("garbage");
        food = bunde.getBoolean("food");
        recycling = bunde.getBoolean("recycling");

        timeView.setText("時間：" + time);
        addressView.setText("地址：" + address);
        carNoView.setText("車號：" + carno);
        carNumberView.setText("車次：" + carnumber);
        memoView.setText("備註："+ memo);

        if (garbage) {
            //今天有收一般垃圾
            garbageView.setImageResource(R.drawable.garbage_y);
        } else {
            //今天沒收一般垃圾"
            garbageView.setImageResource(R.drawable.garbage_n);
        }

        if (food) {
            //今天有收廚餘
            foodView.setImageResource(R.drawable.food_y);
        } else {
            //今天沒收廚餘
            foodView.setImageResource(R.drawable.food_n);
        }

        if (recycling) {
            //今天有收資源回收
            recyclingView.setImageResource(R.drawable.recycling_y);
        } else {
            //今天沒收資源回收
            recyclingView.setImageResource(R.drawable.recycling_n);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, Prefs.class));
                return true;
            case R.id.exit_settings:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goBrowser() {
        //Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        String from = "saddr=" + strFrom;
        String to = "daddr=" + strTo;
        String para = "&hl=zh&dirflg=w";
        String url = "http://maps.google.com.tw/maps?" + from + "&" + to + para;
        Intent ie = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(ie);

    }
}
