package com.dianping.cache.alarm.controller;

import com.dianping.cache.alarm.alarmconfig.AlarmConfigService;
import com.dianping.cache.alarm.alarmtemplate.AlarmTemplateService;
import com.dianping.cache.alarm.alarmtemplate.MemcacheAlarmTemplateService;
import com.dianping.cache.alarm.alarmtemplate.RedisAlarmTemplateService;
import com.dianping.cache.alarm.controller.dto.AlarmConfigDto;
import com.dianping.cache.alarm.controller.mapper.AlarmConfigMapper;
import com.dianping.cache.alarm.dataanalyse.task.BaselineTaskFactory;
import com.dianping.cache.alarm.dataanalyse.thread.BaselineComputeThread;
import com.dianping.cache.alarm.dataanalyse.thread.BaselineThreadFactory;
import com.dianping.cache.alarm.entity.AlarmConfig;
import com.dianping.cache.alarm.entity.AlarmTemplate;
import com.dianping.cache.alarm.threadmanager.ThreadManager;
import com.dianping.cache.controller.AbstractSidebarController;
import com.dianping.cache.controller.RedisDataUtil;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.squirrel.view.highcharts.statsdata.RedisClusterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by lvshiyun on 15/12/6.
 */
@Controller
public class AlarmConfigController extends AbstractSidebarController {

    @Autowired
    private AlarmConfigService alarmConfigService;

    @Autowired
    private MemcacheAlarmTemplateService memcacheAlarmTemplateService;

    @Autowired
    private RedisAlarmTemplateService redisAlarmTemplateService;

    @Autowired
    private AlarmTemplateService alarmTemplateService;

    @Autowired
    private BaselineTaskFactory baselineTaskFactory;

    @Autowired
    private BaselineThreadFactory baselineThreadFactory;

    @RequestMapping(value = "/config/alarm")
    public ModelAndView topicSetting(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("alarm/alarmconfig", createViewMap());
    }

    @RequestMapping(value = "/config/alarm/list", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object alarmMetaList(int offset, int limit, HttpServletRequest request, HttpServletResponse response) {
        List<AlarmConfig> alarmConfigs = alarmConfigService.findByPage(offset, limit);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("size", alarmConfigs.size());
        result.put("entities", alarmConfigs);
        return result;
    }


    @RequestMapping(value = "/config/alarm/create", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public boolean createAlarmConfig(@RequestBody AlarmConfigDto alarmConfigDto) {
        boolean result = false;
        
        if (alarmConfigDto.isUpdate()) {
            AlarmConfig alarmConfig = alarmConfigService.findById(alarmConfigDto.getId());
            alarmConfigDto.setCreateTime(alarmConfig.getCreateTime());
            alarmConfigDto.setUpdateTime(new Date());

            result = alarmConfigService.update(AlarmConfigMapper.convertToAlarmConfig(alarmConfigDto));
        } else {
            alarmConfigDto.setCreateTime(new Date());
            alarmConfigDto.setUpdateTime(new Date());
            result = alarmConfigService.insert(AlarmConfigMapper.convertToAlarmConfig(alarmConfigDto));
        }
        return result;
    }


    @RequestMapping(value = "/config/alarm/remove", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public int removeAlarmConfig(int id) {
        int result = alarmConfigService.deleteById(id);

        return result;
    }

    @Autowired
    private CacheConfigurationService cacheConfigurationService;

    @RequestMapping(value = "/config/alarm/query/memcacheclusters", method = RequestMethod.GET)
    @ResponseBody
    public Object findMemcacheClusters() {
        List<String> clusterNames = new ArrayList<String>();
        List<CacheConfiguration> configList = cacheConfigurationService.findAll();

        for (CacheConfiguration cacheConfiguration : configList) {
            if (cacheConfiguration.getCacheKey().contains("memcache")) {
                clusterNames.add(cacheConfiguration.getCacheKey());
            }
        }

        return clusterNames;
    }

    @RequestMapping(value = "/config/alarm/query/redisclusters", method = RequestMethod.GET)
    @ResponseBody
    public List<String> findRedisClusters() {
        List<String> clusterNames = new ArrayList<String>();
        List<RedisClusterData> redisClusterDatas = RedisDataUtil.getClusterData();

        for (RedisClusterData redisClusterData : redisClusterDatas) {
            clusterNames.add(redisClusterData.getClusterName());
        }

        return clusterNames;
    }

    @RequestMapping(value = "/config/alarm/query/memcachetemplates", method = RequestMethod.GET)
    @ResponseBody
    public List<String> findMemcacheTemplates() {
        List<String> templateNames = new ArrayList<String>();
        List<AlarmTemplate> alarmTemplates = alarmTemplateService.findAll();
        List<AlarmTemplate>memcacheAlarmTemplates = new ArrayList<AlarmTemplate>();
        for(AlarmTemplate alarmTemplate:alarmTemplates){
            if(alarmTemplate.getAlarmType().contains("Memcache")){
                    memcacheAlarmTemplates.add(alarmTemplate);
            }
        }


        for (AlarmTemplate alarmTemplate : memcacheAlarmTemplates) {
            if(!templateNames.contains(alarmTemplate.getTemplateName())) {
                templateNames.add(alarmTemplate.getTemplateName());
            }
        }

        return templateNames;
    }


    @RequestMapping(value = "/config/alarm/query/redistemplates", method = RequestMethod.GET)
    @ResponseBody
    public List<String> findRedisTemplates() {
        List<String> templateNames = new ArrayList<String>();
        List<AlarmTemplate> alarmTemplates = alarmTemplateService.findAll();
        List<AlarmTemplate>redisTemplates = new ArrayList<AlarmTemplate>();
        for(AlarmTemplate alarmTemplate:alarmTemplates){
            if(alarmTemplate.getAlarmType().contains("Redis")){
                redisTemplates.add(alarmTemplate);
            }
        }

        for (AlarmTemplate redisTemplate : redisTemplates) {
            if(!templateNames.contains(redisTemplate.getTemplateName())) {
                templateNames.add(redisTemplate.getTemplateName());
            }
        }

        return templateNames;
    }

    @RequestMapping(value = "/config/alarm/baselineCompute")
    @ResponseBody
    public void baselineCompute() {

        BaselineComputeThread baselineComputeThread = baselineThreadFactory.createBaselineComputeThread();

        ThreadManager.getInstance().execute(baselineComputeThread);

        return;
    }


    @Override
    protected String getSide() {
        return "config";
    }

    @Override
    public String getSubSide() {
        return "alarmconfig";
    }

}
