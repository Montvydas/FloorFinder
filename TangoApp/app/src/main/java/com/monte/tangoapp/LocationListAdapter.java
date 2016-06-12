package com.monte.tangoapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by monty on 12/06/2016.
 */
public class LocationListAdapter extends BaseAdapter{
    private List location;
    private List altitude;
    private List pressure;
    Context context;


    private static LayoutInflater inflater=null;

    public LocationListAdapter (MainActivity mainActivity, List location, List altitude, List pressure){
        this.location = location;
        this.altitude = altitude;
        this.pressure = pressure;
        this.context = mainActivity;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void add (String location, String pressure, String altitude){
        this.location.add(location);
        this.pressure.add(pressure);
        this.altitude.add(altitude);
    }

    public class Holder
    {
        TextView location;
        TextView pressure;
        TextView altitude;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.location_list_adapter_view, null);
        holder.location=(TextView) rowView.findViewById(R.id.locationText);
        holder.pressure=(TextView) rowView.findViewById(R.id.pressureText);
        holder.altitude=(TextView) rowView.findViewById(R.id.altitudeText);
        holder.location.setText(location.get(position).toString());
        holder.pressure.setText(pressure.get(position).toString());
        holder.altitude.setText(altitude.get(position).toString());

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(context, "You Clicked " + location.get(position), Toast.LENGTH_LONG).show();
            }
        });
        return null;
    }

    //    private List locationList
}
