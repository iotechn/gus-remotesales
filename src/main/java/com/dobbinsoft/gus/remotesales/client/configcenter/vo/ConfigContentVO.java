package com.dobbinsoft.gus.remotesales.client.configcenter.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ConfigContentVO {

    private Brand brand;
    private Secret secret;
    private Deposit deposit;
    private Autocomplete autocomplete;
    private StockVisibility stockVisibility;
    private ModifyPrice modifyPrice;
    private Markdown markdown;
    private SplitPayment splitPayment;
    private DeliverySelection deliverySelection;
    private Refund refund;
    private OrderExpiry orderExpiry;
    private ResetDelivery resetDelivery;
    private LogisticsAutoConfirm logisticsAutoConfirm;
    private PickupAutoConfirm pickupAutoConfirm;
    private QrExpiry qrExpiry;
    private Fapiao fapiao;
    private ReceiptNo receiptNo;
    private Extra extra;

    private static final String ENABLE_TURE_STR = "1";

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Brand {
        private String logo;
        private String headImage;
        private String agentId;
        private String brandName;
        private String favicon;
        private String shareImage;
        private String h5ShareTitle;
        private String pageBaseUrl;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Secret {
        private String wechatAppId;
        private String wechatAppSecret;
        private String wecomCorpId;
        private String wecomCorpSecret;
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Deposit {
        private Boolean enabled;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Autocomplete {
        private Boolean enabled;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class StockVisibility {
        private Boolean enabled;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ModifyPrice {
        private Boolean enabled;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Markdown {
        private Boolean enabled;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class SplitPayment {
        private Boolean enabled;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DeliverySelection {
        private Boolean enabled;
        private DeliverySelectionType type;
    }

    public enum DeliverySelectionType {
        SA,
        CUSTOMER
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Refund {
        private Boolean enabled;
        private String refundApproverWwid;
        private String refundApproverEmail;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class OrderExpiry {
        private Boolean enabled;
        private Integer expiryTime;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ResetDelivery {
        private Boolean enabled;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class LogisticsAutoConfirm {
        private Boolean enabled;
        private Integer autoTime;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class PickupAutoConfirm {
        private Boolean enabled;
        private Integer autoTime;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class QrExpiry {
        private Boolean enabled;
        private Integer expiryTime;
    }

    /**
     * 发票
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Fapiao {
        private Boolean enabled;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ReceiptNo {
        private Boolean enabled;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Extra {
        private Boolean manualUploadImgEnabled;
        private Boolean shippingInsuranceEnabled;
        private String logisticsOptionText;
        private String shippingInsurancePercentage;
    }
}
