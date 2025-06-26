package com.adforus.cubidsdksample

enum class AdStatus (text:String) {
    INIT_SUCCESS("success"), FAILED("failed"), FAILED_EMPTY("failed"), AD_REQ("request"),
    AD_LOADED("loaded"), AD_DISPLAY("attach"), AD_SHOW("visible"),
    AD_REMOVE("detach"), AD_SKIP("skip"), AD_HIDE("invisible"), AD_CLOSE("close"),
    AD_COMPLETE("complete"), AD_CLICK("click"), AD_DEFAULT("Status");

    val subText = text
}