package com.JohnZero.lock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.JohnZero.lock.R;
import java.util.ArrayList;

/**
 * @author: JohnZero
 * @date: 2020-09-11
 **/
public class StringAdapter extends BaseAdapter {
    private ArrayList<String> usageList;
    private Context mContext;

    public StringAdapter(Context mContext,ArrayList<String> usageList){
        this.mContext=mContext;
        this.usageList=usageList;
    }

    @Override
    public int getCount() {
        return usageList.size();
    }

    @Override
    public Object getItem(int i) {
        return usageList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder=null;
        if(view==null){
            holder=new ViewHolder();
            view= LayoutInflater.from(mContext).inflate(R.layout.listview_item_layout1,viewGroup,false);
            holder.appName=(TextView)view.findViewById(R.id.appName);
            view.setTag(holder);
        }else{
            holder=(ViewHolder)view.getTag();
        }
        holder.appName.setText(usageList.get(i));
        return view;
    }

    public static class ViewHolder{
        TextView appName;
    }
}
