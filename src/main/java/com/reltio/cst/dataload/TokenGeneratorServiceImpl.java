//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.reltio.cst.dataload;

import com.reltio.cst.domain.AuthenticationResponse;
import com.reltio.cst.exception.handler.APICallFailureException;
import com.reltio.cst.exception.handler.GenericException;
import com.reltio.cst.properties.AuthenticationProperties;
import com.reltio.cst.service.RestAPIService;
import com.reltio.cst.service.TokenGeneratorService;
import com.reltio.cst.service.impl.SimpleRestAPIServiceImpl;
import com.reltio.cst.util.GenericUtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.reltio.cst.dataload.util.Util.isEmpty;

public class TokenGeneratorServiceImpl extends Thread implements TokenGeneratorService {
    private static Logger logger = LoggerFactory.getLogger(TokenGeneratorServiceImpl.class.getName());
    private  boolean isrefreshTokenauth;
    private String refreshToken;
    private String username;
    private String password;
    private String authURL;
    private String token;
    private String clientCredentials;
    private Map<String, String> authHeaders = new HashMap();
    private AuthenticationResponse authenticationResponse;
    private RestAPIService apiService = new SimpleRestAPIServiceImpl();
    private boolean isRunning;
    private boolean isClientCredentialsAuth;

    public TokenGeneratorServiceImpl(String username, String password, String authURL) throws APICallFailureException, GenericException {
        this.username = username;
        this.password = password;
        if (GenericUtilityService.checkNullOrEmpty(authURL)) {
            this.authURL = "https://auth.reltio.com/oauth/token";
        } else {
            this.authURL = authURL;
        }

        this.setDaemon(true);
        this.populateAuthHeaders();
        this.getNewToken();
    }

    public TokenGeneratorServiceImpl(String clientCredentials, String authURL) throws APICallFailureException, GenericException {
        this.isClientCredentialsAuth = true;
        this.clientCredentials = clientCredentials;
        if (!GenericUtilityService.checkNullOrEmpty(clientCredentials) && !clientCredentials.contains("RefreshToken") ) {
            if (GenericUtilityService.checkNullOrEmpty(authURL)) {
                this.authURL = "https://auth.reltio.com/oauth/token";
            } else {
                this.authURL = authURL;
            }

        this.setDaemon(true);
        this.populateAuthHeaders();
        this.getNewToken();
        }
        else {
            //this.isrefreshTokenauth = true;
            this.refreshToken = clientCredentials.replace("RefreshToken","");
            if (GenericUtilityService.checkNullOrEmpty(authURL)) {
                this.authURL = "https://auth.reltio.com/oauth/token";
            } else {
                this.authURL = authURL;
            }

            this.setDaemon(true);
            this.populateAuthHeaders();
            String responseStr = this.getAccessTokenByRefreshToken(this.authURL, this.refreshToken , 1);
            this.authenticationResponse = (AuthenticationResponse)AuthenticationProperties.GSON.fromJson(responseStr, AuthenticationResponse.class);

        }
    }

    /** @deprecated */
    @Deprecated
    public boolean startBackgroundTokenGenerator() {
        if (!this.isRunning) {
            this.isRunning = true;
            this.start();
            return true;
        } else {
            return false;
        }
    }

    public String getToken() throws APICallFailureException, GenericException {
        if (this.authenticationResponse == null) {
            this.getNewToken();
        }

        return this.authenticationResponse.getAccessToken();
    }

    public String getRefreshToken() throws APICallFailureException, GenericException {
        if (this.authenticationResponse == null) {
            this.getNewToken();
        }

        return this.authenticationResponse.getRefreshToken();
    }

    public String getNewToken() throws APICallFailureException, GenericException {
        String responseStr;
        if (this.isClientCredentialsAuth()) {
            responseStr = this.getAccessTokenByClientCredentials(this.authURL, this.getClientCredentials(), 1);
            this.authenticationResponse = (AuthenticationResponse)AuthenticationProperties.GSON.fromJson(responseStr, AuthenticationResponse.class);
        } else if (this.authenticationResponse == null) {
            responseStr = this.getAccessToken(this.authURL, this.username, this.password, 1);
            this.authenticationResponse = (AuthenticationResponse)AuthenticationProperties.GSON.fromJson(responseStr, AuthenticationResponse.class);
        } else {
            this.refreshToken();
        }

        return this.authenticationResponse.getAccessToken();
    }

    /** @deprecated */
    @Deprecated
    public boolean stopBackgroundTokenGenerator() {
        if (this.isRunning && this.isAlive()) {
            logger.info("Background Token Generation process Stopped...");
            this.interrupt();
            return true;
        } else {
            return false;
        }
    }

