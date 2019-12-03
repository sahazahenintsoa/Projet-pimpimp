package com.pimpimp.passenger;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import com.adapter.files.QuestionAnswerEAdapter;
import com.general.files.GeneralFunctions;
import com.utils.Utils;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuestionAnswerActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;

    public GeneralFunctions generalFunc;

    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    ExpandableListView expandableList;

    QuestionAnswerEAdapter adapter;

    private int lastExpandedPosition = -1;
    public int lastGroupId=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_answer);

        generalFunc = new GeneralFunctions(getActContext());


        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        expandableList = (ExpandableListView) findViewById(R.id.list);

        expandableList.setDividerHeight(2);
        expandableList.setClickable(true);

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        adapter = new QuestionAnswerEAdapter(getActContext(), listDataHeader, listDataChild);
        expandableList.setAdapter(adapter);
        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                setListViewHeight(parent, groupPosition);
                return false;
            }
        });
        expandableList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    expandableList.collapseGroup(lastExpandedPosition);
                }

                lastExpandedPosition = groupPosition;
            }
        });

        // Listview Group collapsed listener
        expandableList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
              /*  int height = expandableList.getHeight();
                expandableList.getLayoutParams().height = height;*/
            }
        });

        backImgView.setOnClickListener(new setOnClickList());

        setData();
    }


    private void setListViewHeight(ExpandableListView listView,
                                   int group) {
        ExpandableListAdapter listAdapter = (ExpandableListAdapter) listView.getExpandableListAdapter();
        int totalHeight = (50*listAdapter.getGroupCount());
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.EXACTLY);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listView);
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            totalHeight += groupItem.getMeasuredHeight();


            if (((listView.isGroupExpanded(i)) && (i != group))
                    || ((!listView.isGroupExpanded(i)) && (i == group))) {
                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                    View listItem = listAdapter.getChildView(i, j, false, null,
                            listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

                    totalHeight += listItem.getMeasuredHeight();

                }
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight + (listView.getDividerHeight() * (listAdapter.getGroupCount()));
        if (height < 10)
            height = 200;

        params.height = height ;
        listView.setLayoutParams(params);
        listView.requestLayout();

    }

    public void setData() {

        titleTxt.setText(generalFunc.getJsonValue("vTitle", getIntent().getStringExtra("QUESTION_LIST")));
        JSONArray obj_ques = generalFunc.getJsonArray("Questions", getIntent().getStringExtra("QUESTION_LIST"));
        for (int i = 0; i < obj_ques.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(obj_ques, i);


            listDataHeader.add(generalFunc.getJsonValue("vTitle", obj_temp.toString()));

            List<String> answer = new ArrayList<String>();
            answer.add(generalFunc.getJsonValue("tAnswer", obj_temp.toString()));

            listDataChild.put(listDataHeader.get(i), answer);
        }


        listDataHeader.add(generalFunc.getJsonValue("vTitle", "Test"));

        List<String> answer = new ArrayList<String>();
        answer.add(generalFunc.getJsonValue("tAnswer", "Test"));
        listDataChild.put(listDataHeader.get(obj_ques.length()), answer);
        lastGroupId=obj_ques.length();
        adapter.notifyDataSetChanged();
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.backImgView:
                    QuestionAnswerActivity.super.onBackPressed();
                    break;

            }
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
    public Context getActContext() {
        return QuestionAnswerActivity.this;
    }
}
