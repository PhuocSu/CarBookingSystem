//Xóa vị trí tài xế khi tắt app trên firebase
//Chỉnh sửa geoFire trong DriverWorking sao cho hợp lý?
package phuocsu.carbookingsystem;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import phuocsu.carbookingsystem.databinding.ActivityMapCustomerBinding;

public class map_customer_activity extends FragmentActivity implements OnMapReadyCallback {

    private Button btnDangXuat, btnRequest;
    private ImageButton btnCaNhan, btnHistory;

    private LatLng pickupLocation;
    private LatLng destinationLocation; //Điểm dến
    private boolean isDestinationLocked = false; // Đánh dấu điểm đến đã được "khóa"

    private Marker currentMarker; // Marker biểu thị người dùng hiển thị vị trí điểm đón (tạo ra hoặc được cập nhật)
    private Marker userCurrentMarker; // Marker biểu thị vị trí hiện tại ban đầu của người dùng (Chấm xanh).
    private Circle userCurrentCircle; // Tùy chọn: Vòng tròn xung quanh vị trí ban đầu.

    private int radius = 1; //bán kính xung quanh pickupLocation
    private Boolean driverFound = false;
    private String driverFoundId;

    private GoogleMap mMap;
    private final int FINE_PERMISSION_CODE = 1;
    Location currentLocation; // ~ mLastLocation
    FusedLocationProviderClient fusedLocationProviderClient;

    private Boolean requestBol = false;
    GeoQuery geoQuery;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private Marker pickupMarker;
    private Marker destinationMarker; //Điểm đến

    private ActivityMapCustomerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapCustomerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Khởi tạo fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation(); // Gọi hàm lấy vị trí

