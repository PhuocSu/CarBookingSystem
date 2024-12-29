package phuocsu.carbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forgot_driver_password extends AppCompatActivity {
    private EditText txtEmailInput;
    private Button btnResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_driver_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtEmailInput = findViewById(R.id.txtEmailInput);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = txtEmailInput.getText().toString().trim();
                if(email.isEmpty()){
                    Toast.makeText(forgot_driver_password.this, "Vui lòng nhập email của bạn!", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    sendPasswordResetEmail(email);
                    Intent intent = new Intent(forgot_driver_password.this, signin_driver.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        FirebaseAuth authsendPasswordResetEmail = FirebaseAuth.getInstance();
        authsendPasswordResetEmail.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("ForgotPassword", "Email sent.");
                    Toast.makeText(forgot_driver_password.this, "Đã gửi email khôi phục mật khẩu. Kiểm tra hộp thư của bạn!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.e("ForgotPassword", "Error: " + task.getException().getMessage());
                    Toast.makeText(forgot_driver_password.this, "Lỗi khi gửi email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}