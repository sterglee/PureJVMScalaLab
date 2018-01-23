java -server  -d64  -XX:+UseNUMA -XX:+UseParallelGC -XX:+UseCompressedOops -XX:+AggressiveOpts -XX:+DoEscapeAnalysis  -Djava.library.path=./lib;. -Xss5m -Xms3000m -Xmx23000m -jar ScalaLab.jar
