package com.link.platform.activity;

import android.app.Activity;
import android.os.Bundle;

import com.link.platform.message.MessageCenter;
import com.link.platform.message.MessageListenerDelegate;

/**
 * Created by danyang.ldy on 2014/12/15.
 */
public abstract class BaseActivity extends Activity implements MessageListenerDelegate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentView();
        initView();
        initControl();
    }

    @Override
    public void onResume() {
        super.onResume();
        addMessageListener();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageCenter.getInstance().removeListener(this);
    }

    protected abstract void initContentView();

    protected abstract void initView();

    protected abstract void initControl();

    protected abstract void addMessageListener();
}
