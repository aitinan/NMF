package com.chen.pca;

public class FlagBean {

    private int startFlag;
    private int endFlag;
    private int[] flagUser;

    FlagBean(int startFlag, int endFlag, int[] flagUser) {
        this.startFlag = startFlag;
        this.endFlag = endFlag;
        this.flagUser = flagUser;
    }

    public int getStartFlag() {
        return startFlag;
    }

    public void setStartFlag(int startFlag) {
        this.startFlag = startFlag;
    }

    public int getEndFlag() {
        return endFlag;
    }

    public void setEndFlag(int endFlag) {
        this.endFlag = endFlag;
    }

    public int[] getFlagUser() {
        return flagUser;
    }

    public void setFlagUser(int[] flagUser) {
        this.flagUser = flagUser;
    }
}
