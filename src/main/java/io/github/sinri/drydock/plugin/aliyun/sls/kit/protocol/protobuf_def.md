# The protobuf format definition

```protobuf
message Log
{
  required uint32 Time = 1;// UNIX Time Format
  message Content
  {
    required string Key = 1;
    required string Value = 2;
  }
  repeated Content Contents = 2;
  optional fixed32 Time_ns = 4; // for time nano part
}

message LogTag
{
  required string Key = 1;
  required string Value = 2;
}

message LogGroup
{
  repeated Log Logs = 1;
  optional string Topic = 3;
  optional string Source = 4;
  repeated LogTag LogTags = 6;
}

message LogGroupList
{
  repeated LogGroup logGroupList = 1;
}
```