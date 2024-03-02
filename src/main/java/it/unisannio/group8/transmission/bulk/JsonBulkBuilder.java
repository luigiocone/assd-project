package it.unisannio.group8.transmission.bulk;

import com.google.gson.Gson;
import it.unisannio.group8.model.OneLineFactory;

import java.util.ArrayList;
import java.util.Collection;

public class JsonBulkBuilder implements BulkBuilder<String> {
    private final Gson gson = new Gson();
    private final OneLineFactory factory;

    public JsonBulkBuilder(OneLineFactory factory) {
        this.factory = factory;
    }

    @Override
    public String build(Collection<byte[]> payloads) {
        if (payloads.isEmpty())
            return null;

        ArrayList<Object> samples = new ArrayList<>(payloads.size());
        for (byte[] pl: payloads) {
            String str = new String(pl);
            samples.add(factory.create(str));
        }
        return gson.toJson(samples);
    }
}
