package com.blackdiamond.apps.studio.vpn.view.activites;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.blackdiamond.apps.studio.vpn.R;
import com.blackdiamond.apps.studio.vpn.databinding.ActivityMenuBinding;
import com.blackdiamond.apps.studio.vpn.dialogs.RateDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MenuActivity extends AppCompatActivity {

    ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadBannerAd();


        binding.drawerShareItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareApp();
            }
        });
        binding.drawerPrivacyItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                privacyPolicyLink();
            }
        });
        binding.drawerAboutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });
        binding.drawerRateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RateDialog rateDialog = new RateDialog(MenuActivity.this);
                rateDialog.show();
            }
        });

    }

    private void loadBannerAd() {
        binding.menuBannerBlock.setVisibility(View.VISIBLE);
        binding.menuBannerContainerAdmob.setVisibility(View.GONE);
        binding.menuBannerContainerFacebook.setVisibility(View.GONE);
        binding.menuBannerContainerAdmob.setVisibility(View.VISIBLE);
        loadAdmobBannerAd();

    }

    private void loadAdmobBannerAd() {
        AdView adview = binding.menuBannerContainerAdmob;
        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);
    }
    public void shareApp() {
        StringBuilder msg = new StringBuilder();
        msg.append(getString(R.string.share_msg));
        msg.append("\n");
        msg.append("https://play.google.com/store/apps/details?id=com.snaptube.savevideos.all.videos.downloader2020.allvideodownload");
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg.toString());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }
    private void showAboutDialog() {
        Dialog dialog = new Dialog(MenuActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_about);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.findViewById(R.id.bt_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void privacyPolicyLink() {
        Intent webActivity = new Intent(MenuActivity.this, loadingWebData.class);
        webActivity.putExtra("activityName", "Privacy Policy");
        webActivity.putExtra("webLink", R.string.privacy_policy_link);
        startActivity(webActivity);
    }
}