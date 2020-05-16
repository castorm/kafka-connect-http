# Kafka Connect HTTP Connector
[![Build](https://github.com/castorm/kafka-connect-http/workflows/Build/badge.svg)](https://github.com/castorm/kafka-connect-http/actions?query=workflow%3ABuild)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/35046c6e1af4450bb53f8625c9f286e5)](https://app.codacy.com/manual/castorm/kafka-connect-http?utm_source=github.com&utm_medium=referral&utm_content=castorm/kafka-connect-http&utm_campaign=Badge_Grade_Dashboard)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcastorm%2Fkafka-connect-http.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcastorm%2Fkafka-connect-http?ref=badge_shield)
[![Release to GitHub](https://github.com/castorm/kafka-connect-http/workflows/Release%20to%20GitHub/badge.svg)](https://github.com/castorm/kafka-connect-http/actions?query=workflow%3A%22Release+to+GitHub%22)
[![Release to Maven Central](https://github.com/castorm/kafka-connect-http/workflows/Release%20to%20Maven%20Central/badge.svg)](https://github.com/castorm/kafka-connect-http/actions?query=workflow%3A%22Release+to+Maven+Central%22)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.castorm/kafka-connect-http.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.castorm%22%20AND%20a:%22kafka-connect-http%22)

Set of Kafka Connect connectors that enable Kafka integration with external systems via HTTP.

## Getting Started

If your Kafka Connect deployment is automated and packaged with Maven, you can unpack the artifact on Kafka Connect
plugins folder.
```xml
<plugin>
    <artifactId>maven-dependency-plugin</artifactId>
    <execution>
        <id>copy-kafka-connect-plugins</id>
        <phase>prepare-package</phase>
        <goals>
            <goal>unpack</goal>
        </goals>
        <configuration>
            <outputDirectory>${project.build.directory}/docker-build/plugins</outputDirectory>
            <artifactItems>
                <artifactItem>
                    <groupId>com.github.castorm</groupId>
                    <artifactId>kafka-connect-http</artifactId>
                    <version>0.5.0</version>
                    <type>tar.gz</type>
                    <classifier>plugin</classifier>
                </artifactItem>
            </artifactItems>
        </configuration>
    </execution>
</plugin>
```
Otherwise, you'll have to do it manually by downloading the package from the
[Releases Page](https://github.com/castorm/kafka-connect-http/releases).

More details on how to [Install Connectors](https://docs.confluent.io/current/connect/managing/install.html).

## Source Connector
`com.github.castorm.kafka.connect.http.HttpSourceConnector`

The HTTP Source connector is meant for implementing 
[CDC (Change Data Capture)](https://en.wikipedia.org/wiki/Change_data_capture).

### Requirements for CDC
*   The HTTP resource contains an array of records that is ordered by a given set of properties present on every record
    (we'll call them **offset**)
*   The HTTP resource allows retrieving records starting from a given **offset**
*   The **offset** properties are monotonically increasing

Kafka Connect will store internally these offsets so the connector can continue from where it left after restarting.

### Examples

See [Examples](examples), e.g. 
*   [Jira Search Issues API](examples/jira-search-issues.md)

### Extension points
The connector can be easily extended by implementing your own version of any of the components below.

These are better understood by looking at the source task implementation:
```java
public List<SourceRecord> poll() throws InterruptedException {

    throttler.throttle(offset);

    HttpRequest request = requestFactory.createRequest(offset);

    HttpResponse response = requestExecutor.execute(request);

    return responseParser.parse(response).stream()
            .filter(recordFilterFactory.create(offset))
            .map(recordMapper::map)
            .collect(toList());
}

public void commitRecord(SourceRecord record) {
    offset = Offset.of(record.sourceOffset(), record.timestamp());
}
```

---
<a name="request"/>

### HttpRequestFactory: Preparing a HttpRequest
The first thing our connector will need to do is preparing a `HttpRequest`

```java
public interface HttpRequestFactory extends Configurable {

    HttpRequest createRequest(Offset offset);
}
```
> #### `http.request.factory`
> *   Type: Class
> *   Default: `com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactory`
> *   Available implementations:
>     *   `com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactory`
>
> #### `http.offset.initial`
> Initial offset, comma separated list of pairs
> *   Example: `property1=value1, property2=value2`
> *   Type: String
> *   Default: ""

#### Preparing a HttpRequest with TemplateHttpRequestFactory
This `HttpRequestFactory` is based on template resolution using the `Offset` of the last seen record.
Templates can be provided for url, headers, query params and body.

```java
public interface TemplateFactory {

    Template create(String template);
}

public interface Template {

    String apply(Offset offset);
}
```

> ##### `http.request.url`
> Http method to use in the request.
> *   Type: String
> *   Default: `GET`
> 
> ##### `http.request.method`
> Http url to use in the request, it can contain a `Template`
> *   Required
> *   Type: String
> 
> ##### `http.request.headers`
> Http headers to use in the request, comma separated list of pairs.
> *   Example: `Name: Value, Name2 = Value2`
> *   Type: String
> *   Default: ""
> 
> ##### `http.request.params`
> Http query parameters to use in the request, ampersand separated list of pairs.
> *   Example: `name=value&name2=value2`
> *   Type: String
> *   Default: ""
> 
> ##### `http.request.body`
> Http body to use in the request.
> *   Type: String
> *   Default: ""
> 
> ##### `http.request.template.factory`
> Class responsible for creating the templates that will be used on every request.
> *   Type: Class
> *   Default: `com.github.castorm.kafka.connect.http.request.template.NoTemplateFactory`
> *   Available implementations:
>     *   `com.github.castorm.kafka.connect.http.request.template.NoTemplateFactory`
>     *   `com.github.castorm.kafka.connect.http.request.template.freemarker.FreeMarkerTemplateFactory`
          Implementation based on [FreeMarker](https://freemarker.apache.org/)

---
<a name="client"/>

### HttpClient: Executing a HttpRequest
Once our HttpRequest is ready, we have to execute it to get some results out of it. That's the purpose of the 
`HttpClient`

```java
public interface HttpClient extends Configurable {

    HttpResponse execute(HttpRequest request) throws IOException;
}
```

> #### `http.client`
> *   Type: Class
> *   Default: `com.github.castorm.kafka.connect.http.client.okhttp.OkHttpClient`
> *   Available implementations:
>     *   `com.github.castorm.kafka.connect.http.client.okhttp.OkHttpClient`

#### Executing a HttpRequest with OkHttpClient
Uses a [OkHttp](https://square.github.io/okhttp/) client. 

> ##### `http.client.connection.timeout.millis`
> Timeout for opening a connection
> *   Type: Long
> *   Default: 2000
> 
> ##### `http.client.read.timeout.millis`
> Timeout for reading a response
> *   Type: Long
> *   Default: 2000
> 
> ##### `http.client.connection.ttl.millis`
> Time to live for the connection
> *   Type: Long
> *   Default: 300000
> 
> ##### `http.client.max-idle`
> Maximum number of idle connections in the connection pool
> *   Type: Integer
> *   Default: 1

---
<a name="response"/>

### HttpResponseParser: Parsing a HttpResponse
Once our `HttpRequest` has been executed, as a result we'll have to deal with a `HttpResponse` and translate it into 
a list of `HttpRecord`

```java
public interface HttpResponseParser extends Configurable {

    List<HttpRecord> parse(HttpResponse response);
}
```

> #### `http.response.parser`
> *   Type: Class
> *   Default: `com.github.castorm.kafka.connect.http.response.PolicyResponseParser`
> *   Available implementations:
>     *   `com.github.castorm.kafka.connect.http.response.PolicyResponseParser`
>     *   `com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParser`

#### Parsing a HttpResponse with PolicyResponseParser
Vets the HTTP response deciding whether the response should be processed, skipped or failed. This decision is delegated
to a `HttpResponsePolicy`. 
When decision is to process response, this processing is delegated to a secondary `HttpResponseParser`.

##### HttpResponsePolicy: Vetting a HttpResponse

```java
public interface HttpResponsePolicy extends Configurable {

    HttpResponseOutcome resolve(HttpResponse response);

    enum HttpResponseOutcome {
        PROCESS, SKIP, FAIL
    }
}
```

###### Vetting a HttpResponse with StatusCodeResponsePolicy
Does response vetting based on HTTP status codes:
*   `200`..`299`: `HttpResponseOutcome.PROCESS`
*   `300`..`399`: `HttpResponseOutcome.SKIP`
*   `400`..`599`: `HttpResponseOutcome.FAIL`

> ##### `http.response.policy`
> *   Type: Class
> *   Default: `com.github.castorm.kafka.connect.http.response.StatusCodeResponsePolicy`
> *   Available implementations:
>     *   `com.github.castorm.kafka.connect.http.response.StatusCodeResponsePolicy`

> ##### `http.response.policy.delegate`
> *   Type: Class
> *   Default: `com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParser`
> *   Available implementations:
>     *   `com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParser`

#### Parsing a HttpResponse with JacksonHttpResponseParser
Uses [Jackson](https://github.com/FasterXML/jackson) to look for the records in the response.

> ##### `http.response.records.pointer`
> [JsonPointer](https://tools.ietf.org/html/rfc6901) to the property in the response body containing an array of records 
> *   Type: String
> *   Default: "/"
> 
> ##### `http.response.record.key.pointer`
> [JsonPointer](https://tools.ietf.org/html/rfc6901) to the identifier of the individual record to be used as kafka 
  record key
> This is especially important on partitioned topics  
> *   Type: String
> *   Default: ""
> 
> ##### `http.response.record.value.pointer`
> [JsonPointer](https://tools.ietf.org/html/rfc6901) to the individual record to be used as kafka record body
> *   Type: String
> *   Default: "/"
> 
> ##### `http.response.record.timestamp.pointer`
> [JsonPointer](https://tools.ietf.org/html/rfc6901) to the timestamp of the individual record to be used as kafka 
  record timestamp
> This is especially important to track progress, enable latency calculations, improved throttling and feedback to 
  `TemplateHttpRequestFactory` 
> *   Type: String
> *   Default: ""
> 
> ##### `http.response.record.timestamp.parser`
> Class responsible for converting the timestamp property captured above into a `java.time.Instant`.  
> *   Type: String
> *   Default: `com.github.castorm.kafka.connect.http.response.timestamp.DateTimeFormatterTimestampParser`
> *   Available implementations:
>     *   `com.github.castorm.kafka.connect.http.response.timestamp.DateTimeFormatterTimestampParser` 
           Implementation based on based on a `DateTimeFormatter`
>     *   `com.github.castorm.kafka.connect.http.response.timestamp.NattyTimestampParser`
           Implementation based on [Natty](http://natty.joestelmach.com/) parser
> 
> ##### `http.response.record.timestamp.parser.pattern`
> When using `DateTimeFormatterTimestampParser`, a custom pattern can be specified 
> *   Type: String
> *   Default: `yyyy-MM-dd'T'HH:mm:ss.SSSX`
> 
> ##### `http.response.record.timestamp.parser.zone`
> Timezone of the timestamp. Accepts [ZoneId](https://docs.oracle.com/javase/8/docs/api/java/time/ZoneId.html) valid
  identifiers
> *   Type: String
> *   Default: `UTC`
> 
> ##### `http.response.record.offset.pointer`
> Comma separated list of `key=value` pairs where the key is the name of the property in the offset and the value is
  the [JsonPointer](https://tools.ietf.org/html/rfc6901) to the value being used as offset for future requests
> This is the mechanism that enables sharing state in between `HttpRequests`. `HttpRequestFactory` implementations 
  receive this `Offset`.
> *   Type: String
> *   Default: ""

---
<a name="filter"/>

### HttpRecordFilterFactory: Filtering out HttpRecord

There are cases where we'll be interested in filtering out certain records. One of these would be de-duplication.

```java
public interface HttpRecordFilterFactory extends Configurable {

    Predicate<HttpRecord> create(Offset offset);
}
```

> #### `http.record.filter.factory`
> *   Type: Class
> *   Default: `com.github.castorm.kafka.connect.http.response.PassthroughRecordFilterFactory`
> *   Available implementations:
>     *   `com.github.castorm.kafka.connect.http.response.PassthroughRecordFilterFactory`
>     *   `com.github.castorm.kafka.connect.http.response.OffsetTimestampRecordFilterFactory`

#### Filtering out HttpRecord with OffsetTimestampFilterFactory

De-duplicates based on `Offset`'s timestamp, filtering out records already processed. 
Useful when timestamp is used to filter the HTTP resource, but the filter doesn't have full timestamp precision.
Assumptions:
*   Records are ordered by timestamp
*   No two records can contain the same timestamp (to whatever precision the HTTP resource uses)

If the latter assumption cannot be satisfied, data loss will happen if two records with the same timestamp fall under
different HTTP responses.

---
<a name="mapper"/>

### SourceRecordMapper: Mapping HttpRecord to Kafka Connect's SourceRecord

Once we have our `HttpRecord`s we have to translate them into what Kafka Connect is expecting: `SourceRecord`s

Here is also where we'll tell Kafka Connect to what topic and on what partition do we want to send our record.

```java
public interface SourceRecordMapper extends Configurable {

    SourceRecord map(HttpRecord record);
}
```

> #### `http.record.mapper`
> *   Type: Class
> *   Default: `com.github.castorm.kafka.connect.http.record.SchemedSourceRecordMapper`
> *   Available implementations:
>     *   `com.github.castorm.kafka.connect.http.record.SchemedSourceRecordMapper` 

#### Mapping HttpRecord to Kafka Connect's SourceRecord with SchemedSourceRecordMapper

Embeds the record properties into a common simple envelope to enable schema evolution. This envelope contains simple
a key and a body properties.

> ##### `kafka.topic`
> Name of the topic where the record will be sent to
> *   Required
> *   Type: String
> *   Default: ""

---
<a name="throttler"/>

### Throttler: Throttling HttpRequests

Controls the rate at which HTTP requests are executed.

```java
public interface Throttler extends Configurable {

    void throttle(Offset offset) throws InterruptedException;
}
```

> #### `http.throttler`
> *   Type: Class
> *   Default: `com.github.castorm.kafka.connect.throttle.FixedIntervalThrottler`
> *   Available implementations:
>     *   `com.github.castorm.kafka.connect.throttle.FixedIntervalThrottler`
>     *   `com.github.castorm.kafka.connect.throttle.AdaptableIntervalThrottler`

#### Throttling HttpRequests with FixedIntervalThrottler

Throttles rate of requests based on a fixed interval. 

> ##### `http.throttler.interval.millis`
> Interval in between requests
> *   Type: Long
> *   Default: 10000

#### Throttling HttpRequests with AdaptableIntervalThrottler

Throttles rate of requests based on a fixed interval. However, it has two modes of operation, with two different 
intervals:
*   Up to date: No new records, or they have been created since last poll 
*   Catching up: There were new record in last poll and they were created long ago

> ##### `http.throttler.interval.millis`
> Interval in between requests when up-to-date
> 
> *   Type: Long
> *   Default: 10000
> 
> ##### `http.throttler.catchup.interval.millis`
> Interval in between requests when catching up
> *   Type: Long
> *   Default: 1000

---
## Development

### Building
```bash
mvn package
```
### Running the tests
```bash
mvn test
```
### Releasing
*   Update [CHANGELOG.md](CHANGELOG.md) and [README.md](README.md) files.
*   Prepare release: `mvn release:clean release:prepare -P package`

## Contributing

Contributions are welcome via pull requests, pending definition of code of conduct.

## Versioning

We use [SemVer](http://semver.org/) for versioning. 

## Authors

*   **Cástor Rodríguez** - Only contributor so far - [castorm](https://github.com/castorm)

## License

This project is licensed under the GPLv3 License - see the [LICENSE.txt](LICENSE.txt) file for details

## Built With

*   [Maven](https://maven.apache.org/) - Dependency Management
*   [Kafka Connect](https://kafka.apache.org/documentation/#connect) - The framework for our connectors
*   [OkHttp](https://square.github.io/okhttp/) - HTTP Client
*   [Jackson](https://github.com/FasterXML/jackson) - Json deserialization
*   [FreeMarker](https://freemarker.apache.org/) - Template engine
*   [Natty](http://natty.joestelmach.com/) - Date parser

## Acknowledgments

*   Inspired by [llofberg/kafka-connect-rest](https://github.com/llofberg/kafka-connect-rest)
