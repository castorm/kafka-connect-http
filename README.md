# Kafka Connect HTTP Connector
![Java CI with Maven](https://github.com/castorm/kafka-connect-http-plugin/workflows/Java%20CI%20with%20Maven/badge.svg)

Set of Kafka Connect connectors that enable Kafka integration with external systems via HTTP.

**Available Connectors:**
- `com.github.castorm.kafka.connect.http.HttpSourceConnector`

## Source Connector

A HTTP Source connector is broken down into the following components. You can implement your own version of them.

**Configuration properties**

| Property | Required | Default Value |
|---|---|---|
| `http.source.poll.interceptor` | - | IntervalDelayPollInterceptor | 
| `http.client` | - | OkHttpClient | 
| `http.source.request.factory` | - | TemplateHttpRequestFactory | 
| `http.source.response.parser` | - | JacksonHttpResponseParser | 
| `http.source.record.mapper` | - | SchemedSourceRecordMapper | 

### HttpRequestFactory
Responsible for creating the `HttpRequest`.
```java
public interface HttpRequestFactory extends Configurable {

    void setOffset(Map<String, ?> offset);

    HttpRequest createRequest();
}
```
Available implementations:

#### TemplateHttpRequestFactory
```com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactory```

Enables offset injection on url, headers, query params and body via templates

**Configuration properties**

| Property | Required | Default Value | Description |
|---|---|---|---|
| `http.source.url` | * | - | HTTP Url |
| `http.source.method` | - | GET | HTTP Method |
| `http.source.headers` | - | - | HTTP Headers, Comma separated list of pairs `Name: Value` |
| `http.source.query-params` | - | - | HTTP Method, Ampersand separated list of pairs `name=value` |
| `http.source.body` | - | - | HTTP Body |
| `http.source.template.factory` | - | `NoTemplateFactory` | Template factory |

##### TemplateFactory
```java
public interface TemplateFactory {

    Template create(String template);
}

public interface Template {

    String apply(Map<String, ?> offset);
}
```

Available implementations:

##### FreeMarkerTemplateFactory
```com.github.castorm.kafka.connect.http.request.template.freemarker.FreeMarkerTemplateFactory```

[FreeMarker](https://freemarker.apache.org/) based implementation of `TemplateFactory`

### HttpClient
Responsible for executing the `HttpRequest`, obtaining a `HttpResponse` as a result.
```java
public interface HttpClient extends Configurable {

    HttpResponse execute(HttpRequest request) throws IOException;
}
```
Available implementations:

#### OkHttpClient
```com.github.castorm.kafka.connect.http.client.okhttp.OkHttpClient```

Uses a pooled [OkHttp](https://square.github.io/okhttp/) client. 

**Configuration properties**

| Property | Required | Default Value | Description |
|---|---|---|---|
| `http.client.connection.timeout.millis` | - | 2000 | Connection timeout |
| `http.client.read.timeout.millis` | - | 2000 | Read timeout |
| `http.client.connection.ttl.millis` | - | 300000 | Connection time to live |
| `http.client.max-idle` | - | 5 | Max. idle connections in the pool |

### HttpResponseParser
Responsible for parsing the resulting `HttpResponse` into a list of individual items.
```java
public interface HttpResponseParser extends Configurable {

    List<HttpResponseItem> parse(HttpResponse response);
}
```
Available implementations:

#### JacksonHttpResponseParser
```com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParser```

Uses [Jackson](https://github.com/FasterXML/jackson) to look for the relevant aspects of the response. 

**Configuration properties**

| Property | Required | Default Value | Description |
|---|---|---|---|
| `http.source.response.json.items.pointer` | - | / | [JsonPointer](https://tools.ietf.org/html/rfc6901) to the property containing an array of items |
| `http.source.response.json.item.key.pointer` | - | - | [JsonPointer](https://tools.ietf.org/html/rfc6901) to the identifier of the individual item to be used as kafka record key |
| `http.source.response.json.item.value.pointer` | - | / | [JsonPointer](https://tools.ietf.org/html/rfc6901) to the individual item to be used as kafka record body |
| `http.source.response.json.item.timestamp.pointer` | - | - | [JsonPointer](https://tools.ietf.org/html/rfc6901) to the timestamp of the individual item to be used as kafka record timestamp |
| `http.source.response.json.item.offset.value.pointer` | - | - | [JsonPointer](https://tools.ietf.org/html/rfc6901) to the value of the individual item to be used as offset for future requests |
| `http.source.response.json.item.offset.key` | - | offset | Name of the offset property to be used in HTTP Request templates |

### SourceRecordMapper
Responsible for mapping individual items from the response into Kafka Connect `SourceRecord`.
```java
public interface SourceRecordMapper extends Configurable {

    SourceRecord map(HttpResponseItem item);
}
```
Available implementations:

#### SchemedSourceRecordMapper
```com.github.castorm.kafka.connect.http.record.SchemedSourceRecordMapper```

Embeds the item properties into a common simple envelope to enable schema evolution. This envelope contains simple a key and a body properties. 

**Configuration properties**

| Property | Required | Default Value | Description |
|---|---|---|---|
| `kafka.topic` | * | - | Name of the topic where the record will be sent to |


### PollInterceptor
Hooks that enable influencing the poll control flow.
```java
public interface PollInterceptor extends Configurable {

    void beforePoll() throws InterruptedException;

    void afterPoll(List<SourceRecord> records);
}
```
Available implementations:

#### IntervalDelayPollInterceptor
```com.github.castorm.kafka.connect.http.poll.IntervalDelayPollInterceptor```

Throttles rate of requests based on a given interval, except when connector is not up-to-date. 

**Configuration properties**

| Property | Required | Default Value | Description |
|---|---|---|---|
| `http.source.poll.interval.millis` | - | 60000 | Interval in between requests once up-to-date |


### Prerequisites

- Kafka deployment
- Kafka Connect deployment
- Ability to access the Kafka Connect deployment in order to extend its classpath 

## Getting Started

If your Kafka Connect deployment is automated and packaged with Maven, you can add the dependency from Maven Central, and unpack it on Kafka Connect plugins folder. 
```xml
<dependency>
    <groupId>com.github.castorm</groupId>
    <artifactId>kafka-connect-http-plugin</artifactId>
    <version>0.1-alpha</version>
</dependency>
```

Otherwise, you can just manually download the package and unpack it manually on Kafka Connect plugins folder:

[Releases Page](https://github.com/castorm/kafka-connect-http-plugin/releases)

### Installing

[Install Connectors](https://docs.confluent.io/current/connect/managing/install.html)

## Development

### Building
```
mvn package
```
### Running the test
```
mvn test
```
### Releasing
- Update release version: `mvn versions:set -DnewVersion=X.Y.Z`
- Validate and then commit version: `mvn versions:commit`
- Update [CHANGELOG.md](CHANGELOG.md) and [README.md](README.md) files.
- Merge to master.
- Deploy to Maven Central: `mvn clean deploy -P release`
- Create release on Github project.

## Contributing

Contributions are accepted via pull requests, pending definition of code of conduct.

## Versioning

We use [SemVer](http://semver.org/) for versioning. 

## Authors

* **Cástor Rodríguez** - *Initial work* - [castorm](https://github.com/castorm)

Pending contributions

## License

This project is licensed under the GPLv3 License - see the [LICENSE.txt](LICENSE.txt) file for details

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
* [Kafka Connect](https://kafka.apache.org/documentation/#connect) - The framework for our connectors
* [OkHttp](https://square.github.io/okhttp/) - HTTP Client
* [Jackson](https://github.com/FasterXML/jackson) - Json deserialization
* [FreeMarker](https://freemarker.apache.org/) - Template engine

## Acknowledgments

* Inspired by https://github.com/llofberg/kafka-connect-rest
