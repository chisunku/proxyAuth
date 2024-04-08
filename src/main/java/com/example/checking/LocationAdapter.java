package com.example.checking;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.checking.Model.Location;

import java.io.Serializable;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.handler> {

    private final Context context;
    public List<Location> locationModelArrayList;
    FragmentManager fragmentManager;

    public LocationAdapter(Context context){this.context = context;}

    // Constructor
    public LocationAdapter(Context context, List<Location> locationModelArrayList, FragmentManager fragmentManager) {
        this.context = context;
        this.locationModelArrayList = locationModelArrayList;
        this.fragmentManager = fragmentManager;
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
        System.out.println("Location adapter"+locationModelArrayList.size());
        Location model = locationModelArrayList.get(position);
        holder.locationAddress.setText(model.getAddress());
        holder.locationCity.setText("" + model.getName());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                callback.onItemClicked();
                EditLocationFragment yourFragment = new EditLocationFragment();
                Bundle args = new Bundle();
                args.putSerializable("loc", model);
                yourFragment.setArguments(args);
                fragmentManager.beginTransaction()
                        .replace(R.id.content, yourFragment, "your_fragment_tag")
                        .addToBackStack("location")
                        .commit();
            }
        });

        //Delete


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
