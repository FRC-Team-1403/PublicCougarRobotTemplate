package team1403.lib.util.test;

import java.io.IOException;
import java.util.TreeSet;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableValue;
import edu.wpi.first.networktables.NetworkTablesJNI;


/**
 * Helper functions for interacting with NetworkTables in tests.
 */
public final class NetworkTablesTestUtil {
  /**
   * Denotes the NetworkTableInstance.getDefault().
   */
  public static final String kDefaultNetworkName = "DefaultNetworkTableInstance";

  /**
   * Indentation per level.
   */
  public static final String kIndent = "  ";

  /**
   * Render table as a debug string.
   *
   * @param table The table to render.
   * @param prefix Indent prefix within the string for this table content.
   *
   * @return Debug string.
   */
  public static String tableToDebugString(NetworkTable table, String prefix) {
    var buffer = new StringBuilder();
    var indent = kIndent + prefix;

    buffer.append(String.format("%sTABLE '%s'", prefix, table.getPath()));
    // Use TreeSet to sort the set alphabetically
    for (var key : new TreeSet<>(table.getKeys())) {
      var entry = table.getEntry(key);
      var value = toValueDebugString(entry.getValue());
      buffer.append(String.format("\n%s%s: %s = %s",
                                  indent, entry.getType(), entry.getName(),
                                  value));
    }

    // Use TreeSet to sort the set alphabetically
    for (var name : new TreeSet<>(table.getSubTables())) {
      buffer.append('\n');
      buffer.append(tableToDebugString(table.getSubTable(name), indent));
    }
    return buffer.toString();
  }

  /**
   * Render the NetworkTableInstance as a debug string.
   *
   * @param instance The instance to render.
   * @return Rendered debug string.
   */
  @SuppressWarnings("PMD.ConsecutiveAppendsShouldReuse")
  public static String instanceToDebugString(NetworkTableInstance instance) {
    var buffer = new StringBuilder();
    boolean isDefault = (instance == NetworkTableInstance.getDefault()); // NOPMD
    buffer.append(String.format("NetworkTableInstance %d isDefault=%s",
                                instance.hashCode(), isDefault));
    buffer.append(String.format("\n  Connections=%d",
                                instance.getConnections().length));
    for (var conn : instance.getConnections()) {
      buffer.append(
          String.format("\n  %s = %s:%d",
                        conn.remote_id, conn.remote_ip, conn.remote_port));
      if (conn.last_update > 0) {
        buffer.append(
            String.format(" @ %dus ago",
                          NetworkTablesJNI.now() - conn.last_update));
      } else {
        buffer.append(" @ never");
      }
    }
    buffer.append('\n');
    buffer.append(tableToDebugString(instance.getTable(""), kIndent));
    return buffer.toString();
  }

  /**
   * Create a pair of instances connected through the loopback address.
   *
   * <p>If a kDefaultNetworkName is provided for a role then the
   * {@code NetworkTableInstance.getDefault} will be used. Otherwise
   * a new instance will be created.
   *
   * @param clientName the network identity for the "client" instance.
   * @param serverName the network identify for the "server" instance.
   *
   * @return [client, server] network instances connected to one another.
   *
   * @throws java.io.IOException if could not create a connected pair.
   */
  @SuppressWarnings("PMD.CloseResource")
  public static NetworkTableInstance[] createLoopbackPair(
      String clientName, String serverName) throws IOException {
    NetworkTableInstance client;
    if (kDefaultNetworkName.equals(clientName)) {
      client = NetworkTableInstance.getDefault();
      client.stopClient();
      client.stopServer();
    } else {
      client = NetworkTableInstance.create();
      client.setNetworkIdentity(clientName);
    }

    NetworkTableInstance server;
    if (kDefaultNetworkName.equals(serverName)) {
      server = NetworkTableInstance.getDefault();
      server.stopClient();
      server.stopServer();
    } else {
      server = NetworkTableInstance.create();
      server.setNetworkIdentity(serverName);
    }

    int port;
    String loopback = "127.0.0.1";  // NOPMD
    try (var socket = new java.net.ServerSocket(0)) {
      port = socket.getLocalPort();
    } catch (java.io.IOException ioex) {
      throw new IOException("Cannot find unused port", ioex);
    }

    server.startServer(serverName, loopback, port);
    client.startClient(loopback, port);

    waitForClient(server, serverName);
    return new NetworkTableInstance[]{client, server};
  }

  /**
   * Close all the instances.
   *
   * @param instances instances to close
   */
  public static void closeAll(NetworkTableInstance... instances) {
    for (var instance : instances) {
      instance.close();
    }
  }

  @SuppressWarnings({"PMD.NcssCount", "PMD.CyclomaticComplexity"})
  private static String toValueDebugString(NetworkTableValue value) {
    switch (value.getType()) {
      case kBoolean:
        return Boolean.toString(value.getBoolean());

      case kBooleanArray: {
        var buffer = new StringBuffer();
        var sep = "";
        buffer.append('{');
        for (var val : value.getBooleanArray()) {
          buffer.append(sep);
          sep = ", ";
          buffer.append(val);
        }
        buffer.append('}');
        return buffer.toString();
      }

      case kDouble:
        return Double.toString(value.getDouble());

      case kDoubleArray: {
        var buffer = new StringBuffer();
        var sep = "";
        buffer.append('{');
        for (var val : value.getDoubleArray()) {
          buffer.append(sep);
          sep = ", ";
          buffer.append(val);
        }
        buffer.append('}');
        return buffer.toString();
      }


      case kRaw: {
        var buffer = new StringBuffer();
        var sep = "";
        buffer.append('{');
        for (var val : value.getRaw()) {
          buffer.append(sep);
          sep = ", ";
          buffer.append(String.format("x%02x", val));
        }
        buffer.append('}');
        return buffer.toString();
      }

      case kRpc: {
        var buffer = new StringBuffer();
        var sep = "";
        buffer.append(" RPC{");
        for (var val : value.getRpc()) {
          buffer.append(sep);
          sep = ", ";
          buffer.append(String.format("x%02x", val));
        }
        buffer.append('}');
        return buffer.toString();
      }

      case kString:
        return String.format("'%s'", value.getString());

      case kStringArray: {
        var buffer = new StringBuffer();
        var sep = "";
        buffer.append('{');
        for (var val : value.getStringArray()) {
          buffer.append(sep);
          sep = ", ";
          buffer.append(String.format("'%s'", val));
        }
        buffer.append('}');
        return buffer.toString();
      }

      case kUnassigned:
        return "<undefined>";

      default:
        throw new IllegalArgumentException("Unknown value type=" + value.toString());
    }
  }

  /**
   * Wait for client to connect to server.
   *
   * @param server The server we are waiting on.
   * @param serverName The server name for debugging.
   */
  private static void waitForClient(NetworkTableInstance server, String serverName)
      throws IOException {
    // Use connection listener to ensure we've connected
    int poller = NetworkTablesJNI.createConnectionListenerPoller(server.getHandle());
    NetworkTablesJNI.addPolledConnectionListener(poller, false);
    try {
      final double kTimeoutSecs = 1.0;  // This should happen right away.
      if (NetworkTablesJNI.pollConnectionListenerTimeout(server, poller, kTimeoutSecs)
          .length == 0) {
        throw new IOException("server didn't connect to " + serverName);
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IOException("interrupted while waiting for "
                            + serverName + " connection", ex);
    }
  }

  /**
   * Utility class is not instantiated.
   */
  private NetworkTablesTestUtil() {
  }
}
