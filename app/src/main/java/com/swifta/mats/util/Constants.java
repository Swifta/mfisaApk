package com.swifta.mats.util;

public class Constants {
    public static final String SERVICE_NAME = "MATS";
    public static final String JOB_IDENTITY = "com.swifta.mats.jobs.identity";
    public static final String JOB_DATA = "com.swifta.mats.jobs.data";
    public static final String JOB_RESPONSE = "com.swifta.mats.jobs.response";
    public static final String API_URL = "http://54.173.157.210:8283/";
    public static final String SERVICE_NOTIFICATION = "com.swifta.mats.service.notification";
    public static final String STORE_USERNAME_KEY = "com.swifta.mats.store.username";
    public static final String STORE_CASHOUT_DATA = "com.swifta.mats.cashoutdata";

    /*
     * below for deposit float constant
     */
    public static final String RECEIVING_DESCRIPTION = "Deposit Float";
    public static final String TRANSACTION_TYPE_ID = "1";
    public static final String TRANSACTION_ID = "-1";
    public static final String TRANSACTION_CHANNEL_ID = "2";
    public static final String TRANSACTION_STATUS_ID = "2";
    public static final String FLOAT_TRANSFER_REQUEST_TOKEN_SUCCESS = "ACCOUNT_TRXN_SET_TO_PENDING";
    public static final String DEALER_OTP_CONFIRMATION = "Dealer OTP Confirmation";
    public static final int OTP_REQUEST = 1;
    public static final int OTP_COMPLETE_REQUEST = 2;
    public static final int CASH_OUT_COMPLETED = 3;
    public static final int CASH_IN_COMPLETED = 4;
    public static final int COMPLETE_CASH_OUT_COMPLETED = 5;
    public static final int UNREGISTERED_CASH_OUT = 6;
    public static final int GET_SERVICE_PROVIDER_DETAILS = 7;
    public static final int PAY_BILL_REQUEST = 8;
    public static final String TMP_DEPOSIT_FLOAT_DATA = "tmp_deposit_float";
    public static final String TRANSACTION_WAS_SUCCESSFUL = "TRANSACTION WAS SUCCESSFUL";
    public static final String PASSWORD_RESET_WAS_SUCCESSFUL = "PASSWORD_RESET_WAS_SUCCESSFUL";
    public static final String CASHOUT_TRANSACTION_WAS_SUCCESSFUL = "Redeem cash-out request save and sent to customer successfully!";
    public static final String PAYBILL_REQUEST_WAS_SUCCESSFUL = "Your request is submitted for processing. Please wait for a status confirmation via SMS, and you can check your ministatement for confirmation";

    /*
     * Vendor IDs for Bill Payment
     */
    public static final String DSTV_VENDOR_ID = "BILL001";
    public static final String GOTV_VENDOR_ID = "BILL002";
    public static final String MTN_VENDOR_ID = "BILL009";
    public static final String GLO_VENDOR_ID = "BILL010";
    public static final String ETISALAT_VENDOR_ID = "BILL011";
    public static final String AIRTEL_VENDOR_ID = "BILL012";
    public static final String VENDOR_ID = "VENDOR_ID";
    public static final String NONE = "none";

    public static final String PREVIOUS_ACTIVITY = "previous_activity";
    public static final String CASH_IN = "cash_in";
    public static final String DEALER_ACCOUNT = "dealer_account";
    public static final String UNREGISTERED_CUSTOMER = "unregistered_customer";
    public static final String UNKNOWN = "UNKNOWN";

}
