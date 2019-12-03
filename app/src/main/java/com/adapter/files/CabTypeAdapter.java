package com.adapter.files;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.general.files.GeneralFunctions;
import com.squareup.picasso.Picasso;
import com.pimpimp.passenger.R;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.anim.loader.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 04-07-2016.
 */
public class CabTypeAdapter extends RecyclerView.Adapter<CabTypeAdapter.ViewHolder> {

    ArrayList<HashMap<String, String>> list_item;
    Context mContext;
    public GeneralFunctions generalFunc;

    String vehicleIconPath = CommonUtilities.SERVER_URL + "webimages/icons/VehicleType/";

    OnItemClickList onItemClickList;

    boolean isFirstRun = true;
    ViewHolder viewHolder;

    public CabTypeAdapter(Context mContext, ArrayList<HashMap<String, String>> list_item, GeneralFunctions generalFunc) {
        this.mContext = mContext;
        this.list_item = list_item;
        this.generalFunc = generalFunc;
    }

    @Override
    public CabTypeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_design_cab_type, parent, false);

         viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        setData(viewHolder, position, false);
    }

    public void setData(CabTypeAdapter.ViewHolder viewHolder, final int position, boolean isHover) {
        HashMap<String, String> item = list_item.get(position);

        viewHolder.carTypeTitle.setText(item.get("vVehicleType"));

        isHover = item.get("isHover").equals("true") ? true : false;

        String imageName = "";
        switch (mContext.getResources().getDisplayMetrics().densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                if (isHover == true) {
                    imageName = "mdpi_hover_" + item.get("vLogo");
                } else {
                    imageName = "mdpi_" + item.get("vLogo");
                }
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                if (isHover == true) {
                    imageName = "mdpi_hover_" + item.get("vLogo");
                } else {
                    imageName = "mdpi_" + item.get("vLogo");
                }

                break;
            case DisplayMetrics.DENSITY_HIGH:
                if (isHover == true) {
                    imageName = "hdpi_hover_" + item.get("vLogo");
                } else {
                    imageName = "hdpi_" + item.get("vLogo");
                }

                break;
            case DisplayMetrics.DENSITY_TV:
                if (isHover == true) {
                    imageName = "hdpi_hover_" + item.get("vLogo");
                } else {
                    imageName = "hdpi_" + item.get("vLogo");
                }

                break;
            case DisplayMetrics.DENSITY_XHIGH:
                if (isHover == true) {
                    imageName = "xhdpi_hover_" + item.get("vLogo");
                } else {
                    imageName = "xhdpi_" + item.get("vLogo");
                }

                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                if (isHover == true) {
                    imageName = "xxhdpi_hover_" + item.get("vLogo");
                } else {
                    imageName = "xxhdpi_" + item.get("vLogo");
                }

                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                if (isHover == true) {
                    imageName = "xxxhdpi_hover_" + item.get("vLogo");
                } else {
                    imageName = "xxxhdpi_" + item.get("vLogo");
                }

                break;
        }

        Utils.printLog("imageName", ":" + imageName);

        loadImage(item, imageName);

        if (position == 0) {
            viewHolder.leftSeperationLine.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.leftSeperationLine.setVisibility(View.VISIBLE);
        }

        if (position == list_item.size() - 1) {
            viewHolder.rightSeperationLine.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.rightSeperationLine.setVisibility(View.VISIBLE);
        }

        viewHolder.contentArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickList != null) {
                    onItemClickList.onItemClick(position, "Type");
                }
            }
        });

        if (isHover == true) {
            viewHolder.carTypeTitle.setTextColor(mContext.getResources().getColor(R.color.appThemeColor_2));
            new CreateRoundedView(Color.parseColor("#F5F5F5"), Utils.dipToPixels(mContext, 30), 2,
                    mContext.getResources().getColor(R.color.appThemeColor_2), viewHolder.carTypeImgView);
            viewHolder.carTypeImgView.setBorderColor(mContext.getResources().getColor(R.color.appThemeColor_2));
        } else {
            viewHolder.carTypeTitle.setTextColor(Color.parseColor("#BABABA"));
            new CreateRoundedView(Color.parseColor("#FFFFFF"), Utils.dipToPixels(mContext, 30), 2,
                    Color.parseColor("#BABABA"), viewHolder.carTypeImgView);
            viewHolder.carTypeImgView.setBorderColor(Color.parseColor("#BABABA"));
        }


    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public SelectableRoundedImageView carTypeImgView;
        public MTextView carTypeTitle;
        public View leftSeperationLine;
        public View rightSeperationLine;
        public RelativeLayout contentArea;
        public AVLoadingIndicatorView loaderView;

        public ViewHolder(View view) {
            super(view);

            carTypeImgView = (SelectableRoundedImageView) view.findViewById(R.id.carTypeImgView);
            carTypeTitle = (MTextView) view.findViewById(R.id.carTypeTitle);
            leftSeperationLine = view.findViewById(R.id.leftSeperationLine);
            rightSeperationLine = view.findViewById(R.id.rightSeperationLine);
            contentArea = (RelativeLayout) view.findViewById(R.id.contentArea);
            loaderView = (AVLoadingIndicatorView) view.findViewById(R.id.loaderView);
        }
    }


    private void loadImage(HashMap<String, String> item, String imageName) {

        Picasso.with(mContext)
                .load(vehicleIconPath + item.get("iVehicleTypeId") + "/android/" + imageName)
                .into(viewHolder.carTypeImgView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        viewHolder.loaderView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        viewHolder.loaderView.setVisibility(View.VISIBLE);
                    }
                });
    }
    @Override
    public int getItemCount() {
        return list_item.size();
    }

    public interface OnItemClickList {
        void onItemClick(int position, String from);
    }

    public void setOnItemClickList(OnItemClickList onItemClickList) {
        this.onItemClickList = onItemClickList;
    }

    public void clickOnItem(int position) {
        if (onItemClickList != null) {
            onItemClickList.onItemClick(position, "Type");
        }
    }
}

