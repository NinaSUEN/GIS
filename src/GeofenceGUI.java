import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;

public class GeofenceGUI extends JDialog {
    public JButton confirmInputButton;
    Geofence Fence = new Geofence();
    HeightImporter loadHeight = new HeightImporter();
    DatabaseBuilder databaseBuilder = new DatabaseBuilder();
    Timer timer;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton loadHeightDataButton;
    private JButton readSqlButton;
    private JButton initialiseGeofenceButton;
    private JButton loadPOSTGISButton;
    private JButton loadUAVPositionButton;
    private JTextField latitudeTextField;
    private JButton confirmButton;
    private JTextField statusBarTextField;
    private JTextField longitudeTextField;
    private JPanel MapPanel;
    private JLabel mapLabel;
    private JLabel geofenceLabel;
    private JButton clearButton;
    private JButton loadSurroundingMapButton;
    private JTextArea geofenceAdvisoryTextArea;
    private JTextField bufferRaiousTextField;
    private JCheckBox loadBuildingCheckBox;
    private JCheckBox loadLanduseCheckBox;
    private JTextField UAVHeightInMeterTextField;
    private JTextArea situationAwarenessTextArea;
    private JButton startButton;
    private JTextField UAVHeadingTextField;
    private JTextField refreshTimeGapTextField;
    private JButton stopButton;
    private JCheckBox loadRoadsCheckBox;
    private JButton confirmObstacleButton;
    private Thread mainthread;
    Boolean run = true;

    public GeofenceGUI() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("GAUSS Geofence");


        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                HeightImporter.loadnode();
            }
        });
        loadHeightDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                loadHeight.loadheight();
                statusBarTextField.setText("Height loaded");
            }
        });
        readSqlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                databaseBuilder.LoadSql();
            }
        });
        initialiseGeofenceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                databaseBuilder.loadGeofence();
            }
        });
        loadPOSTGISButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    databaseBuilder.GeofenceConstructer();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });
        loadUAVPositionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Fence.loadUAV();
                ImageIcon fenceIcon = new ImageIcon("img.png");
                fenceIcon.getImage().flush();
                geofenceLabel.setIcon(fenceIcon);
                geofenceLabel.setText("");

            }
        });
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String buffer = bufferRaiousTextField.getText();
                String height = UAVHeightInMeterTextField.getText();
                String heading = UAVHeadingTextField.getText();


                try {
                    try {
                        File imagefile = new File("img.png");
                        imagefile.delete();

                        Double buffervalue = Double.parseDouble(buffer);
                        Double heightvalue = Double.parseDouble(height);
                        Double headingvalue = Double.parseDouble(heading);
                    } catch (NumberFormatException e1) {
                        System.err.println("Non Number element detected");
                        statusBarTextField.setText("Please input numbers");
                        throw e1;
                    }
                    new Geofence.bufferSize(buffer);
                    new Geofence.uavHeight(height);
                    new Geofence.uavHeading(heading);
                    statusBarTextField.setText("buffer size updated!  " + "  UAV height set at " + height + "!   UAV heading set at " + heading);

                } catch (Exception e2) {
                    System.err.println(e.getClass().getName() + ":" + e2.getMessage());
                    JOptionPane.showMessageDialog(null, "alert", "alert", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        latitudeTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                latitudeTextField.setText(null);
            }
        });


        longitudeTextField.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                longitudeTextField.setText(null);
            }
        });
        confirmInputButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        clearButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                longitudeTextField.setText("");
                latitudeTextField.setText("");
            }
        });
        confirmInputButton.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                String Lat = latitudeTextField.getText();
                String Long = longitudeTextField.getText();
                try {
                    Double latValue = Double.parseDouble(Lat);
                    Double longValue = Double.parseDouble(Long);
                } catch (NumberFormatException e1) {
                    throw e1;
                }
                if (((-90 > Double.valueOf(Lat)) || (Double.valueOf(Lat) > 90)) || (Double.valueOf(Long) < -180) || Double.valueOf(Long) > 180) {
                    statusBarTextField.setText("Input exceeds range");
                } else {
                    String msg = Long + "," + Lat;
                    if (loadBuildingCheckBox.isSelected()){
                        Fence.includebuilding();
                    }
                    if (loadRoadsCheckBox.isSelected()){}
                    if (loadLanduseCheckBox.isSelected()){}
//                    if ()

                    new Geofence.uploadUAV(Long, Lat);
                    new Geofence.readUAV.passLL(Long,Lat);
                    statusBarTextField.setText("Coordinates accepted");


                    super.mouseClicked(e);
                }

            }
        });

        bufferRaiousTextField.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {

                super.mouseClicked(e);
                bufferRaiousTextField.setText("");

            }
        });
        bufferRaiousTextField.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
