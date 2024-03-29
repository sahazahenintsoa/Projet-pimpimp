package com.general.files;

import android.os.Handler;
import android.util.Log;

import com.utils.Utils;

/**
 * Created by Admin on 06-07-2016.
 */
public class UpdateFrequentTask implements Runnable {
    int mInterval;
    private Handler mHandler_UpdateTask;

    OnTaskRunCalled onTaskRunCalled;

    boolean isAvoidFirstRun = false;

    public UpdateFrequentTask(int mInterval) {
        this.mInterval = mInterval;
        mHandler_UpdateTask = new Handler();
    }

    public void avoidFirstRun() {
        this.isAvoidFirstRun = true;
    }

    public void changeInterval(int mInterval) {
        this.mInterval = mInterval;
    }

    @Override
    public void run() {
        Utils.printLog("updateFrequentTask", "Run");

        if (isAvoidFirstRun == true) {
            isAvoidFirstRun = false;
            return;
        }
        if (onTaskRunCalled != null) {
            onTaskRunCalled.onTaskRun();
        }

        mHandler_UpdateTask.postDelayed(this, mInterval);
    }

    public void startRepeatingTask() {
        this.run();
    }

    public void stopRepeatingTask() {
        Utils.printLog("Stopp", "yaaaa");
        mHandler_UpdateTask.removeCallbacks(this);
    }

    public interface OnTaskRunCalled {
        void onTaskRun();
    }

    public void setTaskRunListener(OnTaskRunCalled onTaskRunCalled) {
        this.onTaskRunCalled = onTaskRunCalled;
    }
}
