package com.example.checking;

import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.checking.Model.Employee;
import com.example.checking.Model.Leaves;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeViewAdapter extends RecyclerView.Adapter<EmployeeViewAdapter.handler>{
    private List<Employee> empList;
    private Context context;
    APIService apiService;

    public EmployeeViewAdapter(Context context, List<Employee> empList) {
        System.out.println("in adapter binder Employee view adapter");
        this.context = context;
        this.empList = empList;
        apiService = RetrofitClient.getClient().create(APIService.class);
    }

    @NonNull
    @Override
    public EmployeeViewAdapter.handler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_emp_card, parent, false);
        System.out.println("in adapter binder on create view emp adapter");
        return new EmployeeViewAdapter.handler(view);
    }


    @Override
    public void onBindViewHolder(@NonNull EmployeeViewAdapter.handler holder, int position) {
        // to set data to textview and imageview of each card layout
        System.out.println("in adapter binder holder leaves adapter");
        Employee model = empList.get(position);
        Picasso.get()
                .load(model.getImageURL())
                .into(holder.profile_image);
        holder.name.setText(model.getName());
        holder.email.setText(model.getEmail());
        holder.designation.setText(model.getDesignation());
        holder.address.setText(model.getAddress());
        holder.contactNo.setText(model.getContactNo());
    }

    @Override
    public int getItemCount() {
        return empList!=null?empList.size():0;
    }

    public static class handler extends RecyclerView.ViewHolder {

        final private ImageView profile_image;
        final private TextView name;
        final private TextView email;
        final private TextView designation;
        final private TextView address;
        final private TextView contactNo;


        public handler(View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.name);
            email = itemView.findViewById(R.id.email);
            designation = itemView.findViewById(R.id.designation);
            address = itemView.findViewById(R.id.address);
            contactNo = itemView.findViewById(R.id.contact);
        }
    }
}
