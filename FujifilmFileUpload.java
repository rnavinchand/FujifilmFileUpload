package com.fujifilm.libs.spa.utils;

import android.util.Log;
import android.webkit.MimeTypeMap;

import com.fujifilm.libs.spa.handlers.FujifilmFileUploadHandler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by rnavinchand on 12/28/15.
 * Fujifilm SSD
 * rnavinchand@fujifim.com
 */
public class FujifilmFileUpload {
    private static final String BOUNDARY = "----WebKitFormBoundaryzMADQhAQ2JRJ43Wz";
    private static final String LINE_FEED = "\r\n";
    private static final String TWO_HYPHENS = "--";
    private HttpURLConnection mConnection;
    private DataOutputStream mOutputStream;
    private static String TAG = "FujifilmFileUpload";
    private String mRequestUrl;


    public FujifilmFileUpload(String requestUrl) throws IOException {
        this.mRequestUrl = requestUrl;
        URL url = new URL(mRequestUrl);
        mConnection = (HttpURLConnection) url.openConnection();
        mConnection.setReadTimeout(15000);
        mConnection.setConnectTimeout(15000);
        mConnection.setRequestMethod("POST");
        mConnection.setUseCaches(false);
        mConnection.setDoOutput(true);
        mConnection.setDoInput(true);
        mConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=\"" + BOUNDARY + "\"");
        mConnection.setChunkedStreamingMode(8 * 1024);
    }

    public void addHeader(String name, String value) {
        mConnection.setRequestProperty(name, value);
    }

    /**
     * adds file part to the given address
     */
    public void addFilePart(String fieldName, File file) throws IOException {
        mOutputStream = new DataOutputStream(mConnection.getOutputStream());
        String fileName = file.getName();
        String type = MimeTypeMap.getFileExtensionFromUrl(fileName);

        mOutputStream.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_FEED);
        mOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName.replaceAll("[^a-zA-Z0-9.-]", "_") + "\"" + LINE_FEED);
        //RNAV: UnComment Comment out the the following line when using fixedLengthStreamingMode
        //mOutputStream.writeBytes(LINE_FEED);
        //RNAV: Comment out the the next two lines when using fixedLengthStreamingMode
        mOutputStream.writeBytes("Content-Type: " + type + LINE_FEED);
        mOutputStream.writeBytes("Content-Transfer-Encoding: binary" + LINE_FEED);
        mOutputStream.flush();

        FileInputStream inputStream = new FileInputStream(file);
        int bytesAvailable = inputStream.available();
        int maxBufferSize = 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        // read file and write it into form...
        int bytesRead = inputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            mOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = inputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = inputStream.read(buffer, 0, bufferSize);

        }
        inputStream.close();
        inputStream = null;
        mOutputStream.flush();

        mOutputStream.writeBytes(LINE_FEED);
        mOutputStream.writeBytes(LINE_FEED);
        mOutputStream.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_FEED);
        mOutputStream.flush();
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return the response JSON from stage server
     * @throws IOException
     */
    public void finish(FujifilmFileUploadHandler handler) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        int status;
        try {
            // checks server's status code first
            status = mConnection.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                InputStreamReader inputStreamReader = new InputStreamReader(mConnection.getInputStream());
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                handler.onSuccess(responseBuilder.toString(), status);
                inputStreamReader.close();
                reader.close();
            } else {
                handler.onFailure(mConnection.getResponseMessage(), status);
            }

        } catch (SocketTimeoutException ex) {
            Log.e(TAG, ex.getMessage());
            handler.onFailure(ex.getLocalizedMessage(), HttpURLConnection.HTTP_CLIENT_TIMEOUT);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            handler.onFailure(ex.getLocalizedMessage(), HttpURLConnection.HTTP_BAD_REQUEST);
        } finally {
            mOutputStream.flush();
            mOutputStream.close();
            mOutputStream = null;
            mConnection.disconnect();
            mConnection = null;
            System.gc();
        }
    }
}
