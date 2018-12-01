import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class Geofence {
    static final String usr = "postgres";
    static final String pwd = "123";
    public static String url = "jdbc:postgresql://51.140.217.170/new_database";
    public static Statement stmt = null;
    public static String heading = null;
    public static String Longi = null;
    public static String Lati = null;
    Connection c = null;

    public void loadUAV() {
        Connection c = null;
        stmt = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        try {
//            url = "jdbc:postgresql://51.140.217.170/postgis";
            c = DriverManager.getConnection(url, usr, pwd);
            System.out.println("Connection Success");
            stmt = c.createStatement();
            stmt1 = c.createStatement();
            stmt2 = c.createStatement();
            int rt = stmt.executeUpdate("SET postgis.gdal_enabled_drivers = 'ENABLE_ALL';");

            int rt3 = stmt.executeUpdate("delete from temptable;\n" +
                                         "insert into temptable(osm_id, geom, st_within)\n" +
                                         "select osm_id,\n" +
                                         "       geom,\n" +
                                         "       st_within(geom, st_buffer) as st_within\n" +
                                         "from geobox,\n" +
                                         "     uav_location;");
            int rt2 = stmt.executeUpdate("delete from multitable;\n" +
                                         "insert into multitable select *\n" +
                                         "from temptable\n" +
                                         "where st_within = true;");
            int rt1 = stmt.executeUpdate("delete from rastersout;"
                                         + "delete from bufferedref;");
            int rt4 = stmt.executeUpdate( "insert into bufferedref select geom from refbox where st_within(geom,st_buffer);"
                                          + "insert into rastersout select st_asraster(st_union(geom),600,480,ARRAY['8BUI', '8BUI', '8BUI'], ARRAY[255,0,0],ARRAY [0,0,0]) from multitable;\n"
                                          + "insert into rastersout select st_asraster(bufferedref.geom,rast,ARRAY['8BUI', '8BUI', '8BUI'], ARRAY[0,255,0],ARRAY [0,0,0]) from bufferedref,rastersout;"
                                          + "insert into rastersout select st_asraster(uav_location.uavshape,rast,ARRAY['8BUI', '8BUI', '8BUI'], ARRAY[160,32,240],ARRAY [0,0,0]) from uav_location,rastersout;"
                                          + "UPDATE rastersout\n"
                                          + "\tSET rast = ST_SetBandNoDataValue(rast,1, NULL);\n"
                                          + "UPDATE rastersout\n"
                                          + "\tSET rast = ST_SetBandNoDataValue(rast,2, NULL);\n"
                                          + "UPDATE rastersout\n"
                                          + "\tSET rast = ST_SetBandNoDataValue(rast,3, NULL);");
            System.out.println("Geometry updated");
            ResultSet rs = stmt.executeQuery("select st_aspng(st_union(rast)) as color from rastersout;");
//                        ResultSet rs = stmt.executeQuery("select png as png from outputtable;");
//            ResultSet rs1 = stmt1.executeQuery("select st_aspng(rast) as rast from raster_output;");
//            ResultSet rs2 = stmt2.executeQuery("select st_aspng(rast) as rast from raster_obstacles;");
//            FileOutputStream fout1, fout2;

            FileOutputStream fout;
            rs.next();
            File imageFile = new File("img.png");
            imageFile.delete();
            fout = new FileOutputStream(imageFile);
            fout.write(rs.getBytes("color"));
            fout.close();
            rs.close();
            stmt.close();
//            rs1.next();
//            File imageFile1 = new File("img1.png");
//            fout1 = new FileOutputStream(imageFile1);
//            fout1.write(rs1.getBytes("rast"));
//            fout1.close();
//            rs1.close();
//            stmt1.close();
//            rs2.next();
//            File imagefile2 = new File("img2.png");
//            fout2 = new FileOutputStream(imagefile2);
//            fout2.write(rs2.getBytes("rast"));
//            fout2.close();
//            rs2.close();
//            stmt2.close();
            c.close();

//            try {
//                Image bottomImage = ImageIO.read(imageFile1);
//                Image topImage = ImageIO.read(imagefile2);
//                int w = ((BufferedImage) bottomImage).getWidth();
//                int h = ((BufferedImage) bottomImage).getHeight();
//                BufferedImage finalImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//                Graphics2D g = finalImage.createGraphics();
//
//                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.1f));
//                g.drawImage(topImage, 600, 480, null);
//                g.dispose();
//                ImageIO.write(finalImage, "png", new File("img.png"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ":" + e.getMessage());
            e.printStackTrace();
            System.out.println("Database error");
            System.exit(0);
        }
    }

    public void includebuilding() {
        Connection c = null;
        stmt = null;
        try {
            c = DriverManager.getConnection(url, usr, pwd);
            stmt = c.createStatement();
            int rt1 = stmt.executeUpdate("" +
                                         "");
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ":" + e.getMessage());
            System.out.println("Database error");
            System.exit(0);
        }

    }

    public void readUAV() {
    }


    public static class uploadUAV {
        public uploadUAV(String Long, String Lat) {
            System.out.println("msg:" + Lat + "," + Long);
            Connection c = null;
            stmt = null;
            try {
                c = DriverManager.getConnection(url, usr, pwd);
                stmt = c.createStatement();
                System.out.println("Reloading UAV Location");
                int rt2 = stmt.executeUpdate("delete from uav_location;insert into uav_location(lastupdate, height, uavgeom, uavshape) values" +
                                             "(current_time,30,st_setsrid(st_makepoint(" + Long + "," + Lat + "),4326),st_buffer(" +
                                             "st_setsrid(" +
                                             "st_makepoint(" + Long + "," + Lat + "), 4326)," +
                                             "0.00005));");
                int rt1 = stmt.executeUpdate("delete from iswithin;\n" +
                                             "insert into iswithin(osm_id, geom) SELECT osm_id,geom from buildings;\n" +
                                             "update iswithin set uavgeom=uav_location.uavgeom from uav_location;");
                System.out.println("UAV location is confirmed at :" + Lat + "," + Long);
                stmt.close();
                c.close();
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ":" + e.getMessage());
                System.out.println("Database error");
                System.exit(0);
            }

        }
    }

    public static class bufferSize {
        public bufferSize(String buffer) {
            Connection c = null;
            stmt = null;
            Double buffervalue = Double.valueOf(buffer);
            try {
//                url = "jdbc:postgresql://127.0.0.1/postgres";
                c = DriverManager.getConnection(url, usr, pwd);
                System.out.println("Connection Success");
                stmt = c.createStatement();

                int rt = stmt.executeUpdate(
                        "create table IF NOT EXISTS geobox as\n" +
                        "  select iswithin.osm_id, st_buffer((select uavgeom from uav_location)," + buffervalue + "), iswithin.geom" +
                        "  from iswithin;"+
                        "delete from geobox;" +
                        "insert into geobox(osm_id, geom) select osm_id,iswithin.geom from iswithin;" +
                        "update geobox set  st_buffer=st_buffer((select uav_location.uavgeom from uav_location), " + buffervalue + ");");
                int rt1 = stmt.executeUpdate(
                        "create table IF NOT EXISTS refbox as "
                        + "select geom_ref.geom,st_buffer((select uav_location.uavgeom from uav_location), " + buffervalue + ") "
                        + "from geom_ref,uav_location;"
                        + "delete from refbox;"
                        + "insert into refbox select geom_ref.geom from geom_ref;"
                        + "update refbox set st_buffer=st_buffer((select uav_location.uavgeom from uav_location), " + buffervalue + ");");



                ResultSet rs1 = stmt.executeQuery("select updategeometrysrid('iswithin','geom',4326);");
                ResultSet rs2 = stmt.executeQuery("select updategeometrysrid('geobox','geom',4326);");
                System.out.println("buffer size updated!");
                rs1.close();
                rs2.close();
                stmt.close();
                c.close();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ":" + e.getMessage());
                System.out.println("Database error");
                System.exit(0);
            }
        }
    }

    public static class uavHeight {
        public uavHeight(String height) {
            Connection c = null;
            stmt = null;
            try {
                c = DriverManager.getConnection(url, usr, pwd);
                System.out.println("Connection Success");
                stmt = c.createStatement();
                int rt = stmt.executeUpdate("update uavmotion set height = " + height + ";");
                System.out.println("uav height updated!");
                stmt.close();
                c.close();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ":" + e.getMessage());
                System.out.println("Database error");
                System.exit(0);
            }
        }
    }

    public static class uavHeading {

        public uavHeading(String heading) {
            Connection c = null;
            stmt = null;
            try {
                c = DriverManager.getConnection(url, usr, pwd);
                System.out.println("Connection Success");
                stmt = c.createStatement();
                int rt = stmt.executeUpdate("update uavmotion set heading = " + heading + ";");
                System.out.println("uav heading updated!");
                stmt.close();
                c.close();
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ":" + e.getMessage());
                System.out.println("Database error");
                System.exit(0);
            }
        }
    }

    public static class advisory {

        public ArrayList getstmsg() {
            Connection c = null;
            stmt = null;
            ArrayList infoList = new ArrayList();
            try {
                c = DriverManager.getConnection(url, usr, pwd);
                System.out.println("Connection Success");
                stmt = c.createStatement();
                int rt = stmt.executeUpdate("delete from situation;\n" +
                                            "insert into situation(osm_id, length, degrees ) select multitable.osm_id,\n" +
                                            "       st_length(st_shortestline(st_transform(st_setsrid(multitable.geom, 4326), 27700),\n" +
                                            "                                 st_transform(st_setsrid(uav_location.uavgeom, 4326), 27700) ))as length,\n" +
                                            "       degrees(st_azimuth(st_centroid(multitable.geom), uav_location.uavgeom))\n" +
                                            "from uav_location,\n" +
                                            "     multitable;");
                ResultSet rs = stmt.executeQuery("select * from situation order by length asc ;");
                while (rs.next()) {
                    ResultSetMetaData rsm = rs.getMetaData();
                    int id = rs.getInt("osm_id");
                    double degree = rs.getDouble("degrees");
                    String degreeString = String.format("%.0f", degree);
                    double length = rs.getDouble("length");
                    String lengthString = String.format("%.2f", length);
                    String infoString = ("Object OSM ID: " + id + " at degree:" + degreeString + " with distance of " + (lengthString) + " meter" + System.lineSeparator());
                    System.out.println(infoString);

                    infoList.add(infoString);
                }
                stmt.close();
                c.close();


            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ":" + e.getMessage());
                System.out.println("Database error");
                System.exit(0);
            }
            return infoList;
        }

        public ArrayList getadmsg() {
            Connection c = null;
            stmt = null;
            ArrayList adList = new ArrayList();
            try {
                c = DriverManager.getConnection(url, usr, pwd);
                System.out.println("Connection Success");
                System.out.println("Reading headings");
                stmt = c.createStatement();

                ResultSet rs = stmt.executeQuery("select * from uavmotion;");
                rs.next();
                Double heading = rs.getDouble("heading");
                System.out.println("heading read success");
                ResultSet rs2 = stmt.executeQuery("select * from situation order by length asc ;");
                while (rs2.next()) {
                    Double degree = rs2.getDouble("degrees");
                    System.out.println("Situation read success");
                    if (heading <= 350) {
                        if ((heading - 10) < degree && degree < (heading + 10)) {
                            System.out.println("Make diversion");
                            String degreeString = String.format("%.0f", degree);
                            String adString = "Make diversion to aviod going " + degreeString + " degree \n";
                            adList.add(adString);
                        }
                    } else if (heading < 10 && heading >= 0) {
                        if (degree > (350 + heading) || degree < (10 + heading)) {
                            System.out.println("Make diversion");
                            String degreeString = String.format("%.0f", degree);
                            String adString = "Make diversion to aviod going " + degreeString + " degree \n";
                            adList.add(adString);

                        }
                    } else if (heading > 350 && heading <= 360) {
                        if ((heading - 10) < (degree + 360) && (degree + 360) < (heading + 10)) {
                            System.out.println("Make diversion");
                            String degreeString = String.format("%.0f", degree);
                            String adString = "Make diversion to aviod going " + degreeString + " degree \n";
                            adList.add(adString);
                        }
                    }
                }
                stmt.close();
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e.getClass().getName() + ":" + e.getMessage());
                System.out.println("Database error");
                System.exit(0);
            }
            return adList;
        }
    }


    public static class readUAV {


        public String readHeading() {
            Connection c = null;
            stmt = null;
            String heading=null;
            try {
                c = DriverManager.getConnection(url, usr, pwd);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Connection Success");
            System.out.println("Reading headings");
            try {
                stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery("select * from uavmotion;");
                rs.next();
                heading = String.valueOf(rs.getDouble("heading"));
                System.out.println("heading read success");
                stmt.close();
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return heading;
        }

        public String readVelocity() {
            String velocity =null;
            Connection c = null;
            stmt = null;
            try {
                c = DriverManager.getConnection(url, usr, pwd);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Connection Success");
            System.out.println("Reading velocity");
            try {
                stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery("select * from uavmotion;");
                rs.next();
                velocity = String.valueOf(rs.getDouble("velocity"));
                System.out.println("velocity read success");
                stmt.close();
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return velocity;
        }

        public String readHeight() {
            String height = null;
            Connection c = null;
            stmt = null;
            try {
                c = DriverManager.getConnection(url, usr, pwd);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Connection Success");
            System.out.println("Reading height");
            try {
                stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery("select * from uavmotion;");
                rs.next();
                height = String.valueOf(rs.getDouble("height"));
                System.out.println("height read success");
                stmt.close();
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return height;
        }

        public static class passLL {
            public passLL(String aLong, String lat) {
                Lati = lat;
                Longi = aLong;
            }

            public static String readLat() {
                return Lati;
            }

            public static String readLong() {
                return Longi;
            }
        }
    }
}
