package com.JohnZero.lock.entity;

import org.litepal.crud.LitePalSupport;

/**
 * @author: JohnZero
 * @date: 2020-09-23
 **/
public class PasswordInfo extends LitePalSupport {
    private int id;
    private String title;
    private String password;
    private String note;
    private String accessTime;
    private String modifyTime;
    private boolean isChecked;

    public PasswordInfo() { }

    public PasswordInfo(String title, String password, String note, String accessTime, String modifyTime) {
        this.title = title;
        this.password = password;
        this.note = note;
        this.accessTime = accessTime;
        this.modifyTime = modifyTime;
        this.isChecked=false;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getAccessTime() { return accessTime; }

    public void setAccessTime(String accessTime) { this.accessTime = accessTime; }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public boolean isChecked() { return isChecked; }

    public void setChecked(boolean checked) { isChecked = checked; }

}
