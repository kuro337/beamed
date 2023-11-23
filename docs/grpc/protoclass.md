# Proto's

- Protobuf file (foo.proto)

- FooService   : Like an Interface.
- Foo Message  : Protobuf message definition. Similar to a Data Transfer Object.
- Foo Response : Another Protobuf message that represents what the service will return after processing a Foo Message.

```protobuf
syntax = "proto3";

package eventstream.proto;

message Foo {
    string name = 1;
    int32 age = 2;
    string gender = 3;
    double income = 4;
    repeated string some_list = 5;  // List of strings
}

service FooService {
    rpc ProcessFoo (Foo) returns (FooResponse) {}
}

message FooResponse {
    // Define the structure of your response
}

```