    public void run() {
        logger.info("Background Token Generation process Started...");
        if (this.authenticationResponse == null) {
            try {
                this.refreshToken();
            } catch (GenericException | APICallFailureException var4) {
                var4.printStackTrace();
            }
        }

        while(!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(240000L);
                this.refreshToken();
            } catch (GenericException | APICallFailureException var2) {
                var2.printStackTrace();
            } catch (InterruptedException var3) {
            }
        }

    }

    void refreshToken() throws APICallFailureException, GenericException {
        if (this.authenticationResponse == null || this.isClientCredentialsAuth()) {
            this.getNewToken();
        }

        String responseStr = this.getAccessTokenByRefreshToken(this.authURL, this.authenticationResponse.getRefreshToken(), 1);
        this.authenticationResponse = (AuthenticationResponse)AuthenticationProperties.GSON.fromJson(responseStr, AuthenticationResponse.class);
    }

    private String getAccessToken(String url, String username, String password, int retryCount) throws APICallFailureException, GenericException {
        try {
            return this.doAuthAPICall(url, "grant_type=password&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8.name()) + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8.name()), retryCount);
        } catch (UnsupportedEncodingException var6) {
            throw new RuntimeException(var6);
        }
    }

    private String getAccessTokenByClientCredentials(String url, String clientCredenatials, int retryCount) throws APICallFailureException, GenericException {
        try {
            return this.doAuthAPICall(url, "grant_type=client_credentials", retryCount);
        } catch (Exception var5) {
            throw new RuntimeException(var5);
        }
    }

    private String getAccessTokenByRefreshToken(String url, String refreshToken, int retryCount) throws APICallFailureException, GenericException {
        String accessToken;
        try {
            logger.info("Getting New Token Using Refresh Token..", refreshToken);
            accessToken = this.doAuthAPICall(url, "grant_type=refresh_token&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8.name()), retryCount);
            if (accessToken.equals("Invalid refresh token")) {
                logger.info("Invalid Refresh Token.. Will Try again with new refresh token..");
                //return this.getAccessToken(this.authURL, this.username, this.password, 1);
                return this.doAuthAPICall(url, "grant_type=refresh_token&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8.name()), retryCount);
            }
        } catch (UnsupportedEncodingException var6) {
            throw new RuntimeException(var6);
        }

        logger.debug(accessToken);
        return accessToken;
    }

    private String doAuthAPICall(String url, String body, int retryCount) throws APICallFailureException, GenericException {
        try {
            return this.apiService.post(url, this.authHeaders, body);
        } catch (APICallFailureException var10) {
            logger.error("Auth Call Failed: Error Code = " + var10.getErrorCode() + " |||| Error Message: " + var10.getErrorResponse());
            long sleepTime;
            switch(var10.getErrorCode()) {
                case 400:
                    throw var10;
                case 401:
                    if (var10.getErrorResponse().contains("Invalid refresh token")) {
                        return "Invalid refresh token";
                    }

                    if (retryCount < AuthenticationProperties.RETRY_LIMIT) {
                        logger.info("Retrying with new token..");
                        ++retryCount;
                        return this.doAuthAPICall(url, body, retryCount);
                    }
                    break;
                case 502:
                    if (retryCount < AuthenticationProperties.RETRY_LIMIT_FOR_502) {
                        try {
                            sleepTime = (long)(Math.pow(2.0D, (double)retryCount) - 1.0D) * 1000L;
                            logger.info("Retrying in " + sleepTime + " milliseconds..");
                            Thread.sleep(sleepTime);
                            ++retryCount;
                            return this.doAuthAPICall(url, body, retryCount);
                        } catch (InterruptedException var9) {
                            logger.error("Unexpected interruption exception.. " + var9.getMessage());
                        }
                    }
                    break;
                case 503:
                    if (retryCount < AuthenticationProperties.RETRY_LIMIT_FOR_503) {
                        try {
                            sleepTime = (long)(Math.pow(2.0D, (double)retryCount) - 1.0D) * 1000L;
                            logger.info("Retrying in " + sleepTime + " milliseconds..");
                            Thread.sleep(sleepTime);
                            ++retryCount;
                            return this.doAuthAPICall(url, body, retryCount);
                        } catch (InterruptedException var8) {
                            logger.error("Unexpected interruption exception.. " + var8.getMessage());
                        }
                    }
                    break;
                case 504:
                    if (retryCount < AuthenticationProperties.RETRY_LIMIT_FOR_504) {
                        try {
                            sleepTime = (long)(Math.pow(2.0D, (double)retryCount) - 1.0D) * 1000L;
                            logger.info("Retrying in " + sleepTime + " milliseconds..");
                            Thread.sleep(sleepTime);
                            ++retryCount;
                            return this.doAuthAPICall(url, body, retryCount);
                        } catch (InterruptedException var7) {
                            logger.error("Unexpected interruption exception.. " + var7.getMessage());
                        }
                    }
            }

            throw var10;
        } catch (GenericException var11) {
            logger.error("Auth Call Failed Due to unexpected Exception: Error Message: " + var11.getExceptionMessage());
            if (retryCount < AuthenticationProperties.RETRY_LIMIT) {
                ++retryCount;
                return this.doAuthAPICall(url, body, retryCount);
            } else {
                throw var11;
            }
        }
    }

    private void populateAuthHeaders() {
        if (!this.isClientCredentialsAuth()) {
            this.authHeaders.put("Authorization", "Basic cmVsdGlvX3VpOm1ha2l0YQ==");
        } else if (!isEmpty(refreshToken)) {
            this.authHeaders.put("Authorization", "Basic cmVsdGlvX3VpOm1ha2l0YQ==");
        }
        else {
            this.authHeaders.put("Authorization", "Basic " + this.getClientCredentials());
        }

        this.authHeaders.put("Content-Type", "application/x-www-form-urlencoded");
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthURL() {
        return this.authURL;
    }

    public void setAuthURL(String authURL) {
        this.authURL = authURL;
    }

    public String getClientCredentials() {
        return this.clientCredentials;
    }

    public void setClientCredentials(String clientCredentials) {
        this.clientCredentials = clientCredentials;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = clientCredentials.replace("RefreshToken","");
    }

    public boolean isClientCredentialsAuth() {
        return this.isClientCredentialsAuth;
    }
    public boolean isrefreshTokenAuth() {
        return this.isrefreshTokenauth;
    }

    public void setClientCredentialsAuth(boolean isClientCredentialsAuth) {
        this.isClientCredentialsAuth = isClientCredentialsAuth;
    }
    public void setRefreshTokenauth(boolean isrefreshTokenauth) {
        this.isrefreshTokenauth = isrefreshTokenauth;
    }
}
