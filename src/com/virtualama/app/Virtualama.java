/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.virtualama.app;

import android.os.Bundle;
import org.apache.cordova.*;
import com.moodstocks.phonegap.plugin.MoodstocksWebView;
import android.content.Intent;
import android.view.ViewManager;

public class Virtualama extends CordovaActivity 
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.init();
        // Set by <content src="index.html" /> in config.xml
        super.loadUrl(Config.getStartUrl());
        //super.loadUrl("file:///android_asset/www/index.html");
    }

    private boolean scanActivityStarted = false;

    @Override
    public void init() {
      MoodstocksWebView webView = new MoodstocksWebView(this);
      webView.setBackgroundColor(0x00000000);
      CordovaWebViewClient webViewClient;

      if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
        webViewClient = new CordovaWebViewClient(this, webView);
      }
      else {
        webViewClient = new IceCreamCordovaWebViewClient(this, webView);
      }

      super.init(webView, webViewClient, new CordovaChromeClient(this, webView));
    }

    @Override
    public void onPause() {
      super.onPause();

      // Remove the web view from the root view when we launch the Moodstocks scanner
      if (scanActivityStarted) {
        super.root.removeView(super.appView);
      }
    }

    @Override
    public void onResume() {
      super.onResume();

      // this case is occurred when the scanActivity fails at launching
      // the failure of launching scanner is often caused by the camera's unavailability
      // in this case we retrieve & reload the web view before inserting it back
      if (scanActivityStarted && (super.appView.getParent() != null)) {
        ((ViewManager)super.appView.getParent()).removeView(super.appView);
        super.appView.reload();
      }

      // Reset the web view to root container when we dismiss the Moodstocks scanner
      if (scanActivityStarted && (super.appView.getParent() == null)) {
        super.root.addView(super.appView);
        scanActivityStarted = false;
      }
    }

    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
      // If the intent indicate the upcoming activity is a Moodtsocks scan activity
      // We will launch the activity and keep the js/native code running on the background
      if (intent.getExtras().getString("com.moodstocks.phonegap.plugin") != null) {
        if(intent.getExtras().getString("com.moodstocks.phonegap.plugin").equals("MoodstocksScanActivity")) {
          scanActivityStarted = true;
          this.startActivityForResult(intent, requestCode);
        }
      }
      else {
        super.startActivityForResult(command, intent, requestCode);
      }
    }
}