//                String buffer = bufferRaiousTextField.getText();
//                try {
//                    new Geofence.bufferSize(buffer);
//                    statusBarTextField.setText("buffer size updated!");
//                } catch (Exception e2) {
//                    System.err.println(e.getClass().getName() + ":" + e2.getMessage());
//                }
            }
        });
        UAVHeightInMeterTextField.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                UAVHeightInMeterTextField.setText("");
            }
        });
        UAVHeightInMeterTextField.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                String height = UAVHeightInMeterTextField.getText();
                try {
                    new Geofence.uavHeight(height);
                    statusBarTextField.setText("UAV height set at " + height);
                } catch (Exception e3) {
                    System.err.println(e3.getClass().getName() + ":" + e3.getMessage()+"and"+e.getClass().getName()+":"+e.getActionCommand());

                }
            }
        });


        startButton.addActionListener(new ActionListener() {

            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                mainthread = new Thread(){
                    @Override
                    public void run() {
                        while (run=true) {
                            Fence.loadUAV();
                            ImageIcon fenceIcon = new ImageIcon("img.png");
                            fenceIcon.getImage().flush();
                            geofenceLabel.setIcon(fenceIcon);
                            geofenceLabel.setText("");
                            Geofence.advisory advisory = new Geofence.advisory();
                            situationAwarenessTextArea.setText("Situation Awareness:");
                            geofenceAdvisoryTextArea.setText("Geofence Advisory:");
                            ArrayList infoList = advisory.getstmsg();
                            ArrayList adList = advisory.getadmsg();
                            situationAwarenessTextArea.append("\n");
                            for (int i = 0; i < infoList.size(); i++) {
                                situationAwarenessTextArea.append((String) infoList.get(i));
                            }
                            geofenceAdvisoryTextArea.append("\n");
                            for (int i = 0; i < adList.size(); i++) {
                                geofenceAdvisoryTextArea.append((String) adList.get(i));
                            }
                            if (adList.size()>0) {
                                try {
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
                                Long refresh = Long.valueOf(refreshTimeGapTextField.getText());

                                if (mainthread.isInterrupted()) {

                                    try {
                                        break;
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                Thread.sleep(refresh);
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
        loadBuildingCheckBox.addActionListener(new ActionListener() {

            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {


            }
        });

        stopButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {

                mainthread.interrupt();
            }
        });
        UAVHeadingTextField.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                UAVHeightInMeterTextField.setText("");
                super.mouseClicked(e);
            }
        });
        refreshTimeGapTextField.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                refreshTimeGapTextField.setText("");
                super.mouseClicked(e);
            }
        });
    }


    public static void main(String[] args) {
        GeofenceGUI dialog = new GeofenceGUI();
        uavgui dialog2 = new uavgui();
        dialog.setTitle("Geofence Simulator");
        dialog2.setTitle("Geofence UAV GUI");
        dialog2.pack();
        dialog.pack();
        dialog.setVisible(true);
        dialog.dispose();
        dialog2.setVisible(true);


        System.exit(0);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();


    }

}
