HAL+JSON feed server
=====================

[HAL+JSON](http://stateless.co/hal_specification.html) based feed server.

A feed contains entries in descending order of publish date.

Each entry in the feed corresponds to a specific resource.

A resource is specific to your domain, and can be represented by any valid JSON.


Operations currently supported on feed:

* GET to view feed of entries
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
```


# Running server

To start server:

    java -jar target/hal-feed-server.jar


# Usage

## To request current feed:

    GET: http://publicBaseUrl/feedName  HTTP 1.1

### Response:

    200 OK
    Content-Type: application/hal+json
    ...

    {
        "_links": {
            "self": {
                "href": "http://publicBaseUrl/feedName"
            }
        },
        "_embedded": {
            "entries": [
                {
                    "_links": {
                        "self": {
                            "href": "http://publicBaseUrl/feedName/2"
                        }
                    },
                    "published": "17/05/2013 15:58:07",
                    "customerId": "H12345678",
                    "customerName": "Mr B Hal"
                },
                {
                    "_links": {
                        "self": {
                            "href": "http://publicBaseUrl/feedName/1"
                        }
                    },
                    "published": "17/05/2013 14:05:07",
                    "customerId": "D87654321",
                    "customerName": "Mr A Dave"
                }
            ]
        }
    }


## To request specific entry from feed:

    GET: http://publicBaseUrl/feedName/2  HTTP 1.1

### Response:

    200 OK
    Content-Type: application/hal+json
    ...

    {
        "_links": {
            "self": {
                "href": "http://publicBaseUrl/feedName/2"
            }
        },
        "published": "17/05/2013 15:58:07",
        "customerId": "H12345678",
        "customerName": "Mr B Hal"
    }



## To publish new feed entry:

    POST: http://publicBaseUrl/feedName  HTTP 1.1

    {
        "customerId": "B18273645",
        "customerName": "Mr C Bob"
    }

### Response:

    201 Created
    Content-Type: application/hal+json
    ...

    {
        "_links": {
            "self": {
                "href": "http://publicBaseUrl/feedName/3"
            }
        },
        "published": "17/05/2013 16:05:07",
        "customerId": "B18273645",
        "customerName": "Mr C Bob"
    }


# TODOs

* Currently the feed is persisted in memory (just a quick solution, implementation is inefficient).
  NoSql solution would be ideal, maybe Amazon SimpleDB in combination with Amazon S3.

* Support for PUT, DELETE.

* Pagination with 'next' and 'previous' links included as part of feed.

