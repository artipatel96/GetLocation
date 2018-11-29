package com.example.art.getlocation;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {

    ArrayList<String> locationList;
    Context context;

    public CustomAdapter(ArrayList<String> locationList, Context context) {
        this.locationList = locationList;
        this.context = context;
    }

    @Override
    public int getCount() {
         return locationList.size(); // returns the # of items
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        convertView = View.inflate(context, R.layout.text,null);
        TextView Name_view = convertView.findViewById(R.id.locationTV);
        Name_view.setText(locationList.get(position));
        return convertView;
    }
}