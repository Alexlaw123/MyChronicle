package com.openlattice.chronicle.serialization;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;
import com.openlattice.chronicle.android.ChronicleData;
import com.openlattice.chronicle.android.ChronicleSample;
import com.openlattice.chronicle.android.LegacyChronicleData;
import com.openlattice.chronicle.util.RetrofitBuilders;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonSerializer {
    public static final ObjectMapper mapper = RetrofitBuilders.mapper;
    static {
        mapper.registerModule(new KotlinModule());
    }

    public static final byte[] serializeQueueEntry(List<ChronicleSample> queueData) {
        try {
            return mapper.writeValueAsBytes(queueData);
        } catch (JsonProcessingException e) {
            Log.e(JsonSerializer.class.getName(), "Unable to serialize queue entry. " + queueData.toString());
            return new byte[0];
        }
    }

    public static LegacyChronicleData deserializeLegacyQueueEntry(byte[] bytes) {
        try {
            return new LegacyChronicleData(mapper.readValue(bytes, new TypeReference<List<SetMultimap<UUID, Object>>>() {
            }));
        } catch (IOException e) {
            Log.e(JsonSerializer.class.getName(), "Unable to deserialize queue entry " + new String(bytes));
            return new LegacyChronicleData(ImmutableList.of());
        }
    }

    public static ChronicleData deserializeQueueEntry(byte[] bytes) throws IOException {
        return mapper.readValue(bytes, ChronicleData.class);
    }

    public static String serializePropertyTypeIds(Map<FullQualifiedName, UUID> propertyTypeIds) {
        try {
            return mapper.writeValueAsString(propertyTypeIds);
        } catch (JsonProcessingException e) {
            Log.e(JsonSerializer.class.getName(), "Unable to serialize property type ids.");
            return "";
        }
    }

    public static Map<FullQualifiedName, UUID> deserializePropertyTypeIds(String json) {
        Log.e("jsondebug",json);

        String j = "{\"ol.user\":\"188b754c-bd92-4f4a-8d01-a57fe94adc6d\",\"ol.rrule\":\"2d7e9eaf-8404-42b6-ba98-4287eab4901d\",\"ol.name\":\"ddb5d841-4c82-407c-8fcb-58f04ffc20fe\",\"ol.recordtype\":\"285e6bfc-2a73-49ae-8cb2-b112244ed85d\",\"general.fullname\":\"70d2ff1c-2450-4a47-a954-a7641b7399ae\",\"ol.datelogged\":\"e90a306c-ee37-4cd1-8a0e-71ad5a180340\",\"ol.active\":\"54fa6acb-bd3e-4849-85b7-4eadaf33e112\",\"ol.title\":\"f0373614-c607-43b2-99b0-1cd32ff4f921\",\"location.latitude\":\"06083695-aebe-4a56-9b98-da6013e93a5e\",\"general.Duration\":\"c106ee75-f18e-48ed-bc85-b75702bfe802\",\"location.altitude\":\"90203091-5efd-40c4-9372-9782746cd427\",\"general.EndTime\":\"00e5c55f-f1ef-4538-8d48-c08d5bcfe4c7\",\"general.stringid\":\"ee3a7573-aa70-4afb-814d-3fad27cda988\",\"ol.datetimestart\":\"92a6a5c5-b4f1-40ce-ace9-be232acdce2a\",\"ol.timezone\":\"071ba832-035f-4b04-99e4-d11dc4fbe0e8\",\"location.longitude\":\"e8f9026a-2494-4749-84bb-1499cb7f215c\"}";

        try {
            return mapper.readValue(j, new TypeReference<Map<FullQualifiedName, UUID>>() {
            });
        } catch (IOException e) {
            Log.e(JsonSerializer.class.getName(), "Unable to deserialize property type ids.");
            return ImmutableMap.of();
        }
    }
}
