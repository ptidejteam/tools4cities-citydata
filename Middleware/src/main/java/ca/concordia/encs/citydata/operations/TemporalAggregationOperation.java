package ca.concordia.encs.citydata.operations;

import java.util.ArrayList;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.implementations.AbstractOperation;

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

        System.out.println("[DEBUG] Filter params - room: '" + room + "', date: '" + date +
                         "', startTime: '" + startTime + "', endTime: '" + endTime + "'");

        for (String line : input) {
            if (line == null || line.trim().isEmpty()) continue;
            totalLines++;

            String[] cols = line.split("\t");
            if (cols.length < 6) {
                System.out.println("[DEBUG] Skipping line with only " + cols.length + " columns");
                continue;
            }

            // Parse date and time from first column
            String datetime = cols[0].trim();
            String[] dtParts = datetime.split(" ");
            String lineDate = dtParts[0];
            String lineTime = (dtParts.length > 1) ? dtParts[1] : "";
            if (lineTime.length() >= 8) {
                lineTime = lineTime.substring(0, 8);
            }

            String occupancyValue = cols[2].trim(); // correct index
            String lineRoom = cols[5].trim();       // correct index

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

            if (totalLines <= 3) {
                System.out.println("[DEBUG] Line " + totalLines + ": date=" + lineDate +
                        ", time=" + lineTime + ", room=" + lineRoom + ", occupancy=" + occupancyValue +
                        ", matches=" + (matchesRoom && matchesDate && withinTimeRange && isOccupied));
            }
        }

        System.out.println("[DEBUG] Processed " + totalLines + " lines, " + matchedLines +
                         " matched filters, " + occupancyCount + " were occupied");

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
