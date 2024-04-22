package com.example.checking;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.checking.Model.Employee;
import com.example.checking.Model.Leaves;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeavesAdminAdapter extends RecyclerView.Adapter<LeavesAdminAdapter.handler>{

    private List<Leaves> leavesList;
    private Context context;
    private OnClickListener onClickListener;
    APIService apiService;
    Employee employee;

    public LeavesAdminAdapter(Context context, List<Leaves> leavesList, Employee employee) {
        System.out.println("in adapter binder LeavesAdminAdapter");
        this.context = context;
        this.leavesList = leavesList;
        apiService = RetrofitClient.getClient().create(APIService.class);
        this.employee = employee;
    }

    @NonNull
    @Override
    public LeavesAdminAdapter.handler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_leave_admin_card, parent, false);
        System.out.println("in adapter binder on create view LeavesAdapter");
        return new handler(view);
    }


    @Override
    public void onBindViewHolder(@NonNull LeavesAdminAdapter.handler holder, int position) {
        // to set data to textview and imageview of each card layout
        System.out.println("in adapter binder holder leaves adapter");
        Leaves model = leavesList.get(position);
        if(model.getApprovalStatus().equals("Approved")){
            holder.cardView.setBackgroundResource(R.drawable.border_green);
            holder.rejectLayout.setVisibility(View.GONE);
        }
        else if(model.getApprovalStatus().equals("Rejected")){
            holder.cardView.setBackgroundResource(R.drawable.border_red);
            holder.rejectLayout.setVisibility(View.VISIBLE);
            holder.rejectReason.setText(model.getRejectReason());
        }
        holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.cardBG));
        //date diff between start and end
        long diff = model.getEndDate().getTime() - model.getStartDate().getTime();
        long diffDays = diff / (24 * 60 * 60 * 1000);
        holder.applyDays.setText(diffDays + "");
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, YYYY", Locale.ENGLISH);
        Date start = model.getStartDate();
        Date end = model.getEndDate();
        String startDate = format.format(start);
        String endDate = format.format(end);
        holder.dateRange.setText(startDate + " - " + endDate);
        holder.leaveType.setText(model.getLeaveType());
        holder.empName.setText(model.getEmail());
        holder.reason.setText(model.getLeaveReason());
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Accepted : "+model.getId()+" email : "+model.getEmail(), Toast.LENGTH_SHORT).show();
                Call<String> call = apiService.approveLeave(model.getId(), employee.getName());
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        Log.d("TAG", "onResponse: accept "+response);
                        if(response.body().equals("success")){
                            Toast.makeText(context, "Leave Approved", Toast.LENGTH_SHORT).show();
                            holder.cardView.setBackgroundResource(R.drawable.border_green);
                            holder.rejectLayout.setVisibility(View.GONE);
                        }
                        else{
                            Toast.makeText(context, "Something went wrong with accept button. Try again in sometime!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        Log.d("TAG", "onFailure: accept button : "+throwable.getMessage()+" "+throwable.getStackTrace());
                    }
                });
            }
        });

        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Rejected : "+model.getId()+" email : "+model.getEmail(), Toast.LENGTH_SHORT).show();

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context,
                        R.style.MaterialAlertDialog_App);
                builder.setTitle("Reject Leave");
                final EditText input = new EditText(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                builder.setView(input);
                builder.setPositiveButton("Reject", (dialogInterface, i) -> {
                    Call<String> call = apiService.rejectLeave(model.getId(), input.getText().toString(), employee.getName());
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Log.d("TAG", "onResponse: reject "+response);
                            if(response!=null && response.body().equals("success")){
                                Toast.makeText(context, "Leave Rejected", Toast.LENGTH_SHORT).show();
                                holder.cardView.setBackgroundResource(R.drawable.border_red);
                                holder.rejectLayout.setVisibility(View.VISIBLE);
                                holder.rejectReason.setText(input.getText().toString());
                            }
                            else{
                                Toast.makeText(context, "Something went wrong. Try again in sometime!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable throwable) {
                            Log.d("TAG", "onFailure: reject button : "+throwable.getMessage()+" "+throwable.getStackTrace());
                        }
                    });
                });
                builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
                    //call the API
                });
                builder.show();
            }
        });

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (onClickListener != null) {
//                    onClickListener.onItemClick(position, model);
//                }
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return leavesList!=null?leavesList.size():0;
    }

    public static class handler extends RecyclerView.ViewHolder {
        //        private final ImageView courseIV;
        private final MaterialCardView cardView;
        private final TextView dateRange;
        private final TextView applyDays;
        private final TextView leaveType;

        private final TextView empName;
        private final TextView reason;

        private final TextView accept;
        private final TextView reject;
        private final LinearLayout rejectLayout;
        private final TextView rejectReason;

        public handler(View itemView) {
            super(itemView);
            dateRange = itemView.findViewById(R.id.timeLine);
            applyDays = itemView.findViewById(R.id.applyDays);
            leaveType = itemView.findViewById(R.id.leaveType);
            empName = itemView.findViewById(R.id.empName);
            reason = itemView.findViewById(R.id.reason);
            accept = itemView.findViewById(R.id.accept);
            reject = itemView.findViewById(R.id.reject);
            cardView = itemView.findViewById(R.id.cardView);
            rejectLayout = itemView.findViewById(R.id.rejectReasonLayout);
            rejectReason = itemView.findViewById(R.id.reasonToReject);
        }
    }

    //onClick Listener
    public interface OnClickListener {
        void onItemClick(int position, Leaves model);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

}
