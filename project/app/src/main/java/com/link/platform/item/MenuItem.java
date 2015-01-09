package com.link.platform.item;

import com.link.platform.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danyang.ldy on 2014/12/18.
 */
public class MenuItem {

    public final static int MENU_FILE = 0;
    public final static int MENU_PIC = 1;
    public final static int MENU_CAMERA = 2;

    public static List<MenuItem> list = new ArrayList<MenuItem>();

    static {
        list.add(new MenuItem(R.drawable.send_file , "文件") );
        list.add(new MenuItem(R.drawable.send_pic , "图片") );
        list.add(new MenuItem(R.drawable.send_camera , "拍照") );
    }

    public int image;
    public String name;

    public MenuItem(int image, String name) {
        this.image = image;
        this.name = name;
    }

}
