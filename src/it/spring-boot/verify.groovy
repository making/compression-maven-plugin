def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("BUILD SUCCESS")
assert buildLog.contains("Tests run: 2") : "Both tests should have run"
