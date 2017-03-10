CREATE TABLE public.countries (
   name   text   NOT NULL   PRIMARY KEY
);



CREATE TABLE public.areas (
   country   text   NOT NULL ,
   name   text   NOT NULL,
   population   numeric   NOT NULL
);
ALTER TABLE areas
    ADD CONSTRAINT areas_pkey
    PRIMARY KEY (country, name);
ALTER TABLE areas
    ADD CONSTRAINT areas_country_fkey
    FOREIGN KEY (country) REFERENCES countries(name);
ALTER TABLE areas
    ADD CONSTRAINT positive_population
    CHECK (population >= 0::numeric);


CREATE TABLE public.cities (
   country   text   NOT NULL,
   name   text   NOT NULL,
   visitbonus   numeric   NOT NULL
);
ALTER TABLE cities
    ADD CONSTRAINT cities_pkey
    PRIMARY KEY (country, name);
ALTER TABLE cities
    ADD CONSTRAINT cities_country_fkey
    FOREIGN KEY (country, name) REFERENCES areas(country, name);
ALTER TABLE cities
    ADD CONSTRAINT positive_visitbonus
    CHECK (visitbonus >= 0::numeric);


CREATE TABLE public.persons (
   country   text   NOT NULL   REFERENCES countries(name),
   personnummer   character varying(13)   NOT NULL   CHECK (personnummer::text ~* '^[0-9]+[-]+[0-9]'::text AND personnummer::text ~~ '________-____'::text OR personnummer::text ~~ ''::text OR personnummer::text ~~ ' '::text),
   name   text   NOT NULL,
   locationcountry   text NOT NULL,
   locationarea   text NOT NULL,
   budget   numeric   NOT NULL   CHECK (budget >= 0::numeric)
);
ALTER TABLE persons
    ADD CONSTRAINT persons_pkey
    PRIMARY KEY (country, personnummer);
ALTER TABLE persons
    ADD CONSTRAINT persons_locationcountry_fkey
    FOREIGN KEY (locationcountry, locationarea) REFERENCES areas(country, name);



CREATE TABLE public.towns (
    country   text   NOT NULL,
    name   text   NOT NULL
);
ALTER TABLE towns
    ADD CONSTRAINT towns_pkey
    PRIMARY KEY (country, name);
ALTER TABLE towns
    ADD CONSTRAINT towns_country_fkey
    FOREIGN KEY (country, name) REFERENCES areas(country, name);



CREATE TABLE public.roads (
   fromcountry   text   NOT NULL,
   fromarea   text   NOT NULL,
   tocountry   text   NOT NULL,
   toarea   text   NOT NULL,
   ownercountry   text   NOT NULL,
   ownerpersonnummer   text   NOT NULL,
   roadtax   numeric   NOT NULL
);
ALTER TABLE roads
    ADD CONSTRAINT roads_pkey
    PRIMARY KEY (fromcountry, fromarea, tocountry, toarea, ownercountry, ownerpersonnummer);
ALTER TABLE roads
    ADD CONSTRAINT roads_ownercountry_fkey
    FOREIGN KEY (ownercountry, ownerpersonnummer) REFERENCES persons(country, personnummer);
ALTER TABLE roads
    ADD CONSTRAINT roads_tocountry_fkey
    FOREIGN KEY (tocountry, toarea) REFERENCES areas(country, name);
ALTER TABLE roads
    ADD CONSTRAINT positive_roadtax
    CHECK (roadtax >= 0::numeric);



CREATE TABLE public.hotels (
   name   text   NOT NULL,
   locationcountry   text   NOT NULL,
   locationname   text   NOT NULL,
   ownercountry   text   NOT NULL,
   ownerpersonnummer   text   NOT NULL
);
ALTER TABLE hotels
    ADD CONSTRAINT hotels_pkey
    PRIMARY KEY (locationcountry, locationname, ownercountry, ownerpersonnummer);
ALTER TABLE hotels
    ADD CONSTRAINT hotels_ownercountry_fkey
    FOREIGN KEY (ownercountry, ownerpersonnummer) REFERENCES persons(country, personnummer);
ALTER TABLE hotels
    ADD CONSTRAINT hotels_locationcountry_fkey
    FOREIGN KEY (locationcountry, locationname) REFERENCES cities(country, name);



CREATE FUNCTION public.ownedhotelsof(country text, personnummer character varying) RETURNS numeric AS $$  
DECLARE
    xxx NUMERIC;
