# Native debug-symbol warning for version 56

The only native library in the release is MediaPipe Tasks Vision 0.10.32's `libmediapipe_tasks_vision_jni.so`, supplied for arm64-v8a, armeabi-v7a, x86 and x86_64.

Inspection of the original merged libraries confirms all four contain a build ID and dynamic export table (`.dynsym`) but no full symbol table (`.symtab`) or DWARF debug information (`.debug_info`). They were already stripped in the vendor's AAR. The remaining dynamic exports are not a substitute for native debug symbols.

Release configuration now explicitly requests `ndk.debugSymbolLevel 'SYMBOL_TABLE'`, so future builds collect usable symbols if a dependency vendor supplies them. Native symbol extraction produces no symbols for the current MediaPipe binaries. No empty or renamed-library ZIP was manufactured, and no claim is made that this clears Play's warning.

To fully resolve the diagnostic limitation, obtain the matching unstripped binaries/debug-symbol package from the vendor, or build and validate MediaPipe from source with symbols and use those exact resulting binaries in a future release. Symbols from an unrelated source build do not match the existing version 56 binaries.

The uploaded version 56 AAB remains unchanged, with SHA-256 `7d872fba550fb656387cf1293c37df70c9cd376e6b7cbda612a061c68020a90a`. Its R8 Java/Kotlin mapping is included; native symbols and R8 mappings serve different crash stacks.

This is a crash-diagnostics recommendation, not evidence of a gameplay failure. A replacement release solely to repeat extraction cannot recover stripped vendor symbols. Continue testing version 56 and inspect any separate blocking errors in Play Console before rollout.

Reference: [Android native-symbol guidance](https://developer.android.com/build/include-native-symbols).
