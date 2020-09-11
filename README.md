# Tone : An in memeory rate limiter service.

This project offers RESTFul API to use as a rate limiting module for other APIs.

## Getting Started

### Prerequisites
* Git
* Java version "11.0.4" or above.
* Gradle 5.6.1 or above.
* Postman

### Clone
To get started you can simply clone this repository using git:
```
git clone https://github.com/KalpaD/Tone.git
cd Tone
```

### Testing ratelimiting API locally.

Step 1: Navigate to following directory and start the server via boot run task.

``` 
cd /Tone
// then run bootRun
gradle bootRun
``` 

Step 2: Import `Tone-RateLimiter.postman_collection.json` to postman which is located at `Postman` directory at the root of the repo.

![alt text](Images/postman_collection.png)

Step 3: You can use this collection to invoke the /allowd endpoint which does offer the rate limiting.

Step 4: Please note that the postman collection expect you to run the server on port 8080 on localhost.  




