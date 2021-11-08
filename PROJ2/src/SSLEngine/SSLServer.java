import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

public class SSLServer extends SSLCommon {
    private SSLContext context;
    private Selector selector;

    public SSLServer(String protocol, String hostAddress, int port) throws Exception {
        context = SSLContext.getInstance(protocol);
        
        initContext(context);

        // create a temporary session to alocate adequate size to buffers
        SSLSession tempSession = context.createSSLEngine().getSession();
        dataSendEncrypt = ByteBuffer.allocate(tempSession.getApplicationBufferSize());
        dataSendDecrypt = ByteBuffer.allocate(tempSession.getPacketBufferSize());
        dataReceiveEncrypt = ByteBuffer.allocate(tempSession.getApplicationBufferSize());
        dataReceiveDecrypt = ByteBuffer.allocate(tempSession.getPacketBufferSize());
        tempSession.invalidate();

        selector = SelectorProvider.provider().openSelector();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(hostAddress, port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        
    }

    public Object start() throws Exception {
        ByteBuffer returnBytes = null;
        Boolean isActive = true;
        Object returnObject = null;

    	//System.out.println("Waiting for new connections...");

        while (isActive) {
            selector.select();
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                SelectionKey key = selectedKeys.next();
                selectedKeys.remove();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    returnBytes = readObject((SocketChannel) key.channel(), (SSLEngine) key.attachment());
                    isActive = false;
                    returnObject = SSLUtils.deserialize(returnBytes.array());

                    //System.out.println(((Message) returnObject).getName());

                    ((SSLEngine) key.attachment()).closeOutbound();
                    this.doHandshake((SSLEngine) key.attachment(), (SocketChannel) key.channel());
                    key.channel().close();
                }
            }
        }
        returnObject = SSLUtils.deserialize(returnBytes.array());
        return returnObject;
    }

    private void accept(SelectionKey key) throws Exception {

    	System.out.println("New connection request!");

        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        socketChannel.configureBlocking(false);

        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(false);
        engine.beginHandshake();

        doHandshake(engine, socketChannel);
        socketChannel.register(selector, SelectionKey.OP_READ, engine);
    }

    private ByteBuffer readObject(SocketChannel socketChannel, SSLEngine engine) throws IOException {
        ByteBuffer returnBytes = null;
        ByteBuffer b;

        dataReceiveDecrypt.clear();
        int bytesRead = socketChannel.read(dataReceiveDecrypt);
        if (bytesRead > 0) {
            dataReceiveDecrypt.flip();
            while (dataReceiveDecrypt.hasRemaining()) {
                dataReceiveEncrypt.clear();
                SSLEngineResult result = engine.unwrap(dataReceiveDecrypt, dataReceiveEncrypt);
                switch (result.getStatus()) {
                    case OK:
                        dataReceiveEncrypt.flip();
                        returnBytes = dataReceiveEncrypt;

                        break;
                    case BUFFER_OVERFLOW:
                        int appSize = engine.getSession().getApplicationBufferSize();
                        b = ByteBuffer.allocate(appSize + dataSendDecrypt.position());
                        dataSendDecrypt.flip();
                        b.put(dataSendDecrypt);
                        // retry the operation.
                        dataSendDecrypt = b;
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
                        System.out.println("Client wants to close connection...");
                        engine.closeOutbound();
                        doHandshake(engine, socketChannel);
                        socketChannel.close();
                        System.out.println("Goodbye client!");
                        return returnBytes;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                }
            }

            //writeMessage(socketChannel, engine, "Hello! I am your server!");

        } else if (bytesRead < 0) {
            System.err.println("Received end of stream. Will try to close connection with client...");
            try {
                engine.closeInbound();
            } catch (Exception e) {
                System.err.println("Closing inbound, didn't received the proper SSL/TLS close notification ");
            }
            engine.closeOutbound();
            doHandshake(engine, socketChannel);
            socketChannel.close();

        }

        return returnBytes;
    }
}