BEGIN
    xxx := (SELECT COUNT(name) AS name FROM hotels
WHERE ownercountry = country and ownerpersonnummer = personnummer);
    RETURN xxx;
END
$$ LANGUAGE plpgsql;


CREATE FUNCTION public.ownedroadsof(country text, personnummer character varying) RETURNS numeric AS $$      
DECLARE
    xxx NUMERIC;
BEGIN
    xxx := (SELECT COUNT(roadtax) AS roadtax FROM roads
WHERE ownercountry = country and ownerpersonnummer = personnummer);
    RETURN xxx;
END
$$ LANGUAGE plpgsql;




CREATE VIEW public.assetsummary AS
 SELECT p.country, p.personnummer, p.budget, 
    ownedhotelsof(p.country, p.personnummer) * getval('hotelprice'::text) + ownedroadsof(p.country, p.personnummer) * getval('roadprice'::text) AS assets, 
    ownedhotelsof(p.country, p.personnummer) * getval('hotelrefund'::text) * getval('hotelprice'::text) AS reclaimable
   FROM persons p where country != '' AND personnummer != ''
  GROUP BY p.country, p.personnummer, p.budget, ownedhotelsof(p.country, p.personnummer) * getval('hotelprice'::text)
             + ownedroadsof(p.country, p.personnummer) * getval('roadprice'::text);


CREATE VIEW public."combinedroads2" AS
         SELECT r.fromcountry, r.fromarea, r.tocountry AS destcountry, 
            r.toarea AS destarea, min(r.roadtax) AS cost, r.ownercountry, 
            r.ownerpersonnummer, r.roadtax
           FROM roads r
          GROUP BY r.fromcountry, r.fromarea, r.tocountry, r.toarea, r.ownercountry, r.ownerpersonnummer, r.roadtax
UNION 
         SELECT r1.tocountry AS fromcountry, r1.toarea AS fromarea, 
            r1.fromcountry AS destcountry, r1.fromarea AS destarea, 
            min(r1.roadtax) AS cost, r1.ownercountry, r1.ownerpersonnummer, 
            r1.roadtax
           FROM roads r1
          GROUP BY r1.tocountry, r1.toarea, r1.fromcountry, r1.fromarea, r1.ownercountry, r1.ownerpersonnummer, r1.roadtax;


CREATE VIEW public.nextmoves AS
 SELECT p.country AS personcountry, p.personnummer, p.locationcountry as country, p.locationarea as area, 
    r.destcountry, r.destarea, CASE when r.ownercountry = p.country and r.ownerpersonnummer = p.personnummer then 0 
    when r.ownercountry = '' AND r.ownerpersonnummer = '' then 0 
    else min(r.cost)
    END AS cost
   FROM combinedroads2 r, persons p
  WHERE r.fromcountry = p.locationcountry AND r.fromarea = p.locationarea AND
        p.country != '' AND p.personnummer != ''
  GROUP BY p.country , p.personnummer, r.destcountry, r.destarea, r.ownercountry, r.ownerpersonnummer, r.cost;



CREATE FUNCTION public."delete_hotels_trigger"() RETURNS trigger AS $$     
declare 
  tempbudget numeric;
BEGIN
  tempbudget := (select budget from persons where personnummer = old.ownerpersonnummer and country = old.ownercountry);
  update persons set budget = tempbudget + ((select getval('hotelrefund')) * (select getval('hotelprice')))
         where personnummer = old.ownerpersonnummer and country = old.ownercountry;
RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION public."insert_hotels_trigger"() RETURNS trigger AS $$  
declare 
  tempbudget numeric;
