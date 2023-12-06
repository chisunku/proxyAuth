package com.example.checking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class LocationListView extends Fragment{
    private FirebaseFirestore db;
    RecyclerView courseRV;
    ArrayList<LocationsModel> dataList;

    LocationAdapter productAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_locations, parent, false);
//        setContentView(R.layout.activity_list_locations);
        courseRV = view.findViewById(R.id.recycler_view);
        dataList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        new FetchDataAsyncTask().execute();
        FloatingActionButton addLoc = view.findViewById(R.id.addLocation);
        addLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent i = new Intent(getActivity(), Boundary.class);
//                startActivity(i);

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
                // this method is called when we swipe our item to right direction.
                // on below line we are getting the item at a particular position.
                LocationsModel model = dataList.get(viewHolder.getAdapterPosition());

                // below line is to get the position
                // of the item at that position.
                int position = viewHolder.getAdapterPosition();
                // this method is called when item is swiped.
                // below line is to remove item from our array list.
                dataList.remove(viewHolder.getAdapterPosition());

                // below line is to notify our item is removed from adapter.
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

        public FetchDataAsyncTask() {

        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            System.out.println("Helloo????");
            System.out.println("Size : "+dataList.size());
            FragmentManager fragmentManager = getFragmentManager();
            productAdapter = new LocationAdapter(getContext(), dataList, fragmentManager);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(),
                    LinearLayoutManager.VERTICAL, false);

            // in below two lines we are setting layoutmanager and adapter to our recycler view.
            courseRV.setLayoutManager(linearLayoutManager);
            courseRV.setAdapter(productAdapter);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            CollectionReference collectionRef = db.collection("cities");
            Task<QuerySnapshot> task = collectionRef.get();
            task.addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    QuerySnapshot querySnapshot = task1.getResult();
                    if (querySnapshot != null) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            LocationsModel model = document.toObject(LocationsModel.class);
                            System.out.println("data : " + document.getData());
                            System.out.println("after data: " + model.getName());
                            dataList.add(model);
                        }
                    }
                } else {
                    // Handle errors
                    Exception exception = task1.getException();
                    if (exception != null) {
                        // Handle the exception
                    }
                }
            });

            // Wait for the Firestore operation to complete
            try {
                Tasks.await(task);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
