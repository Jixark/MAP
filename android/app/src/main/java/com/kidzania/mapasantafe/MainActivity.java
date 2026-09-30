package com.kidzania.mapasantafe;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.app.Activity;

/**
 * Loads the interactive KidZania Santa Fe map (bundled locally under
 * assets/www) inside a full-screen WebView. Everything the map needs
 * (HTML, pin data, floor images, icons) ships inside the APK, so the
 * map itself works with no network connection. Only the two Google
 * Fonts (Baloo 2 / Figtree) are fetched online; without connectivity
 * the page falls back to the device's system font automatically.
 */
public class MainActivity extends Activity {

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        // Lets you inspect this WebView from a desktop Chrome at
        // chrome://inspect while the tablet is connected over USB
        // (with USB debugging enabled) — useful for diagnosing why
        // something isn't rendering. Harmless to leave on.
        WebView.setWebContentsDebuggingEnabled(true);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                String scheme = url.getScheme();
                if (scheme != null && (scheme.equals("file"))) {
                    // Keep local navigation inside the app.
                    return false;
                }
                // Any external link (there are none in normal use) opens
                // in the device's own browser instead of inside the app.
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, url));
                } catch (Exception ignored) {
                }
                return true;
            }

            // Logs any failed resource load (e.g. a missing map image)
            // to Logcat under the tag "MapaKidZania", visible via:
            //   adb logcat -s MapaKidZania
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e("MapaKidZania", "Failed to load: " + request.getUrl()
                        + " -> " + error.getDescription());
            }
        });

        // Surfaces JavaScript console.log/warn/error output (and any
        // runtime exceptions) in Logcat under "MapaKidZania" too.
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("MapaKidZania", cm.message() + " [" + cm.sourceId() + ":" + cm.lineNumber() + "]");
                return true;
            }
        });

        webView.loadUrl("file:///android_asset/www/index.html");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
