package io.github.sinri.drydock.plugin.aliyun.sls.kit.entity;

import com.google.protobuf.DynamicMessage;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.protocol.ProtocolBufferUtils;

/**
 * @see <a
 *         href="https://help.aliyun.com/zh/sls/developer-reference/api-sls-2020-12-30-struct-logcontent">LogContent</a>
 */
public class LogContent {
    private final String key;
    private final String value;

    public LogContent(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public DynamicMessage toProtobuf() {
        var contentDescriptor = ProtocolBufferUtils.getInstance().getContentDescriptor();
        return DynamicMessage.newBuilder(contentDescriptor)
                             .setField(contentDescriptor.findFieldByName("Key"), key)
                             .setField(contentDescriptor.findFieldByName("Value"), value)
                             .build();
    }
}
