package com.hotel.service.reporting;

import java.time.LocalDate;

public class OccupancyReportRow {

    private final LocalDate date;
    private final int roomsAvailable;
    private final int roomsOccupied;
    private final double occupancyPercent;

    public OccupancyReportRow(LocalDate date, int roomsAvailable, int roomsOccupied, double occupancyPercent) {
        this.date = date;
        this.roomsAvailable = roomsAvailable;
        this.roomsOccupied = roomsOccupied;
        this.occupancyPercent = occupancyPercent;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getRoomsAvailable() {
        return roomsAvailable;
    }

    public int getRoomsOccupied() {
        return roomsOccupied;
    }

    /** Numeric percentage value (e.g. 40.0), not a chart — per the brief. */
    public double getOccupancyPercent() {
        return occupancyPercent;
    }
}
