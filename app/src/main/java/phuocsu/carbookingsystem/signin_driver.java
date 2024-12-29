package phuocsu.carbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signin_driver extends AppCompatActivity {
    private EditText txtTenDangNhap;
    private EditText txtMatKhau;
    private Button btnDangNhap;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signin_driver);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView txtDangKyKhachHang = findViewById(R.id.txtDangKyKhachHang);
        txtDangKyKhachHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signin_driver.this, signup_customer.class);
                startActivity(intent);
            }
        });

        TextView txtDangNhapKhachHang = findViewById(R.id.txtDangNhapKhachHang);
        txtDangNhapKhachHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signin_driver.this, signin_customer.class);
                startActivity(intent);
            }
        });

        TextView txtDangKyTaiXe = findViewById(R.id.txtDangKyTaiXe);
        txtDangKyTaiXe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signin_driver.this, signup_driver.class);
                startActivity(intent);
            }
        });

        //Quên mật khẩu
        TextView txtQuenMatKhau = findViewById(R.id.txtQuenMatKhau);
        txtQuenMatKhau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signin_driver.this, forgot_driver_password.class);
                startActivity(intent);
            }
        });


        txtTenDangNhap = findViewById(R.id.txtTenDangNhap);
        txtMatKhau = findViewById(R.id.txtMatKhau);
        btnDangNhap = findViewById(R.id.btnDangNhap);
        mAuth = FirebaseAuth.getInstance(); // lấy thể hiện

        btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signIn();
//                final String email = txtTenDangNhap.getText().toString();
//                final String password = txtMatKhau.getText().toString();
//
//                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(signin_driver.this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if(!task.isSuccessful()) {
//                            Toast.makeText(signin_driver.this, "Đăng nhập thất bại" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                        else{
//                            Toast.makeText(signin_driver.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
//                            Intent intent = new Intent(signin_driver.this, map_driver_activity.class);
//                            startActivity(intent);
//                        }
//                    }
//                });
            }
        });
    }

    private void signIn(){
        final String email = txtTenDangNhap.getText().toString();
        final String password = txtMatKhau.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(signin_driver.this, "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(signin_driver.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()) {
                    Toast.makeText(signin_driver.this, "Đăng nhập thất bại" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                else{
                    final String email = txtTenDangNhap.getText().toString();
                    final String password = txtMatKhau.getText().toString();

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid(); // Lấy userId
                        checkUserRole(userId); // Gọi hàm kiểm tra vai trò
                    } else {
                        Toast.makeText(signin_driver.this, "Không thể lấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // kiểm tra role
    private void checkUserRole(String userId){
        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);
        refUser.child("Role").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    final String role = task.getResult().getValue(String.class);
                    if("Driver".equals(role)){
                        Toast.makeText(signin_driver.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(signin_driver.this, map_driver_activity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        //Không phải tài xế
                        Toast.makeText(signin_driver.this, "Bạn không có quyền truy cập vào tài khoản tài xế.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(signin_driver.this, "Không thể kiểm tra vai trò người dùng!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}