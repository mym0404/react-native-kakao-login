//
//  RNKakaoLogins.swift
//  RNKakaoLogins
//
//  Created by hyochan on 2021/03/18.
//  Copyright © 2021 Facebook. All rights reserved.
//

import Foundation

import KakaoSDKCommon
import KakaoSDKAuth
import KakaoSDKUser

@objc(RNKakaoLogins)
class RNKakaoLogins: NSObject {
    @objc
    static func requiresMainQueueSetup() -> Bool {
      return true
    }

    @objc(isKakaoTalkLoginUrl:)
    public static func isKakaoTalkLoginUrl(url:URL) -> Bool {

        let appKey = try? KakaoSDK.shared.appKey();

        if (appKey != nil) {
            return AuthApi.isKakaoTalkLoginUrl(url)
        }
        return false
    }

    @objc(handleOpenUrl:)
    public static func handleOpenUrl(url:URL) -> Bool {
        return AuthController.handleOpenUrl(url: url)
    }

    @objc(login:rejecter:)
    func login(_ resolve: @escaping RCTPromiseResolveBlock,
                rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss";

            if (UserApi.isKakaoTalkLoginAvailable()) {
                UserApi.shared.loginWithKakaoTalk {(oauthToken, error) in
                    if let error = error {
                        reject("RNKakaoLogins", error.localizedDescription, nil)
                    }
                    else {
                        resolve([
                            "accessToken": oauthToken?.accessToken ?? "",
                            "refreshToken": oauthToken?.refreshToken ?? "" as Any,
                            "idToken": oauthToken?.idToken ?? "",
                            "accessTokenExpiresAt": dateFormatter.string(from: oauthToken!.expiredAt),
                            "refreshTokenExpiresAt": dateFormatter.string(from: oauthToken!.refreshTokenExpiredAt),
                            "scopes": oauthToken?.scopes ?? "",
                        ])
                    }
                }
            } else {
                UserApi.shared.loginWithKakaoAccount {(oauthToken, error) in
                    if let error = error {
                        reject("RNKakaoLogins", error.localizedDescription, nil)
                    }
                    else {
                        resolve([
                            "accessToken": oauthToken?.accessToken ?? "",
                            "refreshToken": oauthToken?.refreshToken ?? "" as Any,
                            "idToken": oauthToken?.idToken ?? "",
                            "accessTokenExpiresAt": dateFormatter.string(from: oauthToken!.expiredAt),
                            "refreshTokenExpiresAt": dateFormatter.string(from: oauthToken!.refreshTokenExpiredAt),
                            "scopes": oauthToken?.scopes ?? "",
                        ]);
                    }
                }
            }
        }
    }

    @objc(loginWithKakaoAccount:rejecter:)
    func loginWithKakaoAccount(_ resolve: @escaping RCTPromiseResolveBlock,
                rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
                    let dateFormatter = DateFormatter()
                    dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss";
            UserApi.shared.loginWithKakaoAccount {(oauthToken, error) in
                if let error = error {
                    reject("RNKakaoLogins", error.localizedDescription, nil)
                }
                else {
                    resolve([
                        "accessToken": oauthToken?.accessToken ?? "",
                        "refreshToken": oauthToken?.refreshToken ?? "" as Any,
                        "idToken": oauthToken?.idToken ?? "",
                        "accessTokenExpiresAt": dateFormatter.string(from: oauthToken!.expiredAt),
                        "refreshTokenExpiresAt": dateFormatter.string(from: oauthToken!.refreshTokenExpiredAt),
                        "scopes": oauthToken?.scopes ?? "",
                    ]);
                }
            }
        }
    }

    @objc(logout:rejecter:)
    func logout(_ resolve: @escaping RCTPromiseResolveBlock,
               rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            UserApi.shared.logout {(error) in
                if let error = error {
                    reject("RNKakaoLogins", error.localizedDescription, nil)
                }
                else {
                    resolve("Successfully logged out")
                }
            }
        }
    }

    @objc(unlink:rejecter:)
    func unlink(_ resolve: @escaping RCTPromiseResolveBlock,
               rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            UserApi.shared.unlink {(error) in
                if let error = error {
                    reject("RNKakaoLogins", error.localizedDescription, nil)
                }
                else {
                    resolve("Successfully unlinked")
                }
            }
        }
    }

    @objc(getAccessToken:rejecter:)
    func getAccessToken(_ resolve: @escaping RCTPromiseResolveBlock,
               rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            UserApi.shared.accessTokenInfo {(accessTokenInfo, error) in
                if let error = error {
                    reject("RNKakaoLogins", error.localizedDescription, nil)
                }
                else {
                    resolve([
                        "accessToken": TokenManager.manager.getToken()?.accessToken,
                        "expiresIn": accessTokenInfo?.expiresIn,
                    ])
                }
            }
        }
    }
}
