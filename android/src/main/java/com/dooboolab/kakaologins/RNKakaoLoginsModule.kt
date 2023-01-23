package com.dooboolab.kakaologins

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableArray
import com.kakao.sdk.auth.TokenManagerProvider
import com.kakao.sdk.common.model.AuthError
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.ServiceTerms
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class RNKakaoLoginsModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private fun dateFormat(date: Date?)= SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)

    override fun getName(): String {
        return "RNKakaoLogins"
    }

    @ReactMethod
    private fun login(promise: Promise) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(reactContext)) {
            currentActivity?.let {
                UserApiClient.instance.loginWithKakaoTalk(it) { token, error: Throwable? ->
                    if (error != null) {
                        if (error is AuthError && error.statusCode == 302) {
                            this.loginWithKakaoAccount(promise)
                            return@loginWithKakaoTalk
                        }
                        promise.reject("RNKakaoLogins", error.message, error)
                        return@loginWithKakaoTalk
                    }

                    if (token != null) {
                        val (accessToken, accessTokenExpiresAt, refreshToken, refreshTokenExpiresAt, idToken, scopes) = token
                        val map = Arguments.createMap()
                        map.putString("accessToken", accessToken)
                        val scopeArray = Arguments.createArray()
                        if (scopes != null) {
                            for (scope in scopes) {
                                scopeArray.pushString(scope)
                            }
                        }
                        map.putArray("scopes", scopeArray)
                        promise.resolve(map)
                        return@loginWithKakaoTalk
                    }

                    promise.reject("RNKakaoLogins", "Token is null")
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(reactContext) { token, error: Throwable? ->
                if (error != null) {
                    promise.reject("RNKakaoLogins", error.message, error)
                    return@loginWithKakaoAccount
                }

                if (token != null) {
                    val (accessToken, accessTokenExpiresAt, refreshToken, refreshTokenExpiresAt, idToken, scopes) = token
                    val map = Arguments.createMap()
                    map.putString("accessToken", accessToken)
                    map.putString("refreshToken", refreshToken)
                    map.putString("idToken", idToken)
                    map.putString("accessTokenExpiresAt", dateFormat(accessTokenExpiresAt))
                    map.putString("refreshTokenExpiresAt", dateFormat(refreshTokenExpiresAt))
                    val scopeArray = Arguments.createArray()
                    if (scopes != null) {
                        for (scope in scopes) {
                            scopeArray.pushString(scope)
                        }
                    }
                    map.putArray("scopes", scopeArray)
                    promise.resolve(map)
                    return@loginWithKakaoAccount
                }

                promise.reject("RNKakaoLogins", "Token is null")
            }
        }
    }

    @ReactMethod
    private fun loginWithKakaoAccount(promise: Promise) {
        UserApiClient.instance.loginWithKakaoAccount(reactContext) { token, error: Throwable? ->
            if (error != null) {
                promise.reject("RNKakaoLogins", error.message, error)
                return@loginWithKakaoAccount
            }

            if (token == null) {
                promise.reject("RNKakaoLogins", "Token is null")
                return@loginWithKakaoAccount
            }

            val (accessToken, accessTokenExpiresAt, refreshToken, refreshTokenExpiresAt, idToken, scopes) = token
            val map = Arguments.createMap()
            map.putString("accessToken", accessToken)
            map.putString("refreshToken", refreshToken)
            map.putString("idToken", idToken)
            map.putString("accessTokenExpiresAt", dateFormat(accessTokenExpiresAt))
            map.putString("refreshTokenExpiresAt", dateFormat(refreshTokenExpiresAt))
            val scopeArray = Arguments.createArray()
            if (scopes != null) {
                for (scope in scopes) {
                    scopeArray.pushString(scope)
                }
            }
            map.putArray("scopes", scopeArray)
            promise.resolve(map)
            return@loginWithKakaoAccount
        }
    }

    @ReactMethod
    private fun logout(promise: Promise) {
        UserApiClient.instance.logout { error: Throwable? ->
            if (error != null) {
                promise.reject("RNKakaoLogins", error.message, error)
                return@logout
            }
            promise.resolve("Successfully logged out")
        }
    }

    @ReactMethod
    private fun unlink(promise: Promise) {
        UserApiClient.instance.unlink { error: Throwable? ->
            if (error != null) {
                promise.reject("RNKakaoLogins", error.message, error)
                return@unlink
            }
            promise.resolve("Successfully unlinked")
        }
    }

    @ReactMethod
    private fun getAccessToken(promise: Promise) {
        val accessToken = TokenManagerProvider.instance.manager.getToken()?.accessToken

         UserApiClient.instance.accessTokenInfo { token, error: Throwable? ->
            if (error != null) {
                promise.reject("RNKakaoLogins", error.message, error)
                return@accessTokenInfo
            }

            if (token != null && accessToken != null) {
                val (expiresIn) = token
                val map = Arguments.createMap()
                map.putString("accessToken", accessToken.toString())
                map.putString("expiresIn", expiresIn.toString())
                promise.resolve(map)
                return@accessTokenInfo
            }

            promise.reject("RNKakaoLogins", "Token is null")
         }
    }

    @ReactMethod
    private fun getAllowedServiceTermTags(promise: Promise) {
        UserApiClient.instance.serviceTerms { userServiceTerms, error ->
            if (error != null) {
                promise.reject("RNKakaoLogins", error.message, error)
                return@serviceTerms
            }
            else if (userServiceTerms != null) {
                val tags = Arguments.createArray()
                userServiceTerms.allowedServiceTerms?.forEach {
                    tags.pushString(it.tag)
                }
                promise.resolve(tags)
                return@serviceTerms
            }
            promise.reject("RNKakaoLogins", "serviceTerms is null")
        }
    }

    companion object {
        private const val TAG = "RNKakaoLoginModule"
    }
}
