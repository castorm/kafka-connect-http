# Change Log
The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## v0.3.5 (05/08/2020)
-  Support for response item timestamp parsing via [DateTimeFormatter](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html) with optional TimeZone
-  Support for response item timestamp parsing via [Natty](http://natty.joestelmach.com/) with optional TimeZone
-  Timestamp stored in offset map as an ISO8601 string
-  Offset pointer configuration unified into a single property containing a comma separated list of key,value pairs
-  Basic OkHttpClient logging enabled

## v0.2 (05/04/2020)
-  Support for initial offsets
-  Default values for item offsets

## v0.1-alpha (05/04/2020)
-  Initial version
