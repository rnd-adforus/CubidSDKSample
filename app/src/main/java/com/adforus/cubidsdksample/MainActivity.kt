package com.adforus.cubidsdksample

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import com.adforus.sdk.cubid.CuBidInitListener
import com.adforus.sdk.cubid.CuBidSettings
import com.adforus.sdk.cubid.util.CubidError
import com.adforus.sdk.cubid.view.CubidBanner
import com.adforus.sdk.cubid.view.CubidBannerListener
import com.adforus.sdk.cubid.view.CubidInterstitial
import com.adforus.sdk.cubid.view.CubidInterstitialListener
import com.adforus.sdk.cubid.view.CubidNative
import com.adforus.sdk.cubid.view.CubidNativeAdListener
import com.adforus.sdk.cubid.view.CubidNativeView
import com.adforus.sdk.cubid.view.CubidReward
import com.adforus.sdk.cubid.view.CubidRewardListener
import com.adforus.sdk.cubid.view.CubidSize
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
class MainActivity : AppCompatActivity() {

    private var cubidBanner : CubidBanner? = null
    private var cubidNative : CubidNative? = null
    private var cubidInterstitial : CubidInterstitial? = null
    private var cubidReward : CubidReward? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSystemLayout()

        // 화면 회전, 전환 등 구성 변경(configuration change)시에는 메모리 누수를 예방하기 위해 반드시 광고 인스턴스를 초기화하고 새로운 인스턴스를 사용하여 광고를 재 호출하여 사용할 것을 권장합니다.
        // release()

        val initControlBox = findViewById<View>(R.id.box_init)
        val initButton = initControlBox.findViewById<Button>(R.id.btn_init)

        val extendBox = initControlBox.findViewById<View>(R.id.box_init_btns)

        val statusInfoView = extendBox.findViewById<StatusView>(R.id.include_status_view)
        val debugButton = extendBox.findViewById<SwitchCompat>(R.id.btn_debug)

        var failedMessage = ""
        statusInfoView.apply {
            state = AdStatus.AD_DEFAULT
            visibility = View.VISIBLE
        }

        initButton.setOnClickListener {
            //initialize - 초기화
            // 초기화 성공 콜백에서 광고 로드 시작, 초기화 성공 콜백 호출 전 만든 모든 광고 인스턴스는 유효하지 않습니다.
            CuBidSettings.initialize(this@MainActivity, "OQf8T68ys9", "test-cubider", object : CuBidInitListener {
                override fun onSuccess() {
                    runOnUiThread {
                        statusInfoView.setStatus(AdStatus.INIT_SUCCESS)
                        initAds()
                    }
                }

                override fun onFailed(message: String) {
                    runOnUiThread {
                        failedMessage = message
                        statusInfoView.setStatus(AdStatus.FAILED)
                    }
                }
            })
        }

        // 디버깅 로그 활성화 합니다.
        CuBidSettings.setDebugMode(debugButton.isChecked)
        debugButton.setOnCheckedChangeListener { buttonView, isChecked ->
            CuBidSettings.setDebugMode(isChecked)
        }

