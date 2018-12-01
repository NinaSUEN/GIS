import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
public class uavgui extends JDialog {
    Boolean run = true;
    Geofence Fence = new Geofence();
    private JPanel uavPanel;
    private JFormattedTextField STOPFormattedTextField;
    private JButton startButton;
    private JLabel pleaseStartGeofenceLabel;
    private JLabel obstaclesNotDetectedLabel;
    private JLabel headingLabel;
    private JLabel velocityLabel;
    private JLabel heightLabel;
    private JLabel latLabel;
    private JLabel longLabel;
    private JLabel geofenceStatusGeofenceNotLabel;
    private JButton closeButton;
    private Thread mainthread;
    String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    public uavgui() {

        setContentPane(uavPanel);
        setModal(true);
        startButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                File delete = new File("situation");
                delete.delete();
                File delete2 = new File("advisory.txt");
                delete2.delete();
                mainthread = new Thread() {
                    @Override
                    public void run() {
                        while (run = true) {
                            Fence.loadUAV();
                            geofenceStatusGeofenceNotLabel.setText("Geofence Status : Geofence Is Running");
                            geofenceStatusGeofenceNotLabel.setForeground(Color.CYAN);
                            ImageIcon fenceIcon = new ImageIcon("img.png");
                            fenceIcon.getImage().flush();
                            pleaseStartGeofenceLabel.setIcon(fenceIcon);
                            pleaseStartGeofenceLabel.setText("");
                            Geofence.readUAV readUAV = new Geofence.readUAV();
                            String heading = readUAV.readHeading();
                            String velocity = readUAV.readVelocity();
                            String height = readUAV.readHeight();
                            String Long = Geofence.readUAV.passLL.readLong();
                            String Lat = Geofence.readUAV.passLL.readLat();
                            velocityLabel.setText(velocity+" m/s");
                            headingLabel.setText(heading);
                            heightLabel.setText(height+"m");
                            latLabel.setText(Lat);
                            longLabel.setText(Long);
                            Geofence.advisory advisory = new Geofence.advisory();
                            ArrayList infoList = advisory.getstmsg();
                            ArrayList adList = advisory.getadmsg();
                            for (int i = 0; i < infoList.size(); i++) {
                                try {
                                    String filename = "situation.txt";
                                    FileWriter fw = new FileWriter(filename, true);
                                    fw.write((String) timeStamp+"  "+infoList.get(i) + ",");
                                    fw.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            for (int i = 0; i < adList.size(); i++) {
                                String filename = "advisory.txt";
                                try {
                                    FileWriter fw = new FileWriter(filename, true);
                                    fw.write((String) timeStamp+"  "+adList.get(i) + ",");
                                    fw.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            if (adList.size() > 0) {
                                try {
                                    obstaclesNotDetectedLabel.setText("Obstacle Ahead! Waring!");
                                    obstaclesNotDetectedLabel.setForeground(Color.yellow);
                                    STOPFormattedTextField.setForeground(Color.red);
                                    Clip warningSound = AudioSystem.getClip();
                                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("warning.wav").getAbsoluteFile());
                                    warningSound.open(inputStream);
                                    warningSound.start();
                                } catch (LineUnavailableException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedAudioFileException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();

                                }
                            }
                            try {
                                if (mainthread.isInterrupted()) {

                                    try {
                                        break;
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                Thread.sleep(2000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                };

                mainthread.start();
            }
        });
        closeButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

}
