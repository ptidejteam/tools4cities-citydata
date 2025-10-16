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

public class PythonEntryServer {

	public static void main(String[] args) {
		PythonEntryServer pythonEntryServer = new PythonEntryServer();

		IPythonEntryPoint pythonEntryPoint = (IPythonEntryPoint) pythonEntryServer.gatewayServer
				.getPythonServerEntryPoint(new Class[] { IPythonEntryPoint.class });
		pythonEntryServer.createLBBuilding(pythonEntryPoint);

		System.out.println("Server is running!!!");
	}

	private IBuilding building;
	private GatewayServer gatewayServer;

	private PythonObjectCreator pythonObjectCreator = new PythonObjectCreator();

	public PythonEntryServer() {
		this.gatewayServer = new GatewayServer(this);
		this.gatewayServer.start();
	}

	public IBuilding createBuildingFromJson() {
		return building;
	}

	public void createBuildingFromJson(String jsonString) {
		IPythonEntryPoint pythonEntryPoint = (IPythonEntryPoint) this.gatewayServer
				.getPythonServerEntryPoint(new Class[] { IPythonEntryPoint.class });
		building = pythonObjectCreator.createBuildingFromJson(pythonEntryPoint, jsonString);
	}

	public void createLBBuilding(IPythonEntryPoint pythonEntryPoint) {
		building = pythonObjectCreator.createLBBuilding(pythonEntryPoint);
	}

	public IBuilding getBuilding() {
		return building;
	}

	public GatewayServer getGatewayServer() {
		return this.gatewayServer;
	}

	public IBuilding getLBBuilding() {
		return building;
	}

	public PythonObjectCreator getPythonObjectCreator() {
		return this.pythonObjectCreator;
	}
}
