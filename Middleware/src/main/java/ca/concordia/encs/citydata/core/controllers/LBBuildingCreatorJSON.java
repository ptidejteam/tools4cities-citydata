package ca.concordia.encs.citydata.core.controllers;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.concordia.ngci.tools4cities.metamenth.PythonEntryServer;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.IPythonEntryPoint;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.structure.IBuilding;

@RestController
@RequestMapping("/api/building")
public class LBBuildingCreatorJSON {

	private final ObjectMapper mapper = new ObjectMapper();

	@PostMapping("/create")
	public ResponseEntity<?> createBuilding(@RequestBody String buildingJson) {
		try {
			// Parse the JSON from the request
			JsonNode rootNode = mapper.readTree(buildingJson);

			// Initialize the Bridge server
			PythonEntryServer entryServer = new PythonEntryServer();
			IPythonEntryPoint pythonEntryPoint = (IPythonEntryPoint) entryServer.getGatewayServer()
					.getPythonServerEntryPoint(new Class[] { IPythonEntryPoint.class });

			IBuilding building = entryServer.getPythonObjectCreator().createBuildingFromJson(pythonEntryPoint,
					rootNode);

			// Convert the returned building to JSON (you may need a custom serializer)
			String buildingJsonResponse = mapper.writeValueAsString(building);

			return ResponseEntity.ok(buildingJsonResponse);

		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON input");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create building");
		}
	}

}
