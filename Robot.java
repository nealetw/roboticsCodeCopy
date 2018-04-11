package org.usfirst.frc.team4284.robot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.RobotDrive;
//import edu.wpi.first.wpilibj.GyroBase;
import edu.wpi.first.wpilibj.RobotDrive.MotorType;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.cscore.AxisCamera;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import com.kauailabs.navx.frc.AHRS;

//import edu.wpi.first.wpilibj.SensorBase;
//import edu.wpi.first.wpilibj.Relay;
//import edu.wpi.first.wpilibj.Relay.Direction;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */ //Table table = new Table();
public class Robot extends IterativeRobot {
    Timer timer = new Timer();//timertimertimertimertimertimer
    Timer timer1 = new Timer();
    Joystick moveStickA;
    Joystick moveStickB;
    final String defaultAuto = "Default";
    final String customAuto = "Gear Giver Straight";
    final String testingAuto = "Baseline Backup";
    final String customAuto2 = "Gear Giver Right";
    final String customAuto3 = "Gear Giver Right Side";
    final String customAuto4 = "Gear Giver Left Side";
    final String customAuto5 = "We drift to the right a lot...";
    final String customAuto6 = "Gear giver right TEST";
    final String customAuto7 = "Gear giver left TEST";
    final String customAuto8 = "Gear giver straight TEST";
    final String customAuto9 = "More Testing Left Start";
    
    //Talon launcherset;
    Spark climber;
    
   //I2C.Port i2cPort;
   AHRS navx=new AHRS(I2C.Port.kOnboard);
    
    String autoSelected;
    SendableChooser<String> chooser = new SendableChooser<>();
    Thread visionThread;
    
    private static double kVoltsPerDegreePerSecond = 0.0128;
    RobotDrive robotDrive;

    // Channels for the wheels
    final int kFrontLeftChannel = 4;
    final int kRearLeftChannel = 1;
    final int kFrontRightChannel = 2;
    final int kRearRightChannel = 3;
    private static final int kGyroPort = 0;
    
    
   private AnalogGyro gyro = new AnalogGyro(kGyroPort);
    //private Joystick joystick = new Joystick(kJoystickPort);
    
    //other motors
    //Spark ballGraber = new Spark(8);
    //Spark launcher = new Spark(9);
    Servo cameraLR = new Servo(5);
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit() {
        chooser.addDefault("Default Auto", defaultAuto);
        chooser.addObject("Gear Giver Straight", customAuto);
        chooser.addObject("Baseline Backup", testingAuto);
        chooser.addObject("Gear Giver Right", customAuto2);
        chooser.addObject("Gear Giver Right Side", customAuto3);
        chooser.addObject("Gear Giver Left Side", customAuto4);
        chooser.addObject("We drift to the righto...", customAuto5);
        chooser.addObject("Gear giver right TEST", customAuto6);
        chooser.addObject("Gear giver left TEST", customAuto7);
        chooser.addObject("Gear giver straight TEST", customAuto8);
        chooser.addObject("More Testing left side", customAuto9);
        SmartDashboard.putData("Auto choices", chooser);
        
        robotDrive = new RobotDrive(kFrontLeftChannel, kRearLeftChannel, kFrontRightChannel, kRearRightChannel);
        robotDrive.setInvertedMotor(MotorType.kFrontLeft, true);
        robotDrive.setInvertedMotor(MotorType.kFrontRight, false);
        robotDrive.setInvertedMotor(MotorType.kRearLeft, true);
        
        robotDrive.setExpiration(0.1);
        robotDrive.setSafetyEnabled(true);

        gyro.setSensitivity(kVoltsPerDegreePerSecond);
        
        CameraServer.getInstance().startAutomaticCapture();
        
        double senA;
        double senB;
        double senC;
        
    //Victor climber=new Victor(//need port);
        
        moveStickA = new Joystick(0);
        //launcherset = new Talon(7);
        climber = new Spark(0);
        

//camera code
visionThread = new Thread(() -> {
            // Get the Axis camera from CameraServer
            AxisCamera camera = CameraServer.getInstance().addAxisCamera("10.42.84.58");
            // Set the resolution
            camera.setResolution(640, 480);

            // Get a CvSink. This will capture Mats from the camera
            CvSink cvSink = CameraServer.getInstance().getVideo();
            // Setup a CvSource. This will send images back to the Dashboard
            CvSource outputStream = CameraServer.getInstance().putVideo("Rectangle", 640, 480);

            // Mats are very memory expensive. Lets reuse this Mat.
            Mat mat = new Mat();

            // This cannot be 'true'. The program will never exit if it is. This
            // lets the robot stop this thread when restarting robot code or
            // deploying.
            while (!Thread.interrupted()) {
                // Tell the CvSink to grab a frame from the camera and put it
                // in the source mat.  If there is an error notify the output.
                if (cvSink.grabFrame(mat) == 0) {
                    // Send the output the error.
                    outputStream.notifyError(cvSink.getError());
                    // skip the rest of the current iteration
                    continue;
                }
                // Put a rectangle on the image
                Imgproc.rectangle(mat, new Point(100, 100), new Point(400, 400),
                        new Scalar(255, 255, 255), 5);
                // Give the output stream a new image to display
                outputStream.putFrame(mat);
            }
        });
        visionThread.setDaemon(true);
        visionThread.start();


    }

