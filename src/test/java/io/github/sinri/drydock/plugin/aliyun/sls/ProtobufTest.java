package io.github.sinri.drydock.plugin.aliyun.sls;

import io.github.sinri.drydock.plugin.aliyun.sls.kit.entity.*;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.protocol.Lz4Utils;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.protocol.ProtocolBufferUtils;
import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.Test;
import com.google.protobuf.DynamicMessage;
import io.vertx.core.buffer.Buffer;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

public class ProtobufTest extends KeelUnitTest {
    @Test
    public void testProtobufSerialization() {
        // 创建LogContent
        LogContent content = new LogContent("test_key", "test_value");
        com.google.protobuf.DynamicMessage contentPb = content.toProtobuf();
        System.out.println("LogContent serialized: " + contentPb);
        
        // 创建LogItem
        LogItem logItem = new LogItem(
            Math.toIntExact(System.currentTimeMillis() / 1000), 
            List.of(content)
        );
        com.google.protobuf.DynamicMessage logItemPb = logItem.toProtobuf();
        System.out.println("LogItem serialized: " + logItemPb);
        
        // 创建LogTag
        LogTag logTag = new LogTag("tag_key", "tag_value");
        com.google.protobuf.DynamicMessage logTagPb = logTag.toProtobuf();
        System.out.println("LogTag serialized: " + logTagPb);
        
        // 创建LogGroup
        LogGroup logGroup = new LogGroup("test_topic", "test_source")
            .addLogItems(List.of(logItem))
            .addLogTags(List.of(logTag));
        com.google.protobuf.DynamicMessage logGroupPb = logGroup.toProtobuf();
        System.out.println("LogGroup serialized: " + logGroupPb);
        
        // 创建LogGroupList
        LogGroupList logGroupList = new LogGroupList(List.of(logGroup));
        com.google.protobuf.DynamicMessage logGroupListPb = logGroupList.toProtobuf();
        System.out.println("LogGroupList serialized: " + logGroupListPb);
        
        // 验证字节数组
        byte[] bytes = logGroupListPb.toByteArray();
        System.out.println("Serialized bytes length: " + bytes.length);
        
        // 尝试重新解析
        try {
            com.google.protobuf.DynamicMessage parsed = com.google.protobuf.DynamicMessage.parseFrom(
                ProtocolBufferUtils.getInstance().getLogGroupListDescriptor(),
                bytes
            );
            System.out.println("Successfully parsed back: " + parsed);
        } catch (Exception e) {
            System.err.println("Failed to parse back: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test
    public void testProtobufHexDump() {
        // 创建与 Test1 相同的数据
        LogContent content = new LogContent("text", "God Bless!");
        LogItem logItem = new LogItem(
            Math.toIntExact(System.currentTimeMillis() / 1000), 
            List.of(content)
        );
        LogTag logTag = new LogTag("K", "V");
        LogGroup logGroup = new LogGroup("Test", "local")
            .addLogItems(List.of(logItem))
            .addLogTags(List.of(logTag));
        LogGroupList logGroupList = new LogGroupList(List.of(logGroup));
        
        byte[] bytes = logGroupList.toProtobuf().toByteArray();
        System.out.println("LogGroupList hex dump:");
        System.out.println(bytesToHex(bytes));
        System.out.println("Length: " + bytes.length);
        
        // 打印单个 LogGroup 的内容
        byte[] logGroupBytes = logGroup.toProtobuf().toByteArray();
        System.out.println("LogGroup hex dump:");
        System.out.println(bytesToHex(logGroupBytes));
        System.out.println("Length: " + logGroupBytes.length);
    }
    
    @Test
    public void testOrderedProtobufConstruction() {
        // 手动按照字段编号顺序构建消息，确保严格遵循字段顺序
        ProtocolBufferUtils generator = ProtocolBufferUtils.getInstance();
        
        // 构建 Content (field 1: Key, field 2: Value)
        DynamicMessage content = DynamicMessage.newBuilder(generator.getContentDescriptor())
            .setField(generator.getContentDescriptor().findFieldByNumber(1), "text")  // Key = field 1
            .setField(generator.getContentDescriptor().findFieldByNumber(2), "God Bless!")  // Value = field 2
            .build();
        
        // 构建 Log (field 1: Time, field 2: Contents, field 4: Time_ns)
        DynamicMessage log = DynamicMessage.newBuilder(generator.getLogDescriptor())
            .setField(generator.getLogDescriptor().findFieldByNumber(1), Math.toIntExact(System.currentTimeMillis() / 1000))  // Time = field 1
            .addRepeatedField(generator.getLogDescriptor().findFieldByNumber(2), content)  // Contents = field 2
            // 跳过 field 3 (不存在)
            // 暂时不设置 field 4 (Time_ns)
            .build();
        
        // 构建 LogTag (field 1: Key, field 2: Value)
        DynamicMessage logTag = DynamicMessage.newBuilder(generator.getLogTagDescriptor())
            .setField(generator.getLogTagDescriptor().findFieldByNumber(1), "K")  // Key = field 1
            .setField(generator.getLogTagDescriptor().findFieldByNumber(2), "V")  // Value = field 2
            .build();
        
        // 构建 LogGroup (field 1: Logs, field 3: Topic, field 4: Source, field 6: LogTags)
        DynamicMessage logGroup = DynamicMessage.newBuilder(generator.getLogGroupDescriptor())
            .addRepeatedField(generator.getLogGroupDescriptor().findFieldByNumber(1), log)  // Logs = field 1
            // 跳过 field 2 (不存在)
            .setField(generator.getLogGroupDescriptor().findFieldByNumber(3), "Test")  // Topic = field 3
            .setField(generator.getLogGroupDescriptor().findFieldByNumber(4), "local")  // Source = field 4
            // 跳过 field 5 (不存在)
            .addRepeatedField(generator.getLogGroupDescriptor().findFieldByNumber(6), logTag)  // LogTags = field 6
            .build();
        
        // 构建 LogGroupList (field 1: logGroupList)
        DynamicMessage logGroupList = DynamicMessage.newBuilder(generator.getLogGroupListDescriptor())
            .addRepeatedField(generator.getLogGroupListDescriptor().findFieldByNumber(1), logGroup)  // logGroupList = field 1
            .build();
        
        byte[] bytes = logGroupList.toByteArray();
        System.out.println("Ordered LogGroupList hex dump:");
        System.out.println(bytesToHex(bytes));
        System.out.println("Length: " + bytes.length);
        
        // 打印结构化输出
        System.out.println("Ordered LogGroupList:");
        System.out.println(logGroupList);
    }
    
    @Test
    public void testRequestFormat() {
        // 测试完整的请求格式，检查是否符合阿里云的期望
        LogContent content = new LogContent("text", "God Bless!");
        LogItem logItem = new LogItem(
            Math.toIntExact(System.currentTimeMillis() / 1000), 
            List.of(content)
        );
        LogTag logTag = new LogTag("K", "V");
        LogGroup logGroup = new LogGroup("Test", "local")
            .addLogItems(List.of(logItem))
            .addLogTags(List.of(logTag));
        LogGroupList logGroupList = new LogGroupList(List.of(logGroup));
        
        // 获取原始数据
        Buffer raw = Buffer.buffer(logGroupList.toProtobuf().toByteArray());
        System.out.println("Raw data size: " + raw.length());
        System.out.println("Raw data hex: " + bytesToHex(raw.getBytes()));
        
        // 压缩数据
        Buffer compressed = Lz4Utils.compress(raw);
        System.out.println("Compressed data size: " + compressed.length());
        System.out.println("Compressed data hex: " + bytesToHex(compressed.getBytes()));
        
        // 计算 MD5
        try {
            String contentMd5 = Base64.getEncoder().encodeToString(
                MessageDigest.getInstance("MD5").digest(compressed.getBytes())
            );
            System.out.println("Content-MD5: " + contentMd5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 打印请求信息
        System.out.println("x-log-bodyrawsize: " + raw.length());
        System.out.println("Content-Length: " + compressed.length());
        System.out.println("Content-Type: application/x-protobuf");
        System.out.println("x-log-compresstype: lz4");
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x ", b));
        }
        return result.toString();
    }
}
