package com.ctu.cinema;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeTicketBookingSystem {

    private static final int TOTAL_SEATS = 50;
    private final boolean[] seats = new boolean[TOTAL_SEATS + 1];

    // 1. Khóa tường minh bảo vệ miền tối hạn
    private final ReentrantLock lock = new ReentrantLock();

    // 2. Biến đếm nguyên tử an toàn luồng
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedBookings = new AtomicInteger(0);

    public boolean bookSeat(int seatNumber, String customerName) {
        if (seatNumber < 1 || seatNumber > TOTAL_SEATS) {
            System.err.println("❌ Ghế không hợp lệ: " + seatNumber);
            return false;
        }

        // BẮT ĐẦU KHÓA MIỀN TỐI HẠN
        lock.lock();
        try {
            if (!seats[seatNumber]) {
                try { Thread.sleep(10); } catch (InterruptedException e) {}
                seats[seatNumber] = true;
                successfulBookings.incrementAndGet();
                System.out.println("✅ [Thành công] " + customerName + " đã đặt thành công ghế #" + seatNumber);
                return true;
            } else {
                failedBookings.incrementAndGet();
                System.out.println("❌ [Thất bại] " + customerName + " đặt ghế #" + seatNumber + " nhưng ghế ĐÃ BỊ CHIẾM!");
                return false;
            }
        } finally {
            lock.unlock(); // LUÔN GIẢI PHÓNG KHÓA
        }
    }

    public int getSuccessfulBookings() { return successfulBookings.get(); }
    public int getFailedBookings() { return failedBookings.get(); }

    public static void main(String[] args) throws InterruptedException {
        ThreadSafeTicketBookingSystem bookingSystem = new ThreadSafeTicketBookingSystem();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        System.out.println("🚀 Bắt đầu mô phỏng 100 yêu cầu đặt vé đồng thời (THREAD-SAFE)...");

        for (int i = 1; i <= 100; i++) {
            final int customerId = i;
            final int targetSeat = (i % 50) + 1;
            executor.submit(() -> {
                bookingSystem.bookSeat(targetSeat, "Khách hàng #" + customerId);
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("\n========= THỐNG KÊ TỔNG KẾT (THREAD-SAFE RUN) =========");
        System.out.println("Tổng số lượt đặt THÀNH CÔNG: " + bookingSystem.getSuccessfulBookings());
        System.out.println("Tổng số lượt đặt THẤT BẠI: " + bookingSystem.getFailedBookings());
        System.out.println("=======================================================");
    }
}
