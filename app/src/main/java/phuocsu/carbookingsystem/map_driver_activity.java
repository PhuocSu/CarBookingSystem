    //Xóa vị trí khách hàng khi tắt app trên firebase
    //DriverAvailable
    package phuocsu.carbookingsystem;

    import androidx.annotation.NonNull;
    import androidx.core.app.ActivityCompat;
    import androidx.fragment.app.FragmentActivity;

    import android.Manifest;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.location.Location;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.ImageButton;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.bumptech.glide.Glide;
    import com.google.android.gms.location.FusedLocationProviderClient;
    import com.google.android.gms.location.LocationServices;
    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.OnMapReadyCallback;
    import com.google.android.gms.maps.SupportMapFragment;
    import com.google.android.gms.maps.model.BitmapDescriptorFactory;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.Marker;
    import com.google.android.gms.maps.model.MarkerOptions;
    import com.google.android.gms.tasks.OnSuccessListener;
    import com.google.android.gms.tasks.Task;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.firebase.geofire.GeoFire;
    import com.firebase.geofire.GeoLocation;
    import com.google.firebase.database.ValueEventListener;


    import java.util.List;
    import java.util.Map;
    import java.util.Objects;

    import phuocsu.carbookingsystem.databinding.ActivityMapDriverBinding;

    public class map_driver_activity extends FragmentActivity implements OnMapReadyCallback {

        private Button btnDangXuat;
        private ImageButton btnCaNhan, btnHoatDong;
        private String customerId = "";

        private GoogleMap mMap;
        private final int FINE_PERMISSION_CODE = 1;
        Location currentLocation;
        FusedLocationProviderClient fusedLocationProviderClient;

        private Marker pickupMarker;
        private DatabaseReference assignedCustomerPickupLocationRef;
        private ValueEventListener assignedCustomerPickupLocationRefListener;

    private ActivityMapDriverBinding binding;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

         binding = ActivityMapDriverBinding.inflate(getLayoutInflater());
         setContentView(binding.getRoot());

         fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
         getLastLocation();
         getAssignedCustomer(); //TÌm kiếm khách được chỉ định

         //Log out App and Firebase with Button Đăng xuất, quay trở về MainActivity
            btnDangXuat = findViewById(R.id.btnDangXuat);
            btnDangXuat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(map_driver_activity.this,MainActivity.class);
                    startActivity(intent);
                }
            });

            btnCaNhan = findViewById(R.id.btnCaNhan);
            btnCaNhan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(map_driver_activity.this, personal_driver_detail.class);
                    startActivity(intent);
                }
            });
        }

        //Lấy Request của Customer ID
        private void getAssignedCustomer() {
            String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child("Drivers").child(driverId).child("customerRideId"); /*Hiển thị id của customer trong [Drivers>DriverId]
                                                                                                                           => Biểu thị tài xế đó đã nhận request của khách hàng đó*/
            assignedCustomerRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Log.d("Firebase", "Customer assigned: " + snapshot.getValue().toString());
                            customerId = snapshot.getValue().toString();
                            getAssignedCustomerPickupLocation();
                            getAssignedCustomerInfor();
                    }
                    else{ //bị hủy chuyến
                        Log.d("Firebase", "Customer unassigned");
                        customerId = "";
                        if(pickupMarker!=null){
                            pickupMarker.remove();;
                        }
                        if(assignedCustomerPickupLocationRefListener != null){
                            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
                        }
                        LinearLayout customerInfoPanel = findViewById(R.id.customerInfoPanel);
                        customerInfoPanel.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        //display Customer Information on Request
        private void getAssignedCustomerInfor() {
            DatabaseReference customerDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child("Customers").child(customerId);

            // Hiển thị khung thông tin
            LinearLayout customerInfoPanel = findViewById(R.id.customerInfoPanel);
            customerInfoPanel.setVisibility(View.VISIBLE);

            customerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                        // Lấy các TextView và ImageView từ XML
                        TextView customerName = findViewById(R.id.customerName);
                        TextView customerPhone = findViewById(R.id.customerPhone);
                        TextView customerDestination = findViewById(R.id.customerDestination);
                        ImageView customerProfileImage = findViewById(R.id.customerProfileImage);

                        // Hiển thị dữ liệu từ Firebase
                        if (snapshot.hasChild("tenNguoiDung")) {
                            customerName.setText(snapshot.child("tenNguoiDung").getValue().toString());
                        }

                        if (snapshot.hasChild("sdt")) {
                            customerPhone.setText(snapshot.child("sdt").getValue().toString());
                        }

                        if (snapshot.hasChild("destination")) {
                            customerDestination.setText("Điểm đến: " + snapshot.child("destination").getValue().toString());
                        }

                        if (snapshot.hasChild("profileImageUrl")) {
                            String imageUrl = snapshot.child("profileImageUrl").getValue().toString();
                            Glide.with(getApplicationContext())
                                    .load(imageUrl)
                                    .into(customerProfileImage); // Sử dụng thư viện Glide để tải ảnh
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý lỗi
                    Toast.makeText(getApplicationContext(), "Không thể tải thông tin khách hàng!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Nhận địa điểm đón khách hàng được chỉ định
        private void getAssignedCustomerPickupLocation() {
            assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequest").child(customerId).child("l"); //child l trong filrebase
            assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists() && !customerId.equals("")){
                        List<Object> map = (List<Object>) snapshot.getValue(); /*"0": "30.215",     vĩ độ
                                                                                  "1": "-90.253"    kinh độ*/
                        double locationLat = 0, locationLng = 0;
                        if(map.get(0) != null) // trong firebase chứa: g,l,0,1
                        {
                            locationLat = Double.parseDouble(map.get(0).toString()); //vĩ độ
                        }
                        if(map.get(1) != null) // trong firebase chứa: g,l,0,1
                        {
                            locationLng = Double.parseDouble(map.get(1).toString()); //kinh độ
                        }

                        LatLng driverLatLng = new LatLng(locationLat,locationLng); // vị trí của tài xế
                        pickupMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Vị trí khách hàng").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car_foreground)));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void getLastLocation(){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
                return;
            }
            Task <Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        currentLocation = location;


                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        assert mapFragment != null;
                        mapFragment.getMapAsync(map_driver_activity.this);

                        saveUpdatedLocation(currentLocation);

                    }
                    else{
                        Toast.makeText(map_driver_activity.this, "Không thể lấy vị trí, Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        //Sử dụng GeoFire để lưu thông tin (logiture,Latitude) lên Firebase => Save Updated Location
        public void saveUpdatedLocation(@NonNull Location location) {
            //Lưu thông tin vị trí lên GeoFire và Firebase
            String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("DriverAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("DriverWorking");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);

            //Cập nhật vị trí tài xế cho khách hàng
            switch (customerId) {
                case "": //không có khách để tài xế đón
                    // Trường hợp tài xế dừng không làm việc và bắt đầu lại
                    geoFireWorking.removeLocation(userId); //Trạng thái working không còn
                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude())); //Cập nhật lại vị trí

                    break;
                default: //trường hợp khác với điều kiên đầu
                    geoFireAvailable.removeLocation(userId); //Trạng thái Available không còn
                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude())); //Cập nhật lại vị trí

                    break;
            }
        }

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;

            if(currentLocation !=null) {
                // Add a marker in Sydney and move the camera
                LatLng sydney = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()); // hiển thị trong trong Firebase tưong ứng là 0 với 1
                                              //currentLocation.getLatitude(), currentLocation.getLongitude() 10.6748992,106.6908285
                mMap.addMarker(new MarkerOptions().position(sydney).title("Vị trí của bạn đây nè"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15));
            }
            else{
                Toast.makeText(this, "Không thể lấy vị trí hiện tại!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if(requestCode == FINE_PERMISSION_CODE){
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLastLocation();
                } else{
                    Toast.makeText(this, "Quyền truy cập vị trí bị từ chối. Hãy cho phép!",Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onStop() {
            super.onStop();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String driverId = currentUser.getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriverAvailable");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.removeLocation(driverId);
            }
        }
    }