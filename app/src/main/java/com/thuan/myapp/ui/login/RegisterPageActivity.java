package com.thuan.myapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thuan.myapp.R;
import com.thuan.myapp.data.datasource.Callback.AccountOperationCallback;
import com.thuan.myapp.data.datasource.Impl.AccountDAOImpl;
import com.thuan.myapp.data.model.Account;

public class RegisterPageActivity extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    Button signUp;
    TextView signIn;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    AccountDAOImpl accountDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        signUp = findViewById(R.id.sign_in);
        signIn = findViewById(R.id.sign_up);
        accountDAO = new AccountDAOImpl();

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterPageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String email = editTextEmail.getText().toString().trim();
//                String password = editTextPassword.getText().toString().trim();
//                if(TextUtils.isEmpty(email)){
//                    Toast.makeText(RegisterPageActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if(TextUtils.isEmpty(password)){
//                    Toast.makeText(RegisterPageActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if(task.isSuccessful()){
//                            Toast.makeText(RegisterPageActivity.this, "Registration successfully!", Toast.LENGTH_SHORT).show();
//                            Intent intent  =new Intent(RegisterPageActivity.this, MainActivity.class);
//                            startActivity(intent);
//                            finish();
//                        }else {
//                            Toast.makeText(RegisterPageActivity.this, "Registration failed! Please try again.", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }

            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(RegisterPageActivity.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterPageActivity.this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.length() < 6){ // Firebase yêu cầu mật khẩu tối thiểu 6 ký tự
                    Toast.makeText(RegisterPageActivity.this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                // Tạo đối tượng Account mới
                                Account newAccount = new Account();
                                newAccount.setEmail(user.getEmail()); // Lấy email từ Firebase Auth
                                newAccount.setPassword(password);
                                newAccount.setName("user"); // Đặt tên mặc định là "user"
                                newAccount.setRole("user"); // Đặt vai trò mặc định là "user"
                                // Các trường khác như address, dob, gender, image, phoneNumber sẽ là null hoặc rỗng mặc định
                                // Bạn có thể thêm giá trị mặc định cho chúng nếu muốn, ví dụ: newAccount.setGender("Other");

                                // Sử dụng AccountDAOImpl để tạo bản ghi user trong Realtime Database
                                Log.d("Account sign up", newAccount.toString());
                                accountDAO.createAccount(newAccount, new AccountOperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(RegisterPageActivity.this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show();
                                        // Chuyển về màn hình đăng nhập
                                        Intent intent  = new Intent(RegisterPageActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Toast.makeText(RegisterPageActivity.this, "Đăng ký thành công nhưng không thể tạo hồ sơ: " + errorMessage, Toast.LENGTH_LONG).show();
                                        // Nếu không thể tạo hồ sơ, có thể bạn muốn xóa tài khoản Firebase Auth vừa tạo
                                        // Hoặc chuyển hướng đến màn hình đăng nhập và để người dùng thử lại
                                        if (user != null) {
                                            user.delete().addOnCompleteListener(deleteTask -> {
                                                if (deleteTask.isSuccessful()) {
                                                    Toast.makeText(RegisterPageActivity.this, "Đã xóa tài khoản Firebase không có hồ sơ.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(RegisterPageActivity.this, "Không thể xóa tài khoản Firebase.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                        Intent intent  = new Intent(RegisterPageActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            } else {
                                Toast.makeText(RegisterPageActivity.this, "Đăng ký thành công nhưng không lấy được người dùng hiện tại.", Toast.LENGTH_SHORT).show();
                                Intent intent  = new Intent(RegisterPageActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                            Toast.makeText(RegisterPageActivity.this, "Đăng ký thất bại! " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}