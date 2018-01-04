package com.example.benjaminfondell.sqlite_project;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.benjaminfondell.sqlite_project.DatabaseContract.*;
/**
 * Created by benjaminfondell on 11/4/17.
 */


public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.mLocationHolder>{

    private Cursor mCursor;
    private Context mContext;


    public LocationAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }


    @Override
    public mLocationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.location_view, parent, false);
        return new mLocationHolder(itemView);
    }


    @Override
    public void onBindViewHolder(mLocationHolder holder, int index) {

        if (!mCursor.moveToPosition(index)) return;
        String name = mCursor.getString(mCursor.getColumnIndex(DatabaseContract.LocationDBEntry.COLUMN_NAME));
        Float lat = mCursor.getFloat(mCursor.getColumnIndex(DatabaseContract.LocationDBEntry.COLUMN_LATITUDE));
        Float lon = mCursor.getFloat(mCursor.getColumnIndex(DatabaseContract.LocationDBEntry.COLUMN_LONGITUDE));
        long id = mCursor.getLong(mCursor.getColumnIndex(DatabaseContract.LocationDBEntry._ID));

        holder.nameView.setText(name);
        holder.latitudeView.setText(String.format("%.2f", lat));
        holder.longitudeView.setText(String.format("%.2f", lon));
        holder.itemView.setTag(id);
    }



    public class mLocationHolder extends RecyclerView.ViewHolder {

        TextView nameView;
        TextView latitudeView;
        TextView longitudeView;

        public mLocationHolder(View itemView){
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.nameTextView);
            latitudeView  = (TextView) itemView.findViewById(R.id.latTextView);
            longitudeView = (TextView) itemView.findViewById(R.id.lonTextView);
        }
    }

    public void IncrementCursor(Cursor newCursor) {

        if (mCursor != null) mCursor.close();
        mCursor = newCursor;
        if (newCursor != null) this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount(){return mCursor.getCount();}

}
