package com.pimpimp.passenger;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.fragments.EditProfileFragment;
import com.fragments.ProfileFragment;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.OpenMainProfile;
import com.general.files.SetUserData;
import com.general.files.StartActProcess;
import com.general.files.UploadProfileImage;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.editBox.MaterialEditText;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MyProfileActivity extends AppCompatActivity {

    private static final String IMAGE_DIRECTORY_NAME = "Temp";
    public static final int MEDIA_TYPE_IMAGE = 1;

    private static final int SELECT_PICTURE = 2;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    private Uri fileUri;

    MTextView titleTxt;
    ImageView backImgView;

    public GeneralFunctions generalFunc;

    public String userProfileJson = "";

    SelectableRoundedImageView userProfileImgView;
    SelectableRoundedImageView editIconImgView;

    ProfileFragment profileFrag;
    EditProfileFragment editProfileFrag;

    RelativeLayout userImgArea;

    String SITE_TYPE = "";
    String SITE_TYPE_DEMO_MSG = "";
    Menu menu;

    android.support.v7.app.AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_profile);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);

        generalFunc = new GeneralFunctions(getActContext());

        userProfileJson = getIntent().getStringExtra("UserProfileJson");

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        userProfileImgView = (SelectableRoundedImageView) findViewById(R.id.userProfileImgView);
        editIconImgView = (SelectableRoundedImageView) findViewById(R.id.editIconImgView);
        userImgArea = (RelativeLayout) findViewById(R.id.userImgArea);

        backImgView.setOnClickListener(new setOnClickList());
        userImgArea.setOnClickListener(new setOnClickList());

        new CreateRoundedView(getResources().getColor(R.color.editBox_primary), Utils.dipToPixels(getActContext(), 15), 0,
                Color.parseColor("#00000000"), editIconImgView);

        editIconImgView.setColorFilter(getResources().getColor(R.color.appThemeColor_TXT_1));

        userProfileImgView.setImageResource(R.mipmap.ic_no_pic_user);
        generalFunc.checkProfileImage(userProfileImgView, userProfileJson, "vImgName");

        SITE_TYPE = generalFunc.getJsonValue("SITE_TYPE", userProfileJson);
        SITE_TYPE_DEMO_MSG = generalFunc.getJsonValue("SITE_TYPE_DEMO_MSG", userProfileJson);

        openProfileFragment();
    }

    public void changePageTitle(String title) {
        titleTxt.setText(title);
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.backImgView:
                    if (checkEditProfileFrag() == true) {
                        return;
                    }
                    MyProfileActivity.super.onBackPressed();
                    break;

                case R.id.userImgArea:
                    if (generalFunc.isCameraPermissionGranted()) {
                        new ImageSourceDialog().run();
                    } else {
                        generalFunc.showMessage(getCurrView(), "Allow this app to use camera.");
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.my_profile_activity, menu);
        if (editProfileFrag == null) {
            menu.findItem(R.id.menu_edit_profile).setTitle(generalFunc.retrieveLangLBl("", "LBL_EDIT_PROFILE_TXT"));
        } else {
            menu.findItem(R.id.menu_edit_profile).setTitle(generalFunc.retrieveLangLBl("", "LBL_VIEW_PROFILE_TXT"));
        }
        menu.findItem(R.id.menu_change_password).setTitle(generalFunc.retrieveLangLBl("", "LBL_CHANGE_PASSWORD_TXT"));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_edit_profile:
                if (editProfileFrag == null) {
                    openEditProfileFragment();
                    item.setTitle(generalFunc.retrieveLangLBl("", "LBL_VIEW_PROFILE_TXT"));
                } else {
                    checkEditProfileFrag();
                    item.setTitle(generalFunc.retrieveLangLBl("", "LBL_EDIT_PROFILE_TXT"));
                }
//                super.onCreateOptionsMenu(menu);
                return true;

            case R.id.menu_change_password:
                showPasswordBox();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showPasswordBox() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("", "LBL_CHANGE_PASSWORD_TXT"));

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.input_box_view, null);

        final String required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
        final String noWhiteSpace = generalFunc.retrieveLangLBl("Password should not contain whitespace.", "LBL_ERROR_NO_SPACE_IN_PASS");
        final String pass_length = generalFunc.retrieveLangLBl("Password must be", "LBL_ERROR_PASS_LENGTH_PREFIX")
                + " " + Utils.minPasswordLength + " " + generalFunc.retrieveLangLBl("or more character long.", "LBL_ERROR_PASS_LENGTH_SUFFIX");
        final String Passenger_Password_decrypt = generalFunc.getJsonValue("Passenger_Password_decrypt", userProfileJson);

        final MaterialEditText previous_passwordBox = (MaterialEditText) dialogView.findViewById(R.id.editBox);
        previous_passwordBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CURR_PASS_HEADER"));
        previous_passwordBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        if (Passenger_Password_decrypt.equals("")) {
            previous_passwordBox.setVisibility(View.GONE);
        }

        final MaterialEditText newPasswordBox = (MaterialEditText) inflater.inflate(R.layout.editbox_form_design, null);
        newPasswordBox.setLayoutParams(previous_passwordBox.getLayoutParams());
        newPasswordBox.setId(Utils.generateViewId());

        newPasswordBox.setFloatingLabelText(generalFunc.retrieveLangLBl("", "LBL_UPDATE_PASSWORD_HEADER_TXT"));
        newPasswordBox.setHint(generalFunc.retrieveLangLBl("", "LBL_UPDATE_PASSWORD_HINT_TXT"));
        newPasswordBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        final MaterialEditText reNewPasswordBox = (MaterialEditText) inflater.inflate(R.layout.editbox_form_design, null);
        reNewPasswordBox.setLayoutParams(previous_passwordBox.getLayoutParams());
        reNewPasswordBox.setId(Utils.generateViewId());
        reNewPasswordBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        reNewPasswordBox.setFloatingLabelText(generalFunc.retrieveLangLBl("", "LBL_UPDATE_CONFIRM_PASSWORD_HEADER_TXT"));
        reNewPasswordBox.setHint(generalFunc.retrieveLangLBl("", "LBL_UPDATE_CONFIRM_PASSWORD_HINT_TXT"));

        ((LinearLayout) dialogView).addView(newPasswordBox);
        ((LinearLayout) dialogView).addView(reNewPasswordBox);

        builder.setView(dialogView);
        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean isCurrentPasswordEnter = Utils.checkText(previous_passwordBox) ?
                        (Utils.getText(previous_passwordBox).contains(" ") ? Utils.setErrorFields(previous_passwordBox, noWhiteSpace)
                                : (Utils.getText(previous_passwordBox).length() >= Utils.minPasswordLength ? true : Utils.setErrorFields(previous_passwordBox, pass_length)))
                        : Utils.setErrorFields(previous_passwordBox, required_str);

                boolean isNewPasswordEnter = Utils.checkText(newPasswordBox) ?
                        (Utils.getText(newPasswordBox).contains(" ") ? Utils.setErrorFields(newPasswordBox, noWhiteSpace)
                                : (Utils.getText(newPasswordBox).length() >= Utils.minPasswordLength ? true : Utils.setErrorFields(newPasswordBox, pass_length)))
                        : Utils.setErrorFields(newPasswordBox, required_str);

                boolean isReNewPasswordEnter = Utils.checkText(reNewPasswordBox) ?
                        (Utils.getText(reNewPasswordBox).contains(" ") ? Utils.setErrorFields(reNewPasswordBox, noWhiteSpace)
                                : (Utils.getText(reNewPasswordBox).length() >= Utils.minPasswordLength ? true : Utils.setErrorFields(reNewPasswordBox, pass_length)))
                        : Utils.setErrorFields(reNewPasswordBox, required_str);

                if ((!Passenger_Password_decrypt.equals("") && isCurrentPasswordEnter == false) || isNewPasswordEnter == false || isReNewPasswordEnter == false) {
                    return;
                }

                if (!Passenger_Password_decrypt.equals("") && !Passenger_Password_decrypt.equals(Utils.getText(previous_passwordBox))) {
                    Utils.setErrorFields(previous_passwordBox, generalFunc.retrieveLangLBl("", "LBL_CURR_PASS_ERROR_TXT"));
                    return;
                }

                if (!Utils.getText(newPasswordBox).equals(Utils.getText(reNewPasswordBox))) {
                    Utils.setErrorFields(reNewPasswordBox, generalFunc.retrieveLangLBl("", "LBL_VERIFY_PASSWORD_ERROR_TXT"));
                    return;
                }

                alertDialog.dismiss();

                changePassword(Utils.getText(newPasswordBox));
            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

