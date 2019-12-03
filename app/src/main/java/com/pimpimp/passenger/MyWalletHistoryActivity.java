package com.pimpimp.passenger;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.adapter.files.WalletHistoryRecycleAdapter;
import com.general.files.DividerItemDecoration;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.utils.CommonUtilities;
import com.view.ErrorView;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 04-11-2016.
 */
public class MyWalletHistoryActivity extends AppCompatActivity implements WalletHistoryRecycleAdapter.OnItemClickListener {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    ProgressBar loading_transaction_history;
    MTextView noTransactionTxt;
    MTextView transactionsTxt;

    RecyclerView walletHistoryRecyclerView;
    ErrorView errorView;


    ArrayList<HashMap<String, String>> list = new ArrayList<>();

    boolean mIsLoading = false;
    boolean isNextPageAvailable = false;

    String next_page_str = "";

    private WalletHistoryRecycleAdapter wallethistoryRecyclerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mywallet_history);


        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        loading_transaction_history = (ProgressBar) findViewById(R.id.loading_transaction_history);
        noTransactionTxt = (MTextView) findViewById(R.id.noTransactionTxt);
        transactionsTxt = (MTextView) findViewById(R.id.transactionsTxt);
        walletHistoryRecyclerView = (RecyclerView) findViewById(R.id.walletTransactionRecyclerView);
        errorView = (ErrorView) findViewById(R.id.errorView);

        generalFunc = new GeneralFunctions(getActContext());


        list = new ArrayList<>();
        wallethistoryRecyclerAdapter = new WalletHistoryRecycleAdapter(getActContext(), list, generalFunc, false);
        walletHistoryRecyclerView.setAdapter(wallethistoryRecyclerAdapter);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActContext(), DividerItemDecoration.VERTICAL_LIST);
        walletHistoryRecyclerView.addItemDecoration(itemDecoration);

        wallethistoryRecyclerAdapter.setOnItemClickListener(this);

        backImgView.setOnClickListener(new setOnClickList());

        setLabels();

        walletHistoryRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                int lastInScreen = firstVisibleItemPosition + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable == true) {

                    mIsLoading = true;
                    wallethistoryRecyclerAdapter.addFooterView();

                    getTransactionHistory(true);

                } else if (isNextPageAvailable == false) {
                    wallethistoryRecyclerAdapter.removeFooterView();
                }
            }
        });

        getTransactionHistory(false);
    }

    public void setLabels() {

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_Transaction_HISTORY"));

    }

    public void removeNextPageConfig() {
        next_page_str = "";
        isNextPageAvailable = false;
        mIsLoading = false;
        wallethistoryRecyclerAdapter.removeFooterView();
    }

    public void closeLoader() {
        if (loading_transaction_history.getVisibility() == View.VISIBLE) {
            loading_transaction_history.setVisibility(View.GONE);
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
                getTransactionHistory(false);
            }
        });
    }

    public void getTransactionHistory(final boolean isLoadMore) {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading_transaction_history.getVisibility() != View.VISIBLE && isLoadMore == false) {
            loading_transaction_history.setVisibility(View.VISIBLE);
        }

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getTransactionHistory");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", CommonUtilities.app_type);
        if (isLoadMore == true) {
            parameters.put("page", next_page_str);
        }

        noTransactionTxt.setVisibility(View.GONE);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                noTransactionTxt.setVisibility(View.GONE);
                if (responseString != null && !responseString.equals("")) {

                    closeLoader();


                    if (generalFunc.checkDataAvail(CommonUtilities.action_str, responseString) == true) {

                        String nextPage = generalFunc.getJsonValue("NextPage", responseString);
                        JSONArray arr_transhistory = generalFunc.getJsonArray(CommonUtilities.message_str, responseString);

                        if (arr_transhistory != null && arr_transhistory.length() > 0) {
                            for (int i = 0; i < arr_transhistory.length(); i++) {
                                JSONObject obj_temp = generalFunc.getJsonObject(arr_transhistory, i);
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("iUserWalletId", generalFunc.getJsonValue("iUserWalletId", obj_temp.toString()));
                                map.put("iUserId", generalFunc.getJsonValue("iUserId", obj_temp.toString()));
                                map.put("eUserType", generalFunc.getJsonValue("eUserType", obj_temp.toString()));
                                map.put("iBalance", generalFunc.getJsonValue("iBalance", obj_temp.toString()));
                                map.put("eType", generalFunc.getJsonValue("eType", obj_temp.toString()));
                                map.put("iTripId", generalFunc.getJsonValue("iTripId", obj_temp.toString()));
                                map.put("eFor", generalFunc.getJsonValue("eFor", obj_temp.toString()));
                                map.put("tDescription", generalFunc.getJsonValue("tDescription", obj_temp.toString()));
                                map.put("ePaymentStatus", generalFunc.getJsonValue("ePaymentStatus", obj_temp.toString()));
                                map.put("dDate", generalFunc.getJsonValue("dDate", obj_temp.toString()));
                                map.put("currentbal", generalFunc.getJsonValue("currentbal", obj_temp.toString()));
                                map.put("LBL_Status", generalFunc.retrieveLangLBl("", "LBL_Status"));
                                map.put("LBL_TRIP_NO", generalFunc.retrieveLangLBl("", "LBL_TRIP_NO"));
                                map.put("LBL_BALANCE_TYPE", generalFunc.retrieveLangLBl("", "LBL_BALANCE_TYPE"));
                                map.put("LBL_DESCRIPTION", generalFunc.retrieveLangLBl("", "LBL_DESCRIPTION"));
                                map.put("LBL_AMOUNT", generalFunc.retrieveLangLBl("", "LBL_AMOUNT"));
                                list.add(map);
                            }
                        }

                        String LBL_BALANCE = generalFunc.getJsonValue("user_available_balance", responseString);

                        ((MTextView) findViewById(R.id.yourBalTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_USER_BALANCE"));


                        ((MTextView) findViewById(R.id.walletamountTxt)).setText(LBL_BALANCE);


                        if (!nextPage.equals("") && !nextPage.equals("0")) {
                            next_page_str = nextPage;
                            isNextPageAvailable = true;
                        } else {
                            removeNextPageConfig();
                        }

                        wallethistoryRecyclerAdapter.notifyDataSetChanged();
                    } else {
                        if (list.size() == 0) {
                            removeNextPageConfig();
                            noTransactionTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                            noTransactionTxt.setVisibility(View.VISIBLE);
                        }
                    }

                    wallethistoryRecyclerAdapter.notifyDataSetChanged();


                } else {
                    if (isLoadMore == false) {
                        removeNextPageConfig();
                        generateErrorView();
                    }

                }

                mIsLoading = false;
            }
        });
        exeWebServer.execute();
    }

    public Context getActContext() {
        return MyWalletHistoryActivity.this;
    }

    @Override
    public void onItemClickList(View v, int position) {


    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.backImgView:
                    MyWalletHistoryActivity.super.onBackPressed();
                    break;

            }
        }
    }

}

