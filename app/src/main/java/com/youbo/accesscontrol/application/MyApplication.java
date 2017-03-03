package com.youbo.accesscontrol.application;

import android.app.Application;
import android.content.Intent;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.youbo.accesscontrol.huanxin_services.ControlService;


/**
 * Created by kingx on 2017/3/2.
 */

public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        EMOptions options = new EMOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        options.setAutoLogin(false);
        // 初始化
        EMClient.getInstance().init(getApplicationContext(), options);

        this.startService(new Intent(getApplicationContext(),ControlService.class));


    }
}
