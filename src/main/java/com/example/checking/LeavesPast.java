package com.example.checking;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.checking.Model.Employee;
import com.example.checking.Model.Leaves;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeavesPast extends Fragment {
    APIService apiService;
    LeavesAdapter adapter;

    public List<Leaves> leavesList;

    public static LeavesPast newInstance(Employee employee) {
        LeavesPast fragment = new LeavesPast();
        Bundle args = new Bundle();
        args.putSerializable("employee", employee);
        fragment.setArguments(args);
        return fragment;
    }
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_leaves_type, container, false);
        TextView heading = view.findViewById(R.id.heading);
        heading.setVisibility(View.GONE);
        Log.d("TAG", "onCreateView: Inside past leaves fragment");

        Bundle bundle = getArguments();
        Employee employee = (Employee) bundle.getSerializable("employee");
        leavesList = bundle.getParcelableArrayList("leaves");
        ProgressBar progressBar = view.findViewById(R.id.loadingLayoutLeaves);

        TextView noData = view.findViewById(R.id.NoData);

        apiService = RetrofitClient.getClient().create(APIService.class);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        Call<List<Leaves>> call = apiService.getPastLeave(employee.getEmail(), new Date());
        call.enqueue(new Callback<List<Leaves>>() {
            @Override
            public void onResponse(Call<List<Leaves>> call, Response<List<Leaves>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d("TAG", "onResponse: called!! ");
                leavesList = response.body();
                if(leavesList==null || leavesList.size()==0){
                    noData.setVisibility(View.VISIBLE);
                    noData.setText("No Past Leaves");
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
            public void onFailure(Call<List<Leaves>> call, Throwable t) {
                Log.d("TAG", "onFailure: called!!");
            }
        });
        Log.d("TAG", "onCreateView: Inside past leaves fragment");
        return view;
    }
}
