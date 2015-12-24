package com.dianping.cache.alarm.entity;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/10.
 */
public class AlarmConfig {

    private int id = -1;

    private String clusterType;

    private String clusterName;

    private String alarmType;

    private String alarmRule;

    private int threshold;

    private String receiver;

    private boolean mailMode;

    private boolean smsMode;

    private boolean weixinMode;

    private boolean toBusiness;

    private Date createTime;

    private Date updateTime;

    public AlarmConfig(){

    }

    public AlarmConfig(String clusterType, String clusterName, String alarmType){

        this.setId(0);

        this.setClusterType(clusterType)
                .setClusterName(clusterName)
                .setAlarmType(alarmType)
                .setAlarmRule("上阈值");
        if("Memcache".equals(clusterType)){
            if("内存".equals(alarmType)){
                this.threshold = 95;
            }else if("QPS".equals(alarmType)){
                this.threshold = 80000;
            }else if("连接数".equals(alarmType)){
                this.threshold = 28000;
            }
        }else if("Redis".equals(clusterType)){
            if("内存".equals(alarmType)){
                this.threshold = 80;
            }
        }
        this.setReceiver("shiyun.lv,xiaoxiong.dai")
                .setMailMode(true)
                .setSmsMode(false)
                .setWeixinMode(true)
                .setToBusiness(false)
                .setCreateTime(new Date())
                .setUpdateTime(new Date());

    }

    public int getId() {
        return id;
    }

    public AlarmConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getClusterType() {
        return clusterType;
    }

    public AlarmConfig setClusterType(String clusterType) {
        this.clusterType = clusterType;
        return this;
    }

    public String getClusterName() {
        return clusterName;
    }

    public AlarmConfig setClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public AlarmConfig setAlarmType(String alarmType) {
        this.alarmType = alarmType;
        return this;
    }

    public String getAlarmRule() {
        return alarmRule;
    }

    public AlarmConfig setAlarmRule(String alarmRule) {
        this.alarmRule = alarmRule;
        return this;
    }

    public int getThreshold() {
        return threshold;
    }

    public AlarmConfig setThreshold(int threshold) {
        this.threshold = threshold;
        return this;
    }

    public String getReceiver() {
        return receiver;
    }

    public AlarmConfig setReceiver(String receiver) {
        this.receiver = receiver;
        return this;
    }

    public boolean isMailMode() {
        return mailMode;
    }

    public AlarmConfig setMailMode(boolean mailMode) {
        this.mailMode = mailMode;
        return this;
    }

    public boolean isSmsMode() {
        return smsMode;
    }

    public AlarmConfig setSmsMode(boolean smsMode) {
        this.smsMode = smsMode;
        return this;
    }

    public boolean isWeixinMode() {
        return weixinMode;
    }

    public AlarmConfig setWeixinMode(boolean weixinMode) {
        this.weixinMode = weixinMode;
        return this;
    }

    public boolean isToBusiness() {
        return toBusiness;
    }

    public AlarmConfig setToBusiness(boolean toBusiness) {
        this.toBusiness = toBusiness;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public AlarmConfig setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public AlarmConfig setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }
}
