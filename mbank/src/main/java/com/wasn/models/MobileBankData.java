package com.wasn.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.wasn.pojos.Transaction;

import java.util.ArrayList;

/**
 * Database class of mobile bank
 *
 * @author erangaeb@gmail.com (eranga bandara)
 */
public class MobileBankData {

    Context context;

    // DBHelper object,
    // DBHelper using only in this class
    DBHelper dbHelper;

    /**
     * Initialize global fields and DBHelper
     */
    public MobileBankData(Context context) {
        this.context = context;
        dbHelper = new DBHelper();
    }

    /**
     * Clean all
     */
    public void close() {
        //close DB helper
        dbHelper.close();
    }

    /**
     * helper class for create,open and upgrade database
     */
    private class DBHelper extends SQLiteOpenHelper {

        // constants
        public static final String DB_NAME = "mobile_bank_db";
        public static final String TABLE_NAME_CLIENT = "client";
        public static final String TABLE_NAME_TRANSACTION = "raw_transaction";
        public static final String TABLE_NAME_APP_DATA = "app_data";

        public static final int DB_VERSION = 6;

        public DBHelper() {
            super(context, DB_NAME, null, DB_VERSION);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            // create table for client
            String sqlClient = "create table client (id TEXT, " +
                    "name TEXT, " +
                    "nic TEXT, " +
                    "birth_date TEXT, " +
                    "account_no TEXT PRIMARY KEY, " +
                    "balance_amount TEXT, " +
                    "previous_transaction TEXT)";
            db.execSQL(sqlClient);

            // create table for transaction
            String sqlTransaction = "create table raw_transaction (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "branch_id TEXT, " +
                    "client_id TEXT, " +
                    "client_name TEXT, " +
                    "client_nic TEXT, " +
                    "account_no TEXT, " +
                    "previous_balance TEXT, " +
                    "transaction_amount TEXT, " +
                    "current_balance TEXT, " +
                    "transaction_time TEXT, " +
                    "transaction_type TEXT, " +
                    "check_no TEXT, " +
                    "description TEXT, " +
                    "receipt_id TEXT UNIQUE, " +
                    "synced_state TEXT)";
            db.execSQL(sqlTransaction);

            //create table for store application attributes
            String sqlApplicationData = "create table app_data (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "attribute_name TEXT, " +
                    "attribute_value TEXT )";
            db.execSQL(sqlApplicationData);

            // add initial application data to DB
            // keep track with user login state
            ContentValues loginValues = new ContentValues();
            loginValues.put("attribute_name", "isLogged");
            loginValues.put("attribute_value", "0");

            // keep track with client data download state
            ContentValues downloadValues = new ContentValues();
            downloadValues.put("attribute_name", "isDownloaded");
            downloadValues.put("attribute_value", "0");

            // user branch id
            ContentValues branchValues = new ContentValues();
            branchValues.put("attribute_name", "branchId");
            branchValues.put("attribute_value", "0");

            // printing receipt no
            ContentValues receiptValues = new ContentValues();
            receiptValues.put("attribute_name", "receiptNo");
            receiptValues.put("attribute_value", "0");

            // printer bluetooth address
            // need to use when printing
            ContentValues printerAddressValues = new ContentValues();
            printerAddressValues.put("attribute_name", "printerAddress");
            printerAddressValues.put("attribute_value", "");

            // branch telephone
            // need to insert in receipt
            // default value is head office
            ContentValues telephoneNoValues = new ContentValues();
            telephoneNoValues.put("attribute_name", "telephoneNo");
            telephoneNoValues.put("attribute_value", "011 2393759");

            // branch name
            // need to insert in receipt
            ContentValues branchNameValues = new ContentValues();
            branchNameValues.put("attribute_name", "branchName");
            branchNameValues.put("attribute_value", "Colombo 02");

            //insert application data to app_data table
            try {
                //insert fail throw exception
                db.insertOrThrow(DBHelper.TABLE_NAME_APP_DATA, null, loginValues);
                db.insertOrThrow(DBHelper.TABLE_NAME_APP_DATA, null, downloadValues);
                db.insertOrThrow(DBHelper.TABLE_NAME_APP_DATA, null, branchValues);
                db.insertOrThrow(DBHelper.TABLE_NAME_APP_DATA, null, receiptValues);
                db.insertOrThrow(DBHelper.TABLE_NAME_APP_DATA, null, printerAddressValues);
                db.insertOrThrow(DBHelper.TABLE_NAME_APP_DATA, null, telephoneNoValues);
                db.insertOrThrow(DBHelper.TABLE_NAME_APP_DATA, null, branchNameValues);
            } catch (Exception e) {
                System.out.println("database onCreateFails " + e);
            }
        }

