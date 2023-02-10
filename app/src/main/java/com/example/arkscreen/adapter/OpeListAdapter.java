package com.example.arkscreen.adapter;

import android.content.Context;
import android.graphics.Path;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.arkscreen.R;
import com.example.arkscreen.Utils.OpeData;
import com.example.arkscreen.database.Operator;

import java.util.List;

public class OpeListAdapter extends BaseAdapter {

    private List<OpeData> data;
    private LayoutInflater layoutInflater;
    private Context mContext;
    private OpeData opeData;

    public OpeListAdapter(Context context, List<OpeData> data){
        this.data = data;
        this.mContext = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    static class ViewHolder{
        LinearLayout listTags;
        LinearLayout listOpe;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_each, null);
            viewHolder = new ViewHolder();
            viewHolder.listTags = convertView.findViewById(R.id.linear_tags);
            viewHolder.listOpe = convertView.findViewById(R.id.linear_ope);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        opeData = null;
        opeData = data.get(position);

        viewHolder.listTags.removeAllViews();
        for(int i = 0;i<opeData.tags.size();i++){
            String tag = opeData.tags.get(i);
            TextView tagText = new TextView(mContext);
            tagText.setPadding(10,10,10,10);
            tagText.setBackgroundColor(ContextCompat.getColor(mContext,R.color.blue));
            tagText.setTextColor(ContextCompat.getColor(mContext,R.color.white));
            tagText.setText(tag);
            viewHolder.listTags.addView(tagText);
        }
        viewHolder.listOpe.removeAllViews();
        for(int i = 0;i<opeData.operatorList.size();i++){
            Operator operator = opeData.operatorList.get(i);
            View opeView = layoutInflater.inflate(R.layout.operator,null);
            TextView opeName = opeView.findViewById(R.id.text_operator_name);
            TextView opeClass = opeView.findViewById(R.id.text_operator_class);
            TextView opeStar = opeView.findViewById(R.id.text_operator_star);
            switch(operator.opeStar){
                case "6":opeName.setTextColor(ContextCompat.getColor(mContext,R.color.star_6));break;
                case "5":opeName.setTextColor(ContextCompat.getColor(mContext,R.color.star_5));break;
                case "4":opeName.setTextColor(ContextCompat.getColor(mContext,R.color.star_4));break;
                case "1":opeName.setTextColor(ContextCompat.getColor(mContext,R.color.star_1));break;
            }
            opeName.setText(operator.opeName);
            opeClass.setText(operator.opeClass);
            opeStar.setText(operator.opeStar+"★");
            opeView.getMeasuredWidth();
            viewHolder.listOpe.addView(opeView);
        }
        return convertView;
    }

    @Override
    public int getCount() {
        if (data == null) {
            return 0;
        }
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
