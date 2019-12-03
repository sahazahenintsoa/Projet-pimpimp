package com.pimpimp.passenger;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adapter.files.ViewPagerAdapter;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.widget.LoginButton;
import com.fragments.SignInFragment;
import com.fragments.SignUpFragment;
import com.general.files.GeneralFunctions;
import com.general.files.RegisterFbLoginResCallBack;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.utils.CommonUtilities;
import com.view.MTextView;
import com.view.MaterialTabs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static com.pimpimp.passenger.R.string.app_name;
import static com.pimpimp.passenger.R.string.forgot;

public class AppLoginActivity extends AppCompatActivity {

   public GeneralFunctions generalFunc;

    ArrayList<Fragment> fragmentList;

    CallbackManager callbackManager;
    LoginButton loginButton;

    MTextView fbTxt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);







        FacebookSdk.sdkInitialize(getActContext());
        FacebookSdk.setWebDialogTheme(R.style.FBDialogtheme);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN );
        setContentView(R.layout.activity_app_login);
        TextView text = (TextView) findViewById(R.id.lien);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        generalFunc = new GeneralFunctions(getActContext());
         text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             startActivity(new Intent(AppLoginActivity.this,forgot.class));
            }
        });
        //methode2
       /* TextView t = (TextView) findViewById(R.id.link);
        t.setMovementMethod(LinkMovementMethod.getInstance());
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             startActivity(new Intent(AppLoginActivity.this,forgot.class));
            }
        });
*/
        FacebookSdk.setApplicationId(generalFunc.retrieveValue(CommonUtilities.FACEBOOK_APPID_KEY));

        fbTxt = (MTextView) findViewById(R.id.fbTxt);

        ViewPager appLogin_view_pager = (ViewPager) findViewById(R.id.appLogin_view_pager);
        MaterialTabs material_tabs = (MaterialTabs) findViewById(R.id.material_tabs);

        CharSequence titles[] = {generalFunc.retrieveLangLBl("", "LBL_SIGN_IN_TXT"), generalFunc.retrieveLangLBl("", "LBL_SIGN_UP")};

        fragmentList = new ArrayList<>();
        fragmentList.add(new SignInFragment());
        fragmentList.add(new SignUpFragment());
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, fragmentList);

        appLogin_view_pager.setAdapter(adapter);
        material_tabs.setViewPager(appLogin_view_pager);

        loginButton = new LoginButton(getActContext());

        callbackManager = CallbackManager.Factory.create();

        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_friends", "user_about_me"));

        loginButton.registerCallback(callbackManager, new RegisterFbLoginResCallBack(getActContext()));
        fbTxt.setOnClickListener(new setOnClickAct());

        fbTxt.setText(generalFunc.retrieveLangLBl("Facebook","LBL_FACEBOOK"));
        ((MTextView) findViewById(R.id.orLoginWithTxt)).setText(generalFunc.retrieveLangLBl("Or Sign in with","LBL_OR_SIGN_IN_WITH"));
    }

    public class setOnClickAct implements View.OnClickListener {


        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.fbTxt) {
                loginButton.performClick();
            }
        }
    }

    public Context getActContext() {
        return AppLoginActivity.this;
    }

    public SignUpFragment getSignUpFrag(){
        return ((SignUpFragment) fragmentList.get(1));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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


