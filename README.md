# CuBid SDK For Android
## 변경사항
- [버전 변경 기록](https://github.com/rnd-adforus/CubidSDKSample/releases)

## 지원
- 기본 요건
- Gradle 7.0.1 이상 <br>
- minSdkVersion 21 이상 <br>
- OS 지원 Android 8.0 이상 <br>

## 연동 가이드

### 바로가기
1. [Configuration](#chapter-1)
2. [AndroidManifest.xml](#chapter-2)
3. [SDK 초기화](#chapter-3)
4. [광고 호출](#chapter-4)  
   4-1. [배너](#chapter-4-1)  
   4-2. [네이티브](#chapter-4-2)  
   4-3. [리워드](#chapter-4-3)  
   4-4. [전면](#chapter-4-4)  

---  

### 1. Configuration 설정 <a id="chapter-1"/>
Gradle 설정 파일에 maven 레포지토리 다음의 설정을 추가하여 주세요. <br>
#### 1-1. Gradle Settings
```kotlin
// [settings.gradle.kts] - Kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://artifact.bytedance.com/repository/pangle/")
        }
        maven {
            url = uri("https://nexus.adforus.com/repository/cubid")
        }
    }
}
```
###### OR
```groovy
// [settings.gradle] - Groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url "https://artifact.bytedance.com/repository/pangle/"
        }
        maven {
            url "https://nexus.adforus.com/repository/cubid"
        }
    }
}
``` 
</br>

#### 1-2. build file (app-level)
앱 수준의 `build.gradle` 또는 `build.gradle.kts` 파일에 **Cubid SDK 및 필요한 Adapter SDK 의존성**을 추가해 주세요. `cubid` 모듈은 필수(Core) 의존성이며, 광고 기능을 활성화하려면 `adsu`, `cubex`, `upan` 중 **하나 이상의 Adapter 모듈을 함께 추가해야 합니다**.

```kotlin
//[build.gradle.kts] - Kotlin
dependencies {
   implementation("com.adforus.sdk:cubid:1.2.0") // Core Module - required
   implementation("com.adforus.sdk:cubex:1.3.0") // Adapter Module
   implementation("com.adforus.sdk:adsu:2.1.0") // Adapter Module
   implementation("com.adforus.sdk:upan:1.1.0") // Adapter Module
}
```
###### OR
```groovy
//[build.gradle] - Groovy
dependencies {
   implementation 'com.adforus.sdk:cubid:1.2.0' // Core Module - required
   implementation 'com.adforus.sdk:cubex:1.3.0' // Adapter Module 
   implementation 'com.adforus.sdk:adsu:2.1.0' // Adapter Module
   implementation 'com.adforus.sdk:upan:1.1.0' // Adapter Module
}
```

#  

### 2. AndroidManifest.xml <a id="chapter-2"/>
`adsu` 모듈을 사용하는 경우, 앱의 `AndroidManifest.xml` 파일에 반드시 AdMob App ID를 설정해야 합니다. 아래와 같이 `<meta-data>` 태그를 추가해 주세요:
- `android:value`에는 AdForus 운영팀으로부터 발급받은 **AdMob App ID**를 입력해야 합니다.
- App ID가 누락되거나 잘못된 경우, 광고가 정상적으로 초기화되지 않으며 **광고 수익화가 불가능합니다.**
- 본 설정은 **Google AdMob의 정책상 필수 항목**이며, 누락 시 앱 실행 중 예외 또는 크래시가 발생할 수 있습니다.

```xml
<!-- AndroidManifest.xml -->
<manifest>
  <application>
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="YOUR_APP_ID"
        tools:replace="android:value"/>
  </application>
</manifest>
```

# 

### 3. SDK 초기화 <a id="chapter-3"/>
광고를 호출하기 전에 반드시 `CuBidSettings.initialize()` 메서드를 호출하여 SDK를 초기화해야 합니다.  
이 메서드는 SDK 초기화를 수행하며, 성공 또는 실패 여부를 콜백을 통해 전달합니다.
>⚠️ 주의: `adsu`, `cubex`, `upan` 중 하나 이상의 Adapter 라이브러리가 프로젝트에 포함되어 있지 않거나,  
초기화 과정이 12초 이상 지연될 경우 **초기화가 실패할 수 있습니다.**

```kotlin
//Kotlin
CuBidSettings.initialize(context = this, setId = "YOUR_SET_ID", userId = "USER_ID", 
	listener = object : CuBidInitListener {
        override fun onSuccess() {
           // 초기화 성공
        }

        override fun onFailed(message: String) {
            // 초기화 실패
        }
    }
)
```
###### OR
```java
//Java
CuBidSettings.initialize(
    this,             // 또는 getApplicationContext()
    "YOUR_SET_ID",    // 운영팀으로부터 발급받은 Set Id 입력
    "USER_ID",        // 사용자 고유 ID 또는 테스트용 ID
    new CuBidInitListener() {
        @Override
        public void onSuccess() {
            // 초기화 성공
        }

        @Override
        public void onFailed(String message) {
            // 초기화 실패
        }
    }
);
```

#   

### 4. 광고 호출 <a id="chapter-4"/>
### 4-1. 배너 <a id="chapter-4-1">
#### [광고 로드 및 콜백 리스너 설정]
`CubidBanner`는 `FrameLayout`을 상속한 광고 컨테이너이며,  `loadAd()` 호출 이후 `onLoad()` 콜백 시점에 광고 뷰가 내부에 자동으로 주입됩니다.
광고뷰를 화면에 표시하려면 **`onLoad()` 콜백 이후에** `CubidBanner` 인스턴스를 레이아웃에 추가해 주세요.

```kotlin
//Kotlin
cubidBanner = CubidBanner(context = this, placementId = "YOUR_PLACEMENT_ID")
cubidBanner?.setBannerListener(listener = object : CubidBannerListener {
    override fun onLoad() {
        // 광고 로드 성공
        bannerLoadView = cubidBanner
    }

    override fun onLoadFail(errorInfo: CubidError) {
        // 광고 로드 실패
    }
    
    override fun onClick() {
        // 광고 클릭
    }
   
    override fun onDisplay() {
       // 광고 표시
    }
})

cubidBanner?.setSize(CubidSize.TYPE_320X50) 
cubidBanner?.loadAd()
```
###### OR
```java
//Java
cubidBanner = new CubidBanner(this, "YOUR_PLACEMENT_ID");
cubidBanner.setBannerListener(new CubidBannerListener() {
    @Override
    public void onLoad() {
        // 광고 로드 성공
        bannerLoadView = cubidBanner;
    }

    @Override
    public void onLoadFail(CubidError errorInfo) {
        // 광고 로드 실패
    }

    @Override
    public void onClick() {
        // 광고 클릭
    }
   @Override
   public void onDisplay() {
       // 광고 표시
   }
});
cubidBanner.setSize(CubidSize.TYPE_320X50);
cubidBanner.loadAd();
```
<br>

#### [레이아웃에 광고 게제하기]
`CubidBanner` 인스턴스를 원하는 레이아웃에 추가하여 배너 광고를 화면에 표시할 수 있습니다.

```kotlin
//Kotlin
container.removeAllViews()  
container.addView(bannerLoadView)
```
###### OR
```java
//Java
container.removeAllViews();  
container.addView(bannerLoadView);
```
<br>

#### [광고 종료 및 자원 해제]
배너 광고를 끝내고 싶을 때, 배너 광고 자원을 해제 할 수 있습니다. 뷰계층에서의 참조 관계를 끊어내고, 배너 광고 인스턴스의 `destroy()` 메소드를 사용하여 내부 동작을 중단하세요.
```kotlin
//Kotlin
(bannerLoadView.parent as? ViewGroup)?.removeView(bannerLoadView)
bannerLoadView.destroy()
bannerLoadView = null
```
###### OR
```java
//Java
if(bannerLoadView.getParent() instanceof ViewGroup) {
	((ViewGroup)bannerLoadView.getParent()).removeView(bannerLoadView);  
}
bannerLoadView.destroy();
bannerLoadView = null;
```

<br>

### 4-2. 네이티브 <a id="chapter-4-2">
**네이티브 광고는 커스텀 XML 레이아웃에 광고 구성 요소를 자유롭게 배치할 수 있는 광고 형식입니다.**

#### [광고 로드 및 콜백 리스너 설정]
광고 호출 전, `CubidNative.Builder`를 사용하여 `CubidNative` 인스턴스를 생성하세요.  
`Builder`의 메서드 체이닝을 통해 XML에 정의된 광고 요소(`Media`, `Headline`, `CTA` 등)의 View ID를 지정할 수 있습니다.

📌 _광고 필수 요소가 누락된 경우, 광고는 정상적으로 로드되지 않습니다._  
커스텀 [XML 레이아웃 구성 예시](https://github.com/rnd-adforus/CubidSDKSample/blob/master/app/src/main/res/layout/layout_custom_native.xml)는 샘플 프로젝트 내에서 확인할 수 있습니다.

`CubidNative` 인스턴스의 `loadAd()` 메서드를 호출하면, 광고 로드 성공 시 `CubidNativeAdListener`의 `onLoaded(View)` 콜백이 호출되며 광고 뷰가 전달됩니다.  
해당 뷰를 원하는 레이아웃에 직접 추가하여 네이티브 광고를 화면에 표시할 수 있습니다.
```kotlin
//Kotlin
cubidNative = CubidNative.Builder(context = this, placement = "YOUR_PLACEMENT_ID")
    .setNativeLayoutID(R.layout.layout_custom_native)   // 필수: 네이티브 광고 레이아웃
    .setAdMediaID(R.id.item_main)                       // 필수: 미디어 뷰 ID
    .setAdHeadLineID(R.id.item_headline)                // 필수: 광고 제목 텍스트 ID
    .setAdCallToActionID(R.id.item_action)              // 필수: CTA 버튼 ID
    .setAdIconID(R.id.item_icon)                        // 선택: 아이콘 이미지뷰 ID
    .setAdBodyID(R.id.item_body)                        // 선택: 본문 텍스트 ID
    .build()

cubidNative?.setNativeListener(listener = object : CubidNativeAdListener {
    override fun onLoaded(view: CubidNativeView) {
       // 광고 로드 성공
        nativeLoadView = view
    }
    
    override fun onLoadFailed(error: CubidError) {
        // 광고 로드 실패
    }

    override fun onClick() {
       // 광고 클릭
    }

    override fun onImpression() {
       // 광고 게제
    }
})
cubidNative?.loadAd()
```
###### OR
```java
//Java
cubidNative = new CubidNative.Builder(this, "YOUR_PLACEMENT_ID")
    .setNativeLayoutID(R.layout.layout_custom_native) // 필수: 네이티브 광고 레이아웃
    .setAdMediaID(R.id.item_main)                    // 필수: 미디어 뷰 ID
    .setAdHeadLineID(R.id.item_headline)             // 필수: 제목 텍스트 ID
    .setAdCallToActionID(R.id.item_action)           // 필수: CTA 버튼 ID
    .setAdIconID(R.id.item_icon)                     // 선택: 아이콘 이미지 뷰 ID
    .setAdBodyID(R.id.item_body)                     // 선택: 본문 텍스트 ID
    .build();

cubidNative.setNativeListener(new CubidNativeAdListener() {
    @Override
    public void onLoaded(CubidNativeView view) {
        // 광고 로드 성공
        nativeLoadView = view;
    }

    @Override
    public void onLoadFailed(CubidError error) {
        // 광고 로드 실패
    }

    @Override
    public void onClick() {
        // 광고 클릭
    }

    @Override
    public void onImpression() {
        // 광고 게제
    }
});

cubidNative.loadAd();
```
<br>

#### [레이아웃에 광고 게제하기]
`onLoaded(View)` 콜백으로 전달 받은 뷰를 원하는 레이아웃에 추가하여 네이티브 광고를 화면에 표시할 수 있습니다.
```kotlin
//Kotlin
container.removeAllViews()  
container.addView(nativeLoadView)
```
###### OR
```java
//Java
container.removeAllViews();  
container.addView(nativeLoadView);
```
<br>

#### [광고 종료 및 자원 해제]
네이티브 광고를 종료하고 싶을 때, 네이티브 광고 자원을 해제 할 수 있습니다. 뷰계층에서의 참조 관계를 끊어내고, 네이티브 광고 인스턴스의 `destroy()` 메소드를 사용하여 내부 동작을 중단하세요.
```kotlin
//Kotlin
(nativeLoadView.parent as? ViewGroup)?.removeView(nativeLoadView)
cubidNative.destroy()
cubidNative = null
```
###### OR
```java
//Java
if(nativeLoadView.getParent() instanceof ViewGroup) {
	((ViewGroup)nativeLoadView.getParent()).removeView(nativeLoadView);  
}
cubidNative.destroy();
cubidNative = null;
```

<br>

### 4-3. 리워드 <a id="chapter-4-3">
#### [광고 로드 및 콜백 리스너 설정] <a id="chapter-4-3-1">
**리워드 광고는 `CubidReward` 인스턴스를 통해 게재할 수 있습니다.**

인스턴스를 생성할 때, **Context는 `ApplicationContext` 사용을 권장**합니다.  
이렇게 생성된 인스턴스는 `showAd(Activity)` 호출 위치에 관계없이 광고가 **독립적으로 표시**되도록 보장합니다.

광고를 로드하려면 `load()` 메소드를 호출하세요.  
로드가 완료되면 `CubidRewardListener`의 `onLoad()` 콜백이 호출되며,  
이후 `showAd(Activity)`를 통해 광고를 표시할 수 있습니다.
```kotlin
//Kotlin
cubidReward = CubidReward(context = this, placementId = "u365gjTAxA")
cubidReward?.setRewardListener(listener = object : CubidRewardListener {
    override fun onLoad() {
       // 광고 로드 성공
    }
    override fun onLoadFailed(error: CubidError) {
       // 광고 로드 실패
    }
    override fun onSkip() {
       // 광고 보기 완료 실패
    }
    override fun onComplete() {
       // 광고 보기 완료 성공
    }
    override fun onShow() {
       // 광고 보기
    }
    override fun onClick() {
        // 광고 클릭
    }
    override fun onClose() {
        // 광고 닫기
    }
})
cubidReward?.load()
```
###### OR
```java
//Java
cubidReward = new CubidReward(this, "u365gjTAxA");
cubidReward.setRewardListener(new CubidRewardListener() {
    @Override
    public void onLoad() {
        // 광고 로드 성공
    }
    @Override
    public void onLoadFailed(CubidError error) {
        // 광고 로드 실패
    }
    @Override
    public void onSkip() {
        // 광고 보기 완료 실패
    }
    @Override
    public void onComplete() {
        // 광고 보기 완료 성공
    }
    @Override
    public void onShow() {
        // 광고 보기
    }
    @Override
    public void onClick() {
        // 광고 클릭
    }
    @Override
    public void onClose() {
        // 광고 닫기
    }
});
cubidReward.load();
```
<br>

#### [광고 게제]
**리워드 광고를 표시하려면 먼저 [광고 로드 및 콜백 리스너 설정](#chapter-4-3-1)을 완료한 뒤, `onLoad()` 콜백을 수신해야 합니다.**  
광고가 성공적으로 로드된 후에만 `CubidReward` 인스턴스의 `showAd(Activity)` 메소드를 호출할 수 있습니다.
> ⚠️ `showAd()`는 반드시 `Activity`를 인자로 전달해야 정상적으로 동작합니다.

```kotlin
//Kotlin
cubidReward?.showAd(activity = this)
```
###### OR
```java
//Java
cubidReward.showAd(this);
```
<br>

#### [광고 종료 및 자원 해제]
리워드 광고를 종료하고 싶을 때, 광고 인스턴스의 `destroy()` 메소드를 사용하여 내부 동작을 중단하세요.
```kotlin
//Kotlin
cubidReward.destroy()
cubidReward = null
```
###### OR
```java
//Java
cubidReward.destroy();
cubidReward = null;
```
<br>

### 4-4. 전면 <a id="chapter-4-4">
#### [광고 로드 및 콜백 리스너 설정] <a id="chapter-4-4-1">
**전면 광고는 `CubidInterstitial` 인스턴스를 통해 게재할 수 있습니다.**

인스턴스를 생성할 때, **Context는 `ApplicationContext` 사용을 권장**합니다.  
이렇게 생성된 인스턴스는 `showAd(Activity)` 호출 위치에 관계없이 광고가 **독립적으로 표시**되도록 보장합니다.

광고를 로드하려면 `load()` 메소드를 호출하세요.  
로드가 완료되면 `CubidInterstitialListener`의 `onLoad()` 콜백이 호출되며,  
이후 `showAd(Activity)`를 통해 광고를 표시할 수 있습니다.
```kotlin
//Kotlin
cubidInterstitial = CubidInterstitial(context = this, placementId = "5jd1pALQ5Z")
cubidInterstitial?.setInterstitialListener(listener = object : CubidInterstitialListener {
    override fun onLoaded() {
        // 광고 로드 성공
    }
    override fun onLoadFailed(error: CubidError) {
       // 광고 로드 실패
    }
    override fun onShow() {
       // 광고 보기
    }
    override fun onClick() {
       // 광고 클릭
    }
    override fun onClose() {
       // 광고 닫기
    }
})
```
###### OR
```java
//Java
cubidInterstitial = new CubidInterstitial(this, "5jd1pALQ5Z");
cubidInterstitial.setInterstitialListener(new CubidInterstitialListener() {
    @Override
    public void onLoaded() {
        // 광고 로드 성공
    }
    @Override
    public void onLoadFailed(CubidError error) {
        // 광고 로드 실패
    }
    @Override
    public void onShow() {
        // 광고 보기
    }
    @Override
    public void onClick() {
        // 광고 클릭
    }
    @Override
    public void onClose() {
        // 광고 닫기
    }
});
```

#### [광고 게제]
**전면 광고를 표시하려면 먼저 [광고 로드 및 콜백 리스너 설정](#chapter-4-4-1)을 완료한 뒤, `onLoad()` 콜백을 수신해야 합니다.**  
광고가 성공적으로 로드된 후에만 `CubidInterstitial` 인스턴스의 `showAd(Activity)` 메소드를 호출할 수 있습니다.
> ⚠️ `showAd()`는 반드시 `Activity`를 인자로 전달해야 정상적으로 동작합니다.

```kotlin
//Kotlin
cubidInterstitial?.showAd(activity = this)
```
###### OR
```java
//Java
cubidInterstitial.showAd(this);
```
<br>

#### [광고 종료 및 자원 해제]
전면 광고를 종료하고 싶을 때, 광고 인스턴스의 `destroy()` 메소드를 사용하여 내부 동작을 중단하세요.
```kotlin
//Kotlin
cubidInterstitial.destroy()
cubidInterstitial = null
```
###### OR
```java
//Java
cubidInterstitial.destroy()
cubidInterstitial = null
```
<br>


### 기타 플렛폼 지원
- [(준비중) Flutter]()
- [(준비중 )ReactNative]()
