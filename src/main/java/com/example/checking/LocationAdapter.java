package com.example.checking;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.handler> {

    private final Context context;
    private final ArrayList<LocationsModel> locationModelArrayList;

    // Constructor
    public LocationAdapter(Context context, ArrayList<LocationsModel> locationModelArrayList) {
        this.context = context;
        this.locationModelArrayList = locationModelArrayList;
    }

    @NonNull
    @Override
    public LocationAdapter.handler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_location_list_card, parent, false);
        return new handler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationAdapter.handler holder, int position) {
        // to set data to textview and imageview of each card layout
        LocationsModel model = locationModelArrayList.get(position);
        holder.locationAddress.setText(model.getAddress());
        holder.locationCity.setText("" + model.getName());
    }

    @Override
    public int getItemCount() {
        // this method is used for showing number of card items in recycler view
        return locationModelArrayList.size();
    }

    // View holder class for initializing of your views such as TextView and Imageview
    public static class handler extends RecyclerView.ViewHolder {
        //        private final ImageView courseIV;
        private final TextView locationAddress;
        private final TextView locationCity;
        private CardView cardView;

        public handler(View itemView) {
            super(itemView);
            locationCity = itemView.findViewById(R.id.LocationCity);
            locationAddress = itemView.findViewById(R.id.address);
            cardView = itemView.findViewById(R.id.card);
        }
    }
}
