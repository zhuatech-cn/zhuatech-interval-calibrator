package cn.zhuatech.intervalcalibrator;

import static cn.zhuatech.intervalcalibrator.Checks.*;

import java.util.*;

/**
 * 将测试样本标记为低于下界、区间内或高于上界，按越界幅度排序，报告最大越界量与实际覆盖率相对目标的差。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Inspection {
  /**
   * 有序结果，可表达无法估计的数值。上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
   * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  private static Map<String, Object> fields(Object... pairs) {
    var row = new LinkedHashMap<String, Object>();
    for (int i = 0; i < pairs.length; i += 2) row.put((String) pairs[i], pairs[i + 1]);
    return row;
  }

  /**
   * 对已校验的输入生成复核证据。上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
   * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  @SuppressWarnings("unchecked")
  static Map<String, Object> evaluate(
      Map<String, Object> cfg, List<Map<String, Object>> rows, Map<String, Object> base) {
    var details = new ArrayList<Map<String, Object>>();
    var m = (Map<String, Object>) base.get("metrics");

    double rad = ((Number) m.get("radius")).doubleValue(), maximum = 0;
    int low = 0, high = 0;
    for (var r : rows)
      if (r.get("part").equals("test")) {
        double y = num(r, "prediction", -1e12, 1e12),
            a = num(r, "actual", -1e12, 1e12),
            d = a - y,
            over = Math.max(0, Math.abs(d) - rad);
        String side = "inside";
        if (over > 0) {
          if (d < 0) {
            low++;
            side = "below";
          } else {
            high++;
            side = "above";
          }
        }
        maximum = Math.max(maximum, over);
        details.add(fields("id", r.get("id"), "actual", a, "outsideBy", over, "location", side));
      }
    details.sort(
        Comparator.<Map<String, Object>>comparingDouble(
                v -> ((Number) v.get("outsideBy")).doubleValue())
            .reversed()
            .thenComparing(v -> v.get("id").toString()));
    return result(
        fields(
            "below",
            low,
            "above",
            high,
            "maxOutside",
            maximum,
            "coverageGap",
            ((Number) m.get("empiricalCoverage")).doubleValue()
                - ((Number) cfg.get("coverage")).doubleValue()),
        details);
  }
}
