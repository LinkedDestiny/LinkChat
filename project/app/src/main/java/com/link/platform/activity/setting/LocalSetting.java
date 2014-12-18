package com.link.platform.activity.setting;

import com.link.platform.storage.PropertiesHelper;
import com.link.platform.util.StringUtil;
import com.link.platform.util.Utils;

import java.util.Properties;

/**
 * Created by danyang.ldy on 2014/12/10.
 */
public class LocalSetting {

    private static LocalSetting Instance = null;

    public final static String LOCAL_NAME = "name";

    public static LocalSetting getInstance() {
        if( Instance == null ) {
            synchronized (LocalSetting.class ) {
                if( Instance == null ) {
                    Instance = new LocalSetting();
                }
            }
        }
        return Instance;
    }

    private Properties settings;

    private LocalSetting() {
        settings = PropertiesHelper.LOAD_PROP(Utils.LOCAL_SETTING);
    }

    public void store() {
        PropertiesHelper.SAVE_PROP(settings, Utils.LOCAL_SETTING);
    }

    public void setLocalName(String localName) {
        settings.put(LOCAL_NAME, localName);
        store();
    }

    public String getLocalName() {
        String name = settings.getProperty(LOCAL_NAME);
        return StringUtil.isBlank(name) ? "" : name;
    }

}
