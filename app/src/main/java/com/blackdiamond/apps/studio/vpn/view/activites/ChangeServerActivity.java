package com.blackdiamond.apps.studio.vpn.view.activites;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.badoo.mobile.util.WeakHandler;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.blackdiamond.apps.studio.vpn.BuildConfig;
import com.blackdiamond.apps.studio.vpn.R;
import com.blackdiamond.apps.studio.vpn.SharedPreference;
import com.blackdiamond.apps.studio.vpn.adapter.ServerAdapter;
import com.blackdiamond.apps.studio.vpn.databinding.ActivityChangeServerBinding;
import com.blackdiamond.apps.studio.vpn.db.DbHelper;
import com.blackdiamond.apps.studio.vpn.model.Server;
import com.blackdiamond.apps.studio.vpn.utils.CsvParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChangeServerActivity extends AppCompatActivity {

    private ActivityChangeServerBinding binding;

    private WeakHandler handler;
    private OkHttpClient okHttpClient = new OkHttpClient();
    private List<Server> servers = new ArrayList<>();
    private Request request;
    private Call mCall;
    private ServerAdapter adapter;
    private DbHelper dbHelper;

    private SharedPreference sharedPreference;

    private NativeAdLayout nativeAdLayout;
    private NativeBannerAd nativeBannerAd;
    private LinearLayout adView;
    private InterstitialAd facebookInterstitialAd;
    private com.google.android.gms.ads.interstitial.InterstitialAd admobInterstitialAd;
    private Server globalServer;

    private Dialog infoAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFacebookAds();
        handler = new WeakHandler();
        dbHelper = DbHelper.getInstance(getApplicationContext());
        sharedPreference = new SharedPreference(ChangeServerActivity.this);
        binding = ActivityChangeServerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        servers.addAll(dbHelper.getAll());
        setupSwipeRefreshLayout();
        setupRecyclerView();

        if (request == null) {
            request = new Request.Builder()
                    .url(BuildConfig.VPN_GATE_API)
                    .build();
        }

        if (servers.isEmpty()) {
            populateServerList();
        }

        binding.serverBackButton.setOnClickListener(view -> {
            finish();
        });
        binding.changeServerInfoBtn.setOnClickListener(v -> {
            infoDialog();
        });
        binding.changeServerRefreshBtn.setOnClickListener(vv -> {
            populateServerList();
        });

        loadBanner();
        loadInterstitialAd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCall != null) {
            mCall.cancel();
            mCall = null;
        }
        if (infoAlertDialog != null) {
            if (infoAlertDialog.isShowing()) {
                infoAlertDialog.dismiss();
            }
        }
        binding.swipeRefresh.setOnRefreshListener(null);
    }

    private void setupSwipeRefreshLayout() {
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimaryDark);
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateServerList();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ServerAdapter(servers, serverClickCallback);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(ChangeServerActivity.this, 0);
        binding.recyclerview.setHasFixedSize(true);
        binding.recyclerview.addItemDecoration(itemDecoration);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(binding.recyclerview.getContext()));
        binding.recyclerview.setAdapter(adapter);
    }

    private void loadServerList(List<Server> serverList) {
        adapter.setServerList(serverList);
        dbHelper.save(serverList);
    }

    private void populateServerList() {
        binding.swipeRefresh.setRefreshing(true);
        binding.recyclerview.setVisibility(View.INVISIBLE);

        mCall = okHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.swipeRefresh.setRefreshing(false);
                        binding.recyclerview.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final List<Server> servers = CsvParser.parse(response);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadServerList(servers);
                            binding.swipeRefresh.setRefreshing(false);
                            binding.recyclerview.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }

    private final ServerAdapter.ServerClickCallback serverClickCallback =
            server -> {
                Server selectedServer = new Server(
                        server.hostName,
                        server.ipAddress,
                        server.ping,
                        server.speed,
                        server.countryLong,
                        server.countryShort,
                        server.ovpnConfigData,
                        server.port,
                        server.protocol
                );

                sharedPreference.saveServer(selectedServer);

                showInterstitialAd(selectedServer);
            };

    private void loadBanner() {
            binding.changeServerAdBlock.setVisibility(View.VISIBLE);
            binding.changeServerBannerAdmob.setVisibility(View.VISIBLE);
            binding.changeServerBannerFacebook.setVisibility(View.VISIBLE);
            loadAdmobBanner();

    }

    private void loadAdmobBanner() {
        AdView adView = binding.changeServerBannerAdmob;
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void loadFacebookBanner() {
        nativeBannerAd = new NativeBannerAd(this, getString(R.string.facebook_native_banner_id));
        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
            }

            @Override
            public void onError(Ad ad, AdError adError) {
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Race condition, load() called again before last ad was displayed
                if (nativeBannerAd == null || nativeBannerAd != ad) {
                    return;
                }
                // Inflate Native Banner Ad into Container
                inflateAd(nativeBannerAd);
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        };

        nativeBannerAd.loadAd(
                nativeBannerAd.buildLoadAdConfig()
                        .withAdListener(nativeAdListener)
                        .build());
    }

    private void initFacebookAds() {
        AudienceNetworkAds.initialize(this);
    }

    private void inflateAd(NativeBannerAd nativeBannerAd) {
        nativeBannerAd.unregisterView();
        nativeAdLayout = findViewById(R.id.change_server_banner_facebook);
        LayoutInflater inflater = LayoutInflater.from(ChangeServerActivity.this);
        adView = (LinearLayout) inflater.inflate(R.layout.native_banner_ad_unit, nativeAdLayout, false);
        nativeAdLayout.addView(adView);
        RelativeLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(ChangeServerActivity.this, nativeBannerAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        MediaView nativeAdIconView = adView.findViewById(R.id.native_icon_view);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);
        nativeAdCallToAction.setText(nativeBannerAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(
                nativeBannerAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdTitle.setText(nativeBannerAd.getAdvertiserName());
        nativeAdSocialContext.setText(nativeBannerAd.getAdSocialContext());
        sponsoredLabel.setText(nativeBannerAd.getSponsoredTranslation());

        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);
        nativeBannerAd.registerViewForInteraction(adView, nativeAdIconView, clickableViews);
    }

    private void loadInterstitialAd() {
                AdRequest adRequest = new AdRequest.Builder().build();
                com.google.android.gms.ads.interstitial.InterstitialAd.load(this, getResources().getString(R.string.admob_interstitial_id), adRequest,
                        new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                                admobInterstitialAd = interstitialAd;
                                admobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                    @Override
                                    public void onAdClicked() {
                                        super.onAdClicked();
                                    }

                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent();
                                        setIntentResult(globalServer);
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                        super.onAdFailedToShowFullScreenContent(adError);
                                    }

                                    @Override
                                    public void onAdImpression() {
                                        super.onAdImpression();
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        super.onAdShowedFullScreenContent();
                                        admobInterstitialAd = null;
                                    }
                                });
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                // Handle the error
                                admobInterstitialAd = null;
                            }
                        });

    }

    private void showInterstitialAd(Server server) {
        globalServer = server;

            if (admobInterstitialAd != null) {
                admobInterstitialAd.show(ChangeServerActivity.this);
            } else if (facebookInterstitialAd != null) {
                facebookInterstitialAd.show();
            } else {
                setIntentResult(server);
            }

    }

    private void setIntentResult(Server server) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("serverextra", server);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void infoDialog() {

        infoAlertDialog = new Dialog(this);
        infoAlertDialog.setContentView(R.layout.info_dialog);
        infoAlertDialog.setCancelable(false);
        infoAlertDialog.setCanceledOnTouchOutside(false);

        Button okayButton = infoAlertDialog.findViewById(R.id.info_dialog_btn);
        TextView infoTextview = infoAlertDialog.findViewById(R.id.info_dialog_details);

        infoTextview.setMovementMethod(LinkMovementMethod.getInstance());

        okayButton.setOnClickListener(v -> {
            infoAlertDialog.dismiss();
        });

        infoAlertDialog.getWindow().setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        infoAlertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        infoAlertDialog.show();
    }
}