package com.link.platform.ui.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.link.platform.R;

/**
 * Created by danyang.ldy on 2014/12/10.
 */
public class SearchEditText extends EditText {

    private Drawable searchIcon;
    private Drawable deleteIcon;
    private boolean mMagnifyingGlassShown = true;

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        int icon_size = getResources().getDimensionPixelSize(R.dimen.search_icon_size);
//	        int delete_size = (int) getResources().getDimension(R.dimen.search_delete_size);
        searchIcon = getCompoundDrawables()[0];
        if(searchIcon != null){
            searchIcon.setBounds(0, 0, icon_size, icon_size);
        }
        deleteIcon = getCompoundDrawables()[2];
        if(deleteIcon != null){
            deleteIcon.setBounds(0, 0, icon_size, icon_size);
        }
        setCompoundDrawables(searchIcon, null, null, null);

    }

    @Override
    public boolean onPreDraw() {
        boolean isEmpty = TextUtils.isEmpty(getText());
        if (mMagnifyingGlassShown != isEmpty) {
            mMagnifyingGlassShown = isEmpty;
            if (mMagnifyingGlassShown) {
                setCompoundDrawables(searchIcon, null, null, null);
            } else {
                setCompoundDrawables(searchIcon, null, deleteIcon, null);
            }
            return false;
        }
        return super.onPreDraw();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if(action == MotionEvent.ACTION_UP){
            float x = event.getX();
            float w = getWidth() - deleteIcon.getIntrinsicWidth() - getPaddingRight();
            if(x > w){
                setText(null);
                if (searchIcon != null){
                    setCompoundDrawables(searchIcon, null, null, null);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

}
