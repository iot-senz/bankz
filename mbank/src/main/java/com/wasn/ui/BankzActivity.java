package com.wasn.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wasn.R;

/**
 * Main activity class of the application
 *
 * @author erangaeb@gmail.com (eranga bandara)
 */
public class BankzActivity extends Activity implements View.OnClickListener {

    // activity components
    RelativeLayout balanceQueryLayout;
    RelativeLayout summaryLayout;
    RelativeLayout settingsLayout;
    RelativeLayout logout;
    TextView logoutText;
    TextView transactionText;
    TextView transactionIcon;
    TextView summaryText;
    TextView summaryIcon;
    TextView settingsText;
    TextView settingsIcon;
    //TextView mbankIcon;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobile_bank_layout);

        init();
    }

    /**
     * Initialize activity components
     */
    public void init() {
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        balanceQueryLayout = (RelativeLayout) findViewById(R.id.transaction_layout);
        summaryLayout = (RelativeLayout) findViewById(R.id.summary_layout);
        settingsLayout = (RelativeLayout) findViewById(R.id.settings_layout);
        logout = (RelativeLayout) findViewById(R.id.mobile_bank_layout_logout);

        // set custom font
        logoutText = (TextView) findViewById(R.id.mobile_bank_layout_logout_text);
        logoutText.setTypeface(typeface, Typeface.BOLD);

        transactionIcon = (TextView) findViewById(R.id.transaction_icon);
        transactionText = (TextView) findViewById(R.id.tranaction_text);
        transactionIcon.setTypeface(typeface, Typeface.BOLD);
        transactionText.setTypeface(typeface, Typeface.BOLD);

        summaryIcon = (TextView) findViewById(R.id.summary_icon);
        summaryText = (TextView) findViewById(R.id.summary_text);
        summaryIcon.setTypeface(typeface, Typeface.BOLD);
        summaryText.setTypeface(typeface, Typeface.BOLD);

        settingsIcon = (TextView) findViewById(R.id.settings_icon);
        settingsText = (TextView) findViewById(R.id.settings_text);
        settingsIcon.setTypeface(typeface, Typeface.BOLD);
        settingsText.setTypeface(typeface, Typeface.BOLD);

        //   mbankIcon = (TextView) findViewById(R.id.mbank_icon);
        //   mbankIcon.setTypeface(typeface, Typeface.BOLD);

        balanceQueryLayout.setOnClickListener(BankzActivity.this);
        summaryLayout.setOnClickListener(BankzActivity.this);
        settingsLayout.setOnClickListener(BankzActivity.this);
        logout.setOnClickListener(BankzActivity.this);
    }

    /**
     * Call when click on view
     *
     * @param view
     */
    public void onClick(View view) {
        if (view == balanceQueryLayout) {
            // display transaction activity
            //startActivity(new Intent(BankzActivity.this, TransactionActivity.class));
            startActivity(new Intent(BankzActivity.this,GetAccountActivity.class));
            //BankzActivity.this.finish();
        } else if (view == summaryLayout) {
            // display transaction list activity
            startActivity(new Intent(BankzActivity.this, TransactionListActivity.class));
            //BankzActivity.this.finish();
        } else if (view == settingsLayout) {
            // display settings activity
            startActivity(new Intent(BankzActivity.this, SettingsActivity.class));
            //BankzActivity.this.finish();
        } else if (view == logout) {
            displayInformationMessageDialog("Are you sure, you want to logout? ");
        }
    }

    /**
     * Display message dialog when user going to logout
     *
     * @param message
     */
    public void displayInformationMessageDialog(String message) {
        final Dialog dialog = new Dialog(BankzActivity.this);

        //set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.share_confirm_message_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        // set dialog texts
        TextView messageHeaderTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_header_text);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_text);
        messageTextView.setText(message);

        // set custom font
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");
        messageHeaderTextView.setTypeface(face);
        messageHeaderTextView.setTypeface(null, Typeface.BOLD);
        messageTextView.setTypeface(face);

        //set ok button
        Button okButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_ok_button);
        okButton.setTypeface(face);
        okButton.setTypeface(null, Typeface.BOLD);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // back to login activity
                //startActivity(new Intent(MobileBankActivity.this, LoginActivity.class));
                BankzActivity.this.finish();
                dialog.cancel();
            }
        });

        // cancel button
        Button cancelButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_cancel_button);
        cancelButton.setTypeface(face);
        cancelButton.setTypeface(null, Typeface.BOLD);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

}