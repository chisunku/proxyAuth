package com.example.checking;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.checking.Model.Attendance;
import com.example.checking.Model.Leaves;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LeavesAdapter extends RecyclerView.Adapter<LeavesAdapter.handler>{

    private List<Leaves> leavesList;
    private Context context;

    public LeavesAdapter(Context context, List<Leaves> leavesList) {
        System.out.println("in adapter binder LeavesAdapter");
        this.context = context;
        this.leavesList = leavesList;
    }

    @NonNull
    @Override
    public LeavesAdapter.handler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_leave_card, parent, false);
        System.out.println("in adapter binder on create view LeavesAdapter");
        return new handler(view);
    }


    @Override
    public void onBindViewHolder(@NonNull LeavesAdapter.handler holder, int position) {
// to set data to textview and imageview of each card layout
        System.out.println("in adapter binder holder leaves adapter");
        Leaves model = leavesList.get(position);
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
        holder.approvedBy.setText(model.getApprovalManager());
        holder.status.setText(model.getApprovalStatus());
        if (model.getApprovalStatus().equals("Pending")) {
            holder.status.setTextColor(context.getResources().getColor(R.color.pendingText));
            holder.status.setBackgroundResource(R.drawable.button_color_yellow);
            holder.approvedBy.setText("-");
        }
        else if (model.getApprovalStatus().equals("Rejected")) {
            holder.status.setTextColor(context.getResources().getColor(R.color.rejectedText));
            holder.status.setBackgroundResource(R.drawable.button_color_red);
            holder.rejectReasonLayout.setVisibility(View.VISIBLE);
            holder.rejectReason.setText(model.getRejectReason());
        }
        else{
            holder.status.setTextColor(context.getResources().getColor(R.color.approvedText));
            holder.status.setBackgroundResource(R.drawable.button_color_green);
        }

    }

    @Override
    public int getItemCount() {
        return leavesList!=null?leavesList.size():0;
    }

    public static class handler extends RecyclerView.ViewHolder {
        //        private final ImageView courseIV;
        private final TextView dateRange;
        private final TextView applyDays;
        private final TextView leaveType;

        private final TextView approvedBy;
        private CardView cardView;
        private ImageView img;

        private LinearLayout rejectReasonLayout;
        private TextView rejectReason;

        private AppCompatButton status;

        public handler(View itemView) {
            super(itemView);
            dateRange = itemView.findViewById(R.id.dateRange);
            status = itemView.findViewById(R.id.status);
            applyDays = itemView.findViewById(R.id.applyDays);
            leaveType = itemView.findViewById(R.id.leaveType);
            approvedBy = itemView.findViewById(R.id.approvedBy);
            rejectReasonLayout = itemView.findViewById(R.id.rejectReasonLayout);
            rejectReason = itemView.findViewById(R.id.reasonToReject);
        }
    }

}
