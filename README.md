HAL+JSON feed server
=====================

[![Build Status](https://travis-ci.org/qmetric/hal-feed-server.png)](https://travis-ci.org/qmetric/hal-feed-server)

[HAL+JSON](http://stateless.co/hal_specification.html) based feed server.

The server is built using [Dropwizard](http://dropwizard.codahale.com/).

A feed contains feed entries in descending order of publish date.

A feed entry contains a payload of data specific to your domain, represented by any valid JSON.

Operations currently supported on feed:

* GET to view complete feed of entries
* GET to view paginated feed of entries (experimental)
* GET to view specific feed entry
* POST to publish new feed entry


# Downloading pre-built server JAR

The server JAR can be downloaded from https://oss.sonatype.org/content/groups/public/com/qmetric/hal-feed-server/


# Building server JAR from source

To build:

    mvn clean package


# Configuration

To configure the server, create a file in the following location

    /usr/local/config/hal-feed-server/server-config.yml

or specify custom location via a system property,

    java -Dconf=file-path -jar ...


Example configuration file:

```yaml
# Public url of feed.
# This is to cater for load balancers, CNAMEs which may sit in front of your local server.
publicBaseUrl: http://www.domain.com

# Name of feed
feedName: Test feed

# Additional links to include for each feed entry. Links can optionally include named
# parameters (i.e. {name}) that refer to attributes belonging to a feed entry's payload.
feedEntryLinks:
  - rel: other
    href: http://other.com
  - rel: other2
    href: http://other2.com/{nameOfSomePayloadAttr}

# Data source for feed persistence (only Mysql supported)
databaseConfiguration:
  driverClass: com.mysql.jdbc.Driver
  user: usr
  password: pwd
  url: jdbc:mysql://localhost:3306/feed-db
  validationQuery: select 1 from dual

# Local server HTTP configuration
http:
  port: 8080
  adminPort: 8081
  requestLog:
    timeZone: GB
    console:
      enabled: false
    file:
      enabled: true
      currentLogFilename: /usr/local/logs/hal-feed-access.log
      archivedLogFilenamePattern: /usr/local/logs/hal-feed-access-%d.log.gz
      archivedFileCount: 5

# Logging configuration
logging:
  level: INFO
  console:
    enabled: false
    timeZone: GB
  file:
    enabled: true
    timeZone: GB
    logFormat: null
    currentLogFilename: /usr/local/logs/hal-feed.log
    archive: true
    archivedLogFilenamePattern: /usr/local/logs/hal-feed-%d.log.gz
    archivedFileCount: 5
```

This is a [Dropwizard](http://dropwizard.codahale.com/) configuration file - further configuration options available for database, http and logging.


# Database schema creation/ modification

Patches for the database schema are sync'd automatically during server startup.


# Running server

To start server:

    java -jar target/hal-feed-server.jar


# Usage

## To request the current feed:

    GET: <publicBaseUrl>/feed  HTTP 1.1

### Response:

    200 OK
    Content-Type: application/hal+json
    ...

    {
        "_name": "Test feed",

        "_links": {
            "self": {
                "href": "<publicBaseUrl>/feed"
            }
        },

        "_embedded": {
            "entries": [
                {
                    "_links": {
                        "self": {"href": "<publicBaseUrl>/feed/2"}
                    },
                    "_id": "2",
                    "_published": "17/05/2013 15:58:07"
                },
                {
                    "_links": {
                        "self": {"href": "<publicBaseUrl>/feed/1"}
                    },
                    "_id": "1",
                    "_published": "17/05/2013 14:05:07"
                }
            ]
        }
    }

Notes:

* Custom payload attributes are hidden when viewing the feed. These attributes are visible only when requesting a specific feed entry (via the "self" link).
* Each feed entry will include additional "_id" and "_published" attributes. The "_id" is guaranteed to be unique per feed entry. To avoid conflicts, it's advisable not to prefix payload attributes with underscores.


## To request a specific entry from feed:

    GET: <publicBaseUrl>/feed/2  HTTP 1.1

### Response:

    200 OK
    Content-Type: application/hal+json
    ...

    {
        "_links": {
            "self": {"href": "<publicBaseUrl>/feed/2"}
        },
        "_id": "2",
        "_published": "17/05/2013 15:58:07",
        "customerId": "H12345678",
        "customerName": "Mr B Hal"
    }



## To publish a new feed entry containing given payload attributes:

    POST: <publicBaseUrl>/feed  HTTP 1.1

    {
        "customerId": "B18273645",
        "customerName": "Mr C Bob"
    }

### Response:

    201 Created
    Content-Type: application/hal+json
    Location: <publicBaseUrl>/feed/3
    ...

    {
        "_links": {
            "self": {"href": "<publicBaseUrl>/feed/3"}
        },
        "_id": "3",
        "_published": "17/05/2013 16:05:07",
        "customerId": "B18273645",
        "customerName": "Mr C Bob"
    }



# Pagination (experimental, until fully tested)

## To request the latest page of entries (defaults to max of 10 entries per page):

    GET: <publicBaseUrl>/feed/experimental  HTTP 1.1

* Response includes a "next" link relation for navigating to an earlier page of entries (if no earlier entries, then the "next" link will not be included)

* Response includes a "previous" link relation for navigating to a later page of entries (if no later entries, then the "previous" link will not be included)



# Consuming feed

Libraries written to consume from feeds:

* Java - https://github.com/qmetric/hal-feed-consumer



# Health check and Metrics

Provided by [Dropwizard](http://dropwizard.codahale.com/):

* GET: serverhost:port/ping

* GET: serverhost:adminPort/ping

* GET: serverhost:adminPort/healthcheck

* GET: serverhost:adminPort/healthcheck?pretty=true

* GET: serverhost:adminPort/threads
