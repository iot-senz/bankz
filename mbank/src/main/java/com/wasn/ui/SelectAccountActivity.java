package com.wasn.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.wasn.R;
import com.wasn.pojos.Account;

import java.util.ArrayList;




public class SelectAccountActivity extends Activity implements View.OnClickListener{
    private static final String TAG = GetAccountActivity.class.getName();

    RelativeLayout back;
    RelativeLayout done;
    Typeface typeface;
    ListView accountListView;
    ArrayList<Account> accountDetails;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_account_layout);
        initUi();
        getActionBar().hide();
    }

    public void onClick(View view){
        if (view == back) {
            SelectAccountActivity.this.finish();
        } else if (view == done) {
            startActivity(new Intent(SelectAccountActivity.this, TransactionActivity.class));
        }

    }

    public void initUi(){
        back = (RelativeLayout) findViewById(R.id.select_account_layout_back);
        done = (RelativeLayout) findViewById(R.id.select_account_layout_done);

        // set custom font to header text
        typeface = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        back.setOnClickListener(SelectAccountActivity.this);
        done.setOnClickListener(SelectAccountActivity.this);


        Intent intent = this.getIntent();
        String accountDetailsString = intent.getStringExtra("accountDetails");

        Log.d(TAG,"The received values are" + accountDetailsString);
        //Add data to a arraylist
        accountDetails = new ArrayList<Account>();
        addAccountList(accountDetailsString);

        SelectAccountListAdapter accountList =  new SelectAccountListAdapter(this,accountDetails);

        // Attach the adapter to a ListView
        accountListView = (ListView) findViewById(R.id.select_account_list);
        accountListView.setAdapter(accountList);

        accountListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {


                // ListView Clicked item value
                Account  account    = (Account) accountListView.getItemAtPosition(position);
                Intent intent2 = new Intent(SelectAccountActivity.this, TransactionActivity.class);
                intent2.putExtra("accountNumber", account.getAccountNumber());
                startActivity(intent2);



            }

        });



/*

       // ListView listView
        accountListView = (ListView) findViewById(R.id.select_account_list);
        String[] values = new String[] { "Android List View",
                "Adapter implementation",
                "Simple List View In Android",
                "Create List View Android",
                "Android Example",
                "List View Source Code",
                "List View Array Adapter",
                "Android Example List View"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, android.R.id.text1, values);
        accountListView.setAdapter(adapter);

        // ListView Item Click Listener
        accountListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) accountListView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                        .show();

            }

        });

        */

    }



    public void addAccountList(String rawString){
        Log.d(TAG,"yahh..... tryign to handle");
        String[] separatedTilde = rawString.split("~");
        String[] separatedHash;
        for (int i=0; i<separatedTilde.length;i++) {
            separatedHash = separatedTilde[i].split("\\|");
            Account p = new Account();
            p.setAccountType(separatedHash[0]);
            Log.d(TAG,"yahh..... "+ separatedHash[0]);
            p.setAccountNumber(separatedHash[1]);
            Log.d(TAG,"yahh..... "+ separatedHash[1]);
            p.setOwnerName(separatedHash[2].replace("_"," "));
            Log.d(TAG,"yahh..... "+ separatedHash[2]);
            p.setCif(separatedHash[3]);
            Log.d(TAG,"yahh..... "+ separatedHash[3]);
            accountDetails.add(p);

        }

    }



}
