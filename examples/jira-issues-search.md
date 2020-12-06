## Jira Issues Search API
Documentation: [Search for issues using JQL (GET)](https://developer.atlassian.com/cloud/jira/platform/rest/v3/#api-rest-api-3-search-get)

### Sample Request
```bash
curl --request GET \
  --user 'email@example.com:<api_token>' \
  --header 'Accept: application/json' \
  --url 'http://domain/rest/api/3/search?jql=project%20%3D%20HSP'
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
    "name": "sample-search-issues.jira.http.source",
    "config": {
        "connector.class": "com.github.castorm.kafka.connect.http.HttpSourceConnector",
        "tasks.max": "1",
        "http.offset.initial": "timestamp=2020-05-08T07:55:44Z",
        "http.request.url": "https://your-host-here/rest/api/2/search",
        "http.request.headers": "Accept: application/json",
        "http.request.params": "jql=updated>=\"${offset.timestamp?datetime.iso?string['yyyy/MM/dd HH:mm']}\" ORDER BY updated ASC&maxResults=100",
        "http.auth.type": "Basic",
        "http.auth.user": "username",
        "http.auth.password": "password",
        "http.response.list.pointer": "/issues",
        "http.response.record.offset.pointer": "key=/id, timestamp=/fields/updated",
        "http.timer.interval.millis": "30000",
        "http.timer.catchup.interval.millis": "1000",
        "kafka.topic": "topic"
    }
}
```
