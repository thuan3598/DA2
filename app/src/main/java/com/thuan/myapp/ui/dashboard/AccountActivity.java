package com.thuan.myapp.ui.dashboard;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thuan.myapp.R;
import com.thuan.myapp.data.datasource.Callback.AccountLoadCallback;
import com.thuan.myapp.data.datasource.DAO.AccountDAO;
import com.thuan.myapp.data.datasource.Impl.AccountDAOImpl;
import com.thuan.myapp.data.model.Account;
import com.thuan.myapp.data.model.Construction;
import com.thuan.myapp.ui.adapter.AccountAdapter;
import com.thuan.myapp.ui.home.HomePageActivity;

import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends AppCompatActivity {

    private RecyclerView recvAccount;
    private AccountAdapter accountAdapter;
    private SearchView searchView;
    private List<Account> listAccount;
    private AccountDAO accountDAO;
    private ActivityResultLauncher<Intent> accountDetailLauncher;

    FloatingActionButton btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getSupportActionBar().setTitle("All Accounts");

        // Khởi tạo RecyclerView và Adapter
        recvAccount = findViewById(R.id.recvAccount);
        btnAdd = findViewById(R.id.btnAdd);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recvAccount.setLayoutManager(linearLayoutManager);

        // Khởi tạo listAccount và adapter với callback click
        listAccount = new ArrayList<>();
        accountAdapter = new AccountAdapter(listAccount, account -> {

            Intent intent = new Intent(AccountActivity.this, AccountDetailActivity.class);
            intent.putExtra("account", account);
            accountDetailLauncher.launch(intent);

//            // Xử lý khi nhấn vào item
//            Log.d("AccountClick", "Clicked Account ID: " + account.getId());
//            Toast.makeText(AccountActivity.this, "Account ID: " + account.getId(), Toast.LENGTH_SHORT).show();
        });
        recvAccount.setAdapter(accountAdapter);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recvAccount.addItemDecoration(itemDecoration);

        // Khởi tạo DAO và tải danh sách tài khoản
        accountDAO = new AccountDAOImpl();
        // Khởi tạo ActivityResultLauncher
        accountDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadAccounts(); // Tải lại dữ liệu khi AccountDetailActivity báo thành công
                    }
                }
        );
        loadAccounts();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountActivity.this, AccountDetailActivity.class);
                accountDetailLauncher.launch(intent);
            }
        });
    }

    private void loadAccounts() {
        accountDAO.loadListAccount(new AccountLoadCallback() {
            @Override
            public void onAccountsLoaded(List<Account> accounts) {
                listAccount.clear();
                listAccount.addAll(accounts);
                accountAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(AccountActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                accountAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                accountAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }
}