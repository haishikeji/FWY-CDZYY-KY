package com.px.service.admin.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.px.common.annotation.DynamicCache;
import com.px.common.enums.EnPlusApi;
import com.px.common.exception.BusinessException;
import com.px.common.utils.AESUtil;
import com.px.common.utils.CommUtil;
import com.px.entity.admin.*;
import com.px.entity.admin.queryParams.StationQueryParam;
import com.px.entity.admin.vo.LocalStationVo;
import com.px.entity.admin.vo.StationVo;
import com.px.entity.enplus.EnStationStatsInfo;
import com.px.entity.enplus.EnStationStatusInfo;
import com.px.mapper.admin.StationMapper;
import com.px.service.mybatisplus.MyBaseServiceImpl;
import com.px.service.admin.*;
import com.px.service.cache.KymCache;
import com.px.service.enplus.EnPlusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 充电站信息 服务实现类
 * </p>
 *
 * @author 品讯科技
 * @since 2023-08-12
 */
@Service
@DS("db-admin")
@Slf4j
public class StationServiceImpl extends MyBaseServiceImpl<StationMapper, Station> implements StationService {

    private final EnPlusService enPlusService;
    private final EquipmentInfoService equipmentInfoService;
    private final ConnectorInfoService connectorInfoService;
    private final EquipmentRelationService equipmentRelationService;
    private final ActivityStationService activityStationService;
    private final ActivityService activityService;

    public StationServiceImpl(EnPlusService enPlusService, EquipmentInfoService equipmentInfoService,
                              ConnectorInfoService connectorInfoService, EquipmentRelationService equipmentRelationService,
                              ActivityStationService activityStationService, @Lazy ActivityService activityService) {
        this.enPlusService = enPlusService;
        this.equipmentInfoService = equipmentInfoService;
        this.connectorInfoService = connectorInfoService;
        this.equipmentRelationService = equipmentRelationService;
        this.activityStationService = activityStationService;
        this.activityService = activityService;
    }


    @Override
    public List<Station> listStation(StationQueryParam params) {
        // 判断数据权限
        var adminStationIds = KymCache.INSTANCE.getAdminUserStationIds(StpUtil.getLoginIdAsLong());
        QueryWrapper<Station> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("station_status", Station.STATION_STATUS_正常使用);
        if (CommUtil.isNotEmptyAndNull(params.getStationId())) {
            Set<String> stationIdSets = new java.util.HashSet<>(Set.of(params.getStationId()));
            stationIdSets.addAll(adminStationIds);
            queryWrapper.in("station_id", stationIdSets);
        }
        if (CommUtil.isNotEmptyAndNull(adminStationIds)) {
            queryWrapper.in("station_id", adminStationIds);
        }
        queryWrapper.orderByAsc("station_id");
        return list(queryWrapper);
    }

    @Override
    public List<StationVo> queryEnStationInfo(int pageNum, int pageSize) {
        var param = """
                {
                    "PageNo":%d,
                    "PageSize":%d,
                    "LastQueryTime":""
                }
                """.formatted(pageNum, pageSize);
        var response = enPlusService.enPlusPost(EnPlusApi.EN_PLUS_QUERY_STATION_INFO.getApi(), enPlusService.buildParams(param));
        var enStations = JSONObject.parseObject(AESUtil.decrypt(response.getData()));
        var stationList = enStations.getJSONArray("StationInfos").toJavaList(StationVo.class);
        // 我方station表数据
        var stations = list();
        stationList.forEach(station -> {
            station.getEquipmentInfos().forEach(enEquipmentInfo ->
                    enEquipmentInfo.setShortId(KymCache.INSTANCE.getShortIdByEquipmentIdOrConnectorId(enEquipmentInfo.getEquipmentId()))
                            .setParkingNo(KymCache.INSTANCE.getParkNoByEquipmentIdOrConnectorId(enEquipmentInfo.getEquipmentId())));
            var res = stations.stream().filter(item -> item.getStationId().equals(station.getStationId())).toList();
            if (!res.isEmpty()) {
                station.setPictures(res.get(0).getPictures());
            } else {
                station.setPictures(null);
            }
        });
        return stationList;
    }

    @DynamicCache(timeout = 15 * 60 * 1000L)
    @Override
    public Map<String, List<EquipmentInfo>> getCachedEquipmentMap() {
        return equipmentInfoService.list().stream().collect(Collectors.groupingBy(EquipmentInfo::getStationId));
    }

    @DynamicCache(timeout = 15 * 60 * 1000L)
    @Override
    public Map<String, List<ConnectorInfo>> getCachedConnectorMap() {
        return connectorInfoService.list().stream().collect(Collectors.groupingBy(ConnectorInfo::getEquipmentId));
    }


