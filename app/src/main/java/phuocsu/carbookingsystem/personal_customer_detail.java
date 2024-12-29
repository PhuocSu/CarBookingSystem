package phuocsu.carbookingsystem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class personal_customer_detail extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView txtEmail, txtTenNguoiDung, txtGioiTinh, txtSDT;
    private Button btnChinhSua, btnCapNhat;
    private ImageView profileImage, cameraIcon;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_customer_detail);

        // Ánh xạ giao diện
        txtEmail = findViewById(R.id.txtEmail);
        txtTenNguoiDung = findViewById(R.id.txtTenNguoiDung);
        txtGioiTinh = findViewById(R.id.txtGioiTinh);
        txtSDT = findViewById(R.id.txtSDT);
        profileImage = findViewById(R.id.profileImage);
        cameraIcon = findViewById(R.id.cameraIconUploadProfileImage);

        btnChinhSua = findViewById(R.id.btnChinhSua);
        btnCapNhat = findViewById(R.id.btnCapNhat);

        loadPersonalDetail();

        btnChinhSua.setOnClickListener(view -> {
            setEditingEnabled(true);
            btnCapNhat.setEnabled(true);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Thông báo")
                    .setMessage("Được phép chỉnh sửa")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        btnCapNhat.setOnClickListener(view -> updatePersonalDetail());

        cameraIcon.setOnClickListener(v -> openFileChooser());

    }


    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference("profile_images/" + System.currentTimeMillis() + ".jpg");

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Hiển thị ảnh bằng Glide
                        Glide.with(personal_customer_detail.this)
                                .load(uri)
                                .into(profileImage);

                        Toast.makeText(personal_customer_detail.this, "Upload successful!", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> Toast.makeText(personal_customer_detail.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPersonalDetail() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refPersonalDetail = FirebaseDatabase.getInstance()
                .getReference("Users").child("Customers").child(userId);

        refPersonalDetail.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("tenNguoiDung").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("sdt").getValue(String.class);
                    String gender = snapshot.child("gioiTinh").getValue(String.class);

                    // Hiển thị thông tin
                    txtEmail.setText(email);
                    txtTenNguoiDung.setText(name);
                    txtGioiTinh.setText(gender);
                    txtSDT.setText(phone);

                    // Hiển thị ảnh đại diện nếu có
                    String imageUrl = snapshot.child("profileImage").getValue(String.class);
                    if (imageUrl != null) {
                        Glide.with(personal_customer_detail.this)
                                .load(imageUrl)
                                .into(profileImage);
                    }
                } else {
                    Toast.makeText(personal_customer_detail.this, "Khách hàng không tồn tại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(personal_customer_detail.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePersonalDetail() {
        String tenNguoiDung = txtTenNguoiDung.getText().toString().trim();
        String sdt = txtSDT.getText().toString().trim();
        String gioiTinh = txtGioiTinh.getText().toString().trim();

        if (tenNguoiDung.isEmpty() || sdt.isEmpty() || gioiTinh.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refPersonalDetail = FirebaseDatabase.getInstance()
                .getReference("Users").child("Customers").child(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("tenNguoiDung", tenNguoiDung);
        updates.put("sdt", sdt);
        updates.put("gioiTinh", gioiTinh);

        refPersonalDetail.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    setEditingEnabled(false);
                    btnCapNhat.setEnabled(false);
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setEditingEnabled(boolean enabled) {
        txtTenNguoiDung.setEnabled(enabled);
        txtGioiTinh.setEnabled(enabled);
        txtSDT.setEnabled(enabled);
    }
}