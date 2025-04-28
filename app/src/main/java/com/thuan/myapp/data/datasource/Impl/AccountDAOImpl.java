package com.thuan.myapp.data.datasource.Impl;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thuan.myapp.data.datasource.Callback.AccountLoadCallback;
import com.thuan.myapp.data.datasource.Callback.AccountOperationCallback;
import com.thuan.myapp.data.datasource.DAO.AccountDAO;
import com.thuan.myapp.data.model.Account;

import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements AccountDAO {
    private DatabaseReference accountsRef;

    public AccountDAOImpl() {
        accountsRef = FirebaseDatabase.getInstance().getReference("accounts");
    }

    @Override
    public void loadListAccount(AccountLoadCallback callback) {
        List<Account> list = new ArrayList<>();

        accountsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Account account = child.getValue(Account.class);
                    if (account != null) {
                        account.setId(child.getKey()); // Gán key của node làm ID
                        list.add(account);
                        Log.d("AccountDAO", account.toString());
                    }
                }
                callback.onAccountsLoaded(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Failed to load accounts: " + error.getMessage());
            }
        });
    }

    @Override
    public void createAccount(Account account, AccountOperationCallback callback) {
        String id = accountsRef.push().getKey(); // Tạo ID mới
        if (id != null) {
            account.setId(id);
            accountsRef.child(id).setValue(account)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("AccountDAO", "Account created: " + id);
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AccountDAO", "Create failed: " + e.getMessage());
                        callback.onError("Failed to create account: " + e.getMessage());
                    });
        } else {
            callback.onError("Failed to generate account ID");
        }
    }

    @Override
    public void updateAccount(String id, Account account, AccountOperationCallback callback) {
        account.setId(id);
        accountsRef.child(id).setValue(account)
                .addOnSuccessListener(aVoid -> {
                    Log.d("AccountDAO", "Account updated: " + id);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("AccountDAO", "Update failed: " + e.getMessage());
                    callback.onError("Failed to update account: " + e.getMessage());
                });
    }

    @Override
    public void deleteAccount(String id, AccountOperationCallback callback) {
        accountsRef.child(id).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d("AccountDAO", "Account deleted: " + id);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("AccountDAO", "Delete failed: " + e.getMessage());
                    callback.onError("Failed to delete account: " + e.getMessage());
                });
    }
}
