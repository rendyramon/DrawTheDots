package com.villu164.drawthedots;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;
@ReportsCrashes(
		formKey = "",
        formUri = "https://villu164.cloudant.com/acra-drawthedots/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin="orditterytodidendenessai",
        formUriBasicAuthPassword="DUpW5YRQG2whPWBIvHdsEkwC",
        // Your usual ACRA configuration
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
		)

//Key => orditterytodidendenessai
//pass => DUpW5YRQG2whPWBIvHdsEkwC
//this is a cloudant writeonly account
//https://villu164.cloudant.com/acralyzer/_design/acralyzer/index.html#/bugs-browser/drawthedots
public class DrawTheDotsApplication extends Application {
	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		super.onCreate();
		ACRA.init(this);
	}
}
