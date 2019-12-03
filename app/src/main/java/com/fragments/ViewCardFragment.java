package com.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.general.files.GeneralFunctions;
import com.pimpimp.passenger.CardPaymentActivity;
import com.pimpimp.passenger.R;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewCardFragment extends Fragment {

    GeneralFunctions generalFunc;
    View view;

    CardPaymentActivity cardPayAct;

    String userProfileJson;
    MButton btn_type2;
    MButton btn_type2_change;

    boolean isDemoMsgShown = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_view_card, container, false);

        cardPayAct = (CardPaymentActivity) getActivity();
        generalFunc = cardPayAct.generalFunc;
        userProfileJson = cardPayAct.userProfileJson;
        btn_type2 = ((MaterialRippleLayout) view.findViewById(R.id.btn_type2)).getChildView();
        btn_type2_change = ((MaterialRippleLayout) view.findViewById(R.id.btn_type2_change)).getChildView();

        checkData();
        setLabels();

        cardPayAct.changePageTitle(generalFunc.retrieveLangLBl("", "LBL_CARD_PAYMENT_DETAILS"));

        btn_type2.setId(Utils.generateViewId());
        btn_type2_change.setId(Utils.generateViewId());

        btn_type2.setOnClickListener(new setOnClickList());
        btn_type2_change.setOnClickListener(new setOnClickList());



        if(generalFunc.getJsonValue("SITE_TYPE",userProfileJson).equalsIgnoreCase("Demo")){
            SpannableStringBuilder builder = new SpannableStringBuilder();

            String content = cardPayAct.getResources().getString(R.string.demo_text);
            SpannableString redSpannable= new SpannableString(content);
            redSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, 4, 0);
            builder.append(redSpannable);

            (view.findViewById(R.id.demoText)).setVisibility(View.VISIBLE);
            ((MTextView) view.findViewById(R.id.demoText)).setText(builder, TextView.BufferType.SPANNABLE);
        }else{
            (view.findViewById(R.id.demoText)).setVisibility(View.GONE);
        }

        return view;
    }

    public void checkData() {

//        String STRIPE_PUBLISH_KEY = generalFunc.getJsonValue("STRIPE_PUBLISH_KEY", userProfileJson);
        String vStripeToken = generalFunc.getJsonValue("vStripeToken", userProfileJson);
        String vCreditCard = generalFunc.getJsonValue("vCreditCard", userProfileJson);
        String vStripeCusId = generalFunc.getJsonValue("vStripeCusId", userProfileJson);

        if (vStripeCusId.equals("")) {
            (view.findViewById(R.id.cardAddArea)).setVisibility(View.VISIBLE);
            (view.findViewById(R.id.cardViewArea)).setVisibility(View.GONE);
        } else {
            (view.findViewById(R.id.cardAddArea)).setVisibility(View.GONE);
            (view.findViewById(R.id.cardViewArea)).setVisibility(View.VISIBLE);
            ((MTextView) view.findViewById(R.id.cardTxt)).setText(vCreditCard);
        }


    }

    public void setLabels() {
        ((MTextView) view.findViewById(R.id.noCardAvailTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_NO_CARD_AVAIL_NOTE"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_CARD"));
        btn_type2_change.setText(generalFunc.retrieveLangLBl("", "LBL_CHANGE"));
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();

            String vTripStatus = generalFunc.getJsonValue("vTripStatus",userProfileJson);
            if(!vTripStatus.equals("Not Active") && !vTripStatus.equals("NONE") && !vTripStatus.equals("Not Requesting")
                    && !vTripStatus.equals("Cancelled")){

                generalFunc.showGeneralMessage("",generalFunc.retrieveLangLBl("","LBL_DIS_ALLOW_EDIT_CARD"));
                return;
            }

            if (i == btn_type2.getId()) {
                cardPayAct.openAddCardFrag("ADD_CARD");
            } else if (i == btn_type2_change.getId()) {
                cardPayAct.openAddCardFrag("EDIT_CARD");
            }
        }
    }

}
