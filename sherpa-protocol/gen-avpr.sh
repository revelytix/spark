mvn exec:exec -Dexec.executable="java" -Dexec.args="-cp %classpath org.apache.avro.tool.Main idl src/main/avro/sherpa/protocol/sherpa.avdl src/main/avro/sherpa/protocol/sherpa.avpr"
