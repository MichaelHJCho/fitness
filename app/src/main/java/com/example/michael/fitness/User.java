package com.example.michael.fitness;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by michael on 2/18/17.
 */

public class User implements Serializable {

    private String mEmail;
    private String mPassword;
    private String mName;
    private int mDistanceWalked;
    private int mDistanceWalkedToday;

    public int getMilestone() {
        return mMilestone;
    }

    public void setMilestone(int mMilestone) {
        this.mMilestone = mMilestone;
    }

    private int mMilestone;
    private int mCurrentDay = 0;

    public User(String email, String password, String name, int distanceWalked, int distanceWalkedToday, int milestone, int currentDay) {
        mEmail = email;
        mPassword = password;
        mName = name;
        mDistanceWalked = distanceWalked;
        mDistanceWalkedToday = distanceWalkedToday;
        mMilestone = milestone;
        mCurrentDay = currentDay;
    }

    public int getCurrentDay() {
        return mCurrentDay;
    }

    public void setCurrentDay(int mCurrentDay) {
        this.mCurrentDay = mCurrentDay;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public int getDistanceWalked() {
        return mDistanceWalked;
    }

    public void setDistanceWalked(int mDistanceWalked) {
        this.mDistanceWalked = mDistanceWalked;
    }

    public int getDistanceWalkedToday() {
        return mDistanceWalkedToday;
    }

    public void setDistanceWalkedToday(int mDistanceWalkedToday) {
        this.mDistanceWalkedToday = mDistanceWalkedToday;
    }
}