//        LinearLayout r = (LinearLayout) (alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)).getParent();
//        r.setOrientation(LinearLayout.VERTICAL);
//        LinearLayout containerButtons = (LinearLayout) alertDialog.findViewById(R.id.buttonPanel);
//        containerButtons.setOrientation(LinearLayout.VERTICAL);
    }

    public void changePassword(String password) {

        if (SITE_TYPE.equals("Demo")) {
            generalFunc.showGeneralMessage("", SITE_TYPE_DEMO_MSG);
            return;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "updatePassword");
        parameters.put("UserID", generalFunc.getMemberId());
        parameters.put("pass", password);
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
                        changeUserProfileJson(generalFunc.getJsonValue(CommonUtilities.message_str, responseString));
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

    public Context getActContext() {
        return MyProfileActivity.this;
    }

    public void openProfileFragment() {

        if (profileFrag != null) {
            profileFrag = null;
            Utils.runGC();
        }
        profileFrag = new ProfileFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragContainer, profileFrag).commit();
    }

    public void openEditProfileFragment() {

        if (editProfileFrag != null) {
            editProfileFrag = null;
            Utils.runGC();
        }
        editProfileFrag = new EditProfileFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragContainer, editProfileFrag).commit();
    }

    public boolean checkEditProfileFrag() {
        if (editProfileFrag != null) {
            editProfileFrag = null;
            Utils.runGC();
            openProfileFragment();
            return true;
        }

        return false;
    }

    public EditProfileFragment getEditProfileFrag() {
        return this.editProfileFrag;
    }

    public ProfileFragment getProfileFrag() {
        return this.profileFrag;
    }

    public void changeUserProfileJson(String userProfileJson) {
        this.userProfileJson = userProfileJson;

        Bundle bn = new Bundle();
        bn.putString("UserProfileJson", userProfileJson);

        generalFunc.storedata(CommonUtilities.WALLET_ENABLE, generalFunc.getJsonValue("WALLET_ENABLE", userProfileJson));
        generalFunc.storedata(CommonUtilities.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValue("REFERRAL_SCHEME_ENABLE", userProfileJson));

        new StartActProcess(getActContext()).setOkResult(bn);
        checkEditProfileFrag();
        generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("", "LBL_INFO_UPDATED_TXT"));
    }

    class ImageSourceDialog implements Runnable {

        @Override
        public void run() {
            final Dialog dialog_img_update = new Dialog(getActContext(), R.style.ImageSourceDialogStyle);

            dialog_img_update.setContentView(R.layout.design_image_source_select);

            MTextView chooseImgHTxt = (MTextView) dialog_img_update.findViewById(R.id.chooseImgHTxt);
            chooseImgHTxt.setText(generalFunc.retrieveLangLBl("Choose Category", "LBL_CHOOSE_CATEGORY"));

            SelectableRoundedImageView cameraIconImgView = (SelectableRoundedImageView) dialog_img_update.findViewById(R.id.cameraIconImgView);
            SelectableRoundedImageView galleryIconImgView = (SelectableRoundedImageView) dialog_img_update.findViewById(R.id.galleryIconImgView);

            ImageView closeDialogImgView = (ImageView) dialog_img_update.findViewById(R.id.closeDialogImgView);

            closeDialogImgView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (dialog_img_update != null) {
                        dialog_img_update.cancel();
                    }
                }
            });

            new CreateRoundedView(getResources().getColor(R.color.appThemeColor_Dark_1), Utils.dipToPixels(getActContext(), 25), 0,
                    Color.parseColor("#00000000"), cameraIconImgView);

            cameraIconImgView.setColorFilter(getResources().getColor(R.color.appThemeColor_TXT_1));

            new CreateRoundedView(getResources().getColor(R.color.appThemeColor_Dark_1), Utils.dipToPixels(getActContext(), 25), 0,
                    Color.parseColor("#00000000"), galleryIconImgView);

            galleryIconImgView.setColorFilter(getResources().getColor(R.color.appThemeColor_TXT_1));

            cameraIconImgView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (dialog_img_update != null) {
                        dialog_img_update.cancel();
                    }

                    if (!isDeviceSupportCamera()) {
                        generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("", "LBL_NOT_SUPPORT_CAMERA_TXT"));
                    } else {
                        chooseFromCamera();
                    }

                }
            });

            galleryIconImgView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (dialog_img_update != null) {
                        dialog_img_update.cancel();
                    }
                    chooseFromGallery();
                }
            });

            dialog_img_update.setCanceledOnTouchOutside(true);

            Window window = dialog_img_update.getWindow();
            window.setGravity(Gravity.BOTTOM);

            window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog_img_update.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog_img_update.show();

        }

    }

    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public void chooseFromGallery() {
        // System.out.println("Gallery pressed");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void chooseFromCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Utils.printLog(IMAGE_DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onBackPressed() {

        if (checkEditProfileFrag() == true) {
            return;
        }

        super.onBackPressed();
    }

    public View getCurrView() {
        return generalFunc.getCurrentView(MyProfileActivity.this);
    }

    public boolean isValidImageResolution(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);
        int width = options.outWidth;
        int height = options.outHeight;

        if (width >= Utils.ImageUpload_MINIMUM_WIDTH && height >= Utils.ImageUpload_MINIMUM_HEIGHT) {
            return true;
        }
        return false;
    }

    public String[] generateImageParams(String key, String content) {
        String[] tempArr = new String[2];
        tempArr[0] = key;
        tempArr[1] = content;

        return tempArr;
    }

    public String getPath(Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        try {
            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                return filePath;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        boolean isStoragePermissionAvail = generalFunc.isStoragePermissionGranted();
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view

                if (SITE_TYPE.equals("Demo")) {
                    generalFunc.showGeneralMessage("", SITE_TYPE_DEMO_MSG);
                    return;
                }
                ArrayList<String[]> paramsList = new ArrayList<>();
                paramsList.add(generateImageParams("iMemberId", generalFunc.getMemberId()));
                paramsList.add(generateImageParams("MemberType", CommonUtilities.app_type));
                paramsList.add(generateImageParams("tSessionId", generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(CommonUtilities.SESSION_ID_KEY)));
                paramsList.add(generateImageParams("GeneralUserType", CommonUtilities.app_type));
                paramsList.add(generateImageParams("GeneralMemberId", generalFunc.getMemberId()));
                paramsList.add(generateImageParams("type", "uploadImage"));

                if (isValidImageResolution(fileUri.getPath()) == true && isStoragePermissionAvail) {
                    new UploadProfileImage(MyProfileActivity.this, fileUri.getPath(), Utils.TempProfileImageName, paramsList).execute();
                } else {
                    generalFunc.showGeneralMessage("", "Please select greater resolution image. Minimum is 256 * 256.");
                }

            } else if (resultCode == RESULT_CANCELED) {

            } else {
                generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("", "LBL_FAILED_CAPTURE_IMAGE_TXT"));
            }
        }
        if (requestCode == SELECT_PICTURE) {
            if (resultCode == RESULT_OK) {

                if (SITE_TYPE.equals("Demo")) {
                    generalFunc.showGeneralMessage("", SITE_TYPE_DEMO_MSG);
                    return;
                }

                ArrayList<String[]> paramsList = new ArrayList<>();
                paramsList.add(generateImageParams("iMemberId", generalFunc.getMemberId()));
                paramsList.add(generateImageParams("MemberType", CommonUtilities.app_type));
                paramsList.add(generateImageParams("tSessionId", generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(CommonUtilities.SESSION_ID_KEY)));
                paramsList.add(generateImageParams("GeneralUserType", CommonUtilities.app_type));
                paramsList.add(generateImageParams("GeneralMemberId", generalFunc.getMemberId()));


                paramsList.add(generateImageParams("type", "uploadImage"));

                Uri selectedImageUri = data.getData();

                String selectedImagePath = (getPath(selectedImageUri) == null) ? selectedImageUri.getPath() : getPath(selectedImageUri);

//                String selectedImagePath = getPath(selectedImageUri);
//                if (getPath(selectedImageUri) == null) {
//                    selectedImagePath = selectedImageUri.getPath();
//                }

                if (isValidImageResolution(selectedImagePath) == true && isStoragePermissionAvail) {

                    new UploadProfileImage(MyProfileActivity.this, selectedImagePath, Utils.TempProfileImageName, paramsList).execute();
                } else {
                    generalFunc.showGeneralMessage("", "Please select image which has minimum is 256 * 256 resolution.");
                }

            }
        }
    }

    public void handleImgUploadResponse(String responseString) {

        if (responseString != null && !responseString.equals("")) {

            boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

            if (isDataAvail == true) {
                String userProfileJson = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                changeUserProfileJson(userProfileJson);

                generalFunc.checkProfileImage(userProfileImgView, userProfileJson, "vImgName");
            } else {
                generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Error", "LBL_ERROR_TXT"),
                        generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
            }
        } else {
            generalFunc.showError();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        setLablesAsPerCurrentFrag(menu);
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        setLablesAsPerCurrentFrag(menu);

        return super.onPrepareOptionsMenu(menu);
    }

    public void setLablesAsPerCurrentFrag(Menu menu) {
        if (menu != null) {
            if (editProfileFrag == null) {
                menu.findItem(R.id.menu_edit_profile).setTitle(generalFunc.retrieveLangLBl("", "LBL_EDIT_PROFILE_TXT"));
            } else {
                menu.findItem(R.id.menu_edit_profile).setTitle(generalFunc.retrieveLangLBl("", "LBL_VIEW_PROFILE_TXT"));
            }
        }
    }
}
