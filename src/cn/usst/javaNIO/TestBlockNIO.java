package cn.usst.javaNIO;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
/*
 * 一、使用NIO 完成网络通信的三个核心：
 *
 * 1、通道(Channel):负责连接
 *      java.nio.channels.Channel 接口：
 *           |--SelectableChannel
 *               |--SocketChannel
 *               |--ServerSocketChannel
 *               |--DatagramChannel
 *
 *               |--Pipe.SinkChannel
 *               |--Pipe.SourceChannel
 *
 * 2.缓冲区(Buffer):负责数据的存取
 *
 * 3.选择器(Selector):是 SelectableChannel 的多路复用器。用于监控SelectableChannel的IO状况
 */

//没用Selector，阻塞型的
public class TestBlockNIO {

    // 客户端
    @Test
    public void client(){
        SocketChannel socketChannel = null;
        FileChannel fileChannel = null;
        try {
            socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9999));
            fileChannel = FileChannel.open(Paths.get("d:/test.pdf"), StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while(fileChannel.read(buffer) != -1){
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
            }
            socketChannel.shutdownOutput();//关闭发送通道，表明发送完毕

            //接受服务端的反馈
            int len = 0;
            while ((len = socketChannel.read(buffer)) != -1){
                buffer.flip();
                System.out.println(new String(buffer.array(),0,len));
                buffer.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    public void server(){
        ServerSocketChannel serverSocketChannel = null;
        FileChannel fileChannel = null;
        SocketChannel socketChannel = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(9999));
            fileChannel = FileChannel.open(Paths.get("d:/test333.pdf"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            socketChannel = serverSocketChannel.accept();
            while (socketChannel.read(byteBuffer) != -1){
                byteBuffer.flip();
                fileChannel.write(byteBuffer);
                byteBuffer.clear();
            }

            //接受完毕 给客户端反馈
            byteBuffer.put("文件已经成功接收！".getBytes());
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
