# Load testing of Key-DB (opensource clone of Redis)

Here's the table converted to Markdown format:

| No of requests | push scheduler | 3 min pull scheduler | 5 min pull scheduler |
|----------------|-----------------|----------------------|----------------------|
| 100            | 243 ms          | 3 ms                 | 4 ms                 |
| 1000           | 698 ms          | 21 ms                | 13 ms                |
| 2000           | 1111 ms         | 23 ms                | 18 ms                |
| 3000           | 1502 ms         | 37 ms                | 27 ms                |
| 5000           | 2398 ms         | 59 ms                | 42 ms                |
| 10000          | 4237 ms         | 75 ms                | 84 ms                |
| 100000         | 23119 ms        | 729 ms               | 725 ms               |