        /**
         * {@inheritDoc}
         * call when upgrade the database, normally changing the DB_VERSION
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //drop tables
            db.execSQL("drop table if exists " + TABLE_NAME_CLIENT);
            db.execSQL("drop table if exists " + TABLE_NAME_TRANSACTION);
            db.execSQL("drop table if exists " + TABLE_NAME_APP_DATA);

            //create databases again
            this.onCreate(db);
        }
    }

    /**
     * get isLogged attribute from app_data table
     *
     * @return loginState - login state with server
     */
    public String getLoginState() {
        String loginState = "0";

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // create cursor for get app data   ( table name		     columns   		select		 	 	select args  		 groupby   having  orderby )
        Cursor appDataCursor = db.query(DBHelper.TABLE_NAME_APP_DATA, null, "attribute_name=?", new String[]{"isLogged"}, null, null, null);

        //has elements in cursor
        if (appDataCursor.moveToNext()) {
            appDataCursor.moveToLast();
            loginState = appDataCursor.getString(2);
        }

        appDataCursor.close();
        db.close();

        return loginState;
    }

    /**
     * update isLogged attribute in app_data
     *
     * @param loginState determine logged in or not (1 or 0)
     */
    public void setLoginState(String loginState) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //create content values
        ContentValues appDataValues = new ContentValues();
        appDataValues.put("attribute_value", loginState);

        //update application data
        db.update(DBHelper.TABLE_NAME_APP_DATA, appDataValues, "attribute_name=?", new String[]{"isLogged"});

        db.close();
    }

    /**
     * get data download state attribute from app_data table
     *
     * @return downloadState login state with server
     */
    public String getDownloadState() {
        String downloadState = "0";

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // create cursor for get app data   ( table name		     columns   		select		 	 	select args  		    groupby   having  orderby )
        Cursor appDataCursor = db.query(DBHelper.TABLE_NAME_APP_DATA, null, "attribute_name=?", new String[]{"isDownloaded"}, null, null, null);

        //has elements in cursor
        if (appDataCursor.moveToNext()) {
            appDataCursor.moveToLast();
            downloadState = appDataCursor.getString(2);
        }

        appDataCursor.close();
        db.close();

        return downloadState;
    }

    /**
     * update data download state attribute in app_data
     *
     * @param downloadState determine data downloaded in or not (1 or 0)
     */
    public void setDownloadState(String downloadState) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //create content values
        ContentValues appDataValues = new ContentValues();
        appDataValues.put("attribute_value", downloadState);

        //update application data
        db.update(DBHelper.TABLE_NAME_APP_DATA, appDataValues, "attribute_name=?", new String[]{"isDownloaded"});

