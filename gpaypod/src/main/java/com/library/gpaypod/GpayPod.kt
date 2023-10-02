package com.library.gpaypod

import android.app.Activity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tapandpay.TapAndPayClient
import com.google.android.gms.tapandpay.TapAndPayStatusCodes
import com.google.android.gms.tapandpay.issuer.PushTokenizeRequest
import com.library.gpaypod.interfaces.IResponseFromSdkToApp
import com.library.gpaypod.interfaces.IStableHardwareIdAndWalletId
import com.library.gpaypod.models.CardDetails
import com.library.gpaypod.models.UserAddressDetails
import com.library.gpaypod.utils.Constants
import com.library.gpaypod.utils.Util

public class GPayPod(private val activityContext: Activity?) {

    private var tapAndPayClient: TapAndPayClient? = null
    private var requestCodePushTokenize = 10

    init {
        if (tapAndPayClient == null){
            tapAndPayClient = TapAndPayClient.getClient(activityContext)
        }
    }

    /*getting stableHardwareId and walletId, its required for Google Encryption API to get opc payload*/
    public fun getGooglePayWalletRequiredDetails(iStableHardwareIdAndWalletId: IStableHardwareIdAndWalletId?,
                                          iResponseFromSdkToApp: IResponseFromSdkToApp?) {
        try {

            /*validating the input parameters*/
            if (iStableHardwareIdAndWalletId == null || iResponseFromSdkToApp == null){
                iResponseFromSdkToApp?.onResponse(Constants.INVALID_INPUT, Constants.INVALID_INPUT_ERROR)
                return
            }

            tapAndPayClient?.stableHardwareId?.addOnCompleteListener { stableHardwareIdTask ->
                if (stableHardwareIdTask.isSuccessful){
                    /*get client device stable hardware id i.e client device id*/
                    val stableHardwareId = stableHardwareIdTask.result
                    tapAndPayClient?.activeWalletId?.addOnCompleteListener { activeWalletIdIdTask ->
                        if (activeWalletIdIdTask.isSuccessful){
                            /*get client hardware id i.e client wallet id*/
                            val walletId = activeWalletIdIdTask.result
                            iStableHardwareIdAndWalletId.onReturnStableHardwareIdAndWalletId(stableHardwareId, walletId)
                        }else{
                            val apiException = activeWalletIdIdTask.exception as ApiException
                            if (apiException.statusCode == TapAndPayStatusCodes.TAP_AND_PAY_NO_ACTIVE_WALLET)
                                iResponseFromSdkToApp.onResponse(Constants.NO_ACTIVE_WALLET, Constants.NO_ACTIVE_WALLET)
                            else
                                iResponseFromSdkToApp.onResponse(Constants.WENT_WRONG_PACKAGE, "${apiException.message}")
                        }
                    }
                }else
                    iResponseFromSdkToApp.onResponse(Constants.WENT_WRONG_PACKAGE, Constants.NO_ACTIVE_WALLET)
            }
        }
        catch (e : Exception) {
            iResponseFromSdkToApp?.onResponse(Constants.WENT_WRONG_PACKAGE, "${e.message}")
        }
    }

    /**
     * @param opcPayloadData need to get this payload data from the Google Payment Encryption API which is need to configure
     * on your backend API server.
     * This payload contains the card information of the Card Holder and its details
     * */
    public fun makeAddToGooglePayWalletRequest(iResponseFromSdkToApp : IResponseFromSdkToApp?,
                                        opcPayloadData : String?,
                                        cardDetails: CardDetails?,
                                        userAddressDetails : UserAddressDetails?) {
        try {

            /*validating the input parameters*/
            if (activityContext == null || iResponseFromSdkToApp == null || opcPayloadData.isNullOrEmpty() || cardDetails == null || userAddressDetails == null){
                iResponseFromSdkToApp?.onResponse(Constants.INVALID_INPUT, Constants.INVALID_INPUT_ERROR)
                return
            }
            else if(!Util.checkCardType(cardDetails.cardType!!)){
                iResponseFromSdkToApp.onResponse(Constants.INVALID_INPUT, Constants.INVALID_CARD_TYPE_ERROR)
                return
            }
            else if(!Util.checkCardLastFourDigitValidLength(cardDetails.cardType)){
                iResponseFromSdkToApp.onResponse(Constants.INVALID_INPUT, Constants.INVALID_CARD_LENGTH_ERROR)
                return
            }

            val opc = opcPayloadData.toByteArray()

            /*build proper address which required for Google wallet with country code*/
            val userAddress = Util.createUserAddress(activityContext, userAddressDetails, cardDetails.cardHolderName!!)

            /*get card network and card token provider base on card type*/
            val (cardNetwork, tokenProvider) = Util.getCardNetworkAndTokenProvider(cardDetails.cardType)

            /*push request to Google Wallet*/
            val pushTokenizeRequest = PushTokenizeRequest.Builder()
                .setOpaquePaymentCard(opc)
                .setNetwork(cardNetwork)
                .setTokenServiceProvider(tokenProvider)
                .setDisplayName(cardDetails.cardHolderName)
                .setLastDigits(cardDetails.cardNumberLastFourDigit)
                .setUserAddress(userAddress)
                .build()

            /* pass pushTokenizeRequest to the Google Wallet
            * REQUEST_CODE_PUSH_TOKENIZE - a request code value you define as in Android's startActivityForResult
            * */
            tapAndPayClient?.pushTokenize(activityContext, pushTokenizeRequest, requestCodePushTokenize)
        }catch (e : Exception){
            iResponseFromSdkToApp?.onResponse(Constants.WENT_WRONG_PACKAGE, "${e.message}")
        }
    }
}