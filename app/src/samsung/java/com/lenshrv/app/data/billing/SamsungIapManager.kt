package com.lenshrv.app.data.billing

import android.content.Context
import com.samsung.android.sdk.iap.lib.constants.HelperDefine
import com.samsung.android.sdk.iap.lib.helper.IapHelper
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SamsungIapManager @Inject constructor(
    private val context: Context
) {
    private val iapHelper: IapHelper = IapHelper.getInstance(context)

    init {

        iapHelper.setOperationMode(HelperDefine.OperationMode.OPERATION_MODE_TEST)
    }
}
