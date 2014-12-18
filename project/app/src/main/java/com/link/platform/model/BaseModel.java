package com.link.platform.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danyang.ldy on 2014/12/11.
 */
public abstract class BaseModel {

    protected static List<BaseModel> pools = new ArrayList<BaseModel>();

    protected static void register(BaseModel model) {
        pools.add(model);
    }

    public static void release() {
        for(BaseModel model : pools ) {
            model.exit();
        }
        pools.clear();
    }


    abstract protected void exit();
}
