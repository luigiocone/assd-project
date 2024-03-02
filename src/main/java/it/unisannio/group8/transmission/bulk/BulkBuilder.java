package it.unisannio.group8.transmission.bulk;

import java.util.Collection;

public interface BulkBuilder<T> {
    T build(Collection<byte[]> payloads);
}
