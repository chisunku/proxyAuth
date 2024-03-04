package com.example.checking;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.checking.Model.LocationsModel;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationListView extends Fragment{
    RecyclerView courseRV;
    List<LocationsModel> dataList;

    LocationAdapter productAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_locations, parent, false);
        courseRV = view.findViewById(R.id.recycler_view);
        dataList = new ArrayList<>();
        new FetchDataAsyncTask().execute();
        FloatingActionButton addLoc = view.findViewById(R.id.addLocation);
        addLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boundary fragment = new Boundary();
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
                LocationsModel model = dataList.get(viewHolder.getAdapterPosition());
                int position = viewHolder.getAdapterPosition();
                dataList.remove(viewHolder.getAdapterPosition());
                productAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());

                //delete from firestore
//                DataController db = new DataController(getActivity().getApplicationContext());
//                db.deleteFav(deletedCourse.getBarcode());

                // below line is to display our snackbar with action.
                Snackbar.make(courseRV, model.getName(), Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // adding on click listener to our action of snack bar.
                        // below line is to add our item to array list with a position.
//                        insert to firestore
                        dataList.add(position, model);
//                        db.insertFav(fv.getBarcode(), fv.getProductName(),fv.getImageUrl());

                        // below line is to notify item is
                        // added to our adapter class.
                        productAdapter.notifyItemInserted(position);
                    }
                }).show();

            }
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                int backgroundCornerOffset = 20;

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

        return view;
    }

    public class FetchDataAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            System.out.println("in location do in bg");
            try{
                //call updateCheckIn API
                APIService apiService = RetrofitClient.getClient().create(APIService.class);
                Call<List<LocationsModel>> call = apiService.getAllLocations();
                call.enqueue(new Callback<List<LocationsModel>>() {
                    @Override
                    public void onResponse(Call<List<LocationsModel>> call, Response<List<LocationsModel>> response) {
                        System.out.println("response: "+response);
                        if (response.isSuccessful()) {
                            dataList = response.body();
                            System.out.println("location list : "+dataList);
                            FragmentManager fragmentManager = getFragmentManager();
                            productAdapter = new LocationAdapter(getContext(), dataList, fragmentManager);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);

                            // in below two lines we are setting layoutmanager and adapter to our recycler view.
                            courseRV.setLayoutManager(linearLayoutManager);
                            courseRV.setAdapter(productAdapter);
                            // Handle the list of AttendanceModel objects
                        } else {
                            System.out.println("API has no response");
                            // Handle unsuccessful response
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LocationsModel>> call, Throwable t) {
                        // Handle network errors
                        System.out.println(t.fillInStackTrace());
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }
}
