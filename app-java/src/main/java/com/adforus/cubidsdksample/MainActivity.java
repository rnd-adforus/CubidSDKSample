package com.adforus.cubidsdksample;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.adforus.sdk.cubid.CuBidInitListener;
import com.adforus.sdk.cubid.CuBidSettings;
import com.adforus.sdk.cubid.util.CubidError;
import com.adforus.sdk.cubid.view.CubidBanner;
import com.adforus.sdk.cubid.view.CubidBannerListener;
import com.adforus.sdk.cubid.view.CubidInterstitial;
import com.adforus.sdk.cubid.view.CubidInterstitialListener;
import com.adforus.sdk.cubid.view.CubidNative;
import com.adforus.sdk.cubid.view.CubidNativeAdListener;
import com.adforus.sdk.cubid.view.CubidNativeView;
import com.adforus.sdk.cubid.view.CubidReward;
import com.adforus.sdk.cubid.view.CubidRewardListener;
import com.adforus.sdk.cubid.view.CubidSize;
/**
 * 📌 Cubid SDK 연동 샘플 사용 시 유의사항
 *
 * 1. 본 샘플 코드는 테스트 목적의 값들을 포함하고 있습니다.
 *    실제 배포 전 반드시 운영에 적합한 값으로 교체하세요.
 *    관련 정보는 Cubid SDK 운영팀을 통해 확인하시기 바랍니다.
 *
 *    🔸 교체가 필요한 테스트 값 목록:
 *      - AndroidManifest.xml 내 meta-data 태그:
 *          com.google.android.gms.ads.APPLICATION_ID → value
 *      - CuBidSettings.initialize() 호출 시:
 *          setId, userId
 *      - 광고 로드 관련 객체 생성 시:
 *          CubidBanner, CubidNative, CubidInterstitial, CubidReward → placementId
 *
 * 2. 사용자 입력 값(userId 등)은 반드시 적절히 **인코딩 후 전달**해야 합니다.
 *    Cubid SDK는 입력값에 대한 인코딩을 자동으로 처리하지 않으며,
 *    인코딩되지 않은 사용자 데이터로 인해 발생하는 문제
 *    (예: 보안 취약점, 데이터 손상, 예외 발생 등)에 대해 Cubid 측은 책임지지 않습니다.
 */
public class MainActivity extends AppCompatActivity {
    private CubidBanner cubidBanner;
    private CubidNative cubidNative;
    private CubidInterstitial cubidInterstitial;
    private CubidReward cubidReward;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSystemLayout();

        // 화면 회전, 전환 등 구성 변경(configuration change)시에는 메모리 누수를 예방하기 위해 반드시 광고 인스턴스를 초기화하고 새로운 인스턴스를 사용하여 광고를 재 호출하여 사용할 것을 권장합니다.
        //release();

        View initControlBox = findViewById(R.id.box_init);
        Button initButton = initControlBox.findViewById(R.id.btn_init);
        View extendBox = initControlBox.findViewById(R.id.box_init_btns);
        StatusView statusInfoView = extendBox.findViewById(R.id.include_status_view);
        SwitchCompat debugButton = extendBox.findViewById(R.id.btn_debug);

        final String[] failedMessage = {""};

        statusInfoView.setVisibility(View.VISIBLE);
        statusInfoView.setState(AdStatus.AD_DEFAULT);


        initButton.setOnClickListener(v -> {
            //initialize - 초기화
            // 초기화 성공 콜백에서 광고 로드 시작, 초기화 성공 콜백 호출 전 만든 모든 광고 인스턴스는 유효하지 않습니다.
            CuBidSettings.initialize(this, "OQf8T68ys9", "test-cubider", new CuBidInitListener() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        statusInfoView.setState(AdStatus.INIT_SUCCESS);
                        initAds();
                    });
                }

