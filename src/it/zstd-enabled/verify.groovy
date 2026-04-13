def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("Compressed app.js") : "Should log compression of app.js"

def staticDir = new File(basedir, "target/classes/static")
assert new File(staticDir, "app.js.zst").exists() : "Zstd file should exist for app.js"
assert new File(staticDir, "app.js.br").exists() : "Brotli file should still exist for app.js"
assert new File(staticDir, "app.js.gz").exists() : "Gzip file should still exist for app.js"
assert buildLog =~ /Compressed app\.js:.*zst:/ : "Build log should include zst size entry"
assert buildLog.contains("BUILD SUCCESS")
