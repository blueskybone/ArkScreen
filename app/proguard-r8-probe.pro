# First adoption pass: run R8's code shrinking without renaming or optimization.
# Keep this configuration out of release; remove each temporary switch only after
# exercising JNI, remote parsing, local resources, and gacha import/export on device.
-dontobfuscate
-dontoptimize
