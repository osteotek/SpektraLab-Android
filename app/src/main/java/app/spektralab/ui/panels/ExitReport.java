package app.spektralab.ui.panels;

import static app.spektralab.AppLanguage.tr;
import android.content.Intent;
import app.spektralab.CrashTrail;
import app.spektralab.Probe;
import android.net.Uri;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;

import app.spektralab.ui.*;

/** ExitReport builders extracted from MainActivity (architecture Phase C). */
public final class ExitReport {
    private ExitReport() {}

    public static void reportPreviousExit(ExitReportHost host) {
        final StringBuilder body = new StringBuilder();
        final String trail = CrashTrail.previous(host.context());
        // **The last line, read from the same string the report carries.**
        // `CrashTrail.last()` re-reads the preference and splits it itself; on
        // the 1.5.0m build it returned a step that was not the last one, so a
        // trail ending in `app:paused` still fell through to the exit-info
        // branch and asked about a crash that never happened. One source, one
        // answer, no second read.
        final String trimmed = trail.trim();
        final int nl = trimmed.lastIndexOf('\n');
        final String lastStep = nl < 0 ? trimmed : trimmed.substring(nl + 1);
        // **An empty ring is the one thing we can prove was not a crash.**
        // `onDestroy` is the only path that clears it, and a process the
        // system killed never reaches `onDestroy`. `ApplicationExitInfo` still
        // has a row for that ordinary exit (reason 1 or 10), so reading it
        // first turned every "swipe the app away, open it again" into
        // tr("crash.the_app_closed_unexpectedly_last") -- with nothing in the trail, because it had been
        // cleared on the way out.
        if (trail.isEmpty()) {
            Probe.log("exit", "skip=empty ring");
            CrashTrail.clear(host.context());
            return;
        }
        // A trail whose **last** line is `app:paused` means the user left; the
        // process was killed in the background afterwards, which is not a
        // crash. (It used to test `startsWith`, which was right when the trail
        // held one line and is wrong now that it holds sixty-four.)
        // **`app:onDestroy` counts too.** `onDestroy` marks it and then clears
        // the ring; a process the system kills between those two lines leaves
        // `app:onDestroy` as the last step. That is still a deliberate exit,
        // not a crash -- and on this ROM the kill lands exactly there, so the
        // trailing line is the only evidence left.
        // **Look for the pause, not at the last line.** The shelf keeps
        // painting cards after `onPause` -- a worker finishes the frame it
        // already started and marks every card it touches -- so the ring's
        // tail is `prefetch:card`, not `app:paused`. An `endsWith` test
        // therefore missed every ordinary exit. What matters is whether the
        // app came back to the foreground after the pause: a `render:begin`
        // after it means the user returned and the process died during real
        // work.
        final int pausedAt = Math.max(trimmed.lastIndexOf("app:paused"),
                                      trimmed.lastIndexOf("app:onDestroy"));
        // **An export that started and never finished is a crash, whatever the
        // pause says.** A 77 MP export takes long enough that people leave the
        // app while it runs; `onPause` then sits in the ring, and the test below
        // ("no `render:begin` after the pause") reads that as a deliberate exit
        // and stays quiet -- which is exactly why the last one came back with no
        // report at all. `export:written` is marked only after the file is on
        // disk, so its absence is proof the process did not get that far.
        final boolean exportUnfinished = trimmed.contains("export:begin")
                && !trimmed.contains("export:written");
        if (exportUnfinished) {
            Probe.log("exit", "export began and never wrote; reporting");
        } else if (pausedAt >= 0 && trimmed.indexOf("render:begin", pausedAt) < 0) {
            Probe.log("exit", "skip=paused at " + pausedAt + " last=" + lastStep);
            CrashTrail.clear(host.context());
            return;
        }
        body.append("trail:\n").append(trail).append('\n');
        // Reflected, not called: `ApplicationExitInfo` arrived in API 30 and this
        // project compiles against an older SDK, where the symbol does not exist
        // even though the phone has it. Same reason `DeviceReport` reflects its
        // way to the advertising-id-free device name.
        // **Not every exit is a crash.** 1 = exit_self, 10 = user requested,
        // 16 = package updated (an overwrite install); the rest of the numbers
        // outside 2..6 are ordinary lifecycle ends. `ApplicationExitInfo` has a
        // row for all of them, and reporting on every row is what turned
        // tr("ui170.080") into tr("crash.the_app_closed_unexpectedly_last"). 2..6 are the ones where the process
        // did not get to finish: SIGNALED, LOW_MEMORY, Java crash, native
        // crash, ANR. If the list cannot be read, the non-empty trail is
        // evidence enough to ask.
        boolean crashed = true;
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            try {
                final android.app.ActivityManager am =
                        (android.app.ActivityManager) host.context().getSystemService(android.content.Context.ACTIVITY_SERVICE);
                if (am == null) {
                    DebugSink.text("exit", "activity manager unavailable");
                } else {
                    // **`getHistoricalProcessExitReasons`, not ...ExitInfos.**
                    // The latter has never existed on any Android release; it
                    // was a guess inside a reflection loop, so it threw
                    // NoSuchMethodException on every ROM and every report said
                    // "no exit-info API". That one wrong name is why three
                    // native deaths could not be told apart from the kernel
                    // reclaiming the process -- and the two need different
                    // fixes.
                    final java.util.List<android.app.ApplicationExitInfo> list =
                            am.getHistoricalProcessExitReasons(host.context().getPackageName(), 0, 8);
                    android.app.ApplicationExitInfo last = null;
                    if (list != null) {
                        for (android.app.ApplicationExitInfo info : list) {
                            if (info == null) continue;
                            if (info.getPid() == android.os.Process.myPid()) continue;
                            last = info;
                            break;
                        }
                    }
                    if (last == null) {
                        DebugSink.text("exit", "exit-info API present, no record for this package");
                    } else {
                        final int reason = last.getReason();
                        // **Every reason, not just the ones that say "crash".**
                        // 1 exit_self, 2 SIGNALED, 3 LOW_MEMORY, 4 Java crash,
                        // 5 native crash, 6 ANR, 9 excessive resources,
                        // 10 user requested, 13 other. Keeping only 4..6 made a
                        // kernel reclaim (3) or a SIGKILL (2) look like "the app
                        // vanished for no reason".
                        host.setLastExitReason(reason);
                        crashed = reason >= 2 && reason <= 6;
                        if (crashed) {
                            body.append("exit: reason=").append(reason)
                                .append(" (").append(host.exitReasonNameViaHost(reason)).append(')')
                                .append(" status=").append(last.getStatus())
                                .append(" pss=").append(last.getPss()).append("KB")
                                .append(" rss=").append(last.getRss()).append("KB")
                                .append(" desc=").append(last.getDescription()).append('\n');
                            try (java.io.InputStream stream = last.getTraceInputStream()) {
                                if (stream != null) {
                                    final java.io.ByteArrayOutputStream buf =
                                            new java.io.ByteArrayOutputStream();
                                    final byte[] chunk = new byte[8192];
                                    int n, total = 0;
                                    // **A tombstone is not 16 KB.** Truncating
                                    // kept the header and dropped the frame list.
                                    while ((n = stream.read(chunk)) > 0 && total < 262144) {
                                        buf.write(chunk, 0, n);
                                        total += n;
                                    }
                                    body.append(buf.toString("UTF-8")).append('\n');
                                }
                            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:13786", ignored); }
                        }
                    }
                }
            } catch (Throwable e) {
                // **Silence here is what made the first report unreadable.** An
                // exit row that cannot be read looks exactly like one that was
                // never written, and the two mean opposite things.
                body.append(tr("crash.exit_could_not_read_applicationexitinfo")).append(e).append("）\n");
            }
        }
        // **A non-crashing exit reason leaves nothing to ask about.**
        if (!crashed) {
            Probe.log("exit", "skip=not-crash reason=" + host.getLastExitReason());
            CrashTrail.clear(host.context());
            return;
        }
        if (body.length() == 0) { CrashTrail.clear(host.context()); return; }
        try {
            final java.io.File dir = host.context().getExternalFilesDir("crash");
            if (dir != null) {
                if (!dir.exists()) dir.mkdirs();
                final String stamp = new java.text.SimpleDateFormat("MMdd-HHmmss",
                        java.util.Locale.US).format(new java.util.Date());
                final java.io.File out = new java.io.File(dir, "exit-" + stamp + ".txt");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(out)) {
                    fos.write(body.toString().getBytes("UTF-8"));
                }
                host.setLastCrashFile(out.getAbsolutePath());
            }
        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:13816", ignored); }
        // **One row, and it is the whole report.** This used to be a
        // previous-exit row plus a probe row on every launch that had died; the
        // quota is counted in writes, and one is enough.
        final String exitHead = "previous-exit\n" + host.reportHeadHostCall() + "--- exit ---\n";
        final String exitBody = body.toString();
        CrashTrail.clear(host.context());
        new Thread(() -> DebugSink.send("crash", exitHead + exitBody),
                "SpektraLab-report").start();
        // The report is only worth what reaches the developer. A crash on a
        // phone we do not own leaves this file and nothing else, so ask once,
        // late enough not to sit on top of the first layout, and make sending
        // it one tap.
        Probe.now("exit", "report trail=" + trail.length() + "B last=" + lastStep
                + " reason=" + host.getLastExitReason());
        final String summary = lastStep.isEmpty() ? "(no step recorded)" : lastStep;
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                () -> host.offerCrashReportHostCall(summary), 1500);
    }
}
