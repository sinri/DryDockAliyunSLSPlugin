package io.github.sinri.drydock.plugin.aliyun.sls.kit.entity;

import com.google.protobuf.DynamicMessage;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.protocol.ProtocolBufferUtils;

import java.util.List;

/**
 * LogItem实体。
 *
 * @see <a href=
 *         "https://help.aliyun.com/zh/sls/developer-reference/api-sls-2020-12-30-struct-logitem"
 *         >LogItem 实体格式定义</a>
 */
public class LogItem {
    private int time;
    private List<LogContent> contents;
    private Integer nanoPartOfTime=null;

    public LogItem(int time, List<LogContent> contents) {
        this.time = time;
        this.contents = contents;
    }

    public int getTime() {
        return time;
    }

    public List<LogContent> getContents() {
        return contents;
    }

    public LogItem setNanoPartOfTime(Integer nanoPartOfTime) {
        this.nanoPartOfTime = nanoPartOfTime;
        return this;
    }

    public Integer getNanoPartOfTime() {
        return nanoPartOfTime;
    }

    public DynamicMessage toProtobuf() {
        var logDescriptor= ProtocolBufferUtils.getInstance().getLogDescriptor();
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(logDescriptor)
                                                       .setField(logDescriptor.findFieldByName("Time"), time);
        contents.forEach(content -> {
            builder.addRepeatedField(logDescriptor.findFieldByName("Contents"), content.toProtobuf());
        });
        if (nanoPartOfTime != null) {
            builder.setField(logDescriptor.findFieldByName("Time_ns"), nanoPartOfTime);
        }
        return builder.build();
    }
}
