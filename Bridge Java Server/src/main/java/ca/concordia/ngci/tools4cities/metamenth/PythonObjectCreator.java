package ca.concordia.ngci.tools4cities.metamenth;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.concordia.ngci.tools4cities.metamenth.enums.BuildingType;
import ca.concordia.ngci.tools4cities.metamenth.enums.DataMeasurementType;
import ca.concordia.ngci.tools4cities.metamenth.enums.FloorType;
import ca.concordia.ngci.tools4cities.metamenth.enums.MeasurementUnit;
import ca.concordia.ngci.tools4cities.metamenth.enums.MeterMeasureMode;
import ca.concordia.ngci.tools4cities.metamenth.enums.MeterType;
import ca.concordia.ngci.tools4cities.metamenth.enums.SensorMeasure;
import ca.concordia.ngci.tools4cities.metamenth.enums.SensorMeasureType;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.IPythonEntryPoint;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes.IAddress;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes.IBinaryMeasure;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes.IMeasure;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes.IPoint;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.measureinstruments.IMeter;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.measureinstruments.ISensorData;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.measureinstruments.IWeatherData;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.measureinstruments.IWeatherStation;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.structure.IBuilding;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.structure.IFloor;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.structure.IRoom;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.subsystems.IBuildingControlSystem;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.subsystems.IHvacSystem;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.transducers.ISensor;

/**
 * Uses create building objects by access the appropriate middleware
 * operations and producers, and MetamEnTh classes
 * @author Peter Yefi
 */

public class PythonObjectCreator {

	/**
	 * Creates a building model for the LB Building. Must use data and operations from 
	 * the middleware to create the building 
	 * @param pythonEntryPoint, Python object with method to create MetamEnTh objects
	 * @return
	 */

	private ObjectMapper objectMapper = new ObjectMapper();

	public IBuilding createLBBuilding(IPythonEntryPoint pythonEntryPoint) {

		IMeasure roomSize = pythonEntryPoint.createMeasure(MeasurementUnit.SQUARE_METERS.getValue(), 20);
		IBinaryMeasure measurement = (IBinaryMeasure) pythonEntryPoint.createMeasurement(roomSize, "Binary");
		IRoom roomOne = pythonEntryPoint.createRoom(measurement, "Room 001", "Office", "hei.ies.ies");

		ISensor sensor = pythonEntryPoint.createSensor("TMP 01", SensorMeasure.TEMPERATURE.getValue(),
				MeasurementUnit.DEGREE_CELSIUS.getValue(), SensorMeasureType.THERMO_COUPLE_TYPE_A.getValue(), 900);

		ArrayList<ISensorData> sensorData = new ArrayList<>();
		for (int index = 0; index < 10; index++) {
			sensorData.add(pythonEntryPoint.createSensorData(index + 10, null));

		}
		List<Object> sensorDataObjs = new ArrayList<>(sensorData);
		sensor.addData(sensorDataObjs);

		roomOne.addTransducer(sensor);

		IMeasure floorSize = pythonEntryPoint.createMeasure(MeasurementUnit.SQUARE_METERS.getValue(), 150);
		IBinaryMeasure floorMeasurement = (IBinaryMeasure) pythonEntryPoint.createMeasurement(floorSize, "Binary");
		IFloor floorOne = pythonEntryPoint.createFloor(measurement, 1, FloorType.REGULAR.getValue(), floorMeasurement,
				"First floor of the building", roomOne, null);

		IPoint coordinates = pythonEntryPoint.createCoordinates(45.4967765, -73.5806159);
		IAddress address = pythonEntryPoint.createAddress("Montreal", "1400 de Maisonneuve Blvd. W.", "QC", "H3G 1M8",
				"Canada", coordinates);
		IMeasure buildingHeightMeasure = pythonEntryPoint.createMeasure(MeasurementUnit.METERS.getValue(), 15);
		IBinaryMeasure buildingHeight = (IBinaryMeasure) pythonEntryPoint.createMeasurement(buildingHeightMeasure,
				"Binary");

		IMeasure floorAreaMeasure = pythonEntryPoint.createMeasure(MeasurementUnit.SQUARE_METERS.getValue(), 50591.3);
		IBinaryMeasure floorArea = (IBinaryMeasure) pythonEntryPoint.createMeasurement(floorAreaMeasure, "Binary");

		IBuilding building = pythonEntryPoint.createBuilding(1996, buildingHeight, floorArea, address,
				BuildingType.NON_COMMERCIAL.getValue(), floorOne);
		IMeter meter = pythonEntryPoint.createMeter(90, MeasurementUnit.KILOWATTS_PER_HOUR.getValue(),
				MeterType.ELECTRICITY.getValue(), MeterMeasureMode.AUTOMATIC.getValue());
		building.addMeter(meter);

		IWeatherStation weatherStation = pythonEntryPoint.createWeatherStation("LB WS");
		ArrayList<IWeatherData> weatherData = new ArrayList<>();

		for (int index = 0; index < 10; index++) {
			IMeasure weatherDataMeasure = pythonEntryPoint.createMeasure(MeasurementUnit.RELATIVE_HUMIDITY.getValue(),
					index + 40);
			IBinaryMeasure relativeHumidity = (IBinaryMeasure) pythonEntryPoint.createMeasurement(weatherDataMeasure,
					"Binary");
			relativeHumidity.setMeasureType(DataMeasurementType.RELATIVE_HUMIDITY.getValue());
			weatherData.add(pythonEntryPoint.createWeatherData(relativeHumidity, null));
		}

		weatherStation.addWeatherData(weatherData);
		building.addWeatherStation(weatherStation);

		IHvacSystem hvacSystem = pythonEntryPoint.createHvacSystem();

		IBuildingControlSystem bcs = pythonEntryPoint.createBuildingControlSystem("HVAC System", hvacSystem);
		building.addControlSystem(bcs);

		return building;
	}

