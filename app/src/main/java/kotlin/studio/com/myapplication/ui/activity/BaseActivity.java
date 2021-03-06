package kotlin.studio.com.myapplication.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import kotlin.studio.com.myapplication.inject.ViewInjectUtils;

/**
 * Description:
 * Copyright  : Copyright (c) 2016
 * Company    : Android
 * Author     : 关羽
 * Date       : 2018-07-17 14:38
 */
public abstract class BaseActivity<T> extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewInjectUtils.inject(this);
    }


    /**
     * 加载数据
     */
//    protected abstract void initData();


//    protected abstract T createPresenter();
}
