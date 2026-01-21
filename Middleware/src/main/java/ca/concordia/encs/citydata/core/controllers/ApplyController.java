package ca.concordia.encs.citydata.core.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.datastores.InMemoryDataStore;
import ca.concordia.encs.citydata.producers.ExceptionProducer;
import ca.concordia.encs.citydata.runners.SequentialRunner;

/**
 * This class manages all requests sent to the /apply route
 * 
 * @author Gabriel C. Ullmann
 * @since 2024-12-01
 * 
 * Last Update: Added GetMapping for sync and async endpoints for web access
 * Author: Sikandar Ejaz
 * Date: 2025-12-09
 */

@RestController
@RequestMapping("/apply")
public class ApplyController {

	@RequestMapping(value = "/sync", method = RequestMethod.POST)
	public ResponseEntity<String> sync(@RequestBody String steps) {
		UUID runnerId = null;
		String errorMessage = "";
		HttpStatus responseCode = HttpStatus.OK;

		try {
			final JsonObject stepsObject = JsonParser.parseString(steps).getAsJsonObject();
			final SequentialRunner deckard = new SequentialRunner(stepsObject);
			runnerId = deckard.getId();
			final Thread runnerTask = new Thread() {
				public void run() {
					try {
						deckard.runSteps();
						while (!deckard.isDone()) {
							System.out.println("Busy waiting!");
						}
					} catch (Exception e) {
						deckard.setAsDone();
						final InMemoryDataStore store = InMemoryDataStore.getInstance();
						store.set(deckard.getId(), new ExceptionProducer(e));
					}
				}
			};
			runnerTask.start();
			runnerTask.join();
		} catch (IllegalStateException | JsonParseException e) {
			final String detailedMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			errorMessage = "Your query is not a valid JSON file. Details: " + detailedMessage;
			responseCode = HttpStatus.BAD_REQUEST;
		} catch (Exception e) {
			errorMessage = "An error occurred while processing your query. Details: " + e.getClass().getName() + ": "
					+ e.getMessage();
			responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		// if there are execution errors, return an error message
		if (responseCode.isError()) {
			return ResponseEntity.status(responseCode).body(errorMessage);
		}

		// else, return the data
		final InMemoryDataStore store = InMemoryDataStore.getInstance();
		final IProducer<?> resultProducer = store.get(runnerId);

		// if the thread, which cannot throw exceptions, produces an ExceptionProducer,
		// return an error code
		if (resultProducer.getClass() == ExceptionProducer.class) {
			responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
			return ResponseEntity.status(responseCode).body(resultProducer.toString());
		}

		return ResponseEntity.status(responseCode).body(resultProducer.toString());
	}

	@RequestMapping(value = "/async", method = RequestMethod.POST)
	public ResponseEntity<String> async(@RequestBody String steps) {
		String runnerId = "";
		String errorMessage = "";
		HttpStatus responseCode = HttpStatus.OK;

		try {
			JsonObject stepsObject = JsonParser.parseString(steps).getAsJsonObject();
			SequentialRunner deckard = new SequentialRunner(stepsObject);
			runnerId = deckard.getMetadata("id").toString();
			deckard.runSteps();
		} catch (IllegalStateException | JsonParseException e) {
			String detailedMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			errorMessage = "Your query is not a valid JSON file. Details: " + detailedMessage;
			responseCode = HttpStatus.BAD_REQUEST;
		} catch (Exception e) {
			errorMessage = "An error occurred while processing your query. Details: " + e.getClass().getName() + ": "
					+ e.getMessage();
			responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		// if there are execution errors, return an error message
		if (responseCode.isError()) {
			return ResponseEntity.status(responseCode).body(errorMessage);
		}

		return ResponseEntity.status(responseCode)
				.body("Hello! The runner " + runnerId
						+ " is currently working on your request. Please make a GET request to /apply/async/ "
						+ runnerId + " to retrieve request results.");
	}

	@RequestMapping(value = "/async/{runnerId}", method = RequestMethod.GET)
	public ResponseEntity<String> asyncId(@PathVariable("runnerId") String runnerIdStr) {
		try {
			UUID runnerId = UUID.fromString(runnerIdStr);
			InMemoryDataStore store = InMemoryDataStore.getInstance();
			IProducer<?> storeResult = store.get(runnerId);

			if (storeResult != null) {
				return ResponseEntity.status(HttpStatus.OK).body(storeResult.toString());
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("Sorry, your request result is not ready yet. Please try again later.");
			}
		} catch (IllegalArgumentException e) {
			// Handle case where the provided ID is not a valid UUID
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Invalid runner ID format. Please provide a valid UUID.");
		}
	}

	@GetMapping("/sync")
	public ResponseEntity<String> syncForm() {
		String html = """
				        <!DOCTYPE html>
				        <html>
				        <head>
				            <title>Sync Runner</title>
				            <style>
				                body { font-family: Arial, sans-serif; margin: 20px; }
				                label { font-weight: bold; }
				                textarea, input { width: 100%; padding: 8px; margin: 5px 0; }
				                button { padding: 10px 20px; background: #007bff; color: white; border: none; cursor: pointer; }
				                button:hover { background: #0056b3; }
				                #result { margin-top: 20px; padding: 10px; background: #f5f5f5; border: 1px solid #ddd; }
				                .error { background: #ffebee; border-color: #f44336; }
				                .success { background: #e8f5e9; border-color: #4caf50; }
				            </style>
				        </head>
				        <body>
				            <h2>Sync Runner</h2>

				            <div>
				                <label for="token">Bearer Token:</label>
				                <input type="text" id="token" placeholder="Enter your authentication token">
				            </div>

				            <form id="form">
				                <label for="steps">JSON Steps:</label>
				                <textarea id="steps" rows="10" placeholder="Enter JSON here">
				{
				  "step1": "value1"
				}
				                </textarea>
				                <button type="submit">Run Sync</button>
				            </form>

				            <div id="result"></div>

				            <script>
				                document.getElementById('form').addEventListener('submit', async (e) => {
				                    e.preventDefault();

				                    const steps = document.getElementById('steps').value;
				                    const token = document.getElementById('token').value.trim();
				                    const resultDiv = document.getElementById('result');

				                    if (!token) {
				                        resultDiv.className = 'error';
				                        resultDiv.innerHTML = '<h3>Error:</h3><pre>Please enter a Bearer token</pre>';
				                        return;
				                    }

				                    try {
				                        const response = await fetch('/apply/sync', {
				                            method: 'POST',
				                            headers: {
				                                'Content-Type': 'application/json',
				                                'Authorization': 'Bearer ' + token
				                            },
				                            body: steps
				                        });

				                        const data = await response.text();

				                        if (response.ok) {
				                            resultDiv.className = 'success';
				                            resultDiv.innerHTML = '<h3>Success:</h3><pre>' + data + '</pre>';
				                        } else {
				                            resultDiv.className = 'error';
				                            resultDiv.innerHTML = '<h3>Error (' + response.status + '):</h3><pre>' + data + '</pre>';
				                        }
				                    } catch (error) {
				                        resultDiv.className = 'error';
				                        resultDiv.innerHTML = '<h3>Error:</h3><pre>' + error.message + '</pre>';
				                    }
				                });
				            </script>
				        </body>
				        </html>
				        """;

		return ResponseEntity.ok().header("Content-Type", "text/html").body(html);
	}

	@GetMapping("/async")
	public ResponseEntity<String> asyncForm() {
		String html = """
				            <!DOCTYPE html>
				            <html>
				            <head>
				                <title>Async Runner</title>
				                <style>
				                    body { font-family: Arial, sans-serif; margin: 20px; }
				                    label { font-weight: bold; }
				                    textarea, input { width: 100%; padding: 8px; margin: 5px 0; }
				                    button { padding: 10px 20px; background: #007bff; color: white; border: none; cursor: pointer; margin: 5px; }
				                    button:hover { background: #0056b3; }
				                    #result { margin-top: 20px; padding: 10px; background: #f5f5f5; border: 1px solid #ddd; }
				                    .error { background: #ffebee; border-color: #f44336; }
				                    .success { background: #e8f5e9; border-color: #4caf50; }
				                    .section { margin-bottom: 30px; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }
				                </style>
				            </head>
				            <body>
				                <h2>Async Runner</h2>

				                <div class="section">
				                    <h3>Start New Async Operation</h3>
				                    <div>
				                        <label for="token">Bearer Token:</label>
				                        <input type="text" id="token" placeholder="Enter your authentication token">
				                    </div>

				                    <form id="form">
				                        <label for="steps">JSON Steps:</label>
				                        <textarea id="steps" rows="10" placeholder="Enter JSON here">
				{
				  "step1": "value1"
				}
				                        </textarea>
				                        <button type="submit">Run Async</button>
				                    </form>
				                </div>

				                <div class="section">
				                    <h3>Check Existing Runner Status</h3>
				                    <div>
				                        <label for="checkToken">Bearer Token:</label>
				                        <input type="text" id="checkToken" placeholder="Enter your authentication token">
				                    </div>
				                    <div>
				                        <label for="runnerId">Runner ID:</label>
				                        <input type="text" id="runnerId" placeholder="Enter Runner ID (UUID)">
				                    </div>
				                    <button type="button" id="checkButton">Check Status</button>
				                </div>

				                <div id="result"></div>

				                <script>
				                    // Start new async operation
				                    document.getElementById('form').addEventListener('submit', async (e) => {
				                        e.preventDefault();

				                        const steps = document.getElementById('steps').value;
				                        const token = document.getElementById('token').value.trim();
				                        const resultDiv = document.getElementById('result');

				                        if (!token) {
				                            resultDiv.className = 'error';
				                            resultDiv.innerHTML = '<h3>Error:</h3><pre>Please enter a Bearer token</pre>';
				                            return;
				                        }

				                        try {
				                            const response = await fetch('/apply/async', {
				                                method: 'POST',
				                                headers: {
				                                    'Content-Type': 'application/json',
				                                    'Authorization': 'Bearer ' + token
				                                },
				                                body: steps
				                            });

				                            const data = await response.text();

				                            if (response.ok) {
				                                resultDiv.className = 'success';
				                                resultDiv.innerHTML = '<h3>Success:</h3><pre>' + data + '</pre>';

				                                // Extract runner ID from response and populate the check section
				                                const match = data.match(/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/i);
				                                if (match) {
				                                    document.getElementById('runnerId').value = match[0];
				                                    document.getElementById('checkToken').value = token;
				                                    document.getElementById('checkButton').scrollIntoView({ behavior: 'smooth' });
				                                }
				                            } else {
				                                resultDiv.className = 'error';
				                                resultDiv.innerHTML = '<h3>Error (' + response.status + '):</h3><pre>' + data + '</pre>';
				                            }
				                        } catch (error) {
				                            resultDiv.className = 'error';
				                            resultDiv.innerHTML = '<h3>Error:</h3><pre>' + error.message + '</pre>';
				                        }
				                    });

				                    // Check existing runner status
				                    document.getElementById('checkButton').addEventListener('click', async () => {
				                        const runnerId = document.getElementById('runnerId').value.trim();
				                        const token = document.getElementById('checkToken').value.trim();
				                        const resultDiv = document.getElementById('result');

				                        if (!token) {
				                            resultDiv.className = 'error';
				                            resultDiv.innerHTML = '<h3>Error:</h3><pre>Please enter a Bearer token</pre>';
				                            return;
				                        }

				                        if (!runnerId) {
				                            resultDiv.className = 'error';
				                            resultDiv.innerHTML = '<h3>Error:</h3><pre>Please enter a Runner ID</pre>';
				                            return;
				                        }

				                        try {
				                            const response = await fetch('/apply/async/' + runnerId, {
				                                method: 'GET',
				                                headers: {
				                                    'Authorization': 'Bearer ' + token
				                                }
				                            });

				                            const data = await response.text();

				                            if (response.ok) {
				                                resultDiv.className = 'success';
				                                resultDiv.innerHTML = '<h3>Result:</h3><pre>' + data + '</pre>';
				                            } else if (response.status === 404) {
				                                resultDiv.className = 'error';
				                                resultDiv.innerHTML = '<h3>Not Ready Yet:</h3><pre>' + data + '</pre><p><em>Tip: The operation might still be running. Try again in a few moments.</em></p>';
				                            } else {
				                                resultDiv.className = 'error';
				                                resultDiv.innerHTML = '<h3>Error (' + response.status + '):</h3><pre>' + data + '</pre>';
				                            }
				                        } catch (error) {
				                            resultDiv.className = 'error';
				                            resultDiv.innerHTML = '<h3>Error:</h3><pre>' + error.message + '</pre>';
				                        }
				                    });
				                </script>
				            </body>
				            </html>
				            """;

		return ResponseEntity.ok().header("Content-Type", "text/html").body(html);
	}
}