# Change Data Capture

### Introduction
Given the nature of the transport we are using, we can't just subscribe to changes on a HTTP resource, we have to poll for them, that is retrieving changes periodically, via multiple requests.

On the other hand, capturing changes on a given data set requires being able to **identify what has changed** since last time we polled.

In order to identify what has changed, it helps categorizing data sets in:
*   **Immutable**: Data is only _appended_, not _modified_ (an update might be just a new insert)
*   **Mutable**: Data is _appended_ but also _modified_

***

### CDC on immutable data sets
Data in an immutable data set is ordered (clustered) naturally by the timestamp at which data is appended. Depending on the data system it might even provide a monotonically increasing offset (like a sequence) which improves on the timestamp by guaranteeing uniqueness. 

Uniqueness helps us by removing the possibility of retrieving duplicates. Timestamp alone wouldn't avoid that.

Performance of this kind of read access pattern to immutable datasets is usually better as it means reading sequentially, which benefits from data locality.

***

### CDC on mutable data sets
If we were to naively use the same strategy as with immutable data sets, we'd discover that datums that were already read have been mutated without us noticing. This is why creation timestamp or a monotonically increasing offset, alone, wouldn't be enough to identify changes.

Instead, we need a way to sort the data the way it was updated. The problem is, like we said before, timestamps alone are usually not unique, hence not enough to prevent duplicates. Fortunately, there are some things we can do about it, they are covered under [Deduplication](https://github.com/castorm/kafka-connect-http/wiki/Deduplication).

Performance of this kind of read access pattern to mutable datasets is usually not as good as it means random reads. However it might still be good enough when with the aid of optimization mechanisms for traversing datasets sorted by update timestamps, e.g. indexes.

***

### Generalization of Requirements to support CDC
Below the requirements to satisfy to support CDC.

#### Data set Requirements
*   Every datum in the data set contains a notion of _unique offset/timestamp_
*   Data set can be queried from a given _unique offset/timestamp_
*   Data set can be queried sorted by such _unique offset/timestamp_

#### Connector Requirements
*   Extract the last _unique offset/timestamp_ from the HTTP response
*   Keep track of what was the last _unique offset/timestamp_ delivered to Kafka
*   Feed such last delivered _unique offset/timestamp_ into the next HTTP request
