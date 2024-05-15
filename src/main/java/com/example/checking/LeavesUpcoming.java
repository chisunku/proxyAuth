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

public class LeavesUpcoming extends Fragment {

    APIService apiService;
    LeavesAdapter adapter;

    public List<Leaves> leavesList;

    public static LeavesUpcoming newInstance(Employee employee) {
        LeavesUpcoming fragment = new LeavesUpcoming();
        Bundle args = new Bundle();
        args.putSerializable("employee", employee);
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_leaves_type, container, false);

        TextView heading = view.findViewById(R.id.heading);
        heading.setVisibility(View.GONE);

        Log.d("TAG", "onCreateView: Inside upcoming leaves fragment");
        Bundle bundle = getArguments();
        Employee employee = (Employee) bundle.getSerializable("employee");
        TextView noData = view.findViewById(R.id.NoData);

        ProgressBar progressBar = view.findViewById(R.id.loadingLayoutLeaves);

        apiService = RetrofitClient.getClient().create(APIService.class);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        Call<List<Leaves>> call = apiService.getUpcomingLeave(employee.getEmail(), new Date());
        call.enqueue(new Callback<List<Leaves>>() {
            @Override
            public void onResponse(Call<List<Leaves>> call, Response<List<Leaves>> response) {
                progressBar.setVisibility(View.GONE);
                leavesList = response.body();
                Log.d("TAG", "onResponse: called upcomming!! "+leavesList);
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
            public void onFailure(Call<List<Leaves>> call, Throwable t) {
                Log.d("TAG", "onFailure: called!!");
            }
        });

//        TextView tv = view.findViewById(R.id.text);
//        tv.setText("Upcoming Leaves");
        Log.d("TAG", "onCreateView: Inside upcoming leaves fragment");
        return view;
    }
}
