package ca.concordia.encs.citydata.operations;

import java.util.ArrayList;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.implementations.AbstractOperation;

/**
 * This operation filters occupancy readings for a specified room over a given date, with optional time range from occupancy 
 * sensors, then counts how many readings indicate the room's occupancy. It can also notify an observer when the operation is 
 * completed.
 * @author Minette Zongo M.
 * @date: 2025-10-07
 */

public class TemporalAggregationOperation extends AbstractOperation<String> implements IOperation<String> {
    private String room;
    private String date;
    private String startTime;
    private String endTime;
    private IRunner observer;

    public void setRoom(String room) {
        this.room = room;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public ArrayList<String> apply(ArrayList<String> input) {
        int occupancyCount = 0;
        int totalLines = 0;
        int matchedLines = 0;

        for (String line : input) {
            if (line == null || line.trim().isEmpty()) continue;
            totalLines++;

            // Split by COMMA
            String[] cols = line.split(",");

            String timestamp = cols[0].trim();
            String[] timestampParts = timestamp.split(" ");
            String lineDate = timestampParts[0]; 
            String lineTimeRaw = timestampParts[1];
            String lineTime = lineTimeRaw.substring(0, Math.min(8, lineTimeRaw.length())); 

            String lineSensorId = cols[1].trim();
            
            String occupancyValue = cols[2].trim();
            
            String lineBuilding = cols[3].trim();

            String lineFloor = cols[4].trim();

            String lineRoom = cols[5].trim();

            boolean matchesRoom = (room == null || room.isEmpty() || lineRoom.equals(room));
            boolean matchesDate = (date == null || date.isEmpty() || lineDate.equals(date));

            boolean withinTimeRange = true;
            if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
                withinTimeRange = lineTime.compareTo(startTime) >= 0 && lineTime.compareTo(endTime) <= 0;
            }

            boolean isOccupied = "1".equals(occupancyValue);

            if (matchesRoom && matchesDate && withinTimeRange && isOccupied) {
                occupancyCount++;
                matchedLines++;
            }

        }

        ArrayList<String> result = new ArrayList<>();
        result.add("occupancy_count," + occupancyCount + ",room," + room + ",date," + date +
                ",start_time," + startTime + ",end_time," + endTime);
        return result;
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
