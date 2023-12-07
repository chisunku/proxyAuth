package com.example.checking;
        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;
        import androidx.annotation.NonNull;
        import androidx.cardview.widget.CardView;
        import androidx.fragment.app.FragmentManager;
        import androidx.recyclerview.widget.RecyclerView;
        import java.util.ArrayList;
        import java.util.List;

public class AttendanceHistoryAdapter extends RecyclerView.Adapter<AttendanceHistoryAdapter.handler> {

    private final Context context;
    private List<Attendance_model> locationModelArrayList;
    FragmentManager fragmentManager;

    // Constructor
    public AttendanceHistoryAdapter(Context context, List<Attendance_model> locationModelArrayList) {
        this.context = context;
        this.locationModelArrayList = locationModelArrayList;
    }
    public void setData(List<Attendance_model> locationModelArrayList) {
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
        Attendance_model model = locationModelArrayList.get(position);
        holder.boxName.setText(model.getTimeRef());
        holder.time.setText("" + model.getTime());
        holder.img.setImageResource(model.getImgId());
        holder.date.setText(model.getDate().toString());
    }

    @Override
    public int getItemCount() {
        return locationModelArrayList.size();
    }

    // View holder class for initializing of your views such as TextView and Imageview
    public static class handler extends RecyclerView.ViewHolder {
        //        private final ImageView courseIV;
        private final TextView boxName;
        private final TextView time;
        private CardView cardView;
        private TextView date;
        private ImageView img;

        public handler(View itemView) {
            super(itemView);
            boxName = itemView.findViewById(R.id.type);
            time = itemView.findViewById(R.id.time);
            cardView = itemView.findViewById(R.id.card);
            img = itemView.findViewById(R.id.icon);
            date = itemView.findViewById(R.id.date);
        }
    }
}
