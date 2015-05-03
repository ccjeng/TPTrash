package com.oddsoft.tpetrash2.adapter;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.oddsoft.tpetrash2.MainActivity;
import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.TrashItem;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by andycheng on 2015/5/3.
 */
public class HttpGetTask extends AsyncTask<Void, Void, ArrayList<TrashItem>> {
    private static final String TAG = "HttpGetTask";
    private ProgressDialog dialog;
    private ListActivity activity;

    // private List<Message> messages;
    public HttpGetTask(ListActivity activity) {
        this.activity = activity;
        context = activity;
        dialog = new ProgressDialog(context);
    }

    private Context context;

    // Build RESTful trash API
    //private String URL = "http://tptrash-api.herokuapp.com/" + +hour + "/"
    //        + rownum + "/" + distance + "/" + longitude + "/" + latitude;

    AndroidHttpClient mClient = AndroidHttpClient.newInstance("");

    @Override
    protected void onPreExecute() {
        this.dialog.setMessage("處理中");
        this.dialog.show();
    }


    @Override
    protected ArrayList<TrashItem> doInBackground(Void... params) {

        String URL = params[0].toString();
        Log.d(TAG, URL);
        HttpGet request = new HttpGet(URL);
        JSONResponseHandler responseHandler = new JSONResponseHandler();
        try {
            return mClient.execute(request, responseHandler);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<TrashItem> result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (null != mClient)
            mClient.close();
        /****

         setListAdapter(new ArrayAdapter<TrashItem>(
         MainActivity.this,
         R.layout.listitem, result));

         if (getListView().getAdapter().getCount() == 0) {
         Toast.makeText(MainActivity.this, R.string.data_not_found, Toast.LENGTH_LONG)
         .show();
         }**/
        //sorting();

    }
}


