package com.example.checking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class LocationListView extends AppCompatActivity {
    private FirebaseFirestore db;
    RecyclerView courseRV;
    ArrayList<LocationsModel> dataList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_locations);
        courseRV = findViewById(R.id.recycler_view);
        db = FirebaseFirestore.getInstance();
        new FetchDataAsyncTask().execute();
        FloatingActionButton addLoc = findViewById(R.id.addLocation);
        addLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LocationListView.this, Boundary.class);
                startActivity(i);
            }
        });
    }

    public class FetchDataAsyncTask extends AsyncTask<Void, Void, Void> {

        public FetchDataAsyncTask() {

        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            System.out.println("Helloo????");
            System.out.println("Size : "+dataList.size());
            LocationAdapter productAdapter = new LocationAdapter(getApplicationContext(), dataList);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplication().getApplicationContext(),
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
