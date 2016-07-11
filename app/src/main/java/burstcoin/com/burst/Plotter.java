package burstcoin.com.burst;

/**
 * Created by IceBurst on 7/11/2016.
 */
public class Plotter {
    /*static {
        System.loadLibrary("plot");
    }*/


    private IntProvider callback;

    public Plotter (IntProvider cb) {
        this.callback = cb;
    }

    public void plot1GB() {
        callback.notice("PLOTTER", "SUCCESS", "1GB PLOT CREATED");
    }
}
