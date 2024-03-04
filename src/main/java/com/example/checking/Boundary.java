package com.example.checking;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ZoomControls;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.checking.Model.LocationsModel;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Boundary extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<LatLng> polygonPoints = new ArrayList<>();
    private Polygon polygon;
    APIService apiService = RetrofitClient.getClient().create(APIService.class);
    View view;

    private void initializePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), "AIzaSyAHNqB-5OeXeVss95CwnVO7IFjKbJe7mzE");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_maps, parent, false);
        initializePlaces();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button saveBtn = view.findViewById(R.id.btnSave);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePolygon();
            }
        });

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,  Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i("Places_auto_complete", "Place: " + place.getName() + ", " + place.getId()+", "+place.getLatLng());
                if (place != null && place.getLatLng() != null) {
                    Toast.makeText(getContext(), "in if"+ place.getLatLng(), Toast.LENGTH_SHORT).show();
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15), new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            // Re-enable map gestures after the camera animation finishes
                            mMap.getUiSettings().setAllGesturesEnabled(true);
                        }

                        @Override
                        public void onCancel() {
                            // Re-enable map gestures even if the camera animation is canceled
                            mMap.getUiSettings().setAllGesturesEnabled(true);
                        }
                    });
                }
                else{
                    Toast.makeText(getContext(), "in else", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i("Places_auto_complete", "An error occurred: " + status);
            }
        });
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings uiSettings = mMap.getUiSettings();
        // Enable or disable desired UI settings
        uiSettings.setZoomControlsEnabled(true); // Show zoom controls
        uiSettings.setCompassEnabled(true); // Show compass
        uiSettings.setZoomGesturesEnabled(true); // Enable zoom gestures
        uiSettings.setScrollGesturesEnabled(true); // Enable pinch-to-zoom

//        UiSettings uiSettings = mMap.getUiSettings();
//        // Enable or disable desired UI settings
////        uiSettings.setZoomControlsEnabled(true); // Show zoom controls
//        uiSettings.setCompassEnabled(true); // Show compass
//        uiSettings.setZoomGesturesEnabled(true);
////        uiSettings.setMyLocationButtonEnabled(true); // Show my location button
        mMap.setOnMapClickListener(latLng -> {
            // Add the clicked point to the polygon
            polygonPoints.add(latLng);
            Log.d("TAG", "polygone looks like : "+polygonPoints);
            // Draw the Delaunay triangulation polygon
            drawDelaunayPolygon();
        });
    }

    private void savePolygon() {
        if (polygonPoints.size() > 2) {
            TextInputLayout locationName = view.findViewById(R.id.Name);
            TextInputLayout locationAddress = view.findViewById(R.id.Address);
            LocationsModel location = new LocationsModel();
            location.setPolygon(convertLatLngToPoint(polygonPoints));
            location.setName(String.valueOf(locationName.getEditText().getText()));
            location.setAddress(String.valueOf(locationAddress.getEditText().getText()));

            Call<String> call = apiService.addLocation(location);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    System.out.println("response: "+response);
                    if (response.isSuccessful()) {
                        // Handle successful response
                        Toast.makeText(getActivity(), "Location added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle unsuccessful response
                        Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    // Handle network errors
                    System.out.println("error: "+t.getStackTrace());
                }
            });
            LocationListView fragment = new LocationListView();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, "");
            fragmentTransaction.addToBackStack("location");
            fragmentTransaction.commit();
        } else {
            Toast.makeText(getContext(), "Please draw a valid polygon on the map before saving.", Toast.LENGTH_SHORT).show();
        }
    }


    public static List<LocationsModel.Point> convertLatLngToPoint(List<LatLng> latLngList) {
        List<LocationsModel.Point> pointList = new ArrayList<>();
        for (LatLng latLng : latLngList) {
            pointList.add(new LocationsModel.Point(latLng.latitude, latLng.longitude));
        }
        return pointList;
    }

    private void drawDelaunayPolygon() {
        // Clear the existing polygon
        if (polygon != null) {
            polygon.remove();
        }
        Log.d("TAG", "polygonPoints : "+polygonPoints);
        // Draw the Delaunay triangulation polygon with current points
        if (polygonPoints.size() > 2) {
            // Create a DelaunayTriangulationBuilder and add the points
            DelaunayTriangulationBuilder dtb = new DelaunayTriangulationBuilder();
            GeometryFactory geometryFactory = new GeometryFactory();

            // Create a JTS Geometry from the points
            Coordinate[] coordinates = new Coordinate[polygonPoints.size()];
            for (int i = 0; i < polygonPoints.size(); i++) {
                LatLng point = polygonPoints.get(i);
                coordinates[i] = new Coordinate(point.longitude, point.latitude);
            }
            Geometry inputGeometry = geometryFactory.createMultiPointFromCoords(coordinates);

            // Set the input geometry for the DelaunayTriangulationBuilder
            dtb.setSites(inputGeometry);

            // Build the Delaunay triangulation
            QuadEdgeSubdivision subdiv = dtb.getSubdivision();

            // Extract the convex hull from the triangulation
            Geometry convexHull = subdiv.getEdges(new GeometryFactory()).convexHull();

            // Draw the Delaunay triangulation polygon (convex hull)
            List<LatLng> triangulationPoints = new ArrayList<>();
            for (Coordinate coordinate : convexHull.getCoordinates()) {
                LatLng point = new LatLng(coordinate.y, coordinate.x);
                triangulationPoints.add(point);
            }

            PolygonOptions polygonOptions = new PolygonOptions()
                    .addAll(triangulationPoints);

            polygon = mMap.addPolygon(polygonOptions);
        }
    }
}
