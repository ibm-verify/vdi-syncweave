/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.oidc;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import java.io.IOException;
import java.net.URI;

public class OIDCRPUtils {

    protected OIDCProviderMetadata opMetadata;

    public static String getAuthorizationUri(String authorizationEndpointUrl, String clientId, String redirectUri,
            String scope) {
        String uri = "";
        try {
            ClientID clientID = new ClientID(clientId);
            URI callback = new URI(redirectUri);
            State state = new State();
            Nonce nonce = new Nonce();
            AuthenticationRequest request = new AuthenticationRequest.Builder(
                    new ResponseType("code"),
                    new Scope(scope),
                    clientID,
                    callback)
                    .endpointURI(new URI(authorizationEndpointUrl))
                    .state(state)
                    .nonce(nonce)
                    .build();

            uri = request.toURI().toString();
        } catch (Exception e) {
            System.out.println(e);
        }

        return uri;
    }

    public static String getOIDCTokens(String authCode, String redirectURI, String clientID, String clientSecret,
            String tokenEndpointUrl) throws IOException, ParseException {

        OIDCTokenResponse successResponse = null;

        try {
            AuthorizationCode authcode = new AuthorizationCode(authCode);
            AuthorizationGrant codeGrant = new AuthorizationCodeGrant(authcode,
                    URI.create(redirectURI));

            // The credentials to authenticate the client at the token endpoint
            ClientID cid = new ClientID(clientID);
            Secret cSecret = new Secret(clientSecret);
            ClientAuthentication clientAuth = new ClientSecretBasic(cid, cSecret);

            URI tokenEndpoint = new URI(tokenEndpointUrl);

            // Make the token request
            TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant);

            TokenResponse tokenResponse = OIDCTokenResponseParser.parse(request.toHTTPRequest().send());

            if (tokenResponse.indicatesSuccess()) {
                // Safe casting since we checked success
                successResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
            } else {
                // Handle the error response properly
                TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
                System.err.println("Token Request Failed: " + errorResponse.toJSONObject());
                return errorResponse.toString();
            }
        } catch (Exception e) {
            System.out.println("Error in getOIDCTokens: " + e);
            return e.toString();
        }

        return successResponse.getOIDCTokens().getAccessToken().toString();

    }

    public static String getUserInfo(String userinfo_endpoint, String userIdentityToMatch, String bearerAccessToken)
            throws IOException, ParseException {

        String userInfoVal = "";
        try {

            URI userInfoEndpoint = new URI(userinfo_endpoint);

            BearerAccessToken bearerToken = new BearerAccessToken(bearerAccessToken);

            HTTPResponse httpResponse = new UserInfoRequest(userInfoEndpoint,
                    bearerToken)
                    .toHTTPRequest()
                    .send();

            UserInfoResponse userInfoResponse = UserInfoResponse.parse(httpResponse);

            if (!userInfoResponse.indicatesSuccess()) {
                // The request failed, e.g. due to invalid or expired token
                System.out.println(userInfoResponse.toErrorResponse().getErrorObject().getCode());
                System.out.println(userInfoResponse.toErrorResponse().getErrorObject().getDescription());
                return userInfoResponse.toErrorResponse().getErrorObject().getCode().toString();
            }

            // Extract the claims
            UserInfo userInfo = userInfoResponse.toSuccessResponse().getUserInfo();
            
            // Get user mail ID
            if (userIdentityToMatch.equals("mail"))
                userInfoVal = userInfo.getEmailAddress().toString();
            
            // Get user preferred username
            if (userIdentityToMatch.equals("cn"))
                userInfoVal = userInfo.getPreferredUsername().toString();
            
            // Get user first name
            if (userIdentityToMatch.equals("uid"))
                userInfoVal = userInfo.getGivenName().toString();

        } catch (Exception e) {
            System.out.println("Error in getUserInfo: " + e);
            return e.toString();
        }

        return userInfoVal;
    }

    public static void main(String[] args) {
        OIDCRPUtils Otest = new OIDCRPUtils();
        try {
            String authCode = "<authorization code>";
            String redirectURI = "https://<IP>:1060/user";
            String clientID = "<client id>";
            String clientSecret = "<client secret>";
            String tokenEndpointUrl = "https://<tenant-name>/v1.0/endpoint/default/token";
            String userinfo_endpoint = "https://<tenant-name>/v1.0/endpoint/default/userinfo";

            String authorizationEndpointUrl = "https://<tenant-name>/v1.0/endpoint/default/authorize";
            String scope = "openid";

            String userIdentityToMatch = "cn";

            String uri = getAuthorizationUri(authorizationEndpointUrl, clientID, redirectURI, scope);
            System.out.println("Authorization URI : " + uri);
            String accessToken = getOIDCTokens(authCode, redirectURI, clientID, clientSecret, tokenEndpointUrl);
            System.out.println("Access Token : " + accessToken);
            String userInfoVal = getUserInfo(userinfo_endpoint, userIdentityToMatch, accessToken);
            System.out.println("User Information: " + userIdentityToMatch + " : " + userInfoVal);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}