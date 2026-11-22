package app.spektralab;

/** Marker for the workers extracted out of MainActivity.
 *
 * After the Phase 2 host-interface extraction each worker holds its own
 * narrow, typed host interface instead of the whole Activity. This base class
 * intentionally exposes no Activity state.
 */
abstract class HostWorker {
}
