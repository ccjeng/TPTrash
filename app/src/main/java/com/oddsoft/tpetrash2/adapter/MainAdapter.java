package com.oddsoft.tpetrash2.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oddsoft.tpetrash2.R;

import java.util.List;

/**
 * Created by andycheng on 2016/7/15.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private static final String TAG = MainAdapter.class.getSimpleName();

    private OnItemClickListener onItemClickListener;

    private List<String> items;

    public MainAdapter(List<String> items) {
        this.items = items;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_main, parent, false);

        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        holder.tvName.setText(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public class MainViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView tvName;

        public MainViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            tvName = (TextView) itemView.findViewById(R.id.tv_name);

        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(getAdapterPosition(), items.get(getAdapterPosition()));
        }


    }

    public interface OnItemClickListener{
        void onItemClick(int position, String name);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
