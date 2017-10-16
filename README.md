# Ticket Reservation System
Application which provide REST API for registering movies, reserving seats and loading this information back

## App details
 - API is built with Akka HTTP.
 - Domain logic is built with Akka Actors framework. Persistence is supported with akka-persistence and embedded Cassandra DB.
 - Classes are linked together with MacWire DI framework.

### API

Next API is supported:
```
PUT
 /movie                           {                                                   Content-Type: application/json
                                    "imdbId": "tt0111141",
                                    "screenId": "screen_43456",
                                    "movieTitle": "The Shawshank Redemption",
                                    "availableSeats": 100
                                  } 

POST
 /movie/reserve-seat              {                                                   Content-Type: application/json
                                    "imdbId": "tt0111141",
                                    "screenId": "screen_43456",
                                  }
GET
 /movie?imdbId=tt0111141&screenId=screen_43456                      
```


### Distribution
In order to build executable use: `sbt oneJar` - it will generate a fat jar with all dependencies. 
Executable jar will be located by `<base_dir>/target/scala-2.12/ticket-reservation-system_2.12-1.0-one-jar.jar`


Running it via `java -jar <jar>` will launch webserver on port 9000 as well as cassandra. 
Cassandra will put its journal next to the executable jar at `tatget/.cassandra`.


### Testing
There are couple unit tests to verify Actors work and integration tests to verify overall app with API works.

Use `sbt test` to run tests
