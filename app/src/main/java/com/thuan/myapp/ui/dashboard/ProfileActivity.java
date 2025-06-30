package com.thuan.myapp.ui.dashboard;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.thuan.myapp.R; // Đảm bảo R được import
import com.thuan.myapp.data.datasource.Callback.AccountOperationCallback;
import com.thuan.myapp.data.datasource.Impl.AccountDAOImpl;
import com.thuan.myapp.data.model.Account;
import com.thuan.myapp.ui.dashboard.BaseActivity;

import java.util.Calendar;

public class ProfileActivity extends BaseActivity {

    private ImageView profileImageView;
    private TextView tvProfileTitle;
    private EditText edtName, edtEmail, edtPassword, edtPhoneNumber, edtAddress, edtDob;
    private AutoCompleteTextView acGender, acRole;
    private Button btnSaveProfile;

    private Account currentAccount;
    private AccountDAOImpl accountDAO;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Áp dụng ngôn ngữ đã lưu trước khi gọi super.onCreate() và setContentView()
        // (Điều này đã được xử lý trong attachBaseContext của BaseActivity, nhưng cần lưu ý thứ tự)
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileImageView = findViewById(R.id.profile_image_view);
        tvProfileTitle = findViewById(R.id.tvProfileTitle);
        edtName = findViewById(R.id.edtName);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtDob = findViewById(R.id.edtDob);
        edtAddress = findViewById(R.id.edtAddress);
        acGender = findViewById(R.id.acGender);
        acRole = findViewById(R.id.acRole);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        accountDAO = new AccountDAOImpl();
        firebaseAuth = FirebaseAuth.getInstance();

        loadAccountDataAndSetupUI();

        // Cập nhật hint và title từ strings.xml
        tvProfileTitle.setText(R.string.profile_info);
        edtName.setHint(R.string.user_name);
        edtPhoneNumber.setHint(R.string.phone_number);
        edtEmail.setHint(R.string.email);
        edtPassword.setHint(R.string.new_password);
        edtDob.setHint(R.string.date_of_birth);
        edtAddress.setHint(R.string.address);
        acGender.setHint(R.string.gender);
        acRole.setHint(R.string.role);
        btnSaveProfile.setText(R.string.save_changes);


        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileChanges();
            }
        });
    }

    private void loadAccountDataAndSetupUI() {
        String jsonAccount = sharedPreferences.getString(KEY_ACCOUNT_JSON, null);
        if (jsonAccount != null) {
            currentAccount = gson.fromJson(jsonAccount, Account.class);

            if (currentAccount != null) {
                edtName.setText(currentAccount.getName());
                edtPhoneNumber.setText(currentAccount.getPhoneNumber());
                edtEmail.setText(currentAccount.getEmail());
                edtDob.setText(currentAccount.getDob());
                edtAddress.setText(currentAccount.getAddress());
                acGender.setText(currentAccount.getGender(), false);
                acRole.setText(currentAccount.getRole(), false);

                String[] genders = new String[]{getString(R.string.male), getString(R.string.female), getString(R.string.other)}; // Cần thêm các chuỗi này vào strings.xml
                ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        genders
                );
                acGender.setAdapter(genderAdapter);
                acGender.setThreshold(1);
                acGender.setOnClickListener(v -> acGender.showDropDown());

                String[] roles = new String[]{"admin", "user"}; // Vai trò thường không cần dịch
                ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        roles
                );
                acRole.setAdapter(roleAdapter);
                acRole.setThreshold(1);

                edtDob.setOnClickListener(v -> {
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            ProfileActivity.this,
                            (view1, selectedYear, selectedMonth, selectedDay) -> {
                                String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                                edtDob.setText(selectedDate);
                            },
                            year, month, day);
                    datePickerDialog.show();
                });

            } else {
                Toast.makeText(this, R.string.loading_account_error, Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(this, R.string.not_logged_in_error, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void saveProfileChanges() {
        if (currentAccount == null) {
            Toast.makeText(this, R.string.no_account_to_save, Toast.LENGTH_SHORT).show();
            return;
        }

        String name = edtName.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        String newPassword = edtPassword.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String gender = acGender.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String role = acRole.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            edtName.setError(getString(R.string.name_empty_error));
            edtName.requestFocus();
            return;
        }
        if (!TextUtils.isEmpty(newPassword) && newPassword.length() < 6) {
            edtPassword.setError(getString(R.string.password_length_error));
            edtPassword.requestFocus();
            return;
        }

        currentAccount.setName(name);
        currentAccount.setPhoneNumber(phoneNumber);
        currentAccount.setDob(dob);
        currentAccount.setAddress(address);
        currentAccount.setGender(gender);

        if (!TextUtils.isEmpty(newPassword)) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                user.updatePassword(newPassword).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, R.string.password_update_success, Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(ProfileActivity.this, getString(R.string.password_update_error, errorMessage), Toast.LENGTH_LONG).show();
                        if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                            Toast.makeText(ProfileActivity.this, R.string.recent_login_required, Toast.LENGTH_LONG).show();
                        }
                    }
                    updateAccountInDatabase(currentAccount);
                });
            } else {
                Toast.makeText(this, R.string.no_user_logged_in, Toast.LENGTH_SHORT).show();
                updateAccountInDatabase(currentAccount);
            }
        } else {
            updateAccountInDatabase(currentAccount);
        }
    }

    private void updateAccountInDatabase(Account accountToUpdate) {
        accountDAO.updateAccount(accountToUpdate.getId(), accountToUpdate, new AccountOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ProfileActivity.this, R.string.profile_saved_success, Toast.LENGTH_SHORT).show();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                String updatedJsonAccount = gson.toJson(accountToUpdate);
                editor.putString(KEY_ACCOUNT_JSON, updatedJsonAccount);
                editor.apply();

                finish();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ProfileActivity.this, getString(R.string.profile_save_error, errorMessage), Toast.LENGTH_LONG).show();
            }
        });
    }
}