BEGIN
 
   IF ( exists(select * from hotels where ownerpersonnummer = new.ownerpersonnummer and ownercountry = new.ownercountry
             AND locationname = new.locationname and locationcountry = new.locationcountry))
             then RAISE EXCEPTION 'Can only own one hotel/city';
   else
     tempbudget := (select budget from persons where personnummer = new.ownerpersonnummer and country = new.ownercountry);
     update persons set budget = tempbudget - (select getval('hotelprice')) where personnummer = new.ownerpersonnummer and country = new.ownercountry;
   end if;
 
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE or replace FUNCTION public."insert_roads"() RETURNS trigger AS $$                            
BEGIN
  if((select count(*) from areas where 
    (country = new.fromcountry AND name = new.fromarea OR
     country = new.tocountry AND name = new.toarea)) < 2 )
    then
       RAISE exception 'Must have 2 of these countries-area available' ;
  end if;
  if(
    ((NEW.fromcountry=persons.locationcountry AND NEW.fromarea = persons.locationarea) OR
    (NEW.tocountry = persons.locationcountry AND NEW.toarea = persons.locationarea)) 
    )
  FROM persons WHERE
  persons.personnummer = NEW.ownerpersonnummer AND 
  persons.country = NEW.ownercountry
  THEN 
    if(NEW.fromarea = NEW.toarea AND NEW.fromcountry = NEW.tocountry) then
      RAISE EXCEPTION 'Cant make a road to itself!' ;
    elsif EXISTS (SELECT * FROM roads r WHERE
      r.ownercountry = NEW.ownercountry AND
      r.ownerpersonnummer = NEW.ownerpersonnummer AND
      (r.toarea = NEW.toarea AND r.fromarea = NEW.fromarea AND
       r.tocountry = NEW.tocountry AND r.fromcountry = NEW.tocountry) OR
      (r.toarea = NEW.fromarea AND r.fromarea = NEW.toarea AND 
       r.tocountry = NEW.fromcountry AND r.fromcountry = NEW.fromcountry) AND
      r.ownerpersonnummer = NEW.ownerpersonnummer AND 
	  r.ownercountry = NEW.ownercountry
      ) 
    THEN
      RAISE EXCEPTION 'Cant own multiple roads of same kind' ;
    else
      UPDATE persons
       SET budget = budget - (SELECT getval('roadprice')) WHERE 
       personnummer = NEW.ownerpersonnummer AND country = NEW.ownercountry;
    END IF;
  elsif (NEW.ownercountry = '' AND NEW.ownerpersonnummer = '') then
    return new;
  else
     RAISE EXCEPTION 'Must be in start or end of the road' ;
  END IF;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;



CREATE FUNCTION public."update_hotels_trigger"() RETURNS trigger AS $$      
BEGIN
 IF new.locationcountry != old.locationcountry OR new.locationname != old.locationname
    then RAISE EXCEPTION 'cant change location of hotel';
 end if;
 IF (new.ownerpersonnummer != old.ownerpersonnummer OR new.ownercountry != old.ownercountry)
   then IF ( exists(select * from hotels where ownerpersonnummer = new.ownerpersonnummer and ownercountry = new.ownercountry
             AND locationname = new.locationname and locationcountry = new.locationcountry))
             then RAISE EXCEPTION 'Can only own one hotel/city';
        end if;
 end if;
 
RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE FUNCTION public."update_roads_trigger"() RETURNS trigger AS $$  
BEGIN
 IF NEW.fromcountry != old.fromcountry or new.fromarea != old.fromarea or
  NEW.tocountry != OLD.tocountry or NEW.toarea != OLD.toarea or
  NEW.ownercountry != OLD.ownercountry or NEW.ownerpersonnummer != OLD.ownerpersonnummer THEN
  RAISE EXCEPTION 'Can only update roadtax!';
 END IF;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ------------------------------------------------------------------------------


