package com.ldz.fpt.businesscardscannerandroid.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by linhdq on 5/25/17.
 */

public class CheckDataTrainingProcess extends AsyncTask<Context, Void, Void> {

    private void copyFiles(String datapath, Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            String filepath = datapath + "/eng.traineddata";

            //open byte streams for reading/writing
            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkFile(File dir, String langCode, Context context) {
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (dir.exists()) {
            String datapath = dir.getPath();
            String datafilepath = datapath + "/" + langCode + ".traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles(datapath, context);
            }
        }
    }

    @Override
    protected Void doInBackground(Context... params) {
        checkFile(Util.getTrainingDataDir(params[0]), "eng", params[0]);
        return null;
    }
}
