import javax.net.ssl.*;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public abstract class SSLCommon {

    protected ByteBuffer dataReceiveEncrypt;
    protected ByteBuffer dataReceiveDecrypt;
    protected ByteBuffer dataSendDecrypt;
    protected ByteBuffer dataSendEncrypt;

    protected ExecutorService executor = Executors.newSingleThreadExecutor();

    protected void initContext(SSLContext context) throws Exception {

        // Create KeyManager
        String keyFilePath = "resources/selfSignedKS.jks";
        char[] keystorePassword = "sh<H7z>/ry?UZ;s9".toCharArray();

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyFilePath), keystorePassword);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
        kmf.init(ks, keystorePassword);

        // Create TrustManager
        String trustFilePath = "resources/trustStore.jks";
        char[] passwordTrust = ";q8J}bq`txAfMc_-".toCharArray();

        KeyStore trustStore = KeyStore.getInstance("JKS");
        InputStream trustStoreIS = new FileInputStream(trustFilePath);
        try {
            trustStore.load(trustStoreIS, passwordTrust);
        } finally {
            if (trustStoreIS != null) {
                trustStoreIS.close();
            }
        }
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);

        context.init(kmf.getKeyManagers(), trustFactory.getTrustManagers(), new SecureRandom());
    }

    protected void doHandshake(SSLEngine engine, SocketChannel socketChannel) {

        int bufferSize = engine.getSession().getApplicationBufferSize();
        ByteBuffer appData = ByteBuffer.allocate(bufferSize);
        ByteBuffer peerAppData = ByteBuffer.allocate(bufferSize);
        ByteBuffer b;

        dataSendDecrypt.clear();
        dataReceiveDecrypt.clear();

        HandshakeStatus hsStatus = engine.getHandshakeStatus();

        SSLEngineResult result = null;

        // While not successful
        while(hsStatus != HandshakeStatus.FINISHED && hsStatus != HandshakeStatus.NOT_HANDSHAKING) {

            // ---------- NEED TO DECODE ----------
            if (hsStatus == HandshakeStatus.NEED_UNWRAP) {
                try {
                    if (socketChannel.read(dataReceiveDecrypt) < 0) {
                        try {
                            engine.closeInbound();
                            engine.closeOutbound();
                            hsStatus = engine.getHandshakeStatus();
                        } catch (SSLException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Clear
                dataReceiveDecrypt.flip();

                try {
                    result = engine.unwrap(dataReceiveDecrypt, peerAppData);
                    dataReceiveDecrypt.compact();
                    hsStatus = result.getHandshakeStatus();
                } catch (SSLException e) {
                    e.printStackTrace();
                }

                switch (result.getStatus()) {
                    case OK:
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
                        try {
                            dataSendDecrypt.flip();
                            while (dataSendDecrypt.hasRemaining()) {
                                socketChannel.write(dataSendDecrypt);
                            }
                            dataReceiveDecrypt.clear();
                        } catch (Exception e) {
                            System.err.println("Failed to send server's CLOSE message due to socket channel's failure.");
                            hsStatus = engine.getHandshakeStatus();
                        }
                        break;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                }
            }

            // ---------- NEED TO ENCODE ----------
            else if (hsStatus == HandshakeStatus.NEED_WRAP) {
                dataSendDecrypt.clear();
                try {
                    result = engine.wrap(appData, dataSendDecrypt);
                    hsStatus = result.getHandshakeStatus();
                } catch (SSLException e) {
                    System.err.println("Error in SSL connection");
                }

                switch (result.getStatus()) {
                    case OK:
                        dataSendDecrypt.flip();
                        while (dataSendDecrypt.hasRemaining()) {
                            try {
                                socketChannel.write(dataSendDecrypt);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
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
                        try {
                            dataSendDecrypt.flip();
                            while (dataSendDecrypt.hasRemaining()) {
                                socketChannel.write(dataSendDecrypt);
                            }
                            dataReceiveDecrypt.clear();
                        } catch (Exception e) {
                            System.err.println("Failed to send Close message to the SSLServer");
                            hsStatus = engine.getHandshakeStatus();
                        }
                        break;
                    default:
                        System.err.println("illegal status");                }
            }

            // ---------- Assign a thread to the runnable engine task ----------
            else if (hsStatus == HandshakeStatus.NEED_TASK) {
                Runnable task;
                while ((task = engine.getDelegatedTask()) != null) {
                    executor.execute(task);
                }
                hsStatus = engine.getHandshakeStatus();
            }

            else
                System.err.println("illegal state");
        }
    }

}


