package com.example.checking;
        import android.content.Context;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;
        import androidx.annotation.NonNull;
        import androidx.cardview.widget.CardView;
        import androidx.fragment.app.FragmentManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.example.checking.Model.Attendance;

        import java.text.SimpleDateFormat;
        import java.util.List;
        import java.util.Locale;

public class AttendanceHistoryAdapter extends RecyclerView.Adapter<AttendanceHistoryAdapter.handler> {

    private final Context context;
    private List<Attendance> locationModelArrayList;
    FragmentManager fragmentManager;

    // Constructor
    public AttendanceHistoryAdapter(Context context, List<Attendance> locationModelArrayList) {
        this.context = context;
        this.locationModelArrayList = locationModelArrayList;
    }
    public void setData(List<Attendance> locationModelArrayList) {
        this.locationModelArrayList = locationModelArrayList;
    }

    @NonNull
    @Override
    public AttendanceHistoryAdapter.handler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_attendace_history_card, parent, false);
        System.out.println("in adapter binder on create view");
        return new handler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceHistoryAdapter.handler holder, int position) {
        // to set data to textview and imageview of each card layout
        System.out.println("in adapter binder holder");
        Attendance model = locationModelArrayList.get(position);

//        Log.d("atthistory", "onBindViewHolder atthistory: "+model+" "+model.getLocationsModel().getName());

        // Create a SimpleDateFormat with the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        // Extract date and time
        String formattedDate = dateFormat.format(model.getCheckInDate());
        String formattedTime = timeFormat.format(model.getCheckInDate());

        String time = "Check-in: "+ formattedTime;
        if(model.getCheckOutDate()!=null){
            formattedTime = timeFormat.format(model.getCheckOutDate());
            time += "\n\nCheck-out: "+formattedTime;
        }
        holder.date.setText(time);
        holder.time.setText(formattedDate);
        holder.location.setText(model.getLocationsModel().getName());
    }

    @Override
    public int getItemCount() {
        Log.d("attendance history adapter", "getItemCount: item count called : "+locationModelArrayList.size());
        return locationModelArrayList.size();
    }

    // View holder class for initializing of your views such as TextView and Imageview
    public static class handler extends RecyclerView.ViewHolder {
        //        private final ImageView courseIV;
        private final TextView date;
        private final TextView location;
        private final TextView time;
        private CardView cardView;
        private ImageView img;

        public handler(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            location = itemView.findViewById(R.id.location);
            time = itemView.findViewById(R.id.time);
            cardView = itemView.findViewById(R.id.card);
            img = itemView.findViewById(R.id.icon);
        }
    }
}
