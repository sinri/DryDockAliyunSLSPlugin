package io.github.sinri.drydock.plugin.aliyun.sls.kit.entity;

import com.google.protobuf.DynamicMessage;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.protocol.LogEntityDescriptors;

import java.util.ArrayList;
import java.util.List;

/**
 * LogItem实体。
 *
 * @see <a href=
 *         "https://help.aliyun.com/zh/sls/developer-reference/api-sls-2020-12-30-struct-logitem"
 *         >LogItem 实体格式定义</a>
 * @since 1.0
 */
public class LogItem {
    private final int time;
    private final List<LogContent> contents;
    private Integer nanoPartOfTime = null;

    public LogItem(int time) {
        this.time = time;
        this.contents = new ArrayList<>();
    }

    public int getTime() {
        return time;
    }

    public List<LogContent> getContents() {
        return contents;
    }

    public LogItem addContent(LogContent content) {
        contents.add(content);
        return this;
    }

    public LogItem addContent(String key, String value) {
        contents.add(new LogContent(key, value));
        return this;
    }

    public Integer getNanoPartOfTime() {
        return nanoPartOfTime;
    }

    public LogItem setNanoPartOfTime(Integer nanoPartOfTime) {
        this.nanoPartOfTime = nanoPartOfTime;
        return this;
    }

    public DynamicMessage toProtobuf() {
        var logDescriptor = LogEntityDescriptors.getInstance().getLogDescriptor();
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(logDescriptor)
                                                       .setField(logDescriptor.findFieldByName("Time"), time);
        contents.forEach(content -> builder.addRepeatedField(logDescriptor.findFieldByName("Contents"), content.toProtobuf()));
        if (nanoPartOfTime != null) {
            builder.setField(logDescriptor.findFieldByName("Time_ns"), nanoPartOfTime);
        }
        return builder.build();
    }
}
