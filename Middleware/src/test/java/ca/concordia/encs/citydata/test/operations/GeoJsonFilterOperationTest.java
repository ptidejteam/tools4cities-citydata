package ca.concordia.encs.citydata.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Unit tests for GeoJsonFilterOperation.
 */
public class GeoJsonFilterOperationTest {

	@Test
	void testFilterByRadiusUsingFeatureCentroids() {
		final GeoJsonFilterOperation operation = new GeoJsonFilterOperation();
		operation.setCenterLatitude(45.0);
		operation.setCenterLongitude(-73.0);
		operation.setRadiusMeters(500.0);

		final ArrayList<String> input = new ArrayList<>();
		input.add(buildFeatureCollection(
				buildSquareFeature("inside", -73.0005, 44.9995, -72.9995, 45.0005),
				buildSquareFeature("outside", -73.2005, 45.1995, -73.1995, 45.2005)
		).toString());

		final ArrayList<String> output = operation.apply(input);
		assertEquals(1, output.size());

		final JsonObject filteredCollection = JsonParser.parseString(output.getFirst()).getAsJsonObject();
		final JsonArray features = filteredCollection.getAsJsonArray("features");
		assertEquals(1, features.size());
		assertEquals("inside",
				features.get(0).getAsJsonObject().getAsJsonObject("properties").get("id").getAsString());
	}

	@Test
	void testMissingParametersThrowsError() {
		final GeoJsonFilterOperation operation = new GeoJsonFilterOperation();
		operation.setCenterLatitude(45.0);
		operation.setCenterLongitude(-73.0);
		// Missing radiusMeters

		final ArrayList<String> input = new ArrayList<>();
		input.add(buildFeatureCollection(buildSquareFeature("inside", -73.0005, 44.9995, -72.9995, 45.0005)).toString());

		assertThrows(IllegalArgumentException.class, () -> operation.apply(input));
	}

	private JsonObject buildFeatureCollection(JsonObject... features) {
		final JsonObject collection = new JsonObject();
		collection.addProperty("type", "FeatureCollection");
		final JsonArray featureArray = new JsonArray();
		for (JsonObject feature : features) {
			featureArray.add(feature);
		}
		collection.add("features", featureArray);
		return collection;
	}

	private JsonObject buildSquareFeature(String id, double minLon, double minLat, double maxLon, double maxLat) {
		final JsonObject feature = new JsonObject();
		feature.addProperty("type", "Feature");

		final JsonObject properties = new JsonObject();
		properties.addProperty("id", id);
		feature.add("properties", properties);

		final JsonObject geometry = new JsonObject();
		geometry.addProperty("type", "Polygon");

		final JsonArray coordinates = new JsonArray();
		final JsonArray ring = new JsonArray();

		ring.add(point(minLon, minLat));
		ring.add(point(minLon, maxLat));
		ring.add(point(maxLon, maxLat));
		ring.add(point(maxLon, minLat));
		ring.add(point(minLon, minLat));

		coordinates.add(ring);
		geometry.add("coordinates", coordinates);
		feature.add("geometry", geometry);
		return feature;
	}

	private JsonArray point(double lon, double lat) {
		final JsonArray point = new JsonArray();
		point.add(lon);
		point.add(lat);
		return point;
	}
}
