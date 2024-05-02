package com.example.checking;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.checking.Model.Location;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationListView extends Fragment{
    RecyclerView courseRV;
    List<Location> dataList;

    LocationAdapter productAdapter;
    Boolean admin;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_locations, parent, false);
        courseRV = view.findViewById(R.id.recycler_view);
        ProgressBar loadingProgressBar = view.findViewById(R.id.loadingLayout);
        loadingProgressBar.setVisibility(View.VISIBLE);

        Bundle bundle = getArguments();
        admin = bundle != null ? bundle.getBoolean("admin") : false;

        if(bundle == null)
            Log.d("TAG", "onCreateView: bundle is null");

        //if bundle item null then set to null
        dataList = bundle != null ? (List<Location>) bundle.getSerializable("dataList") : new ArrayList<>();

//        dataList = new ArrayList<>();
        APIService apiService = RetrofitClient.getClient().create(APIService.class);
        Call<List<Location>> call = apiService.getAllLocations();
        call.enqueue(new Callback<List<Location>>() {
            @Override
            public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                System.out.println("response: "+response);
                if (response.isSuccessful()) {
                    loadingProgressBar.setVisibility(View.GONE);
                    dataList = response.body();
                    System.out.println("location list : "+dataList);
                    FragmentManager fragmentManager = getFragmentManager();
                    productAdapter = new LocationAdapter(getContext(), dataList, fragmentManager, admin);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
                    courseRV.setLayoutManager(linearLayoutManager);
                    courseRV.setAdapter(productAdapter);
                    // Handle the list of AttendanceModel objects
                } else {
                    System.out.println("API has no response");
                    // Handle unsuccessful response
                }
            }

            @Override
            public void onFailure(Call<List<Location>> call, Throwable t) {
                // Handle network errors
                System.out.println(t.fillInStackTrace());
            }
        });

        FloatingActionButton addLoc = view.findViewById(R.id.addLocation);

        if(admin){
            addLoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Boundary fragment = new Boundary();
                    fragment.fetchAdapeter(productAdapter, dataList);

                    Bundle bundle = new Bundle();
                    bundle.putBoolean("admin", admin);
                    fragment.setArguments(bundle);

                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.content, fragment, "");
                    fragmentTransaction.addToBackStack("boundary");
                    fragmentTransaction.commit();
                }
            });

            ColorDrawable background = new ColorDrawable(ContextCompat.getColor(getContext(), R.color.delete));

            //Delete
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    // this method is called
                    // when the item is moved.
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    Location model = dataList.get(viewHolder.getAdapterPosition());
                    int position = viewHolder.getAdapterPosition();
                    dataList.remove(viewHolder.getAdapterPosition());
                    productAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());

                    Call<Void> call = apiService.deleteLocation(model.getId());
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            Log.d("TAG", "onResponse: deleted location " + response);
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable throwable) {
                            Log.d("TAG", "onResponse: couldn't delete location " + throwable.getStackTrace() + " " + throwable.getMessage());
                        }
                    });

                    // below line is to display our snackbar with action.
                    Snackbar.make(courseRV, model.getName(), Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // adding on click listener to our action of snack bar.
                            dataList.add(position, model);
                            Call<String> call = apiService.addLocation(model);
                            call.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    Log.d("TAG", "onResponse: added location " + response);
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable throwable) {
                                    Log.d("TAG", "onResponse: couldn't add location " + throwable.getStackTrace() + " " + throwable.getMessage());
                                }
                            });
                            productAdapter.notifyItemInserted(position);
                        }
                    }).show();

                }

                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                        int actionState, boolean isCurrentlyActive) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    View itemView = viewHolder.itemView;
                    int backgroundCornerOffset = 5;

                    if (dX > 0) { // Swiping to the right
                        background.setBounds(itemView.getLeft(), itemView.getTop(),
                                itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                                itemView.getBottom());

                    } else if (dX < 0) { // Swiping to the left
                        background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                                itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    } else { // view is unSwiped
                        background.setBounds(0, 0, 0, 0);
                    }
                    background.draw(c);
                }
                // at last we are adding this
                // to our recycler view.
            }).attachToRecyclerView(courseRV);
        }
        else{
            addLoc.setVisibility(View.GONE);
            Log.d("TAG", "onCreateView: admin is false "+admin);
        }

        return view;
    }
}
