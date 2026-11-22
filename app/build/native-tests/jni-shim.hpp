#pragma once
// Compile-only shim for C ABI APIs that an engine checkout may not declare
// (see docs/source-build.md). verify.ps1 defines one SPK_SHIM_<API> macro per
// API actually missing from the supplied engine header, so a complete patched
// tree produces no duplicate declarations. This header is NEVER shipped or
// linked into the APK.
#include "spektrafilm/spk_engine.h"
#include <cstddef>
#include <cstdint>
#include <string>

extern "C" {
#ifdef SPK_SHIM_SPK_DEVICE_PROFILE
const char* spk_device_profile(spk_engine*);
#endif
#ifdef SPK_SHIM_SPK_ERROR_CODE
int spk_error_code(const char*);
#endif
#ifdef SPK_SHIM_SPK_EXPORT_SOURCE
spk_status spk_export_source(spk_session*, const char*, spk_result*);
#endif
#ifdef SPK_SHIM_SPK_PROBE_CANARY
const char* spk_probe_canary(spk_engine*, uint64_t);
#endif
#ifdef SPK_SHIM_SPK_TRIM_POOL
uint64_t spk_trim_pool(spk_engine*);
#endif
#ifdef SPK_SHIM_SPK_PIN_AUTO_EXPOSURE_EV
void spk_pin_auto_exposure_ev(spk_session*, double);
#endif
#ifdef SPK_SHIM_SPK_CLEAR_PIN_AUTO_EXPOSURE_EV
void spk_clear_pin_auto_exposure_ev(spk_session*);
#endif
#ifdef SPK_SHIM_SPK_SUSPEND_SESSION
int spk_suspend_session(spk_session*);
#endif
#ifdef SPK_SHIM_SPK_RESUME_SESSION
int spk_resume_session(spk_session*);
#endif
#ifdef SPK_SHIM_SPK_STAGE_SNAPSHOT
int spk_stage_snapshot(spk_session*, char*, size_t);
#endif
#ifdef SPK_SHIM_SPK_DEBUG_PRINT_EXPOSURE
double spk_debug_print_exposure(spk_session*);
#endif
#ifdef SPK_SHIM_SPK_DEBUG_PRINT_GAIN
double spk_debug_print_gain(spk_session*);
#endif
#ifdef SPK_SHIM_SPK_SET_OUTPUT_LINEAR
void spk_set_output_linear(spk_session*, int);
#endif
}
namespace spk { namespace io {
bool write_dng(const std::string&, const uint16_t*, uint32_t, uint32_t, uint32_t, std::string&);
}}
