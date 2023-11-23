# Protobuf gRPC Load Test

```bash
brew install ghz

ghz --proto=./test.proto \
--call=eventstream.proto.FooService.ProcessFoo \
--data='{"name":"John Doe", "age":30, "gender":"Male", "income":50000.0, "someList":["Reading", "Cycling"]}' \
--insecure \
--concurrency=10 \
--total=1000 \
--duration=30s \
<IPAddr>:50051

# Windows Address - run ipconfig from Command Prompt - get ipv4 addr : XX.YYY.ZZZ
```

- Results

```bash

Summary:
  Count:        514656
  Total:        30.00 s
  Slowest:      223.15 ms
  Fastest:      0.17 ms
  Average:      0.44 ms
  Requests/sec: 17155.32

Response time histogram:
  0.175   [1]      |
  22.473  [514639] |∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎
  44.770  [0]      |
  67.068  [0]      |
  89.366  [0]      |
  111.664 [0]      |
  133.962 [0]      |
  156.259 [0]      |
  178.557 [0]      |
  200.855 [0]      |
  223.153 [10]     |

Latency distribution:
  10 % in 0.29 ms 
  25 % in 0.33 ms 
  50 % in 0.38 ms 
  75 % in 0.45 ms 
  90 % in 0.58 ms 
  95 % in 0.76 ms 
  99 % in 1.36 ms 

Status code distribution:
  [OK]            514650 responses   
  [Unavailable]   5 responses        
  [Canceled]      1 responses   

```