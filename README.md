# Backend Engineering Case Study

### Overview

As described in the requirements the game needs to be highly performant and scalable in order to serve to tens of
millions users daily. In order to achieve this level of performance I took some architectural design decisions.

### Architecture and Design

In order to satisfy real-time requirements of the game, I decided to use utilize redis to achieve real-time constraint.
I used redis for caching and accessing sorted data quickly. Besides using redis I also went with pure JDBC in order to
eliminate abstraction overhead and have more granular control over database interactions. Same principle also applied
for interactions with redis. I directly interacted with redis using a quite a low level api.
That way I aimed reducing overhead and unexpected performance bottlenecks due to unpredictable underlying abstractions.

Other than these decisions I made use of batching and concurrency where it is possible and needed.

For me the most challenging part was group matching part because of the requirements, I needed to match at the moment of
request made, and return a leaderboard back. This limited me quite much because I couldn't make any use of async
processing or decoupled architectures. Instead, I tried to rely on database transactions but in such a high load system
it suffered deadlocks inevitably. Even tough I implemented deadlock
recovery mechanisms it was not enough. So instead I switch synchronization in the application level.
I used monitors in order to synchronize the access to the shared resources. I definitely agree that this decision is not
even close to the optimal solution but for the correctness of the program I stick to it.
I have better solutions for a little relaxed version of the requirements that I would love to discuss.

### Tests

I wrote many unit tests aiming the service layers. Unfortunately I couldn't have time to increase test coverage any
further so, I prioritized the service layer.
In addition to unit tests I tested the system for multiple cases using JMeter and made use of it while testing
concurrent scenarios.

### How to run

I dockerized the redis as well so all components can be run using docker-compose.

```bash
docker-compose up
```

### Endpoints

I shared an exported [Postman Collection](Dream.postman_collection.json) in the root directory of the project. You can
import it and test the endpoints.
Additionally I also shared file called `Dream.http` that you can use to test the endpoints using IntelliJ IDE.