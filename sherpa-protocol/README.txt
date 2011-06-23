The Avro protocol file[1] is manually generated from the Avro IDL file[2] by running ./gen-avpr.sh.

So to make changes to the protocol edit the sherpa.avdl file and then run ./gen-avpr.sh.

[1] src/main/avro/sherpa/protocol/sherpa.avpr
[2] src/main/avro/sherpa/protocol/sherpa.avdl