        btnDangXuat = findViewById(R.id.btnDangXuat);
        btnDangXuat.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(map_customer_activity.this, MainActivity.class);
            startActivity(intent);
        });

        btnCaNhan = findViewById(R.id.btnCaNhan);
        btnCaNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(map_customer_activity.this, personal_customer_detail.class);
                startActivity(intent);
            }
        });

        btnRequest = findViewById(R.id.btnRequest);
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestBol) {
                    cancelRequest();
                    isDestinationLocked = false; // Mở khóa điểm đến khi hủy yêu cầu.
                } else {
                    if (destinationLocation != null) {
                        createRequest();
                        isDestinationLocked = true; // Khóa điểm đến khi yêu cầu được tạo.
                    } else {
                        Toast.makeText(map_customer_activity.this, "Vui lòng chọn vị trí điểm đến trên bản đồ!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    private void createRequest() {
        requestBol = true;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userId,new GeoLocation(currentLocation.getLatitude(),currentLocation.getLongitude()));

        pickupLocation = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Đón tại đây"));

        destinationLocation = new LatLng(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude);
        DatabaseReference destinationRef = FirebaseDatabase.getInstance().getReference("CustomerRequest");
        destinationRef.child(userId).setValue(new GeoLocation(destinationLocation.latitude, destinationLocation.longitude));


        btnRequest.setText("Bắt tài xế của bạn ...");
        getClosetDriver(); //Tìm kiếm vị trí driver gần nhất
    }

    private void cancelRequest() {
        requestBol = false;
        isDestinationLocked = false; // Mở khóa điểm đến khi yêu cầu bị hủy.

        driverFound = false;
        radius = 1;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        DatabaseReference destinationRef = FirebaseDatabase.getInstance().getReference("CustomerDestinations");
        destinationRef.child(userId).removeValue();

        if (currentMarker != null) {
            currentMarker.remove();
        }

        if (destinationMarker != null) {
            destinationMarker.remove();
        }

        btnRequest.setText("Đặt chuyến đi");

    }

    private void getClosetDriver() {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("DriverAvailable");
        GeoFire geofire = new GeoFire(driverLocation);
        GeoQuery geoQuerry = geofire.queryAtLocation(new GeoLocation(pickupLocation.latitude,pickupLocation.longitude),radius);
        geoQuerry.removeAllListeners(); //Xóa tất cả sự kiện đăng ký trước đó với GeoFire
        //Không cần theo dõi các thay đổi trong khu vực (tìm tài xế, dừng theo dõi vị trí)
        //Giải phóng tài nguyên(khi người dùng thoát màn hình tìm kiếm tài xế)

        geoQuerry.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && requestBol){
                    driverFound = true; //đang tìm tài xế
                    driverFoundId = key; /*khóa này không thay đổi khi có nhiều tài xế trong 1 km,
                                           key sẽ mặc định Id đã tìm kiếm của tài xế đó và không thay đổi*/

                    //Tìm kiếm tài xế xung quanh địa điểm đón

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRideId",customerId);
                    /*Nếu "customerRideId" chưa tồn tại trong map, cặp khóa-giá trị (key-value) này sẽ được thêm vào.
                    Nếu "customerRideId" đã tồn tại, giá trị tương ứng sẽ được thay thế bằng giá trị mới là customerId.
                    => customerRideId sẽ thay thế cho customerId*/
                    driverRef.updateChildren(map);

                    //Hiển thị vị trí tài xế trong bản đồ của khách hàng
                    getDriverLocation();
                    btnRequest.setText("Tìm kiếm vị trí tài xế ...");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound){
                    radius++; // Nếu không tìm thấy driver, số kilometes tìm kiếm sẽ tăng lên
                    getClosetDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker mDriverMarker; //đánh dấu vị trí của một member
    private void getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().getReference()
                .child("DriverAvailable") //DriverWorking
                .child(driverFoundId)
                .child("l");//child l trong filrebase
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && requestBol){
                    List<Object> map = (List<Object>) snapshot.getValue(); //lưu vĩ độ và kinh độ dưới dạng danh sách
                    double locationLat = 0, locationLng = 0;
                    btnRequest.setText("Tìm thấy tài xế !!!");
                    if(map.get(0) != null) // trong firebase chứa: g,l,0,1
                    {
                        locationLat = Double.parseDouble(map.get(0).toString()); //vĩ độ
                    }
                    if(map.get(1) != null) // trong firebase chứa: g,l,0,1
                    {
                        locationLng = Double.parseDouble(map.get(1).toString()); //kinh độ
                    }

                    LatLng driverLatLng = new LatLng(locationLat,locationLng); // vị trí của tài xế
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }

                    //Tính khoảng cách giữa tài xế và điểm đón khách (pickupLocation)
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location(""); //Location tính khoảng cách cách bằng meter
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if(distance < 100) {
                        btnRequest.setText("Tài xế gần đây. Vị trí: " + String.valueOf(distance));
                    }else{
                        btnRequest.setText("Đã tìm thấy tài xế " + String.valueOf(distance));
                    }

                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Vị trí tài xế của bạn")
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car_foreground)));
                }
                else{
                    Toast.makeText(map_customer_activity.this, "Không lấy được tọa độ từ Firebase!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = location;
                        updateMap(location); // Hiển thị vị trí hiện tại trên bản đồ.
                        chooseDestination();
                        saveUpdatedLocation(location);
                    } else {
                        Toast.makeText(map_customer_activity.this, "Không thể lấy vị trí, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateMap(Location location) {
        if (mMap == null) {
            Toast.makeText(this, "Bản đồ chưa được khởi tạo.", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        // Thêm marker/vòng tròn cho vị trí hiện tại của người dùng (màu xanh).
        if (userCurrentMarker == null) {
            userCurrentMarker = mMap.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .title("Vị trí hiện tại")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); // Chấm màu xanh.
        } else {
            userCurrentMarker.setPosition(userLocation); // Cập nhật vị trí nếu đã có marker.
        }

        // Tùy chọn: Thêm vòng tròn xung quanh vị trí hiện tại.
        if (userCurrentCircle == null) {
            userCurrentCircle = mMap.addCircle(new CircleOptions()
                    .center(userLocation)
                    .radius(30) // Bán kính 30 mét.
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)); // Màu xanh nhạt.
        } else {
            userCurrentCircle.setCenter(userLocation); // Cập nhật tâm của vòng tròn.
        }

        // Di chuyển camera đến vị trí hiện tại.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }
    public void chooseDestination(){
        //Chọn điểm đến trên map
        mMap.setOnMapClickListener(latLng -> {
            if (isDestinationLocked) {
                Toast.makeText(this, "Bạn không thể thay đổi điểm đến sau khi yêu cầu đã được gửi.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (destinationMarker != null) {
                destinationMarker.remove();
            }

            destinationMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Vị trí điểm đến")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            destinationLocation = latLng;
        });
    }

    public void saveUpdatedLocation(@NonNull Location location) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

        pickupLocation = new LatLng(location.getLatitude(), location.getLongitude());
        btnRequest.setText("Tìm kiếm vị trí tài xế");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (currentLocation != null) {
            updateMap(currentLocation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Quyền truy cập vị trí bị từ chối. Vui lòng cấp quyền!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();

        // Xóa listener GeoQuery nếu có
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
            geoQuery = null;
        }

        // Xóa listener vị trí tài xế nếu có
        if (driverLocationRef != null && driverLocationRefListener != null) {
            driverLocationRef.removeEventListener(driverLocationRefListener);
            driverLocationRef = null;
            driverLocationRefListener = null;
        }

    }
}