package burstcoin.com.burst.burstcoin.com.burst.plotting;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by tim on 7/19/2016.
 */

/*
 * Used to maintain all of the Plot Files stored on the system
 */

public class PlotFiles {

    private ArrayList<PlotFile> mPlotFiles;
    //private ArrayList<String> mPlotFiles;
    private String mPath;
    private String mNumericID;

    public PlotFiles(String path, String numericID) {
        mPath = path;
        mNumericID = numericID;
        getPlotFiles();
    }

    // Returns the number of plots
    public int size() { return mPlotFiles.size(); }

    // Delete the last plot in the list
    public void deletePlot() {
        if (mPlotFiles.size() > 0) {
            // delete it from disk
        }
        getPlotFiles();
    }

    private void getPlotFiles() {
        String workingFileName = "";
        Log.d("Files", "Path: " + mPath);
        File f = new File(mPath);
        File file[] = f.listFiles();
        Log.d("Files", "Size: "+ file.length);
        for (int i=0; i < file.length; i++)
        {
            workingFileName = file[i].getName();
            Log.d("Files", "FileName:" + workingFileName);
            if(workingFileName.contains(mNumericID)) {
                mPlotFiles.add(new PlotFile(workingFileName)); // Put it on the stack if it starts with numericID
            }
        }
    }

}
