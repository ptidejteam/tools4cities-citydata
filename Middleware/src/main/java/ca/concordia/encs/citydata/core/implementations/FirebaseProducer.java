package ca.concordia.encs.citydata.core.implementations;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;

/***
 * This is the Producer class for Firebase, which fetches data from Firebase
 * @author Rushin Makwana
 * @since 2024-02-07
 */

//TODO: Need to discuss with Yann, and then probably move this back to the producers package

public non-sealed class FirebaseProducer extends AbstractProducer<JsonObject> implements IProducer<JsonObject> {

	private final String nodePath; // Path to the Firebase node to fetch data from

	public FirebaseProducer(String databaseURL, String nodePath, RequestOptions requestOptions) {
		this.nodePath = nodePath;
		// Any request options if needed

		FirebaseDatabase.getInstance().setPersistenceEnabled(true);
	}

	@Override
	public void fetch() {
		final List<JsonObject> jsonObjects = new ArrayList<>();

		final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(nodePath);

		// Adding a ValueEventListener to fetch data
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				// Retrieve JSON string from dataSnapshot
				final String result = dataSnapshot.getValue(String.class);

				if (result != null) {
					// Convert JSON string to object
					final JsonElement jsonElement = JsonParser.parseString(result);
					final JsonObject jsonObject = jsonElement.getAsJsonObject();
					jsonObjects.add(jsonObject);
					applyOperation();
					System.out.println("Data fetched from Firebase");
				} else {
					System.err.println("No data found at the specified node path.");
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				System.err.println("Error fetching data: " + databaseError.getMessage());
			}
		});
	}
}
