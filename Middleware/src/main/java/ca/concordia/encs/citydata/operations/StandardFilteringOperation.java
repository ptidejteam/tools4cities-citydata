package ca.concordia.encs.citydata.operations;

import java.util.ArrayList;

import java.util.List;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.implementations.AbstractOperation;

/**
 * This operation filters CSV sensor readings by sensor ID, room, and date.
 * @author Minette Zongo M.
 * @since 2025-10-06
 */

public class StandardFilteringOperation extends AbstractOperation<String> implements IOperation<String> {

	private String sensorId;
    private String room;
    private String date;
    private IRunner observer;

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public ArrayList<String> apply(ArrayList<String> input) {
        ArrayList<String> filtered = new ArrayList<>();
        
        int lineNum = 0;
        for (String line : input) {
            if (line == null || line.trim().isEmpty()) continue;
            
            String[] cols = line.split(",");
            if (cols.length < 6) {
                continue;
            }
            
            String timestamp = cols[0].trim(); 
            String lineSensorId = cols[1].trim();
            String lineRoom = cols[5].trim();
            
            String lineDate = timestamp.length() >= 10 ? timestamp.substring(0, 10) : timestamp;
            
            boolean matchesSensor = (sensorId == null || sensorId.isEmpty() || lineSensorId.equals(sensorId));
            boolean matchesRoom = (room == null || room.isEmpty() || lineRoom.equals(room));
            boolean matchesDate = (date == null || date.isEmpty() || lineDate.equals(date));
            
            if (lineNum < 3) {
                System.out.println("[DEBUG]   matchesSensor = " + matchesSensor);
                System.out.println("[DEBUG]   matchesRoom = " + matchesRoom);
                System.out.println("[DEBUG]   matchesDate = " + matchesDate);
            }
            
            if (matchesSensor && matchesRoom && matchesDate) {
                filtered.add(line);
            }
            
            lineNum++;
        }

        return filtered;
    }

    @Override
    public void addObserver(IRunner aRunner) {
        this.observer = aRunner;
    }

    @Override
    public void notifyObservers() {
        if (observer != null) {
            observer.newOperationApplied(this);
        }
    }
}