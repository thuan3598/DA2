package com.thuan.myapp.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson; // Import Gson
import com.thuan.myapp.R;
import com.thuan.myapp.data.datasource.Callback.SingleAccountLoadCallback;
import com.thuan.myapp.data.datasource.Impl.AccountDAOImpl;
import com.thuan.myapp.data.model.Account;
import com.thuan.myapp.ui.home.HomePageActivity;

public class MainActivity extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    Button signIn;
    TextView signUp;
    FirebaseAuth firebaseAuth;
    AccountDAOImpl accountDAO;
    SharedPreferences sharedPreferences;
    Gson gson; // Khai báo Gson

    // Khóa cho SharedPreferences
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ACCOUNT_JSON = "loggedInAccountJson"; // Khóa mới để lưu toàn bộ Account

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        accountDAO = new AccountDAOImpl();
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson(); // Khởi tạo Gson

        // Kiểm tra trạng thái đăng nhập trước đó
        if (sharedPreferences.getBoolean(KEY_LOGGED_IN, false) && firebaseAuth.getCurrentUser() != null) {
            // Nếu đã đăng nhập, chuyển đến HomePageActivity
            Toast.makeText(MainActivity.this, "Đã đăng nhập trước đó, đang chuyển hướng...", Toast.LENGTH_SHORT).show();
            navigateToHomePage();
            return; // Quan trọng: dừng lại không cho code bên dưới chạy
        }

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        signIn = findViewById(R.id.sign_in);
        signUp = findViewById(R.id.sign_up);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterPageActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng nhập Firebase Auth thành công
                            String loggedInEmail = firebaseAuth.getCurrentUser().getEmail();
                            if (loggedInEmail != null) {
                                // Lấy thông tin tài khoản từ Firebase Realtime Database
                                accountDAO.getAccountByEmail(loggedInEmail, new SingleAccountLoadCallback() {
                                    @Override
                                    public void onAccountLoaded(Account account) {
                                        if (account != null) {
                                            // Lưu trạng thái đăng nhập và TOÀN BỘ thông tin tài khoản vào SharedPreferences
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putBoolean(KEY_LOGGED_IN, true);
                                            String jsonAccount = gson.toJson(account); // Chuyển đổi Account thành JSON
                                            editor.putString(KEY_ACCOUNT_JSON, jsonAccount); // Lưu chuỗi JSON
                                            editor.apply();

                                            Log.d("account login", jsonAccount);

                                            Toast.makeText(MainActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                            navigateToHomePage();
                                        } else {
                                            // Không tìm thấy thông tin tài khoản trong Realtime Database
                                            Toast.makeText(MainActivity.this, "Đăng nhập thành công nhưng không tìm thấy thông tin người dùng.", Toast.LENGTH_LONG).show();
                                            firebaseAuth.signOut(); // Đăng xuất để tránh trạng thái không nhất quán
                                            sharedPreferences.edit().clear().apply(); // Xóa thông tin đã lưu
                                        }
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Toast.makeText(MainActivity.this, "Lỗi khi lấy thông tin tài khoản: " + errorMessage, Toast.LENGTH_LONG).show();
                                        firebaseAuth.signOut(); // Đăng xuất để tránh trạng thái không nhất quán
                                        sharedPreferences.edit().clear().apply(); // Xóa thông tin đã lưu
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Đăng nhập thất bại! Vui lòng kiểm tra email và mật khẩu.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void navigateToHomePage() {
        Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}