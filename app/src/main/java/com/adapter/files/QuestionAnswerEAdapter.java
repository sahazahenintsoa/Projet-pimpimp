package com.adapter.files;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.GeneralFunctions;
import com.pimpimp.passenger.QuestionAnswerActivity;
import com.pimpimp.passenger.R;
import com.view.MTextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Admin on 17-05-2016.
 */
public class QuestionAnswerEAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader;
    private GeneralFunctions generalFunc;

    private HashMap<String, List<String>> _listDataChild;

    public QuestionAnswerEAdapter(Context context, List<String> listDataHeader, HashMap<String, List<String>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        generalFunc=new GeneralFunctions(context);
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }



    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public String wrapHtml(Context context, String html) {
        return context.getString(R.string.html, html);
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);


        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.help_answers_list_item, null);
        }

        LinearLayout container = (LinearLayout) convertView.findViewById(R.id.container);
        WebView webView = new WebView(_context);
        webView.setVerticalScrollBarEnabled(false);
        webView.setLongClickable(false);
        webView.setHapticFeedbackEnabled(false);
        container.removeAllViewsInLayout();
        container.addView(webView);


        if (((QuestionAnswerActivity)_context).lastGroupId!=-1 && ((QuestionAnswerActivity)_context).lastGroupId==childPosition)
        {
            container.setVisibility(View.GONE);
        }


        webView.loadDataWithBaseURL(null,generalFunc.wrapHtml(webView.getContext(), childText),
                "text/html", "UTF-8", null);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        convertView=null;
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.questions_answer_list_header,
                    null);
        }

        MTextView lblListHeader = (MTextView) convertView.findViewById(R.id.questionTxt);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        ImageView indicatorImg = (ImageView) convertView.findViewById(R.id.indicatorImg);

        if (isExpanded) {
            indicatorImg.setImageResource(R.mipmap.ic_help_question_expand);
        } else {
            indicatorImg.setImageResource(R.mipmap.ic_help_question_collapse);
        }

        if (((QuestionAnswerActivity)_context).lastGroupId!=-1 && ((QuestionAnswerActivity)_context).lastGroupId==groupPosition)
        {
            convertView.findViewById(R.id.mainArea).setVisibility(View.GONE);
        }

        return convertView;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }


}

