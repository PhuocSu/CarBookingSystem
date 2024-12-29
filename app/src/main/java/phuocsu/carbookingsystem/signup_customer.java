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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class signup_customer extends AppCompatActivity {
    private EditText txtEmail;
    private EditText txtPassword;
    private EditText txtTenNguoiDung;
    private EditText txtSDT;
    private EditText txtGioiTinh;
    private Button btnDangKy;
    private TextView txtDangNhap;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup_customer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtDangNhap = findViewById(R.id.txtDangNhap);
        txtDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signup_customer.this, signin_driver.class);
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance(); // lấy thể hiện

        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtTenNguoiDung = findViewById(R.id.txtTenNguoiDung);
        txtSDT = findViewById(R.id.txtSDT);
        txtGioiTinh = findViewById(R.id.txtGioiTinh);

        btnDangKy = findViewById(R.id.btnDangKy);
        btnDangKy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = txtEmail.getText().toString();
                final String password = txtPassword.getText().toString();
                final String tenNguoiDung = txtTenNguoiDung.getText().toString();
                final String sdt = txtSDT.getText().toString();
                final String gioiTinh = txtGioiTinh.getText().toString();
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(signup_customer.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(signup_customer.this,"Đăng ký thất bại" +  Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_SHORT).show();
                        }else{
                            String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);// tham chiếu cơ sở dữ liệu

                            //Thêm dữ liệu
                            current_user_db.child("email").setValue(email);
                            current_user_db.child("tenNguoiDung").setValue(tenNguoiDung);
                            current_user_db.child("sdt").setValue(sdt);
                            current_user_db.child("gioiTinh").setValue(gioiTinh);
                            current_user_db.child("Role").setValue("Customer"); //set role

                            Intent intent = new Intent(signup_customer.this, signin_driver.class);
                            startActivity(intent);
                            Toast.makeText(signup_customer.this, "Đăng ký tài khoản khách hàng thành công", Toast.LENGTH_SHORT).show();


//                            current_user_db.setValue(true);
                        }
                    }
                });
            }
        });

    }


}