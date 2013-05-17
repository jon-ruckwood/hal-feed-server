HAL+JSON feed server
=====================

[HAL](http://stateless.co/hal_specification.html) based JSON feed server.

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

    /usr/local/config/resource-feed/server-config.yml

or specify custom location via a system property,

    java -Dconf=file-path -jar ...


Example minimal configuration file:

```yaml
# Public url of feed. May have a different port to the 'localPort'(shown below).
# This is to cater for load balancers, CNAMEs which may sit in front of your local server.
publicBaseUrl: http://www.domain.com

# Local http port the server will run on.
localPort: 5500

# Name of feed (this is used as the root context of your feed url).
feedName: test-feed
```

Example fully featured configuration file:

```yaml
# Public url of feed. May have a different port to the 'localPort'(shown below).
# This is to cater for load balancers, CNAMEs which may sit in front of your local server.
publicBaseUrl: http://www.domain.com

# Local http port the server will run on.
localPort: 5500

# Name of feed (this is used as the root context of your feed url).
feedName: test-feed

# Customised links listed within each entry in the feed.
#   rel - Link relation name
#   href - Absolute url of link. Can include {value} of resource attribute.
#   includeInSummarisedFeedEntry -
#       true  - Always include per feed entry.
#       false - Exclude from responses containing multiple feed entries
#               Include in responses containing a single feed entry.
feedEntryLinks:
    - link:
        rel: other
        href: http://other-feed.com/feed
        includeInSummarisedFeedEntry: true

    - link:
        rel: other2
        href: http://other-feed-2.com/feed/{resourceAttribute2}
        includeInSummarisedFeedEntry: false

# The names of resource attributes for displaying as part of a summarised entry.
# To minimise the data per entry within responses that contain multiple feed
# entries, a (summarised) subset of resource attributes are included for each
# entry listed.
resourceAttributesForSummarisedFeedEntry:
    - resourceAttribute1
    - resourceAttribute2
```


# Running server

To start server:

    java -jar target/resource-feed.jar


