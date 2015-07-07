/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.accountcreator;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * 認証Proxy用HttpClient.
 */
public class HttpClientForProxy extends DefaultHttpClient implements HttpClient {
    /**
     * 認証プロキシを通過するためのHttpClient。
     */
    public HttpClientForProxy() {
        super();
        String proxyHost = System.getProperty("http.proxyHost");

        if (proxyHost != null) {
            String proxyPortStr = System.getProperty("http.proxyPort");
            int proxyPort = 0;
            if (proxyPortStr != null) {
                proxyPort = Integer.parseInt(proxyPortStr);
            }
            String proxyUser = System.getProperty("http.proxyUser");
            if (proxyUser != null) {
                String proxyPassword = System.getProperty("http.proxyPassword");
                List<String> authpref = new ArrayList<String>();
                authpref.add(AuthPolicy.BASIC);
                this.getParams().setParameter(AuthPNames.PROXY_AUTH_PREF, authpref);
                CredentialsProvider credsProvider = this.getCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), new UsernamePasswordCredentials(
                                proxyUser, proxyPassword));
                this.setCredentialsProvider(credsProvider);
            }

            String protocol = "";
            if (proxyHost.indexOf("https") != 0) {
                protocol = "http";
            } else {
                protocol = "https";
            }
            HttpHost proxy = new HttpHost(proxyHost, proxyPort, protocol);
            this.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
    }
}