                @Override
                public void onFailed(String message) {
                    runOnUiThread(() -> {
                        failedMessage[0] = message;
                        statusInfoView.setState(AdStatus.FAILED);
                    });
                }
            });
        });

        CuBidSettings.setDebugMode(debugButton.isChecked());
        debugButton.setOnCheckedChangeListener((buttonView, isChecked) -> CuBidSettings.setDebugMode(isChecked));

        statusInfoView.setOnClickListener(v -> checkPopMessage(statusInfoView.getState(), failedMessage[0]));
    }


    private void initAds() {
        //banner
        initBanner();
        //native
        initNative();
        //interstitial
        initInterstitial();
        //reward
        initReward();
    }

    private void initBanner() {
        View bannerControlBox = findViewById(R.id.box_banner);
        RadioGroup bannerSizeRadioGroup = bannerControlBox.findViewById(R.id.size_radio_button);
        View bannerFunctionBox = bannerControlBox.findViewById(R.id.box_function_views);
        View bannerButtonBox = bannerControlBox.findViewById(R.id.box_btns_and_view);
        StatusView bannerStatusView = bannerControlBox.findViewById(R.id.box_status);
        ToggleButton bannerToggleButton = bannerControlBox.findViewById(R.id.arrow_ad_function);
        EtcInteraction.setToggleViewInteraction(bannerToggleButton, bannerFunctionBox);

        Button bannerButtonLoad = bannerButtonBox.findViewById(R.id.btn_load);
        CheckBox bannerButtonDisplay = bannerButtonBox.findViewById(R.id.btn_display);
        CheckBox bannerButtonVisible = bannerButtonBox.findViewById(R.id.btn_visible);

        LinearLayout container = bannerButtonBox.findViewById(R.id.ad_view_container);
        final CubidBanner[] bannerLoadView = {null};
        final String[] failedMessage = {""};

        bannerStatusView.setState(AdStatus.AD_DEFAULT);
        bannerStatusView.setVisibility(View.VISIBLE);

        final CubidSize[] size = {CubidSize.TYPE_320X50};
        bannerSizeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_320X100:
                    size[0] = CubidSize.TYPE_320X100;
                    break;
                case R.id.radio_300X250:
                    size[0] = CubidSize.TYPE_300X250;
                    break;
                default:
                    size[0] = CubidSize.TYPE_320X50;
            }
        });

        // 배너 광고 인스턴스 준비
        cubidBanner = new CubidBanner(this, "uyXywuOXTt");
        cubidBanner.setBannerListener(new CubidBannerListener() {
            @Override
            public void onLoad() {
                bannerStatusView.setState(AdStatus.AD_LOADED);
                bannerLoadView[0] = cubidBanner;

                bannerButtonDisplay.setEnabled(true);
                bannerButtonVisible.setEnabled(true);
                bannerButtonVisible.setChecked(bannerLoadView[0].getVisibility() == View.VISIBLE);
                bannerButtonVisible.setText(bannerLoadView[0].getVisibility() == View.VISIBLE ? R.string.hide : R.string.show);
            }

            @Override
            public void onLoadFail(CubidError errorInfo) {
                failedMessage[0] = "[" + errorInfo.getCode() + "]\n" + errorInfo.getMessage();
                bannerStatusView.setState(AdStatus.FAILED);
            }

            @Override
            public void onDisplay() {
                bannerStatusView.setState(AdStatus.AD_DISPLAY);

            }

            @Override
            public void onClick() {
                bannerStatusView.setState(AdStatus.AD_CLICK);
            }
        });

        bannerButtonLoad.setOnClickListener(v -> {
            bannerStatusView.setState(AdStatus.AD_REQ);
            // 배너 사이즈 설정
            cubidBanner.setSize(size[0]);
            // 배너 광고 로드
            cubidBanner.loadAd();
        });

        // 로드 된 광고 뷰 조작 예시
        bannerButtonDisplay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            CubidBanner _bannerLoadView = bannerLoadView[0];
            if (_bannerLoadView == null) return;
            if (isChecked) {
                container.removeAllViews();
                container.addView(_bannerLoadView);
                bannerButtonDisplay.setText(R.string.remove);
                bannerStatusView.setState(AdStatus.AD_DISPLAY);
            } else {
                ViewGroup parent = (ViewGroup) _bannerLoadView.getParent();
                if (parent != null) {
                    parent.removeView(_bannerLoadView);
                    bannerButtonDisplay.setText(R.string.display);
                    bannerStatusView.setState(AdStatus.AD_REMOVE);
                }
            }
        });

        bannerButtonVisible.setOnCheckedChangeListener((buttonView, isChecked) -> {
            View _bannerLoadView = bannerLoadView[0];
            if (_bannerLoadView == null) return;
            _bannerLoadView.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            bannerButtonVisible.setText(isChecked ? R.string.hide : R.string.show);
            bannerStatusView.setState(isChecked ? AdStatus.AD_SHOW : AdStatus.AD_HIDE);
        });

        bannerStatusView.setOnClickListener(v -> {
            EtcInteraction.popMessageDialog(this, failedMessage[0]);
        });

    }
    private void initNative() {
        View nativeControlBox = findViewById(R.id.box_native);
        View nativeButtonBox = nativeControlBox.findViewById(R.id.box_btns_and_view);
        StatusView nativeStateView = nativeControlBox.findViewById(R.id.box_status);
        ToggleButton nativeToggleButton = nativeControlBox.findViewById(R.id.arrow_ad_function);
        EtcInteraction.setToggleViewInteraction(nativeToggleButton, nativeButtonBox);

        Button btnLoad = nativeButtonBox.findViewById(R.id.btn_load);
        CheckBox btnDisplay = nativeButtonBox.findViewById(R.id.btn_display);
        CheckBox btnVisible = nativeButtonBox.findViewById(R.id.btn_visible);

        LinearLayout container = nativeButtonBox.findViewById(R.id.ad_view_container);

        final View[] nativeLoadView = {null};
        final String[] failedMessage = {""};

        nativeStateView.setState(AdStatus.AD_DEFAULT);
        nativeStateView.setVisibility(View.VISIBLE);

        // 네이티브 광고 인스턴스 준비 - 필수 값은 반드시 유효한 id로 입력 하여 주세요.
        cubidNative = new CubidNative.Builder(this, "5xJlpEi80m")
                .setNativeLayoutID(R.layout.layout_custom_native) // 필수
                .setAdMediaID(R.id.item_main) // 필수
                .setAdHeadLineID(R.id.item_headline) // 필수
                .setAdCallToActionID(R.id.item_action) // 필수
                .setAdIconID(R.id.item_icon)
                .setAdBodyID(R.id.item_body)
                .build();

        cubidNative.setNativeListener(new CubidNativeAdListener() {
            @Override
            public void onLoaded(@NonNull CubidNativeView cubidNativeView) {
                btnDisplay.setEnabled(true);
                btnVisible.setEnabled(true);
                btnVisible.setText(cubidNativeView.getVisibility() == View.VISIBLE ?
                        getResources().getText(R.string.hide) :
                        getResources().getText(R.string.show));

                nativeLoadView[0] = cubidNativeView;
                btnVisible.setChecked(cubidNativeView.getVisibility() == View.VISIBLE);
                nativeStateView.setState(AdStatus.AD_LOADED);
            }

            @Override
            public void onLoadFailed(CubidError error) {
                failedMessage[0] = "[" + error.getCode() + "]\n" + error.getMessage();
                nativeStateView.setState(AdStatus.FAILED);
            }

            @Override
            public void onClick() {
                nativeStateView.setState(AdStatus.AD_CLICK);
            }

            @Override
            public void onImpression() {
                nativeStateView.setState(AdStatus.AD_DISPLAY);
            }
        });

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativeStateView.setState(AdStatus.AD_REQ);
                if (cubidNative != null) {
                    // 네이티브 광고 로드
                    cubidNative.loadAd();
                }
            }
        });

        // 로드 된 광고 뷰 조작 예시
        btnDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = nativeLoadView[0];
                if (view == null) return;

                ViewParent parent = view.getParent();
                if (parent instanceof ViewGroup) {
                    ((ViewGroup) parent).removeView(view);
                    btnDisplay.setText(getResources().getText(R.string.display));
                    nativeStateView.setState(AdStatus.AD_REMOVE);
                } else {
                    container.removeAllViews();
                    container.addView(view);
                    btnDisplay.setText(getResources().getText(R.string.remove));
                    nativeStateView.setState(AdStatus.AD_DISPLAY);
                }
            }
        });

        btnVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = nativeLoadView[0];
                if (view == null) return;

                if (view.getVisibility() == View.VISIBLE) {
                    view.setVisibility(View.INVISIBLE);
                    btnVisible.setText(getResources().getText(R.string.show));
                    nativeStateView.setState(AdStatus.AD_HIDE);
                } else {
                    view.setVisibility(View.VISIBLE);
                    btnVisible.setText(getResources().getText(R.string.hide));
                    nativeStateView.setState(AdStatus.AD_SHOW);
                }
            }
        });

        nativeStateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPopMessage(nativeStateView.getState(), failedMessage[0]);
            }
        });
    }

    private void initReward() {
        View rewardControlBox = findViewById(R.id.box_reward);
        View rewardButtonBox = rewardControlBox.findViewById(R.id.box_btns);
        StatusView rewardStatusView = rewardControlBox.findViewById(R.id.box_status);
        ToggleButton rewardToggleButton = rewardControlBox.findViewById(R.id.arrow_ad_function);

        EtcInteraction.setToggleViewInteraction(rewardToggleButton, rewardButtonBox);

        Button btnLoad = rewardButtonBox.findViewById(R.id.btn_load);
        Button btnShow = rewardButtonBox.findViewById(R.id.btn_show);

        final String[] failedMessage = {""};

        rewardStatusView.setState(AdStatus.AD_DEFAULT);
        rewardStatusView.setVisibility(View.VISIBLE);

        // 광고 인스턴스 준비
        cubidReward = new CubidReward(this, "u365gjTAxA");
        cubidReward.setRewardListener(new CubidRewardListener() {
            @Override
            public void onLoad() {
                btnShow.setEnabled(true);
                rewardStatusView.setState(AdStatus.AD_LOADED);
            }

            @Override
            public void onLoadFailed(CubidError error) {
                failedMessage[0] = "[" + error.getCode() + "]\n" + error.getMessage();
                rewardStatusView.setState(AdStatus.FAILED);
            }

            @Override
            public void onSkip() {
                rewardStatusView.setState(AdStatus.AD_SKIP);
            }

            @Override
            public void onComplete() {
                rewardStatusView.setState(AdStatus.AD_COMPLETE);
            }

            @Override
            public void onShow() {
                btnShow.setEnabled(false);
                rewardStatusView.setState(AdStatus.AD_SHOW);
            }

            @Override
            public void onClick() {
                rewardStatusView.setState(AdStatus.AD_CLICK);
            }

            @Override
            public void onClose() {
                rewardStatusView.setState(AdStatus.AD_CLOSE);
            }
        });

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rewardStatusView.setState(AdStatus.AD_REQ);
                if (cubidReward != null) {
                    // 광고 로드
                    cubidReward.load();
                }
            }
        });

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cubidReward != null) {
                    // 광고 화면 팝업
                    cubidReward.showAd(MainActivity.this);
                }
            }
        });

        rewardStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPopMessage(rewardStatusView.getState(), failedMessage[0]);
            }
        });
    }

    private void initInterstitial() {
        View interstitialControlBox = findViewById(R.id.box_interstitial);
        View interstitialButtonBox = interstitialControlBox.findViewById(R.id.box_btns);
        StatusView interstitialStatusView = interstitialControlBox.findViewById(R.id.box_status);
        ToggleButton interstitialToggleButton = interstitialControlBox.findViewById(R.id.arrow_ad_function);
        EtcInteraction.setToggleViewInteraction(interstitialToggleButton, interstitialButtonBox);

        Button btnLoad = interstitialButtonBox.findViewById(R.id.btn_load);
        Button btnShow = interstitialButtonBox.findViewById(R.id.btn_show);

        final String[] failedMessage = {""};

        interstitialStatusView.setState(AdStatus.AD_DEFAULT);
        interstitialStatusView.setVisibility(View.VISIBLE);

        // 광고 인스턴스 준비
        cubidInterstitial = new CubidInterstitial(this, "5jd1pALQ5Z");
        cubidInterstitial.setInterstitialListener(new CubidInterstitialListener() {
            @Override
            public void onLoaded() {
                btnShow.setEnabled(true);
                interstitialStatusView.setState(AdStatus.AD_LOADED);
            }

            @Override
            public void onLoadFailed(CubidError error) {
                failedMessage[0] = "[" + error.getCode() + "]\n" + error.getMessage();
                interstitialStatusView.setState(AdStatus.FAILED);
            }

            @Override
            public void onShow() {
                btnShow.setEnabled(false);
                interstitialStatusView.setState(AdStatus.AD_SHOW);
            }

            @Override
            public void onClick() {
                interstitialStatusView.setState(AdStatus.AD_CLICK);
            }

            @Override
            public void onClose() {
                interstitialStatusView.setState(AdStatus.AD_CLOSE);
            }
        });

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interstitialStatusView.setState(AdStatus.AD_REQ);
                if (cubidInterstitial != null) {
                    // 광고 로드
                    cubidInterstitial.load();
                }
            }
        });

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cubidInterstitial != null) {
                    // 광고 화면 팝업
                    cubidInterstitial.showAd(MainActivity.this);
                }
            }
        });

        interstitialStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPopMessage(interstitialStatusView.getState(), failedMessage[0]);
            }
        });
    }

    private void checkPopMessage(AdStatus status, String msg) {
        String message = null;
        if (status == AdStatus.FAILED) {
            message = msg;
        } else if (status == AdStatus.AD_DEFAULT) {
            message = "실패 사유에 대한 메시지가 표시되는 곳입니다.";
        }
        if (message != null) {
            EtcInteraction.popMessageDialog(this, message);
        }
    }

    private void setSystemLayout() {
        View mainLayout = findViewById(R.id.main_box);
        BaseSystemSetting.setInsetDirection(mainLayout);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    private void release() {
        if (cubidBanner != null) cubidBanner.destroy();
        if (cubidNative != null) cubidNative.destroy();
        if (cubidInterstitial != null) cubidInterstitial.destroy();
        if (cubidReward != null) cubidReward.destroy();

        cubidBanner = null;
        cubidNative = null;
        cubidInterstitial = null;
        cubidReward = null;
    }
}