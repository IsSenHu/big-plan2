package com.gapache.cloud.money.management.server.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gapache.cloud.money.management.common.model.FundDTO;
import com.gapache.cloud.money.management.common.model.FundNetWorthDTO;
import com.gapache.cloud.money.management.server.service.FundNetWorthService;
import com.gapache.cloud.money.management.server.service.FundService;
import com.gapache.commons.utils.HttpUtils;
import com.gapache.commons.utils.TimeUtils;
import com.gapache.job.common.model.BlockingStrategy;
import com.gapache.job.common.model.JobStatus;
import com.gapache.job.common.model.TaskInfo;
import com.gapache.job.sdk.JobTrigger;
import com.gapache.job.sdk.annotation.ZzhJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HuSen
 * @since 2021/2/7 5:48 下午
 */
@Slf4j
@ZzhJob(
        name = "AutoCrawlingFundDataJob",
        author = "胡森",
        cron = "0/30 * * * * ? *",
        description = "自动拉取基金数据",
        status = JobStatus.RUNNING,
        retryTimes = 1,
        blockingStrategy = BlockingStrategy.COVER
)
public class AutoCrawlingFundDataJob implements JobTrigger {

    private static final Set<String> CRAWLING_RECORD = new HashSet<>(64);

    @Resource
    private FundService fundService;

    @Resource
    private FundNetWorthService fundNetWorthService;

    @Override
    public boolean execute(TaskInfo taskInfo) {
        autoCrawlingData();
        return true;
    }

    private void autoCrawlingData() {
        log.info("autoCrawlingData start");
        String nowStr = TimeUtils.format(TimeUtils.Format._1, LocalDate.now());
        Set<String> remove = CRAWLING_RECORD.stream().filter(key -> !key.startsWith(nowStr)).collect(Collectors.toSet());
        for (String key : remove) {
            CRAWLING_RECORD.remove(key);
        }

        List<FundDTO> hold = fundService.queryHold();
        List<FundDTO> optional = fundService.queryOptional();

        hold.addAll(optional);
        Set<String> codes = hold.stream().map(FundDTO::getCode).collect(Collectors.toSet());

        for (String code : codes) {
            if (CRAWLING_RECORD.contains(nowStr + code)) {
                continue;
            }
            String resp = getByCode(code);
            log.info("autoCrawlingData:{}, {}", code, resp);
            if (StringUtils.isBlank(resp)) {
                log.warn("拉取基金数据失败:{}", code);
                continue;
            }
            String jsonStr = StringUtils.substringBetween(resp, "jQuery18307379642342092141_1610676271182(", ")");
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);

            if (0 != jsonObject.getInteger("ErrCode")) {
                log.warn("拉取基金数据失败:{}", code);
                continue;
            }

            JSONArray jsonArray = jsonObject.getJSONObject("Data").getJSONArray("LSJZList");
            int size = jsonArray.size();
            List<FundNetWorthDTO> dtoList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                FundNetWorthDTO dto = new FundNetWorthDTO();
                dto.setCode(code);
                dto.setDay(TimeUtils.parseLocalDate(TimeUtils.Format._1, item.getString("FSRQ")));
                dto.setNetWorth(Double.parseDouble(item.getString("DWJZ")));
                dto.setAddUpNetWorth(Double.parseDouble(item.getString("LJJZ")));
                dtoList.add(dto);
            }

            if (fundNetWorthService.add(dtoList)) {
                CRAWLING_RECORD.add(nowStr + code);
            }
        }
    }

    private String getByCode(String code) {
        Map<String, String> params = new HashMap<>(6);
        Map<String, String> headers = new HashMap<>(12);

        params.put("callback", "jQuery18307379642342092141_1610676271182");
        params.put("fundCode", code);
        params.put("pageIndex", "1");
        params.put("pageSize", "20");
        params.put("startDate", "");
        params.put("endDate", "");

        headers.put("Accept", "*/*");
        headers.put("Accept-Encoding", "zh-CN,zh;q=0.9");
        headers.put("Connection", "keep-alive");
        headers.put("Cookie", "em_hq_fls=js; HAList=f-0-399001-%u6DF1%u8BC1%u6210%u6307; qgqp_b_id=08f8eb285ae25422dd4f46a4c8e814b2; st_si=26616275876504; st_asi=delete; EMFUND1=null; EMFUND2=null; EMFUND3=null; EMFUND4=null; EMFUND5=null; EMFUND6=null; EMFUND7=null; EMFUND8=null; EMFUND0=null; EMFUND9=01-15 14:02:42@#$%u62DB%u5546%u4E2D%u8BC1%u767D%u9152%u6307%u6570%28LOF%29@%23%24161725; st_pvi=67385768425746; st_sp=2020-10-26%2015%3A52%3A47; st_inirUrl=https%3A%2F%2Fwww.baidu.com%2Flink; st_sn=16; st_psi=20210115140242171-0-6833573879");
        headers.put("Host", "api.fund.eastmoney.com");
        headers.put("Referer", "http://fundf10.eastmoney.com/");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36");

        return HttpUtils.getSync("http://api.fund.eastmoney.com/f10/lsjz", params, headers);
    }
}
