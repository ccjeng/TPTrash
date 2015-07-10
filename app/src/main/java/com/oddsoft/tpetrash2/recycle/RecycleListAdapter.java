package com.oddsoft.tpetrash2.recycle;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.oddsoft.tpetrash2.R;

import java.util.List;

/**
 * Created by andycheng on 2015/7/10.
 */
public class RecycleListAdapter  extends ArrayAdapter<RecycleItem> {
        private final Activity context;
        private final List<RecycleItem> items;

        public RecycleListAdapter(Activity context, List<RecycleItem> list) {
            super(context, 0, list);
            this.context = context;
            this.items = list;
        }

        // static to save the reference to the outer class and to avoid access to
        // any members of the containing class
        static class ViewHolder {
            public TextView itemView;
            public TextView descrView;
            public TextView categoryView;
            public TextView subcateView;
            public TextView timeView;

        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // ViewHolder will buffer the assess to the individual fields of the row
            // layout

            ViewHolder holder;
            // Recycle existing view if passed as parameter
            // This will save memory and time on Android
            // This only works if the base layout for all classes are the same
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.recycle_listitem, null, true);
                holder = new ViewHolder();
                //holder.imageView = (ImageView) rowView.findViewById(R.id.icon);
                holder.itemView = (TextView) rowView.findViewById(R.id.item_row);
                holder.descrView = (TextView) rowView.findViewById(R.id.description_row);
                holder.categoryView = (TextView) rowView.findViewById(R.id.category_row);
                holder.subcateView = (TextView) rowView.findViewById(R.id.subcategory_row);
                holder.timeView = (TextView) rowView.findViewById(R.id.time_row);
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }
            holder.itemView.setText(items.get(position).getItem());
            holder.descrView.setText(items.get(position).getDescription());
            holder.categoryView.setText(items.get(position).getCategory());
            holder.subcateView.setText(items.get(position).getSubcategory());
            holder.timeView.setText(items.get(position).getTime());

            return rowView;
        }

    }