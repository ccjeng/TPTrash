package com.oddsoft.tpetrash2.realtime;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.oddsoft.tpetrash2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andycheng on 2015/8/13.
 */
public class RealtimeListAdapter extends ArrayAdapter<RealtimeItem> {

    private final Activity context;
    private final List<RealtimeItem> items;

    public RealtimeListAdapter(Activity context, ArrayList<RealtimeItem> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
    }

    static class ViewHolder {
        public TextView timeView;
        public TextView locationrView;
        public TextView carnoView;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.listitem_realtime, null, true);
            holder = new ViewHolder();
            //holder.imageView = (ImageView) rowView.findViewById(R.id.icon);
            holder.timeView = (TextView) rowView.findViewById(R.id.tvCarTime);
            holder.locationrView = (TextView) rowView.findViewById(R.id.tvLocation);
            holder.carnoView = (TextView) rowView.findViewById(R.id.tvCarNO);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        holder.timeView.setText(items.get(position).getCarTime());
        holder.locationrView.setText(items.get(position).getCarLocation());

        //holder.carnoView.setText(items.get(position).getCarNO());
        holder.carnoView.setText(items.get(position).getDistanceText()
        );


        return rowView;
    }


}