package phuocsu.carbookingsystem;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class personal_driver_detail extends AppCompatActivity {
    private TextView txtEmail, txtTenNguoiDung, txtGioiTinh, txtSDT, txtCCCD, txtBienSoXe;
    private Button btnChinhSua, btnCapNhat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_driver_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtEmail = findViewById(R.id.txtEmail);
        txtTenNguoiDung = findViewById(R.id.txtTenNguoiDung);
        txtGioiTinh = findViewById(R.id.txtGioiTinh);
        txtSDT = findViewById(R.id.txtSDT);
        txtCCCD = findViewById(R.id.txtCCCD);
        txtBienSoXe = findViewById(R.id.txtBienSoXe);

        loadPersonalDetail();

        btnChinhSua = findViewById(R.id.btnChinhSua);
        btnChinhSua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEditingEnabled(true);
                btnCapNhat.setEnabled(true);

                //Thông báo được phép cập nhật
                AlertDialog.Builder builder = new AlertDialog.Builder(personal_driver_detail.this);
                builder.setTitle("Thông báo");
                builder.setMessage("Được phép chỉnh sửa");
                builder.setPositiveButton("OK", (dialog,which) -> dialog.dismiss()); //Nút Ok để đóng

                //Hiển thị AlertDialog
                AlertDialog dialog = builder.create();;
                dialog.show();
            }
        });

        btnCapNhat = findViewById(R.id.btnCapNhat);
        btnCapNhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePersonalDetail();
            }
        });
    }

    private void updatePersonalDetail() {
        final String tenNguoiDung = txtTenNguoiDung.getText().toString().trim();
        final String sdt = txtSDT.getText().toString().trim();
        final String gioiTinh = txtGioiTinh.getText().toString().trim();
        final String CCCD = txtCCCD.getText().toString().trim();
        final String bienSoXe = txtBienSoXe.getText().toString().trim();

        if (tenNguoiDung.isEmpty() || sdt.isEmpty() || gioiTinh.isEmpty() || CCCD.isEmpty() || bienSoXe.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo một Map để cập nhật các trường cụ thể
        Map<String, Object> updates = new HashMap<>();
        updates.put("tenNguoiDung", tenNguoiDung);
        updates.put("sdt", sdt);
        updates.put("gioiTinh", gioiTinh);
        updates.put("cccd", CCCD);
        updates.put("bienSoXe", bienSoXe);

        // Cập nhật thông tin lên Firebase Realtime
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refPersonalDetail = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(userId);
        refPersonalDetail.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    setEditingEnabled(false); // Tắt chế độ chỉnh sửa
                    btnCapNhat.setEnabled(false); // Tắt nút cập nhật
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPersonalDetail () {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refPersonalDetail = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(userId);

        refPersonalDetail.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    final String name = snapshot.child("tenNguoiDung").getValue(String.class);
                    final String email = snapshot.child("email").getValue(String.class);
                    final String phone = snapshot.child("sdt").getValue(String.class);
                    final String gender = snapshot.child("gioiTinh").getValue(String.class);
                    final String cccd = snapshot.child("cccd").getValue(String.class);
                    final String bienSoXe = snapshot.child("bienSoXe").getValue(String.class);


                    // Hiển thị thông tin trên giao diện
                    txtEmail.setText(email);
                    txtTenNguoiDung.setText(name);
                    txtGioiTinh.setText(gender);
                    txtSDT.setText(phone);
                    txtCCCD.setText(cccd);
                    txtBienSoXe.setText(bienSoXe);
                } else {
                    Toast.makeText(personal_driver_detail.this, "Khách hàng không tồn tại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(personal_driver_detail.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void setEditingEnabled(boolean enabled){
        txtTenNguoiDung.setEnabled(enabled);
        txtGioiTinh.setEnabled(enabled);
        txtSDT.setEnabled(enabled);
        txtCCCD.setEnabled(enabled);
        txtBienSoXe.setEnabled(enabled);
    }

}