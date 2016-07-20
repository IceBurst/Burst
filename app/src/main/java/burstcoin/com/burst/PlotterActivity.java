package burstcoin.com.burst;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.TreeMap;

import burstcoin.com.burst.BurstUtil;
import burstcoin.com.burst.IntProvider;
import burstcoin.com.burst.MainActivity;
import burstcoin.com.burst.PlotterActivity;
import burstcoin.com.burst.burstcoin.com.burst.plotting.IntPlotStatus;
import burstcoin.com.burst.burstcoin.com.burst.plotting.Plotter;

public class PlotterActivity extends AppCompatActivity implements IntPlotStatus {

    //private Plotter plotter;

    private TreeMap<String, String> mMiningPools = null;
    private TextView mTxtDriveInfo;
    private TextView mTxtTest;
    private TextView mTxtDriveMessage;
    private TextView mTxtDrivePlotSize;
    private Button mBtnDone;
    private Button mBtnPlot;
    private Button mBtnDeletePlot;
    private SeekBar mSizeBar;

    private String numericID;
    private double mTotalSpace = 0;
    private double mFreeSpace = 0;
    private final static String TAG = "PlotterActivity";
    private Plotter mPlotter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plotter);
        numericID = getIntent().getStringExtra(MainActivity.NUMERICID);
        mPlotter = new Plotter((IntPlotStatus)this, numericID);

        // Enable the Done button
        mBtnDone = (Button) findViewById(R.id.btnDone);
        mBtnPlot = (Button) findViewById(R.id.btnPlot);
        mBtnDeletePlot = (Button) findViewById(R.id.btnDeletePlot);
        mSizeBar = (SeekBar) findViewById(R.id.setPlotSize);
        mTxtDriveInfo =  (TextView) findViewById(R.id.txtDriveInfo);
        mTxtDriveMessage =  (TextView) findViewById(R.id.txtDriveMessage);
        mTxtDrivePlotSize = (TextView) findViewById(R.id.txtPlotGBSize);
        mTxtTest = (TextView) findViewById(R.id.txtTestHolder);

        mBtnPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToDo: Start a Wait wheel
                mPlotter.plot1GB();
            }
        });

        mBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Lets update the Screen with current System/Plot Information
        updateDriveInfo();
        updateDriveMessage();

        mSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                progress = seekBar.getProgress();
                if(progress > 0) {
                    mBtnPlot.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    mBtnPlot.setEnabled(true);
                } else {
                    mBtnPlot.setBackgroundColor(Color.DKGRAY);
                    mBtnPlot.setEnabled(false);
                }
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                //Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
                mTxtDrivePlotSize.setText("Plot "+Integer.toString(progress)+"GB");
                if(progress > 0) {
                    mBtnPlot.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    mBtnPlot.setEnabled(true);
                } else {
                    mBtnPlot.setBackgroundColor(Color.DKGRAY);
                    mBtnPlot.setEnabled(false);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mTxtDrivePlotSize.setText("Plot "+Integer.toString(progress)+"GB");
                if(progress > 0) {
                    mBtnPlot.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    mBtnPlot.setEnabled(true);
                } else {
                    mBtnPlot.setBackgroundColor(Color.DKGRAY);
                    mBtnPlot.setEnabled(false);
                }
            }
        });




        // below here is all pilot code that is a work in progress

        String mFreeMem = Long.toString(new BurstUtil().getFreeMemoryInMB(this));
        String mTotalMem = Long.toString(new BurstUtil().getTotalMemoryInMB(this));
        mTxtTest.setText("Free Memory "+mFreeMem+"MB of "+mTotalMem+"MB");
        updateCurrentPlotInfo();

        // This might be best in the Mining Activity
        //loadMiningPools();

    }

    // We should pass in the Text box to update
    private void updateDriveInfo() {
        mTotalSpace = BurstUtil.getTotalSpaceInGB();
        String mStringTotalSpace = Double.toString(mTotalSpace);
        mFreeSpace =  BurstUtil.getFreeSpaceInGB();
        String mStringFreeSpace = Double.toString(mFreeSpace);
        mTxtDriveInfo.setText(mStringFreeSpace+"GB Free of "+ mStringTotalSpace+"GB Total");
        ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.barDriveUseage);
        // Just Trying this out
        mProgressBar.setMax((int)(mTotalSpace*100));
        mProgressBar.setProgress((int)((mTotalSpace-mFreeSpace)*100));
        //android:max
        //android:progress
        DecimalFormat roundingFormat = new DecimalFormat("#");
        roundingFormat.setRoundingMode(RoundingMode.DOWN);

        // Set the Max Slider to the Max Round Down free GB
        mSizeBar.setMax(Integer.parseInt(roundingFormat.format(mFreeSpace)));
    }
    // Best Memory sizes for plotting are things that break nicely into 1GB, powers of 2.
    // Try to target for 512mb of RAM, less is OK

    private void updateDriveMessage() {
        if (mFreeSpace < 1) {
            mTxtDriveMessage.setText("Not Enough Space to Plot");
            mBtnPlot.setEnabled(false);
            mBtnPlot.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
        else {
            mTxtDriveMessage.setText("");
            mBtnPlot.setEnabled(true);
            mBtnPlot.setBackgroundColor(Color.DKGRAY);  // ToDo: this didn't become darkGry
        }
    }

    private void updateCurrentPlotInfo() {

    }

    // This should probably be in the Mining section so we can check/Set reward assignment
    // We can also later show the user which pool they are tied to if we know about it
    private void loadMiningPools( ){
        // Later this should get data from an External Source
        mMiningPools = null;
        mMiningPools = new TreeMap<String, String>();
        mMiningPools.put("pool.burst-team.us","BURST-32TT-TSAC-HTKW-CC26C");
        // Add more pools just like above

        /*
        Pool.Burstcoin.de / pool.Burstcoin.uk
        Recipient: BURST-GHTV-7ZP3-DY4B-FPBFA
        URL: http://pool.burstcoin.de (Port 8080 )
        */
    }

    @Override
    public void notice(String... args){
        // some updates we get are about the plotting and when it's done
        // This is how we get data back from the Plotter Tool
        String line="";
        for (String s : args)
            line+=" " + s;
        Log.d(TAG, line);
    }

}
