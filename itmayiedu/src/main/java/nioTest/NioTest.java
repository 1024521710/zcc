package nioTest;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class NioTest {

    //使用直接缓冲区完成文件的复制(內存映射文件)
    @Test
    public void test2() throws Exception {
        long startTime = System.currentTimeMillis();
        FileChannel inChannel = FileChannel.open(Paths.get("f://1.txt"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("f://2.txt"), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        // 映射文件
        MappedByteBuffer inMapperBuff = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        MappedByteBuffer outMapperBuff = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());
        // 直接对缓冲区进行数据读写操作
        byte[] dst = new byte[inMapperBuff.limit()];
        inMapperBuff.get(dst);
        outMapperBuff.put(dst);
        outChannel.close();
        inChannel.close();
        long endTime = System.currentTimeMillis();
        System.out.println("内存映射文件耗时:"+(endTime-startTime));
    }
    // 1.利用通道完成文件复制(非直接缓冲区)
    @Test
    public void test1() throws Exception {
        long startTime = System.currentTimeMillis();
        FileInputStream fileInputStream = new FileInputStream("f://3.txt");
        FileOutputStream fileOutputStream = new FileOutputStream("f://4.txt");
        //获取通道
        FileChannel inChannel = fileInputStream.getChannel();
        FileChannel outChannel = fileOutputStream.getChannel();
        //分配指定的缓冲区大小
        ByteBuffer allocate = ByteBuffer.allocate(1024);
        while(inChannel.read(allocate)!=-1){
            //切换到读取模式
            allocate.flip();
            outChannel.write(allocate);
            //清空缓冲区
            allocate.clear();
            fileInputStream.close();
            fileOutputStream.close();
            long endTime = System.currentTimeMillis();
            System.out.println("非缓冲区:"+(endTime-startTime));
        }
    }
    //分散读取与聚集写入
    @Test
    public void test3() throws IOException {
        //随机访问 rw：读写模式
        RandomAccessFile raf1 = new RandomAccessFile("f://test.txt","rw");
        //获取通道
        FileChannel channel = raf1.getChannel();
        //指定缓冲区大小
        ByteBuffer buf1 = ByteBuffer.allocate(100);
        ByteBuffer buf2 = ByteBuffer.allocate(1024);
        //分散读取
        ByteBuffer[] bfus={buf1,buf2};
        channel.read(bfus);
        //循环读取
        for (ByteBuffer byteBuff:bfus){
            //切换为读取模式
            byteBuff.flip();
        }
//        System.out.println(new String(bfus[0],0,bfus[0].limit()));
          System.out.println("------------------分算读取线分割--------------------");
//       System.out.println(new String(bfus[1].array(), 0, bfus[1].limit()));
       //聚集写入
        RandomAccessFile raf2 = new RandomAccessFile("f://6.txt","rw");
        FileChannel channel2 = raf2.getChannel();
        channel2.write(bfus);
    }
}
