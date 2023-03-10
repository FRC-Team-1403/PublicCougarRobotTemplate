package team1403.lib.device.test;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

import team1403.lib.device.BaseDevice;
import team1403.lib.device.CougarDoubleSolenoid;
import team1403.lib.util.CougarLogger;

/**
 * Device implementation for a WpiDoubleSolenoid.
 */
public class FakeDoubleSolenoid extends BaseDevice
                               implements CougarDoubleSolenoid {

  /**
   * Constructor.
   *
   * @param name The name for this instance.
   * @param logger The logger for this instance.
   */
  public FakeDoubleSolenoid(String name, CougarLogger logger) {
    super(name);
    m_name = name;
    m_logger = logger;
    m_state = Value.kOff;
    m_stateList = new ArrayList<Value>();
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public void setState(Value value) {
    m_state = value;
    m_logger.tracef("setValue %s %s", getName(), getState().toString());
    m_stateList.add(m_state);
  }

  @Override
  public void toggle() {
    switch (m_state) {
      case kReverse:
        m_state = Value.kForward;
        break;
      case kForward:
        m_state = Value.kReverse;
        break;
      case kOff:
        break;
      default:
        m_logger.errorf("Toggling from unknown state %s", m_state.toString());
        break;
    }
    m_logger.tracef("toggled %s, new state is %s", getName(), getState().toString());
    m_stateList.add(m_state);
  }

  @Override
  public Value getState() {
    return m_state;
  }

  /**
   * Returns the sequence of state transitions since last cleared.
   * This is so call sequences can be checked if desired.
   *
   * @return The arraylist containing the states this instance has gone through.
   */
  public List<Value> getStateList() {
    return m_stateList;
  }
  /**
   * Gives the amount of states that are stored inside of the states list.
   *
   * @return The size of the state list.
   */

  public int getStateListSize() {
    return m_stateList.size();
  }

  /**
   * Clears the accumulated state changes.
   */
  public void clearStateList() {
    m_stateList.clear();
  }

  private final CougarLogger m_logger;
  private final String m_name;
  private Value m_state; 
  private final List<Value> m_stateList;
}
