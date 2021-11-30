package com.chm.bledemo.utils;

/**
 * Created by _H_JY on 2019/3/7.
 */
public class Tools {

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv + " ");
        }
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 将两个byte数组合并为一个
     * @param data1  要合并的数组1
     * @param data2  要合并的数组2
     * @return 合并后的新数组
     */
    public static byte[] mergeBytes(byte[] data1, byte[] data2) {
        if (data1 ==null) {
            data1 = new byte[0];
        }

        if (data2 ==null) {
            data2 = new byte[0];
        }

        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }


    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
     */
    public static int bytesToIntHighAhead(byte[] src, int offset) {
        return ((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF);
    }


    /**
     * 两个字节byte转int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
     */
    public static int towBytesToIntHighAhead(byte head, byte tail) {
        byte[] tempByte = new byte[4];
        tempByte[0] = 0x00;
        tempByte[1] = 0x00;
        tempByte[2] = head;
        tempByte[3] = tail;


        return ((tempByte[0] & 0xFF) << 24)
                | ((tempByte[1] & 0xFF) << 16)
                | ((tempByte[2] & 0xFF) << 8)
                | (tempByte[3] & 0xFF);
    }

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。  和bytesToInt2（）配套使用
     */
    public static byte[] intToBytes(int value)
    {
        byte[] src = new byte[4];
        src[0] = (byte) ((value>>24) & 0xFF);
        src[1] = (byte) ((value>>16)& 0xFF);
        src[2] = (byte) ((value>>8)&0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /**
     *
     * @param src
     * @param offset
     * @return
     */
    public static int bytesToCRCInt(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF)<<16) | ((src[offset+1] & 0xFF)<<8) | (src[offset+2] & 0xFF));
        return value;
    }





}
