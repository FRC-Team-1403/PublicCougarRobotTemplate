package team1403.lib.device.test;

import team1403.lib.device.BaseDevice;
import team1403.lib.device.CurrentSensor;


/**
 * Implements a fake current sensor.
 */
public class FakeCurrentSensor
       extends BaseDevice
       implements CurrentSensor {
  /**
   * Constructor.
   *
   * @param name The name of the instance.
   */
  public FakeCurrentSensor(String name) {
    super(name);
  }

  /**
   * Set the amps to be returned.
   *
   * @param amps Amps to return
   */
  public void setAmps(double amps) {
    m_amps = amps;
  }

  @Override
  public final double getAmps() {
    return m_amps;
  }

  private double m_amps = Double.NaN;
}