    @Override
    @DynamicCache
    public List<LocalStationVo> queryLocalStationInfo(int pageNum, int pageSize) {
        StationService proxy = (StationService) AopContext.currentProxy();
        var stationList = list();
        var equipmentInfoMap = proxy.getCachedEquipmentMap();
        var connectorInfoMap = proxy.getCachedConnectorMap();

        var stationVoList = new ArrayList<LocalStationVo>();
        for (var station : stationList) {
            var stationVo = new LocalStationVo();
            BeanUtils.copyProperties(station, stationVo);
            var equipmentInfos = equipmentInfoMap.get(station.getStationId());
            if (CommUtil.isNotEmptyAndNull(equipmentInfos)) {
                equipmentInfos.parallelStream().forEach(equipmentInfo -> {
                    // 填充短编号和车位号
                    equipmentInfo.setShortId(KymCache.INSTANCE.getShortIdByEquipmentIdOrConnectorId(equipmentInfo.getEquipmentId()))
                            .setParkingNo(KymCache.INSTANCE.getParkNoByEquipmentIdOrConnectorId(equipmentInfo.getEquipmentId()))
                            .setConnectorInfos(connectorInfoMap.get(equipmentInfo.getEquipmentId()));

                });
                stationVo.setEquipmentInfos(equipmentInfos);
                var res = stationList.stream().filter(item -> item.getStationId().equals(station.getStationId())).toList();
                // 填充站点图片
                if (!res.isEmpty()) {
                    station.setPictures(res.get(0).getPictures());
                } else {
                    station.setPictures(null);
                }
                stationVoList.add(stationVo);
            }
        }

        // 查询正在进行中的活动和各站点正在进行中的活动
        var activityList = activityService.lambdaQuery().eq(Activity::getStatus, Activity.STATUS_进行中).list();
        if (CommUtil.isNotEmptyAndNull(activityList)) {
            var station2ActivityListMap = activityStationService.lambdaQuery().eq(ActivityStation::getStatus, Activity.STATUS_进行中).list()
                    .stream().collect(Collectors.groupingBy(ActivityStation::getStationId));
            var station2ActivityList = new HashMap<String, List<Activity>>();
            station2ActivityListMap.forEach((k, v) -> station2ActivityList.put(k,
                    v.stream().map(item -> activityList.stream().filter(activity -> activity.getId().equals(item.getActivityId())).findFirst().orElse(null)).toList()));
            stationVoList.forEach(vo -> vo.setActivityList(station2ActivityList.get(vo.getStationId()))
                    .setEquipmentInfos(vo.getEquipmentInfos().stream().sorted(Comparator.comparing(EquipmentInfo::getShortId)).toList()));
        }

        return stationVoList;
    }


    @Override
    public List<EnStationStatusInfo> stationStatus(String[] ids) {
        var param = """
                {
                    "StationIDs":["%s"]
                }
                """.formatted(String.join("\",\"", ids));
        var response = enPlusService.enPlusPost(EnPlusApi.EN_PLUS_QUERY_STATION_STATUS.getApi(), enPlusService.buildParams(param));
        var enStationStatus = JSONObject.parseObject(AESUtil.decrypt(response.getData()));
        return enStationStatus.getJSONArray("StationStatusInfos").toJavaList(EnStationStatusInfo.class);
    }


