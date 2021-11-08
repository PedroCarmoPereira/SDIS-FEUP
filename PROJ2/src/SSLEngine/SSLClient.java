import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SSLClient extends SSLCommon {

	private String ipAddr;
	private int port;
    private SSLEngine engine;
    private SocketChannel socketChannel;

    public SSLClient(String protocol, String ipAddr, int port) throws Exception  {
    	this.ipAddr = ipAddr;
    	this.port = port;

        SSLContext context = SSLContext.getInstance(protocol);

        initContext(context);

        engine = context.createSSLEngine(ipAddr, port);
        engine.setUseClientMode(true);

        SSLSession session = engine.getSession();
        dataSendEncrypt = ByteBuffer.allocate(128000);
        dataSendDecrypt = ByteBuffer.allocate(session.getPacketBufferSize());
        dataReceiveEncrypt = ByteBuffer.allocate(128000);
        dataReceiveDecrypt = ByteBuffer.allocate(session.getPacketBufferSize());


    }

    public void connectToSv() throws Exception {
    	socketChannel = SocketChannel.open();
    	socketChannel.configureBlocking(false);
        
        socketChannel.connect(new InetSocketAddress(ipAddr, port));
        socketChannel.finishConnect();

    	engine.beginHandshake();

        doHandshake(engine, socketChannel);
    }

    public void shutdown() throws IOException {
        engine.closeOutbound();
        doHandshake(engine, socketChannel);
        socketChannel.close();

        // Shutdown runnable thread
        executor.shutdown();
    }

    public void writeObject(SocketChannel socketChannel, SSLEngine engine, Object obj) throws IOException {

        byte[] objBytes = SSLUtils.serialize(obj);

        if (objBytes.length > 16000) {
            System.err.println("Warning! Message larger than 16KB, will crash!");
        }

        dataSendEncrypt.clear();
        dataSendEncrypt.put(objBytes);
        dataSendEncrypt.flip();

        ByteBuffer b;

        while (dataSendEncrypt.hasRemaining()) {

            dataSendDecrypt.clear();

            // Encrypt data wrap(source, destitation)
            SSLEngineResult result = engine.wrap(dataSendEncrypt, dataSendDecrypt);
            switch (result.getStatus()) {
                case OK:
                    // Buffer clear
                    dataSendDecrypt.flip();
                    while (dataSendDecrypt.hasRemaining()) {
                        socketChannel.write(dataSendDecrypt);
                        //System.out.println("writing...!");
                    }

                    break;
                case BUFFER_OVERFLOW:
                    int appSize = engine.getSession().getApplicationBufferSize();
                    b = ByteBuffer.allocate(appSize + dataSendDecrypt.position());
                    dataSendDecrypt.flip();
                    b.put(dataSendDecrypt);
                    // retry the operation.
                    dataSendDecrypt = b;
                    System.out.println("overflow!");
                    break;
                case BUFFER_UNDERFLOW:
                    int netSize = engine.getSession().getPacketBufferSize();
                    // Resize buffer if needed.
                    if (netSize > dataSendDecrypt.capacity()) {
                        b = ByteBuffer.allocate(netSize);
                        dataSendEncrypt.flip();
                        b.put(dataSendEncrypt);
                        dataSendEncrypt = b;
                    }
                    // Obtain more inbound network data for src,
                    // then retry the operation.
                    break;

                case CLOSED:
                    engine.closeOutbound();
                    doHandshake(engine, socketChannel);
                    socketChannel.close();
                    return;
                default:
                    System.err.println("SSL status error!");
            }
        }

    }

    public SSLEngine getEngine() {
        return this.engine;
    }

    public SocketChannel getSocketChannel() {
        return this.socketChannel;
    }
}
