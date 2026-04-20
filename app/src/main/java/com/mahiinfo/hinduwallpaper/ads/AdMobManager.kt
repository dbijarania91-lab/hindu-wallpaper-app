package com.mahiinfo.hinduwallpaper.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.mahiinfo.hinduwallpaper.data.model.AppConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AdMobManager"

// Test IDs — replaced automatically from Supabase AppConfig
private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
private const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"
private const val TEST_APP_OPEN = "ca-app-pub-3940256099942544/9257395921"

@Singleton
class AdMobManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var config: AppConfig? = null
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var actionsSinceLastInterstitial = 0

    private val _adsReady = MutableStateFlow(false)
    val adsReady: StateFlow<Boolean> = _adsReady

    // Called once config is fetched from Supabase
    fun initialize(appConfig: AppConfig) {
        config = appConfig
        if (!appConfig.adsEnabled) return

        MobileAds.initialize(context) { initStatus ->
            Log.d(TAG, "AdMob initialized: ${initStatus.adapterStatusMap}")
            _adsReady.value = true
            preloadInterstitial()
            preloadRewarded()
        }
    }

    // ─── IDs (live from Supabase, test fallback) ──────────────────────────────

    val bannerId: String get() = config?.admobBannerId?.ifBlank { TEST_BANNER } ?: TEST_BANNER
    val interstitialId: String get() = config?.admobInterstitialId?.ifBlank { TEST_INTERSTITIAL } ?: TEST_INTERSTITIAL
    val rewardedId: String get() = config?.admobRewardedId?.ifBlank { TEST_REWARDED } ?: TEST_REWARDED
    val nativeId: String get() = config?.admobNativeId?.ifBlank { TEST_NATIVE } ?: TEST_NATIVE
    val isEnabled: Boolean get() = config?.adsEnabled ?: false

    // ─── Interstitial ─────────────────────────────────────────────────────────

    private fun preloadInterstitial() {
        InterstitialAd.load(
            context, interstitialId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Interstitial failed: ${error.message}")
                }
            }
        )
    }

    fun showInterstitialIfReady(activity: Activity, onDismiss: () -> Unit = {}) {
        if (!isEnabled) { onDismiss(); return }
        actionsSinceLastInterstitial++
        val interval = config?.interstitialInterval ?: 3
        if (actionsSinceLastInterstitial < interval) { onDismiss(); return }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    actionsSinceLastInterstitial = 0
                    preloadInterstitial()
                    onDismiss()
                }
                override fun onAdFailedToShowFullScreenContent(e: AdError) { onDismiss() }
            }
            ad.show(activity)
        } else {
            onDismiss()
            preloadInterstitial()
        }
    }

    // ─── Rewarded ─────────────────────────────────────────────────────────────

    private fun preloadRewarded() {
        RewardedAd.load(
            context, rewardedId, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Rewarded failed: ${error.message}")
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, onRewarded: () -> Unit, onDismiss: () -> Unit = {}) {
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    preloadRewarded()
                    onDismiss()
                }
            }
            ad.show(activity) { onRewarded() }
        } ?: run { onRewarded() } // no ad? give reward anyway (user-friendly)
    }
}

// ─── Composable Banner ────────────────────────────────────────────────────────

@Composable
fun AdMobBanner(adUnitId: String) {
    AndroidView(
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
