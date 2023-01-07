package team1403.lib.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;

import edu.wpi.first.networktables.NetworkTableInstance;


/**
 * Test fixture for NetworkTablesTestUtil.
 */
class NetworkTablesTestUtilTest {
  static void sync() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException intex) {
      // empty;
    }
  }

  @Test
  void testConnection() throws IOException {
    var pair = NetworkTablesTestUtil.createLoopbackPair("TestConnectionClient",
                                                        "TestConnectionServer");
    assertEquals(2, pair.length);
    var client = pair[0];
    var server = pair[1];
    final String kTestTableName = "MyTable";
    final String kTestEntryName = "MyEntry";
    final String kTestString = "Hello, World!";
    assertNotEquals(client, server);

    var table = server.getTable(kTestTableName);
    var entry = table.getEntry(kTestEntryName);
    entry.setString(kTestString);

    assertEquals(0, client.getTable("").getSubTables().size());
    server.flush();

    sync();

    assertEquals(1, client.getTable("").getSubTables().size());
    var clientEntry = client.getTable(kTestTableName).getEntry(kTestEntryName);
    assertEquals(clientEntry.getString(""), kTestString);

    NetworkTablesTestUtil.closeAll(pair);
  }

  @Test
  void testConnectWithDefaultClient() throws IOException {
    var pair = NetworkTablesTestUtil.createLoopbackPair(
                   NetworkTablesTestUtil.kDefaultNetworkName, "NotDefaultServer");
    assertNotEquals(pair[0], pair[1]);
    assertEquals(pair[0], NetworkTableInstance.getDefault());
  }

  @Test
  void testConnectWithDefaultServer() throws IOException {
    var pair = NetworkTablesTestUtil.createLoopbackPair(
                   "NotDefaultClient", NetworkTablesTestUtil.kDefaultNetworkName);
    assertNotEquals(pair[0], pair[1]);
    assertEquals(pair[1], NetworkTableInstance.getDefault());
  }

  @Test
  void testDumpTableToString() {
    var instance = NetworkTableInstance.create();
    var table = instance.getTable("TestRoot");
    table.getEntry("bool").setBoolean(true);
    table.getEntry("dbl").setNumber(Double.valueOf(3.14));
    table.getEntry("raw").setRaw(new byte[]{1, 0, 2});
    table.getEntry("str").setString("FIRST VALUE");

    var subtable = table.getSubTable("TestChild");
    subtable.getEntry("bools").setBooleanArray(new Boolean[]{
        true, false, true
    });
    subtable.getEntry("second bool").setBoolean(false);
    subtable.getEntry("strings").setStringArray(new String[]{
        "one", "two", "three"
    });
    subtable.getEntry("numbers").setNumberArray(new Number[]{
        Integer.valueOf(11),
        Integer.valueOf(22),
        Integer.valueOf(33),
    });

    // Not yet visible in table.
    subtable.getEntry("NotUsed");

    var got = NetworkTablesTestUtil.tableToDebugString(table, "");
    String expect = new StringBuffer()
        .append("TABLE '/TestRoot'\n")
        .append("  kBoolean: /TestRoot/bool = true\n")
        .append("  kDouble: /TestRoot/dbl = 3.14\n")
        .append("  kRaw: /TestRoot/raw = {x01, x00, x02}\n")
        .append("  kString: /TestRoot/str = 'FIRST VALUE'\n")
        .append("  TABLE '/TestRoot/TestChild'\n")
        .append("    kBooleanArray: /TestRoot/TestChild/bools = {true, false, true}\n")
        .append("    kDoubleArray: /TestRoot/TestChild/numbers = {11.0, 22.0, 33.0}\n")
        .append("    kBoolean: /TestRoot/TestChild/second bool = false\n")
        .append("    kStringArray: /TestRoot/TestChild/strings = {'one', 'two', 'three'}")
        .toString();
    assertEquals(expect, got);
  }

  @Test
  void testDumpInstanceToString() {
    var instance = NetworkTableInstance.create();

    // Create "out of order"
    var tableA = instance.getTable("TestA");
    tableA.getEntry("name").setString("A");

    var tableC = instance.getTable("TestC");
    tableC.getEntry("name").setString("C");

    var tableB = instance.getTable("TestB");
    tableB.getEntry("name").setString("B");

    var got = NetworkTablesTestUtil.instanceToDebugString(instance);
    String expect = new StringBuffer()
        .append(String.format("NetworkTableInstance %d isDefault=false\n",
                              instance.hashCode()))
        .append("  Connections=0\n")
        .append("  TABLE ''\n")
        .append("    TABLE '/TestA'\n")
        .append("      kString: /TestA/name = 'A'\n")
        .append("    TABLE '/TestB'\n")
        .append("      kString: /TestB/name = 'B'\n")
        .append("    TABLE '/TestC'\n")
        .append("      kString: /TestC/name = 'C'")
        .toString();
    assertEquals(expect, got);
  }

  @Test
  void testDumpInstanceConnectionsToString() throws IOException {
    final String kClientName = "TestClientName";
    final String kServerName = "TestServerName";

    var pair = NetworkTablesTestUtil.createLoopbackPair(
         kClientName, kServerName);
    var client = pair[0];
    var got = NetworkTablesTestUtil.instanceToDebugString(client);

    if (client.getConnections().length == 0) {
      // The makePair waited for the server to see the client,
      // but sometimes the client has not yet seen the server
      // (because they are each asynchronous). So stall to
      // give it a chance to catch up before we test it.
      client.waitForConnectionListenerQueue(0.25);
    }
    assertEquals(1, client.getConnections().length);

    var expect = new StringBuffer()
        .append(String.format("NetworkTableInstance %d isDefault=false\n",
                              client.hashCode()))
        .append("  Connections=1\n")
        .append(String.format("  %s = 127.0.0.1:%d @ never\n",
                              kServerName,
                              client.getConnections()[0].remote_port))
        .append("  TABLE ''")
        .toString();
    assertEquals(expect, got);
  }
}
