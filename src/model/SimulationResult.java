package model;

import model.enums.LockMechanism;

/**
 * Data class chua ket qua cua mot lan gia lap (Simulation) cho 1 co che Lock.
 *
 * Moi object chua:
 * - mechanism:    Co che Lock da su dung (NO_LOCK, SYNCHRONIZED, ...)
 * - successCount: So giao dich thanh cong (trung binh)
 * - failCount:    So giao dich that bai (trung binh)
 * - elapsedMs:    Thoi gian xu ly (trung binh, ms)
 * - tps:          Throughput = successCount / (elapsedMs / 1000.0)
 * - negativeStock: So luong am kho (overselling) phat hien duoc
 *
 * @author Thanh vien 1 - Simulator Tool (T8)
 */
public class SimulationResult {

    private LockMechanism mechanism;
    private int successCount;
    private int failCount;
    private long elapsedMs;
    private double tps;
    private int negativeStock;

    public SimulationResult(LockMechanism mechanism, int successCount,
                            int failCount, long elapsedMs, int negativeStock) {
        this.mechanism = mechanism;
        this.successCount = successCount;
        this.failCount = failCount;
        this.elapsedMs = elapsedMs;
        this.negativeStock = negativeStock;
        this.tps = (elapsedMs > 0)
                ? (successCount * 1000.0 / elapsedMs)
                : 0.0;
    }

    /** Constructor ho tro truyen TPS tinh toan tu NanoTime de tranh chia cho 0 */
    public SimulationResult(LockMechanism mechanism, int successCount,
                            int failCount, long elapsedMs, double tps, int negativeStock) {
        this.mechanism = mechanism;
        this.successCount = successCount;
        this.failCount = failCount;
        this.elapsedMs = elapsedMs;
        this.tps = tps;
        this.negativeStock = negativeStock;
    }

    // Getters
    public LockMechanism getMechanism() { return mechanism; }
    public int getSuccessCount() { return successCount; }
    public int getFailCount() { return failCount; }
    public long getElapsedMs() { return elapsedMs; }
    public double getTps() { return tps; }
    public int getNegativeStock() { return negativeStock; }

    /**
     * Tinh phan tram chenh lech TPS so voi Baseline (NO_LOCK).
     * @param baselineTps TPS cua NO_LOCK
     * @return Phan tram chenh lech (am = cham hon Baseline)
     */
    public String getVsBaseline(double baselineTps) {
        if (mechanism == LockMechanism.NO_LOCK) {
            return "(Baseline)";
        }
        if (baselineTps <= 0) {
            return "N/A";
        }
        double diff = ((tps - baselineTps) / baselineTps) * 100.0;
        return String.format("%+.1f%%", diff);
    }

    /**
     * Xuat ket qua ra dang CSV de luu file experiment.
     */
    public String toCsvLine(int threadCount, int iteration) {
        return String.join(",",
                String.valueOf(threadCount),
                mechanism.name(),
                String.valueOf(iteration),
                String.valueOf(successCount),
                String.valueOf(failCount),
                String.valueOf(elapsedMs),
                String.format("%.2f", tps),
                String.valueOf(negativeStock));
    }
}
