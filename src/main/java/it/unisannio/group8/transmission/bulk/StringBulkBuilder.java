package it.unisannio.group8.transmission.bulk;

import java.util.Collection;
import java.util.Iterator;

public class StringBulkBuilder implements BulkBuilder<String> {
    private final StringBuilder builder = new StringBuilder();
    private final String separator;

    public StringBulkBuilder(String separator) {
        this.separator = separator;
    }

    @Override
    public String build(Collection<byte[]> payloads) {
        if (payloads.isEmpty())
            return null;

        // Actual build
        Iterator<byte[]> iterator = payloads.iterator();
        String str = new String(iterator.next());
        builder.append(str);

        while (iterator.hasNext()) {
            str = new String(iterator.next());
            builder.append(separator).append(str);
        }

        // Reset builder before returning the result
        String result = builder.toString();
        builder.setLength(0);
        return result;
    }
}
