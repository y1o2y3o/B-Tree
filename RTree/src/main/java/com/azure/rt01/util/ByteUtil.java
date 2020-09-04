package com.azure.rt01.util;

public class ByteUtil {
    public static void double2Bytes(double data, byte[] bytes, int offset) {
        long2Bytes(Double.doubleToLongBits(data), bytes, offset);
    }

    public static void long2Bytes(long data, byte[] bytes, int offset) {
        bytes[offset] = (byte) ((data >> 56) & 0xFF);
        bytes[offset + 1] = (byte) ((data >> 48) & 0xFF);
        bytes[offset + 2] = (byte) ((data >> 40) & 0xFF);
        bytes[offset + 3] = (byte) ((data >> 32) & 0xFF);
        bytes[offset + 4] = (byte) ((data >> 24) & 0xFF);
        bytes[offset + 5] = (byte) ((data >> 16) & 0xFF);
        bytes[offset + 6] = (byte) ((data >> 8) & 0xFF);
        bytes[offset + 7] = (byte) (data & 0xFF);
    }

    public static void int2Bytes(int data, byte[] bytes, int offset) {
        bytes[offset] = (byte) ((data >> 24) & 0xFF);
        bytes[offset + 1] = (byte) ((data >> 16) & 0xFF);
        bytes[offset + 2] = (byte) ((data >> 8) & 0xFF);
        bytes[offset + 3] = (byte) (data & 0xFF);
    }

    public static double bytes2Double(byte[] bytes, int offset) {
        return Double.longBitsToDouble(bytes2Long(bytes, offset));
    }

    public static long bytes2Long(byte[] bytes, int offset) {
        long high = (bytes2Int(bytes, offset) & 0xFFFFFFFFL);
        long low = (bytes2Int(bytes, offset + 4) & 0xFFFFFFFFL);
        return (high << 32) | low;
    }

    public static int bytes2Int(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24)
                | ((bytes[offset + 1] & 0xFF) << 16)
                | ((bytes[offset + 2] & 0xFF) << 8)
                | ((bytes[offset + 3] & 0xFF));
    }

    public static void main(String[] args) {
        byte[] bytes = new byte[8];
//        int ans = Integer.MAX_VALUE;
//        int2Bytes(ans, bytes, 0);
//        System.out.println(bytes2Int(bytes, 0) == ans);
        double ans = Double.MIN_VALUE;

        double2Bytes(ans, bytes, 0);
        System.out.println(bytes2Double(bytes, 0) == ans);

    }
}
