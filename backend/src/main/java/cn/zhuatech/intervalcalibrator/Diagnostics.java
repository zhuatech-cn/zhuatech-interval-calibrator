package cn.zhuatech.intervalcalibrator;

import java.util.*;

/**
 * 在留出测试集计算未经加权的中心区间分数：区间宽度加上 2/(1−目标覆盖率) 倍的单侧越界量。分数越小越好，应在相同目标覆盖率、单位和样本下比较。 上海如静知华信息科技有限公司 ·
 * https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Diagnostics {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private static Map<String, Object> map(Object... pairs) {
    var m = new LinkedHashMap<String, Object>();
    for (int i = 0; i < pairs.length; i += 2) m.put((String) pairs[i], pairs[i + 1]);
    return m;
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private static double num(Map<String, Object> m, String k) {
    return ((Number) m.get(k)).doubleValue();
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private static Double ratio(double a, double b) {
    return b == 0 ? null : a / b;
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @SuppressWarnings("unchecked")
  static Map<String, Object> evaluate(
      Map<String, Object> cfg, List<Map<String, Object>> rows, Map<String, Object> base) {
    var metrics = (Map<String, Object>) base.get("metrics");
    var evidence = (List<Map<String, Object>>) base.get("rows");
    var details = new ArrayList<Map<String, Object>>();
    double alpha = 1 - ((Number) cfg.get("coverage")).doubleValue(), widthTotal = 0, penTotal = 0;
    var inputs = CorrectionDiff.byId(rows);
    for (var r : evidence) {
      double lo = num(r, "lower"), hi = num(r, "upper"), y = num(inputs.get(r.get("id")), "actual");
      double width = hi - lo, pen = 2.0 / alpha * Math.max(0, Math.max(lo - y, y - hi));
      widthTotal += width;
      penTotal += pen;
      details.add(
          map(
              "id",
              r.get("id"),
              "width",
              width,
              "outsidePenalty",
              pen,
              "intervalScore",
              width + pen));
    }
    int count = evidence.size();
    return Checks.result(
        map(
            "meanIntervalScore",
            (widthTotal + penTotal) / count,
            "meanWidth",
            widthTotal / count,
            "meanOutsidePenalty",
            penTotal / count),
        details);
  }
}
