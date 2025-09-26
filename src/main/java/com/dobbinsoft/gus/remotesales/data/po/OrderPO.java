package com.dobbinsoft.gus.remotesales.data.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dobbinsoft.gus.remotesales.client.gus.logistics.model.LpCode;
import com.dobbinsoft.gus.remotesales.data.enums.PayStatusEnum;
import com.dobbinsoft.gus.remotesales.data.po.base.SoftDeleteMyBatisBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@TableName("rs_order")
@Schema(description = "订单表")
public class OrderPO extends SoftDeleteMyBatisBaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单编号")
    private String orderNo;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.OrderTypeEnum
     */
    @Schema(description = "1:订单 2:订金单")
    private Integer type;

    @Schema(description = "CA员工的WWID")
    private String caWwid;

    @Schema(description = "CA员工姓名")
    private String caName;

    @Schema(description = "CA员工职位")
    private String caPosition;

    @Schema(description = "CA员工头像URL")
    private String caAvatar;

    @Schema(description = "区域ID")
    private String regionId;

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "外部用户ID")
    private String customerExternalUserid;

    @Schema(description = "用户的UnionID")
    private String customerUnionid;

    @Schema(description = "用户OpenID")
    private String customerOpenid;

    @Schema(description = "用户昵称")
    private String customerNickname;

    @Schema(description = "用户真实姓名")
    private String customerName;

    @Schema(description = "用户头像URL")
    private String customerAvatar;

    @Schema(description = "用户性别 0-未知 1-男性 2-女性")
    private Integer customerGender;

    @Schema(description = "用户手机号码")
    private String customerMobile;

    @Schema(description = "最后一次打开详情的用户OpenID")
    private String customerBrowseOpenid;

    @Schema(description = "最后一次打开详情的用户UnionID")
    private String customerBrowseUnionid;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.OrderStatusEnum
     */
    @Schema(description = "订单状态（0=待转发, 10=待付款, 20=重置物流中, 30=待发货, 40=待提货, 50=待收货, 60=已完成, 70=已过期, -1=未知）")
    private Integer status;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.PayStatusEnum
     */
    @Schema(description = "支付状态（0=未支付, 1=已支付）")
    private Integer payStatus;

    @Schema(description = "支付日期")
    private ZonedDateTime payTime;

    /**
     * 老系统中这个字段表示，设置物流的时间，这个命名歧义巨大。
     *
     * 由于新系统无需记录此时间，所以 这里统一定义为订单创建时间。
     */
    @Schema(description = "订单提交日期")
    private ZonedDateTime submitTime;

    /**
     * 枚举 com.dobbinsoft.gus.remotesales.data.enums.PayTypeEnum
     */
    @Schema(description = "支付类型")
    private String payType;

    /**
     * 枚举 com.dobbinsoft.gus.remotesales.data.enums.PayModeSetEnum
     */
    @Schema(description = "支付方式（如支付宝、微信支付等）")
    private String payMethod;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "订单总金额")
    private BigDecimal amount;

    @Schema(description = "支付单号")
    private String payNo;

    @Schema(description = "")
    private String paymentProviderId;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.DeliveryMethodEnum
     */
    @Schema(description = "配送方式（0=物流发货, 1=客户自提）")
    private Integer deliveryMethod;

    @Schema(description = "配送方式选择时间（最后）")
    private ZonedDateTime deliveryMethodChooseTime;

    @Schema(description = "重置过物流次数")
    private Integer resetDeliverMethodTimes;

    /**
     * 配送地址JSON对象
     */
    @Schema(description = "配送地址")
    private String address;

    @Schema(description = "物流商code")
    private LpCode logisticsCompanyCode;

    @Schema(description = "物流公司名称")
    private String logisticsCompany;

    @Schema(description = "物流单号")
    private String logisticsNo;

    @Schema(description = "物流预估费用")
    private BigDecimal logisticsEstimatedPrice;

    @Schema(description = "发货时间")
    private ZonedDateTime deliveryTime;

    @Schema(description = "提货店铺ID")
    private String pickupStoreId;

    @Schema(description = "实际收货日期")
    private ZonedDateTime receiveTime;

    @Schema(description = "发票号码")
    private String billNo;

    /**
     * 票据内容 JSON
     */
    @Schema(description = "发票内容")
    private String bill;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.ReceiveType
     */
    @Schema(description = "收货方式（0=客人主动, 1=自动收货）")
    private Integer receiveType;

    @Schema(description = "提货店铺名称")
    private String pickupStoreName;

    @Schema(description = "提货扫码CA ID")
    private String pickupCaWwid;

    @Schema(description = "提货扫码姓名")
    private String pickupCaName;

    @Schema(description = "提货日期")
    private ZonedDateTime pickupTime;

    @Schema(description = "支付时用户的OpenID")
    private String payCustomerOpenid;

    @Schema(description = "支付时用户的UnionID")
    private String payCustomerUnionid;

    @Schema(description = "子商户ID（如有）")
    private String subMchId;

    @Schema(description = "是否审核（1=已审核，0=未审核）")
    private Integer audited;

    @Schema(description = "最后一次退款单号")
    private String lastRefundNo;

    @Schema(description = "退款次数")
    private Integer totalRefundCount;

    @Schema(description = "退款总金额")
    private BigDecimal totalRefund;

    @Schema(description = "POS Number")
    private String posNumber;

    @Schema(description = "小票图片")
    private String receipt;
    @Schema(description = "小票单号")
    private String receiptNumber;
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "内部备注")
    private String innerRemark;


    public boolean pickupStoreShow() {
        return StringUtils.isNotEmpty(this.getPickupStoreId()) || this.getDeliveryMethod() == null || this.getPayStatus().equals(PayStatusEnum.UNPAID.getCode());
    }


}
