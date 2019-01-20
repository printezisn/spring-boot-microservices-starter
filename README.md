# Microservices Starter Guide

If you ever tried to study the overly-famous microservices, but got frustrated by the number of tools you need to learn, the level of abstraction you need to apply, found no guide to help you and you don't know where to start from, then we are the same brother. In fact, I created this project for you.

Microservices have a lot of theory and concepts, but this project focuses on the core concepts of distributed programming, while being as simple as possible (e.g. service discovery is done with environment variables with links to other services). The purpose is to help people going down the path of microservices, as I did a year ago. In the end, microservices are not dreadful, they just have their own style.

## About the Project

The **MovieStore** company has a website where users can register, search for movies, vote and also create new movies. The traffic has increased the last few months and the company decided to scale up the website. The architect came up with the following design:
* We are gonna use a NoSQL database (**MongoDB**) to store all our data (accounts, movies, etc.).
* We are gonna use **Elasticsearch** to index movies and run full text search on them.
* The website will be split into the following services:
  * The **account service**. A REST service with operations on accounts.
  * The **movie service**. A REST service with operations on movies.
  * The rest of the **website**, which will make HTTP requests to the account and movie services.
  
It's obvious that we can no longer use the ACID capabilities of a relational database. However, let's see how we can target each separate service.
  
### Account Service

The account service stores account information in a **MongoDB** database. All write operations interact with a single document, so there is nothing much to worry about.

### Movie Service

The movie service stores movie information in a **MongoDB** database and indexes them using **Elasticsearch**. The problem here is that each write operation needs to update a MongoDB document and an Elasticsearch index. If, for example, the update in MongoDB succeeds but the update in Elasticsearch fails, then we have an inconsistency that won't be fixed until the operation is called again with the same input.

This can be handled by using **two-phase commits**, which involve the following actions:
1. Update the MongoDB document, but also add information about the pending change.
1. Update the Elasticsearch index.
1. Update the MongoDB document again in order to remove the pending change information.

Now, let's consider the following cases:
1. If the first update to the MongoDB document fails, we don't have a problem.
1. If the update to the Elasticsearch index fails, we still have the pending change information in the MongoDB document, so it won't be lost.
1. If the second update to the MongoDB document fails, we still have the pending change information, so it won't be lost.

However, in order for this to work, we need the following:
1. An operation using two-phase commits must be **idempotent**. This means that we can call the operation multiple times, with the same input, but it will produce the same result without side-effects.
1. We need a background worker which can detect the documents with pending changes and run the operation for them.

One last point to keep an eye on is that multiple updates on the same document can lead to inconsistencies, because of race conditions. For example:
1. User A and user B make an update to the same movie.
1. User A's request updates the movie's document in MongoDB.
1. User B's request updates the movie's document in MongoDB.
1. User B's request updates the movie's index in Elasticsearch.
1. User A's request updates the movie's index in Elasticsearch.

As you can see, user B's update was the last one, but Elasticsearch will have an old state, by user A. The way to overcome this is by using **optimistic locking**. This involves the following actions:
1. User A reads the document from MongoDB, which contains a revision field. The revision is 1.
1. User B reads the same document from MongoDB.
1. User A updates the document, but only if its revision is 1. The update also changes the revision to 2.
1. User B updates the document, but only if its revision is 1, so the update fails.
This process makes sure that, in case of concurrent updates on the same document, any of the two-phase commit steps will fail in the revision check and the operation will be retried, thus leading to a consistent end state.

With the above strategies, we can make sure that the data are eventually consistent.

### Website

The website makes HTTP requests to the account and movie services to get information and make updates. Again, there is nothing much to worry about here.

## Getting Started

The project is made in **Java** and uses the following tools:
* [Spring Boot](http://spring.io/projects/spring-boot) - The core Java framework used.
* [Gradle](https://gradle.org/) - The build tool and package manager for Java.
* [NodeJS](https://nodejs.org/) - Used for front-end development.
* [NPM](https://www.npmjs.com/) - The package manager for front-end tools.
* [Webpack](https://webpack.js.org/) - The build tool for front-end assets.
* [Docker](https://www.docker.com/) - Used for containers.
* [Docker Compose](https://docs.docker.com/compose/) - Used for defining and running multiple containerized services.
* [MongoDB](https://www.mongodb.com/) - NoSQL database system used as the main storage system.
* [Elasticsearch](https://www.elastic.co/products/elasticsearch) - NoSQL system used for running search queries on movies.

If you want to run tests or contribute, then you need to install the above tools.
If you just want to run the project, then Docker and Docker Compose are enough (a docker-compose.yml configuration is included).

### How to run the services

Using **Docker**:
```
docker-compose up
```
Using **gradle**:
```
gradle :AccountService:bootRun
gradle :MovieService:bootRun
gradle :Website:bootRun
```

### How to run the integration tests

For the **account service**:
```
gradle :AccountService:clean && gradle :AccountService:integTest
```
For the **movie service**:
```
gradle :MovieService:clean && gradle :MovieService:integTest
```
For the **website** (requires the other services to run in test mode):
```
SPRING_PROFILES_ACTIVE=test gradle :AccountService:bootRun
SPRING_PROFILES_ACTIVE=test gradle :MovieService:bootRun
gradle :Website:clean && gradle :Website:integTest
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

