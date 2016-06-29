package com.oddsoft.tpetrash2.view.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.utils.Analytics;
import com.oddsoft.tpetrash2.view.base.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NTFixActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.webView)
    WebView webView;

    private Analytics ga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntfix);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        initActionBar();


        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        webView.loadUrl("http://crd-rubbish.epd.ntpc.gov.tw/dispPageBox/NtpcepdMB/NtpMCp.aspx?ddsPageID=MPOINT");

    }


    private void initActionBar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