	public IBuilding createBuildingFromJson(IPythonEntryPoint pythonEntryPoint, String jsonString) {
		try {
			JsonNode rootNode = objectMapper.readTree(jsonString);
			JsonNode buildingNode = rootNode.get("building");

			return parseBuildingFromJson(pythonEntryPoint, buildingNode);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates a building model from JSON file in resources
	 * @param pythonEntryPoint Python object with method to create MetamEnTh objects
	 * @param jsonFileName Name of JSON file in resources folder
	 * @return IBuilding object
	 */
	public IBuilding createBuildingFromJsonFile(IPythonEntryPoint pythonEntryPoint, String jsonFileName) {
		try {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFileName);
			if (inputStream == null) {
				throw new RuntimeException("JSON file not found: " + jsonFileName);
			}
			JsonNode rootNode = objectMapper.readTree(inputStream);
			JsonNode buildingNode = rootNode.get("building");

			return parseBuildingFromJson(pythonEntryPoint, buildingNode);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private IBuilding parseBuildingFromJson(IPythonEntryPoint pythonEntryPoint, JsonNode buildingNode) {
		try {
			// Parse address
			JsonNode addressNode = buildingNode.get("address");
			JsonNode coordsNode = addressNode.get("coordinates");
			IPoint coordinates = pythonEntryPoint.createCoordinates(coordsNode.get("x").asDouble(),
					coordsNode.get("y").asDouble());
			IAddress address = pythonEntryPoint.createAddress(addressNode.get("city").asText(),
					addressNode.get("street").asText(), addressNode.get("province").asText(),
					addressNode.get("postalCode").asText(), addressNode.get("country").asText(), coordinates);

			// Parse building height
			JsonNode heightNode = buildingNode.get("height");
			IMeasure buildingHeightMeasure = pythonEntryPoint.createMeasure(heightNode.get("unit").asText(),
					heightNode.get("value").asDouble());
			IBinaryMeasure buildingHeight = (IBinaryMeasure) pythonEntryPoint.createMeasurement(buildingHeightMeasure,
					"Binary");

			// Parse floor area
			JsonNode floorAreaNode = buildingNode.get("floorArea");
			IMeasure floorAreaMeasure = pythonEntryPoint.createMeasure(floorAreaNode.get("unit").asText(),
					floorAreaNode.get("value").asDouble());
			IBinaryMeasure floorArea = (IBinaryMeasure) pythonEntryPoint.createMeasurement(floorAreaMeasure, "Binary");

			// Parse floors
			JsonNode floorsNode = buildingNode.get("floors");
			IFloor firstFloor = null;
			for (JsonNode floorNode : floorsNode) {
				IFloor floor = parseFloor(pythonEntryPoint, floorNode);
				if (firstFloor == null) {
					firstFloor = floor;
				}
			}

			// Create building
			IBuilding building = pythonEntryPoint.createBuilding(buildingNode.get("yearBuilt").asInt(), buildingHeight,
					floorArea, address, buildingNode.get("type").asText(), firstFloor);

			// Add additional floors
			int floorIndex = 0;
			for (JsonNode floorNode : floorsNode) {
				if (floorIndex > 0) {
					IFloor floor = parseFloor(pythonEntryPoint, floorNode);
					building.addFloor(floor);
				}
				floorIndex++;
			}

			// Parse and add meters
			JsonNode metersNode = buildingNode.get("meters");
			if (metersNode != null) {
				for (JsonNode meterNode : metersNode) {
					IMeter meter = pythonEntryPoint.createMeter(meterNode.get("value").asDouble(),
							meterNode.get("unit").asText(), meterNode.get("type").asText(),
							meterNode.get("mode").asText());
					building.addMeter(meter);
				}
			}

			// Parse and add weather station
			JsonNode weatherStationNode = buildingNode.get("weatherStation");
			if (weatherStationNode != null) {
				IWeatherStation weatherStation = pythonEntryPoint
						.createWeatherStation(weatherStationNode.get("id").asText());

				ArrayList<IWeatherData> weatherDataList = new ArrayList<>();
				JsonNode weatherDataNode = weatherStationNode.get("data");
				for (JsonNode dataNode : weatherDataNode) {
					IMeasure weatherDataMeasure = pythonEntryPoint.createMeasure(dataNode.get("measure").asText(),
							dataNode.get("value").asDouble());
					IBinaryMeasure relativeHumidity = (IBinaryMeasure) pythonEntryPoint
							.createMeasurement(weatherDataMeasure, "Binary");
					relativeHumidity.setMeasureType(dataNode.get("measure").asText());
					weatherDataList.add(pythonEntryPoint.createWeatherData(relativeHumidity, null));
				}

				weatherStation.addWeatherData(weatherDataList);
				building.addWeatherStation(weatherStation);
			}

			// Parse and add control systems
			JsonNode controlSystemsNode = buildingNode.get("controlSystems");
			if (controlSystemsNode != null) {
				for (JsonNode csNode : controlSystemsNode) {
					if ("HVAC".equals(csNode.get("type").asText())) {
						IHvacSystem hvacSystem = pythonEntryPoint.createHvacSystem();
						IBuildingControlSystem bcs = pythonEntryPoint
								.createBuildingControlSystem(csNode.get("name").asText(), hvacSystem);
						building.addControlSystem(bcs);
					}
				}
			}

			return building;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private IFloor parseFloor(IPythonEntryPoint pythonEntryPoint, JsonNode floorNode) {
		try {
			// Parse floor size
			JsonNode floorSizeNode = floorNode.get("size");
			IMeasure floorSize = pythonEntryPoint.createMeasure(floorSizeNode.get("unit").asText(),
					floorSizeNode.get("value").asDouble());
			IBinaryMeasure floorMeasurement = (IBinaryMeasure) pythonEntryPoint.createMeasurement(floorSize, "Binary");

			// Parse rooms
			JsonNode roomsNode = floorNode.get("rooms");
			IRoom firstRoom = null;
			for (JsonNode roomNode : roomsNode) {
				IRoom room = parseRoom(pythonEntryPoint, roomNode);
				if (firstRoom == null) {
					firstRoom = room;
				}
			}

			// Create floor
			IFloor floor = pythonEntryPoint.createFloor(floorMeasurement, floorNode.get("number").asInt(),
					floorNode.get("type").asText(), floorMeasurement, floorNode.get("description").asText(), firstRoom,
					null);

			return floor;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private IRoom parseRoom(IPythonEntryPoint pythonEntryPoint, JsonNode roomNode) {
		try {
			// Parse room size
			JsonNode roomSizeNode = roomNode.get("size");
			IMeasure roomSize = pythonEntryPoint.createMeasure(roomSizeNode.get("unit").asText(),
					roomSizeNode.get("value").asDouble());
			IBinaryMeasure roomMeasurement = (IBinaryMeasure) pythonEntryPoint.createMeasurement(roomSize, "Binary");

			// Create room
			IRoom room = pythonEntryPoint.createRoom(roomMeasurement, roomNode.get("name").asText(),
					roomNode.get("type").asText(), "hei.ies.ies");

			// Parse and add sensors
			JsonNode sensorsNode = roomNode.get("sensors");
			if (sensorsNode != null) {
				for (JsonNode sensorNode : sensorsNode) {
					ISensor sensor = pythonEntryPoint.createSensor(sensorNode.get("id").asText(),
							sensorNode.get("measure").asText(), sensorNode.get("unit").asText(),
							sensorNode.get("type").asText(), sensorNode.get("frequency").asInt());

					// Add sensor data
					JsonNode dataNode = sensorNode.get("data");
					if (dataNode != null && dataNode.isArray()) {
						ArrayList<ISensorData> sensorData = new ArrayList<>();
						for (JsonNode value : dataNode) {
							sensorData.add(pythonEntryPoint.createSensorData(value.asDouble(), null));
						}
						List<Object> sensorDataObjs = new ArrayList<>(sensorData);
						sensor.addData(sensorDataObjs);
					}

					room.addTransducer(sensor);
				}
			}

			return room;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
