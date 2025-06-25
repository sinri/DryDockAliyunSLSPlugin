package io.github.sinri.drydock.plugin.aliyun.sls.kit.entity;

import com.google.protobuf.DynamicMessage;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.protocol.LogEntityDescriptors;

import java.util.ArrayList;
import java.util.List;

public class LogGroupList {
    private final List<LogGroup> logGroups;

    public LogGroupList() {
        this.logGroups = new ArrayList<>();
    }

    public LogGroupList addLogGroup(LogGroup logGroup) {
        this.logGroups.add(logGroup);
        return this;
    }

    public LogGroupList addLogGroups(List<LogGroup> logGroups) {
        this.logGroups.addAll(logGroups);
        return this;
    }

    public List<LogGroup> getLogGroups() {
        return logGroups;
    }

    public DynamicMessage toProtobuf() {
        var logGroupListDescriptor = LogEntityDescriptors.getInstance().getLogGroupListDescriptor();
        var builder = DynamicMessage.newBuilder(logGroupListDescriptor);
        logGroups.forEach(logGroup -> builder.addRepeatedField(logGroupListDescriptor.findFieldByName("logGroupList"), logGroup.toProtobuf()));
        return builder.build();
    }
}
