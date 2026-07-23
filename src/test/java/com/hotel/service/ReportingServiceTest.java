package com.hotel.service;

import com.hotel.model.AdminUser;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.enums.ReservationStatus;
import com.hotel.model.enums.Role;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.repository.AdminUserRepository;
import com.hotel.repository.AuditLogRepository;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.reporting.OccupancyReportRow;
import com.hotel.service.reporting.RevenueReportRow;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportingServiceTest {

    private static final long DAY_OFFSET = 18000 + new Random().nextInt(3000);

    private final GuestRepository guestRepository = new GuestRepository();
    private final RoomRepository roomRepository = new RoomRepository();
    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final BillingRepository billingRepository = new BillingRepository();
    private final AuditLogRepository auditLogRepository = new AuditLogRepository();

    private final ReportingService reportingService = new ReportingService(
            reservationRepository, roomRepository, billingRepository, auditLogRepository);

    @Test
    void revenueReportReconcilesExactlyWithTheReservationsCreatedOnThatDay() {
        LocalDate day = LocalDate.now().plusDays(DAY_OFFSET);

        Guest guestA = new Guest("Rev A", "555-0800", "reva." + UUID.randomUUID() + "@example.com", "1 A St", "A1B 2C3");
        Guest guestB = new Guest("Rev B", "555-0801", "revb." + UUID.randomUUID() + "@example.com", "1 B St", "B1C 2D3");
        guestRepository.save(guestA);
        guestRepository.save(guestB);

        Room roomA = roomRepository.save(new Room(RoomType.SINGLE, 100.0, RoomStatus.AVAILABLE));
        Room roomB = roomRepository.save(new Room(RoomType.DOUBLE, 160.0, RoomStatus.AVAILABLE));

        // Two reservations checking in on `day`: subtotal 200+13tax=... use explicit totals.
        reservationRepository.createWithAssociations(guestA, List.of(roomA), List.of(),
                day, day.plusDays(2), 1, 0, ReservationStatus.CONFIRMED, 200.0, 26.0, 226.0);
        reservationRepository.createWithAssociations(guestB, List.of(roomB), List.of(),
                day, day.plusDays(2), 1, 0, ReservationStatus.CONFIRMED, 320.0, 41.6, 361.6);
        // A cancelled reservation on the same day must be excluded entirely.
        Room roomC = roomRepository.save(new Room(RoomType.DELUXE, 220.0, RoomStatus.AVAILABLE));
        reservationRepository.createWithAssociations(guestA, List.of(roomC), List.of(),
                day, day.plusDays(2), 1, 0, ReservationStatus.CANCELLED, 1000.0, 130.0, 1130.0);

        List<RevenueReportRow> rows = reportingService.revenueReport(day, day);
        assertEquals(1, rows.size());

        RevenueReportRow row = rows.get(0);
        assertEquals(2, row.getReservationCount());
        assertEquals(520.0, row.getSubtotal(), 0.001);
        assertEquals(67.6, row.getTax(), 0.001);
        // No discounts applied, so total should equal subtotal+tax exactly.
        assertEquals(587.6, row.getTotal(), 0.001);
    }

    @Test
    void occupancyReportProducesAPlainNumericPercentageThatMatchesRoomsOccupiedOverTotal() {
        LocalDate day = LocalDate.now().plusDays(DAY_OFFSET + 200);

        int totalRoomsBefore = roomRepository.findAll().size();
        Guest guest = new Guest("Occ Guest", "555-0802", "occ." + UUID.randomUUID() + "@example.com",
                "1 Occ Ave", "O1C 2C3");
        guestRepository.save(guest);
        Room room = roomRepository.save(new Room(RoomType.PENTHOUSE, 400.0, RoomStatus.AVAILABLE));
        int totalRoomsAfter = totalRoomsBefore + 1;

        reservationRepository.createWithAssociations(guest, List.of(room), List.of(),
                day, day.plusDays(1), 1, 0, ReservationStatus.CONFIRMED, 400.0, 52.0, 452.0);

        List<OccupancyReportRow> rows = reportingService.occupancyReport(day, day);
        assertEquals(1, rows.size());

        OccupancyReportRow row = rows.get(0);
        assertEquals(totalRoomsAfter, row.getRoomsAvailable() + row.getRoomsOccupied());
        double expectedPercent = (row.getRoomsOccupied() * 100.0) / totalRoomsAfter;
        assertEquals(expectedPercent, row.getOccupancyPercent(), 0.001);
        // It's a plain number, not a chart object.
        assertTrue(row.getOccupancyPercent() >= 0.0 && row.getOccupancyPercent() <= 100.0);
    }

    @Test
    void activityLogReportOnlyReturnsRowsWithinTheDateRange() {
        AdminUserRepository adminUserRepository = new AdminUserRepository();
        AdminUser admin = new AdminUser("report-test-" + UUID.randomUUID(), "hash", Role.ADMIN);
        adminUserRepository.save(admin);

        LocalDate inRangeDay = LocalDate.now().plusDays(DAY_OFFSET + 400);
        auditLogRepository.save(new com.hotel.model.AuditLog(admin, "TEST_ACTION_IN_RANGE",
                inRangeDay.atTime(10, 0)));
        auditLogRepository.save(new com.hotel.model.AuditLog(admin, "TEST_ACTION_OUT_OF_RANGE",
                inRangeDay.plusDays(10).atTime(10, 0)));

        List<com.hotel.model.AuditLog> rows = reportingService.activityLogReport(inRangeDay, inRangeDay);

        assertTrue(rows.stream().anyMatch(r -> r.getAction().equals("TEST_ACTION_IN_RANGE")));
        assertTrue(rows.stream().noneMatch(r -> r.getAction().equals("TEST_ACTION_OUT_OF_RANGE")));
    }
}
