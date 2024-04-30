# Backend Engineering Case Study

### Overview

As described in the requirements the game needs to be highly performant and scalable in order to serve to tens of
millions of users daily. In order to achieve this level of performance I took some architectural design decisions.

### Architecture and Design

In order to satisfy the real-time requirements of the game, I decided to utilize redis to achieve real-time constraints.
I used Redis for caching and accessing sorted data quickly. Besides using Redis I also went with pure JDBC in order to
eliminate abstraction overhead and have more granular control over database interactions. The same principle also applied
to interactions with Redis. I directly interacted with Redis using quite a low-level API.
That way I aimed to reduce overhead and unexpected performance bottlenecks due to unpredictable underlying abstractions.

Other than these decisions I made use of batching and concurrency where it is possible and needed.

For me the most challenging part was the group matching part because of the requirements, I needed to match at the moment of
request made and return a leaderboard back. This limited me quite much because I couldn't make any use of async
processing or decoupled architectures. Instead, I tried to rely on database transactions but in such a high-load system
it suffered deadlocks inevitably. Even though I implemented deadlock
recovery mechanisms it was not enough. So instead I switch synchronization at the application level.
I used monitors to synchronize the access to the shared resources. I agree that this decision is not
even close to the optimal solution but for the correctness of the program I stick to it.
I have better solutions for a little relaxed version of the requirements that I would love to discuss.

### Tests

I wrote many unit tests aiming at the service layers. Unfortunately, I didn't have time to increase test coverage any
further, so I prioritized the service layer.
In addition to unit tests, I tested the system for multiple cases using JMeter and made use of it while testing
concurrent scenarios.

### How to run

I dockerized the redis as well so all components can be run using docker-compose.

```bash
docker-compose up
```

### Endpoints

I shared an exported [Postman Collection](Dream.postman_collection.json) in the root directory of the project. You can
import it and test the endpoints.
Additionally, I also shared a file called `Dream.http` that you can use to test the endpoints using IntelliJ IDE.


### Design Schemas
## DB Design
<img width="1202" alt="image" src="https://github.com/emresin12/backend-engineering-case-study/assets/17512057/de90d39d-c586-42f8-b468-971c4eeb68c0">

## Redis Desing
<img width="637" alt="image" src="https://github.com/emresin12/backend-engineering-case-study/assets/17512057/7d05ea4b-eb5e-4086-9a7f-4588700dbb8d">


