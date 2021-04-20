package com.example.uberclone;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("TKLLnb57irRfpwiBmTj7fmXvf9rK0pn6p1ZqsgRW")
                .clientKey("QPCMJItcVp92mGSYrnGH3YxVgt6ieZj7DOj3s5eD")
                .server("https://parseapi.back4app.com/")
                .build()
        );
    }
}
