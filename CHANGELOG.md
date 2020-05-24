# Change Log
The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

---

## v0.7

### v0.7.1 (TBD)
-   `timestamp_iso` key in `Offset` renamed to plain `timestamp`
-   `key` added to `Offset`
-   `JacksonKvRecordHttpResponseParser` to generate a consistent `UUID` as key when key is missing 

### v0.7.0 (05/23/2020)
-   `http.response.records.pointer` renamed to `http.response.list.pointer`
-   `http.response.record.value.pointer` renamed to `http.response.record.pointer`
-   Enable compound keys on `JacksonRecordParser`
-   Change default value for `http.response.record.timestamp.parser.pattern` from `yyyy-MM-dd'T'HH:mm:ss.SSSX` to `yyyy-MM-dd'T'HH:mm:ss[.SSS]X`

---

## v0.6

### v0.6.3 (05/22/2020)
-   Rename plugin folder name to match artifactId

### v0.6.2 (05/22/2020)
-   Bump okhttp.version from 4.6.0 to 4.7.2
-   Assemble a package ready to be unpacked

### v0.6.1 (05/18/2020)
-   Bump okhttp.version from 4.6.0 to 4.7.0

### v0.6.0 (05/18/2020)
-   First iteration of `PolicyResponseParser` to cope with unexpected http status codes
-   Implemented `OffsetRecordFilterFactory` to better manage de-duplication
-   Simplify configuration by providing sensible defaults

---

## v0.5.x and before

### v0.5.0 (05/10/2020)
-   `PollInterceptor` refactored into `Throttler`
-   Implemented `FixedIntervalThrottler` with default interval of 10s
-   Implemented `AdaptableIntervalThrottler` with up-to-date interval of 10s catch-up interval of 1s

### v0.4.0 (05/09/2020)
-   Timestamp offset filtering
-   Breaking changes on SPI `HttpRequestFactory` caused by introducing `Offset` class in the model 

### v0.3.5 (05/08/2020)
-   Support for response item timestamp parsing via [DateTimeFormatter](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html) with optional TimeZone
-   Support for response item timestamp parsing via [Natty](http://natty.joestelmach.com/) with optional TimeZone
-   Timestamp stored in offset map as an ISO8601 string
-   Offset pointer configuration unified into a single property containing a comma separated list of key,value pairs
-   Basic OkHttpClient logging enabled

### v0.2 (05/04/2020)
-   Support for initial offsets
-   Default values for item offsets

### v0.1-alpha (05/04/2020)
-   Initial version
