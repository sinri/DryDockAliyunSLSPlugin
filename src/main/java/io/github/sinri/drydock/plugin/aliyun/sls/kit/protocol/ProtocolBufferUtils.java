package io.github.sinri.drydock.plugin.aliyun.sls.kit.protocol;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import io.vertx.core.buffer.Buffer;

public class ProtocolBufferUtils {
    private static final ProtocolBufferUtils instance = new ProtocolBufferUtils();
    private final FileDescriptorProto fileDescriptorProto;
    private final FileDescriptor fileDescriptor;
    private final Descriptor logDescriptor;
    private final Descriptor contentDescriptor;
    private final Descriptor logTagDescriptor;
    private final Descriptor logGroupDescriptor;
    private final Descriptor logGroupListDescriptor;

    private ProtocolBufferUtils() {
        // 创建文件描述符原型
        fileDescriptorProto = createFileDescriptorProto();
        // 构建文件描述符（需要处理依赖关系）
        try {
            fileDescriptor = FileDescriptor.buildFrom(
                    fileDescriptorProto,
                    new FileDescriptor[0]
            );
        } catch (Descriptors.DescriptorValidationException e) {
            throw new RuntimeException(e);
        }

        // 获取各个消息类型的描述符
        logDescriptor = fileDescriptor.findMessageTypeByName("Log");
        contentDescriptor = logDescriptor.findNestedTypeByName("Content");
        logTagDescriptor = fileDescriptor.findMessageTypeByName("LogTag");
        logGroupDescriptor = fileDescriptor.findMessageTypeByName("LogGroup");
        logGroupListDescriptor = fileDescriptor.findMessageTypeByName("LogGroupList");
    }

    public static ProtocolBufferUtils getInstance() {
        return instance;
    }

    public static void main(String[] args) throws Exception {
        // 创建文件描述符原型
        FileDescriptorProto fileDescriptorProto = createFileDescriptorProto();

        // 构建文件描述符（需要处理依赖关系）
        FileDescriptor fileDescriptor = FileDescriptor.buildFrom(
                fileDescriptorProto, new FileDescriptor[0]);

        // 获取各个消息类型的描述符
        Descriptor logDescriptor = fileDescriptor.findMessageTypeByName("Log");
        Descriptor contentDescriptor = logDescriptor.findNestedTypeByName("Content");
        Descriptor logTagDescriptor = fileDescriptor.findMessageTypeByName("LogTag");
        Descriptor logGroupDescriptor = fileDescriptor.findMessageTypeByName("LogGroup");
        Descriptor logGroupListDescriptor = fileDescriptor.findMessageTypeByName("LogGroupList");

        // 示例：创建一个 LogGroupList 动态消息
        DynamicMessage logGroupList = createSampleLogGroupList(
                logGroupListDescriptor, logGroupDescriptor,
                logDescriptor, contentDescriptor, logTagDescriptor);

        // 序列化和反序列化示例
        byte[] bytes = logGroupList.toByteArray();
        DynamicMessage parsedLogGroupList = DynamicMessage.parseFrom(logGroupListDescriptor, bytes);

        System.out.println("Parsed LogGroupList: " + parsedLogGroupList);
    }

