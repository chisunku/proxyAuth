package com.example.checking;

import static android.app.ProgressDialog.show;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.checking.Model.Employee;
import com.example.checking.Model.Leaves;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeavesAdmin extends Fragment {

    APIService apiService;
    LeavesAdminAdapter adapter;
    public List<Leaves> leavesList;
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_leaves_type, parent, false);

        Bundle bundle = getArguments();
        Employee employee = (Employee) bundle.getSerializable("employee");
        TextView noData = view.findViewById(R.id.NoData);

        ProgressBar progressBar = view.findViewById(R.id.loadingLayoutLeaves);

        apiService = RetrofitClient.getClient().create(APIService.class);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

//        Call<List<Leaves>> call = apiService.getNotApprovedLeaves(new Date());
        Call<List<Leaves>> call = apiService.getAllAfterStartDate(new Date());
        call.enqueue(new Callback<List<Leaves>>() {
            @Override
            public void onResponse(Call<List<Leaves>> call, Response<List<Leaves>> response) {
                progressBar.setVisibility(View.GONE);
                leavesList = response.body();
                Log.d("TAG", "onResponse: called admin Leaves!! "+leavesList);
                if(leavesList==null || leavesList.size()==0){
                    Log.d("TAG", "onResponse: no data found upcoming leaves!!");
                    noData.setVisibility(View.VISIBLE);
                    noData.setText("No Upcoming Leaves");
                    noData.setTextColor(getResources().getColor(R.color.black));
                    recyclerView.setVisibility(View.GONE);
                }
                else {
                    adapter = new LeavesAdminAdapter(getContext(), leavesList, employee);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(),
                            LinearLayoutManager.VERTICAL, false);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Leaves>> call, Throwable t) {
                Log.d("TAG", "onFailure: called!!");
            }
        });

        return view;
    }
}
