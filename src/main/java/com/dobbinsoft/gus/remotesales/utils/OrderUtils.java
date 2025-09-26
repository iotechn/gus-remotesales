package com.dobbinsoft.gus.remotesales.utils;

import com.dobbinsoft.gus.remotesales.data.po.OrderPO;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderUtils {

    public static String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        String randomDigits = String.format("%05d", (int)(System.nanoTime() % 10000));
        return timestamp + randomDigits;
    }

    public static String generateRefundNo(OrderPO order) {
        String refundNo = "";
        Integer refundCount = order.getTotalRefundCount();
        if (refundCount == null || refundCount == 0) {
            refundNo = order.getOrderNo() + "0001";

        } else if (refundCount >= 1) {
            refundCount = refundCount + 1;
            refundNo = order.getOrderNo() + String.format("%04d", refundCount);
        }
        return refundNo;
    }

}
