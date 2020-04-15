package com.example.projectgooglemap;

import Module.HuongDi;
import Module.TimHuongDi;
import Module.TimHuongDiListener;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , TimHuongDiListener {

    private GoogleMap mMap;
    private Button btnTimDuong;
    private EditText txtKhoiHanh;
    private EditText txtKetThuc;
    private List<Marker> markersKhoiHanh=new ArrayList<>();
    private List<Marker> markersKetThuc= new ArrayList<>();
    private List<Polyline> polylinesPath= new ArrayList<>();
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btnTimDuong=(Button)findViewById(R.id.btnTimDuong);
        txtKhoiHanh=(EditText)findViewById(R.id.txtDiemKhoiHanh);
        txtKetThuc=(EditText) findViewById(R.id.txtDiemDen);
        
        btnTimDuong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GuiYeuCau();
            }
        });
    }

    private void GuiYeuCau() {
        String khoihanh=txtKhoiHanh.getText().toString();
        String ketthuc=txtKetThuc.getText().toString();
        if(khoihanh.isEmpty()){
            Toast.makeText(this,"Làm ơn nhập vào điểm khởi hành !", Toast.LENGTH_LONG).show();
            return;
        }
        if(ketthuc.isEmpty()){
            Toast.makeText(this, "Làm ơn nhập điểm đến !", Toast.LENGTH_LONG).show();
            return;
        }

        try{
            new TimHuongDi(this, khoihanh,ketthuc).thucthu();
        }

        catch (UnsupportedEncodingException ex){
            ex.printStackTrace();
    }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng UTC = new LatLng(21.028375, 105.803528);
        mMap.addMarker(new MarkerOptions()
                .position(UTC)
                .title("Đại Học Giao Thông Vận Tải ")
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pushpin)));



        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UTC, 18));
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Vui Lòng Đợi....!",
                "Đang Tìm Đường...!!", true);

        if (markersKhoiHanh != null) {
            for (Marker marker : markersKhoiHanh) {
                marker.remove();
            }
        }

        if (markersKetThuc != null) {
            for (Marker marker : markersKetThuc) {
                marker.remove();
            }
        }

        if (polylinesPath != null) {
            for (Polyline polyline:polylinesPath ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<HuongDi> routes) {
        progressDialog.dismiss();
        polylinesPath = new ArrayList<>();
        markersKhoiHanh = new ArrayList<>();
        markersKetThuc = new ArrayList<>();

        for (HuongDi route : routes ) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.toadoKhoiHanh, 16));
            ((TextView) findViewById(R.id.txtThoiGian)).setText(route.thoiGian.text);
            ((TextView) findViewById(R.id.txtKhoangCach)).setText(route.quangDuong.text);

            markersKhoiHanh.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.diachiKhoiHanh)
                    .position(route.toadoKhoiHanh)));
            markersKetThuc.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.diachiKetThuc)
                    .position(route.toadoKetThuc)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.Diem.size(); i++)
                polylineOptions.add(route.Diem.get(i));

            polylinesPath.add(mMap.addPolyline(polylineOptions));
    }
}





}
