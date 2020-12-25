package com.JohnZero.lock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.JohnZero.lock.entity.AppInfo;
import com.JohnZero.lock.R;
import java.util.ArrayList;

/**
 * @author: JohnZero
 * @date: 2020-09-07
 **/
public class AppInfoAdapter extends BaseAdapter {
    private ArrayList<AppInfo> appList;
    private Context mContext;
    
    public AppInfoAdapter(Context mContext,ArrayList<AppInfo> appList){
        this.mContext=mContext;
        this.appList=appList;
    }
    
    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public Object getItem(int i) {
        return appList.get(i);
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
            view= LayoutInflater.from(mContext).inflate(R.layout.listview_item_layout,viewGroup,false);
            holder.icon=(ImageView)view.findViewById(R.id.icon);
            holder.appName=(TextView)view.findViewById(R.id.appName);
            holder.appTime=(TextView)view.findViewById(R.id.appTime);
            view.setTag(holder);
        }else{
            holder=(ViewHolder)view.getTag();
        }

        holder.icon.setImageDrawable(appList.get(i).getIcon());
        holder.appName.setText(appList.get(i).getAppName());
        holder.appTime.setText(appList.get(i).getCurTime()/60+"/"+appList.get(i).getMaxTime()/60);
        if(!appList.get(i).isLock()) holder.appTime.setVisibility(View.GONE);
        return view;
    }
    
    static class ViewHolder{
        ImageView icon;
        TextView appName;
        TextView appTime;
    }
}
