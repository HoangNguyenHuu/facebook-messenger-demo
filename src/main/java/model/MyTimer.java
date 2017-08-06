package model;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hoangnh on 05/08/2017.
 */
public class MyTimer {
    private Timer timer;
    private Conversation parent;
    public MyTimer(Conversation parent){
        this.parent = parent;
        setSchedule();
    }

//    public void resetTimer(){
//        timer.cancel();
//        timer.purge();
//        setSchedule();
//    }

    public void setSchedule(){
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("End the session");
                parent = null;
                System.gc();
            }
        };
        timer.schedule(task, 60000);
    }
}