    private static FileDescriptorProto createFileDescriptorProto() {
        // 创建 Content 嵌套消息描述符
        DescriptorProto contentDescriptor = DescriptorProto.newBuilder()
                                                           .setName("Content")
                                                           .addField(FieldDescriptorProto.newBuilder()
                                                                                         .setName("Key")
                                                                                         .setNumber(1)
                                                                                         .setType(FieldDescriptorProto.Type.TYPE_STRING)
                                                                                         .setLabel(FieldDescriptorProto.Label.LABEL_REQUIRED)
                                                                                         .build())
                                                           .addField(FieldDescriptorProto.newBuilder()
                                                                                         .setName("Value")
                                                                                         .setNumber(2)
                                                                                         .setType(FieldDescriptorProto.Type.TYPE_STRING)
                                                                                         .setLabel(FieldDescriptorProto.Label.LABEL_REQUIRED)
                                                                                         .build())
                                                           .build();

        // 创建 Log 消息描述符
        DescriptorProto logDescriptor = DescriptorProto.newBuilder()
                                                       .setName("Log")
                                                       .addField(FieldDescriptorProto.newBuilder()
                                                                                     .setName("Time")
                                                                                     .setNumber(1)
                                                                                     .setType(FieldDescriptorProto.Type.TYPE_UINT32)
                                                                                     .setLabel(FieldDescriptorProto.Label.LABEL_REQUIRED)
                                                                                     .setJsonName("Time")
                                                                                     .setDefaultValue("0")
                                                                                     .build())
                                                       .addNestedType(contentDescriptor)
                                                       .addField(FieldDescriptorProto.newBuilder()
                                                                                     .setName("Contents")
                                                                                     .setNumber(2)
                                                                                     .setType(FieldDescriptorProto.Type.TYPE_MESSAGE)
                                                                                     .setLabel(FieldDescriptorProto.Label.LABEL_REPEATED)
                                                                                     .setTypeName(".Log.Content")
                                                                                     .build())
                                                       .addField(FieldDescriptorProto.newBuilder()
                                                                                     .setName("Time_ns")
                                                                                     .setNumber(4)
                                                                                     .setType(FieldDescriptorProto.Type.TYPE_FIXED32)
                                                                                     .setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL)
                                                                                     .setJsonName("Time_ns")
                                                                                     .build())
                                                       .build();

        // 创建 LogTag 消息描述符
        DescriptorProto logTagDescriptor = DescriptorProto.newBuilder()
                                                          .setName("LogTag")
                                                          .addField(FieldDescriptorProto.newBuilder()
                                                                                        .setName("Key")
                                                                                        .setNumber(1)
                                                                                        .setType(FieldDescriptorProto.Type.TYPE_STRING)
                                                                                        .setLabel(FieldDescriptorProto.Label.LABEL_REQUIRED)
                                                                                        .build())
                                                          .addField(FieldDescriptorProto.newBuilder()
                                                                                        .setName("Value")
                                                                                        .setNumber(2)
                                                                                        .setType(FieldDescriptorProto.Type.TYPE_STRING)
                                                                                        .setLabel(FieldDescriptorProto.Label.LABEL_REQUIRED)
                                                                                        .build())
                                                          .build();

