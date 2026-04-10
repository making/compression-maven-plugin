def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("Compressed app.js") : "Should log compression of app.js"
assert buildLog.contains("Compressed style.css") : "Should log compression of style.css"
assert buildLog.contains("Compressed icon.svg") : "Should log compression of icon.svg"

def staticDir = new File(basedir, "target/classes/static")
assert new File(staticDir, "app.js.br").exists() : "Brotli file should exist for app.js"
assert new File(staticDir, "app.js.gz").exists() : "Gzip file should exist for app.js"
assert new File(staticDir, "style.css.br").exists() : "Brotli file should exist for style.css"
assert new File(staticDir, "style.css.gz").exists() : "Gzip file should exist for style.css"
assert new File(staticDir, "icon.svg.br").exists() : "Brotli file should exist for icon.svg"
assert new File(staticDir, "icon.svg.gz").exists() : "Gzip file should exist for icon.svg"
assert buildLog.contains("BUILD SUCCESS")
