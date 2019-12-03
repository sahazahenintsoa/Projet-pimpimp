package com.general.files;

import android.content.Context;
import android.os.AsyncTask;

import com.pimpimp.passenger.MyProfileActivity;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.MyProgressDialog;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;

public class UploadProfileImage extends AsyncTask<String, String, String> {

    String selectedImagePath;
    String responseString = "";

    String temp_File_Name = "";
    ArrayList<String[]> paramsList;

    MyProfileActivity myProfileAct;
    MyProgressDialog myPDialog;

    public UploadProfileImage(MyProfileActivity myProfileAct, String selectedImagePath, String temp_File_Name, ArrayList<String[]> paramsList) {
        this.selectedImagePath = selectedImagePath;
        this.temp_File_Name = temp_File_Name;
        this.paramsList = paramsList;
        this.myProfileAct=myProfileAct;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        myPDialog = new MyProgressDialog(myProfileAct.getActContext(), false, myProfileAct.generalFunc.retrieveLangLBl("Loading", "LBL_LOADING_TXT"));
        myPDialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        String filePath = myProfileAct.generalFunc.decodeFile(selectedImagePath, Utils.ImageUpload_DESIREDWIDTH, Utils.ImageUpload_DESIREDHEIGHT, temp_File_Name);
        try {
            responseString = new ExecuteResponse().uploadImageAsFile(filePath, temp_File_Name, "vImage", paramsList);
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        myPDialog.close();
        myProfileAct.handleImgUploadResponse(responseString);
    }

}
