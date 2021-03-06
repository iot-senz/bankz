package com.wasn.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;
import com.wasn.R;
import com.wasn.db.SenzorsDbSource;
import com.wasn.exceptions.InvalidAccountException;
import com.wasn.exceptions.InvalidInputFieldsException;
import com.wasn.exceptions.InvalidPhoneNoException;
import com.wasn.pojos.BalanceQuery;
import com.wasn.pojos.Transaction;
import com.wasn.utils.ActivityUtils;
import com.wasn.utils.NetworkUtil;
import com.wasn.utils.TransactionUtils;

import java.util.HashMap;

/**
 * Activity class to do new transaction
 *
 * @author erangaeb@gmail.com (eranga bandara)
 */
public class TransactionActivity extends Activity implements View.OnClickListener {

    private static final String TAG = TransactionActivity.class.getName();

    // form components
    private EditText accountEditText;
    private EditText amountEditText;
    private EditText phoneEditText;

    // header
    private RelativeLayout back;
    private RelativeLayout done;
    private TextView headerText;

    // custom font
    private Typeface typeface;

    // use to track registration timeout
    private SenzCountDownTimer senzCountDownTimer;
    private boolean isResponseReceived;

    // service interface
    private ISenzService senzService;
    private boolean isServiceBound;

    // current transaction
    private Transaction transaction;

    // service connection
    private ServiceConnection senzServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("TAG", "Connected with senz service");
            isServiceBound = true;
            senzService = ISenzService.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d("TAG", "Disconnected from senz service");

