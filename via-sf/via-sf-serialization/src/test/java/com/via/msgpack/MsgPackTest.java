package com.via.msgpack;

import org.msgpack.MessagePack;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by stereo on 16-8-5.
 */
public class MsgPackTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        MsgPackTest test = new MsgPackTest();
        test.msgpacktest(args[0]);
    }

    public void msgpacktest(String file) throws IOException, InterruptedException {
        //传输包
        Packet packet = new Packet();
        packet.setId("1-seq");
        packet.setType((byte) 0x01);
        packet.setState((byte) 0x02);
        packet.setInterfaceName("调用接口");
        packet.setMethod("调用方法");

        Map<String, Object> p1 = new HashMap<String, Object>();
        p1.put("p1", new UserInfo());

        Map<Object, String> p2 = new HashMap<Object, String>();
        p2.put(new UserInfo(), "p2");

        Map<Integer, Object> p3 = new HashMap<Integer, Object>();
        p3.put(3, new UserInfo());

        Map<Integer, Long> p4 = new HashMap<Integer, Long>();
        p4.put(3, 4l);

        Map<String, List<UserInfo>> p5 = new HashMap<String, List<UserInfo>>();
        List<UserInfo> list = new ArrayList<UserInfo>();
        list.add(new UserInfo());
        p5.put("p5", list);

        Map<String, Map<Date, UserInfo>> p6 = new HashMap<String, Map<Date, UserInfo>>();
        Map<Date, UserInfo> dateUserInfoMap = new HashMap<Date, UserInfo>();
        dateUserInfoMap.put(new java.sql.Date(System.currentTimeMillis()), new UserInfo());
        p6.put("p6", dateUserInfoMap);

        List<UserInfo> p7 = new ArrayList<UserInfo>();
        p7.add(new UserInfo());

        Set<UserInfo> p8 = new HashSet<UserInfo>();
        p8.add(new UserInfo());

        FileInputStream fileInputStream = new FileInputStream(file);
        ByteBuffer byteBuffer = ByteBuffer.allocate(fileInputStream.available());
        byte[] buf = new byte[1024];
        int len;
        while ((len = fileInputStream.read(buf)) != -1) {
            byteBuffer.put(buf, 0, len);
        }
        Object p9 = byteBuffer.array();

        //装填参数
        Object[] params = {p1, p2, p3, p4, p5, p6, p7, p8, p9};
        packet.setParams(params);
        packet.setResult(new char[]{'A', 'B', 'C', 'D', 'E'});
        packet.setReturnType(char[].class);
        packet.setException(new NullPointerException("is null").toString());

        long start;
        for (; ; ) {
            Thread.sleep(1000);
            System.out.println("======================================= 测试序列化开始 =======================================");
            //msgpack序列化
            MessagePack messagePack = new MessagePack();
            start = System.currentTimeMillis();
            byte[] data = messagePack.write(packet);
            System.out.println("msgpack序列化后 data len is " + data.length);
            System.out.println("msgpack序列化时间：" + (System.currentTimeMillis() - start));

            //msgpack反序列化
            start = System.currentTimeMillis();
            packet = messagePack.read(data, Packet.class);
            //System.out.println("msgpack反序列化后 data is " + packet);
            System.out.println("msgpack反序列化时间：" + (System.currentTimeMillis() - start));

            //hessian序列化
            /*
            start = System.currentTimeMillis();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Hessian2Output hessian2Output = new Hessian2Output(os);
            hessian2Output.writeObject(packet);
            hessian2Output.flush();
            data = os.toByteArray();
            System.out.println("hessian序列化后 data len is "+ data.length);
            System.out.println("hessian序列化时间：" + (System.currentTimeMillis() - start));

            //hessian反序列化
            start = System.currentTimeMillis();
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            Hessian2Input hessian2Input = new Hessian2Input(is);
            packet = (Packet) hessian2Input.readObject();
            //System.out.println("hessian反序列化后 data is " + packet);
            System.out.println("hessian反序列化时间：" + (System.currentTimeMillis() - start));
            */
            System.out.println("======================================= 测试序列化结束 =======================================");
        }
    }
}
