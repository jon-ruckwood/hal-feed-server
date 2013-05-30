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

# Customized links for feed entries. Links can optionally include named parameters that
# refer to attributes of the resource.
feedEntryLinks:
    - link:
        rel: other
        href: http://other.com
    - link:
        rel: other2
        href: http://other2.com/{nameOfSomeResourceAttr}
```


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
                        "self": {
                            "href": "publicBaseUrl/feedName/47407be8-f89c-466e-a7c9-cc57cc279bba"
                        }
                    },
                    "id": "47407be8-f89c-466e-a7c9-cc57cc279bba",
                    "published": "17/05/2013 15:58:07"
                },
                {
                    "_links": {
                        "self": {
                            "href": "publicBaseUrl/feedName/9433ff3b-6f9d-4a85-9902-a6cab6cd68ac"
                        }
                    },
                    "id": "9433ff3b-6f9d-4a85-9902-a6cab6cd68ac",
                    "published": "17/05/2013 14:05:07"
                }
            ]
        }
    }

Notes:

* Resource attributes are hidden when viewing the feed. This content is visible when requesting to view a specific feed entry (via the "self" link).
* Each feed entry will include an "id" and "published" property. The "id" is guaranteed to be unique per feed entry. To avoid conflicts, please avoid usage of these names for submitted resource attributes.


## To request a specific entry from feed:

    GET: publicBaseUrl/feedName/47407be8-f89c-466e-a7c9-cc57cc279bba  HTTP 1.1

### Response:

    200 OK
    Content-Type: application/hal+json
    ...

    {
        "_links": {
            "self": {
                "href": "publicBaseUrl/feedName/47407be8-f89c-466e-a7c9-cc57cc279bba"
            }
        },
        "id": "47407be8-f89c-466e-a7c9-cc57cc279bba",
        "published": "17/05/2013 15:58:07",
        "customerId": "H12345678",
        "customerName": "Mr B Hal"
    }



## To publish a new feed entry:

    POST: publicBaseUrl/feedName  HTTP 1.1

    {
        "customerId": "B18273645",
        "customerName": "Mr C Bob"
    }

### Response:

    201 Created
    Content-Type: application/hal+json
    Location: publicBaseUrl/feedName/354d1a92-d59c-4946-8965-4973419b6e80
    ...

    {
        "_links": {
            "self": {
                "href": "publicBaseUrl/feedName/354d1a92-d59c-4946-8965-4973419b6e80"
            }
        },
        "id": "354d1a92-d59c-4946-8965-4973419b6e80",
        "published": "17/05/2013 16:05:07",
        "customerId": "B18273645",
        "customerName": "Mr C Bob"
    }


# TODOs

* Currently the feed is persisted in memory (just a quick solution, implementation is inefficient).
  NoSql solution would be ideal, maybe Amazon SimpleDB in combination with Amazon S3, MongoDB etc.

* Consider support for PUT, DELETE.

* Pagination with 'next' and 'previous' links included as part of feed.

