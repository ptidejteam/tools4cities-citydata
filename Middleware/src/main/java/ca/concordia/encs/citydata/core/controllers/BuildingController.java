package ca.concordia.encs.citydata.core.controllers;

import java.util.HashMap;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.concordia.ngci.tools4cities.metamenth.PythonEntryServer;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes.IAddress;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes.IBinaryMeasure;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.measureinstruments.IMeter;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.measureinstruments.IWeatherData;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.measureinstruments.IWeatherStation;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.structure.IBuilding;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.structure.IFloor;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.structure.IRoom;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.subsystems.IBuildingControlSystem;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.transducers.IAbstractTransducer;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.transducers.ISensor;

@RestController
@RequestMapping("/api/building")
public class BuildingController {

	private PythonEntryServer pythonEntryServer;
	private ObjectMapper objectMapper = new ObjectMapper();

	@PostMapping("/create")
	public ResponseEntity<String> createBuilding(@RequestBody String jsonString) {
		try {
			// Initialize Py4J gateway only when needed
			if (pythonEntryServer == null) {
				pythonEntryServer = new PythonEntryServer();
			}

			// Create building from JSON
			pythonEntryServer.createBuildingFromJson(jsonString);
			IBuilding building = pythonEntryServer.getBuilding();

			if (building == null) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("{\"error\": \"Failed to create building\"}");
			}

			// Convert building to JSON response
			String buildingJson = convertBuildingToJson(building);

			return ResponseEntity.ok(buildingJson);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + e.getMessage() + "\"}");
		}
	}

	private String convertBuildingToJson(final IBuilding building) {
		try {
			final ObjectNode rootNode = objectMapper.createObjectNode();
			final ObjectNode buildingNode = objectMapper.createObjectNode();

			// Basic building info
			// buildingNode.put("uid", building.getUID());
			buildingNode.put("yearBuilt", building.getConstructionYear());
			buildingNode.put("type", building.getBuildingType());

			// Height
			if (building.getHeight() != null) {
				final ObjectNode heightNode = objectMapper.createObjectNode();
				heightNode.put("unit", building.getHeight().getMeasurementUnit());
				heightNode.put("value", ((IBinaryMeasure) building.getHeight()).getValue());
				buildingNode.set("height", heightNode);
			}

			// Floor area
			if (building.getFloorArea() != null) {
				final ObjectNode floorAreaNode = objectMapper.createObjectNode();
				floorAreaNode.put("unit", building.getFloorArea().getMeasurementUnit());
				floorAreaNode.put("value", ((IBinaryMeasure) building.getFloorArea()).getValue());
				buildingNode.set("floorArea", floorAreaNode);
			}

			// Address
			if (building.getAddress() != null) {
				final IAddress address = building.getAddress();
				final ObjectNode addressNode = objectMapper.createObjectNode();
				addressNode.put("city", address.getCity());
				addressNode.put("street", address.getStreet());
				addressNode.put("province", address.getState());
				addressNode.put("postalCode", address.getZipCode());
				addressNode.put("country", address.getCountry());

				if (address.getGeocoordinates() != null) {
					final ObjectNode coordsNode = objectMapper.createObjectNode();
					coordsNode.put("x", address.getGeocoordinates().getLatitude());
					coordsNode.put("y", address.getGeocoordinates().getLongitude());
					addressNode.set("coordinates", coordsNode);
				}
				buildingNode.set("address", addressNode);
			}

			// Floors
			List<IFloor> floors = building.getFloors(new HashMap<>());
			if (floors != null && !floors.isEmpty()) {
				ArrayNode floorsArray = objectMapper.createArrayNode();
				for (IFloor floor : floors) {
					final ObjectNode floorNode = objectMapper.createObjectNode();
					// floorNode.put("uid", floor.getUID());
					floorNode.put("number", floor.getNumber().toString());
					floorNode.put("type", floor.getFloorType());
					floorNode.put("description", floor.getDescription());

					if (floor.getHeight() != null) {
						final ObjectNode sizeNode = objectMapper.createObjectNode();
						sizeNode.put("unit", floor.getHeight().getMeasurementUnit());
						sizeNode.put("value", ((IBinaryMeasure) floor.getHeight()).getValue());
						floorNode.set("size", sizeNode);
					}

					// Rooms
					final List<IRoom> rooms = floor.getRooms(new HashMap<>());
					if (rooms != null && !rooms.isEmpty()) {
						final ArrayNode roomsArray = objectMapper.createArrayNode();
						for (final IRoom room : rooms) {
							final ObjectNode roomNode = objectMapper.createObjectNode();
							// roomNode.put("uid", room.getUID());
							roomNode.put("name", room.getName());
							roomNode.put("type", room.getRoomType());

							if (room.getArea() != null) {
								ObjectNode roomSizeNode = objectMapper.createObjectNode();
								roomSizeNode.put("unit", room.getArea().getMeasurementUnit());
								roomSizeNode.put("value", ((IBinaryMeasure) room.getArea()).getValue());
								roomNode.set("size", roomSizeNode);
							}

							// TODO Do not hardcode this particular sensor name, iterate through all sensors
							// Get sensor by name (TMP 01 from the JSON example)
							final ArrayNode sensorsArray = objectMapper.createArrayNode();
							final IAbstractTransducer transducer = room.getTransducer("TMP 01");
							if (transducer != null && transducer instanceof ISensor) {
								ISensor sensor = (ISensor) transducer;
								final ObjectNode sensorNode = objectMapper.createObjectNode();
								// sensorNode.put("id", sensor.getUID());
								sensorNode.put("measure", sensor.getMeasure());
								sensorNode.put("unit", sensor.getUnit());
								sensorNode.put("type", sensor.getMeasureType());
								sensorNode.put("frequency", sensor.getDataFrequency());

								final List<Object> sensorDataList = sensor.getData(new HashMap<>());
								if (sensorDataList != null && !sensorDataList.isEmpty()) {
									final ArrayNode dataArray = objectMapper.createArrayNode();
									for (final Object dataObj : sensorDataList) {
										// Create the missing IDataMeasure interface
										// dataArray.add(((IDataMeasure) dataObj).getValue());
										dataArray.add(dataObj.toString());
									}
									sensorNode.set("data", dataArray);
								}

								sensorsArray.add(sensorNode);
							}

							if (sensorsArray.size() > 0) {
								roomNode.set("sensors", sensorsArray);
							}

							roomsArray.add(roomNode);
						}
						floorNode.set("rooms", roomsArray);
					}

					floorsArray.add(floorNode);
				}
				buildingNode.set("floors", floorsArray);
			}

			// Meters
			final List<IMeter> meters = building.getMeters(new HashMap<>());
			if (meters != null && !meters.isEmpty()) {
				final ArrayNode metersArray = objectMapper.createArrayNode();
				for (final IMeter meter : meters) {
					final ObjectNode meterNode = objectMapper.createObjectNode();
					// TODO Create the missing getID() method (different from getUID())
					// meterNode.put("uid", meter.getID());
					//meterNode.put("id", meter.getDeviceID());
					meterNode.put("type", meter.getMeterType());
					meterNode.put("unit", meter.getMeasurementUnit());
					meterNode.put("mode", meter.getMeasureMode());
					meterNode.put("frequency", meter.getMeasurementFrequency());
					metersArray.add(meterNode);
				}
				buildingNode.set("meters", metersArray);
			}

			// Weather Station
			// Do NOT hardcode weather station name but iterate through all stations
			final IWeatherStation weatherStation = building.getWeatherStation("LB WS");
			if (weatherStation != null) {
				final ObjectNode wsNode = objectMapper.createObjectNode();
				wsNode.put("id", weatherStation.getName());

				final List<IWeatherData> weatherDataList = weatherStation.getWeatherData(new HashMap<>());
				if (weatherDataList != null && !weatherDataList.isEmpty()) {
					final ArrayNode dataArray = objectMapper.createArrayNode();
					for (final IWeatherData data : weatherDataList) {
						ObjectNode dataNode = objectMapper.createObjectNode();
						if (data.getData() != null) {
							dataNode.put("measure", data.getData().getMeasureType());
							dataNode.put("value", ((IBinaryMeasure) data.getData()).getValue());
						}
						dataArray.add(dataNode);
					}
					wsNode.set("data", dataArray);
				}
				buildingNode.set("weatherStation", wsNode);
			}

			// Control Systems
			final List<IBuildingControlSystem> controlSystems = building.getBuildingControlSystem();
			if (controlSystems != null && !controlSystems.isEmpty()) {
				final ArrayNode csArray = objectMapper.createArrayNode();
				for (final IBuildingControlSystem cs : controlSystems) {
					final ObjectNode csNode = objectMapper.createObjectNode();
					csNode.put("name", cs.getName());
					csNode.put("type", cs.getHvacSystem() != null ? "HVAC" : "UNKNOWN");
					csArray.add(csNode);
				}
				buildingNode.set("controlSystems", csArray);
			}

			rootNode.set("building", buildingNode);
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		} catch (final Exception e) {
			e.printStackTrace();
			return "{\"error\": \"Failed to convert building to JSON: " + e.getMessage() + "\"}";
		}
	}
}