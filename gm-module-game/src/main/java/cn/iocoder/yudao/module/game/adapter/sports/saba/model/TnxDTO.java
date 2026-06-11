package cn.iocoder.yudao.module.game.adapter.sports.saba.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
    public  class TnxDTO {

        private Integer betType;
        private String userId;
        private LocalDateTime winlostDate;
        private String licenseeTxId;
        private String txId;
        private String refId;
        private Double stake;
        private Double odds;
        private List<TransHistoryDTO> transHistory;
    }