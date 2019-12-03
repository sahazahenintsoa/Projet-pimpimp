package com.pimpimp.passenger;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;

import com.VolleyLibFiles.AppController;
import com.general.files.DownloadImage;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.SetUserData;
import com.general.files.StartActProcess;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.ErrorView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.NetworkRoundedImageView;
import com.view.SelectableRoundedImageView;
import com.view.editBox.MaterialEditText;
import com.view.pinnedListView.CountryListItem;
import com.view.pinnedListView.PinnedSectionListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class RatingActivity extends AppCompatActivity {

    String vehicleIconPath = CommonUtilities.SERVER_URL + "webimages/icons/VehicleType/";

    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;

    ProgressBar loading;
    ErrorView errorView;
    MButton btn_type2;
    //    ImageView editCommentImgView;
//    MTextView commentBox;
    MTextView generalCommentTxt;
    MaterialEditText commentBox;

    int submitBtnId;

    String appliedComment = "";
    LinearLayout container;

    RatingBar ratingBar;
    String iTripId_str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        generalFunc = new GeneralFunctions(getActContext());


        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        loading = (ProgressBar) findViewById(R.id.loading);
        errorView = (ErrorView) findViewById(R.id.errorView);
//        editCommentImgView = (ImageView) findViewById(R.id.editCommentImgView);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        commentBox = (MaterialEditText) findViewById(R.id.commentBox);
//        commentBox = (MTextView) findViewById(R.id.commentBox);
        generalCommentTxt = (MTextView) findViewById(R.id.generalCommentTxt);
        container = (LinearLayout) findViewById(R.id.container);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);

        btn_type2.setOnClickListener(new setOnClickList());
//        editCommentImgView.setOnClickListener(new setOnClickList());
        backImgView.setVisibility(View.GONE);
        setLabels();

        getFare();

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleTxt.getLayoutParams();
        params.setMargins(Utils.dipToPixels(getActContext(), 15), 0, 0, 0);
        titleTxt.setLayoutParams(params);

        new CreateRoundedView(Color.parseColor("#FFFFFF"), Utils.dipToPixels(getActContext(), 4),
                Utils.dipToPixels(getActContext(), 2), Color.parseColor("#d2d2d2"), findViewById(R.id.commentArea));

        commentBox.setSingleLine(false);
        commentBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        commentBox.setGravity(Gravity.TOP);
        commentBox.setHideUnderline(true);
        commentBox.setFloatingLabel(MaterialEditText.FLOATING_LABEL_NONE);
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == submitBtnId) {
                if (ratingBar.getRating() < 1) {
                    generalFunc.showMessage(generalFunc.getCurrentView(RatingActivity.this), generalFunc.retrieveLangLBl("", "LBL_ERROR_RATING_DIALOG_TXT"));
                    return;
                }
                submitRating();
            }/* else if (i == editCommentImgView.getId()) {
                showCommentBox();
            }*/
        }
    }

    public Context getActContext() {
        return RatingActivity.this;
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RATING"));
//        commentBox.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_COMMENT_TXT"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));
        commentBox.setHint(generalFunc.retrieveLangLBl("", "LBL_WRITE_COMMENT_HINT_TXT"));
//        ((MTextView) findViewById(R.id.tripSummaryTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_TRIP_SUMMARY_TXT"));
        ((MTextView) findViewById(R.id.addCommentHTxt)).setText(generalFunc.retrieveLangLBl("How was ride?", "LBL_HOW_WAS_RIDE"));
        ((MTextView) findViewById(R.id.yourBillTxt)).setText(generalFunc.retrieveLangLBl("Your Bill", "LBL_YOUR_BILL"));
    }

    /*public void showCommentBox() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("", "LBL_ADD_COMMENT_HEADER_TXT"));

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.input_box_view, null);
        builder.setView(dialogView);

        final MaterialEditText input = (MaterialEditText) dialogView.findViewById(R.id.editBox);

        input.setSingleLine(false);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMaxLines(5);
        if (!appliedComment.equals("")) {
            input.setText(appliedComment);
        }
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Utils.getText(input).trim().equals("") && appliedComment.equals("")) {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_ENTER_PROMO"));
                } else if (Utils.getText(input).trim().equals("") && !appliedComment.equals("")) {
                    appliedComment = "";
                    commentBox.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_COMMENT_TXT"));
                    generalFunc.showGeneralMessage("", "Your comment has been removed.");
                } else {
                    appliedComment = Utils.getText(input);
                    commentBox.setText(appliedComment);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }*/

    public void getFare() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "displayFare");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", CommonUtilities.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    closeLoader();

