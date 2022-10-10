# Change Log
The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

---

## v0.8

### v0.8.11 (03/26/2021)
-   Support for null item lists, courtesy of @ueisele

### v0.8.10 (02/07/2021)
-   Escaping headers' colons

### v0.8.9 (01/04/2021)
-   Added implementation to parse substring as timestamp field 

### v0.8.8 (12/25/2020)
-   Bugfix #82: NPE when commit before ever polling. Happening on long initial polls 

### v0.8.7 (12/06/2020)
-   FreeMarker number representation as "computer" by default, offset.timestamp explicitly documented as ISO8601 format, elasticsearch
    example updated accordingly 

### v0.8.6 (11/30/2020)
-   Fix ConfluentHub package

### v0.8.5 (11/30/2020)
-   Handle records committed out of order

### v0.8.1 (11/09/2020)
-   Project restructured to enable integration testing

### v0.8.0 (11/07/2020)
-   Provided different log levels for `OkHttpClient`.`TRACE`: `BODY`, `DEBUG`: `BASIC`, `*`: `NONE`
-   Refactored throttler adding the notion of timer
-   Support for authentication extension. Initial implementations for None and Basic authentication types

## v0.7

### v0.7.6 (05/28/2020)
-   Fix typo in `http.throttler.catchup.interval.millis` configuration property
-   Change default polling interval to 60s and 30s when catching up

### v0.7.5 (05/27/2020)
-   Fix https://github.com/castorm/kafka-connect-http/issues/22: build fails with jdk 11
-   Simplify `SchemedKvSourceRecordMapper` by removing irrelevant configuration
-   Add log INFO with offset and results per poll iteration 

### v0.7.4 (05/26/2020)
-   Avoid storing default timestamp in `Offset` to better manage the scenario in which connector config goes from 
    not specifying `http.response.record.timestamp.pointer` to specifying one
-   Remove `BytesKvSourceRecordMapper` before it gets actually used for considering it redundant
-   `SchemedKvSourceRecordMapper` to produce a value with key and timestamp
-   Implement a `SourceRecordSorter` to enable support of HTTP resources with reverse order of records

### v0.7.3 (05/25/2020)
-   Provide `BytesKvSourceRecordMapper` to avoid both the envelope wrapping key and value and the escaped encoding of the json document 

### v0.7.2 (05/25/2020)
-   Provide `StringKvSourceRecordMapper` to avoid the envelope wrapping key and value

### v0.7.1 (05/24/2020)
-   `timestamp_iso` key in `Offset` renamed to plain `timestamp`
-   `key` added to `Offset`
-   `JacksonKvRecordHttpResponseParser` to generate a consistent `UUID` as key when key is missing
-   `AdaptableIntervalThrottler` to skip throttling on first request
-   `SimpleKvSourceRecordMapper` to support custom key/value property names 

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
