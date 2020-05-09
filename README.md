# Kafka Connect HTTP Connector
[![Build](https://github.com/castorm/kafka-connect-http-plugin/workflows/Build/badge.svg)](https://github.com/castorm/kafka-connect-http-plugin/actions?query=workflow%3ABuild)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/35046c6e1af4450bb53f8625c9f286e5)](https://app.codacy.com/manual/castorm/kafka-connect-http-plugin?utm_source=github.com&utm_medium=referral&utm_content=castorm/kafka-connect-http-plugin&utm_campaign=Badge_Grade_Dashboard)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/48a1f47d73ba4131b0ec2f9376c06fb0)](https://www.codacy.com/manual/castorm/kafka-connect-http-plugin?utm_source=github.com&utm_medium=referral&utm_content=castorm/kafka-connect-http-plugin&utm_campaign=Badge_Coverage)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcastorm%2Fkafka-connect-http-plugin.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcastorm%2Fkafka-connect-http-plugin?ref=badge_shield)
[![DepShield Badge](https://depshield.sonatype.org/badges/castorm/kafka-connect-http-plugin/depshield.svg)](https://depshield.github.io)
[![Release to GitHub](https://github.com/castorm/kafka-connect-http-plugin/workflows/Release%20to%20GitHub/badge.svg)](https://github.com/castorm/kafka-connect-http-plugin/actions?query=workflow%3A%22Release+to+GitHub%22)
[![Release to Maven Central](https://github.com/castorm/kafka-connect-http-plugin/workflows/Release%20to%20Maven%20Central/badge.svg)](https://github.com/castorm/kafka-connect-http-plugin/actions?query=workflow%3A%22Release+to+Maven+Central%22)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.castorm/kafka-connect-http-plugin.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.castorm%22%20AND%20a:%22kafka-connect-http-plugin%22)

Set of Kafka Connect connectors that enable Kafka integration with external systems via HTTP.

## Getting Started

If your Kafka Connect deployment is automated and packaged with Maven, you can unpack the artifact on Kafka Connect plugins folder. 
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
                    <artifactId>kafka-connect-http-plugin</artifactId>
                    <version>0.3.4</version>
                    <type>tar.gz</type>
                    <classifier>plugin</classifier>
                </artifactItem>
            </artifactItems>
        </configuration>
    </execution>
</plugin>
```
Otherwise, you'll have to do it manually by downloading the package from the [Releases Page](https://github.com/castorm/kafka-connect-http-plugin/releases).

More details on how to [Install Connectors](https://docs.confluent.io/current/connect/managing/install.html)

## Source Connector
`com.github.castorm.kafka.connect.http.HttpSourceConnector`

The HTTP Source connector is meant to implement [CDC (Change Data Capture)](https://en.wikipedia.org/wiki/Change_data_capture).

### Requirements for CDC
*   The HTTP resource contains an array of items that is ordered by a given set of properties present on every item (we'll call them **offset**)
*   The HTTP resource allows retrieving items starting from a given **offset**
*   The **offset** properties are monotonically increasing

Kafka Connect will store internally these offsets so the connector can continue from where it left after restarting.

The connector breaks down the different responsibilities into the following components.

| Property                       | Description                                                         |
|--------------------------------|---------------------------------------------------------------------|
| `http.request.factory`         | [`com...request.offset.OffsetTemplateHttpRequestFactory`](#request) | 
| `http.client`                  | [`com....client.okhttp.OkHttpClient`](#client)                      | 
| `http.response.parser`         | [`com...response.jackson.JacksonHttpResponseParser`](#response)     | 
| `http.response.filter.factory` | [`com...response.spi.HttpResponseFilterFactory`](#filter)           | 
| `http.record.mapper`           | [`com...record.SchemedSourceRecordMapper`](#record)                 |
| `http.poll.interceptor`        | [`com...poll.IntervalDelayPollInterceptor`](#interceptor)           |  
| `http.offset.initial`          | Initial offset, comma separated list of pairs `offset=value`        |  

Below further details on these components 

<a name="request"/>

### Examples

See [Examples](examples), e.g. 
*   [Jira Search Issues API](examples/jira-search-issues.md)

### HttpRequestFactory
Responsible for creating the `HttpRequest`.

#### OffsetTemplateHttpRequestFactory
`com.github.castorm.kafka.connect.http.request.offset.OffsetTemplateHttpRequestFactory`

Enables offset injection on url, headers, query params and body via templates

| Property                        | Req | Default             | Description                                                  |
|:--------------------------------|:---:|:-------------------:|:-------------------------------------------------------------|
| `http.request.url`              | *   | -                   | HTTP Url                                                     |
| `http.request.method`           | -   | GET                 | HTTP Method                                                  |
| `http.request.headers`          | -   | -                   | HTTP Headers, Comma separated list of pairs `Name: Value`    |
| `http.request.params`           | -   | -                   | HTTP Method, Ampersand separated list of pairs `name=value`  |
| `http.request.body`             | -   | -                   | HTTP Body                                                    |
| `http.request.template.factory` | -   | `NoTemplateFactory` | Template factory                                             |

### TemplateFactory
#### FreeMarkerTemplateFactory
`com.github.castorm.kafka.connect.http.request.offset.freemarker.FreeMarkerTemplateFactory`

[FreeMarker](https://freemarker.apache.org/) based implementation of `TemplateFactory`

<a name="client"/>

### HttpClient
Responsible for executing the `HttpRequest`, obtaining a `HttpResponse` as a result.

#### OkHttpClient
`com.github.castorm.kafka.connect.http.client.okhttp.OkHttpClient`

Uses a pooled [OkHttp](https://square.github.io/okhttp/) client. 

| Property                                | Req | Default | Description                       |
|:----------------------------------------|:---:|:-------:|:----------------------------------|
| `http.client.connection.timeout.millis` | -   | 2000    | Connection timeout                |
| `http.client.read.timeout.millis`       | -   | 2000    | Read timeout                      |
| `http.client.connection.ttl.millis`     | -   | 300000  | Connection time to live           |
| `http.client.max-idle`                  | -   | 5       | Max. idle connections in the pool |

<a name="response"/>

### HttpResponseParser
Responsible for parsing the resulting `HttpResponse` into a list of individual items.

#### JacksonHttpResponseParser
`com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParser`

Uses [Jackson](https://github.com/FasterXML/jackson) to look for the relevant aspects of the response. 

| Property                                    | Req | Default                            | Description                                                                                                                                                                                                                                  |
|:--------------------------------------------|:---:|:----------------------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `http.response.items.pointer`               | -   | /                                  | [JsonPointer](https://tools.ietf.org/html/rfc6901) to the property containing an array of items                                                                                                                                              |
| `http.response.item.key.pointer`            | -   | -                                  | [JsonPointer](https://tools.ietf.org/html/rfc6901) to the identifier of the individual item to be used as kafka record key                                                                                                                   |
| `http.response.item.value.pointer`          | -   | /                                  | [JsonPointer](https://tools.ietf.org/html/rfc6901) to the individual item to be used as kafka record body                                                                                                                                    |
| `http.response.item.timestamp.pointer`      | -   | -                                  | [JsonPointer](https://tools.ietf.org/html/rfc6901) to the timestamp of the individual item to be used as kafka record timestamp                                                                                                              |
| `http.response.item.timestamp.parser.class` | -   | `DateTimeFormatterTimestampParser` | Converts the timestamp property into a `java.time.Instant`                                                                                                                                                                                   |
| `http.response.item.offset.pointer`         | -   | -                                  | Comma separated list of key=value pairs where the key is the name of the item in the offset and the value is [JsonPointer](https://tools.ietf.org/html/rfc6901) to the value of the individual item being used as offset for future requests |

#### DateTimeFormatterTimestampParser
`com.github.castorm.kafka.connect.http.response.timestamp.DateTimeFormatterTimestampParser`

TimestampParser based on a `DateTimeFormatter`

| Property                                      | Req | Default                      | Description                                                                                                                    |
|:----------------------------------------------|:---:|:----------------------------:|:-------------------------------------------------------------------------------------------------------------------------------|
| `http.response.item.timestamp.parser.pattern` | -   | `yyyy-MM-dd'T'HH:mm:ss.SSSX` | `DateTimeFormatter` pattern                                                                                                    |
| `http.response.item.timestamp.parser.zone`    | -   | `UTC`                        | TimeZone of the timestamp. Accepts [ZoneId](https://docs.oracle.com/javase/8/docs/api/java/time/ZoneId.html) valid identifiers |

#### NattyTimestampParser
`com.github.castorm.kafka.connect.http.response.timestamp.NattyTimestampParser`

TimestampParser based on [Natty](http://natty.joestelmach.com/) parser

| Property                                   | Req | Default | Description                                                                                                                    |
|:-------------------------------------------|:---:|:-------:|:-------------------------------------------------------------------------------------------------------------------------------|
| `http.response.item.timestamp.parser.zone` | -   | `UTC`   | TimeZone of the timestamp. Accepts [ZoneId](https://docs.oracle.com/javase/8/docs/api/java/time/ZoneId.html) valid identifiers |

<a name="filter"/>

### HttpResponseFilterFactory
Responsible for filtering out items from the `HttpResponse` 

#### PassthroughFilterFactory
Default filter which doesn't actually filter anything out.

#### OffsetFilterFactory
De-duplicates based on offset's timestamp, filtering out records already processed. Assumes records will be ordered by timestamp.
Useful when timestamp is used to filter the HTTP resource, but the filter doesn't have full timestamp granularity.

<a name="record"/>

### SourceRecordMapper
Responsible for mapping individual items from the response into Kafka Connect `SourceRecord`.

#### SchemedSourceRecordMapper
`com.github.castorm.kafka.connect.http.record.SchemedSourceRecordMapper`

Embeds the item properties into a common simple envelope to enable schema evolution. This envelope contains simple a key and a body properties. 

| Property      | Req | Default | Description                                        |
|:--------------|:---:|:-------:|:---------------------------------------------------|
| `kafka.topic` | *   | -       | Name of the topic where the record will be sent to |

<a name="interceptor"/>

### PollInterceptor
Hooks that enable influencing the poll control flow.

#### IntervalDelayPollInterceptor
`com.github.castorm.kafka.connect.http.poll.IntervalDelayPollInterceptor`

Throttles rate of requests based on a given interval, except when connector is not up-to-date. 

| Property                    | Req | Default | Description                                  |
|:----------------------------|:---:|:-------:|:---------------------------------------------|
| `http.poll.interval.millis` | -   | 60000   | Interval in between requests once up-to-date |

### Prerequisites

*   Kafka deployment
*   Kafka Connect deployment
*   Ability to access the Kafka Connect deployment in order to extend its classpath 

## Development

### SPI
The connector can be easily extended by implementing your own version of any of the components below.

These are better understood by looking at the source task implementation:
```java
public List<SourceRecord> poll() throws InterruptedException {

    pollInterceptor.beforePoll();

    HttpRequest request = requestFactory.createRequest(lastConfirmedOffset);

    HttpResponse response = requestExecutor.execute(request);

    List<SourceRecord> records = responseParser.parse(response).stream()
            .filter(responseFilterFactory.create(lastConfirmedOffset))
            .map(recordMapper::map)
            .collect(toList());

    return pollInterceptor.afterPoll(records);
}

public void commitRecord(SourceRecord record) {
    lastConfirmedOffset = Offset.of(record.sourceOffset());
}
```

#### HttpRequestFactory
```java
public interface HttpRequestFactory extends Configurable {

    HttpRequest createRequest(Offset offset);
}
```

#### OffsetTemplateFactory
```java
public interface OffsetTemplateFactory {

    OffsetTemplate create(String template);
}

public interface OffsetTemplate {

    String apply(Offset offset);
}
```

#### HttpClient
```java
public interface HttpClient extends Configurable {

    HttpResponse execute(HttpRequest request) throws IOException;
}
```

#### HttpResponseParser
```java
public interface HttpResponseParser extends Configurable {

    List<HttpResponseItem> parse(HttpResponse response);
}
```

#### HttpResponseFilterFactory
```java
public interface HttpResponseFilterFactory extends Configurable {

    Predicate<HttpResponseItem> create(Offset offset);
}
```

#### SourceRecordMapper
```java
public interface SourceRecordMapper extends Configurable {

    SourceRecord map(HttpResponseItem item);
}
```

#### PollInterceptor
```java
public interface PollInterceptor extends Configurable {

    void beforePoll() throws InterruptedException;

    List<SourceRecord> afterPoll(List<SourceRecord> records);
}
```

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
