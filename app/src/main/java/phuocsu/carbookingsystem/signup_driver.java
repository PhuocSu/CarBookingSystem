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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class signup_driver extends AppCompatActivity {
    private EditText txtEmail;
    private EditText txtPassword;
    private Button btnDangKy2;
    private TextView txtDangNhap;



    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup_driver);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtDangNhap = findViewById(R.id.txtDangNhap2);
        txtDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signup_driver.this, signin_user.class);
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance(); // lấy thể hiện

        //Nếu đăng ký thành công, quay trở lại signin và thông báo thành công
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(signup_driver.this, signin_user.class);
                    startActivity(intent);
                    Toast.makeText(signup_driver.this, "Đăng ký tài khoản tài xế thành công", Toast.LENGTH_SHORT).show();
                }
            }
        };

        txtEmail = findViewById(R.id.txtEmail2);
        txtPassword = findViewById(R.id.txtPassword2);

        btnDangKy2 = findViewById(R.id.btnDangKy2);
        btnDangKy2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = txtEmail.getText().toString();
                final String password = txtPassword.getText().toString();
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(signup_driver.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(signup_driver.this,"Đăng ký thất bại" + Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_SHORT).show();
                        }else{
                            String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(user_id);// tham chiếu cơ sở dữ liệu
                            current_user_db.setValue(true);
                        }
                    }
                });
            }
        });

    }

}