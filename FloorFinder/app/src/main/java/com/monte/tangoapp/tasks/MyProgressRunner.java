package com.monte.tangoapp.tasks;

import android.os.Handler;
import android.widget.ProgressBar;

/**
 * Created by monte on 05/01/2017.
 */
public class MyProgressRunner extends Thread {
    private int status;
    private ProgressBar progressBar;
    private Handler handler = new Handler();
    
    public MyProgressRunner (int status, ProgressBar progressBar){
        this.status = status;
        this.progressBar = progressBar;
    }
    
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (status == 100)
                    status = 0;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(status);
                    }
                });
                Thread.sleep(10);
                status++;
            }
        } catch (InterruptedException e){
        }
    }

    public int getStatus() {
        return status;
    }
}