            senzService = null;
            isServiceBound = false;
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_layout);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        initUi();

        // service
        senzService = null;
        isServiceBound = false;

        // register broadcast receiver
        registerReceiver(senzMessageReceiver, new IntentFilter("com.wasn.bankz.DATA_SENZ"));

        // bind with senz service
        // bind to service from here as well
        if (!isServiceBound) {
            Intent intent = new Intent();
            intent.setClassName("com.wasn", "com.wasn.services.RemoteSenzService");
            bindService(intent, senzServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(senzServiceConnection);
        unregisterReceiver(senzMessageReceiver);
    }

    /**
     * Initialize form components and values
     */
    public void initUi() {
        // init text/edit text fields
        accountEditText = (EditText) findViewById(R.id.transaction_layout_account_text);
        amountEditText = (EditText) findViewById(R.id.transaction_layout_amount_text);
        phoneEditText = (EditText) findViewById(R.id.transaction_layout_phone_text);
        headerText = (TextView) findViewById(R.id.transaction_layout_header_text);

        // set custom font
        accountEditText.setTypeface(typeface, Typeface.BOLD);
        amountEditText.setTypeface(typeface, Typeface.BOLD);
        phoneEditText.setTypeface(typeface, Typeface.BOLD);
        headerText.setTypeface(typeface, Typeface.BOLD);

        back = (RelativeLayout) findViewById(R.id.transaction_layout_back);
        done = (RelativeLayout) findViewById(R.id.transaction_layout_done);

        back.setOnClickListener(TransactionActivity.this);
        done.setOnClickListener(TransactionActivity.this);

        // balance query receives from previous activity
        Intent intent2 = getIntent();
        if (intent2.getExtras() != null) {
            String accountNo = intent2.getStringExtra("accountNumber");
            //BalanceQuery balance = intent.getExtras().getParcelable("balance");
            //accountEditText.setText(balance.getClientAccount(), TextView.BufferType.NORMAL);
            accountEditText.setText(accountNo);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View view) {
        if (view == back) {
            // back to main activity
            TransactionActivity.this.finish();
        } else if (view == done) {
            onClickPut();
        }
    }

    private void onClickPut() {
        ActivityUtils.hideSoftKeyboard(this);

        try {
            String account = accountEditText.getText().toString().trim();
            int amount = Integer.parseInt(amountEditText.getText().toString().trim());
            String phoneNo = phoneEditText.getText().toString().trim();
            ActivityUtils.isValidTransactionFields(account, amount, phoneNo);

            // initialize transaction
            transaction = new Transaction(1, "", "", account, "", amount, TransactionUtils.getCurrentTime(), "", phoneNo);

            //new SenzorsDbSource(TransactionActivity.this).createTransaction(transaction);
            //navigateTransactionDetails(transaction);
            if (NetworkUtil.isAvailableNetwork(this)) {
                displayInformationMessageDialog("Are you sure you want to do the transaction #Account " + transaction.getClientAccountNo() + " #Amount " + transaction.getTransactionAmount() + " For " + transaction.getPhoneNo());
            } else {
                displayMessageDialog("#ERROR", "No network connection");
            }
        } catch (InvalidInputFieldsException | NumberFormatException e) {
            e.printStackTrace();

            displayMessageDialog("#ERROR", "Invalid Account no/Amount/Phone No");
        } catch (InvalidAccountException e) {
            e.printStackTrace();

            displayMessageDialog("#ERROR", "Account no should be 12 characters in length");
        } catch (InvalidPhoneNoException e) {
            e.printStackTrace();

            displayMessageDialog("#ERROR", "Phone no should be 10 characters  in length");
        }

    }

    private Senz getPutSenz() {
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("acc", accountEditText.getText().toString().trim());
        senzAttributes.put("amnt", amountEditText.getText().toString().trim());
        senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
        senzAttributes.put("mobile", phoneEditText.getText().toString().trim());

        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.PUT;
        User receiver = new User("", "sdbltrans");

        return new Senz(id, signature, senzType, null, receiver, senzAttributes);
    }

    private void doPut(Senz senz) {
        try {
            senzService.send(senz);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Keep track with share response timeout
     */
    private class SenzCountDownTimer extends CountDownTimer {

        // timer deals with only one senz
        private Senz senz;

        public SenzCountDownTimer(long millisInFuture, long countDownInterval, final Senz senz) {
            super(millisInFuture, countDownInterval);

            this.senz = senz;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // if response not received yet, resend share
            if (!isResponseReceived) {
                doPut(senz);
                Log.d(TAG, "Response not received yet");
            }
        }

        @Override
        public void onFinish() {
            ActivityUtils.hideSoftKeyboard(TransactionActivity.this);
            ActivityUtils.cancelProgressDialog();

            // display message dialog that we couldn't reach the user
            if (!isResponseReceived) {
                String message = "Seems we couldn't complete the transaction at this moment";
                displayMessageDialog("#PUT Fail", message);
            }
        }
    }

    private BroadcastReceiver senzMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message from Senz service");
            handleMessage(intent);
        }
    };

    /**
     * Handle broadcast message receives
     * Need to handle registration success failure here
     *
     * @param intent intent
     */
    private void handleMessage(Intent intent) {
        String action = intent.getAction();

        if (action.equals("com.wasn.bankz.DATA_SENZ")) {
            Senz senz = intent.getExtras().getParcelable("SENZ");

            if (senz.getAttributes().containsKey("msg")) {
                // msg response received
                ActivityUtils.cancelProgressDialog();
                isResponseReceived = true;
                senzCountDownTimer.cancel();

                String msg = senz.getAttributes().get("msg");
                if (msg != null && msg.equalsIgnoreCase("PUTDONE")) {
                    Toast.makeText(this, "Transaction successful", Toast.LENGTH_LONG).show();

                    // save transaction in db
                    if (transaction != null)
                        new SenzorsDbSource(TransactionActivity.this).createTransaction(transaction);

                    // navigate
                    navigateTransactionDetails(transaction);
                } else {
                    String informationMessage = "Failed to complete the transaction";
                    displayMessageDialog("PUT fail", informationMessage);
                }
            }
        }
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
     * Display message dialog when user going to logout
     *
     * @param message
     */
    public void displayInformationMessageDialog(String message) {
        final Dialog dialog = new Dialog(this);

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
                // do transaction
                dialog.cancel();

                ActivityUtils.showProgressDialog(TransactionActivity.this, "Please wait...");

                // start new timer
                isResponseReceived = false;
                senzCountDownTimer = new SenzCountDownTimer(16000, 5000, getPutSenz());
                senzCountDownTimer.start();
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


    private void navigateTransactionDetails(Transaction transaction) {
        // navigate to transaction details
        Intent intent = new Intent(TransactionActivity.this, TransactionDetailsActivity.class);
        intent.putExtra("transaction", transaction);
        intent.putExtra("ACTIVITY_NAME", TransactionActivity.class.getName());
        startActivity(intent);

        TransactionActivity.this.finish();
    }

}
