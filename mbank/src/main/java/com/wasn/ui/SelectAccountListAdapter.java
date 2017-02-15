package com.wasn.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wasn.R;
import com.wasn.pojos.Account;

import java.util.ArrayList;

/**
 * Created by senz on 2/14/17.
 */

public class SelectAccountListAdapter extends ArrayAdapter<Account>{

    public SelectAccountListAdapter(Context context, ArrayList<Account> users) {
        super(context, 0, users);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Account account = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.select_account_list_item, parent, false);

        }

        TextView accountNo = (TextView) convertView.findViewById(R.id.account_no);
        TextView ownerName = (TextView) convertView.findViewById(R.id.owner_name);

        accountNo.setText(account.getAccountNumber());
        ownerName.setText(account.getOwnerName());

        return convertView;
    }

}
