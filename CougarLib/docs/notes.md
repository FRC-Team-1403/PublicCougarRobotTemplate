# About This Document

This is intended to be a running set of notes to provide
background information for the sake of teaching others
(or recalling what I did). It will probably be incomplete.
The hope is that this will contain commentary beyond what
might be appropriate for the commit descriptions.

When I show command lines, they will usually be assuming a
unix (i.e. linux) operating system. MacOs is unix. There are
probably windows equivalents, but I am not familiar with them.
If you are using windows, I encourage you to download
[git bash](https://git-scm.com/downloads). You can use that bash shell,
which has most of the common unix commands. Alternatively you
can use [Windows Subsystem for Linux (WSL)](
https://docs.microsoft.com/en-us/windows/wsl/install)


# Setup Git

1. Install GitLens into Visual Code from Marketplace

1. Configure git
   This is so the repository knows who made various changes.
   We should have this set in our private repositories.

   Identify who you are so that git commits can be tagged and
   the changes are traceable back to the person who made them.
   Do not use shared accounts as a rule of thumb.

   **warning**: In public repositories there may be some privacy concerns
   exposing names and contact information, particularly of minors.

   Our repositories should be private, but they could potentially become
   public and the identities in the history are hard (if not impossible)
   ot cleanse.

   If you do not want to expose your identify then "<your name>" would
   be the identity that your registered as under github and leave your
   email out. However if you do this then other members of the team
   (including mentors) are not going to know who you are. We should have
   some private document that maps team members with their git id's so
   that we can know who is who and be able to trace back changes.

       git config --global user.name "<your github id>"

   or (see **warning** above)

       git config --global user.name "<your name>"
       git config --global user.email "<your email>"

   set [end-of-line handling](https://docs.github.com/en/get-started/getting-started-with-git/configuring-git-to-handle-line-endings)

       **windows**: `git config --global core.autocrlf true`
       **linux/mac**: `git config --global core.autocrlf input`

1. Check git setup

       mkdir test
       cd test
       touch file
       git init
       git add .
       git commit -a -m "test"
       git log

   Confirm the `Author` line is correct

1. Remove temporary repository

       cd ..
       rm -rf test


# Create Initial Project

## Create Project From Example

We'll start with an example project to seed this exploration.

1. From VSCode WPI menu (or View/Command Palette):
   1. Select `WPILib: Create Project`

   1. Choose
        * project type: `Example`
        * language: `Java`
        * example: `Scheduler Event Logging`

   1. Select project folder

   1. Set project name (e.g. `DataLoggingExploration`)
        * Select `Create a new folder` if applicable

   1. Set team number `1403`

   1. Select `Enable Desktop Support` checkbox

   1. Click `Generate Project`

1. When prompted to open the folder, pick one of the `Yes` options
     * Or open the project from the `File` menu

1. Create a git repository (from either Command Line or Visual Code)
     * From Command Line

       1. cd to root directory of project folder

       1. Run
              git init
              git add .
              git commit -a -m "The base 'Scheduler Event Logging' example from WPILib 2022.4.1"

     1. Note this is a local git repository, not github. You could create a
        github repo and clone it here. Or you could turn this into a github
        repo later by creating a new repo then pointing this to it as its remote

            git remote add origin <github repo url>

   * From Visual Code

     1. make sure you have "Git Lens" installed from marketplace

     1. Select the `Source Code Control` icon from the left pane toolbar
          * or `Git: Initialize Repository` from `View/Command Palette`

     1. Stage the changes by clicking the `+` icon next to the
        `Staged Changes` dropdown under `Source Control` in the
         left-hand pane.

     1. Type `The base 'Scheduler Event Logging' example from WPILib 2022.4.1`
        into the commit message box at the top of the `Source Control` section 

     1. Click the `Check` icon next to `Source Control`
          * or select `Git: Commit` from `View/Command Palette`
   
## Run Project
Before making changes, let's be sure that this project runs
and see what it does.

1. From VSCode `View/Command Palette`, select `WPILib Simualte Robot Code`

1. Select all the checkboxes (SimGui and SimDriverStation)

1. In the Gui, select `Autonomous` from the Robot State panel (upper left))

1. In the network tables panel there is a Suffleboard table.

   1. Open it to reveal `.recording/events`.

   1. This is what the sample is writing.

   1. If you have a joystick then connect it
        * Drag the joystick from Systems Joystick panel (bottom left)
          to the Joysticks map (bottom center) for Joystick [0]

   1. Click the buttons and see the recording update.

1. Terminate the simulator clicking the red square in the VSCode toolbar.

## Add Buttons for Commands
This example needs a joystick. We probably don't have one unless we
are at an actual Driver Station. So let's add additional command triggers
to the dashboard so we can click buttons in a window rather than a controller.

1. Let's create a new branch before making this change:

       git checkout -b add_command_buttons
      
1. Edit the RobotContainer to `SmartDashboard.put` the commands onto
the dashboard so that you can run them without a joystick.

1. Open the shuffleboard from the WPI Run Tool command (via Command Palette or WPI toolbar)

1. Rerun the simulator.
     * The buttons should appear in the Shuffleboard
     * Click them to execute the commands.
     * quit

1. Commit the change

       # Make sure files make sense   
       git status

       # Review the change in visual code, or review in shell.
       git diff
   
       git commit -a -m "Added command buttons to shuffleboard."


## Add Data Logging
Let's add Data Logging. It is simple enough that we'll tack it
on as part of this first milestone.

1. Let's create a new branch before making this change:

       git checkout -b data_logging
      
1. In the `Robot.robotInit()` method, add the following:

       public void robotInit() {
         DataLogManager.start();
         ...
       }

1. Rerun the simulator
     * go into autonomous then teleop
     * click some buttons
     * end the simulator

1. Confirm the datalog
     * There should be an FRC_*.wpilog file in the workspace dir

1. Open the DataLogTool.
     * From the Command Palette `Start Tool` choice, select `DataLog Tool`
     * Open your datalog using the file browser.

1. Browse the datalog using the tool.

1. Write a List CSV file with everything selected.

1. Look at the CSV file.
     * Regenerate the CSV file with a subset of entries selected
     * With a few entries selected generate a Table view
     * Load that table CSV into a spreadsheet like Google Sheets

1. Commit the change

       # Make sure files make sense   
       git status

       # Review the change in visual code, or since this was simple
       # we can do it directly in the shell.
       git diff
   
       git commit -a -m "Enabled WPI data logging with DataLogManager."


# Fix package naming

Rather than using the default `frc.robots` package, lets use our own
team1403. This will make it easier to identify which is our code,
especially if we want to share with other people (or other people use ours)
because there might otherwise be name conflicts among classes in the
different codebases.

Furthermore, let's put code specific to a specific robot in a package
for that robot. e.g. `team1403.robots.dataloggingexplorer`. We can put
common library code in another base (e.g. `team1403.lib`). Again, this
may make it easier to manage our codebase, especially if we want to
share common code. It will also force more awareness about what is unique
to the robot, and what are standard libraries we create.

It might be possible to do this in Visual Code, but here's an easy
way to do it in a bash shell. Let's do it in a new git branch.

Bash commands are beyond the scope of this initiative, it's just easier
for me to do (and document) this way because I dont know Visual Code that
well.
    ```bash
    git checkout -b rename_packages

    mkdir -p src/main/java/team1403/robots

    # Move the code in the package to the new location.
    # By using git mv, git becomes aware we are moving the files.
    git mv src/main/java/frc/robot src/main/java/team1403/robots/datalogexplorer

    # Change the build.gradle references to the frc.robot package to become
    # references to the the team1403.robots.datalogexplorer package.
    sed -i "s/frc\.robot/team1403.robots.datalogexplorer/g" build.gradle

    # Update the Java code so that references to the old "frc.robot" become
    # references to the new "team1403.robots.datalogexplorer".
    find src -type f -exec sed -i "s/frc\.robot/team1403.robots.datalogexplorer/g" {} \;

    # Confirm the files changed are the ones we expected to be changed.
    git status

    # Confirm our workspace still builds.
    ./gradlew build

    # Commit our change.
    git commit -a -m "Moved classes from frc.robot to team1403.robots.datalogexplorer package"
    ```


# Setup Static Analysis Tools

Before we start doing more interesting programming, lets setup static analysis
tools that can automatically review our code against some standard rules and
practices. We'll use [checkstyle](https://github.com/checkstyle/checkstyle)
and [PMD](https://pmd.github.io/latest). These tools have some overlap, but
are also different. It's easy enough to configure both. This is not an
endorsement of these specific tools, rather of the practice. It looks like
PMD is used internally within the WPI libraries, as is checkstyle.

We can use the default style for these, but there is also a configuration
for the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) so we'll start with that.

1. Add a `checkstyle.xml` and `pmd.xml`
       ```
       git checkout -b static_analysis

       mkdir config
       cd config
       curl -O https://raw.githubusercontent.com/checkstyle/checkstyle/master/src/main/resources/google_checks.xml
       mv google_checks.xml checkstyle.xml
       cd ..
       ```

   Add the initial minimal config/pmd.xml starting point
       ```
       <?xml version="1.0"?>
       <ruleset name="WPILibRuleset"
         xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 http://pmd.sourceforge.net/ruleset_2_0_0.xsd">

         <description>PMD Ruleset for Team 1403</description>

         <rule ref="category/java/bestpractices.xml">
         </rule>
       </ruleset>
       ```

1. Configure `build.gradle` to use checkstyle and pmd.
       ```
       plugins {
           id "java"
           id "edu.wpi.first.GradleRIO" version "2022.4.1"
           id "checkstyle"
           id "pmd"
       }

       ...

       checkstyle {
          toolVersion = '10.3.1'
          configFile = file("config/checkstyle.xml")
          reportsDir = file("${project.rootDir}/build/reports/${project.name}/checkstyle")
          // Dont check test code
          sourceSets = [project.sourceSets.main]
       }

       pmd {
          toolVersion = '6.47.0'
          ignoreFailures = true
          ruleSets = []
          ruleSetFiles = files("config/pmd.xml")
          reportsDir = file("${project.rootDir}/build/reports/${project.name}/pmd")
       }
       ```

1. Build the project
     * `./gradlew build`
     * Notice the warnings
     * Go to the webpages provided in the output to see the warnings
     * ./gradlew checkstyleMain will run checkstyle on `src/main`
     * This caches results.
       * Running once may show errors, then again will not.
       * Edits to the config files or to the source files will
       also invalidate the cache
     * `./gradlew clean` to clear the cache


1. Configure Checkstyle in Visual Code
   1. Install `Checkstyle for Java` (ShengChen) from Visual Code Marketplace.
       * I am not familiar with this plugin, but smoke tested it and seems
         to work when viewing indivdiual files

       * The Checkstyle: Check Code With Checkstyle command from Command Palette
         does not seem to do anything if not viewing the file.

   1. Restart Visual Code if asked
   1. Configure in the Workspace (the default is User)
       1. Open File/Preferences/Settings
       1. Search on `checkstyle`
       1. Set `Java > Checkstyle: Configuration` to `${workspaceFolder}/config/checkstyle.xml`
       1. Set `Java > Checkstyle: Version` to `10.3.1`

   1. Test it
      1. Violate a rule (e.g. move an import out of order).
      1. Visit the file if not already there.
      1. The warning count on bottom of the screen should increment.
      1. Select the warnings and your violation should appear.


# Implement a new class (CougarLogger)

It's often useful to add print statements into a program to help debug an issue.
With the WPI library when you write to System.out or System.err, these are sent
to the driver station and are sent synchronously. That means that the program
will wait until the message is transmitted to the driver station. The driver
station is connected through a WiFi network, and this transmission is going
to be slow relative to the timing in our robot, which is going to cause
problems. We can show that in a future experiment.

For the time being, we would like to be able to have some print statements
in the program, but do not want to have them writing all the time, nor
do we want to have to modify the program to add them in and take them out
each time we want to debug something.

Java has a [logging package](https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html) for this. The package allows programs to create
multiple loggers and turn them on and off individually (or at different levels
of refinement) to control more precisely which debug output you want.

The Logger API is a little cumbersome so let's create a wrapper around it
tailored more for our usage. This is also an opportunity to show what
expectations for creating a class might look like, including how it is
tested.

Note that this class has nothing to do with our particular robot so rather
than putting it under the team1403.robots package, we'll put it somewhere
else. I am putting it in team1403.lib.util because I think we might want
to have that extra level of grouping.

## Testing our new class

Note that this commit has a fully tested component even though we do not yet
introduce it into our robot. To test it I am using [Jnit5 rather than JUnit4](https://blogs.oracle.com/javamagazine/post/migrating-from-junit-4-to-junit-5-important-differences-and-benefits). The reason I am doing this now is, quite
frankly, I already had this class from my earlier prototypes and I needed
featurers in JUnit5 for other components down the line. So since I anticipate
we might want JUnit5, and we do not yet really use JUnit4 anyway (other than
the WPI gradlew came specifying it out of the box), we might as well start
off with 5. I noticed that tests in the WPI libraries themselves are written
using JUnit5.

The first line of testing was in the CheckStyles and Static Analysis plugins
we added earlier. Those raised some issues when I did builds. I either fixed
the issue, decided that the rule is not of interest to us so modified the
tools to not flag those violations, or thought that the check made sense but
was not  applicable to the particular flagged violation so disabled the check
there.

The second line of testing were the unit tests themselves.

The third line of testing was adding code coverage with the Jacoco plugin.
This produces a report showing which parts of the class were executed in
tests and which were not. Code coverage is misleading because behavior might
depend on context but the same lines of code are executed for both. But
if the lines were not covered then that's a signal that you are clearly
missing a test. It is ok to not have 100% coverage, and is not always practical
to achieve. Here we have 100% coverage because it was straight forward to
achieve.

Note that we only care about our class (CougarLogger). Not any of the 
dependencies. We can assume those are already tested. There are different
techniques for writing tests. The most interesting tests here are on verifying
that exactly the right messages are written, and are rendered the way we expect
them to be. To do this I write the messages then read back the output and
compare against expectations.

The CougarLogger is built on top of the Java Logger, which is configured
through a property file that specifies what to render and where to render it
(e.g. file vs console). The tests include verifying that the configuration
file is properly hooked up and our loggers initialize as specified. We could
have a bunch of different files for the different cases, but the test
explicitly writes the files it is going to use. This way it is encapsulated
together to make it easier to maintain and easier to read and understand
what the test is doing and exactly what is being tested.

Running the test via `./gradlew test` will generate the coverage report into
`build/reports/jacoco/test/html/index.html`. You can view this in your browser
with the url `file://<path to workspace>/build./reports/jacoco/test/html/index.html`.
The report will have the packages and drill down to subpackages, classes, and
methods showing both instructions and branches. A branch is something like
the `if` vs `else` clause of a conditional statement, or the individual `case`
clauses within a `switch` statement. The class will show covered lines as
green and missed lines as red.

There are probably plugins for Visual Code, but I use gradlew directly
so havent investigated.


# Control Logging from the Dashboard

Let's change logging levels from the driver station dashboard so that we
can increase logging to diagnose problems when the occur without having
to shutdown the robot, modify the code, redeploy it, start the robot again,
recreate the scenario, then eventually shutdown the robot, take out the code,
and redeploy again. Instead we'll just set individual logger levels to debug
what we want at the level of refinement we want. We might still need to add
new logging code to diagnose issues, but we can leave the useful code in
for next time and turn it on as needed.

There are two basic ways we can do this. The most simplistic would be
to use commands where the command sets the level on the item. This would
probably be cumbersome to use unless we created some kind of selector with
all the loggers and another with the levels. This might not be bad to do.

Another way, which is what we'll do here, is to communicate directly through
the network table where the dashboard operator can directly modify a network
table value and the logger will see the change and adjust itself.


# Add CallTimer

Measuring how long things take is important to understanding performance
both in running bots as well as in experimenting with different techniques.
These timings are often made adhoc. Let's create a class to do this for us.
The timer will record its value somewhere that we can see it on the driver
station and in the datalog. For now this is in the SmartDashboard.
