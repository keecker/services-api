package com.keecker.embedded.camera.interfaces;

public class DewarpInfo{
    private float mPhi;
    private float mTheta;
    private float mScale;

    public float getPhi() {
        return mPhi;
    }

    public float getTheta() {
        return mTheta;
    }

    public float getScale() {
        return mScale;
    }

    public DewarpInfo(float mPhi, float mTheta, float mScale) {
        this.mPhi = mPhi;
        this.mTheta = mTheta;
        this.mScale = mScale;
    }

    public void setPhi(float mPhi) {
        this.mPhi = mPhi;
    }

    public void setTheta(float mTheta) {
        this.mTheta = mTheta;
    }

    public void setScale(float mScale) {
        this.mScale = mScale;
    }
}
