package com.library.gpaypod.utils
import android.content.Context
import android.util.Log
import com.google.android.gms.tapandpay.TapAndPay
import com.google.android.gms.tapandpay.issuer.UserAddress
import com.library.gpaypod.models.CardType
import com.library.gpaypod.models.UserAddressDetails
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Objects
object Util {

    /*get card network and card token provider base on input card type*/
    fun getCardNetworkAndTokenProvider(cardType: String) : Pair<Int, Int>{
        return when(cardType) {
            CardType.DISCOVER.toString() -> Pair(TapAndPay.CARD_NETWORK_DISCOVER, TapAndPay.TOKEN_PROVIDER_DISCOVER)
            CardType.MASTERCARD.toString() -> Pair(TapAndPay.CARD_NETWORK_MASTERCARD, TapAndPay.TOKEN_PROVIDER_MASTERCARD)
            CardType.VISA.toString() -> Pair(TapAndPay.CARD_NETWORK_VISA, TapAndPay.TOKEN_PROVIDER_VISA)
            CardType.INTERAC.toString() -> Pair(TapAndPay.CARD_NETWORK_INTERAC, TapAndPay.TOKEN_PROVIDER_INTERAC)
            CardType.EFTPOS.toString() -> Pair(TapAndPay.CARD_NETWORK_EFTPOS, TapAndPay.TOKEN_PROVIDER_EFTPOS)
            CardType.JCB.toString() -> Pair(TapAndPay.CARD_NETWORK_JCB, TapAndPay.TOKEN_PROVIDER_JCB)
            CardType.ELO.toString() -> Pair(TapAndPay.CARD_NETWORK_ELO, TapAndPay.TOKEN_PROVIDER_ELO)
            else -> Pair(TapAndPay.CARD_NETWORK_AMEX, TapAndPay.TOKEN_PROVIDER_AMEX)
        }
    }

    /*check send valid card type or not*/
    fun checkCardType(cardType: String) : Boolean{
        return when(cardType) {
            CardType.AMEX.toString() -> true
            CardType.DISCOVER.toString() -> true
            CardType.MASTERCARD.toString() -> true
            CardType.VISA.toString() -> true
            CardType.INTERAC.toString() -> true
            CardType.EFTPOS.toString() -> true
            CardType.JCB.toString() -> true
            CardType.ELO.toString() -> true
            else -> false
        }
    }

    /*validate the card last 4 digit length*/
    fun checkCardLastFourDigitValidLength(cardNumberLastFourDigit : String) : Boolean
        = cardNumberLastFourDigit.length == 4

    /*create valid user address*/
    fun createUserAddress(context: Context, userAddressDetails : UserAddressDetails, cardHolderName : String) : UserAddress {
        return UserAddress.newBuilder()
            .setAddress1(userAddressDetails.address1)
            .setAddress2(userAddressDetails.address2)
            .setCountryCode(getCountryCode(context, userAddressDetails.country!!))
            .setLocality(userAddressDetails.locality)
            .setAdministrativeArea(userAddressDetails.administrativeArea)
            .setName(cardHolderName)
            .setPhoneNumber(userAddressDetails.phoneNumber)
            .setPostalCode(userAddressDetails.postalCode)
            .build()
    }

    /*extracting the country wise country code from the country asset file*/
    private fun getCountryCode(context: Context, countryName: String) : String {
        var countryCode = ""
        try {
            val countryCodeJson = loadJSONFromAsset(context)
            val jsonObjectRoot = JSONObject(countryCodeJson!!)
            val jsonArrayData = jsonObjectRoot.getJSONArray("countryCodes")
            for (i in 0 until jsonArrayData.length()) {
                val jsonObjectCountry = jsonArrayData.getJSONObject(i)
                if (jsonObjectCountry.has("name")) {
                    val name = jsonObjectCountry.getString("name")
                    if (name.equals(countryName, ignoreCase = true)) {
                        countryCode = jsonObjectCountry.getString("code")
                        break
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return countryCode
    }

    /*loading the country code from the country code json file, which is added in asset folder*/
    private fun loadJSONFromAsset(context: Context) : String? {
        val json: String?
        var inputStream : InputStream? = null
        try {
            inputStream = context.assets.open(Constants.COUNTRY_CODE_JSON_ASSET)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.close()
            json = String(buffer, StandardCharsets.UTF_8)
        } catch (ex: IOException) {
            return null
        } finally {
            try {
                inputStream?.close()
            } catch (io: IOException) {
                Objects.requireNonNull(io.message).let { Log.e("app", "$it") }
            }
        }
        return json
    }
}