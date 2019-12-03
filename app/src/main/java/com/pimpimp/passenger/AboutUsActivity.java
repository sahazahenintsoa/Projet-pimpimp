package com.pimpimp.passenger;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.utils.CommonUtilities;
import com.view.ErrorView;
import com.view.MTextView;
import com.view.pinnedListView.CountryListItem;
import com.view.pinnedListView.PinnedSectionListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class AboutUsActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;
    ProgressBar loading;
    ErrorView errorView;

    LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        generalFunc = new GeneralFunctions(getActContext());

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        loading = (ProgressBar) findViewById(R.id.loading);
        errorView = (ErrorView) findViewById(R.id.errorView);
        container = (LinearLayout) findViewById(R.id.container);

        setLabels();

        backImgView.setOnClickListener(new setOnClickList());

        loadAboutUsData();
    }


    public void setLabels() {
        if (getIntent().hasExtra("from")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PRIVACY_TXT"));
        } else {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ABOUT_US_HEADER_TXT"));
        }
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.backImgView:
                    AboutUsActivity.super.onBackPressed();
                    break;

            }
        }
    }

    public void loadAboutUsData() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "staticPage");
        if (getIntent().hasExtra("from")) {
            parameters.put("iPageId", "32");
        } else {
            parameters.put("iPageId", "1");
        }
        parameters.put("appType", CommonUtilities.app_type);
        parameters.put("iMemberId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {


                if (responseString != null && !responseString.equals("")) {

                    closeLoader();

                    loadAboutUsDetail(responseString);
                } else {
                    generateErrorView();
                }
            }
        });
        exeWebServer.execute();
    }

    public void loadAboutUsDetail(String aboutUsData) {
        String tPageDesc = generalFunc.getJsonValue("page_desc", aboutUsData);

        WebView view = new WebView(this);
        view.setVerticalScrollBarEnabled(false);

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        view.setLongClickable(false);
        view.setHapticFeedbackEnabled(false);

        container.addView(view);

        view.loadDataWithBaseURL(null, generalFunc.wrapHtml(view.getContext(), tPageDesc), "text/html", "UTF-8", null);
    }

    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                loadAboutUsData();
            }
        });
    }

    public Context getActContext() {
        return AboutUsActivity.this;
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}
