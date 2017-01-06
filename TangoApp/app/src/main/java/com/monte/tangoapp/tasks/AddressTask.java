package com.monte.tangoapp.tasks;

import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;

import com.monte.tangoapp.FloorOffsets;

/**
 * Created by monte on 05/01/2017.
 */
public class AddressTask extends AsyncTask<Double, Void, Address> {
    private Context context;
    private AddressTaskListener taskListener;

    public AddressTask (Context context, AddressTaskListener taskListener){
        super();
        this.context = context;
        this.taskListener = taskListener;
    }

    @Override
    protected Address doInBackground(Double... params) {
        return FloorOffsets.getLocationAddress(this.context, params[0], params[1]);
    }

    @Override
    protected void onPostExecute(Address address) {
        super.onPostExecute(address);
        taskListener.onAddressUpdated(address);
    }
}
