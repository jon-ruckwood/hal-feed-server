package com.qmetric.feed.app


import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.*
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.BasicClientConnectionManager
import org.apache.http.util.EntityUtils

class SparkTestUtil
{
    private int port;

    private HttpClient httpClient;

    def SparkTestUtil(int port)
    {
        this.port = port;
        Scheme http = new Scheme("http", port, PlainSocketFactory.getSocketFactory());
        SchemeRegistry sr = new SchemeRegistry();
        sr.register(http);
        ClientConnectionManager connMrg = new BasicClientConnectionManager(sr);
        this.httpClient = new DefaultHttpClient(connMrg);
    }

    def UrlResponse doMethod(String requestMethod, String path, String body) throws Exception
    {
        return doMethod(requestMethod, path, body, false, "text/html");
    }

    def UrlResponse doMethod(String requestMethod, String path, String body, boolean secureConnection,
                             String acceptType) throws Exception
    {

        HttpUriRequest httpRequest = getHttpRequest(requestMethod, path, body, secureConnection, acceptType);
        HttpResponse httpResponse = httpClient.execute(httpRequest);

        UrlResponse urlResponse = new UrlResponse();
        urlResponse.status = httpResponse.getStatusLine().getStatusCode();
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null)
        {
            urlResponse.body = EntityUtils.toString(entity);
        }
        else
        {
            urlResponse.body = "";
        }
        Map<String, String> headers = new HashMap<String, String>();
        Header[] allHeaders = httpResponse.getAllHeaders();
        for (Header header : allHeaders)
        {
            headers.put(header.getName(), header.getValue());
        }
        urlResponse.headers = headers;
        return urlResponse;
    }

    def HttpUriRequest getHttpRequest(String requestMethod, String path, String body, boolean secureConnection,
                                      String acceptType)
    {
        try
        {
            String protocol = secureConnection ? "https" : "http";
            String uri = protocol + "://localhost:" + port + path;

            if (requestMethod.equals("GET"))
            {
                HttpGet httpGet = new HttpGet(uri);

                httpGet.setHeader("Accept", acceptType);

                return httpGet;
            }

            if (requestMethod.equals("POST"))
            {
                HttpPost httpPost = new HttpPost(uri);
                httpPost.setHeader("Accept", acceptType);
                httpPost.setEntity(new StringEntity(body));
                return httpPost;
            }

            if (requestMethod.equals("PUT"))
            {
                HttpPut httpPut = new HttpPut(uri);
                httpPut.setHeader("Accept", acceptType);
                httpPut.setEntity(new StringEntity(body));
                return httpPut;
            }

            throw new IllegalArgumentException("Unknown method " + requestMethod);

        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }

    }

    def static class UrlResponse
    {

        public Map<String, String> headers;
        public String body;
        public int status;
    }
}
