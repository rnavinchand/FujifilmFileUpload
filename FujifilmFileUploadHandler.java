import android.util.Log;

/**
 * Created by rnavinchand on 12/29/15.
 * Fujifilm SSD
 * rnavinchand@fujifim.com
 */
public abstract class FujifilmFileUploadHandler {

    private static final String TAG = "fujifilm.spa.sdk";

    public void onSuccess(String response, int statusCode) {
        Log.w(TAG, "onSuccess(String response, int statusCode) was not overridden, but callback was received");

    }

    public void onFailure(String errorResponse, int statusCode) {
        Log.w(TAG, "onFailure(String errorResponse, int statusCode) was not overridden, but callback was received");
    }
}
