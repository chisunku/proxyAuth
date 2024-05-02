package com.example.checking;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import com.example.checking.Model.Employee;
import com.squareup.picasso.Picasso;

public class Profile extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.activity_profile, container, false);
        Bundle args = getArguments();
        Employee employee = (Employee) args.getSerializable("employee");

        Log.d("TAG", "onCreateView: employee.getImageURL() "+ employee.getImageURL());

        ImageView imageView = view.findViewById(R.id.profile_image);
        Picasso.get()
                .load(employee.getImageURL())
                .into(imageView);


        TextView name = view.findViewById(R.id.name);
        name.setText(employee.getName());

        TextView designation = view.findViewById(R.id.designation);
        designation.setText(employee.getDesignation());

        TextView email = view.findViewById(R.id.email);
        email.setText(employee.getEmail());

        TextView phone = view.findViewById(R.id.phone);
        phone.setText(employee.getAddress());

        TextView address = view.findViewById(R.id.address);
        address.setText(employee.getAddress());

        TextView contact = view.findViewById(R.id.phone);
        contact.setText(employee.getContactNo());

        TextView location = view.findViewById(R.id.office);
        location.setText(employee.getLocationsModel().getName());

        TextView officeLocation = view.findViewById(R.id.officeLocation);
        officeLocation.setText(employee.getLocationsModel().getAddress());

        AppCompatButton button = view.findViewById(R.id.logout);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                Intent i = new Intent(getActivity(), Authentication.class);
                startActivity(i);
            }
        });


        return view;
    }
}
