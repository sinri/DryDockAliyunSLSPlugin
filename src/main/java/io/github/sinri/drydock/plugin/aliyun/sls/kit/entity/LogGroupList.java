package io.github.sinri.drydock.plugin.aliyun.sls.kit.entity;

import com.google.protobuf.DynamicMessage;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.protocol.ProtocolBufferUtils;

import java.util.List;

public class LogGroupList {
    private List<LogGroup> logGroups;

    public LogGroupList(List<LogGroup> logGroups) {
        this.logGroups = logGroups;
    }

    public List<LogGroup> getLogGroups() {
        return logGroups;
    }

    public DynamicMessage toProtobuf() {
        var logGroupListDescriptor = ProtocolBufferUtils.getInstance().getLogGroupListDescriptor();
        var builder = DynamicMessage.newBuilder(logGroupListDescriptor);
        logGroups.forEach(logGroup -> {
            builder.addRepeatedField(logGroupListDescriptor.findFieldByName("logGroupList"), logGroup.toProtobuf());
        });
        return builder.build();
    }
}
