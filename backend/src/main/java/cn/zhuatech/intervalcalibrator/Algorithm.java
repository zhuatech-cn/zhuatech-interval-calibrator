package cn.zhuatech.intervalcalibrator;

import static cn.zhuatech.intervalcalibrator.Checks.*;

import java.util.*;

/**
 * 用独立校准集的绝对残差，计算有限样本分位点和留出集区间覆盖率。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Algorithm {
  /**
   * 适用于可交换样本；不训练预测模型。校准样本不足以得到有限区间时明确拒绝。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
   * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  static Map<String, Object> compute(Map<String, Object> cfg, List<Map<String, Object>> rows) {
    var base = calculate(cfg, rows);
    var enriched = new LinkedHashMap<String, Object>(base);
    enriched.put("inspection", Inspection.evaluate(cfg, rows, base));
    enriched.put("diagnostics", Diagnostics.evaluate(cfg, rows, base));
    return enriched;
  }

  /**
   * 保留原始计算口径。上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  private static Map<String, Object> calculate(
      Map<String, Object> cfg, List<Map<String, Object>> rows) {
    must(rows != null && !rows.isEmpty() && rows.size() <= 200, "需要 1–200 行");
    unique(rows);
    var evidence = new ArrayList<Map<String, Object>>();
    double cov = num(cfg, "coverage", 0.01, 0.999);
    var errors = new ArrayList<Double>();
    for (var r : rows) {
      String p = str(r, "part", false);
      must(Set.of("cal", "test").contains(p), "part 应为 cal/test");
      double a = num(r, "actual", -1e12, 1e12), y = num(r, "prediction", -1e12, 1e12);
      if (p.equals("cal")) errors.add(Math.abs(a - y));
    }
    int rank =
        java.math.BigDecimal.valueOf(cov)
            .multiply(java.math.BigDecimal.valueOf(errors.size() + 1))
            .setScale(0, java.math.RoundingMode.CEILING)
            .intValue();
    must(rank <= errors.size() && errors.size() < rows.size(), "需要足够校准记录及独立测试记录");
    Collections.sort(errors);
    double radius = errors.get(rank - 1);
    int hit = 0;
    for (var r : rows)
      if (r.get("part").equals("test")) {
        double y = ((Number) r.get("prediction")).doubleValue(),
            a = ((Number) r.get("actual")).doubleValue();
        boolean ok = Math.abs(a - y) <= radius;
        if (ok) hit++;
        evidence.add(
            Map.of("id", r.get("id"), "lower", y - radius, "upper", y + radius, "covered", ok));
      }
    return result(
        Map.of(
            "rank",
            rank,
            "radius",
            radius,
            "covered",
            hit,
            "empiricalCoverage",
            (double) hit / evidence.size()),
        evidence);
  }
}
