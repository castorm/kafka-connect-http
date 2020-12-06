## Elasticsearch Search API
Documentation: [Search API](https://www.elastic.co/guide/en/elasticsearch/reference/7.7/search-search.html)

### Sample Request
```bash
curl --request POST \
  --header 'Content-Type: application/json' \
  --data '{"size": 100, "sort": [{"@timestamp": "asc"}], "search_after": [1589500805994]}' \
  --url 'http://domain/index_name/_search'
```

### Sample Response
```json
{
  "took":87,
  "timed_out":false,
  "_shards":{
    "total":4,
    "successful":4,
    "skipped":0,
    "failed":0
  },
  "hits":{
    "total":10836885,
    "max_score":null,
    "hits":[
      ...,
      {
        "_index":"index_name",
        "_type":"type_name",
        "_id":"Y2Q3MGM3OWQtNWVkZC00YzFjLTliMDAtZTMyMDAxZDg2MTI0",
        "_score":null,
        "_source":{
          ...,
          "@timestamp":"2020-05-15T00:00:06.008000000+00:00"
        },
        "sort":[
          1589500806008
        ]
      }
    ]
  }
}
```

### Recommended approach: CDC
Elasticsearch recommends using [search_after](https://www.elastic.co/guide/en/elasticsearch/reference/7.7/search-request-body.html#request-body-search-search-after)
for deep pagination, which fits perfectly with our CDC purposes.

All we have to do is select some properties to sort by, and those will be then used as a scrolling mechanism in between
queries.

#### Sample Configuration
```json
{
    "name": "my_index.elasticsearch.http.source",
    "config": {
        "connector.class": "com.github.castorm.kafka.connect.http.HttpSourceConnector",
        "tasks.max": "1",
        "http.offset.initial": "timestamp=2020-01-01T00:00:00Z",
        "http.request.url": "http://domain/index_name/_search",
        "http.request.method": "POST",
        "http.request.headers": "Content-Type: application/json",
        "http.request.body": "{\"size\": 100, \"sort\": [{\"@timestamp\": \"asc\"}], \"search_after\": [${offset.timestamp?datetime.iso?long}]}",
        "http.response.list.pointer": "/hits/hits",
        "http.response.record.pointer": "/_source",
        "http.response.record.offset.pointer": "key=/_id, timestamp=/sort/0",
        "http.timer.interval.millis": "30000",
        "http.timer.catchup.interval.millis": "1000",
        "kafka.topic": "topic"
    }
}
```
