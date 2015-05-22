package com.oddsoft.tpetrash2;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class InfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView timeView = (TextView) findViewById(R.id.time);
        TextView addressView = (TextView) findViewById(R.id.address);
        TextView carNoView = (TextView) findViewById(R.id.carno);
        TextView carNumberView = (TextView) findViewById(R.id.carnumber);
        TextView memoView = (TextView) findViewById(R.id.memo);
        TextView garbageView = (TextView) findViewById(R.id.garbage);
        TextView foodView = (TextView) findViewById(R.id.food);
        TextView recyclingView = (TextView) findViewById(R.id.recycling);

        Bundle bunde = this.getIntent().getExtras();
        final String from = bunde.getString("from");
        final String to = bunde.getString("to");
        final String address = bunde.getString("address");
        final String carno = bunde.getString("carno");
        final String carnumber = bunde.getString("carnumber");
        final String time = bunde.getString("time");
        final String memo = bunde.getString("memo");
        final Boolean garbage = bunde.getBoolean("garbage");
        final Boolean food = bunde.getBoolean("food");
        final Boolean recycling = bunde.getBoolean("recycling");

        timeView.setText("時間：" + time);
        addressView.setText("地址：" + address);
        carNoView.setText("車號：" + carno);
        carNumberView.setText("車次：" + carnumber);
        memoView.setText("備註："+ memo);

        String garbageText;
        String foodText;
        String recyclingText;

        if (garbage) {
            garbageText = "今天有收一般垃圾";
        } else {
            garbageText = "今天沒收一般垃圾";
        }

        if (food) {
            foodText = "今天有收廚餘";
        } else {
            foodText = "今天沒收廚餘";
        }

        if (recycling) {
            recyclingText = "今天有收資源回收";
        } else {
            recyclingText = "今天沒收資源回收";
        }

        garbageView.setText(garbageText);
        foodView.setText(foodText);
        recyclingView.setText(recyclingText);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_info, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
