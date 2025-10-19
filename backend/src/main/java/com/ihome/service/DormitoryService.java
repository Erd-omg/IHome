package com.ihome.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ihome.entity.Bed;
import com.ihome.entity.Dormitory;
import com.ihome.entity.DormitoryAllocation;
import com.ihome.mapper.BedMapper;
import com.ihome.mapper.DormitoryAllocationMapper;
import com.ihome.mapper.DormitoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DormitoryService {
    private final DormitoryMapper dormitoryMapper;
    private final BedMapper bedMapper;
    private final DormitoryAllocationMapper allocationMapper;

    public DormitoryService(DormitoryMapper dormitoryMapper, BedMapper bedMapper, DormitoryAllocationMapper allocationMapper) {
        this.dormitoryMapper = dormitoryMapper;
        this.bedMapper = bedMapper;
        this.allocationMapper = allocationMapper;
    }

    public List<Dormitory> listDormitories(String buildingId, String status) {
        return dormitoryMapper.selectList(
                Wrappers.<Dormitory>lambdaQuery()
                        .eq(buildingId != null, Dormitory::getBuildingId, buildingId)
                        .eq(status != null, Dormitory::getStatus, status)
        );
    }

    public List<Bed> listBeds(String dormitoryId, String status) {
        return bedMapper.selectList(
                Wrappers.<Bed>lambdaQuery()
                        .eq(Bed::getDormitoryId, dormitoryId)
                        .eq(status != null, Bed::getStatus, status)
        );
    }

    @Transactional
    public void chooseBed(String studentId, String bedId) {
        Bed bed = bedMapper.selectById(bedId);
        if (bed == null) throw new IllegalStateException("床位不存在");
        if (!"可用".equals(bed.getStatus())) throw new IllegalStateException("床位已被占用");

        Dormitory dorm = dormitoryMapper.selectById(bed.getDormitoryId());
        if (dorm == null) throw new IllegalStateException("宿舍不存在");
        if ("已满".equals(dorm.getStatus())) throw new IllegalStateException("宿舍已满");

        // 插入分配记录
        DormitoryAllocation alloc = new DormitoryAllocation();
        alloc.setStudentId(studentId);
        alloc.setBedId(bedId);
        alloc.setCheckInDate(LocalDate.now());
        alloc.setStatus("在住");
        allocationMapper.insert(alloc);

        // 更新床位状态
        bed.setStatus("已占用");
        bedMapper.updateById(bed);

        // 更新宿舍人数与状态
        dorm.setCurrentOccupancy(dorm.getCurrentOccupancy() + 1);
        if (dorm.getCurrentOccupancy() >= dorm.getBedCount()) {
            dorm.setStatus("已满");
        }
        dormitoryMapper.updateById(dorm);
    }

    @Transactional
    public void checkout(String studentId) {
        List<DormitoryAllocation> allocs = allocationMapper.selectList(
                Wrappers.<DormitoryAllocation>lambdaQuery()
                        .eq(DormitoryAllocation::getStudentId, studentId)
                        .eq(DormitoryAllocation::getStatus, "在住")
        );
        if (allocs.isEmpty()) throw new IllegalStateException("当前未在住");
        
        // 处理所有在住的分配记录
        for (DormitoryAllocation alloc : allocs) {
            // 释放床位
            Bed bed = bedMapper.selectById(alloc.getBedId());
            if (bed != null) {
                bed.setStatus("可用");
                bedMapper.updateById(bed);
                Dormitory dorm = dormitoryMapper.selectById(bed.getDormitoryId());
                if (dorm != null) {
                    dorm.setCurrentOccupancy(Math.max(0, dorm.getCurrentOccupancy() - 1));
                    if (dorm.getCurrentOccupancy() < dorm.getBedCount() && "已满".equals(dorm.getStatus())) {
                        dorm.setStatus("可用");
                    }
                    dormitoryMapper.updateById(dorm);
                }
            }

            // 更新分配记录
            alloc.setStatus("已退宿");
            alloc.setCheckOutDate(LocalDate.now());
            allocationMapper.updateById(alloc);
        }
    }
}


