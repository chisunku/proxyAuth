package com.example.checking;

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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeavesFilter extends Fragment {

    LeavesAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_leaves_type, parent, false);
        Bundle bundle = getArguments();
        Employee employee = (Employee) bundle.getSerializable("employee");
        String status = bundle.getString("status");
        ProgressBar progressBar = view.findViewById(R.id.loadingLayoutLeaves);
        TextView noData = view.findViewById(R.id.NoData);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        APIService apiService = RetrofitClient.getClient().create(APIService.class);
        Call<List<Leaves>> call = apiService.getLeavesByFilter(employee.getEmail(), status);
        call.enqueue(new Callback<List<Leaves>>() {
            @Override
            public void onResponse(Call<List<Leaves>> call, Response<List<Leaves>> response) {
                List<Leaves> leavesList = response.body();
                leavesList = response.body();
                progressBar.setVisibility(View.GONE);
                Log.d("TAG", "onResponse: called status!! "+leavesList+" statyus :"+status);
                if(leavesList==null || leavesList.size()==0){
                    Log.d("TAG", "onResponse: no data found upcoming leaves!!");
                    noData.setVisibility(View.VISIBLE);
                    noData.setText("No Upcoming Leaves");
                    noData.setTextColor(getResources().getColor(R.color.black));
                    recyclerView.setVisibility(View.GONE);
                }
                else {
                    adapter = new LeavesAdapter(getContext(), leavesList);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(),
                            LinearLayoutManager.VERTICAL, false);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setAdapter(adapter);
                }

            }

            @Override
            public void onFailure(Call<List<Leaves>> call, Throwable throwable) {
                progressBar.setVisibility(View.GONE);
                noData.setVisibility(View.VISIBLE);
                noData.setText("Something went wrong while fetching Leaves. Try again later");
            }
        });

        return view;
    }

}
