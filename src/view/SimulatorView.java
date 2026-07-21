package view;

import controller.SimulatorController;
import model.SimulationResult;
import model.FlashSaleItem;
import model.enums.LockMechanism;

import java.util.List;

/**
 * Giao dien Console cho cong cu gia lap (Simulator Tool).
 *
 * Chuc nang:
 * 1. Hien thi menu cau hinh (so threads, san pham, so lan lap).
 * 2. Hien thi tien trinh chay gia lap (realtime).
 * 3. In bang so sanh ASCII ket qua 4 co che Lock.
 * 4. In bieu do ASCII don gian (Bar chart).
 *
 * @author Thanh vien 1 - Simulator Tool (T8)
 */
public class SimulatorView {

    private final SimulatorController simulatorController;

    public SimulatorView() {
        this.simulatorController = new SimulatorController();
    }

    /**
     * Hien thi menu chinh cua Simulator.
     */
    public void showSimulatorMenu() {
        while (true) {
            ConsoleUI.printHeader("SIMULATOR TOOL - STRESS TEST");
            System.out.println("1. Chay Simulator (Flash Sale Item)");
            System.out.println("2. Chay nhanh (Quick Test - 100 threads)");
            System.out.println("3. Research Experiment (T9 - Batch 100/250/500/1000)");
            System.out.println("0. Quay lai");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon (0-3): ", 0, 3);
            switch (choice) {
                case 1:
                    runCustomSimulation();
                    break;
                case 2:
                    runQuickTest();
                    break;
                case 3:
                    runResearchExperiment();
                    break;
                case 0:
                    return;
            }
            if (choice != 0) {
                ConsoleUI.pause();
            }
        }
    }

    /**
     * Chay Simulator voi cau hinh tuy chinh.
     */
    private void runCustomSimulation() {
        // 1. Hien thi danh sach Flash Sale Items de chon
        List<FlashSaleItem> items = simulatorController.getAvailableItems();
        if (items.isEmpty()) {
            ConsoleUI.printError("Khong co Flash Sale Item nao trong he thong!");
            return;
        }

        ConsoleUI.printHeader("DANH SACH FLASH SALE ITEMS");
        System.out.printf("%-10s | %-12s | %-10s | %-10s | %-10s%n",
                "Item ID", "Product ID", "SalePrice", "Stock", "Sold");
        System.out.println("-------------------------------------------------------------");
        for (FlashSaleItem item : items) {
            System.out.printf("%-10s | %-12s | %-10.2f | %-10d | %-10d%n",
                    item.getId(), item.getProductId(), item.getSalePrice(),
                    item.getLimitedQty(), item.getSoldQty());
        }
        System.out.println();

        // 2. Nhap cau hinh
        String itemId = ConsoleUI.getString("Nhap ID Flash Sale Item: ");
        int threadCount = ConsoleUI.getInt("Nhap so luong threads (50-1000): ", 50, 1000);
        int stock = ConsoleUI.getInt("Nhap so luong hang ton kho (stock) cho moi lan chay: ", 10, 10000);
        int iterations = ConsoleUI.getInt("Nhap so lan lap lai (1-10): ", 1, 10);

        // 3. Xac nhan
        System.out.println();
        System.out.println("=== CAU HINH GIA LAP ===");
        System.out.println("  Item ID    : " + itemId);
        System.out.println("  Threads    : " + threadCount);
        System.out.println("  Stock      : " + stock);
        System.out.println("  Iterations : " + iterations);
        System.out.println("========================");
        String confirm = ConsoleUI.getString("Xac nhan chay? (y/n): ");
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Da huy.");
            return;
        }

        // 4. Chay gia lap
        System.out.println();
        ConsoleUI.printHeader("DANG CHAY GIA LAP...");

        simulatorController.setProgressCallback(message ->
                System.out.println(ConsoleUI.CYAN + "  >> " + message + ConsoleUI.RESET));

        List<SimulationResult> results = simulatorController.runFullSimulation(
                itemId, threadCount, stock, iterations);

