package com.thuan.myapp.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.thuan.myapp.R;
import com.thuan.myapp.data.datasource.Callback.AccountOperationCallback;
import com.thuan.myapp.data.datasource.DAO.AccountDAO;
import com.thuan.myapp.data.datasource.Impl.AccountDAOImpl;
import com.thuan.myapp.data.model.Account;

public class AccountDetailActivity extends AppCompatActivity {

    Boolean isEdit = false;
    EditText edtName, edtEmail, edtPassword, edtPhone, edtAddress, edtDob;
    AutoCompleteTextView acGender, acRole;
    Button btnSave;
    Button btnDelete;
    Account account;
    private AccountDAO accountDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtPhone = findViewById(R.id.edtPhoneNumber);
        edtAddress = findViewById(R.id.edtAddress);
        edtDob = findViewById(R.id.edtDob);
        acGender = findViewById(R.id.acGender);
        acRole = findViewById(R.id.acRole);
        btnSave = findViewById(R.id.btnSave);

        // Khởi tạo DAO
        accountDAO = new AccountDAOImpl();
        account = (Account) getIntent().getSerializableExtra("account");
        if (account != null) {
            isEdit = true;
        }
        
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    savedAccount();
            }
        });
//        getSupportActionBar().setTitle(isEdit ? "Edit Account" : "Create Account");
        getSupportActionBar().hide();
        InitAccountData();
    }


    private void savedAccount() {
        // Lấy dữ liệu từ các trường nhập liệu
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();
        String gender = acGender.getText().toString().trim();
        String role = acRole.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in required fields (Name, Email, Password)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo hoặc cập nhật Account
        Account newAccount = new Account(
                address, dob, email,  gender,
                isEdit ? account.getId() : null,
                "122334",
                name, password, phone,  role
        );

        if (isEdit) {
            // Cập nhật tài khoản
            accountDAO.updateAccount(newAccount.getId(), newAccount, new AccountOperationCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AccountDetailActivity.this, "Account updated", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Báo cho activity gọi rằng đã cập nhật
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(AccountDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Tạo tài khoản mới
            accountDAO.createAccount(newAccount, new AccountOperationCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AccountDetailActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Báo cho activity gọi rằng đã tạo
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(AccountDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void InitAccountData() {
        // Dữ liệu cho giới tính
        String[] genders = new String[]{"male", "female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genders
        );
        acGender.setAdapter(genderAdapter);

        // Dữ liệu cho vai trò
        String[] roles = new String[]{"admin", "user"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                roles
        );
        acRole.setAdapter(roleAdapter);

        // Thiết lập số ký tự cần nhập trước khi hiển thị gợi ý
        acGender.setThreshold(1);
        acRole.setThreshold(1);

//        acRole.setText("user");
//        acGender.setText("male");

        // Xử lý khi click vào (tùy chọn)
        acGender.setOnClickListener(v -> acGender.showDropDown());
        acRole.setOnClickListener(v -> acRole.showDropDown());

        if (isEdit){
            btnSave.setText("Edit");
            edtName.setText(account.getName());
            edtEmail.setText(account.getEmail());
            edtPassword.setText(account.getPassword());
            edtPhone.setText(account.getPhoneNumber());
            edtAddress.setText(account.getAddress());
            edtDob.setText(account.getDob());
//            edtGender.setText(account.getGender());
//            edtRole.setText(account.getRole());
        }
        else{
            btnSave.setText("Create");
        }


    }

    private void DeleteAccount() {
        if (!isEdit || account == null) {
            Toast.makeText(this, "No account to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xóa tài khoản
        accountDAO.deleteAccount(account.getId(), new AccountOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AccountDetailActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // Báo cho activity gọi rằng đã xóa
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(AccountDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}