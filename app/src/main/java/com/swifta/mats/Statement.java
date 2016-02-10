package com.swifta.mats;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by moyinoluwa on 2/10/16.
 */
public class Statement implements Parcelable {
    private String transactionTypeValue;
    private String dateValue;
    private int amountValue;
    private String receiverValue;
    private String statusValue;

    public Statement(String transactionTypeValue, String dateValue, int amountValue, String receiverValue, String statusValue) {
        this.transactionTypeValue = transactionTypeValue;
        this.dateValue = dateValue;
        this.amountValue = amountValue;
        this.receiverValue = receiverValue;
        this.statusValue = statusValue;
    }

    protected Statement(Parcel in) {
        transactionTypeValue = in.readString();
        dateValue = in.readString();
        amountValue = in.readInt();
        receiverValue = in.readString();
        statusValue = in.readString();
    }

    public String getTransactionTypeValue() {
        return this.transactionTypeValue;
    }

    public String getDateValue() {
        return this.dateValue;
    }

    public int getAmountValue() {
        return this.amountValue;
    }

    public String getReceiverValue() {
        return this.receiverValue;
    }


    public String getStatusValue() {
        return this.statusValue;
    }

    public static final Creator<Statement> CREATOR = new Creator<Statement>() {
        @Override
        public Statement createFromParcel(Parcel in) {
            return new Statement(in);
        }

        @Override
        public Statement[] newArray(int size) {
            return new Statement[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(transactionTypeValue);
        dest.writeString(dateValue);
        dest.writeInt(amountValue);
        dest.writeString(receiverValue);
        dest.writeString(statusValue);
    }
}
