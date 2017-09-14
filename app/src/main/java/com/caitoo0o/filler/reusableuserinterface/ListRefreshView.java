package com.caitoo0o.filler.reusableuserinterface;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by caitoo0o on 8/21/2017.
 */

public class ListRefreshView extends ListView {

    public interface OnRefreshHandler {
        void onRefresh();
    }

    OnRefreshHandler onRefreshHandler;

    public ListRefreshView(Context context) {
        super(context);
    }

    public ListRefreshView(Context context, AttributeSet attrs) {
            super(context, attrs);
    }

    public ListRefreshView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
    }

    public void setOnRefreshHandler(OnRefreshHandler onRefreshHandler) {
        this.onRefreshHandler = onRefreshHandler;
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (onRefreshHandler != null) {
            onRefreshHandler.onRefresh();
        }
    }
}
