package com.JohnZero.lock.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.JohnZero.lock.entity.PasswordInfo;
import com.JohnZero.lock.R;
import java.util.List;

/**
 * @author: JohnZero
 * @date: 2020-09-23
 **/
public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.ViewHolder> {
    private List<PasswordInfo> passwordList;
    public static boolean isShow=false;

    public PasswordAdapter(List<PasswordInfo> passwordList){
        this.passwordList=passwordList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_modifyTime;
        CheckBox checkBox;
        public ViewHolder(View view){
            super(view);
            tv_title=(TextView)view.findViewById(R.id.tv_title);
            tv_modifyTime=(TextView)view.findViewById(R.id.tv_modifyTime);
            checkBox=(CheckBox)view.findViewById(R.id.checkBox);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_layout,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PasswordInfo passwordInfo=passwordList.get(position);
        holder.tv_title.setText(passwordInfo.getTitle());
        holder.tv_modifyTime.setText(passwordInfo.getModifyTime());
        holder.checkBox.setChecked(passwordInfo.isChecked());
        if(isShow) holder.checkBox.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return passwordList.size();
    }

}
