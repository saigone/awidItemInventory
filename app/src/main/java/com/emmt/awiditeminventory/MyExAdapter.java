package com.emmt.awiditeminventory;

import java.util.List;
import java.util.Map;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyExAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<Map<String, String>> groups; // 父集合
    private List<List<Map<String, String>>> childs; // 子集合
    private LayoutInflater mInflater; // layout轉換工具

    private int[][] picture = { { R.drawable.battery, R.drawable.power },
            { R.drawable.database, R.drawable.inventory, R.drawable.search }, {R.drawable.internet} };

    // private CharSequence [][] title = {{"版本", "溫度"}, {"盤點", "TID"}, {"音效"}};

    // private CharSequence [] groupTitle = {"讀取器資訊", "讀取器功能", "系統設定"};
	/*
	 * 構造函數: 參數1:context物件 參數2:一級清單資料來源 參數3:二級清單資料來源
	 */

    public MyExAdapter(Context context, List<Map<String, String>> groups,
                       List<List<Map<String, String>>> childs) {
        this.groups = groups;
        this.childs = childs;
        this.context = context;
        mInflater = LayoutInflater.from(context); // 初始化
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childs.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.adapter_child, null); // 產生一個layout

        ImageView image = (ImageView) convertView
                .findViewById(R.id.MyAdapter_ImageView_icon);
        TextView text = (TextView) convertView
                .findViewById(R.id.MyAdapter_TextView_title);
        TextView textInfo = (TextView) convertView
                .findViewById(R.id.MyAdapter_TextView_info);

        image.setBackgroundResource(picture[groupPosition][childPosition]); // 設定小圖示
        // text.setText(title[groupPosition][childPosition]);
        String txt = childs.get(groupPosition).get(childPosition)
                .get(ExpandAvtivity.CHILD_NAME); // 取得在ExpandAvtivity設定好的名稱
        text.setText(txt); // 設定txt

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childs.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        if (convertView == null)
            convertView = mInflater.inflate(R.layout.adapter_group, null);

        TextView txt = (TextView) convertView.findViewById(R.id.txtResult);
        txt.setText(groups.get(groupPosition).get(ExpandAvtivity.GROUP_NAME)); // 取得在ExpandAvtivity設定好的名稱

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // 要想讓child獲得焦點，改成傳回true
        return true;
    }

}
