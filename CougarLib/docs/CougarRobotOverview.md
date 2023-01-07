# CougarRobot Overview

This document provides an overview to the Cougar Robot programming model and introduces basic usage.

# Background

CougarRobots are meant to compete in [FRC Robotics Competitions](http://firstinspires.org/robotics/frc) and thus is built on top of the standard
[Java WPILib](https://docs.wpilib.org/en/stable/docs/software/what-is-wpilib.html).

These robots offer students with limited experience an opportunity to create
interesting things and learn from one another in a friendly competitive environment.

CougarRobots builds on the WPILib by adding some abstractions to make robots
a little simpler to write and think about, and more easily testable. It is
intended to provide exposure to some healthier programming practices to
help students create better habits. Hopefully this will also lead to being
able to get more out of the experiences and opportunities that FRC offers.

Beyond the physical code, the creation and development of CougarRobots also
offers students exposure and experiences to the processes around developing
software outside the extra constraints and pressures of the competition build
season. The processes, practices, and experiences should carry over into
competition builds and raising the bar to try tackling more advanced problems
by having gained more experience and become better programmers than otherwise.

This document focuses more on the abstractions and implementations and less
on the processes. This is not intended to be a tutorial for how to write a
robot. Rather provides the background and design information that those tutorials
and guides can reference.

# Model Overview

We'll introduce the model in two ways. First we'll talk about what a running
robot looks like. Then we'll talk about how that robot was constructed and
assembled. The code that actually creates and assembles the robot object involves
more abstractions used only during construction. So we'll leave these out of
the runtime view to help clarify a simpler view.

## Runtime CougarRobot Components

This view is mostly a standard WpiLib "Command-Based robot" but with
more strict separation of concerns (no global variables and more encapsulation
where random code cannot directly call other random code across subsystems.

A CougarRobot is decomposed into `Subsystems`. Subsystems are composed of
`Devices` where each device is part of exactly one Subsystem but a subsystem
can contain multiple devices. Sensors (e.g. encoders) and actuators (e.g. motors)
are devices. Subsystems have exclusive control over the devices they own. They
use the devices to implement primitive capabilities for the subsystem. For
example a drive train might be able to move and turn. An elevator might be able
to move up, down, and hold a position. A shooter might be able to acquire a
target, aim, track, and shoot. These subsystems might provide additional
primitive capabilities such as calibrating themselves.

Robots perform these capabilities via `Commands`. A command is an object
that represents some kind of action to take. The action may be immediate
(e.g. stop a motor) or may be continuous (e.g. track the target) or
something inbetween where the command runs until some goal has been reached
or the command is cancelled for some reason (e.g. lower the elevator until
it reaches the bottom).

Commands are triggered by specific events. The most primitive commands are
triggered by events such as interactions with the driver controller (e.g.
buttons pressed on a joystick) that allow the driver to directly operate
the robot. However triggers can be created around any arbitrary conditions,
such as stopping when a limit switch is triggered, or picking up a game piece
if one is within reach and we have room to store it.

Commands provide the means to script the low level primitive capabilities
provided by the subsystems into higher level actions or more complex
sequences. Especially long running actions that continue while the robot
is thinking about or doing other things (e.g. tracking a target while moving)
it still needs to do internal housekeeping and report back to the driver
station so that safety controls know the robot is still healthy and dont
force it to shutdown. The WPILIb commands make this type of concurrent
programming much easier.

The main program is an event loop that calls into the robot when it is
time for the robot to do something. In practice these are typically events
that trigger commands, or calls to a command telling it to execute the
vnext loop cycle.



| Component   | Class | Purpose    |
|-------------|-------|------------|
| Robot | CougarRobot | The top level "robot" is used to represent the robot. In practice it is decomposed into subsystems that do all the interesting work so the CougarRobot class itself does not actually do anything outside its constructor. In a traditional WPILib Command based robot, this is typically the "RobotContainer". However, CougarRobots have more formal abstraction and expecctations.
| Subsystem | CougarSubsystem | A subsystem is a specialized class responsible for some subset of the robot's devices and capabilities that those devices provide. For example a DriveTrain or a Shooter. If the robot is interacting with something physical, it is a subsystem that is actually responsible for that. Subsystems are also responsible for reporting their state so they can be monitored and debugged.
| Device | Device | Devices are the software implementations around the electronic sensors and actuators. In practice devices are interfaces implemented by very specialized external classes offered by the WPILib or third party vendors for the electromechanical hardware. CougarRobots abstract these specific devices into interfaces that hide the actual implementations allowing them to be swapped with "fake" implementations that can be controlled by tests to create specific reporoducible scenarios to verify the code's ability to handle them as desired.
| Command | Command | Commands provide an object that implements an activity. Usually commands call into subsystem interfaces to control the robot through the capabilities offered by that subsystem. There is a base CougarCommand, but any WPILib command can be used. The base cougar command merely offers more debugging support.
| WPI Adapter | WpiLibAdapter | The adapter handles all the responsibilities of WPILib robots that are outside the scope of the custom robot code required for the game that the robot was designed for. This is typically boilerplate code for managing the low level event loop and interactions with the driverstation. The adapter updates the CougarRobot as necessary or simply takes care of that lower level of abstraction itself allowing programmers to focus on the robot's unique capabilities. The adapter is responsible for creating the CougarRobot. In traditional WPILib Command based robots, this is the "BaseRobot" class (e.g. TimedRobot).


## Construction CougarRobot Components

| Component   | Purpose    |
|-------------|------------|
| DeviceFactory | Subsystems use a [factory method pattern](https://en.wikipedia.org/wiki/Factory_method_pattern) to create devices rather than instantiating classes directly. The `DeviceFactory` is the factory that subsystems use.
| CougarLibInjectedParameters | There are certain "global" objects that robots use. For example the DeviceFactory. These need to come from somewhere. Rather than making them global, we use [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) to pass them into the robot as it is constructed. This allows tests to control them. For example to return fake devices from the DeviceFactory or to control the time reported by the clock. All these components are packaged together and inijected through a single CougarLibInjectedParameters instance. This makes for a single argument rather than many, and can evolve to add more without breaking any existing code.
| RobotConfig | Robots often have special values for things, such as tuning thresholds (how "close" is "close enough") or speeds to run at. Rather than having these appear as "magic numbers" in the code, they are collected together into a configuration object. A RobotConfig is customized for the needs of the particular robot, but is decomposed so that each subsystem has its own config. This clearly documents what the "variables" to the subsystem are and what can be tuned. It makes this information available to tests to know what to expect or to change this configuration to confirm that the implementation is actually paying attention to it. Fixed constants that cannot (or are not expected to) change are not considered "config" and should remain built-in code constants. The RobotConfig is passed into the Robot constructor along with the CougarLibInjectedParameters.
| CougarRobotFactory | The CougarRobotFactory acts as the factory to create our custom CougarRobot. In WPILib there is a RobotSupplier that acts as the WPILib robot factory. It is expected to create a WPILib BaseRobot. It takes no arguments. The CougarLib base robot is a standard implementation that will create the custom CougarRobot class with the robot's interesting implementation. That CougarRobot class requires 2 arguments (The CougarLibInjectedParameters and the RobotConfig). The CougarRobotFactory is what will create our CougarRobot withi the right injected parameters and config. In practice this is [the only] boilerplate code in our robots.


# Example Cougar Robot Code

The example presented here is a simplification of the example robot in the
[Template Cougar Robot Repository](https://github.com/FRC-Team-1403/TemplateRobotRepository/tree/main/Robot/src/main/java/team1403/robot/__replaceme__). See that repository for the complete example but this simplification will be easier to learn the basics from.

Let's assume we have a robot that has a bed that can move along a one-dimensional line.
Like the bed of a scanner. The robot is designed to have a motor that moves the bed
along a rail with limit switches terminating each end.

For now we'll say that the robot is operated by a person using an xbox controller
using two buttons A (forward) and Y (backward) where
forward moves the bed toward one end until it hits the limit switch, and backward
moves the bed toward the other until it hits the other limit switch. These limit
switches are wired to the microcontroller, not to the motors for whatever reason
so we will need to explicitly tell the bed to stop moving.

The point of this example is to illustrate basic concepts. We can build on these
later.

We'll model the software side of this robot has containing a single subsystem
(ExampleRail) that will contain corresponding devices.

The rail subsystem provides the capabilities to:
   * Move at a particular speed in either direction.
   * Stop moving.
   * Determine if it is at either end of the rail.


### Robot Config

This is what the `RobotConfig` class might look like.
For a robot this simple, we do not need the nested classes and attributes,
we could just have a single class with all the attributes. However for purposes
of illustration, more complex robots would benefit from a structure looking more
like this.

Note that when we address motors we will need to provide their address, which
is the CAN bus channel id they are wired to. When we address limit switches
we will need to provide the port on the Robot RIO they are plugged into.

We treat this wiring information as part of the robot configuration and not
hardcoded numbers in the program. We will group the CAN bus channels and
RoboRio ports together as their own modules rather than putting those individual
device addresses as part of the subsystem configuration. The reason for this
is that in practice when there are many more devices, grouping like this will
make it easier to check the electrical wiring and also see if there are conflicts
with the same address being declared for different devices across different
subsystems.


```java
public final class RobotConfig {
  /**
   * Configures the CAN bus.
   */
  public static class CanBus {
    /**
     * The can bus port for the rail motor.
     */
    public int exampleRailMotor = 10;
  }

  /**
   * Ports on the RoboRIO.
   */
  public static class RioPorts {
    /**
     * The rio port that the forward limit switch uses.
     */
    public int exampleRailForwardLimitSwitch = 1;

    /**
     * The rio port that the backward/reverse limit switch uses.
     */
    public int exampleRailReverseLimitSwitch = 2;
  }

  /**
   * Config parameters for tuning the operator interface.
   */
  public static class OperatorConfig {
    /**
     * The joystick port for the driver's controller.
     */
    public int pilotPort = 1;
  }

  /**
   * exampleRail subsystem configuration.
   *
   * <p>Encapsulates the parameters controlling the rail subsystem behavior.
   * The device wiring parameters are specified with their respective bus's
   * configuration.
   *
   */
  public static class ExampleRail {
    /**
     * True if the motor is inverted.
     */
    public boolean motorInverted = false;

    /**
     * The default motor speed for commands.
     */
    public double motorSpeed = 0.75;

    /**
     * The minimum motor speed to move.
     *
     * <p>Anything less will be considered stopping.
     */
    public double minSpeed = 0.01;
  }

  // These are the actual configuration attributes.
  // Each independent aspect of config has its own type
  // so they are scoped to where they are needed and relevant.

  /**
   * The CAN bus configuration.
   */
  public CanBus canBus = new CanBus();

  /**
   * The port allocation on the RoboRIO.
   */
  public RioPorts ports = new RioPorts();

  /**
   * Configuration related to the operator interface.
   */
  public OperatorConfig operator = new OperatorConfig();

  /**
   * Configuration for the ExampleRail subsystem.
   */
  public ExampleRail exampleRail = new ExampleRail();
}
```

### ExampleRail Subsystem

The subsystem implementation would look something like the following.
We'll leave the constructor out for now; it is the most interesting
part so we'll discuss it in more detail afterward.

Note also that this `ExampleRail` class is different from the
`RobotConfig.ExampleRail` class above. The `RobotConfig` class was intentionally
chosen to be named for the subsystem it was for. It is only used by the
subsystem so there should be no ambiguity.

```java
public class ExampleRail extends CougarSubsystem {
  /** 
   * Constructor.
   *
   * @param injectedParameters The standard CougarRobot injected parameters.
   * @param robotConfig Provides the ExampleRail configuration for tuning.
   */
  public ExampleRail(CougarLibInjectedParameters injectedParameters,
                     RobotConfig robotConfig) {
    ...  // Discussed later
  }

  /**
   * Stop the rail motor.
   */
  public void stop() {
    m_motor.setSpeed(0.0);
    m_motor.stopMotor();
  }

  /**
   * Move the rail motor at the given speed.
   *
   * @param speed -1.0..1.0 of motor power.
   *              Positive is forward, negative is backward.
   */
  public void setSpeed(double speed) {
    if (speed > -m_railConfig.minSpeed && speed < m_railConfig.minSpeed) {
      stop();
      return;
    }

    m_motor.setSpeed(speed);
  }

  /**
   * Determine if motor is at the very front of the rail.
   *
   * @return true if front limit switch is triggered, otherwise false.
   */
  public boolean isAtFront() {
    return m_frontLimitSwitch.isTriggered();
  }

  /**
   * Determine if motor is at the very end of the rail.
   *
   * @return true if back limit switch is triggered, otherwise false.
   */
  public boolean isAtBack() {
    return m_backLimitSwitch.isTriggered();
  }

  private final MotorController m_motor;
  private final LimitSwitch m_frontLimitSwitch;
  private final LimitSwitch m_backLimitSwitch;

  private final RobotConfig.ExampleRail m_railConfig;
}

```

#### Subsystem Constructor

The constructor would look something like the following:

```java
  public ExampleRail(CougarLibInjectedParameters injectedParameters,
                     RobotConfig robotConfig) {
    super("Rail", injectedParameters);

    m_railConfig = robotConfig.exampleRail;
    RobotConfig.CanBus can = robotConfig.canBus;
    RobotConfig.RioConfig ports = robotConfig.ports;

    DeviceFactory factory = injectedParameters.getDeviceFactory();
    m_motor = factory.makeTalon("Rail.Motor", can.exampleRailMotor, logger);
    m_motor.setInverted(m_railConfig.motorInverted);
    m_frontLimitSwitch
        = factory.makeLimitSwitch("Rail.Front",
                                  ports.exampleRailForwardLimitSwitch);
    m_backLimitSwitch
        = factory.makeLimitSwitch("Rail.Back",
                                  ports.exampleRailReverseLimitSwitch);
  }
```

The various devices are created by calling methods on the DeviceFactory.
The device names (e.g. "Rail.Motor" are names that we chose to identify
the devices for debugging purposes. The convention we are using gives
each device a unique name and clearly informs what it is for while
stiill being concise.

This constructor is straight forward other than where those injectedParameters
and RobotConfig come from. These will come from the CougarRobot object that
contains these subsystems.

Let's have a look.

### Example CougarRobot

We have not yet introduced commands. For the time being, let's assume we have
a command `SeekEndCommand` that seeks to one of the ends of the rail. We will
discuss how this command is implemented later.

```java
public class CougarRobotImpl extends CougarRobot {
  public CougarRobotImpl(CougarLibInjectedParameters parameters,
                         RobotConfig config) {
    super(parameters);
    m_exampleRail = new ExampleRail(parameters, config);

    configureOperatorInterface(config.operator);
  }

  private void configureOperatorInterface(RobotConfig.OperatorConfig config) {
    XboxController xboxDriver = getJoystick("Driver", config.pilotPort);

    SeekEndCommand railForward
        = new SeekEndCommand(m_exampleRail, SeekEndCommand.Position.FRONT);
    SeekEndCommand railBackward
        = new SeekEndCommand(m_exampleRail, SeekEndCommand.Position.BACK);

    new JoystickButton(xboxDriver, Button.kA.value).whenPressed(railForward);
    new JoystickButton(xboxDriver, Button.kY.value).whenPressed(railBackward);

    // Only for the sake of running this without a controller as an example.
    SmartDashboard.putData(railForward);
    SmartDashboard.putData(railBackward);
  }

  private final ExampleRail m_exampleRail;
}
```

This really is all there is to it. The actual CougarRobot class is not
interesting. It just creates the subsystems and configures commands. The
interesting stuff happens in those subsystems and in those commands.

We still did not answer the question about where the
`CougarLibInjectedParameters` and `RobotConfig` come from, we just pushed
it up a level of abstraction. These will come from the CougarRobotFactory.
The answer is different depending on whether these are coming from a factory
that creates a real robot (one using real electromechanical devices) or a
fake robot for unit testing that will use fake devices controlled by the
tests themselves.


## Creation within a Real Robot

This is the [only] boilerplate code that creates a new CougarRobot using WPILib.
It is perhaps the most complicated code within a CougarLib but is boilerplate so
you will never need to write it yourself.

The cougarFactory is the CougarRobotFactory that creates our new CougarRobot
with the custom RobotConfig. Since the CougarLibInjectedParameters are standard,
the WpiLibRobotAdapter will create them internally (for real robots). This is
simple and straightforward.

The WPILib robot supplier (that takes no args) will return a
`WpiLibRobotAdapter`, which is the `BaseRobot` it wants. However this adapter
requires a CougarRobotFactory so that it can create the CougarRobot that
implements all the interesting things (the WPI robot is just the standard
boilerplate things). We will create that adapter with our custom cougarFactory
as its CougarRobotFactory.

```java
  public static void main(String... args) {
    // This is going to create our CougarRobotImpl when called.
    Function<CougarLibInjectedParameters, CougarRobotImpl> cougarFactory =
        (CougarLibInjectedParameters params) -> {
          RobotConfig config = new RobotConfig();
          return new CougarRobotImpl(params, config);
        };

    RobotBase.startRobot(
        () -> {
          return new WpiLibRobotAdapter<CougarRobotImpl>(cougarFactory);
        });
  }
```

Here we have created the RobotConfig using the default constructor and
default values. The CougarLibInjectedParameters will be created within
the WpiLibRobotAdapter which knows it is a real robot so will give us
the appropriate real parameters.

## Creation within a Test Fixture

Tests create robots with fake devices. Therefore we wont be using the
`WPILibRobtAdapter` and will just create the robot ourselves. This is much
simpler. Except that we need to populate the `DeviceFactory` with the fake
devices for our test. This is straightforward, but requires writing a bunch
of code. That code is part of our test.

Since it is likely that we'll write multiple tests, we can write a common
function to do this for us and have all the tests call that function. We
only have to write the tedious code once (for each subsystem).

```java
  public static class FakeParts {
    /**
     * Construct fake devices and factory for assembling a faked Rail.
     */
    public FakeParts() {
      frontLimitSwitch = new ManualLimitSwitch("Rail.Front");
      backLimitSwitch = new ManualLimitSwitch("Rail.Back");
      fakeMotor = new FakeMotorController("Rail.Motor", logger);

      deviceFactory = new MappedDeviceFactory();
      deviceFactory.putLimitSwitch(frontLimitSwitch);
      deviceFactory.putLimitSwitch(backLimitSwitch);
      deviceFactory.putMotorController(fakeMotor);
    }

    public CougarLogger logger = CougarLogger.getCougarLogger("Rail");
    public ManualLimitSwitch frontLimitSwitch;
    public ManualLimitSwitch backLimitSwitch;
    public FakeMotorController fakeMotor;
    public MappedDeviceFactory deviceFactory;
  }
```
The `MappedDeviceFactory` is a `DeviceFactory` intended for tests which
allows the test to inject the device to use into the factory which will
then be returned when someone asks to construct a device with that particular
name. That way the test can configure the subsystem and control what the
devices say when called or check to see if they were called correctly.

The `MappedDeviceFactory` also keeps track of the calls made into it so can
be used to check whether the devices were constructed correctly (e.g.
did we assign it the correct port that we said it was wired for).

A test to see we constructed the robot correctly
(e.g. did we properly configure the limit switches to the right rio ports?)
might look like this:

```java
  @Test
  void testConstructor() {
    var parts = new FakeParts();
    var parameters = new CougarLibInjectedParameters.Builder()
      .deviceFactory(parts.deviceFactory)
      .build();

    var rail = new ExampleRail(parameters, makeConfig());
    assertEquals("Rail", rail.getName());

    var factoryCalls = parts.deviceFactory.getCalls();
    assertEquals(0, parts.deviceFactory.getRemainingDevices().size());
    assertEquals(Arrays.asList(Integer.valueOf(kFrontPort),"Rail.Front"),
                  factoryCalls.get("Rail.Front"));
    assertEquals(Arrays.asList(Integer.valueOf(kBackPort), "Rail.Back"),
                 factoryCalls.get("Rail.Back"));
    assertEquals(Arrays.asList(Integer.valueOf(kMotorChannel),
                               "Rail.Motor", rail.getLogger()),
                 factoryCalls.get("Rail.Motor"));
  }

```

Note that the test created the `CougarLibInjectedParameters` using the
default values (i.e. same as real) except for the `DeviceFactory` which
it injected using the deviceFactory we set up with all the fake devices.
Other tests that might require other injected parameters might inject
additional parameters they can control. In this case they are not in
scope for the test so the defaults are fine.

## Unit Testing a Subsystem

The whole point of the `DeviceFactory` was to be able to test how we
control subsystems. So how do we actually do that?

Here is an example that tests those primitive capabilities of the
subsystem -- that it will turn motors on and off and report when
the bed is at a limit switch at the end of the rail.

The gist of this is that we control what the fake device will tell the
subsystem (or what the subsystem told the fake device), make
the calls into the subsystem API that we want to test, then check our
fakes or results from the subsystem against our expectations.

```java
  @Test
  void testControl() {
    var parts = new FakeParts();
    var parameters = new CougarLibInjectedParameters.Builder()
      .deviceFactory(parts.deviceFactory)
      .build();

    var rail = new ExampleRail(parameters, makeConfig());
    assertFalse(rail.isAtFront());
    assertFalse(rail.isAtBack());

    parts.frontLimitSwitch.setTriggered(true);
    assertTrue(rail.isAtFront());
    assertFalse(rail.isAtBack());
    parts.frontLimitSwitch.setTriggered(false);

    parts.backLimitSwitch.setTriggered(true);
    assertFalse(rail.isAtFront());
    assertTrue(rail.isAtBack());
    parts.backLimitSwitch.setTriggered(false);

    rail.setSpeed(kMinSpeed);
    assertEquals(kMinSpeed, parts.fakeMotor.getSpeed());

    var fakeMotor = parts.fakeMotor;
    var numStopMotorCalls = fakeMotor.countStopMotorCalls();
    rail.setSpeed(-kMinSpeed);
    assertEquals(numStopMotorCalls, fakeMotor.countStopMotorCalls());
    assertEquals(-kMinSpeed, fakeMotor.getSpeed());

    rail.setSpeed(0.1 - kMinSpeed);
    assertEquals(numStopMotorCalls + 1, fakeMotor.countStopMotorCalls());
    assertEquals(0, fakeMotor.getSpeed());

    rail.setSpeed(kMinSpeed - 0.1);
    assertEquals(numStopMotorCalls + 2, fakeMotor.countStopMotorCalls());
    assertEquals(0, fakeMotor.getSpeed());
  }
```

## Commands

WPILib command-based programming is discussed in the [WPILib documenatation
for Command-based programming](https://docs.wpilib.org/en/stable/docs/software/commandbased/commands.html).

### Example Commands

The following is an example command for seeking to either the front or back of our rail.
It will continue to run until it reaches the desired end. Note that it uses the subsystem
API and does not directly interact with the sensors.

```java
public class SeekEndCommand extends CommandBase {
  /**
   * Specifies where on the rail to seek to.
   */
  public enum Position {
    /**
     * The beginning of the rail.
     */
    FRONT,

    /**
     * The end of the rail.
     */
    BACK
  }

  /**
   * Creates a new SeekEndCommand.
   *
   * @param rail The rail subsystem this command is controlling.
   * @param position The position to seek to
   */
  public SeekEndCommand(ExampleRail rail, Position position) {
    addRequirements(rail);
    m_rail = rail;
    m_goal = position;

    final double direction = position == Position.FRONT ? -1.0 : 1.0;
    m_speed = direction * rail.getRailConfig().motorSpeed;
  }

  /**
   * Returns the goal position we're seeking to.
   *
   * @return The goal bound by the constructor.
   */
  public final Position getGoal() {
    return m_goal;
  }

  /**
   * Move the motor until toward the limit switch until it is triggered.
   */
  @Override
  public void execute() {
    m_rail.setSpeed(m_speed);
  }

  /**
   * Stop the rail from moving when the command finishes for whatever reason.
   */
  @Override
  public void end(boolean interrupted) {
    m_rail.stop();
  }

  
  /**
   * We are finished when we reach the desired position.
   */
  @Override
  public boolean isFinished() {
    if (m_goal == Position.FRONT) {
      return m_rail.isAtFront();
    }

    return m_rail.isAtBack();
  }


  // The rail we're controlling.
  private final ExampleRail m_rail;

  // The goal for this command.
  private final Position m_goal;

  // The speed we'll drive the motor with.
  private final double m_speed;
}
```

### Using Commands

We showed how commands are used earlier. We'll show again.
The command is bound to a trigger. The WPILib scheduler will
then call it when it sees the trigger fire. In practice this
happens inside the event loop where the triggers are checked
every iteration through the loop.

Our program only needs to associate the trigger, then can
forget about it entirely. Notice below we dont even assign the
Joystick button to a variable. This is because internally the
trigger is added to a registry that maintains the reference to
the object so it stays around, and the scheduler will check it
on each iteration.

```java
  private void configureOperatorInterface(RobotConfig.OperatorConfig config) {
    XboxController xboxDriver = getJoystick("Driver", config.pilotPort);

    SeekEndCommand railForward
        = new SeekEndCommand(m_exampleRail, SeekEndCommand.Position.FRONT);
    SeekEndCommand railBackward
        = new SeekEndCommand(m_exampleRail, SeekEndCommand.Position.BACK);

    new JoystickButton(xboxDriver, Button.kA.value).whenPressed(railForward);
    new JoystickButton(xboxDriver, Button.kY.value).whenPressed(railBackward);

    // Only for the sake of running this without a controller as an example.
    SmartDashboard.putData(railForward);
    SmartDashboard.putData(railBackward);
  }
```


### Unit Testing Commands

We can test our command just as we tested our subsystem. We dont need
a real robot or even a real joystick. This test is completely automated.

This test uses the fake parts as above to setup the factory with fakes
that it can control. It creates a custom config to check that it is properly
consulted (e.g. the motor speed will be the configured speed).

Since we are just testing the command we will explicitly schedule it
ourselves. We could test the trigger binding, but that would be part
of the Operator Interface test. Here the command is independent of
the triggers and doesnt need to know about joysticks at all.

We can call the command methods directly to test them. However it
is a little more interesting to have the command scheduler call the
commands as it normally would. This will provide a more robust test
in case our test gets the protocol wrong and does not call the right
methods in the right order. We are really interested in making sure
that our command works properly with the command scheduler, so we
will test our command through the scheduler.

One important thing to be aware of when testing commands is that
by default they will not schedule when the robot is disabled. When our
running our test the program is not connected to a driver station so
the WPILib will think the robot is disabled. We can use the
`DriverStationSim` to pretend the DriverStation is attached and
enabled which will then let us schedule our command.

The test makes successive calls to `scheduler.run()` as if it
were the event loop iterating. It will use the fake devices to
control the scenario of what it is testing -- such as what happens
when the limit switch is reached (or when the opposite limit switch
fires).

```java
class SeekEndCommandTest {
  void doTest(SeekEndCommand.Position position) {
    final double kMotorSpeed = 0.175;
    final var robotConfig = new RobotConfig();
    robotConfig.exampleRail.motorSpeed = kMotorSpeed;

    final var parts = new ExampleRailTest.FakeParts();
    final var parameters = new CougarLibInjectedParameters.Builder()
      .deviceFactory(parts.deviceFactory)
      .build();
    final var rail = new ExampleRail(parameters, robotConfig);
    final var motor = parts.fakeMotor;

    final SeekEndCommand command = new SeekEndCommand(rail, position);
    int stopCalls = motor.countStopMotorCalls();

    final var scheduler = CommandScheduler.getInstance();

    // The command has no effect while the robot is disabled.
    DriverStationSim.setDsAttached(true);
    DriverStationSim.setEnabled(true);
    assertTrue(DriverStationSim.getEnabled());
    command.schedule();
    assertTrue(command.isScheduled());
    // The command does not alter the current motor speed until it is run.
    assertEquals(Double.NaN, motor.getSpeed());


    ManualLimitSwitch testSwitch;
    ManualLimitSwitch otherSwitch;
    double expectSpeed;
    String expectName;
    if (position == SeekEndCommand.Position.FRONT) {
      expectSpeed = -kMotorSpeed;
      testSwitch = parts.frontLimitSwitch;
      otherSwitch = parts.backLimitSwitch;
      expectName = "SeekRailFRONT";
    } else {
      assertEquals(position, SeekEndCommand.Position.BACK);
      expectSpeed = kMotorSpeed;
      testSwitch = parts.backLimitSwitch;
      otherSwitch = parts.frontLimitSwitch;
      expectName = "SeekRailBACK";
    }

    assertEquals(expectName, command.getName());

    scheduler.run();
    assertEquals(expectSpeed, motor.getSpeed());
    scheduler.run();
    assertEquals(expectSpeed, motor.getSpeed());

    // Trigger the other limit switch, it should be ignored.
    otherSwitch.setTriggered(true);
    scheduler.run();
    assertEquals(expectSpeed, motor.getSpeed());
    assertTrue(command.isScheduled());

    // And again to make sure we arent off by one in checking switch.
    scheduler.run();
    assertEquals(expectSpeed, motor.getSpeed());
    assertTrue(command.isScheduled());

    // Now trigger the switch we are moving towards.
    // It should terminate the command and stop the motor.
    otherSwitch.setTriggered(false);
    testSwitch.setTriggered(true);

    assertEquals(stopCalls, motor.countStopMotorCalls());
    scheduler.run();
    assertEquals(stopCalls + 1, motor.countStopMotorCalls());
    assertEquals(0, motor.getSpeed());
    assertFalse(command.isScheduled());
    assertEquals(stopCalls + 1, motor.countStopMotorCalls());
  }

  @Test
  void testForward() {
    doTest(SeekEndCommand.Position.FRONT);
  }

  @Test
  void testReverse() {
    doTest(SeekEndCommand.Position.BACK);
  }
}
```

# CougarLib and TemplateRobotRepository

CougarLib is a repository providing reusable components for writing
and testing CougarRobots. The TemplateRobotRepository is a repository
that acts as a starting point for new robot repositories. The code
added to that repository will be for that specific new unique robot,
but the repository will make available the CougarLib as well making
it straight forward to make changes to common code (e.g. add new
features or fix bugs) shared among other robots. This way there
are not copies of different versions floating around where changes
and features will be lost among them.

## CougarLib Java packages.

CougarLib code is in the java packages starting with `team1403.lib`.
There are different packages.

| Package | Purpose |
| core | Defines classes core to CougarRobot model, except for Devices.
| core.test | Defines classes meant only to facilitate writing tests involving core classes.
| device | Defines device interfaces |
| device.test| Defines devices only meant for tests (e.g. fake devices). This package should only be referenced in test fixtures to ensure they are only used by tests.
| device.virtual | Defines virtual devices. These are components that act like devices (implement device interfaces) but are completely software based so are available to both real and fake robots.
| device.wpi | Defines real devices using WPILibrary (including third party) |
| util | Defines miscellaneous classes providing some sort of helper functionality.
| util.test | Defines classes meant only to facilitate writing tests involving util classes or that are util in nature.

## TemplateRobotRepository Java packages

The source code in the template repository is in the Robot gradle subproject
(i.e. under `Robot/src`). Each repository uses a unique package name (or at least
tries to) reflecting the uniqueness of the code written there. Conceptually all
the unique repository source code directories can be combined together into a single
repository in the future and there would be no collisions among them.

The package that the repository will use is determined by the `setup_repository.sh`
script. The prefix will be `team1403.robots.` The standard boilerplate code in the
Main function that provides the CougarRobotFactory implementation assumes that the
CougarRobot class name is CougarRobotImpl and the config is RobotConfig where
both these classes are in that robot package.

The custom classes are left to the discression of the robot designer. It is
recommended that complex robots put subsystems into their own packages
along with the commands for that subsystem.

Note we do not advise the common practice within WPILib of having
all the commands in a `command` package and all the autonomous code in
an `autonomous` package. CougarRobots take the view that commands are
maintained along the subsystems that they are for rather than maintaining
all the commands together separate from the subsystems they are for. It
also takes the view that there is nothing special about autonomous. How
the commands are triggered or called is independent of what the commands
do. Commands that require operator inputs are inappropriate for autonomous,
but the commands that are autonomous can be available at any time.


# CougarRobot organization vs typical WPILib Command-based robot

| CougarRobot | WPILib Command Robot | Purpose
|-------------|----------------------|--------
| Main | Main | Instantiate the robot class and start the main event loop within WPILib
| WpiLibRobotAdapter (reused directly from CougarLib) | Robot (mostly boilerplate) | The robot base class implementation and boilerplate actions including creating the RobotContainer (WPILib Command Robot only) or CougarRobot (CougarRobot only) and calling the scheduler to run commands.
| CougarRobotImpl | RobotContainer | Creates the custom subsystems and sets up the operator interface and other command triggers.
| RobotConfig | Constants | Magic values for how the robot is wired or tuned.
| per-subsystem/ | commands/ | Directory where command implementations are (and package).
| per-subsystem/ | autonomous/ | Directory where autonomous commands are.
| per-subsystem/ | subsystems/ | Directory where subsystems are.


# Debugging

CougarLib provides a few different types of debugging support.

* The WpiLibRobotAdapter turns on [WPILib DataLogging](https://docs.wpilib.org/en/stable/docs/software/telemetry/datalog.html) so all changes to network tables will be added to the datalog. The datalog will be written to a "Logs" directory in the robot operation directory (e.g. the github root or lvuser home directory on the RoboRio.)

* The WpiLibRobotAdapter turns on debug file logging automatically writing debug logs to the "Logs" directory. This feature might become optional and off by default in real robots if there turns out to be a performance issue with doing it.

* CougarLogger provides a higher level of abstraction for java.util.Logging making it
easy to write formatted messages at different levels, and for dynamically turning on different modules at different levels of displaying messages directly to the console when debugging (where the performance of this IO is not problematic).

* The CougarLib libraries tend to add debug logging messages to help expose the flow control.
This may be pruned in the future if performance becomes an issue.

* Builtin timers measure critical areas of performance providing visibility in the dashbaords
and datalogs as to how long parts of the event loop are taking.

* Programs can add additional timers around code where more precise timing is of interest.