CREATE or replace FUNCTION public."update_person"() RETURNS trigger AS $$        
  DECLARE visitingbonus numeric; 
  DECLARE roadtaxx numeric; 
  DECLARE cityvisited numeric := 0;
  DECLARE looparow record;
  DECLARE finalBudget text;
  BEGIN
  
  DROP TABLE IF EXISTS temp_table;
  CREATE TEMP TABLE temp_table ON COMMIT DROP AS SELECT * FROM combinedroads2 WHERE 
    destcountry = NEW.locationcountry AND 
    destarea = NEW.locationarea AND 
    fromarea = old.locationarea and 
    fromcountry = old.locationcountry;
    if(NEW.locationcountry = OLD.locationcountry and NEW.locationarea = OLD.locationarea AND OLD.budget = NEW.budget)
        THEN 
	
  RAISE EXCEPTION 'DETTA ÄR NEW if 1: %', new;
  
  
      --RAISE EXCEPTION 'you are moving to your current position';
    elsif(OLD.budget != NEW.budget or finalBudget = 'final')
    then
  --RAISE EXCEPTION 'DETTA ÄR NEW if 2: %', new;
		finalBudget := '';
      if(new.country = '' AND new.personnummer = '') then
        new.budget = old.budget;
        return old;        
      else
        return new;
      end if;
    END IF;

    IF(EXISTS(SELECT * FROM temp_table WHERE 
  destarea = new.locationarea AND destcountry = new.locationcountry OR
  fromarea = new.locationarea AND fromcountry = new.locationcountry))
        THEN 
    
            IF(exists(select * from temp_table where 
                     (ownerpersonnummer = new.personnummer and 
                      ownercountry = new.country) OR 
                     (ownerpersonnummer = '' and ownercountry = '')))
                THEN 
                    RAISE INFO 'VAD HÄNDER HÄR??';
                 
                else 
                    roadtaxx := (SELECT min(roadtax) from temp_table );  

                    new.budget = (new.budget - roadtaxx);
               
                    UPDATE persons set budget = (budget + roadtaxx) where
                        (country, personnummer) IN 
                        (SELECT ownercountry as country , ownerpersonnummer as personnummer FROM combinedroads2 where
                            destcountry = new.locationcountry and destarea = new.locationarea LIMIT 1);


          if(new.budget < 0) then 
            RAISE EXCEPTION 'You have no money to go this road, it cost: %, you have: %', roadtaxx, (new.budget + roadtaxx);
          end if;
      end if;
    ELSE 
      RAISE EXCEPTION 'no road to given location';
    end if; 

    IF(exists(select * from cities where 
              country = new.locationcountry and 
              name = new.locationarea))
        then 
            IF(exists(select * from hotels where locationcountry = new.locationcountry and
                      locationname = new.locationarea))
                then
                    cityvisited := (select getval('cityvisit') / (select count(*) from hotels where
                        locationcountry = new.locationcountry and locationname = new.locationarea));
                    
          RAISE INFO ' newbudget %, oldbudget %, after %', old.budget, new.budget, (old.budget - getval('cityvisit'));
          
          new.budget = (new.budget - getval('cityvisit'));
          if(new.budget < 0) then 
            RAISE EXCEPTION 'You are to poor to visit this city, it cost: %, you have: %', getval('cityvisit'),
                 (new.budget+getval('cityvisit'));
          end if;
            RAISE INFO 'test %, % ', new.budget, (new.budget - getval('cityvisit'));
          
          for looparow IN select hotels.*, p.budget FROM hotels, persons p WHERE 
            hotels.locationcountry = new.locationcountry and 
            locationname = new.locationarea and
		    p.personnummer = ownerpersonnummer and
		    p.country = ownercountry
          LOOP 
          if(looparow.ownerpersonnummer = new.personnummer AND looparow.ownercountry = new.country) then
		  new.budget = (new.budget + cityvisited);
          else
			UPDATE persons set budget = (looparow.budget + cityvisited) from hotels where 
            country = looparow.ownercountry AND 
            personnummer = looparow.ownerpersonnummer AND
            new.locationarea = hotels.locationname AND 
            new.locationcountry = hotels.locationcountry;
            
          end if;
          
          END LOOP;
          
            RAISE INFO 'test % ', new.locationarea;

    END IF; 

        visitingbonus := (SELECT visitbonus from cities WHERE 
                country=new.locationcountry AND
                name = new.locationarea);
		
		if(visitingbonus != 0 or cityvisited != 0) then
			UPDATE persons set 
				  budget = (new.budget + visitingbonus),
				  locationarea = new.locationarea,
				  locationcountry = new.locationcountry
				  where personnummer = new.personnummer AND country = new.country;
			update cities set visitbonus = 0 where country = new.locationcountry AND name = new.locationarea;
		end if;

         
    END IF;
    

            RAISE INFO 'test % :::: pp % ', new ,(select budget from persons where personnummer = '12345678-1234');
  RAISE info 'new at the end is: %,', new;
    return NEW;
    
  END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER deletehotels BEFORE DELETE ON hotels FOR EACH ROW EXECUTE PROCEDURE delete_hotels_trigger();
CREATE TRIGGER inserthotels BEFORE INSERT ON hotels FOR EACH ROW EXECUTE PROCEDURE insert_hotels_trigger();
CREATE TRIGGER updatehotels BEFORE UPDATE ON hotels FOR EACH ROW EXECUTE PROCEDURE update_hotels_trigger();


CREATE TRIGGER update_persons BEFORE UPDATE ON persons FOR EACH ROW EXECUTE PROCEDURE update_person();


CREATE TRIGGER insertroads BEFORE INSERT ON roads FOR EACH ROW EXECUTE PROCEDURE insert_roads();
CREATE TRIGGER updateroadstrigger BEFORE UPDATE ON roads FOR EACH ROW EXECUTE PROCEDURE update_roads_trigger();


