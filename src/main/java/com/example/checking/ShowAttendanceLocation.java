package com.example.checking;

        import android.graphics.Color;
        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.TextView;

        import androidx.fragment.app.Fragment;

        import com.example.checking.Model.AttendanceModel;
        import com.example.checking.Model.LocationsModel;
        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.LatLngBounds;
        import com.google.android.gms.maps.model.Polygon;
        import com.google.android.gms.maps.model.PolygonOptions;
        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.List;

public class ShowAttendanceLocation extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    List<LocationsModel.Point> polygon;
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_show_attendance_location, parent, false);
        Bundle args = getArguments();
        LocationsModel loc = (LocationsModel) args.getSerializable("loc");
        AttendanceModel att = (AttendanceModel) args.getSerializable("attendance");
        System.out.println("Name: "+loc.getName());
        polygon = loc.getPolygon();
        TextView address = view.findViewById(R.id.address);
        address.setText("LOCATION: "+loc.getName());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        TextView date  = view.findViewById(R.id.date);
        TextView time = view.findViewById(R.id.time);
        date.setText(att.getTime());
        time.setText(loc.getAddress());

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Draw polygon on the map
        drawPolygon(polygon);

        // Move camera to the bounds of the polygon
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getPolygonBounds(polygon), 50));
    }

    private void drawPolygon(List<LocationsModel.Point> polygonPoints) {
        if (polygonPoints.size() >= 3) {
            List<LatLng> latLngs = sortPointsClockwise(polygonPoints);

            // Draw the polygon on the map
            PolygonOptions polygonOptions = new PolygonOptions()
                    .addAll(latLngs)
                    .strokeColor(Color.RED)
                    .fillColor(Color.argb(100, 255, 0, 0));
            Polygon polygon1 = mMap.addPolygon(polygonOptions);
        }
    }

    private List<LatLng> sortPointsClockwise(List<LocationsModel.Point> points) {
        // Calculate the centroid of the points
        double cx = 0, cy = 0;
        for (LocationsModel.Point point : points) {
            cx += point.getLatitude();
            cy += point.getLongitude();
        }
        cx /= points.size();
        cy /= points.size();

        // Sort points based on the angle from the centroid
        double finalCy = cy;
        double finalCx = cx;
        Collections.sort(points, (p1, p2) -> {
            double angle1 = Math.atan2(p1.getLongitude() - finalCy, p1.getLatitude() - finalCx);
            double angle2 = Math.atan2(p2.getLongitude() - finalCy, p2.getLatitude() - finalCx);
            return Double.compare(angle1, angle2);
        });

        // Convert to LatLng and return
        List<LatLng> sortedLatLngs = new ArrayList<>();
        for (LocationsModel.Point point : points) {
            sortedLatLngs.add(new LatLng(point.getLatitude(), point.getLongitude()));
        }
        return sortedLatLngs;
    }



    private LatLngBounds getPolygonBounds(List<LocationsModel.Point> polygonPoints) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LocationsModel.Point point : polygonPoints) {
            builder.include(new LatLng(point.getLatitude(), point.getLongitude()));
        }
        return builder.build();
    }


}