    /**
     * This autonomous (along with the chooser code above) shows how to select
     * between different autonomous modes using the dashboard. The sendable
     * chooser code works with the Java SmartDashboard. If you prefer the
     * LabVIEW Dashboard, remove all of the chooser code and uncomment the
     * getString line to get the auto name from the text box below the Gyro
     *
     * You can add additional auto modes by adding additional comparisons to the
     * switch structure below with additional strings. If using the
     * SendableChooser make sure to add them to the chooser code above as well.
     */
    @Override
    public void autonomousInit() {
        autoSelected = chooser.getSelected();
        // autoSelected = SmartDashboard.getString("Auto Selector",
        // defaultAuto);
        timer.reset(); //Dan Boles
        timer.start();
         timer1.reset();
         timer1.start();
        System.out.println("Auto selected: " + autoSelected);
        //myRobot.setSafetyEnabled(false);
        
    }

    /**
     * This function is called periodically during autonomous
     */
    @Override
    public void autonomousPeriodic() {
        switch (autoSelected) {//result of current test:drives straight for about a second, strafes right
        case customAuto://intended for 2nd station to center peg
            if (timer.get() < 1.9) {
                robotDrive.mecanumDrive_Cartesian(0,-0.4,0,0); // drives forward towards goal-edited
            }
            //else if (timer.get() < 3.0)    {
                ///robotDrive.mecanumDrive_Cartesian(0, -.3, 0, 0); // slows down driving-edited
            //}
           /* else if (timer.get() < 4.0) {
                robotDrive.mecanumDrive_Cartesian(0, 0, 0, 0); // waits for airship squad to pick up gear
            }
            else if (timer.get() < 8.8) {
                robotDrive.mecanumDrive_Cartesian(0, .6, 0, 0); // drives away from airship
            }
            else if (timer.get() < 10.0) {
                robotDrive.mecanumDrive_Cartesian(0, -.6, 0, 0); // drives back to airship
            }*/
            else {
                robotDrive.mecanumDrive_Cartesian(0,0,0,0); // stops robot
            }
            break;
        case customAuto2: //Dan Boles
            if (timer.get() < 3.4) {
                robotDrive.mecanumDrive_Cartesian(0,-0.6,0,0); // drives forward towards goal-edited
            }
            //else if (timer.get() < 3.0)    {
                ///robotDrive.mecanumDrive_Cartesian(0, -.3, 0, 0); // slows down driving-edited
            //}
            else if (timer.get() < 7.0) {
                robotDrive.mecanumDrive_Cartesian(0, 0, 0, 0); // waits for airship squad to pick up gear
            }
            else if (timer.get() < 8.0) {
                robotDrive.mecanumDrive_Cartesian(0, .6, 0, 0); // drives away from airship
            }
            else {
                robotDrive.mecanumDrive_Cartesian(0,0,0,0); // stops robot
            }
            break;
        
        case customAuto3:
            if (timer.get()<1.0)
              {
                      robotDrive.mecanumDrive_Cartesian(0,-0.6,0,0);
              }
              
               else if (timer.get()<1.2)
               {
                   robotDrive.mecanumDrive_Cartesian(0,0,-.5,0);
               }
               else if (timer.get()<1.5)
               {
                   robotDrive.mecanumDrive_Cartesian(0,-.6,0,0);
               }
               else 
               {
                   robotDrive.mecanumDrive_Cartesian(0,0,0,0);
               }
             
         case customAuto4:
             if (timer.get()<1.0)
             {
                     robotDrive.mecanumDrive_Cartesian(0,-0.6,0,0);
             }
             
              else if (timer.get()<1.2)
              {
                  robotDrive.mecanumDrive_Cartesian(0,0,.5,0);
              }
              else if (timer.get()<1.5)
              {
                  robotDrive.mecanumDrive_Cartesian(0,-.6,0,0);
              }
              else 
              {
                  robotDrive.mecanumDrive_Cartesian(0,0,0,0);
              }
             
         case customAuto6:
            //CONSTRUCTOR IS DEPENDENT UPON COMMUNICATION METHOD

            navx.enableLogging(true); //sends output to console
            navx.resetDisplacement(); //displacement==0
            navx.zeroYaw();
            double sixYaw = navx.getYaw();
            if (timer.get()<1.75)
            {
                    robotDrive.mecanumDrive_Cartesian(0,-0.6,0,sixYaw);
            }
            
             else if (timer.get()<3.75) //Dan Boles
             {
                 robotDrive.mecanumDrive_Cartesian(0,0,.5,sixYaw-60);
             }
             else if (timer.get()<4.75)
             {
                 robotDrive.mecanumDrive_Cartesian(0,-.6,0,navx.getYaw());
             }
             else 
             {
                 robotDrive.mecanumDrive_Cartesian(0,0,0,navx.getYaw());
             }
            

         case customAuto7:
         { navx.enableLogging(true); //sends output to console
             navx.resetDisplacement(); //displacement==0
             navx.zeroYaw();
             double sevYaw = navx.getYaw();
             //work pls
             if (timer1.get()<1.00)
             {
                     robotDrive.mecanumDrive_Cartesian(0,-0.3,0,sevYaw);
             }
             else  if (timer1.get() < 1.2)
         {
             robotDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
         }
             
              else if (timer1.get()<3.75)
              {
                  robotDrive.mecanumDrive_Cartesian(0,0,.5,sevYaw+60);
              }
              else if (timer1.get()<4.75)
              {
                  robotDrive.mecanumDrive_Cartesian(0,-.6,0,navx.getYaw());
              }
              else 
              {
                  robotDrive.mecanumDrive_Cartesian(0,0,0,navx.getYaw());
              }}
         case customAuto8:
            navx.enableLogging(true); //sends output to console
             navx.resetDisplacement(); //displacement==0
              navx.zeroYaw(); //Dan Boles
               double origYaw = navx.getYaw();
               timer.reset();

             if (timer.get() < 2.40)
             {
                     robotDrive.mecanumDrive_Cartesian(0,-0.25,0, origYaw);
             }
             else if (timer.get()< 6.0)
             {
                 robotDrive.mecanumDrive_Cartesian(0, 0, 0, navx.getYaw());
             }
             else if (timer.get()< 6.5)
             {
                 robotDrive.mecanumDrive_Cartesian(0, .2, 0, navx.getYaw());
             }
             else if (timer.get()<7.0)
            {
                    robotDrive.mecanumDrive_Cartesian(0,-0.2,0,navx.getYaw());
            }
             else
             {
                 robotDrive.mecanumDrive_Cartesian(0,0,0,0);
             }

         case customAuto9:
            navx.enableLogging(true); //sends output to console
              navx.resetDisplacement(); //displacement==0
              navx.zeroYaw();
              timer.reset();
              double nineYaw = navx.getYaw();
             
             if (timer.get()<2.40)
                {
                        robotDrive.mecanumDrive_Cartesian(0,-0.4,0,nineYaw);
                }
             else if (navx.getYaw() > 62)
             {
                 robotDrive.mecanumDrive_Cartesian(0, 0, -.4, nineYaw);
             }
            
             else if (navx.getYaw() < 58)
             {
                 robotDrive.mecanumDrive_Cartesian(0, 0, 0.4, nineYaw);
             }
             
             timer.reset();
             double newNineAngle = navx.getAngle();
             if (timer.get() < 1)
             {
                 robotDrive.mecanumDrive_Cartesian(0, -0.4, 0, newNineAngle);
             }
             else 
             {
                 robotDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
             }
             
        case defaultAuto:
        default:
            // Put default auto code here
            break;
        case testingAuto:
            if (timer.get() < 1) {
                robotDrive.mecanumDrive_Cartesian(0, -1, 0, 0);
                //parameter 1 turns right if positive
                //parameter 2 drives forward if positive
                //parameter 3 strafes right if positive
            }
            else {
                robotDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
            }
        case customAuto5://what we've been doing
            if (timer.get() < 1.85) {
                robotDrive.mecanumDrive_Cartesian(0,-0.6,0,0); // drives forward towards goal-edited
            }
            else {
                robotDrive.mecanumDrive_Cartesian(0,0,0,0); // stops robot
            }
            break;
        }
    }

