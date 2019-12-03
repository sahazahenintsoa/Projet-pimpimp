package com.general.files;

import android.util.Log;

import com.utils.CommonUtilities;
import com.utils.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;

public class ExecuteResponse {

    public String getResponse(String url_str) {

        String responseString = "";
        HttpURLConnection urlConnection = null;

        try {
            Utils.printLog("Api", "Url" + url_str);
            URL url = new URL(url_str);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            responseString = readStream(in);
        } catch (IOException e) {
            e.printStackTrace();
            responseString = "";
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
//        Utils.printLog("Api", "responseString" + responseString);

        return responseString;
    }

    public String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();

        BufferedReader r = new BufferedReader(new InputStreamReader(is), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

  /*  public String uploadImageAsFile(String sourceFileUri, String fileName, String imageParamKey, ArrayList<String[]> params) {

        String responseString = "";
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(new File(sourceFileUri));
            byte[] data;
            try {
                data = convertToByteArray(inputStream);

                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(CommonUtilities.SERVER_URL_WEBSERVICE);

                InputStreamBody inputStreamBody = new InputStreamBody(new ByteArrayInputStream(data), fileName);
                MultipartEntity multipartEntity = new MultipartEntity();
                multipartEntity.addPart(imageParamKey, inputStreamBody);

                for (int i = 0; i < params.size(); i++) {
                    String[] paramsArr = params.get(i);
                    multipartEntity.addPart(paramsArr[0], new StringBody(paramsArr[1]));
                }

                httpPost.setEntity(multipartEntity);
                HttpResponse httpResponse = httpClient.execute(httpPost);

                // Handle response back from script.
                if (httpResponse != null) {

                    Utils.printLog("success", "success:" + httpResponse.toString());
                    responseString = EntityUtils.toString(httpResponse.getEntity());

                } else { // Error, no response.
                    Utils.printLog("Failed", "failed:" + httpResponse.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        return responseString;
    }
*/


    public String uploadImageAsFile(String sourceFileUri, String fileName, String imageParamKey, ArrayList<String[]> params) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        String responseString = "";
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(new File(sourceFileUri));
            byte[] data;
            try {
                data = convertToByteArray(inputStream);

                SSLSocketFactory sslFactory = new SimpleSSLSocketFactory(null);
                sslFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                HttpParams paramsdata = new BasicHttpParams();
                HttpProtocolParams.setVersion(paramsdata, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(paramsdata, HTTP.UTF_8);

                // Register the HTTP and HTTPS Protocols. For HTTPS, register our custom SSL Factory object.
                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                registry.register(new Scheme("https", sslFactory, 443));

                // Create a new connection manager using the newly created registry and then create a new HTTP client using this connection manager
                ClientConnectionManager ccm = new ThreadSafeClientConnManager(paramsdata, registry);
                HttpClient  httpClient = new DefaultHttpClient(ccm, paramsdata);

                //HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(CommonUtilities.SERVER_URL_WEBSERVICE);


                InputStreamBody inputStreamBody = new InputStreamBody(new ByteArrayInputStream(data), fileName);
                MultipartEntity multipartEntity = new MultipartEntity();
                FileBody fileBody=new FileBody(new File(sourceFileUri));
                multipartEntity.addPart(imageParamKey, fileBody);

                for (int i = 0; i < params.size(); i++) {
                    String[] paramsArr = params.get(i);
                    multipartEntity.addPart(paramsArr[0], new StringBody(paramsArr[1]));
                }

                httpPost.setEntity(multipartEntity);


                HttpResponse httpResponse = httpClient.execute(httpPost);

                // Handle response back from script.
                if (httpResponse != null) {

                    Utils.printLog("success", "success:" + httpResponse.toString());
                    responseString = EntityUtils.toString(httpResponse.getEntity());

                } else { // Error, no response.

                    Utils.printLog("Failed", "failed:" + httpResponse.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        return responseString;
    }

    private byte[] convertToByteArray(InputStream inputStream) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int next = inputStream.read();
        while (next > -1) {
            bos.write(next);
            next = inputStream.read();
        }

        bos.flush();

        return bos.toByteArray();
    }
}
