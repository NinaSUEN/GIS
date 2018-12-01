-- enable gdal drivers for png output
set postgis.gdal_enabled_drivers = 'enable_all';
-- Enable PostGIS (includes raster)
CREATE EXTENSION postgis;
-- Enable Topology
CREATE EXTENSION postgis_topology;
-- Enable PostGIS Advanced 3D
-- and other geoprocessing algorithms
-- sfcgal not available with all distributions
CREATE EXTENSION postgis_sfcgal;
-- fuzzy matching needed for Tiger
CREATE EXTENSION fuzzystrmatch;
-- rule based standardizer
CREATE EXTENSION address_standardizer;
-- example rule data set
CREATE EXTENSION address_standardizer_data_us;
-- Enable US Tiger Geocoder
CREATE EXTENSION postgis_tiger_geocoder;
-- buildings table exists before the program starts, it is supplied by the map provider

alter table buildings
  add unique (osm_id);
-- add table building3d for 3d fencing
select * into building3d
from buildings;
alter table building3d
  add height float;
update building3d
set geom3d = st_force3d(geom);
select st_setsrid(geom3d, 4326);


alter table building3d
  add unique (osm_id);
alter table "building3d"
  add constraint "fk_building3d_gid_osm_id_name_type_geom" foreign key ("gid")
references "buildings" ("gid");
alter table building3d
  add geom3d geometry;


-------------------------------------
-- add table height for 3d fencing reference.
select gid, osm_id into "height"
from buildings;
alter table "height"
  add height float;
alter table "height"
  add constraint "fk_height_gid_osm_id" foreign key ("osm_id")
references "building3d" ("osm_id");
-------------------------------------
-- add table of uav location
create table uav_location (
  lastupdate time,
  height     int
);
-- uavgeom is the point location of uav;
-- uavshape is the geometry of the size of a uav
select addgeometrycolumn('uav_location', 'uavgeom', 4326, 'point', 2);
select addgeometrycolumn('uav_location', 'uavshape', 4326, 'polygon', 2);
-- a testing value for uav_location
insert into uav_location (lastupdate, height, uavgeom, uavshape)
values (current_time,
        30,
        st_setsrid(st_makepoint(-0.627365, 52.072989), 4326),
        st_buffer(st_setsrid(st_makepoint(-0.627365, 52.072989), 4326),
                  0.00001, 'quad_segs=2'));

-- the uavgeom is unique and is used as the primary key here
alter table uav_location
  add constraint "pk_uav_location" primary key ("uavgeom");

-- uavmotion table contains the uav heading and velocity for Geofence advisory
select uav_location.uavgeom as coordinate into uavmotion
from uav_location;
alter table uavmotion
  add velocity float;
alter table uavmotion
  add heading float;
alter table uavmotion
  add constraint "pk_uavmotion" foreign key ("coordinate") references uav_location (uavgeom);

-------------------------------------
-- iswithin table store the value for st_within function to compare.
create table iswithin as
  select buildings.osm_id, buildings.geom, uav_location.uavgeom
  from buildings,
       uav_location;
alter table iswithin
  add unique (osm_id);
alter table "iswithin"
  add constraint "fk_iswithin_osm_id_geom" foreign key ("osm_id")
references "buildings" ("osm_id");
alter table "iswithin"
  add constraint "fk_iswithin_uavgeom" foreign key ("uavgeom")
references "uav_location" ("uavgeom");

-------------------------------------
-- geobox is alternative name for buffered zone, where the centre location is the uav and the radius is defined by the degree in wgs84 system
create table geobox as
  select iswithin.osm_id, st_buffer((select iswithin.uavgeom from uav_location), 0.012), iswithin.geom
  from iswithin;

alter table geobox
  add unique (osm_id);
-- st_area to return the size of the buffered zone
select st_area(st_transform(st_buffer, 27700))
from geobox;
alter table "geobox"
  add constraint "fk_geobox_osmid" foreign key ("osm_id")
references "iswithin" ("osm_id");

-------------------------------------
-- temptable stores the boolean value from st_within function and surronding objects' bearing and distance
create table temptable as select osm_id,geom from geobox;

select osm_id,
       geom,
       st_within(geom, st_buffer),
       st_distancesphere(uav_location.uavgeom, geobox.geom),
       degrees(st_azimuth(st_centroid(geobox.geom), uav_location.uavgeom))
from geobox,
     uav_location;

select st_distancesphere(uav_location.uavgeom, geobox.geom),
       degrees(st_azimuth(st_centroid(geobox.geom), uav_location.uavgeom))
from geobox,
     uav_location;
select st_srid(geobox.geom) from geobox;
select st_setsrid(geobox.geom,4326) from geobox;

alter table temptable
  add unique (osm_id);
alter table temptable
  add unique (geom);
alter table "temptable"
  add constraint "fk_temptable_osm_id_geom" foreign key ("osm_id")
references "buildings" ("osm_id");

-------------------------------------
-- multitable gathers all the geomtries within the radius of the buffer zone geobox and prepare them to be unioned into a multipolygon
select * into multitable
from temptable
where st_within = true;
alter table multitable
  add unique (geom);
alter table multitable
  add unique (osm_id);
alter table "multitable"
  add constraint "fk_multitable_osm_id_geom" foreign key ("osm_id")
references "temptable" ("osm_id");
-------------------------------------
-- outputtable stores all the ready to display image from the multitable results
set postgis.gdal_enabled_drivers = 'enable_all';
select st_aspng(st_asraster(st_multi(st_union(multitable.geom)), 500, 500)) as png into outputtable
from multitable;

alter table "outputtable"
  add constraint "fk_outputtable_asraster" foreign key ("png")
references "multitable" ("geom");
-------------------------------------
-- situation table contains the bearing from uav to building object and the distance from uav to buildings
select multitable.osm_id,
       st_length(st_shortestline(st_transform(st_setsrid(uav_location.uavgeom, 4326), 27700),
                                 st_transform(st_setsrid(multitable.geom, 4326), 27700))),
       degrees(st_azimuth(st_centroid(multitable.geom), uav_location.uavgeom)) into situation
from uav_location,
     multitable;

alter table "situation"
  add constraint "fk_situation_geom" foreign key ("osm_id")
references "multitable" ("osm_id");



