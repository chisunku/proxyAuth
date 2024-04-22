package com.example.checking;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.checking.Model.Employee;
import com.example.checking.Model.Leaves;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddLeaveFragment extends Fragment {

    Leaves leave;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_leave_form, container, false);
        Bundle bundle = getArguments();
        Employee employee = (Employee) bundle.getSerializable("Employee");
        leave = new Leaves();
        leave.setEmail(employee.getEmail());
        APIService apiService = RetrofitClient.getClient().create(APIService.class);

        ArrayList<String> leaveTypes = new ArrayList<>();
        leaveTypes.add("Casual Leave");
        leaveTypes.add("Sick Leave");
        leaveTypes.add("Maternity Leave");
        leaveTypes.add("Paternity Leave");
        leaveTypes.add("PTO");
        leaveTypes.add("Unpaid Leave");

        ArrayAdapter arrayadapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, leaveTypes);
        arrayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        AutoCompleteTextView type = view.findViewById(R.id.leaveType);
        type.setAdapter(arrayadapter);
        type.setCursorVisible(false);

        type.setOnItemClickListener((parent, view1, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            leave.setLeaveType(selected);
        });

        MaterialButton dateRange = view.findViewById(R.id.dateRange);
        dateRange.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             setupRangePickerDialog();
                                         }
                                     });

        MaterialButton save = view.findViewById(R.id.save);

        EditText reason = view.findViewById(R.id.reason);
        reason.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                leave.setLeaveReason(reason.getText().toString());
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<Leaves> call = apiService.addLeave(leave);
                call.enqueue(new Callback<Leaves>() {
                    @Override
                    public void onResponse(Call<Leaves> call, Response<Leaves> response) {
                        Log.d("TAG", "onResponse: Leave added : "+response+" "+response.body());
                        LeavesFragment leavesFragment = new LeavesFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Employee", employee);
                        leavesFragment.setArguments(bundle);
                        getParentFragmentManager().popBackStack();
                    }

                    @Override
                    public void onFailure(Call<Leaves> call, Throwable t) {
                        Log.d("TAG", "onFailure: Leave not added");
                    }
                });
            }
        });


        return view;
    }

    private void setupRangePickerDialog() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builderRange = MaterialDatePicker.Builder.dateRangePicker();
        MaterialDatePicker<Pair<Long, Long>> pickerRange = builderRange.build();
        pickerRange.show(getParentFragmentManager(), pickerRange.toString());

        pickerRange.addOnPositiveButtonClickListener(selection -> {
            TextInputLayout startDateIL = getView().findViewById(R.id.dateStartLayout);
            TextInputLayout endDateIL = getView().findViewById(R.id.dateEndLayout);
            startDateIL.setVisibility(View.VISIBLE);
            endDateIL.setVisibility(View.VISIBLE);
            EditText startDate = getView().findViewById(R.id.startDate);
            EditText endDate = getView().findViewById(R.id.endDate);
            if (selection.first != null && selection.second != null) {
                // start date
                Calendar start = Calendar.getInstance();
                start.setTimeInMillis(selection.first);
                startDate.setText(start.get(Calendar.DAY_OF_MONTH) + "/" + (start.get(Calendar.MONTH) + 1) + "/" + start.get(Calendar.YEAR));
                leave.setStartDate(start.getTime());

                // end date
                Calendar end = Calendar.getInstance();
                end.setTimeInMillis(selection.second);
                endDate.setText(end.get(Calendar.DAY_OF_MONTH) + "/" + (end.get(Calendar.MONTH) + 1) + "/" + end.get(Calendar.YEAR));
                leave.setEndDate(end.getTime());
            }
        });

    }

}
