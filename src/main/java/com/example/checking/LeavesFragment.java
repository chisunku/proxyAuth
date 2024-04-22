package com.example.checking;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.example.checking.Model.Employee;
import com.example.checking.Model.Leaves;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.material.tabs.TabLayout;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeavesFragment extends Fragment {
    Employee employee;
    TabLayout tabLayout;
    ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    APIService apiService;

    List<Leaves> leaves;
    Boolean admin = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_leave, parent, false);
        Bundle bundle = getArguments();
        employee = (Employee) bundle.getSerializable("Employee");
        admin = bundle.getBoolean("admin");
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        if(admin){
            LeavesAdmin leavesAdmin = new LeavesAdmin();
            Bundle bundle2 = new Bundle();
            bundle2.putSerializable("employee", employee);
            leavesAdmin.setArguments(bundle2);
            fragmentTransaction.replace(R.id.content, leavesAdmin);
            fragmentTransaction.addToBackStack("LeaveFragment");
            fragmentTransaction.commit();
        }

        apiService = RetrofitClient.getClient().create(APIService.class);
        Log.d("TAG", "onCreateView: layout inflated Leaves Fragment : "+employee.getEmail());
        Log.d("TAG", "onCreateView: progressbar set to visible");
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.view_pager);

        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.add(LeavesUpcoming.newInstance(employee), "Upcoming");
        viewPagerAdapter.add(LeavesPast.newInstance(employee), "Past");

        // add the fragments
        //add extra to the fragment
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        TextView balanceNumber = view.findViewById(R.id.balanceNumber);
        TextView approvedNumber = view.findViewById(R.id.approvedNumber);
        TextView pendingNumber = view.findViewById(R.id.pendingNumber);
        TextView cancelledNumber = view.findViewById(R.id.cancelledNumber);

        Log.d("TAG", "onCreateView: emp email : "+ employee.getEmail());

        Call<List<Leaves>> call = apiService.getLeavesStatusCount(employee.getEmail());
        call.enqueue(new Callback<List<Leaves>>() {
            @Override
            public void onResponse(Call<List<Leaves>> call, Response<List<Leaves>> response) {
                Log.d("TAG", "onResponse: status group by called resp : "+response+" "+response.body());
                leaves = response.body();
                if(leaves==null){
                    Toast.makeText(getContext(), "Something went wrong. Try again in sometime!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d("TAG", "onResponse: response size : "+response.body().size());
                    HashMap<String, Integer> map = new HashMap<>();
                    for(Leaves leave: leaves){
                        Log.d("TAG", "onResponse: "+leave.getApprovalStatus()+" "+leave.getCount());
                        map.put(leave.getApprovalStatus(), leave.getCount());
                    }
                    System.out.println("map : "+map);
                    balanceNumber.setText((30 -  (map.get("Approved") != null ? map.get("Approved") : 0)) +"");
                    approvedNumber.setText((map.get("Approved")!=null?map.get("Approved"):0)+"");
                    pendingNumber.setText((map.get("Pending")!=null?map.get("Pending"):0)+"");
                    cancelledNumber.setText((map.get("Rejected")!=null?map.get("Rejected"):0)+"");
                }
            }

            @Override
            public void onFailure(Call<List<Leaves>> call, Throwable throwable) {
                Log.d("TAG", "onFailure: status group by failed!");
            }
        });

        ImageView addLeave = view.findViewById(R.id.addLeaves);
        addLeave.setOnClickListener(v -> {
            AddLeaveFragment addLeaveFragment = new AddLeaveFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putSerializable("Employee", employee);
            addLeaveFragment.setArguments(bundle1);
            fragmentTransaction.replace(R.id.content, addLeaveFragment);
            fragmentTransaction.addToBackStack("LeaveFragment");
            fragmentTransaction.commit();
        });

        LeavesFilter leavesFilter = new LeavesFilter();
        Bundle bundle1 = new Bundle();
        bundle1.putSerializable("employee", employee);


        //leave approves
        CardView approved = view.findViewById(R.id.approvedCardView);
        approved.setOnClickListener(v -> {
            bundle1.putString("status", "Approved");
            leavesFilter.setArguments(bundle1);
            fragmentTransaction.replace(R.id.content, leavesFilter);
            fragmentTransaction.addToBackStack("LeaveFragment");
            fragmentTransaction.commit();
        });

        //leave rejected
        CardView cancelled = view.findViewById(R.id.cancelledCardView);
        cancelled.setOnClickListener(v -> {
            bundle1.putString("status", "Rejected");
            leavesFilter.setArguments(bundle1);
            fragmentTransaction.replace(R.id.content, leavesFilter);
            fragmentTransaction.addToBackStack("LeaveFragment");
            fragmentTransaction.commit();
        });

        //leave pending
        CardView pendingCardView = view.findViewById(R.id.pendingCardView);
        pendingCardView.setOnClickListener(v -> {
            bundle1.putString("status", "Pending");
            leavesFilter.setArguments(bundle1);
            fragmentTransaction.replace(R.id.content, leavesFilter);
            fragmentTransaction.addToBackStack("LeaveFragment");
            fragmentTransaction.commit();
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
