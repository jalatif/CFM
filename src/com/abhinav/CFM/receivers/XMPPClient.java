package com.abhinav.CFM.receivers;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.abhinav.CFM.R;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import java.util.ArrayList;

public class XMPPClient extends Activity {

    private ArrayList<String> messages = new ArrayList();
    private Handler mHandler = new Handler();
    private SettingsDialog mDialog;
    private EditText mRecipient;
    private EditText mSendText;
    private ListView mList;
    private XMPPConnection connection;

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.xmpp);

        mRecipient = (EditText) this.findViewById(R.id.etRecipient);
        mSendText = (EditText) this.findViewById(R.id.etMessage);
        mList = (ListView) this.findViewById(R.id.lvMessages);
        setListAdapter();

        // Dialog for getting the xmpp settings
        mDialog = new SettingsDialog(this);

        // Set a listener to show the settings dialog
        Button setup = (Button) this.findViewById(R.id.bSetup);
        setup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mHandler.post(new Runnable() {
                    public void run() {
                        mDialog.show();
                    }
                });
            }
        });

        String host = "54.200.222.190";//"talk.google.com";
        String port = "5222";
        String service = "chat.jalatif.com";
        final String username = "jalatif";//"abhinavsharma3105";
        final String password = "poptfs";//"seandevlinsaboteur";

        // Create a connection
        ConnectionConfiguration connConfig =
                new ConnectionConfiguration(host, Integer.parseInt(port), service);
        connection = new XMPPConnection(connConfig);
        Thread a = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connection.connect();
                    Log.i("XMPPClient", "[SettingsDialog] Connected to " + connection.getHost());
                } catch (XMPPException ex) {
                    Log.e("XMPPClient", "[SettingsDialog] Failed to connect to " + connection.getHost());
                }
                try {
                    connection.login(username, password);
                    Log.i("XMPPClient", "Logged in as " + connection.getUser());

                    // Set the status to available
                    Presence presence = new Presence(Presence.Type.available);
                    connection.sendPacket(presence);
                } catch (XMPPException ex) {
                    Log.e("XMPPClient", "[SettingsDialog] Failed to log in as " + username);
                }
            }
        });
        a.start();
        try {
            a.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setConnection(connection);
        // Set a listener to send a chat text message
        Button send = (Button) this.findViewById(R.id.bSend);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String to = mRecipient.getText().toString();
                String text = mSendText.getText().toString();

                Log.i("XMPPClient", "Sending text [" + text + "] to [" + to + "]");
                Message msg = new Message(to, Message.Type.chat);
                msg.setBody(text);
                System.out.println("Jalatif Message ---- " + msg + " Username = " + to + " String = " + text);
                System.out.println("Jalatif Connected to " + connection.getUser() + " and Port = " + connection.getPort());
                connection.sendPacket(msg);
                messages.add(connection.getUser() + ":");
                messages.add(text);
                setListAdapter();
            }
        });
    }

    /**
     * Called by Settings dialog when a connection is establised with the XMPP server
     *
     * @param connection
     */
    public void setConnection(XMPPConnection connection) {
        System.out.println("Jalatif Connected to " + connection.getUser() + " and Port = " + connection.getPort());
        this.connection = connection;
        if (connection != null) {
            // Add a packet listener to get messages sent to us
            PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
            connection.addPacketListener(new PacketListener() {
                public void processPacket(Packet packet) {
                    Message message = (Message) packet;
                    if (message.getBody() != null) {
                        String fromName = StringUtils.parseBareAddress(message.getFrom());
                        Log.i("XMPPClient", "Got text [" + message.getBody() + "] from [" + fromName + "]");
                        messages.add(fromName + ":");
                        messages.add(message.getBody());
                        // Add the incoming message to the list view
                        mHandler.post(new Runnable() {
                            public void run() {
                                setListAdapter();
                            }
                        });
                    }
                }
            }, filter);
        }
    }

    private void setListAdapter() {
        mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages));
        /*ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.two_line_list_item, R.id.lvMessages,
                messages);
        mList.setAdapter(adapter);*/
    }

    public void signOut()
    {
        connection.disconnect();
    }

}