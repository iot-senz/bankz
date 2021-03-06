package com.wasn.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wasn.R;
import com.wasn.enums.PrintType;
import com.wasn.exceptions.BluetoothNotAvailableException;
import com.wasn.exceptions.BluetoothNotEnableException;
import com.wasn.listeners.PrintListener;
import com.wasn.pojos.Attribute;
import com.wasn.pojos.Transaction;
import com.wasn.services.printservices.TransactionPrintService;
import com.wasn.utils.PrintUtils;

import java.util.ArrayList;

/**
 * Activity class to display transaction details
 *
 * @author erangaeb@gmail.com (eranga bandara)
 */
public class TransactionDetailsActivity extends Activity implements View.OnClickListener, PrintListener {

    // use to populate list
    ListView transactionDetailsListView;
    ArrayList<Attribute> attributesList;
    AttributeListAdapter adapter;

    // form components
    RelativeLayout back;
    RelativeLayout help;
    RelativeLayout print;
    TextView headerText;

    // display when printing
    public ProgressDialog progressDialog;

    private Transaction transaction;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_details_list_layout);

        init();
    }

    /**
     * Initialize activity components
     */
    public void init() {
        back = (RelativeLayout) findViewById(R.id.transaction_details_layout_back);
        help = (RelativeLayout) findViewById(R.id.transaction_details_layout_help);
        print = (RelativeLayout) findViewById(R.id.transaction_details_layout_print);

        // set custom font for header text
        headerText = (TextView) findViewById(R.id.transaction_details_list_layout_header_text);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");
        headerText.setTypeface(face);
        headerText.setTypeface(null, Typeface.BOLD);

        back.setOnClickListener(TransactionDetailsActivity.this);
        help.setOnClickListener(TransactionDetailsActivity.this);
        print.setOnClickListener(TransactionDetailsActivity.this);

        this.transaction = getIntent().getParcelableExtra("transaction");

        // populate list only have transaction
        if (transaction != null) {
            // fill attribute list from with transaction details
            attributesList = new ArrayList<Attribute>();
            //attributesList.add(new Attribute("Client Name", transaction.getClientName()));
            //attributesList.add(new Attribute("Client NIC", transaction.getClientNic()));
            attributesList.add(new Attribute("Account No", transaction.getClientAccountNo()));
            //attributesList.add(new Attribute("Transaction Type", transaction.getTransactionType()));
            attributesList.add(new Attribute("Amount", Integer.toString(transaction.getTransactionAmount())));
            attributesList.add(new Attribute("Phone No", transaction.getPhoneNo()));
            attributesList.add(new Attribute("Time", transaction.getTransactionTime()));


            // populate list
            transactionDetailsListView = (ListView) findViewById(R.id.transaction_details_list);

            // add header and footer
            View headerView = View.inflate(this, R.layout.header, null);
            View footerView = View.inflate(this, R.layout.footer, null);

            transactionDetailsListView.addHeaderView(headerView);
            transactionDetailsListView.addFooterView(footerView);

            adapter = new AttributeListAdapter(TransactionDetailsActivity.this, attributesList);
            transactionDetailsListView.setAdapter(adapter);
        } else {
            // To-Do display empty view
        }
    }

    /**
     * Display message dialog when user going to logout
     *
     * @param message
     */
    public void displayInformationMessageDialog(String message) {
        final Dialog dialog = new Dialog(TransactionDetailsActivity.this);

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
                // printing event need to handle according to previous activity
                dialog.cancel();
                // print and save transaction in database
                // print two receipts
                try {
                    if (PrintUtils.isEnableBluetooth()) {
                        progressDialog = ProgressDialog.show(TransactionDetailsActivity.this, "", "Printing receipt, Please wait ...");
                        new TransactionPrintService(TransactionDetailsActivity.this, TransactionDetailsActivity.this, transaction, PrintType.PRINT).execute();
                    }
                } catch (BluetoothNotEnableException e) {
                    Toast.makeText(TransactionDetailsActivity.this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
                } catch (BluetoothNotAvailableException e) {
                    Toast.makeText(TransactionDetailsActivity.this, "Bluetooth not available", Toast.LENGTH_LONG).show();
                }
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

    /**
     * Display message dialog
     *
     * @param messageHeader message header
     * @param message       message to be display
     */
    public void displayMessageDialog(String messageHeader, String message) {
        final Dialog dialog = new Dialog(this);

        //set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.information_message_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        // set dialog texts
        TextView messageHeaderTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_header_text);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_text);
        messageHeaderTextView.setText(messageHeader);
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
                dialog.cancel();
            }
        });

        dialog.show();
    }

    /**
     * Close progress dialog
     */
    public void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * Execute after printing task
     *
     * @param status print status
     */
    public void onPostPrint(String status) {
        // close progress dialog
        closeProgressDialog();

        if (status.equals("1")) {
            // clear shared objects
            Toast.makeText(TransactionDetailsActivity.this, "Transaction saved", Toast.LENGTH_LONG).show();

            // need to go back to transaction activity
            TransactionDetailsActivity.this.finish();
        } else if (status.equals("0")) {
            Toast.makeText(TransactionDetailsActivity.this, "Cannot print receipt", Toast.LENGTH_LONG).show();
        } else if (status.equals("-2")) {
            Toast.makeText(TransactionDetailsActivity.this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
        } else if (status.equals("-3")) {
            Toast.makeText(TransactionDetailsActivity.this, "Bluetooth not available", Toast.LENGTH_LONG).show();
        } else if (status.equals("-5")) {
            // invalid bluetooth address
            displayMessageDialog("Error", "Invalid printer address, Please make sure correct printer address in Settings");
        } else {
            // cannot print
            // may be invalid printer address
            displayMessageDialog("Cannot print", "Printer address might be incorrect, Please make sure correct printer address in Settings and printer switched ON");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onClick(View view) {
        if (view == back) {
            TransactionDetailsActivity.this.finish();
        } else if (view == print) {
            displayInformationMessageDialog("Do you wnt to print the receipt? make sure bluetooth is ON");
        }
    }

}