//                    Utils.printLog("responseString", "responseString:" + responseString);

                    if (generalFunc.checkDataAvail(CommonUtilities.action_str, responseString) == true) {

                        String message = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);

                        String FormattedTripDate = generalFunc.getJsonValue("FormattedTripDate", message);
                        String TotalFare = generalFunc.getJsonValue("TotalFare", message);
                        String fDiscount = generalFunc.getJsonValue("fDiscount", message);
                        String vDriverImage = generalFunc.getJsonValue("vDriverImage", message);
                        String CurrencySymbol = generalFunc.getJsonValue("CurrencySymbol", message);
                        String iVehicleTypeId = generalFunc.getJsonValue("iVehicleTypeId", message);
                        String iDriverId = generalFunc.getJsonValue("iDriverId", message);
                        String tEndLat = generalFunc.getJsonValue("tEndLat", message);
                        String tEndLong = generalFunc.getJsonValue("tEndLong", message);
                        String eCancelled = generalFunc.getJsonValue("eCancelled", message);
                        String vCancelReason = generalFunc.getJsonValue("vCancelReason", message);
                        String vCancelComment = generalFunc.getJsonValue("vCancelComment", message);
                        String vCouponCode = generalFunc.getJsonValue("vCouponCode", message);
                        String carImageLogo = generalFunc.getJsonValue("carImageLogo", message);
                        String iTripId = generalFunc.getJsonValue("iTripId", message);
                        String eType = generalFunc.getJsonValue("eType", message);
                        iTripId_str = iTripId;

                        ((MTextView) findViewById(R.id.dateTxt)).setText(FormattedTripDate);
                        ((MTextView) findViewById(R.id.sAddr)).setText(generalFunc.getJsonValue("tSaddress", message));
                        ((MTextView) findViewById(R.id.dAddr)).setText(generalFunc.getJsonValue("tDaddress", message));
                        ((MTextView) findViewById(R.id.carType)).setText(generalFunc.getJsonValue("carTypeName", message));
                        ((MTextView) findViewById(R.id.fareTxt)).setText(CurrencySymbol + " " + TotalFare);

                        if (vDriverImage == null || vDriverImage.equals("") || vDriverImage.equals("NONE")) {
//                            ((SelectableRoundedImageView) findViewById(R.id.driverImgView)).setImageResource(R.mipmap.ic_no_pic_user);
                        } else {
                            String image_url = CommonUtilities.SERVER_URL_PHOTOS + "upload/Driver/" + iDriverId + "/"
                                    + vDriverImage;

//                            new DownloadImage(image_url, ((SelectableRoundedImageView) findViewById(R.id.driverImgView))).execute();
//                            Picasso.with(getActContext())
//                                    .load(image_url)
//                                    .placeholder(R.mipmap.ic_no_pic_user)
//                                    .error(R.mipmap.ic_no_pic_user)
//                                    .into(((SelectableRoundedImageView) findViewById(R.id.driverImgView)));
                        }


                        setImages(carImageLogo, iVehicleTypeId, tEndLat, tEndLong);

                        if (eCancelled.equals("Yes")) {
                            generalCommentTxt.setText(generalFunc.retrieveLangLBl("Trip is cancelled by driver. Reason:", "LBL_PREFIX_TRIP_CANCEL_DRIVER")
                                    + " " + vCancelReason);
                            generalCommentTxt.setVisibility(View.VISIBLE);
                        } else {
                            generalCommentTxt.setVisibility(View.GONE);
                        }

                        if (!fDiscount.equals("") && !fDiscount.equals("0")&& !fDiscount.equals("0.00")) {

                            ((MTextView) findViewById(R.id.promoAppliedTxt)).setText(CurrencySymbol + fDiscount + " " +
                                    generalFunc.retrieveLangLBl("discount applied", "LBL_DIS_APPLIED"));
//                            generalCommentTxt.setText(generalCommentTxt.getText().toString() + "\n" +
//                                    generalFunc.retrieveLangLBl("Promo code discount applied", "LBL_PROMO_DIS_APPLIED_PREFIX") +
//                                    " : " + CurrencySymbol + " " + fDiscount);
                            (findViewById(R.id.promoView)).setVisibility(View.VISIBLE);
                        }

                        new CreateRoundedView(Color.parseColor("#54A626"), Utils.dipToPixels(getActContext(), 9), 0, Color.parseColor("#54A626"),
                                findViewById(R.id.sourceRoundView));

                        new CreateRoundedView(Color.parseColor("#E7675A"), Utils.dipToPixels(getActContext(), 9), 0, Color.parseColor("#E7675A"),
                                findViewById(R.id.endRoundView));

                        container.setVisibility(View.VISIBLE);
                    } else {
                        generateErrorView();
                    }
                } else {
                    generateErrorView();
                }
            }
        });
        exeWebServer.execute();
    }

    public void setImages(String carImageLogo, String iVehicleTypeId, String endLatitude, String endLongitude) {
        String imageName = "";
        String size = "";
        switch (getResources().getDisplayMetrics().densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                imageName = "mdpi_" + carImageLogo;
                size = "80";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                imageName = "mdpi_" + carImageLogo;
                size = "80";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                imageName = "hdpi_" + carImageLogo;
                size = "120";
                break;
            case DisplayMetrics.DENSITY_TV:
                imageName = "hdpi_" + carImageLogo;
                size = "120";
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                imageName = "xhdpi_" + carImageLogo;
                size = "160";
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                imageName = "xxhdpi_" + carImageLogo;
                size = "240";
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                imageName = "xxxhdpi_" + carImageLogo;
                size = "320";
                break;
        }

        Utils.printLog("imageName", ":" + imageName);
//        new DownloadImage(vehicleIconPath + iVehicleTypeId + "/android/" + imageName, ((SelectableRoundedImageView) findViewById(R.id.carImgView))).execute();

        Picasso.with(getActContext())
                .load(vehicleIconPath + iVehicleTypeId + "/android/" + imageName)
                .placeholder(R.mipmap.ic_car_default)
                .error(R.mipmap.ic_car_default)
                .into(((ImageView) findViewById(R.id.carImgView)));

//        new DownloadImage(
//                "https://maps.googleapis.com/maps/api/staticmap?zoom=16&size=" + size + "x" + size + "&maptype=roadmap&markers=color:blue|label:Sorce|"
//                        + endLatitude + "," + endLongitude,
//                ((SelectableRoundedImageView) findViewById(R.id.locationImgView))).execute();
    }

    public void submitRating() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "submitRating");
        parameters.put("iGeneralUserId", generalFunc.getMemberId());
        parameters.put("tripID", iTripId_str);
        parameters.put("rating", "" + ratingBar.getRating());
        parameters.put("message", Utils.getText(commentBox));
        parameters.put("UserType", CommonUtilities.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(),parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Utils.printLog("Response", "::" + responseString);

                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    if (isDataAvail == true) {

                        buildMessage(generalFunc.retrieveLangLBl("", "LBL_TRIP_FINISHED_TXT"),
                                generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), true);

                    } else {
                        generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Error", "LBL_ERROR_TXT"),
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void buildMessage(String message, String positiveBtn, final boolean isRestart) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlert.closeAlertBox();
                if (isRestart == true) {
                    generalFunc.restartApp();
                }
            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(positiveBtn);
        generateAlert.showAlertBox();
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
                getFare();
            }
        });
    }


    @Override
    public void onBackPressed() {
        return;
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
