package com.example.checking;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

        return view;
    }
}
