package com.example.checking;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.checking.Model.Employee;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmpView extends Fragment {
    APIService apiService;
    EmployeeViewAdapter adapter;
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.activity_emp_view, viewGroup, false);
        apiService = RetrofitClient.getClient().create(APIService.class);
        Call<List<Employee>> call = apiService.getAllEmployees();
        RecyclerView recyclerView = view.findViewById(R.id.emp_recycler);
        ProgressBar loadingProgressBar = view.findViewById(R.id.empLoading);
        call.enqueue(new Callback<List<Employee>>() {
            public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                List<Employee> employees = response.body();
                Log.d("TAG", "onResponse: called admin Leaves!! " + employees);
                adapter = new EmployeeViewAdapter(getContext(), employees);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(adapter);
                loadingProgressBar.setVisibility(View.GONE);
            }

            public void onFailure(Call<List<Employee>> call, Throwable t) {
                Log.d("TAG", "onFailure: called admin Leaves!! ");
            }
        });

        ImageView addEmp = view.findViewById(R.id.addEmp);
        addEmp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
//                AddEmployee addEmployee = new AddEmployee();
//                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, addEmployee).commit();
            }
        });

        return view;
    }
}
