def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("Compression plugin is skipped") : "Should log skip message"
assert !buildLog.contains("Compressed ") : "Should not compress anything"
assert buildLog.contains("BUILD SUCCESS")
