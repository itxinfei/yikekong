package com.yikekong.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yikekong.emq.EmqClient;
import com.yikekong.entity.AlarmEntity;
import com.yikekong.exception.BussinessException;
import com.yikekong.service.AlarmService;
import com.yikekong.vo.Pager;
import com.yikekong.vo.QuotaVO;
import com.yikekong.entity.QuotaEntity;
import com.yikekong.service.QuotaService;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("/quota")
public class QuotaController {

    @Autowired
    private QuotaService quotaService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private EmqClient emqClient;

    /**
     * 创建指标
     *
     * @param vo
     * @return
     */
    @PostMapping
    public boolean create(@RequestBody QuotaVO vo) {
        try {
            QuotaEntity quotaEntity = new QuotaEntity();
            BeanUtils.copyProperties(vo, quotaEntity);
            //订阅主题
            try {
                emqClient.subscribe("$queue/" + vo.getSubject());
            } catch (MqttException e) {
                e.printStackTrace();
            }
            return quotaService.save(quotaEntity);
        } catch (DuplicateKeyException e) {
            throw new BussinessException("已存在该名称");
        }
    }

    /**
     * 分页获取所有指标
     *
     * @param page
     * @param pageSize
     * @param quotaName
     * @return
     */
    @GetMapping
    public Pager<QuotaEntity> queryPage(@RequestParam(value = "page", required = false, defaultValue = "1") Long page,
                                        @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize,
                                        @RequestParam(value = "quotaName", required = false) String quotaName) {
        return new Pager<>(quotaService.queryPage(page, pageSize, quotaName));
    }

    /**
     * 更新指标
     *
     * @param vo
     * @return
     */
    @PutMapping
    public Boolean update(@RequestBody QuotaVO vo) {
        try {
            QuotaEntity entity = new QuotaEntity();
            BeanUtils.copyProperties(vo, entity);

            return quotaService.updateById(entity);
        } catch (DuplicateKeyException e) {
            throw new BussinessException("已存在该名称");
        }
    }

    /**
     * 删除指标
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Integer id) {
        QueryWrapper<AlarmEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AlarmEntity::getQuotaId, id);
        Integer count = alarmService.count(queryWrapper);
        if (count > 0) {
            throw new BussinessException("该指标使用中");
        }
        return quotaService.removeById(id);
    }

    /**
     * 分页获取数值型指标
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/numberQuota")
    public Pager<QuotaEntity> queryNumberQuota(
            @RequestParam(value = "page", required = false, defaultValue = "1") Long page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        IPage<QuotaEntity> pageResult = quotaService.queryNumberQuota(page, pageSize);
        return new Pager<>(pageResult);
    }
}
