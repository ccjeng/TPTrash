package com.oddsoft.tpetrash2.adapter;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oddsoft.tpetrash2.R;
import com.oddsoft.tpetrash2.utils.Time;
import com.oddsoft.tpetrash2.utils.Utils;

import java.util.List;

/**
 * Created by andycheng on 2016/4/21.
 */
public class ArrayItemAdapter extends RecyclerView.Adapter<ArrayItemAdapter.TrashCarViewHolder>  {

    private static final String TAG = ArrayItemAdapter.class.getSimpleName();

    private OnItemClickListener onItemClickListener;

    private List<ArrayItem> trashcar;
    private Context context;
    private String day;
    private int currentHour;
    private Location currentLocation;

    public ArrayItemAdapter(Context context, List<ArrayItem> trashcar, String day, int currentHour, Location currentLocation) {
        this.context = context;
        this.trashcar = trashcar;
        this.day = day;
        this.currentHour = currentHour;
        this.currentLocation = currentLocation;
    }

    @Override
    public TrashCarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trash_item, parent, false);

        return new TrashCarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrashCarViewHolder holder, int position) {

        holder.timeView.setText(trashcar.get(position).getCarTime());

        if (currentLocation != null) {
            holder.distanceView.setText(trashcar.get(position).getDistance(Utils.geoPointFromLocation(currentLocation)));
        } else {
            holder.distanceView.setVisibility(View.GONE);
        }

        holder.addressView.setText(trashcar.get(position).getAddress());

        //Log.d(TAG, trash.getCarTime() + Time.getCurrentHHMM() + " # " + trash.getCarStartTime() + " # " + trash.getCarEndTime());
        if (day.equals(Time.getDayOfWeekNumber()) && Integer.valueOf(trashcar.get(position).getCarHour()).equals(currentHour)) {

            if ((trashcar.get(position).getCarStartTime() <= Time.getCurrentHHMM()) && (Time.getCurrentHHMM() <= trashcar.get(position).getCarEndTime())) {
                //within time 執行勤務中
                holder.statusView.setText("執行勤務中");
                holder.statusView.setVisibility(View.VISIBLE);
                holder.row.setBackgroundColor(context.getResources().getColor(R.color.md_red_50));

            } else if (Time.getCurrentHHMM() > trashcar.get(position).getCarEndTime()) {
                holder.statusView.setText("已結束勤務");
                holder.statusView.setVisibility(View.VISIBLE);
                holder.row.setBackgroundColor(context.getResources().getColor(R.color.lightyellow));
            } else {
                holder.statusView.setVisibility(View.GONE);
                holder.row.setBackgroundColor(context.getResources().getColor(R.color.write));
            }
        }
        else {
            holder.statusView.setVisibility(View.GONE);
            holder.row.setBackgroundColor(context.getResources().getColor(R.color.write));
        }

        if (trashcar.get(position).checkTodayAvailableGarbage(day)) {
            holder.garbageView.setText("[收一般垃圾]");
            holder.garbageView.setTextColor(context.getResources().getColor(R.color.green));

        } else {
            holder.garbageView.setText("[不收一般垃圾]");
            holder.garbageView.setTextColor(context.getResources().getColor(R.color.red));
        }

        if (trashcar.get(position).checkTodayAvailableFood(day)) {
            holder.foodView.setText(" [收廚餘]");
            holder.foodView.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            holder.foodView.setText(" [不收廚餘]");
            holder.foodView.setTextColor(context.getResources().getColor(R.color.red));
        }

        if (trashcar.get(position).checkTodayAvailableRecycling(day)) {
            holder.recyclingView.setText(" [收資源回收]");
            holder.recyclingView.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            holder.recyclingView.setText(" [不收資源回收]");
            holder.recyclingView.setTextColor(context.getResources().getColor(R.color.red));
        }
    }

    @Override
    public int getItemCount() {
        return trashcar.size();
    }



    public class TrashCarViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        RelativeLayout row;
        TextView statusView;
        TextView timeView;
        TextView addressView;
        TextView distanceView;
        TextView garbageView;
        TextView foodView;
        TextView recyclingView;

        public TrashCarViewHolder(View view) {
            super(view);
            itemView.setOnClickListener(this);

            row = (RelativeLayout) view.findViewById(R.id.row);

            statusView = (TextView) view.findViewById(R.id.status_view);
            timeView = (TextView) view.findViewById(R.id.time_view);
            addressView = (TextView) view.findViewById(R.id.address_view);
            distanceView = (TextView) view.findViewById(R.id.distance_view);

            garbageView = (TextView) view.findViewById(R.id.garbage_view);
            foodView = (TextView) view.findViewById(R.id.food_view);
            recyclingView = (TextView) view.findViewById(R.id.recycling_view);

        }

        @Override
        public void onClick(View v) {

            ArrayItem item = trashcar.get(getAdapterPosition());

            onItemClickListener.onItemClick(item);
        }


    }

    public interface OnItemClickListener{
        void onItemClick(ArrayItem item);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
