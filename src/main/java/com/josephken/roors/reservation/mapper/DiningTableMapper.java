package com.josephken.roors.reservation.mapper;

import com.josephken.roors.reservation.dto.DiningTableDto;
import com.josephken.roors.reservation.dto.DiningTableSummaryDto;
import com.josephken.roors.reservation.entity.DiningTable;

public class DiningTableMapper {

    public static DiningTableSummaryDto toSummaryDto(DiningTable diningTable){
        return DiningTableSummaryDto.builder()
                .id(diningTable.getId())
                .name(diningTable.getName())
                .floor(diningTable.getFloor())
                .capacity(diningTable.getCapacity())
                .build();
    }

    public static DiningTableDto toDto(DiningTable diningTable) {
        return DiningTableDto.builder()
                .id(diningTable.getId())
                .name(diningTable.getName())
                .floor(diningTable.getFloor())
                .capacity(diningTable.getCapacity())
                .status(diningTable.getStatus())
                .build();
    }
}