    /**
     * 充电桩统计
     *
     * @param stationId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public EnStationStatsInfo stationStats(String stationId, String startTime, String endTime) {
        var param = """
                {
                    "StationID":"%s",
                    "StartTime":"%s",
                    "EndTime":"%s"
                }
                """.formatted(stationId, startTime, endTime);
        var response = enPlusService.enPlusPost(EnPlusApi.EN_PLUS_QUERY_STATION_STATS.getApi(), enPlusService.buildParams(param));
        // TODO: 2023-08-12 包装成自己的数据格式
        var enStationStats = JSONObject.parseObject(AESUtil.decrypt(response.getData()));
        return enStationStats.getJSONObject("StationStats").toJavaObject(EnStationStatsInfo.class);
    }


    /**
     * 拉取EN+充电站信息数据并更新本地服务器数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pullEnStationInfos(String stationId) {
        // 指定的站点信息
        var stationVo = queryEnStationInfo(1, 1000).stream().filter(vo -> stationId.equals(vo.getStationId())).findFirst().orElse(null);

        var station = new Station();
        var equipmentList = new ArrayList<EquipmentInfo>();
        var connectorList = new ArrayList<ConnectorInfo>();

        // 组装数据
        assert stationVo != null;
        if (!stationVo.getEquipmentInfos().isEmpty()) {
            stationVo.getEquipmentInfos().forEach(item -> {
                // 桩体
                var equipment = new EquipmentInfo()
                        .setStationId(stationVo.getStationId())
                        .setShortId(item.getShortId())
                        .setPower(item.getConnectorInfos().get(0).getPower());
                BeanUtils.copyProperties(item, equipment);
                equipmentList.add(equipment);
                // 枪
                var connectorInfos = item.getConnectorInfos();
                if (!CommUtil.isEmptyOrNull(connectorInfos)) {
                    connectorInfos.forEach(connector -> {
                        // 默认状态设置为空闲
                        var connectorInfo = new ConnectorInfo().setStatus(EquipmentInfo.SERVICE_STATUS_空闲);
                        BeanUtils.copyProperties(connector, connectorInfo);
                        connectorList.add(connectorInfo);
                    });
                }
            });
            equipmentList.sort(Comparator.comparing(EquipmentInfo::getShortId));
        }
        BeanUtils.copyProperties(stationVo, station);

        DynamicDataSourceContextHolder.push("db-admin");
        // 更新站点信息
        updateByQueryWrapper(station, s -> new QueryWrapper<Station>().eq("station_id", station.getStationId()));

        // 更新设备信息
        equipmentInfoService.updateBatchByQueryWrapper(equipmentList, equipmentInfo ->
                new QueryWrapper<EquipmentInfo>().eq("equipment_id", equipmentInfo.getEquipmentId()));

        // 更新枪信息
        connectorInfoService.updateBatchByQueryWrapper(connectorList, connectorInfo ->
                new QueryWrapper<ConnectorInfo>().eq("connector_id", connectorInfo.getConnectorId()));

        DynamicDataSourceContextHolder.poll();

        // 缓存更新，其他数据在站点导入时更新 com.px.service.admin.impl.StationServiceImpl.importStation
        KymCache.INSTANCE.putStationId2Name(Map.of(stationVo.getStationId(), stationVo.getStationName()));
        KymCache.INSTANCE.putConnectorId2Status(connectorList.stream().collect(Collectors.toMap(ConnectorInfo::getConnectorId, ConnectorInfo::getStatus)));

    }


    @Override
    @Transactional
    public void modifyStation(Station station) {
        lambdaUpdate().set(!CommUtil.isEmptyOrNull(station.getPictures()), Station::getPictures, station.getPictures())
                .eq(Station::getStationId, station.getStationId()).update();
    }


    @Override
    @Transactional
    public void importStation(JSONObject data) {
        var fieldIndexes = data.getJSONArray("fieldIndexes");
        if (fieldIndexes.size() != 7) {
            throw new BusinessException("导入数据格式错误，请正确匹配对应数据列");
        }

        String dataList = data.getString("dataList");
        var equipmentRelations = JSON.parseArray(dataList, EquipmentRelation.class).stream().filter(item -> !CommUtil.isEmptyOrNull(item.getId())).toList();

        // t_station
        var stations = equipmentRelations.stream().filter(CommUtil.distinctByKey(EquipmentRelation::getStationId))
                .map(item -> Map.of("stationId", item.getStationId(), "stationName", item.getStationName())).toList();
        var stationList = stations.stream().map(map -> new Station().setStationId(map.get("stationId")).setStationName(map.get("stationName"))).toList();
        saveBatch(stationList);

        // t_equipment_info
        var equipmentInfoList = equipmentRelations.stream().map(item -> {
            var equipmentInfo = new EquipmentInfo();
            BeanUtils.copyProperties(item, equipmentInfo, "id");
            return equipmentInfo;
        }).toList();
        equipmentInfoService.saveBatch(equipmentInfoList);

        // t_connector_info
        var connectorInfoList = equipmentRelations.stream().map(item -> {
            var connectorInfo = new ConnectorInfo();
            BeanUtils.copyProperties(item, connectorInfo, "id");
            return connectorInfo;
        }).toList();
        connectorInfoService.saveBatch(connectorInfoList);

        // t_equipment_relation
        equipmentRelationService.saveBatch(equipmentRelations);

        // 缓存更新
        KymCache.INSTANCE.putStationId2Name(stations.stream().collect(Collectors.toMap(map -> map.get("stationId"), map -> map.get("stationName"))));
        KymCache.INSTANCE.putConnectorId2ShortId(equipmentRelations.stream().collect(Collectors.toMap(EquipmentRelation::getConnectorId, EquipmentRelation::getShortId)));
        KymCache.INSTANCE.putConnectorId2StationId(equipmentRelations.stream().collect(Collectors.toMap(EquipmentRelation::getConnectorId, EquipmentRelation::getStationId)));
        KymCache.INSTANCE.putConnectorId2ParkingNo(equipmentRelations.stream().collect(Collectors.toMap(EquipmentRelation::getConnectorId, EquipmentRelation::getParkingNo)));
    }

}
