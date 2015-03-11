-- name: routes-connecting-1000m
-- Returns geojson of routes intersecting origin and destination 1000m buffer
SELECT ST_AsGeoJSON(geom) geom, route_id, route_short_name, route_long_name, route_desc,
       route_type, route_url
FROM routes
WHERE route_short_name='16'
LIMIT 1;


-- name: rt-json
SELECT row_to_json(fc)
FROM (SELECT array_to_json(array_agg(f)) As features
     FROM (SELECT ST_AsGeoJSON(routes.geom) As geometry,
          row_to_json((SELECT l FROM
                              (SELECT route_id, route_short_name,
                              route_long_name, route_desc,
                              route_type, route_url)
                           As l)) As properties
           FROM routes
           WHERE route_short_name='71')
     As f )
As fc;
