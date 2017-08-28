package com.polito.cesarldm.tfg_bitadroidbeta;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class PopMapActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener,ActivityCompat.OnRequestPermissionsResultCallback{


    MapView mapView;
    GoogleMap gMap;
    Polyline polyLine;
    Location myLocation;
    ArrayList<Location> locations=new ArrayList<Location>();
    List<LatLng> routeList=new ArrayList<LatLng>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_map);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width=displayMetrics.widthPixels;
        int height=displayMetrics.heightPixels;
        getWindow().setLayout((int)(width*0.8),(int)(height*0.6));
        if(getIntent().hasExtra("Locations")) {
            locations = getIntent().getParcelableArrayListExtra("Locations");
            checkDuplicateLocations();
        }else{
            Toast.makeText(this,"No location data available",Toast.LENGTH_SHORT).show();

        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void checkDuplicateLocations() {
        for(int i=0;i<locations.size();i++){
            Location l=locations.get(i);
            LatLng tempLatLng=new LatLng(l.getLatitude(),l.getLongitude());
            if (!routeList.contains(tempLatLng)){
                routeList.add(tempLatLng);
            }

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap=googleMap;
        gMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
        currentLocation();
        drawPoints();
        drawLine();
    }

    private void currentLocation() {
        if(gMap.isMyLocationEnabled()&&locations.size()!=0){
            Location l=locations.get(locations.size()-1);
                LatLng mLatLng = new LatLng(l.getLatitude(), l.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mLatLng, 17);
                gMap.animateCamera(cameraUpdate);

        }
    }

    private void drawPoints() {
    }

    public void drawLine() {
        if (routeList == null) {
            Log.e("Draw Line", "got null as parameters");
            return;
        }

        Polyline line = gMap.addPolyline(new PolylineOptions().width(4).color(Color.BLUE));
        line.setPoints(routeList);
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
         if (gMap != null) {
            // Access to the location has been granted to the app.
             try {
                 gMap.setMyLocationEnabled(true);
             }catch (SecurityException se){
                 Toast.makeText(this,"Location Permission not granted",Toast.LENGTH_SHORT);
                 finish();
             }
        }
    }
    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }
}
/**
 * public List<LatLng> routeArray = new ArrayList<LatLng>();

 for(int i =0; i<contentAsJsonObject.size(); i++) {
 JSONObject json = contentAsJsonObject.get(i);
 try {
 final String lat = json.getString("Lat");
 final String lng = json.getString("Lng");
 LatLng latLng = new LatLng(Double.parseDouble(lat.trim()),Double.parseDouble(lng.trim()));
 if (!routeArray.contains(latLng)){
 routeArray.add(lat);
 }
 } catch (Exception e) {
 e.printStackTrace();
 return;
 }
 }
 drawLine(routeArray);


 public void drawLine(List<LatLng> points) {
 if (points == null) {
 Log.e("Draw Line", "got null as parameters");
 return;
 }

 Polyline line = map.addPolyline(new PolylineOptions().width(3).color(Color.RED));
 line.setPoints(points);
 }
 *
 *
 */
