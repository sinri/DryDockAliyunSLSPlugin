# Drydock Aliyun SLS Plugin

This project provides a solution for Aliyun SLS based logging
without [Aliyun official SDK](https://mvnrepository.com/artifact/com.aliyun.openservices/aliyun-log-producer),
which contains vulnerable facts, such as out-of-dated guava and protobuf-java.

This project relies on the newer and safe SDKs:

* io.github.sinri::Keel | [GPL 3.0](https://www.gnu.org/licenses/gpl-3.0.txt)
* org.lz4::lz4-java | [Apache 2.0](https://github.com/lz4/lz4-java#Apache-2.0-1-ov-file)
* com.google.protobuf::protobuf-java | [BSD 3-Clause](https://opensource.org/licenses/BSD-3-Clause)

