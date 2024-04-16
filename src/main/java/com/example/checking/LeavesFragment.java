package com.example.checking;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.checking.Model.Employee;
import com.example.checking.Model.Leaves;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_leave, parent, false);

        // Restore ViewPager state if available
        if (savedInstanceState != null) {
            int viewPagerPosition = savedInstanceState.getInt("viewPagerPosition", 0);
            viewPager.setCurrentItem(viewPagerPosition);
        }

        // Restore state of each fragment in the ViewPager
        if (savedInstanceState != null) {
            ArrayList<Leaves> upcomingLeaves = savedInstanceState.getParcelableArrayList("upcomingLeaves");
            ArrayList<Leaves> pastLeaves = savedInstanceState.getParcelableArrayList("pastLeaves");
            // Update UI with restored data if needed
        }

        Bundle bundle = getArguments();
        employee = (Employee) bundle.getSerializable("Employee");
        apiService = RetrofitClient.getClient().create(APIService.class);
        Log.d("TAG", "onCreateView: layout inflated Leaves Fragment : "+employee.getEmail());
        Log.d("TAG", "onCreateView: progressbar set to visible");
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.view_pager);

        viewPagerAdapter = new ViewPagerAdapter(getParentFragmentManager());

        // add the fragments
        //add extra to the fragment
        viewPagerAdapter.add(new UpcomingLeaves(), "Upcoming", employee);
        viewPagerAdapter.add(new PastLeaves(), "Past", employee);

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
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, addLeaveFragment);
            fragmentTransaction.addToBackStack("LeaveFragment");
            fragmentTransaction.commit();
        });


        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save ViewPager state if needed
        // For example, you can save the current item position:
        if (viewPager != null && viewPager.getAdapter() != null) {
            outState.putInt("viewPagerPosition", viewPager.getCurrentItem());
        }

        // Save state of each fragment in the ViewPager
        if (viewPagerAdapter != null) {
            for (int i = 0; i < viewPagerAdapter.getCount(); i++) {
                Fragment fragment = viewPagerAdapter.getItem(i);
                if (fragment instanceof UpcomingLeaves) {
                    // Save state of the UpcomingLeaves fragment
                    UpcomingLeaves upcomingFragment = (UpcomingLeaves) fragment;
                    outState.putParcelableArrayList("upcomingLeaves", (ArrayList<? extends Parcelable>) upcomingFragment.leavesList);
                } else if (fragment instanceof PastLeaves) {
                    // Save state of the PastLeaves fragment
                    PastLeaves pastFragment = (PastLeaves) fragment;
                    outState.putParcelableArrayList("pastLeaves", (ArrayList<? extends Parcelable>) pastFragment.leavesList);
                }
            }
        }
    }
}
