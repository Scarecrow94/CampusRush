package com.blackviking.campusrush.Settings;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.campusrush.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Settings extends AppCompatActivity {

    private TextView activityName;
    private ImageView exitActivity, helpActivity;
    private LinearLayout accountSetting, notificationSetting, inviteFriends, helpSettings;
    private RelativeLayout appInfoSetting;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*---   FONT MANAGEMENT   ---*/
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Wigrum-Regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_settings);


        /*---   WIDGETS   ---*/
        activityName = (TextView)findViewById(R.id.activityName);
        exitActivity = (ImageView)findViewById(R.id.exitActivity);
        helpActivity = (ImageView)findViewById(R.id.helpIcon);
        accountSetting = (LinearLayout)findViewById(R.id.accountSettingsLayout);
        notificationSetting = (LinearLayout)findViewById(R.id.notificationSettingLayout);
        inviteFriends = (LinearLayout)findViewById(R.id.inviteFriendsLayout);
        helpSettings = (LinearLayout)findViewById(R.id.helpLayout);
        appInfoSetting = (RelativeLayout)findViewById(R.id.appInfoLayout);


        /*---   ACTIVITY BAR FUNCTIONS   ---*/
        exitActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_right, R.anim.slide_right);
            }
        });

        activityName.setText("Settings");
        helpActivity.setVisibility(View.GONE);


        /*---   ACCOUNT SETTING   ---*/
        accountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accountSettingIntent = new Intent(Settings.this, AccountSettings.class);
                startActivity(accountSettingIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   NOTIFICATION SETTING   ---*/
        notificationSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


        /*---   INVITE FRIENDS   ---*/
        inviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Campus Rush Invite");
                i.putExtra(android.content.Intent.EXTRA_TEXT, "Hey There, \n \nGet Latest Info Of Happenings Around The Campus On The CAMPUS RUSH App. You Can Download For Free On PlayStore And Connect With Other Students. \nUse The Link below \nhttps://play.google.com/store/apps/details?id=com.blackviking.hosh");
                startActivity(Intent.createChooser(i,"Share via"));
            }
        });


        /*---   HELP   ---*/
        helpSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(Settings.this, Help.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });


        /*---   APP INFO   ---*/
        appInfoSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent appInfoIntent = new Intent(Settings.this, AppInfo.class);
                startActivity(appInfoIntent);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}