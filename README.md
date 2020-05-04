# Kafka Connect HTTP Connector
![Java CI with Maven](https://github.com/castorm/kafka-connect-http-plugin/workflows/Java%20CI%20with%20Maven/badge.svg)

Set of Kafka Connect connectors to enable Kafka integration with external systems via HTTP.

**Available Connectors:**
- `com.github.castorm.kafka.connect.http.HttpSourceConnector`

### Prerequisites

- Kafka deployment
- Kafka Connect deployment
- Ability to access the Kafka Connect deployment in order to extend its classpath 

## Getting Started

If your Kafka Connect deployment is automated and packaged with Maven, you can add the dependency from Maven Central, and unpack it on Kafka Connect plugins folder. 
```
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
