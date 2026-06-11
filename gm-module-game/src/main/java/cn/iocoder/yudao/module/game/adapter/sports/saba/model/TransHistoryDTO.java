package cn.iocoder.yudao.module.game.adapter.sports.saba.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
    public  class TransHistoryDTO {

        /**
         * 操作类型
         */
        private String action;

        /**
         * 操作时间
         */
        private LocalDateTime actionDate;

        /**
         * 投注金额
         */
        private Double stake;

        /**
         * 赔率
         */
        private Double odds;

        /**
         * 状态
         */
        private Integer status;

        /**
         * 输赢金额
         */
        private Double winlostAmount;

        /**
         * 结算时间
         */
        private LocalDateTime winlostDate;

        /**
         * 操作ID
         */
        private String operationId;

        /**
         * 入账金额
         */
        private Double creditAmount;

        /**
         * 出账金额
         */
        private Double debitAmount;

        /**
         * 是否重试
         */
        private Boolean inRetry;

        /**
         * 是否达到重试上限
         */
        private Boolean reachRetryLimit;
    }
