def buildLog = new File(basedir, "build.log").text
def staticDir = new File(basedir, "target/classes/static")

assert new File(staticDir, "index.html.br").exists() : "Brotli file should exist for index.html"
assert new File(staticDir, "index.html.gz").exists() : "Gzip file should exist for index.html"
assert new File(staticDir, "data.json.br").exists() : "Brotli file should exist for data.json"
assert new File(staticDir, "data.json.gz").exists() : "Gzip file should exist for data.json"
assert !new File(staticDir, "app.js.br").exists() : "JS should not be compressed with custom extensions"
assert !new File(staticDir, "app.js.gz").exists() : "JS should not be compressed with custom extensions"
assert buildLog.contains("BUILD SUCCESS")