        statusInfoView.setOnClickListener {
            checkPopMessage(statusInfoView, failedMessage)
        }
    }

    private fun initAds() {
        //banner
        initBanner()
        //native
        initNative()
        //interstitial
        initInterstitial()
        //reward
        initReward()
    }

    private fun initBanner() {
        val bannerControlBox = findViewById<View>(R.id.box_banner)
        val bannerSizeRadioGroup = bannerControlBox.findViewById<RadioGroup>(R.id.size_radio_button)
        val bannerFunctionBox = bannerControlBox.findViewById<View>(R.id.box_function_views)
        val bannerButtonBox = bannerControlBox.findViewById<View>(R.id.box_btns_and_view)
        val bannerStatusView = bannerControlBox.findViewById<StatusView>(R.id.box_status)
        val bannerToggleButton = bannerControlBox.findViewById<ToggleButton>(R.id.arrow_ad_function)
        EtcInteraction.setToggleViewInteraction(bannerToggleButton, bannerFunctionBox)

        val bannerButtonLoad = bannerButtonBox.findViewById<Button>(R.id.btn_load)
        val bannerButtonDisplay = bannerButtonBox.findViewById<CheckBox>(R.id.btn_display)
        val bannerButtonVisible = bannerButtonBox.findViewById<CheckBox>(R.id.btn_visible)

        val container = bannerButtonBox.findViewById<LinearLayout>(R.id.ad_view_container)
        var bannerLoadView : CubidBanner? = null
        var failedMessage = ""
        bannerStatusView.apply {
            state = AdStatus.AD_DEFAULT
            visibility = View.VISIBLE
        }

        var size : CubidSize = CubidSize.TYPE_320X50
        bannerSizeRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            size = when(checkedId) {
                R.id.radio_320X100 -> CubidSize.TYPE_320X100
                R.id.radio_300X250 -> CubidSize.TYPE_300X250
                else -> CubidSize.TYPE_320X50
            }
        }

        // 배너 광고 인스턴스 준비
        cubidBanner = CubidBanner(this@MainActivity, "uyXywuOXTt")
        cubidBanner?.setBannerListener(listener = object : CubidBannerListener {
            override fun onLoad() {
                bannerStatusView.setStatus(AdStatus.AD_LOADED)
                bannerLoadView = cubidBanner

                bannerButtonDisplay.isEnabled = true
                bannerButtonVisible.apply {
                    isChecked = bannerLoadView?.isVisible ?: false
                    isEnabled = true
                    text = if(isVisible) {
                        resources.getText(R.string.hide)
                    }else {
                        resources.getText(R.string.show)
                    }
                }
            }

            override fun onLoadFail(errorInfo: CubidError) {
                failedMessage = "[${errorInfo.getCode()}]\n${errorInfo.getMessage()}"
                bannerStatusView.setStatus(AdStatus.FAILED)
            }

            override fun onClick() {
                bannerStatusView.setStatus(AdStatus.AD_CLICK)
            }

            override fun onDisplay() {
                bannerStatusView.setStatus(AdStatus.AD_DISPLAY)
            }
        })

        bannerButtonLoad.setOnClickListener {
            bannerStatusView.setStatus(AdStatus.AD_REQ)
            // 배너 사이즈 설정
            cubidBanner?.setSize(size)
            // 배너 광고 로드
            cubidBanner?.loadAd()
        }


        // 로드 된 광고 뷰 조작 예시
        bannerButtonDisplay.setOnCheckedChangeListener { buttonView, isChecked ->
            val _bannerLoadView = bannerLoadView ?: return@setOnCheckedChangeListener
            if(isChecked) {
                container.removeAllViews()
                container.addView(_bannerLoadView)
                bannerButtonDisplay.text = resources.getText(R.string.remove)
                bannerStatusView.setStatus(AdStatus.AD_DISPLAY)
            }else {
                (_bannerLoadView.parent as? ViewGroup)?.apply {
                    this.removeView(_bannerLoadView)
                    bannerButtonDisplay.text = resources.getText(R.string.display)
                    bannerStatusView.setStatus(AdStatus.AD_REMOVE)
                }
            }
        }

        bannerButtonVisible.setOnCheckedChangeListener { buttonView, isChecked ->
            val _bannerLoadView = bannerLoadView ?: return@setOnCheckedChangeListener
            if(isChecked) {
                _bannerLoadView.visibility = View.VISIBLE
                bannerButtonVisible.text = resources.getText(R.string.hide)
                bannerStatusView.setStatus(AdStatus.AD_SHOW)
            }else {
                _bannerLoadView.visibility = View.INVISIBLE
                bannerButtonVisible.text = resources.getText(R.string.show)
                bannerStatusView.setStatus(AdStatus.AD_HIDE)
            }
        }

        bannerStatusView.setOnClickListener {
            checkPopMessage(bannerStatusView, failedMessage)
        }
    }

    private fun initNative(){
        val nativeControlBox = findViewById<View>(R.id.box_native)
        val nativeButtonBox = nativeControlBox.findViewById<View>(R.id.box_btns_and_view)
        val nativeStateView = nativeControlBox.findViewById<StatusView>(R.id.box_status)
        val nativeToggleButton = nativeControlBox.findViewById<ToggleButton>(R.id.arrow_ad_function)
        EtcInteraction.setToggleViewInteraction(nativeToggleButton, nativeButtonBox)

        val btnLoad = nativeButtonBox.findViewById<Button>(R.id.btn_load)
        val btnDisplay = nativeButtonBox.findViewById<CheckBox>(R.id.btn_display)
        val btnVisible = nativeButtonBox.findViewById<CheckBox>(R.id.btn_visible)

        val container = nativeButtonBox.findViewById<LinearLayout>(R.id.ad_view_container)

        var nativeLoadView : View? = null
        var failedMessage = ""
        nativeStateView.apply {
            state = AdStatus.AD_DEFAULT
            visibility = View.VISIBLE
        }

        // 네이티브 광고 인스턴스 준비 - 필수 값은 반드시 유효한 id로 입력 하여 주세요.
        cubidNative = CubidNative.Builder(this@MainActivity, "5xJlpEi80m")
            .setNativeLayoutID(R.layout.layout_custom_native) // 필수
            .setAdMediaID(R.id.item_main) // 필수
            .setAdHeadLineID(R.id.item_headline) // 필수
            .setAdCallToActionID(R.id.item_action) // 필수
            .setAdIconID(R.id.item_icon)
            .setAdBodyID(R.id.item_body)
            .build()

        cubidNative?.setNativeListener(listener = object : CubidNativeAdListener {

            override fun onLoaded(view: CubidNativeView) {
                btnDisplay.isEnabled = true
                btnVisible.apply {
                    isEnabled = true
                    text = if(isVisible) {
                        resources.getText(R.string.hide)
                    }else {
                        resources.getText(R.string.show)
                    }
                }
                nativeLoadView = view
                btnVisible.isChecked = view.isVisible
                nativeStateView.setStatus(AdStatus.AD_LOADED)
            }

            override fun onLoadFailed(error: CubidError) {
                failedMessage = "[${error.getCode()}]\n${error.getMessage()}"
                nativeStateView.setStatus(AdStatus.FAILED)
            }

            override fun onClick() {
                nativeStateView.setStatus(AdStatus.AD_CLICK)
            }

            override fun onImpression() {
                nativeStateView.setStatus(AdStatus.AD_DISPLAY)
            }
        })

        btnLoad.setOnClickListener {
            nativeStateView.setStatus(AdStatus.AD_REQ)
            // 네이티브 광고 로드
            cubidNative?.loadAd()
        }

        // 로드 된 광고 뷰 조작 예시
        btnDisplay.setOnClickListener {
            val nativeView = nativeLoadView ?: return@setOnClickListener
            (nativeView.parent as? ViewGroup)?.apply {
                this.removeView(nativeView)
                btnDisplay.text = resources.getText(R.string.display)
                nativeStateView.setStatus(AdStatus.AD_REMOVE)
            } ?: run {
                container.removeAllViews()
                container.addView(nativeView)
                btnDisplay.text = resources.getText(R.string.remove)
                nativeStateView.setStatus(AdStatus.AD_DISPLAY)
            }
        }

        btnVisible.setOnClickListener {
            val nativeView = nativeLoadView ?: return@setOnClickListener
            if(nativeView.isVisible) {
                nativeView.visibility = View.INVISIBLE
                btnVisible.text = resources.getText(R.string.show)
                nativeStateView.setStatus(AdStatus.AD_HIDE)
            }else {
                nativeView.visibility = View.VISIBLE
                btnVisible.text = resources.getText(R.string.hide)
                nativeStateView.setStatus(AdStatus.AD_SHOW)
            }
        }

        nativeStateView.setOnClickListener {
            checkPopMessage(nativeStateView, failedMessage)
        }
    }

    private fun initReward(){
        val rewardControlBox = findViewById<View>(R.id.box_reward)
        val rewardButtonBox = rewardControlBox.findViewById<View>(R.id.box_btns)
        val rewardStatusView = rewardControlBox.findViewById<StatusView>(R.id.box_status)
        val rewardToggleButton = rewardControlBox.findViewById<ToggleButton>(R.id.arrow_ad_function)

        EtcInteraction.setToggleViewInteraction(rewardToggleButton, rewardButtonBox)

        val btnLoad = rewardButtonBox.findViewById<Button>(R.id.btn_load)
        val btnShow = rewardButtonBox.findViewById<Button>(R.id.btn_show)

        var failedMessage = ""
        rewardStatusView.apply {
            state = AdStatus.AD_DEFAULT
            visibility = View.VISIBLE
        }

        // 광고 인스턴스 준비
        cubidReward = CubidReward(this@MainActivity, "u365gjTAxA")
        cubidReward?.setRewardListener(listener = object : CubidRewardListener {
            override fun onLoad() {
                btnShow.isEnabled = true
                rewardStatusView.setStatus(AdStatus.AD_LOADED)
            }

            override fun onLoadFailed(error: CubidError) {
                failedMessage = "[${error.getCode()}]\n${error.getMessage()}"
                rewardStatusView.setStatus(AdStatus.FAILED)
            }

            override fun onSkip() {
                rewardStatusView.setStatus(AdStatus.AD_SKIP)
            }

            override fun onComplete() {
                rewardStatusView.setStatus(AdStatus.AD_COMPLETE)
            }

            override fun onShow() {
                btnShow.isEnabled = false
                rewardStatusView.setStatus(AdStatus.AD_SHOW)
            }

            override fun onClick() {
                rewardStatusView.setStatus(AdStatus.AD_CLICK)
            }

            override fun onClose() {
                rewardStatusView.setStatus(AdStatus.AD_CLOSE)
            }
        })

        btnLoad.setOnClickListener {
            rewardStatusView.setStatus(AdStatus.AD_REQ)
            // 광고 로드
            cubidReward?.load()
        }

        btnShow.setOnClickListener {
            // 광고 화면 팝업
            cubidReward?.showAd(this@MainActivity)
        }

        rewardStatusView.setOnClickListener {
            checkPopMessage(rewardStatusView, failedMessage)
        }
    }

    private fun initInterstitial() {
        val interstitialControlBox = findViewById<View>(R.id.box_interstitial)
        val interstitialButtonBox = interstitialControlBox.findViewById<View>(R.id.box_btns)
        val interstitialStatusView = interstitialControlBox.findViewById<StatusView>(R.id.box_status)
        val interstitialToggleButton = interstitialControlBox.findViewById<ToggleButton>(R.id.arrow_ad_function)
        EtcInteraction.setToggleViewInteraction(interstitialToggleButton, interstitialButtonBox)

        val btnLoad = interstitialButtonBox.findViewById<Button>(R.id.btn_load)
        val btnShow = interstitialButtonBox.findViewById<Button>(R.id.btn_show)

        var failedMessage = ""
        interstitialStatusView.apply {
            state = AdStatus.AD_DEFAULT
            visibility = View.VISIBLE
        }

        // 광고 인스턴스 준비
        cubidInterstitial = CubidInterstitial(this@MainActivity, "5jd1pALQ5Z")
        cubidInterstitial?.setInterstitialListener(listener = object : CubidInterstitialListener {
            override fun onLoaded() {
                btnShow.isEnabled = true
                interstitialStatusView.setStatus(AdStatus.AD_LOADED)
            }

            override fun onLoadFailed(error: CubidError) {
                failedMessage = "[${error.getCode()}]\n${error.getMessage()}"
                interstitialStatusView.setStatus(AdStatus.FAILED)
            }

            override fun onShow() {
                btnShow.isEnabled = false
                interstitialStatusView.setStatus(AdStatus.AD_SHOW)
            }

            override fun onClick() {
                interstitialStatusView.setStatus(AdStatus.AD_CLICK)
            }

            override fun onClose() {
                interstitialStatusView.setStatus(AdStatus.AD_CLOSE)
            }
        })

        btnLoad.setOnClickListener {
            interstitialStatusView.setStatus(AdStatus.AD_REQ)
            // 광고 로드
            cubidInterstitial?.load()
        }

        btnShow.setOnClickListener {
            // 광고 화면 팝업
            cubidInterstitial?.showAd(this@MainActivity)
        }

        interstitialStatusView.setOnClickListener {
            checkPopMessage(interstitialStatusView, failedMessage)
        }
    }

    private fun checkPopMessage(stateView:StatusView, msg: String?)  {
        val state = (stateView.state as? AdStatus) ?: return
        val _msg = when(state) {
            AdStatus.FAILED -> {
                msg
            }
            AdStatus.AD_DEFAULT -> {
                "실패 사유에 대한 메시지가 표시되는 곳입니다."
            }
            else -> null
        }
        if(_msg != null)
            EtcInteraction.popMessageDialog(this@MainActivity, _msg)
    }

    private fun setSystemLayout() {
        val mainLayout = findViewById<View>(R.id.main_box)
        BaseSystemSetting.setInsetDirection(mainLayout)
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }

    private fun release(){
        cubidBanner?.destroy()
        cubidInterstitial?.destroy()
        cubidReward?.destroy()
        cubidNative?.destroy()

        cubidBanner = null
        cubidInterstitial = null
        cubidReward = null
        cubidNative = null
    }

}