## Jira API Search Issues
Documentation: [Search for issues using JQL (GET)](https://developer.atlassian.com/cloud/jira/platform/rest/v3/#api-rest-api-3-search-get)

### Sample Request
```bash
curl --request GET \
  --url '/rest/api/3/search?jql=project%20%3D%20HSP' \
  --user 'email@example.com:<api_token>' \
  --header 'Accept: application/json'
```

### Sample Response
```json
{
  "expand": "names,schema",
  "startAt": 0,
  "maxResults": 50,
  "total": 1,
  "issues": [
    {
      "id": "10002",
      "key": "ED-1",
      "fields": {
        "updated": "2020-05-08T07:55:44.099+0000",
        ...
      },
      ...
    }
  ]
}
```

### Recommended approach: CDC
We can leverage the fact that issues contain an `updated` property which is monotonically increasing, and the fact that 
the API allows for the data to be **filtered** and **ordered** based on `updated` property.

Based on this, we could prepare a first query with the following query parameters:
`jql=updated>="2020-05-08 07:55" ORDER BY updated ASC&maxResults=100`

And based on the results we would be updating the `updated` filter for subsequent queries.

#### Sample Configuration
```json
{
    "name": "sample-search-issues.jira.source",
    "config": {
        "connector.class": "com.github.castorm.kafka.connect.http.HttpSourceConnector",
        "tasks.max": "1",
        "http.offset.initial": "timestamp_iso=2020-05-08T07:55:44Z",
        "http.request.url": "https://your-host-here/rest/api/2/search",
        "http.request.headers": "Authorization: Basic TBD, Accept: application/json",
        "http.request.params": "jql=updated>=\"${timestamp_iso?datetime.iso?string['yyyy/MM/dd HH:mm']}\" ORDER BY updated ASC&maxResults=100",
        "http.request.template.factory": "com.github.castorm.kafka.connect.http.request.template.freemarker.FreeMarkerTemplateFactory",    
        "http.response.filter.factory": "com.github.castorm.kafka.connect.http.response.OffsetTimestampFilterFactory",
        "http.response.items.pointer": "/issues",
        "http.response.item.key.pointer": "/id",
        "http.response.item.value.pointer": "/",
        "http.response.item.timestamp.pointer": "/fields/updated",
        "http.throttle.interval.millis": "30000",
        "kafka.topic": "topic"
    }
}
```
