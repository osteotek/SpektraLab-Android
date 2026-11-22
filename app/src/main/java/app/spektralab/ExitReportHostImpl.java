package app.spektralab;

/** Delegating implementation of ExitReportHost. */
final class ExitReportHostImpl implements app.spektralab.ui.panels.ExitReportHost {
    private final MainActivity activity;
    ExitReportHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public java.util.HashMap<java.lang.String, java.lang.Long> getArrived() { return activity.arrived; }
    @Override public java.lang.String getLastCrashFile() { return activity.lastCrashFile; }
    @Override public void setLastCrashFile(java.lang.String value) { activity.lastCrashFile = value; }
    @Override public int getLastExitReason() { return activity.lastExitReason; }
    @Override public void setLastExitReason(int value) { activity.lastExitReason = value; }
    @Override public app.spektralab.DeviceProbe getProbe() { return activity.probe; }
    @Override public void setProbe(app.spektralab.DeviceProbe value) { activity.probe = value; }
    @Override public java.lang.String exitReasonNameViaHost(int a0) { return activity.exitReasonName(a0); }
    @Override public void offerCrashReportHostCall(java.lang.String a0) { activity.offerCrashReport(a0); }
    @Override public java.lang.String reportHeadHostCall() { return activity.reportHead(); }
    @Override public android.content.Context context() { return activity; }
}
