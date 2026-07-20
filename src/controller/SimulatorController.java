package controller;

import model.SimulationResult;
import model.Customer;
import model.FlashSaleItem;
import model.enums.LockMechanism;
import repository.CustomerRepository;
import repository.FlashSaleItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller dieu khien cong cu gia lap (Simulator Tool) cho Tuan 8 & 9.
 *
 * Chuc nang chinh:
 * 1. Gia lap N threads (100-1000) cung mua hang dong thoi.
 * 2. Su dung CountDownLatch de tat ca threads KHOI CHAY CUNG LUC (bung no).
 * 3. So sanh 4 co che Lock: NO_LOCK, SYNCHRONIZED, FILE_LOCK, OPTIMISTIC_LOCK.
 * 4. Do Throughput (TPS) va phat hien am kho (Negative Stock / Overselling).
 * 5. Lap lai nhieu lan (iterations) de lay ket qua trung binh (Average).
 *
 * THIET KE QUAN TRONG:
 * Simulator goi TRUC TIEP vao FlashSaleItemRepository.sellWith*() thay vi
 * di qua OrderController, de tranh:
 * - Sinh hang ngan record gia vao orders.csv, order_details.csv, transactions.csv
 * - Xung dot reload() / saveToFile() giua nhieu threads
 * - Do DUNG hieu suat cua co che Lock, khong do overhead cua tao Order/Transaction
 *
 * @author Thanh vien 1 - Simulator Tool (T8-T9)
 */
public class SimulatorController extends BaseController {

    private final FlashSaleItemRepository itemRepo;
    private final CustomerRepository customerRepo;
    private final Random random = new Random();

    // Callback de thong bao tien trinh chay cho View
    private SimulatorProgressCallback progressCallback;

    /**
     * Interface callback de View cap nhat tien trinh.
     */
    public interface SimulatorProgressCallback {
        void onProgress(String message);
    }

    public SimulatorController() {
        this.itemRepo = authState.getFlashSaleItemRepo();
        this.customerRepo = authState.getCustomerRepo();
    }

    public void setProgressCallback(SimulatorProgressCallback callback) {
        this.progressCallback = callback;
    }

    // =====================================================================
    // CHAY GIA LAP CHINH (Core Simulation)
    // =====================================================================

