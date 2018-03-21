package com.monte.tangoapp.model;

/**
 * Created by monte on 19/12/2016.
 */
public class SparkFunPostStatus {
    private boolean status;
    private String message;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
