package com.lenshrv.app.data.billing

import android.content.Context
import com.lenshrv.app.data.repository.AppPreferencesRepository
import com.samsung.android.sdk.iap.lib.helper.IapHelper
import com.samsung.android.sdk.iap.lib.vo.ErrorVo
import com.samsung.android.sdk.iap.lib.vo.OwnedProductVo
import com.samsung.android.sdk.iap.lib.vo.ProductVo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds


@Singleton
class SamsungIapManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appPreferencesRepository: AppPreferencesRepository,
) {
    private var iapHelper: IapHelper? = null
    private val _showRescueThanks = MutableStateFlow(false)
    val showRescueThanks = _showRescueThanks.asStateFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    fun dismissRescueThanks() {
        _showRescueThanks.value = false
    }

    fun initialize() {
        try {
            iapHelper = IapHelper.getInstance(context)
            if (iapHelper != null) {
                //iapHelper?.setOperationMode(HelperDefine.OperationMode.OPERATION_MODE_TEST)
                iapHelper?.getOwnedList("all") { errorVo: ErrorVo, ownedList: ArrayList<OwnedProductVo>? ->
                    if (errorVo.errorCode == IapHelper.IAP_ERROR_NONE && !ownedList.isNullOrEmpty()) {
                        val allPurchaseIds = ownedList.joinToString(",") { it.purchaseId }
                        iapHelper?.consumePurchasedItems(allPurchaseIds) { consumeErrorVo, _ ->
                            if (consumeErrorVo.errorCode == IapHelper.IAP_ERROR_NONE) {
                                scope.launch {
                                    appPreferencesRepository.setNextPromptCount(
                                        appPreferencesRepository.nextPromptCount.first() + 30,
                                    )
                                    _showRescueThanks.value = true
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {

        }
    }

    fun getListItems(onResult: (List<ProductVo>) -> Unit) {
        val helper = iapHelper
        if (helper == null) {
            onResult(emptyList())
            return
        }
        helper.getProductsDetails(
            "tip_small,tip_medium,tip_big"
        ) { errorVo, productList ->
            if (errorVo.errorCode == IapHelper.IAP_ERROR_NONE) {
                onResult(productList)
            } else {
                onResult(emptyList())
            }
        }
    }

    fun startPayment(itemId: String, onResult: (Boolean, Int) -> Unit) {
        iapHelper?.startPayment(itemId) { errorVo, purchaseVo ->
            when (errorVo.errorCode) {
                IapHelper.IAP_ERROR_NONE -> {
                    if (purchaseVo != null) {
                        iapHelper?.consumePurchasedItems(purchaseVo.purchaseId) { _, _ ->
                            onResult(true, errorVo.errorCode)
                        }
                    } else {
                        onResult(false, errorVo.errorCode)
                    }
                }

                -1002 -> {
                    scope.launch {
                        delay(4000.milliseconds)
                        iapHelper?.getOwnedList("all") { getOwnedErrorVo, ownedList ->
                            if (getOwnedErrorVo.errorCode == IapHelper.IAP_ERROR_NONE && ownedList.isNotEmpty()) {
                                val allPurchaseIds = ownedList.joinToString(",") { it.purchaseId }
                                iapHelper?.consumePurchasedItems(allPurchaseIds) { consumeErrorVo, _ ->
                                    val success =
                                        consumeErrorVo.errorCode == IapHelper.IAP_ERROR_NONE
                                    onResult(success, 0)
                                }
                            } else {
                                onResult(false, errorVo.errorCode)
                            }
                        }
                    }
                }
                else -> {
                    onResult(false, errorVo.errorCode)
                }
            }
        }
    }
}

