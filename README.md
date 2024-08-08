# Spring-redis

# performance bencmark / load testing results [Redis] :

| Number of Requests | 3 mins Pull Scheduler | 5 mins Pull Scheduler | Push Scheduler |
|--------------------|-----------------------|-----------------------|-----------------|
| 100                | 18 ms                 | 17 ms                 | 137 ms          |
| 1000               | 61 ms                 | 53 ms                 | 715 ms          |
| 2000               | 98 ms                 | 76 ms                 | 1132 ms         |
| 3000               | 138 ms                | 119 ms                | 1530 ms         |
| 5000               | 191 ms                | 154 ms                | 2187 ms         |
| 10000              | 325 ms                | 303 ms                | 4156 ms         |
| 100000             | 6633 ms               | 6166 ms               | 43652 ms        |


