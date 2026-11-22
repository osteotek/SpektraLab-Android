package app.spektralab;

/** Common host reference for the workers extracted out of MainActivity. */
abstract class HostWorker {
    final MainActivity host;

    HostWorker(MainActivity host) { this.host = host; }
}
