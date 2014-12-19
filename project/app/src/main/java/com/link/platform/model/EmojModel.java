package com.link.platform.model;

import com.link.platform.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danyang.ldy on 2014/12/18.
 */
public class EmojModel extends BaseModel {

    public static EmojModel Instance = null;
    public static EmojModel getInstance() {
        if(Instance == null) {
            synchronized (EmojModel.class) {
                if(Instance == null) {
                    Instance = new EmojModel();
                    BaseModel.register(Instance);
                }
            }
        }
        return Instance;
    }

    private final static int AMOUNT = 20;
    private final static int TOTAL = 20;

    private List<Integer> emojs = new ArrayList<Integer>();

    private EmojModel() {
        for(int i = 0 ; i < TOTAL ; i ++ ) {
            emojs.add(R.drawable.eaa + i );
        }
    }

    public List<Integer> getEmojs( int index ) {
        int begin = index * AMOUNT;
        int len = AMOUNT;
        if( ( begin + AMOUNT ) > emojs.size() ) {
            len = emojs.size() - begin;
        }
        return emojs.subList( index * AMOUNT, len );
    }



    @Override
    protected void exit() {

    }
}
