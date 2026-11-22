#pragma once
// Shared check/failure-report scaffolding for the header-only native tests.
// Each test is its own binary, so one inline counter per process is enough.
#include <cstdio>

namespace spk_test {

inline int failures = 0;

inline void check(bool ok, const char* what) {
    if (!ok) {
        std::fprintf(stderr, "FAIL: %s\n", what);
        ++failures;
    }
}

inline int report(const char* name) {
    if (failures) {
        std::fprintf(stderr, "%d %s check(s) failed\n", failures, name);
        return 1;
    }
    std::printf("%s: all checks passed\n", name);
    return 0;
}

}  // namespace spk_test
