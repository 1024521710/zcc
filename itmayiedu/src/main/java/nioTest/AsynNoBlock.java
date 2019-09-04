package nioTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

public class AsynNoBlock {
}

class Client{
    public static void main(String[] args) throws IOException {
        System.out.println("客户端已启动");
        // 1.创建通道
        SocketChannel open = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8085));
        //切换异步非阻塞
        open.configureBlocking(false);
        //指定缓冲区大小
        ByteBuffer byteBuffer  = ByteBuffer.allocate(1024);
        Scanner sc=new Scanner(System.in);
        while(sc.hasNext()){
            String str=sc.next();
            byteBuffer.put((new Date().toString()+"\n"+str).getBytes());
            // 4.切换读取模式
            byteBuffer.flip();
            open.write(byteBuffer);
            byteBuffer.clear();
        }
        open.close();
    }
}

class Server{
    public static void main(String[] args) throws IOException {
        System.out.println("服务端已启动");
        //创建通道
        ServerSocketChannel open = ServerSocketChannel.open();
        //切换读取模式
        open.configureBlocking(false);
        //绑定链接
        open.bind(new InetSocketAddress(8085));
        //获取选择器
        Selector selector = Selector.open();
        //5.将通道注册到选择器 "并且指定监听接受事件"\
        SelectionKey register = open.register(selector, SelectionKey.OP_ACCEPT);
        // 6. 轮训式 获取选择 "已经准备就绪"的事件
        while(selector.select()>0){
            // 7.获取当前选择器所有注册的"选择键(已经就绪的监听事件)"
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                // 8.获取准备就绪的事件
                SelectionKey sk = it.next();
                // 9.判断具体是什么事件准备就绪
                if (sk.isAcceptable()) {
                    // 10.若"接受就绪",获取客户端连接
                    SocketChannel socketChannel = open.accept();
                    // 11.设置阻塞模式
                    socketChannel.configureBlocking(false);
                    // 12.将该通道注册到服务器上
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }else if(sk.isReadable()){
                    // 13.获取当前选择器"就绪" 状态的通道
                    SocketChannel socketChannel = (SocketChannel) sk.channel();
                    // 14.读取数据
                    ByteBuffer buf = ByteBuffer.allocate(1024);
                    int len = 0;
                    while ((len = socketChannel.read(buf)) > 0) {
                        buf.flip();
                        System.out.println(new String(buf.array(), 0, len));
                        buf.clear();
                    }
                }
                it.remove();
            }
        }
    }
}