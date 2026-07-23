package com.hotel.service;

import com.hotel.model.AuditLog;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.repository.AuditLogRepository;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.reporting.OccupancyReportRow;
import com.hotel.service.reporting.RevenueReportRow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportingService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final BillingRepository billingRepository;
    private final AuditLogRepository auditLogRepository;

    public ReportingService(ReservationRepository reservationRepository, RoomRepository roomRepository,
                             BillingRepository billingRepository, AuditLogRepository auditLogRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.billingRepository = billingRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * One row per day in [from, to]: reservations checking in that day (cancelled ones
     * excluded), with subtotal/tax straight off the reservation and discounts pulled from
     * each one's Billing.
     */
    public List<RevenueReportRow> revenueReport(LocalDate from, LocalDate to) {
        List<Reservation> all = reservationRepository.findAll();
        List<RevenueReportRow> rows = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            LocalDate day = date;
            List<Reservation> onDay = all.stream()
                    .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                    .filter(r -> r.getCheckIn().equals(day))
                    .collect(Collectors.toList());

            double subtotal = onDay.stream().mapToDouble(Reservation::getSubtotal).sum();
            double tax = onDay.stream().mapToDouble(Reservation::getTax).sum();
            double discounts = onDay.stream()
                    .mapToDouble(r -> billingRepository.findByReservation(r)
                            .map(b -> b.getDiscount() + b.getLoyaltyDiscount())
                            .orElse(0.0))
                    .sum();
            double total = onDay.stream().mapToDouble(Reservation::getTotal).sum() - discounts;

            rows.add(new RevenueReportRow(date, onDay.size(), subtotal, tax, discounts, total));
        }
        return rows;
    }

    /**
     * One row per day in [from, to]: how many of the hotel's rooms are occupied that day
     * (a non-cancelled reservation covers it) vs. available, and the occupancy percentage
     * as a plain number — no chart, per the brief.
     */
    public List<OccupancyReportRow> occupancyReport(LocalDate from, LocalDate to) {
        int totalRooms = roomRepository.findAll().size();
        List<Reservation> all = reservationRepository.findAllWithRooms();
        List<OccupancyReportRow> rows = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            LocalDate day = date;
            long occupied = all.stream()
                    .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                    .filter(r -> !day.isBefore(r.getCheckIn()) && day.isBefore(r.getCheckOut()))
                    .flatMap(r -> r.getRooms().stream())
                    .map(Room::getId)
                    .distinct()
                    .count();

            double percent = totalRooms == 0 ? 0.0 : (occupied * 100.0) / totalRooms;
            rows.add(new OccupancyReportRow(date, totalRooms - (int) occupied, (int) occupied, percent));
        }
        return rows;
    }

    /** Activity log rows within [from, to] (inclusive), newest first. */
    public List<AuditLog> activityLogReport(LocalDate from, LocalDate to) {
        return auditLogRepository.findAll().stream()
                .filter(log -> {
                    LocalDate logDate = log.getTimestamp().toLocalDate();
                    return !logDate.isBefore(from) && !logDate.isAfter(to);
                })
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }
}
