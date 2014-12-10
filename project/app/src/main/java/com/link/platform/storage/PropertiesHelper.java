package com.link.platform.storage;

import android.os.Environment;

import com.link.platform.MainApplication;
import com.link.platform.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by danyang.ldy on 2014/12/10.
 */
public class PropertiesHelper {
    private static String PATH = null;

    private static void INIT_PROP() {
        Utils.PATH = "/data" + Environment.getDataDirectory().getAbsolutePath() + "/com.link.platform";
        PATH = Utils.PATH + "/files/";
        File file = new File(PATH);
        if( !file.exists() ) {
            file.mkdirs();
        }
    }

    public static Properties LOAD_PROP( String filename ) {
        if( PATH == null ) {
            INIT_PROP();
        }
        try{
            Properties prop = new Properties();
            File file = new File( PATH + filename + ".properties" );
            if( !file.exists() ) {
                FileOutputStream out = MainApplication.getContext().openFileOutput(filename + ".properties" , MainApplication.getContext().MODE_APPEND );
                out.close();
            }
            FileInputStream fis = MainApplication.getContext().openFileInput(filename + ".properties");
            prop.load(fis);

            return prop;
        } catch( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean SAVE_PROP( Properties p , String filename ) {
        if( PATH == null ) {
            INIT_PROP();
        }
        try{
            FileOutputStream out = MainApplication.getContext().openFileOutput(filename + ".properties" , MainApplication.getContext().MODE_PRIVATE );
            p.store(out,"");

            return true;
        }
        catch( IOException e ) {
            e.printStackTrace();
            return false;
        }
    }
}
