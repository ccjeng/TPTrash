package com.oddsoft.tpetrash2;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.oddsoft.tpetrash2.recycle.RecycleItem;
import com.oddsoft.tpetrash2.recycle.RecycleListAdapter;
import com.oddsoft.tpetrash2.utils.Analytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TooManyListenersException;

/**
 * Created by andycheng on 2015/7/10.
 */
public class RecycleActivity extends Activity {

    private ListView recycleView;
    private AutoCompleteTextView searchText;
    private AdView adView;
    private RecycleListAdapter adapter;

    private static final String[] KEYWORD = new String[] {
            "米","飯","麥","食物殘渣","殘渣","廚餘","食物","麵","麵條","麵包","麵粉","粉","豆","渣","豆乾"
            ,"豆腐","豆花","雞","鴨","魚","肉","內臟","肉乾","牛","豬","羊","鵝","零食","餅乾","糖果","巧克力"
            ,"罐","罐頭","奶","粉","奶粉","粉末","果","醬","果醬","乳","煉乳","調味","過期","食品","過期食品"
            ,"酸","臭","酸臭","衛生筷","免洗筷","免洗","筷","竹籤","牙籤","籤","塑膠刀叉","塑膠","刀","叉"
            ,"刀叉","牙線","牙","紙尿褲","衛生紙","衛生棉","複寫紙","蠟紙","離心紙","貼紙","貼紙底襯","轉印紙"
            ,"砂紙","塑膠光面廢紙","熱感應傳真紙","塑膠袋","袋","髒","污","髒污","舊衣","舊衣物","衣"
            ,"衣物","褲","褲子","棉被","地毯","踏墊","浴巾","毛巾","帽子","棉被","枕頭","床單","床罩","男內衣褲"
            ,"內褲","布料","碎布","鞋","襪","襪子","窗簾","桌布","圍裙","裙","泡水","髒汙","破舊","舊","破"
            ,"發臭","臭","舊衣","瓶罐","容器","鐵","鋁","玻璃","飲料","紙盒","鋁箔包","鋁箔","紙餐盒"
            ,"塑膠餐盒","玩具","布偶","絨毛","腳踏墊","泡棉","鞋","筆","打包帶","白板","旅行袋","球","花盆"
            ,"小家電","家電","電器","吹風機","檯燈","電話","傳真機","錄放影機","隨身聽","吸塵器","手提式收錄音機"
            ,"捕蚊燈","電蚊拍","電子遊樂器","塑膠外殼燈具","鐵","鋁","銅","金屬","瓦斯罐","瓦斯","罐"
            ,"殺蟲","殺蟲劑","日光燈","燈泡","燈","燈管","CD","DVD","光碟","手機","大哥大","行動電話","HID"
            ,"體溫計","水銀","電池","保麗龍","餐具","工業","緩衝材","緩衝","燕尾夾","提款機列印單","保溫瓶"
            ,"環保杯","L夾","剪刀","吸管","延長線","電線","耳機","手機充電線","座充","眼鏡盒","計算機","珍珠板"
            ,"麥克風","電腦椅","電腦桌","磁鐵","水槍"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        recycleView = (ListView) findViewById(R.id.listRecycleInfo);

        Analytics ga = new Analytics();
        if (!Application.APPDEBUG) {
            ga.trackerPage(this);
        }
        adView();

        //Autocomplete Search Text
        ArrayAdapter<String> keywordAdapter= new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,KEYWORD);
        searchText =(AutoCompleteTextView)findViewById(R.id.searchText);
        searchText.setThreshold(1);
        searchText.setAdapter(keywordAdapter);


        // Construct the data source
        ArrayList<RecycleItem> recycleItems = new ArrayList<RecycleItem>();
        // Create the adapter to convert the array to views
        adapter = new RecycleListAdapter(this, recycleItems);

        // Assign adapter to ListView
        recycleView.setAdapter(adapter);

        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                adapter.clear();

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //RecycleActivity.this.adapter.getFilter().filter(s);
                if (!s.toString().trim().equals("")) {
                    adapter.addAll(getJsonData(s));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (adapter.getCount() == 0 && !searchText.getText().toString().trim().equals(""))
                    Toast.makeText(RecycleActivity.this, R.string.search_nodata, Toast.LENGTH_SHORT).show();
            }
        });

    }


    protected String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("recycle.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private ArrayList<RecycleItem> getJsonData(CharSequence keyword) {

        ArrayList<RecycleItem> items = new ArrayList<RecycleItem>();
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            JSONArray m_jArry = obj.getJSONArray("recycle");
            RecycleItem item = new RecycleItem();
            items = item.fromJson(m_jArry, keyword);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return items;
    }

    private void adView() {
        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest;

        if (Application.APPDEBUG) {
            //Test Mode
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice(Application.ADMOB_TEST_DEVICE_ID)
                    .build();
        } else {
            adRequest = new AdRequest.Builder().build();

        }
        adView.loadAd(adRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (!Application.APPDEBUG)
            GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    /*
    * Called when the Activity is restarted, even before it becomes visible.
    */
    @Override
    public void onStart() {
        super.onStart();

        if (!Application.APPDEBUG)
            GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
}
