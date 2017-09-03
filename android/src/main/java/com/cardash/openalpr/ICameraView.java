package com.cardash.openalpr;


public interface ICameraView {

    void setCountry(String country);

    void setTarget(int target);

    void setQuality(int quality);

    void setAspect(int aspect);

    void setType(int type);

    void setPlateBorderColorHex(String colorStr);

    void setPlateBorderEnabled(boolean enabled);

    void setRotateMode(boolean enabled);

    void setTorchMode(boolean enabled);

    void setTapToFocus(boolean enabled);

    void setResultsCallback(ALPR.ResultsCallback callback);

    void onResumeALPR();

    void disableView();
}
