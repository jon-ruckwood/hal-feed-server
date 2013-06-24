HAL+JSON feed server
=====================

[![Build Status](https://travis-ci.org/qmetric/hal-feed-server.png)](https://travis-ci.org/qmetric/hal-feed-server)

[HAL+JSON](http://stateless.co/hal_specification.html) based feed server.

A feed contains feed entries in descending order of publish date.

A feed entry contains a payload of data specific to your domain, represented by any valid JSON.

Operations currently supported on feed:

* GET to view complete feed of entries
* GET to view paginated feed of entries (experimental)
* GET to view specific feed entry
* POST to publish new feed entry


# Building server

To build:

    mvn clean package


# Configuration

To configure the server, create a file in the following location

    /usr/local/config/hal-feed-server/server-config.yml

or specify custom location via a system property,

    java -Dconf=file-path -jar ...


Example configuration file:

```yaml
# Public url of feed. May have a different port to the 'localPort'(shown below).
# This is to cater for load balancers, CNAMEs which may sit in front of your local server.
publicBaseUrl: http://www.domain.com

# Local http port the server will listen on.
localPort: 5500

# Name of feed (this is used as the root context of your feed url).
feedName: test-feed

# Customized links for feed entries. Links can optionally include named parameters that
# refer to attributes of the payload.
feedEntryLinks:
    - link:
        rel: other
        href: http://other.com
    - link:
        rel: other2
        href: http://other2.com/{nameOfSomePayloadAttr}

# Mysql data source for feed persistence
mysqlDataSource:
    url: jdbc:mysql://localhost:3306/feed
    username: user
    password: password
```


# Database schema creation/ modification

Patches for the database schema are sync'd automatically during server startup.


# Running server

To start server:

    java -jar target/hal-feed-server.jar


# Usage

## To request the current feed:

    GET: publicBaseUrl/feedName  HTTP 1.1

### Response:

    200 OK
    Content-Type: application/hal+json
    ...

    {
        "_links": {
            "self": {
                "href": "publicBaseUrl/feedName"
            }
        },
        "_embedded": {
            "entries": [
                {
                    "_links": {
                        "self": {"href": "publicBaseUrl/feedName/2"}
                    },
                    "_id": "2",
                    "_published": "17/05/2013 15:58:07"
                },
                {
                    "_links": {
                        "self": {"href": "publicBaseUrl/feedName/1"}
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

    GET: publicBaseUrl/feedName/2  HTTP 1.1

### Response:

    200 OK
    Content-Type: application/hal+json
    ...

    {
        "_links": {
            "self": {"href": "publicBaseUrl/feedName/2"}
        },
        "_id": "2",
        "_published": "17/05/2013 15:58:07",
        "customerId": "H12345678",
        "customerName": "Mr B Hal"
    }



## To publish a new feed entry containing given payload attributes:

    POST: publicBaseUrl/feedName  HTTP 1.1

    {
        "customerId": "B18273645",
        "customerName": "Mr C Bob"
    }

### Response:

    201 Created
    Content-Type: application/hal+json
    Location: publicBaseUrl/feedName/3
    ...

    {
        "_links": {
            "self": {"href": "publicBaseUrl/feedName/3"}
        },
        "_id": "3",
        "_published": "17/05/2013 16:05:07",
        "customerId": "B18273645",
        "customerName": "Mr C Bob"
    }



# Pagination (experimental, until fully tested)

## To request the latest page of entries (defaults to max of 10 entries per page):

    GET: publicBaseUrl/feedName/experimental  HTTP 1.1

* Response includes a "next" link relation for navigating to an earlier page of entries (if no earlier entries, then the "next" link will not be included)

* Response includes a "previous" link relation for navigating to a later page of entries (if no later entries, then the "previous" link will not be included)
