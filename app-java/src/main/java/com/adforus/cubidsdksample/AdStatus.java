package com.adforus.cubidsdksample;

public enum AdStatus {
    INIT_SUCCESS("success"),
    FAILED("failed"),
    FAILED_EMPTY("failed"),
    AD_REQ("request"),
    AD_LOADED("loaded"),
    AD_DISPLAY("attach"),
    AD_SHOW("visible"),
    AD_REMOVE("detach"),
    AD_SKIP("skip"),
    AD_HIDE("invisible"),
    AD_CLOSE("close"),
    AD_COMPLETE("complete"),
    AD_CLICK("click"),
    AD_DEFAULT("Status");

    private final String subText;

    AdStatus(String text) {
        this.subText = text;
    }

    public String getSubText(){
        return subText;
    }
}