        // 创建 LogGroup 消息描述符
        DescriptorProto logGroupDescriptor = DescriptorProto.newBuilder()
                                                            .setName("LogGroup")
                                                            .addField(FieldDescriptorProto.newBuilder()
                                                                                          .setName("Logs")
                                                                                          .setNumber(1)
                                                                                          .setType(FieldDescriptorProto.Type.TYPE_MESSAGE)
                                                                                          .setLabel(FieldDescriptorProto.Label.LABEL_REPEATED)
                                                                                          .setTypeName(".Log")
                                                                                          .build())
                                                            .addField(FieldDescriptorProto.newBuilder()
                                                                                          .setName("Topic")
                                                                                          .setNumber(3)
                                                                                          .setType(FieldDescriptorProto.Type.TYPE_STRING)
                                                                                          .setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL)
                                                                                          .build())
                                                            .addField(FieldDescriptorProto.newBuilder()
                                                                                          .setName("Source")
                                                                                          .setNumber(4)
                                                                                          .setType(FieldDescriptorProto.Type.TYPE_STRING)
                                                                                          .setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL)
                                                                                          .build())
                                                            .addField(FieldDescriptorProto.newBuilder()
                                                                                          .setName("LogTags")
                                                                                          .setNumber(6)
                                                                                          .setType(FieldDescriptorProto.Type.TYPE_MESSAGE)
                                                                                          .setLabel(FieldDescriptorProto.Label.LABEL_REPEATED)
                                                                                          .setTypeName(".LogTag")
                                                                                          .build())
                                                            .build();

        // 创建 LogGroupList 消息描述符
        DescriptorProto logGroupListDescriptor = DescriptorProto.newBuilder()
                                                                .setName("LogGroupList")
                                                                .addField(FieldDescriptorProto.newBuilder()
                                                                                              .setName("logGroupList")
                                                                                              .setNumber(1)
                                                                                              .setType(FieldDescriptorProto.Type.TYPE_MESSAGE)
                                                                                              .setLabel(FieldDescriptorProto.Label.LABEL_REPEATED)
                                                                                              .setTypeName(".LogGroup")
                                                                                              .build())
                                                                .build();

        // 创建文件描述符
        return FileDescriptorProto.newBuilder()
                                  .setName("log.proto")
                                  .setSyntax("proto2")
                                  .addMessageType(logDescriptor)
                                  .addMessageType(logTagDescriptor)
                                  .addMessageType(logGroupDescriptor)
                                  .addMessageType(logGroupListDescriptor)
                                  .build();
    }

    private static DynamicMessage createSampleLogGroupList(
            Descriptor logGroupListDescriptor,
            Descriptor logGroupDescriptor,
            Descriptor logDescriptor,
            Descriptor contentDescriptor,
            Descriptor logTagDescriptor) {

        // 创建 Content 消息
        DynamicMessage content1 = DynamicMessage.newBuilder(contentDescriptor)
                                                .setField(contentDescriptor.findFieldByName("Key"), "level")
                                                .setField(contentDescriptor.findFieldByName("Value"), "INFO")
                                                .build();

        DynamicMessage content2 = DynamicMessage.newBuilder(contentDescriptor)
                                                .setField(contentDescriptor.findFieldByName("Key"), "message")
                                                .setField(contentDescriptor.findFieldByName("Value"), "System started")
                                                .build();

        // 创建 Log 消息
        DynamicMessage log = DynamicMessage.newBuilder(logDescriptor)
                                           .setField(logDescriptor.findFieldByName("Time"), 1624352000)
                                           .addRepeatedField(logDescriptor.findFieldByName("Contents"), content1)
                                           .addRepeatedField(logDescriptor.findFieldByName("Contents"), content2)
                                           .setField(logDescriptor.findFieldByName("Time_ns"), 123456789)
                                           .build();

        // 创建 LogTag 消息
        DynamicMessage logTag = DynamicMessage.newBuilder(logTagDescriptor)
                                              .setField(logTagDescriptor.findFieldByName("Key"), "service")
                                              .setField(logTagDescriptor.findFieldByName("Value"), "api-gateway")
                                              .build();

        // 创建 LogGroup 消息
        DynamicMessage logGroup = DynamicMessage.newBuilder(logGroupDescriptor)
                                                .addRepeatedField(logGroupDescriptor.findFieldByName("Logs"), log)
                                                .setField(logGroupDescriptor.findFieldByName("Topic"), "system-logs")
                                                .setField(logGroupDescriptor.findFieldByName("Source"), "server-01")
                                                .addRepeatedField(logGroupDescriptor.findFieldByName("LogTags"), logTag)
                                                .build();

        // 创建 LogGroupList 消息
        return DynamicMessage.newBuilder(logGroupListDescriptor)
                             .addRepeatedField(logGroupListDescriptor.findFieldByName("logGroupList"), logGroup)
                             .build();
    }

    public Descriptor getLogDescriptor() {
        return logDescriptor;
    }

    public Descriptor getContentDescriptor() {
        return contentDescriptor;
    }

    public Descriptor getLogGroupDescriptor() {
        return logGroupDescriptor;
    }

    public Descriptor getLogGroupListDescriptor() {
        return logGroupListDescriptor;
    }

    public Descriptor getLogTagDescriptor() {
        return logTagDescriptor;
    }

    public byte[] toBytes(DynamicMessage dynamicMessage) {
        return dynamicMessage.toByteArray();
    }

    public Buffer toBuffer(DynamicMessage dynamicMessage) {
        return Buffer.buffer(dynamicMessage.toByteArray());
    }
}