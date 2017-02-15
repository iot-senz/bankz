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
import com.wasn.pojos.Account;
import com.wasn.R;

import com.wasn.exceptions.InvalidIDNumberException;

import com.wasn.utils.ActivityUtils;
import com.wasn.utils.NetworkUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by senz on 1/19/17.
 */

public class GetAccountActivity extends Activity implements View.OnClickListener {

    private static final String TAG = GetAccountActivity.class.getName();



    private EditText idEditText;
    private RelativeLayout back;
    private RelativeLayout done;
    private TextView headerText;

    // custom font
    private Typeface typeface;

    // use to track registration timeout
    private GetAccountActivity.SenzCountDownTimer senzCountDownTimer;
    private boolean isResponseReceived;

    // service interface
    private ISenzService senzService;
    private boolean isServiceBound;


    private ArrayList<Account> accountDetails;

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



    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_account_layout);
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



    private BroadcastReceiver senzMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"laksithsssssssssssssssssssssssssssssssssssssssssssssssssssssss");
            Log.d(TAG, "Got message from Senz service");
            handleMessage(intent);
        }
    };

    private void doPut(Senz senz) {
        try {
            senzService.send(senz);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onClick(View view){
        if (view == back){
            //back to getAccount Activity
            GetAccountActivity.this.finish();
        }
        else if (view == done) {
            // 1 go to select account activity on success
            sendIDNumber();

        }
    }

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

            if (senz.getAttributes().containsKey("msg") ) {
                // msg response received
                ActivityUtils.cancelProgressDialog();
                isResponseReceived = true;
                senzCountDownTimer.cancel();
                String msg = senz.getAttributes().get("msg");

                String accounts = senz.getAttributes().get("accounts");

                //String accounts = "#aaa#sss#ffff#gggg~#qqq#www#eee#rrr~#yyy#uuu#iii#oooo";
                if (msg != null && msg.equalsIgnoreCase("PUTDONE") && accounts != null) {
                    Toast.makeText(this, "Accounts fetching successful", Toast.LENGTH_LONG).show();
                    // stores the accounts in to the account details array and saves it


                    navigateSelectAccount(accounts);
                } else {
                    String informationMessage = "Failed to complete the Account fetch";
                    displayMessageDialog("PUT fail", informationMessage);
                }

            }
        }

    }

    private Senz getPutSenz() {
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("idno", idEditText.getText().toString().trim());
        senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());


        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.PUT;
        User receiver = new User("", "sdbltrans");

        return new Senz(id, signature, senzType, null, receiver, senzAttributes);
    }

    public void initUi(){
        idEditText = (EditText) findViewById(R.id.get_account_layout_id_number_text);
        back = (RelativeLayout) findViewById(R.id.get_account_layout_back);
        done = (RelativeLayout) findViewById(R.id.get_account_layout_done);
        headerText = (TextView) findViewById(R.id.get_account_layout_header_text);

        //set Custom font
        idEditText.setTypeface(typeface,Typeface.BOLD);
        headerText.setTypeface(typeface,Typeface.BOLD);


        back.setOnClickListener(GetAccountActivity.this);
        done.setOnClickListener(GetAccountActivity.this);

    }


    public void sendIDNumber(){
        ActivityUtils.hideSoftKeyboard(this);
        try{
            String idNumber = idEditText.getText().toString().trim();
            ActivityUtils.isValidIDNumber(idNumber);
            idNumber.toUpperCase();


            if (NetworkUtil.isAvailableNetwork(this)) {
                displayInformationMessageDialog("Are you sure you want to fetch Accounts for ID Number " + idNumber);
            } else {
                displayMessageDialog("#ERROR", "No network connection");
            }

        }

        catch (InvalidIDNumberException e){
            e.printStackTrace();
            //displayMessageDialog();
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
            ActivityUtils.hideSoftKeyboard(GetAccountActivity.this);
            ActivityUtils.cancelProgressDialog();

            // display message dialog that we couldn't reach the user
            if (!isResponseReceived) {
                String message = "Seems we couldn't get Account details at this moment";
                displayMessageDialog("#PUT Fail", message);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(senzServiceConnection);
        unregisterReceiver(senzMessageReceiver);
    }

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

                ActivityUtils.showProgressDialog(GetAccountActivity.this, "Please wait...");

                // start new timer
                isResponseReceived = false;
                senzCountDownTimer = new GetAccountActivity.SenzCountDownTimer(16000, 5000, getPutSenz());
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

    private void navigateSelectAccount(String accountDetails) {
        // navigate to select Account

        Intent intent = new Intent(GetAccountActivity.this, SelectAccountActivity.class);
        intent.putExtra("accountDetails", accountDetails);
        //intent.putExtra("idno", )
        intent.putExtra("ACTIVITY_NAME", GetAccountActivity.class.getName());
        startActivity(intent);

        GetAccountActivity.this.finish();
    }


    /*
    public ArrayList<Account> addAccountList(String rawString){

        ArrayList<Account> accountsList = new ArrayList<>();
        String[] separatedTilde = rawString.split("~");
        String[] separatedHash;
        for (int i=0; i<separatedTilde.length;i++) {
            separatedHash = separatedTilde[i].split("#");
            Account p = new Account();
            p.setAccountType(separatedHash[1]);
            p.setAccountNumber(separatedHash[2]);
            p.setOwnerName(separatedHash[3]);
            p.setCif(separatedHash[4]);
            accountsList.add(p);

        }
        Log.d(TAG,"yahh..... tryign to handle");
        return accountsList;
    }*/


}


