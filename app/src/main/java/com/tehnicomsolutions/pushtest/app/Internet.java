package com.tehnicomsolutions.pushtest.app;

import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.http.NameValuePair;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author Predrag Čokulov
 */

public class Internet
{
    private static final boolean log = BuildConfig.DEBUG;
    private static final boolean printResponse = log && true;

    private Internet()
    {
    }

    static
    {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    /**
     * Executes HTTP POST request and returns response as string<br>
     * This method will not check if response code from server is OK ( < 400)<br>
     *
     * @return server response as string
     */
    public static Response httpPost(String url, @NonNull List<NameValuePair> postParams)
    {
        Response response = new Response();
        InputStream is = null;
        try
        {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            //conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(Constants.CONN_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            StringBuilder builder = new StringBuilder();
            for (NameValuePair pair : postParams)
            {
                builder.append("&").append(pair.getName()).append("=").append(pair.getValue());
            }
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", Integer.toString(builder.toString().getBytes().length));
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(builder.toString());
            wr.flush();
            wr.close();

            // Starts the query
            conn.connect();

            response.responseData = readStreamToString(is = conn.getInputStream());
            response.code = conn.getResponseCode();
            response.responseMessage = conn.getResponseMessage();
            if (log) Log.d(Constants.LOG_TAG, "httpPost[" + url+ "]: " + response);
        }
        catch (UnsupportedEncodingException e)
        {
            response.code = Response.RESPONSE_CODE_UNSUPPORTED_ENCODING;
            response.responseMessage = "net error";
        }
        catch (MalformedURLException e)
        {
            response.code = Response.RESPONSE_CODE_MALFORMED_URL;
            response.responseMessage = "net error";
        }
        catch (IOException e)
        {
            response.code = Response.RESPONSE_CODE_IO_ERROR;
            response.responseMessage = "net error";
        }
        finally
        {
            response.request = url;
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException ignored){}
            }
        }

        return response;
    }

    /**
     * Executes HTTP GET request and returns response as string<br>
     * This method will not check if response code from server is OK ( < 400)<br>
     *
     * @return server response as string
     */
    public static Response httpGet(String url)
    {
        Response response = new Response();
        InputStream is = null;
        try
        {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            //conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(Constants.CONN_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();

            response.responseData = readStreamToString(is =conn.getInputStream());
            response.code = conn.getResponseCode();
            response.responseMessage = conn.getResponseMessage();
            if (log) Log.d(Constants.LOG_TAG, "httpGet[" + url + "]: " + response);
        }
        catch (UnsupportedEncodingException e)
        {
            response.code = Response.RESPONSE_CODE_UNSUPPORTED_ENCODING;
            response.responseMessage = "net error";
        }
        catch (MalformedURLException e)
        {
            response.code = Response.RESPONSE_CODE_MALFORMED_URL;
            response.responseMessage = "net error";
        }
        catch (IOException e)
        {
            response.code = Response.RESPONSE_CODE_IO_ERROR;
            response.responseMessage = "net error";
        }
        finally
        {
            response.request = url;
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException ignored){}
            }
        }

        return response;
    }

    public static String readStreamToString(InputStream stream) throws IOException
    {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        StringBuilder string = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null)
        {
            string.append(line);
        }
        return string.toString();
    }

    public static class Response
    {
        public static final int RESPONSE_CODE_UNKNOWN_ERROR = -1;
        public static final int RESPONSE_CODE_UNSUPPORTED_ENCODING = -2;
        public static final int RESPONSE_CODE_MALFORMED_URL = -3;
        public static final int RESPONSE_CODE_IO_ERROR = -4;

        public int code = RESPONSE_CODE_UNKNOWN_ERROR;
        public String responseMessage;
        public String responseData;
        public String request;

        public boolean isResponseOk()
        {
            return code < 400 && code > 0;
        }

        @Override
        public String toString()
        {
            return "Response{" +
                    "code=" + code +
                    ", responseMessage='" + responseMessage + '\'' +
                    (printResponse ? ", responseData='" + responseData : "") + '\'' +
                    '}';
        }
    }
}