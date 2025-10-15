package ca.concordia.ngci.tools4cities.metamenth;

import ca.concordia.ngci.tools4cities.metamenth.interfaces.IPythonEntryPoint;
import ca.concordia.ngci.tools4cities.metamenth.interfaces.structure.IBuilding;
import py4j.GatewayServer;

/**
 * A Java client that creates Python objects by 
  * calling the appropriate methods on a Python entry point object
  * provided when connection is established with a Python server. 
  * PythonObjectCreate passed to the connection enable python to access
  * the appropriate Java objects
 * @author Peter Yefi
 */
/*public class PythonEntryServer {

	private PythonObjectCreator pythonObjectCreator = new PythonObjectCreator();
	private GatewayServer gatewayServer;
	private IBuilding building;

	public PythonEntryServer() {
		this.gatewayServer = new GatewayServer(this);
		this.gatewayServer.start();
	}

	*//**
		* Creates the LB building by access the relevant MetamEnTh class
		* and Middleare operations and productions
		* @param pythonEntryPoint, python object to provides access to MetamEnTh classes
		*/
/*

public void createLBBuilding(IPythonEntryPoint pythonEntryPoint) {
building = pythonObjectCreator.createLBBuilding(pythonEntryPoint);
}

*//**
	* Creates a building from JSON string
	* @param pythonEntryPoint python object to provides access to MetamEnTh classes
	* @param jsonString JSON string containing building data
	*/

/*
public void createBuildingFromJson(IPythonEntryPoint pythonEntryPoint, String jsonString) {
building = pythonObjectCreator.createBuildingFromJson(pythonEntryPoint, jsonString);
}

*//**
	* Creates a building from JSON file in resources
	* @param pythonEntryPoint python object to provides access to MetamEnTh classes
	* @param jsonFileName name of JSON file in resources folder
	*//*
		public void createBuildingFromJsonFile(IPythonEntryPoint pythonEntryPoint, String jsonFileName) {
		building = pythonObjectCreator.createBuildingFromJsonFile(pythonEntryPoint, jsonFileName);
		}
		
		public IBuilding getLBBuilding() {
		return building;
		}
		
		public IBuilding getBuilding() {
		return building;
		}
		
		public GatewayServer getGatewayServer() {
		return this.gatewayServer;
		}
		
		public PythonObjectCreator getPythonObjectCreator() {
		return this.pythonObjectCreator;
		}
		
		public static void main(String[] args) {
		PythonEntryServer pythonEntryServer = new PythonEntryServer();
		
		IPythonEntryPoint pythonEntryPoint = (IPythonEntryPoint) pythonEntryServer.gatewayServer
				.getPythonServerEntryPoint(new Class[] { IPythonEntryPoint.class });
		pythonEntryServer.createLBBuilding(pythonEntryPoint);
		System.out.println("Server is running!!!");
		}
		
		}*/

public class PythonEntryServer {

	private PythonObjectCreator pythonObjectCreator = new PythonObjectCreator();
	private GatewayServer gatewayServer;
	private IBuilding building;

	public PythonEntryServer() {
		this.gatewayServer = new GatewayServer(this);
		this.gatewayServer.start();
	}

	/**
	 * Creates the LB building by accessing the relevant MetamEnTh class
	 * and Middleware operations and productions
	 * @param pythonEntryPoint python object to provides access to MetamEnTh classes
	 */
	public void createLBBuilding(IPythonEntryPoint pythonEntryPoint) {
		building = pythonObjectCreator.createLBBuilding(pythonEntryPoint);
	}

	/**
	 * Creates a building from JSON string
	 * @param jsonString JSON string containing building data
	 */
	public void createBuildingFromJson(String jsonString) {
		IPythonEntryPoint pythonEntryPoint = (IPythonEntryPoint) this.gatewayServer
				.getPythonServerEntryPoint(new Class[] { IPythonEntryPoint.class });
		building = pythonObjectCreator.createBuildingFromJson(pythonEntryPoint, jsonString);
	}

	/**
	 * Creates a building from JSON file in resources
	 * @param jsonFileName name of JSON file in resources folder
	 */
	public void createBuildingFromJsonFile(String jsonFileName) {
		IPythonEntryPoint pythonEntryPoint = (IPythonEntryPoint) this.gatewayServer
				.getPythonServerEntryPoint(new Class[] { IPythonEntryPoint.class });
		building = pythonObjectCreator.createBuildingFromJsonFile(pythonEntryPoint, jsonFileName);
	}

	public IBuilding createBuildingFromJson() {
		return building;
	}

	public IBuilding createBuildingFromJsonFile() {
		return building;
	}

	public IBuilding getLBBuilding() {
		return building;
	}

	public IBuilding getBuilding() {
		return building;
	}

	public GatewayServer getGatewayServer() {
		return this.gatewayServer;
	}

	public PythonObjectCreator getPythonObjectCreator() {
		return this.pythonObjectCreator;
	}

	public static void main(String[] args) {
		PythonEntryServer pythonEntryServer = new PythonEntryServer();

		IPythonEntryPoint pythonEntryPoint = (IPythonEntryPoint) pythonEntryServer.gatewayServer
				.getPythonServerEntryPoint(new Class[] { IPythonEntryPoint.class });
		pythonEntryServer.createLBBuilding(pythonEntryPoint);
		System.out.println("Server is running!!!");
	}
}
