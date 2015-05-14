package com.tehnicomsolution.pushtest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.http.HttpServletResponse;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.xmlpull.v1.XmlPullParser;

/**
 * Sample Smack implementation of a client for GCM Cloud Connection Server.
 * <p/>
 * <p>For illustration purposes only.
 */
public class SmackCcsClient
{

    Logger logger = Logger.getLogger("SmackCcsClient");

    public static final String GCM_SERVER = "gcm.googleapis.com";
    public static final int GCM_PORT = 5235;

    public static final String GCM_ELEMENT_NAME = "gcm";
    public static final String GCM_NAMESPACE = "google:mobile:data";

    static Random random = new Random();
    XMPPConnection connection;
    ConnectionConfiguration config;
    PrintWriter writter;

    /**
     * XMPP Packet Extension for GCM Cloud Connection Server.
     */
    class GcmPacketExtension extends DefaultPacketExtension
    {
        String json;

        public GcmPacketExtension(String json)
        {
            super(GCM_ELEMENT_NAME, GCM_NAMESPACE);
            this.json = json;
        }

        public String getJson()
        {
            return json;
        }

        @Override
        public String toXML()
        {
            return String.format("<%s xmlns=\"%s\">%s</%s>", GCM_ELEMENT_NAME,
                    GCM_NAMESPACE, json, GCM_ELEMENT_NAME);
        }

        @SuppressWarnings("unused")
        public Packet toPacket()
        {
            return new Message()
            {
                // Must override toXML() because it includes a <body>
                @Override
                public String toXML()
                {

                    StringBuilder buf = new StringBuilder();
                    buf.append("<message");
                    if (getXmlns() != null)
                    {
                        buf.append(" xmlns=\"").append(getXmlns()).append("\"");
                    }
                    if (getLanguage() != null)
                    {
                        buf.append(" xml:lang=\"").append(getLanguage()).append("\"");
                    }
                    if (getPacketID() != null)
                    {
                        buf.append(" id=\"").append(getPacketID()).append("\"");
                    }
                    if (getTo() != null)
                    {
                        buf.append(" to=\"").append(StringUtils.escapeForXML(getTo())).append("\"");
                    }
                    if (getFrom() != null)
                    {
                        buf.append(" from=\"").append(StringUtils.escapeForXML(getFrom())).append("\"");
                    }
                    buf.append(">");
                    buf.append(GcmPacketExtension.this.toXML());
                    buf.append("</message>");
                    return buf.toString();
                }
            };
        }
    }

    public SmackCcsClient(HttpServletResponse response) throws IOException
    {
        writter = response.getWriter();
        // Add GcmPacketExtension
        ProviderManager.getInstance().addExtensionProvider(GCM_ELEMENT_NAME,
                GCM_NAMESPACE, new PacketExtensionProvider()
        {

            @Override
            public PacketExtension parseExtension(XmlPullParser parser)
                    throws Exception
            {
                String json = parser.nextText();
                GcmPacketExtension packet = new GcmPacketExtension(json);
                return packet;
            }
        });
    }

    /**
     * Returns a random message id to uniquely identify a message.
     * <p/>
     * <p>Note:
     * This is generated by a pseudo random number generator for illustration purpose,
     * and is not guaranteed to be unique.
     */
    public String getRandomMessageId()
    {
        return "m-" + Long.toString(random.nextLong());
    }

    /**
     * Sends a downstream GCM message.
     */
    public void send(String jsonRequest)
    {
        writter.println("Message: " + jsonRequest + "\n");
        Packet request = new GcmPacketExtension(jsonRequest).toPacket();
        connection.sendPacket(request);
    }

    /**
     * Handles an upstream data message from a device application.
     * <p/>
     * <p>This sample echo server sends an echo message back to the device.
     * Subclasses should override this method to process an upstream message.
     */
    public void handleIncomingDataMessage(Map<String, Object> jsonObject)
    {
        String from = jsonObject.get("from").toString();

        // PackageName of the application that sent this message.
        String category = jsonObject.get("category").toString();

        // Use the packageName as the collapseKey in the echo packet
        String collapseKey = "echo:CollapseKey";
        @SuppressWarnings("unchecked")
        Map<String, String> payload = (Map<String, String>) jsonObject.get("data");
        payload.put("ECHO", "Application: " + category);

        // Send an ECHO response back
        String echo = createJsonMessage(from, getRandomMessageId(), payload, collapseKey, null, false);
        send(echo);
    }

    /**
     * Handles an ACK.
     * <p/>
     * <p>By default, it only logs a INFO message, but subclasses could override it to
     * properly handle ACKS.
     */
    public void handleAckReceipt(Map<String, Object> jsonObject)
    {
        String messageId = jsonObject.get("message_id").toString();
        String from = jsonObject.get("from").toString();
        logger.log(Level.INFO, "handleAckReceipt() from: {0}, messageId: {1}", new Object[]{from, messageId});
        writter.println("handleAckReceipt() from: " + from + ", messageId: " + messageId + "\n");
    }

    /**
     * Handles a NACK.
     * <p/>
     * <p>By default, it only logs a INFO message, but subclasses could override it to
     * properly handle NACKS.
     */
    public void handleNackReceipt(Map<String, Object> jsonObject)
    {
        String messageId = jsonObject.get("message_id").toString();
        String from = jsonObject.get("from").toString();
        logger.log(Level.INFO, "handleNackReceipt() from: " + from + ", messageId: " + messageId);
        writter.println("handleNackReceipt() from: " + from + ", messageId: " + messageId + "\n");
    }