        db.close();
    }

    /**
     * get branch id attribute from app_data table
     *
     * @return branch id
     */
    public String getBranchId() {
        String branchId = "0";

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // create cursor for get app data   ( table name		     columns   		select		 	 	select args  		  groupby   having  orderby )
        Cursor appDataCursor = db.query(DBHelper.TABLE_NAME_APP_DATA, null, "attribute_name=?", new String[]{"branchId"}, null, null, null);

        //has elements in cursor
        if (appDataCursor.moveToNext()) {
            appDataCursor.moveToLast();
            branchId = appDataCursor.getString(2);
        }

        appDataCursor.close();
        db.close();

        return branchId;
    }

    /**
     * update branchId attribute in app_date table
     *
     * @param branchId branch id
     */
    public void setBranchId(String branchId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //create content values
        ContentValues appDataValues = new ContentValues();
        appDataValues.put("attribute_value", branchId);

        //update application data
        db.update(DBHelper.TABLE_NAME_APP_DATA, appDataValues, "attribute_name=?", new String[]{"branchId"});

        db.close();
    }

    /**
     * get receiptNo attribute from app_data table
     *
     * @return last receipt no
     */
    public String getReceiptNo() {
        String receiptNo = "0";

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // create cursor for get app data   ( table name		     columns   		select		 	 	select args  		  groupby   having  orderby )
        Cursor appDataCursor = db.query(DBHelper.TABLE_NAME_APP_DATA, null, "attribute_name=?", new String[]{"receiptNo"}, null, null, null);

        //has elements in cursor
        if (appDataCursor.moveToNext()) {
            appDataCursor.moveToLast();
            receiptNo = appDataCursor.getString(2);
        }

        appDataCursor.close();
        db.close();

        return receiptNo;
    }

    /**
     * update receiptNo attribute in app_data table
     *
     * @param receiptNo
     */
    public void setReceiptNo(String receiptNo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //create content values
        ContentValues appDataValues = new ContentValues();
        appDataValues.put("attribute_value", receiptNo);

        //update application data
        db.update(DBHelper.TABLE_NAME_APP_DATA, appDataValues, "attribute_name=?", new String[]{"receiptNo"});

        db.close();
    }

    /**
     * Get printer bluetooth URL
     *
     * @return printer bluetooth address
     */
    public String getPrinterAddress() {
        String printerAddress = "";

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // create cursor for get app data   ( table name		     columns   		select		 	 	select args  		  groupby   having  orderby )
        Cursor appDataCursor = db.query(DBHelper.TABLE_NAME_APP_DATA, null, "attribute_name=?", new String[]{"printerAddress"}, null, null, null);

        //has elements in cursor
        if (appDataCursor.moveToNext()) {
            appDataCursor.moveToLast();
            printerAddress = appDataCursor.getString(2);
        }

        appDataCursor.close();
        db.close();

        return printerAddress;
    }

    /**
     * Set printer address
     *
     * @param printerAddress bluetooth url
     */
    public void setPrinterAddress(String printerAddress) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //create content values
        ContentValues appDataValues = new ContentValues();
        appDataValues.put("attribute_value", printerAddress);

        //update application data
        db.update(DBHelper.TABLE_NAME_APP_DATA, appDataValues, "attribute_name=?", new String[]{"printerAddress"});

        db.close();
    }

    /**
     * Get branch telephone no
     *
     * @return telephone no
     */
    public String getTelephoneNo() {
        String telephoneNo = "";

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // create cursor for get app data   ( table name		     columns   		select		 	 	select args  		  groupby   having  orderby )
        Cursor appDataCursor = db.query(DBHelper.TABLE_NAME_APP_DATA, null, "attribute_name=?", new String[]{"telephoneNo"}, null, null, null);

        //has elements in cursor
        if (appDataCursor.moveToNext()) {
            appDataCursor.moveToLast();
            telephoneNo = appDataCursor.getString(2);
        }

        appDataCursor.close();
        db.close();

        return telephoneNo;
    }

    /**
     * Set branch telephone noe
     *
     * @param telephoneNo
     */
    public void setTelephoneNo(String telephoneNo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //create content values
        ContentValues appDataValues = new ContentValues();
        appDataValues.put("attribute_value", telephoneNo);

        //update application data
        db.update(DBHelper.TABLE_NAME_APP_DATA, appDataValues, "attribute_name=?", new String[]{"telephoneNo"});

        db.close();
    }

    /**
     * Get current branch name
     *
     * @return branch name
     */
    public String getBranchName() {
        String branchName = "";

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // create cursor for get app data   ( table name		     columns   		select		 	 	select args  		  groupby   having  orderby )
        Cursor appDataCursor = db.query(DBHelper.TABLE_NAME_APP_DATA, null, "attribute_name=?", new String[]{"branchName"}, null, null, null);

        //has elements in cursor
        if (appDataCursor.moveToNext()) {
            appDataCursor.moveToLast();
            branchName = appDataCursor.getString(2);
        }

        appDataCursor.close();
        db.close();

        return branchName;
    }

    /**
     * Set branch name
     *
     * @param branchName
     */
    public void setBranchName(String branchName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //create content values
        ContentValues appDataValues = new ContentValues();
        appDataValues.put("attribute_value", branchName);

        //update application data
        db.update(DBHelper.TABLE_NAME_APP_DATA, appDataValues, "attribute_name=?", new String[]{"branchName"});

        db.close();
    }

    /**
     * update account balance of corresponding client
     *
     * @param accountNo clients account no
     * @param balance   balance amount
     */
    public void updateBalanceAmount(String accountNo, String balance) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //create content values
        ContentValues clientValues = new ContentValues();
        clientValues.put("balance_amount", balance);

        //update balance amount data
        db.update(DBHelper.TABLE_NAME_CLIENT, clientValues, "account_no=?", new String[]{accountNo});

        db.close();
    }

    /**
     * update previous transaction amount of corresponding client
     *
     * @param accountNo client account no
     * @param amount    transaction amount
     */
    public void updatePreviousTransactionAmount(String accountNo, String amount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // create content values
        ContentValues clientValues = new ContentValues();
        clientValues.put("previous_transaction", amount);

        // update previous transaction amount
        db.update(DBHelper.TABLE_NAME_CLIENT, clientValues, "account_no=?", new String[]{accountNo});

        db.close();
    }

    /**
     * Get all transactions that match for given branch
     *
     * @param branch current branch
     */
    public ArrayList<Transaction> getAllTransactions(String branch) {
        ArrayList<Transaction> transactionList = new ArrayList<Transaction>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // create a cursor to get transaction data
        Cursor transactionCursor = db.query(DBHelper.TABLE_NAME_TRANSACTION, null, "branch_id=?", new String[]{branch}, null, null, null);

        // read all
        while (transactionCursor.moveToNext()) {
            // get transaction attributes
            int id = transactionCursor.getInt(0);
            String clientName = transactionCursor.getString(3);
            String clientNic = transactionCursor.getString(4);
            String accountNo = transactionCursor.getString(5);
            String previousBalance = transactionCursor.getString(6);
            String transactionAmount = transactionCursor.getString(7);
            String currentBalance = transactionCursor.getString(8);
            String transactionTime = transactionCursor.getString(9);
            String transactionType = transactionCursor.getString(10);

            Transaction transaction = new Transaction(id,
                    clientName,
                    clientNic,
                    accountNo,
                    previousBalance,
                    transactionAmount,
                    currentBalance,
                    transactionTime,
                    transactionType);

            transactionList.add(transaction);
        }

        transactionCursor.close();
        db.close();

        return transactionList;
    }

    /**
     * Insert transaction into database
     *
     * @param transaction
     * @throws SQLiteException
     */
    public void insertTransaction(Transaction transaction) throws android.database.SQLException {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //create content values for transaction record
        ContentValues transactionValues = new ContentValues();
        transactionValues.put("client_name", transaction.getClinetName());
        transactionValues.put("client_nic", transaction.getClinetNic());
        transactionValues.put("account_no", transaction.getClientAccountNo());
        transactionValues.put("previous_balance", transaction.getPreviousBalance());
        transactionValues.put("transaction_amount", transaction.getTransactionAmount());
        transactionValues.put("current_balance", transaction.getCurrentBalance());
        transactionValues.put("transaction_time", transaction.getTransactionTime());
        transactionValues.put("transaction_type", transaction.getTransactionType());

        // throws exception if inset fails
        db.insertOrThrow(DBHelper.TABLE_NAME_TRANSACTION, null, transactionValues);

        db.close();
    }

    /**
     * update synced state of a transaction
     * set synced_state to 1
     */
    public void updateTransactionSyncState(ArrayList<Transaction> unSyncedTransactionList) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //create content values
        ContentValues updateValues = new ContentValues();
        updateValues.put("synced_state", "1");

        for (int i = 0; i < unSyncedTransactionList.size(); i++) {
            //update sync state
            db.update(DBHelper.TABLE_NAME_TRANSACTION, updateValues, "account_no=?", new String[]{unSyncedTransactionList.get(i).getClientAccountNo()});
        }

        db.close();
    }

    /**
     * Delete all transactions that match for given branch
     * delete after day end
     *
     * @param branch current branch
     */
    public void deleteAllTransaction(String branch) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //delete records
        db.delete(DBHelper.TABLE_NAME_TRANSACTION, "branch_id=?", new String[]{branch});

        db.close();
    }

}