    /**
     * This function is called periodically during operator control
     */
    @Override


    public void teleopPeriodic() {
//Drive    

        double senA = moveStickA.getZ();
        double senB = moveStickA.getY(); //Dan Boles
        double senC = moveStickA.getX();
        //Tolerance limiters. There to make it so raw axis isn't directly converted into the movement, making it easier and more uniform to strafe and stuff
       
      
        if(moveStickA.getZ() >= 0.2 && moveStickA.getZ()<=.4)
        {
            senA = 0.33;
        }
        else if(moveStickA.getZ() > 0.4 && moveStickA.getZ()<=.75)
        {
            senA = 0.66;
        }
        else if (moveStickA.getZ() > 0.75)
        {
            senA = 1.0;
        }
        else if (moveStickA.getZ()<=-0.2 && moveStickA.getZ() >= -0.4)
        {
            senA = -0.33;
        }
        else if (moveStickA.getZ()< -0.4 && moveStickA.getZ() >= -0.75)
        {
            senA = -0.66;
        }
        else if (moveStickA.getZ() < -0.75)
        {
            senA = -1.0;
        }
        else
        {
            senA = 0;
        }
        
        
        
        //More tolerance limiters
        if(moveStickA.getY()<=.4 && moveStickA.getY() >= 0.2)
        {
            senB = 0.33;
        }
        else if (moveStickA.getY()<=.75 && moveStickA.getY() > 0.4)
        {
            senB = 0.66;
        }
        else if (moveStickA.getY() > 0.75)
        {
            senB = 1.0;
        }
        else if (moveStickA.getY()<=-0.2 && moveStickA.getY() >= -0.4)
        {
            senB = -0.33;
        }
        else if (moveStickA.getY()< -0.4 && moveStickA.getY() >= -0.75)
        {
            senB = -0.66;
        }
        else if (moveStickA.getY() < -0.75)
        {
            senB = -1.0;
        }
        else
        {
            senB = 0;
        }
        
        
        //MORE tolerance limiters
        if(moveStickA.getX()<=.4 && moveStickA.getX() >= 0.2)
        {
            senC = 0.33;
        } //Dan Boles
        else if (moveStickA.getX()<=.75 && moveStickA.getX() > 0.4)
        {
            senC = 0.66;
        }
        else if (moveStickA.getX() > 0.75)
        {
            senC = 1.0;
        }
        else if (moveStickA.getX()<=-0.2 && moveStickA.getX() >= -0.4)
        {
            senC = -0.33;
        }
        else if (moveStickA.getX()< -0.4 && moveStickA.getX() >= -0.75)
        {
            senC = -0.66;
        }
        else if (moveStickA.getX() < -0.75)
        {
            senC = -1.0;
        }
        else
        {
            senC = 0;
        }
        robotDrive.mecanumDrive_Cartesian(-senC *.5  /*Adjusts the throttle of the robot in the x-axis*/ /* * ((-moveStickA.getRawAxis(3) + 1) / 2)*/, -senB *.5 /*Adjusts the throttle of the y-axis*/ /* * ((-moveStickA.getRawAxis(3) + 1) / 2)*/, -senA *.5 /*Adjusts the rotational throttle of the robot when the bot turns in place*/ /* * ((-moveStickA.getRawAxis(3) + 1) / 2)*/, 0.0);

        Timer.delay(0.005); // wait 5ms to avoid hogging CPU cycles    
        gyro.initGyro();
        kVoltsPerDegreePerSecond=0;
        gyro.setSensitivity(kVoltsPerDegreePerSecond);
        
    
//rope climber
    if(moveStickA.getRawButton(3)){
        climber.set(-0.65);
        }
    else if(moveStickA.getRawButton(4)){
        climber.set(0.65);
    }
    else if (moveStickA.getRawButton(5)) 
    {
        climber.set(-1);
    }
    else if (moveStickA.getRawButton(6))
    {
        climber.set(1);
    }
    else {
        climber.set(0);
    }
    
//Launcher stuff
    /*if(moveStickA.getRawButton(6)) //Starts ball setter when button 2 is pressed
        {
            launcherset.set(1);
        }
    else
        {
            launcherset.set(0);
        }
    
    if(moveStickA.getRawButton(1)) //Starts ball launcher when trigger is pressed
        {
            launcher.set(-0.075);
        }
    else //Dan Boles
        {
            launcher.set(0);
        }
    
//Ballgraber stuff
    if(moveStickA.getRawButton(2)) //Starts grabing balls when thumb button is pushed
        {
            ballGraber.set(-1);
        }
    else if (moveStickA.getRawButton(11))
    {
        ballGraber.set(1);
    }
    else
        {
            ballGraber.set(0);
        }
       */
  //updated camera control
    if (moveStickA.getRawButton(7))
    {
        cameraLR.setAngle(0);
    }
    else if (moveStickA.getRawButton(11))
    {
        cameraLR.setAngle(170);
    }
    else if (moveStickA.getRawButton(8))
    {
        cameraLR.setAngle(40);
    }
    else if (moveStickA.getRawButton(9))
    {
        cameraLR.setAngle(80);
    }
    else if (moveStickA.getRawButton(10))
    {
        cameraLR.setAngle(120);
    }
    
//Allows for small incremental changes in camera angle. Do NOT enable unless you want to swap speed and camera angle constantly
    /*boolean throttle=false;
    if (moveStickA.getRawButton(12))
    {
        throttle=!throttle;
    }
    if (throttle)
    {
        cameraLR.setAngle(moveStickA.getRawAxis(3)*170);
    }*/
        } 
    
//Camera control
    /*if (moveStickA.getPOV()==90)//Turns the camera right
        {
            
                cameraLR.setAngle(cameraLR.getAngle() + 5);
        }
        
    else if (moveStickA.getPOV()==270)//Turns the camera left
        {
            
                cameraLR.setAngle(cameraLR.getAngle() - 5);
            
        }
        
    else if (moveStickA.getPOV()==180)//Turns the camera down
        { //Dan Boles
            
                cameraUD.setAngle(cameraUD.getAngle() + 3);
            
        }
        
    else if (moveStickA.getPOV()==0)//Turns the camera uperoni and cheese
        {
            
                cameraUD.setAngle(cameraUD.getAngle() - 3);
            
        }
    else if (moveStickA.getPOV()==45)//Turns the camera upright
        {
        
            cameraLR.setAngle(cameraLR.getAngle() + 5);
            cameraUD.setAngle(cameraUD.getAngle() - 3);
        }
    else if (moveStickA.getPOV()==135)//Turns the camera downright
        {
        
            cameraLR.setAngle(cameraLR.getAngle() + 5);
            cameraUD.setAngle(cameraUD.getAngle() + 3);
        }
    else if (moveStickA.getPOV()==225)//Turns the camera downleft
        {
        
            cameraLR.setAngle(cameraLR.getAngle() - 5);
            cameraUD.setAngle(cameraUD.getAngle() + 3);
        }
    else if (moveStickA.getPOV()==315)//Turns the camera upleft
        {
        
            cameraLR.setAngle(cameraLR.getAngle() - 5);
            cameraUD.setAngle(cameraUD.getAngle() - 3);
        }
    else if (moveStickA.getRawButton(5)) //Centers camera
        {
            cameraLR.setAngle(135);
            cameraUD.setAngle(108);
        }
    }*/
    

    /**
     * This function is called periodically during test mode
     */
    @Override
    public void testPeriodic() {
        
        if(moveStickA.getRawButton(1))
        {
      
                robotDrive.mecanumDrive_Cartesian(0.5, 0, 0, 0);
        }
        }
        //first positive = strafe left
        //second parameter positive = Forward
        //third parameter negative = right turn
    } //Dan Boles