    /**
     * Creates a JSON encoded GCM message.
     *
     * @param to             RegistrationId of the target device (Required).
     * @param messageId      Unique messageId for which CCS will send an "ack/nack" (Required).
     * @param payload        Message content intended for the application. (Optional).
     * @param collapseKey    GCM collapse_key parameter (Optional).
     * @param timeToLive     GCM time_to_live parameter (Optional).
     * @param delayWhileIdle GCM delay_while_idle parameter (Optional).
     * @return JSON encoded GCM message.
     */
    public static String createJsonMessage(String to, String messageId, Map<String, String> payload,
                                           String collapseKey, Long timeToLive, Boolean delayWhileIdle)
    {
        Map<String, Object> message = new HashMap<String, Object>();
        message.put("to", to);
        if (collapseKey != null)
        {
            message.put("collapse_key", collapseKey);
        }
        if (timeToLive != null)
        {
            message.put("time_to_live", timeToLive);
        }
        if (delayWhileIdle != null && delayWhileIdle)
        {
            message.put("delay_while_idle", true);
        }
        message.put("message_id", messageId);
        message.put("data", payload);
        return JSONValue.toJSONString(message);
    }

    /**
     * Creates a JSON encoded ACK message for an upstream message received from an application.
     *
     * @param to        RegistrationId of the device who sent the upstream message.
     * @param messageId messageId of the upstream message to be acknowledged to CCS.
     * @return JSON encoded ack.
     */
    public static String createJsonAck(String to, String messageId)
    {
        Map<String, Object> message = new HashMap<String, Object>();
        message.put("message_type", "ack");
        message.put("to", to);
        message.put("message_id", messageId);
        return JSONValue.toJSONString(message);
    }

    /**
     * Connects to GCM Cloud Connection Server using the supplied credentials.
     *
     * @param username GCM_SENDER_ID@gcm.googleapis.com
     * @param password API Key
     * @throws XMPPException
     */
    public void connect(String username, String password) throws XMPPException
    {
        config = new ConnectionConfiguration(GCM_SERVER, GCM_PORT);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
        config.setReconnectionAllowed(true);
        config.setRosterLoadedAtLogin(false);
        config.setSendPresence(false);
        config.setSocketFactory(SSLSocketFactory.getDefault());

        // NOTE: Set to true to launch a window with information about packets sent and received
        config.setDebuggerEnabled(true);

        // -Dsmack.debugEnabled=true
        XMPPConnection.DEBUG_ENABLED = true;

        connection = new XMPPConnection(config);
        connection.connect();

        connection.addConnectionListener(new ConnectionListener()
        {

            @Override
            public void reconnectionSuccessful()
            {
                logger.info("Reconnecting..");
                writter.println("Reconnecting...\n");
            }

            @Override
            public void reconnectionFailed(Exception e)
            {
                logger.log(Level.INFO, "Reconnection failed.. ", e);
                writter.println("Reconnection failed.. \n");
            }

            @Override
            public void reconnectingIn(int seconds)
            {
                logger.log(Level.INFO, "Reconnecting in %d secs", seconds);
                writter.println("Reconnecting in " + seconds + " secs\n");
            }

            @Override
            public void connectionClosedOnError(Exception e)
            {
                logger.log(Level.INFO, "Connection closed on error.");
                writter.println("Connection closed on error.\n");
            }

            @Override
            public void connectionClosed()
            {
                logger.info("Connection closed.");
                writter.println("Connection closed.\n");
            }
        });

        // Handle incoming packets
        connection.addPacketListener(new PacketListener()
        {

            @Override
            public void processPacket(Packet packet)
            {
                logger.log(Level.INFO, "Received: " + packet.toXML());
                writter.println("Received: " + packet.toXML() + "\n");
                Message incomingMessage = (Message) packet;
                GcmPacketExtension gcmPacket =
                        (GcmPacketExtension) incomingMessage.getExtension(GCM_NAMESPACE);
                String json = gcmPacket.getJson();
                try
                {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jsonObject =
                            (Map<String, Object>) JSONValue.parseWithException(json);

                    // present for "ack"/"nack", null otherwise
                    Object messageType = jsonObject.get("message_type");

                    if (messageType == null)
                    {
                        // Normal upstream data message
                        handleIncomingDataMessage(jsonObject);

                        // Send ACK to CCS
                        String messageId = jsonObject.get("message_id").toString();
                        String from = jsonObject.get("from").toString();
                        String ack = createJsonAck(from, messageId);
                        send(ack);
                    }
                    else if ("ack".equals(messageType.toString()))
                    {
                        // Process Ack
                        handleAckReceipt(jsonObject);
                    }
                    else if ("nack".equals(messageType.toString()))
                    {
                        // Process Nack
                        handleNackReceipt(jsonObject);
                    }
                    else
                    {
                        logger.log(Level.WARNING, "Unrecognized message type (%s)",
                                messageType.toString());
                        writter.println("Unrecognized message type (" + messageType.toString() + ")\n");
                    }
                }
                catch (ParseException e)
                {
                    logger.log(Level.SEVERE, "Error parsing JSON " + json, e);
                    writter.println("Error parsing JSON " + json + "\n");
                }
                catch (Exception e)
                {
                    logger.log(Level.SEVERE, "Couldn't send echo.", e);
                    writter.println("Couldn't send echo.\n");
                }
            }
        }, new PacketTypeFilter(Message.class));


        // Log all outgoing packets
        connection.addPacketInterceptor(new PacketInterceptor()
        {
            @Override
            public void interceptPacket(Packet packet)
            {
                logger.log(Level.INFO, "Sent: {0}", packet.toXML());
                writter.println("Sent: {" + packet.toXML() + "}\n");
            }
        }, new PacketTypeFilter(Message.class));

        connection.login(username, password);
    }
    
    public XMPPConnection getConnection()
    {
        return connection;
    }
}