        // 5. Hien thi ket qua
        if (!results.isEmpty()) {
            printResultTable(results, threadCount, stock, iterations);
            printBarChart(results);
        } else {
            ConsoleUI.printError("Khong co ket qua gia lap nao!");
        }
    }

    /**
     * Chay nhanh voi cau hinh mac dinh (100 threads, 50 stock, 1 lan).
     */
    private void runQuickTest() {
        List<FlashSaleItem> items = simulatorController.getAvailableItems();
        if (items.isEmpty()) {
            ConsoleUI.printError("Khong co Flash Sale Item nao!");
            return;
        }

        // Lay item dau tien lam mac dinh
        String itemId = items.get(0).getId();
        int threadCount = 100;
        int stock = 50;
        int iterations = 1;

        System.out.println();
        ConsoleUI.printHeader("QUICK TEST (100 threads, 50 stock)");
        System.out.println("Su dung item: " + itemId);

        simulatorController.setProgressCallback(message ->
                System.out.println(ConsoleUI.CYAN + "  >> " + message + ConsoleUI.RESET));

        List<SimulationResult> results = simulatorController.runFullSimulation(
                itemId, threadCount, stock, iterations);

        if (!results.isEmpty()) {
            printResultTable(results, threadCount, stock, iterations);
            printBarChart(results);
        }
    }

    // =====================================================================
    // IN BANG ASCII KET QUA
    // =====================================================================

    /**
     * In bang so sanh ASCII dep mat voi cot "vs Baseline".
     */
    private void printResultTable(List<SimulationResult> results,
                                   int threadCount, int stock, int iterations) {
        System.out.println();
        ConsoleUI.printHeader("KET QUA GIA LAP");
        System.out.println("  Cau hinh: " + threadCount + " threads | "
                + stock + " stock | " + iterations + " lan lap");
        System.out.println();

        // Tim Baseline TPS (NO_LOCK)
        double baselineTps = 0;
        for (SimulationResult r : results) {
            if (r.getMechanism() == LockMechanism.NO_LOCK) {
                baselineTps = r.getTps();
                break;
            }
        }

        // In bang
        String border =    "+---------------------+---------+--------+-----------+----------+---------------+-----------+";
        String header =    "| Mechanism           | Success |  Fail  | Elapsed   |   TPS    | vs Baseline   | Neg.Stock |";

        System.out.println(border);
        System.out.println(header);
        System.out.println(border);

        for (SimulationResult r : results) {
            String mechName = String.format("%-19s", r.getMechanism().name());
            String success = String.format("%7d", r.getSuccessCount());
            String fail = String.format("%6d", r.getFailCount());
            String elapsed = String.format("%7dms", r.getElapsedMs());
            String tps = String.format("%8.1f", r.getTps());
            String vsBaseline = String.format("%-13s", r.getVsBaseline(baselineTps));
            String negStock = String.format("%9d", r.getNegativeStock());

            // Mau sac cho cot Neg.Stock
            String negColor = (r.getNegativeStock() > 0) ? ConsoleUI.RED : ConsoleUI.GREEN;

            System.out.printf("| %s | %s | %s | %s | %s | %s | %s%s%s |%n",
                    mechName, success, fail, elapsed, tps, vsBaseline,
                    negColor, negStock, ConsoleUI.RESET);
        }

        System.out.println(border);
        System.out.println();

        // In nhan xet tu dong
        printAnalysis(results, baselineTps);
    }

    /**
     * In bieu do ASCII don gian (TPS Bar Chart).
     */
    private void printBarChart(List<SimulationResult> results) {
        System.out.println();
        ConsoleUI.printHeader("BIEU DO THROUGHPUT (TPS)");

        // Tim max TPS de scale
        double maxTps = 0;
        for (SimulationResult r : results) {
            if (r.getTps() > maxTps) maxTps = r.getTps();
        }

        int maxBarLen = 40; // Do dai toi da cua thanh bar

        for (SimulationResult r : results) {
            String mechName = String.format("%-17s", r.getMechanism().name());
            int barLen = (maxTps > 0)
                    ? (int) (r.getTps() / maxTps * maxBarLen)
                    : 0;

            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < barLen; i++) {
                bar.append("\u2588"); // Full block Unicode character
            }

            // Mau sac theo co che
            String color;
            switch (r.getMechanism()) {
                case NO_LOCK:        color = ConsoleUI.RED; break;
                case SYNCHRONIZED:   color = ConsoleUI.YELLOW; break;
                case FILE_LOCK:      color = ConsoleUI.CYAN; break;
                case OPTIMISTIC_LOCK: color = ConsoleUI.GREEN; break;
                default:             color = ConsoleUI.RESET;
            }

            System.out.printf("%s %s%s%s %.1f TPS%n",
                    mechName, color, bar.toString(), ConsoleUI.RESET, r.getTps());
        }
        System.out.println();
    }

    /**
     * In phan tich tu dong dua tren ket qua.
     */
    private void printAnalysis(List<SimulationResult> results, double baselineTps) {
        System.out.println(ConsoleUI.YELLOW + "=== PHAN TICH TU DONG ===" + ConsoleUI.RESET);

        for (SimulationResult r : results) {
            switch (r.getMechanism()) {
                case NO_LOCK:
                    if (r.getNegativeStock() > 0) {
                        System.out.println(ConsoleUI.RED
                                + "  [!] NO_LOCK: Phat hien AM KHO " + r.getNegativeStock()
                                + " don vi. KHONG AN TOAN cho production!" + ConsoleUI.RESET);
                    } else {
                        System.out.println(ConsoleUI.GREEN
                                + "  [OK] NO_LOCK: Khong phat hien am kho (may man do it threads)."
                                + ConsoleUI.RESET);
                    }
                    break;
                case SYNCHRONIZED:
                    System.out.printf("  [*] SYNCHRONIZED: TPS = %.1f (an toan trong 1 JVM).%n",
                            r.getTps());
                    break;
                case FILE_LOCK:
                    System.out.printf("  [*] FILE_LOCK: TPS = %.1f (an toan da tien trinh, nhung cham nhat).%n",
                            r.getTps());
                    break;
                case OPTIMISTIC_LOCK:
                    double diff = (baselineTps > 0)
                            ? ((r.getTps() - baselineTps) / baselineTps) * 100.0
                            : 0;
                    String verdict = (Math.abs(diff) <= 30)
                            ? ConsoleUI.GREEN + "DAT" + ConsoleUI.RESET
                            : ConsoleUI.RED + "KHONG DAT" + ConsoleUI.RESET;
                    System.out.printf("  [*] OPTIMISTIC_LOCK: TPS = %.1f | vs Baseline: %.1f%% → Muc tieu <=30%%: %s%n",
                            r.getTps(), diff, verdict);
                    break;
            }
        }

        System.out.println();
        System.out.println(ConsoleUI.CYAN
                + "  Research Question: \"Co che nao dat 0%% am kho VA throughput khong giam qua 30%%?\""
                + ConsoleUI.RESET);

        // Tu dong tra loi
        for (SimulationResult r : results) {
            if (r.getMechanism() != LockMechanism.NO_LOCK
                    && r.getNegativeStock() == 0) {
                double diff = (baselineTps > 0)
                        ? Math.abs((r.getTps() - baselineTps) / baselineTps * 100.0)
                        : 0;
                if (diff <= 30) {
                    System.out.println(ConsoleUI.GREEN
                            + "  >>> TRA LOI: " + r.getMechanism().name()
                            + " dat ca 2 tieu chi! (0 am kho, TPS giam "
                            + String.format("%.1f%%", diff) + ")" + ConsoleUI.RESET);
                }
            }
        }
        System.out.println();
    }

    // =====================================================================
    // TUAN 9: RESEARCH EXPERIMENT
    // =====================================================================

    /**
     * Chay thi nghiem nghien cuu day du (Batch 100/250/500/1000 threads).
     * Ket qua xuat ra file data/transactions.csv.
     */
    private void runResearchExperiment() {
        List<FlashSaleItem> items = simulatorController.getAvailableItems();
        if (items.isEmpty()) {
            ConsoleUI.printError("Khong co Flash Sale Item nao!");
            return;
        }

        // Hien thi danh sach items
        ConsoleUI.printHeader("RESEARCH EXPERIMENT - TUAN 9");
        System.out.printf("%-10s | %-12s | %-10s | %-10s%n",
                "Item ID", "Product ID", "Stock", "Sold");
        System.out.println("-----------------------------------------------");
        for (FlashSaleItem item : items) {
            System.out.printf("%-10s | %-12s | %-10d | %-10d%n",
                    item.getId(), item.getProductId(),
                    item.getLimitedQty(), item.getSoldQty());
        }
        System.out.println();

        String itemId = ConsoleUI.getString("Nhap ID Flash Sale Item: ");
        int stock = ConsoleUI.getInt("Nhap stock cho moi lan chay (50-500): ", 50, 500);
        int iterations = ConsoleUI.getInt("Nhap so lan lap lai moi batch (1-5): ", 1, 5);

        System.out.println();
        System.out.println(ConsoleUI.YELLOW + "=== CAU HINH RESEARCH EXPERIMENT ===" + ConsoleUI.RESET);
        System.out.println("  Item ID      : " + itemId);
        System.out.println("  Stock/run    : " + stock);
        System.out.println("  Iterations   : " + iterations);
        System.out.println("  Thread batches: 100, 250, 500, 1000");
        System.out.println("  Tong so lan chay: " + (4 * 4 * iterations) + " (4 batches x 4 mechanisms x " + iterations + " iters)");
        System.out.println("  Output file  : data/transactions.csv");
        System.out.println("====================================");

        String confirm = ConsoleUI.getString("Day la quy trinh chay LAU. Xac nhan? (y/n): ");
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Da huy.");
            return;
        }

        System.out.println();
        ConsoleUI.printHeader("DANG CHAY RESEARCH EXPERIMENT...");

        simulatorController.setProgressCallback(message ->
                System.out.println(ConsoleUI.CYAN + "  >> " + message + ConsoleUI.RESET));

        int[] threadCounts = {100, 250, 500, 1000};
        String outputPath = "data/transactions.csv";

        List<SimulatorController.ExperimentEntry> entries =
                simulatorController.runResearchExperiment(
                        itemId, threadCounts, stock, iterations, outputPath);

        // In bang tong hop ket qua
        if (!entries.isEmpty()) {
            printExperimentSummary(entries, threadCounts);
        }
    }

    /**
     * In bang tong hop ket qua Research Experiment (nhieu muc threads).
     */
    private void printExperimentSummary(List<SimulatorController.ExperimentEntry> entries,
                                        int[] threadCounts) {
        System.out.println();
        ConsoleUI.printHeader("TONG HOP KET QUA RESEARCH EXPERIMENT");

        String border = "+--------+---------------------+---------+--------+-----------+----------+-----------+";
        String header = "| Thread | Mechanism           | Success |  Fail  | Elapsed   |   TPS    | Neg.Stock |";

        System.out.println(border);
        System.out.println(header);
        System.out.println(border);

        for (SimulatorController.ExperimentEntry e : entries) {
            String negColor = (e.result.getNegativeStock() > 0) ? ConsoleUI.RED : ConsoleUI.GREEN;

            System.out.printf("| %6d | %-19s | %7d | %6d | %7dms | %8.1f | %s%9d%s |%n",
                    e.threadCount,
                    e.result.getMechanism().name(),
                    e.result.getSuccessCount(),
                    e.result.getFailCount(),
                    e.result.getElapsedMs(),
                    e.result.getTps(),
                    negColor, e.result.getNegativeStock(), ConsoleUI.RESET);

            // In duong ke ngan giua cac batch
            boolean isLastInBatch = false;
            for (int tc : threadCounts) {
                if (e.threadCount == tc && e.result.getMechanism() == LockMechanism.OPTIMISTIC_LOCK) {
                    isLastInBatch = true;
                }
            }
            if (isLastInBatch) {
                System.out.println(border);
            }
        }

        System.out.println();
        ConsoleUI.printSuccess("Da luu ket qua vao file: data/transactions.csv");
        System.out.println(ConsoleUI.CYAN
                + "  Ban co the dung file CSV nay de ve bieu do trong Excel/Google Sheets."
                + ConsoleUI.RESET);

        // In Research Question summary
        System.out.println();
        System.out.println(ConsoleUI.YELLOW + "=== TRA LOI RESEARCH QUESTION ===" + ConsoleUI.RESET);
        System.out.println(ConsoleUI.CYAN
                + "  \"Co che nao dat 0% am kho VA throughput khong giam qua 30%?\"" + ConsoleUI.RESET);

        for (int tc : threadCounts) {
            double baselineTps = 0;
            // Tim baseline cho muc thread nay
            for (SimulatorController.ExperimentEntry e : entries) {
                if (e.threadCount == tc && e.result.getMechanism() == LockMechanism.NO_LOCK) {
                    baselineTps = e.result.getTps();
                    break;
                }
            }

            System.out.println("\n  >> " + tc + " threads:");
            for (SimulatorController.ExperimentEntry e : entries) {
                if (e.threadCount != tc) continue;
                if (e.result.getMechanism() == LockMechanism.NO_LOCK) continue;

                double diff = (baselineTps > 0)
                        ? Math.abs((e.result.getTps() - baselineTps) / baselineTps * 100.0)
                        : 0;
                boolean safeStock = (e.result.getNegativeStock() == 0);
                boolean goodTps = (diff <= 30);

                String stockIcon = safeStock ? ConsoleUI.GREEN + "OK" + ConsoleUI.RESET
                        : ConsoleUI.RED + "FAIL" + ConsoleUI.RESET;
                String tpsIcon = goodTps ? ConsoleUI.GREEN + "OK" + ConsoleUI.RESET
                        : ConsoleUI.RED + "FAIL" + ConsoleUI.RESET;
                String verdict = (safeStock && goodTps)
                        ? ConsoleUI.GREEN + ">>> DAT <<<" + ConsoleUI.RESET
                        : ConsoleUI.RED + "KHONG DAT" + ConsoleUI.RESET;

                System.out.printf("     %-17s | NegStock: %s | TPS giam: %.1f%% %s | %s%n",
                        e.result.getMechanism().name(),
                        stockIcon, diff, tpsIcon, verdict);
            }
        }
        System.out.println();
    }
}