    /**
     * Chay gia lap day du cho TAT CA 4 co che Lock.
     *
     * Thiet ke: Simulator goi TRUC TIEP itemRepo.sellWith*() de do hieu suat
     * lock thuan tuy, khong di qua OrderController (tranh ghi Order/Transaction gia).
     *
     * @param flashSaleItemId Ma san pham Flash Sale de gia lap
     * @param threadCount     So luong threads (50-1000)
     * @param stockPerRun     So luong hang ton kho reset truoc moi lan chay
     * @param iterations      So lan lap lai de lay trung binh
     * @return Danh sach 4 SimulationResult (moi co che 1 ket qua trung binh)
     */
    public List<SimulationResult> runFullSimulation(String flashSaleItemId,
                                                     int threadCount,
                                                     int stockPerRun,
                                                     int iterations) {
        List<SimulationResult> results = new ArrayList<>();
        LockMechanism[] mechanisms = LockMechanism.values();

        // Kiem tra item ton tai
        FlashSaleItem checkItem = itemRepo.getById(flashSaleItemId);
        if (checkItem == null) {
            log("[ERROR] Khong tim thay Flash Sale Item: " + flashSaleItemId);
            return results;
        }

        // === BAT SIMULATION MODE ===
        // Tat file I/O de tranh race condition giua cac threads
        // Moi thao tac getById/update/deductStock chi chay tren RAM
        itemRepo.setSimulationMode(true);
        log("[SIM] Da bat Simulation Mode (RAM-only, khong doc/ghi file)");

        try {

        for (int m = 0; m < mechanisms.length; m++) {
            LockMechanism mechanism = mechanisms[m];
            log(String.format("[%d/%d] Dang chay %s (%d threads x %d lan)...",
                    m + 1, mechanisms.length, mechanism.name(), threadCount, iterations));

            long totalElapsed = 0;
            long totalElapsedNanos = 0;
            int totalSuccess = 0;
            int totalFail = 0;
            int totalNegativeStock = 0;

            for (int iter = 0; iter < iterations; iter++) {
                // 1. Reset stock ve stockPerRun (dong bo, truoc khi bat dau threads)
                resetStock(flashSaleItemId, stockPerRun);

                // 2. Tao thread pool + CountDownLatch (START GATE)
                ExecutorService executor = Executors.newFixedThreadPool(
                        Math.min(threadCount, 100)); // Gioi han pool size de tranh OOM
                CountDownLatch startGate = new CountDownLatch(1);
                CountDownLatch doneLatch = new CountDownLatch(threadCount);
                AtomicInteger successCount = new AtomicInteger(0);
                AtomicInteger failCount = new AtomicInteger(0);

                // 3. Submit tat ca tasks
                // Moi task goi TRUC TIEP itemRepo.sellWith*() de do hieu suat Lock
                for (int t = 0; t < threadCount; t++) {
                    executor.submit(() -> {
                        try {
                            startGate.await(); // CHO TIN HIEU → dong loat khoi chay
                            boolean ok = callSellMethod(mechanism, flashSaleItemId, 1);
                            if (ok) {
                                successCount.incrementAndGet();
                            } else {
                                failCount.incrementAndGet();
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            failCount.incrementAndGet();
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }

                // 4. BAN TIN HIEU + Do thoi gian
                long startNanos = System.nanoTime();
                startGate.countDown(); // >>> TAT CA THREADS BAT DAU! <<<
                try {
                    doneLatch.await(120, TimeUnit.SECONDS); // Cho toi da 2 phut
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                long elapsedNanos = System.nanoTime() - startNanos;
                long elapsedMs = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);

                executor.shutdownNow();

                // 5. Kiem tra am kho (Negative Stock)
                // Fix Lost Updates trong RAM: Khi chay tren RAM, bien soldQty 
                // bi ghi de lan nhau (Race Condition) nen item.getSoldQty() bi sai lech.
                // Tinh truc tiep so luong am kho tu so luong don thanh cong thuc te.
                int negStock = Math.max(0, successCount.get() - stockPerRun);

                totalElapsed += elapsedMs;
                totalElapsedNanos += elapsedNanos;
                totalSuccess += successCount.get();
                totalFail += failCount.get();
                totalNegativeStock += negStock;

                log(String.format("  Lan %d/%d: Success=%d, Fail=%d, Time=%dms, NegStock=%d",
                        iter + 1, iterations, successCount.get(), failCount.get(), elapsedMs, negStock));
            }

            // 6. Tinh trung binh va luu ket qua
            double avgElapsedNanos = (double) totalElapsedNanos / iterations;
            double exactTps = (avgElapsedNanos > 0) ? 
                    ( (totalSuccess / (double)iterations) * 1_000_000_000.0 / avgElapsedNanos ) : 0.0;

            SimulationResult avg = new SimulationResult(
                    mechanism,
                    totalSuccess / iterations,
                    totalFail / iterations,
                    totalElapsed / iterations,
                    exactTps,
                    totalNegativeStock / iterations
            );
            results.add(avg);
        }

        log("[HOAN TAT] Gia lap xong tat ca 4 co che!");

        } finally {
            // === TAT SIMULATION MODE ===
            // Phuc hoi data goc tu file CSV (tat ca thay doi trong RAM bi huy)
            itemRepo.setSimulationMode(false);
            log("[SIM] Da tat Simulation Mode (data goc duoc phuc hoi tu file)");
        }

        return results;
    }

    // =====================================================================
    // HELPER METHODS
    // =====================================================================

    /**
     * Goi truc tiep phuong thuc sell tuong ung tren FlashSaleItemRepository.
     * Tranh di qua OrderController de khong sinh Order/Transaction gia.
     */
    private boolean callSellMethod(LockMechanism mechanism, String itemId, int quantity) {
        switch (mechanism) {
            case NO_LOCK:
                return itemRepo.sellWithNoLock(itemId, quantity);
            case SYNCHRONIZED:
                return itemRepo.sellWithSynchronized(itemId, quantity);
            case FILE_LOCK:
                return itemRepo.sellWithFileLock(itemId, quantity);
            case OPTIMISTIC_LOCK:
                return itemRepo.sellWithOptimisticLock(itemId, quantity);
            default:
                return false;
        }
    }

    /**
     * Reset stock cua Flash Sale Item ve gia tri ban dau.
     * Goi TRUOC khi bat dau threads de dam bao trang thai sach.
     */
    private void resetStock(String itemId, int newStock) {
        FlashSaleItem item = itemRepo.getById(itemId);
        if (item != null) {
            item.setSoldQty(0);
            item.setLimitedQty(newStock);
            item.setVersion(0);
            itemRepo.update(item);
        }
    }

    /**
     * Lay danh sach Customer ID tu file CSV (chi lay APPROVED).
     */
    private List<String> getCustomerIds() {
        List<String> ids = new ArrayList<>();
        List<Customer> customers = customerRepo.getAll();
        for (Customer c : customers) {
            if (c.getStatus() == model.enums.AccountStatus.APPROVED) {
                ids.add(c.getId());
            }
        }
        if (ids.isEmpty()) {
            for (Customer c : customers) {
                ids.add(c.getId());
            }
        }
        return ids;
    }

    /**
     * Gui thong bao tien trinh cho View (neu co callback).
     */
    private void log(String message) {
        if (progressCallback != null) {
            progressCallback.onProgress(message);
        } else {
            System.out.println(message);
        }
    }

    /**
     * Lay danh sach Flash Sale Items de View hien thi cho nguoi dung chon.
     */
    public List<FlashSaleItem> getAvailableItems() {
        return itemRepo.getAll();
    }

    // =====================================================================
    // TUAN 9: RESEARCH EXPERIMENT (Batch chay nhieu muc threads)
    // =====================================================================

    /**
     * Chay thi nghiem nghien cuu day du (Research Experiment).
     *
     * Chay batch gia lap voi nhieu muc threads khac nhau (100, 250, 500, 1000)
     * x 4 co che Lock x N lan lap de lay trung binh.
     * Ket qua duoc xuat ra file CSV de ve bieu do.
     *
     * @param flashSaleItemId Ma san pham Flash Sale
     * @param threadCounts    Mang cac muc threads can chay (VD: {100, 250, 500, 1000})
     * @param stockPerRun     So luong hang ton kho moi lan
     * @param iterations      So lan lap lai moi muc (de lay trung binh)
     * @param outputCsvPath   Duong dan file CSV xuat ket qua
     * @return Danh sach tat ca ket qua (threadCounts.length * 4 entries)
     */
    public List<ExperimentEntry> runResearchExperiment(String flashSaleItemId,
                                                       int[] threadCounts,
                                                       int stockPerRun,
                                                       int iterations,
                                                       String outputCsvPath) {
        List<ExperimentEntry> allEntries = new ArrayList<>();

        log("========================================");
        log("  RESEARCH EXPERIMENT - TUAN 9");
        log("  Stock/run: " + stockPerRun + " | Iterations: " + iterations);
        log("  Thread counts: " + java.util.Arrays.toString(threadCounts));
        log("========================================");

        for (int tc = 0; tc < threadCounts.length; tc++) {
            int threadCount = threadCounts[tc];
            log(String.format("\n>>> BATCH %d/%d: %d threads <<<",
                    tc + 1, threadCounts.length, threadCount));

            List<SimulationResult> batchResults = runFullSimulation(
                    flashSaleItemId, threadCount, stockPerRun, iterations);

            for (SimulationResult r : batchResults) {
                allEntries.add(new ExperimentEntry(threadCount, r));
            }
        }

        // Xuat ket qua ra file CSV
        exportToCsv(allEntries, outputCsvPath);

        log("\n[HOAN TAT] Da xuat ket qua ra file: " + outputCsvPath);
        log("Tong so dong: " + allEntries.size());
        return allEntries;
    }

    /**
     * Xuat ket qua thi nghiem ra file CSV.
     */
    private void exportToCsv(List<ExperimentEntry> entries, String filePath) {
        try {
            java.io.File file = new java.io.File(filePath);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            java.io.BufferedWriter writer = new java.io.BufferedWriter(
                    new java.io.FileWriter(file));

            // Header
            writer.write("threads,mechanism,avg_success,avg_fail,avg_elapsed_ms,tps,negative_stock");
            writer.newLine();

            // Data
            for (ExperimentEntry e : entries) {
                writer.write(String.join(",",
                        String.valueOf(e.threadCount),
                        e.result.getMechanism().name(),
                        String.valueOf(e.result.getSuccessCount()),
                        String.valueOf(e.result.getFailCount()),
                        String.valueOf(e.result.getElapsedMs()),
                        String.format("%.2f", e.result.getTps()),
                        String.valueOf(e.result.getNegativeStock())
                ));
                writer.newLine();
            }

            writer.flush();
            writer.close();
            log("[CSV] Xuat thanh cong " + entries.size() + " dong vao " + filePath);

        } catch (java.io.IOException ex) {
            log("[ERROR] Loi khi ghi file CSV: " + ex.getMessage());
        }
    }

    /**
     * Data class gom threadCount + SimulationResult (dung cho Research Experiment).
     */
    public static class ExperimentEntry {
        public final int threadCount;
        public final SimulationResult result;

        public ExperimentEntry(int threadCount, SimulationResult result) {
            this.threadCount = threadCount;
            this.result = result;
        }
    }
}
