package app.spektralab.ui.panels;

/** Narrow Activity surface for the ExitReport builders. Implemented by ExitReportHostImpl. */
public interface ExitReportHost {
    java.util.HashMap<java.lang.String, java.lang.Long> getArrived();
    java.lang.String getLastCrashFile();
    void setLastCrashFile(java.lang.String value);
    int getLastExitReason();
    void setLastExitReason(int value);
    app.spektralab.DeviceProbe getProbe();
    void setProbe(app.spektralab.DeviceProbe value);
    java.lang.String exitReasonNameViaHost(int a0);
    void offerCrashReportHostCall(java.lang.String a0);
    java.lang.String reportHeadHostCall();
    android.content.Context context();
}
