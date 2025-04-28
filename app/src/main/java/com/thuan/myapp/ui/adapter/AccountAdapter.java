package com.thuan.myapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thuan.myapp.R;
import com.thuan.myapp.data.model.Account;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> implements Filterable {

    private List<Account> mListAccount;
    private List<Account> mListAccountOld;
    private OnAccountClickListener clickListener;

    public interface OnAccountClickListener {
        void onAccountClick(Account account);
    }

    public AccountAdapter(List<Account> mListAccount, OnAccountClickListener clickListener) {
        this.mListAccount = mListAccount;
        this.mListAccountOld = mListAccount;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = mListAccount.get(position);
        if (account == null) {
            return;
        }
        holder.tvName.setText(account.getName());
        holder.tvAddress.setText(account.getEmail());

//        holder.imgAccount.setImageResource(account.getImage());

    }

    @Override
    public int getItemCount() {
        if (mListAccount != null) {
            return mListAccount.size();
        }
        return 0;
    }

    public class AccountViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName, tvAddress;
        private CircleImageView imgAccount;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            imgAccount = itemView.findViewById(R.id.img_account);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onAccountClick(mListAccount.get(position));
                }
            });
        }
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String strSearch = constraint.toString();
                if (strSearch.isEmpty())
                    mListAccount = mListAccountOld;
                else{
                        List<Account> list = new ArrayList<>();
                        for (Account account : mListAccountOld) {
                            if (account.getName().toLowerCase().contains(strSearch.toLowerCase())) {
                                list.add(account);
                            }
                        }
                        mListAccount = list;

                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mListAccount;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mListAccount = (List<Account>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
