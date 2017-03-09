/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;
/* This is the driving engine of the program. It parses the command-line
 * arguments and calls the appropriate methods in the other classes.
 *
 * You should edit this file in three ways:
 * 1) Insert your database username and password in the proper places.
 * 2) Implement the generation of the world by reading the world file.
 * 3) Implement the three functions showPossibleMoves, showPlayerAssets
 *    and showScores.
 */
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*; // JDBC stuff.
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.io.*;  // Reading user input.
import static java.lang.Math.random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;

public class Game
{
    public class Player
    {
        String playername;
        String personnummer;
        String country;
        private String startingArea;

        public Player (String name, String nr, String cntry, String startingArea) {
            this.playername = name;
            this.personnummer = nr;
            this.country = cntry;
            this.startingArea = startingArea;
        }
    }

    String USERNAME = "tda357_017";
    String PASSWORD = "tda357_017";
    private static Statement statement;

    /* Print command optionssetup.
    * /!\ you don't need to change this function! */
    public void optionssetup() {
        System.out.println();
        System.out.println("Setup-Options:");
        System.out.println("		n[ew player] <player name> <personnummer> <country>");
        System.out.println("		d[one]");
        System.out.println();
    }

    /* Print command options.
    * /!\ you don't need to change this function! */
    public void options() {
        System.out.println("\nOptions:");
        System.out.println("    n[ext moves] [area name] [area country]");
        System.out.println("    l[ist properties] [player number] [player country]");
        System.out.println("    s[cores]");
        System.out.println("    r[efund] <area1 name> <area1 country> [area2 name] [area2 country]");
        System.out.println("    b[uy] [name] <area1 name> <area1 country> [area2 name] [area2 country]");
        System.out.println("    m[ove] <area1 name> <area1 country>");
        System.out.println("    p[layers]");
        System.out.println("    q[uit move]");
        System.out.println("    [...] is optional\n");
    }
 
    /* Given a town name, country and population, this function
      * should try to insert an area and a town (and possibly also a country)
      * for the given attributes.
      */
    void insertTown(Connection conn, String name, String country, String population) throws SQLException  {
       
        statement = conn.createStatement(); //connection till databas
        String query = "SELECT count(*) FROM countries where name = " + setString(country); //skapar commando till postgresql
        ResultSet result = statement.executeQuery(query);   //skickar commando till postgresql
        result.next();
        if(result.getInt(1) == 0){
            query = "INSERT INTO countries VALUES("+ setString(country)+")";
            statement.executeUpdate(query);
        }
        
        /*
        skapar aarea 
        */
        query = "SELECT count(*) FROM areas where country = " + setString(country) + " AND name = "+setString(name);        
        result = statement.executeQuery(query);   
        result.next();
        if(result.getInt(1) == 0){
            query = "INSERT INTO areas VALUES("+ setString(country)+","+setString(name)+","+setString(population)+")"; 
            statement.executeUpdate(query);
        }
        
        /*
        skapa town
        */
        query = makeQuery("SELECT count(*) FROM towns where country = % AND name = %", new String[]{country, name});
        
        result = statement.executeQuery(query);   
        result.next();
        if(result.getInt(1) == 0){
            query = "INSERT INTO towns VALUES("+ setString(country)+","+setString(name)+")"; 
            statement.executeUpdate(query);
        }
        
    }

    public String setString(String s){
        return "'"+s+"'";
    }
    
    
    /* Given a city name, country and population, this function
      * should try to insert an area and a city (and possibly also a country)
      * for the given attributes.
      * The city visitbonus should be set to 0.
      */
    void insertCity(Connection conn, String name, String country, String population) throws SQLException {
        statement = conn.createStatement(); //connection till databas
        String query = "SELECT count(*) FROM countries where name = " + setString(country); //skapar commando till postgresql
        ResultSet result = statement.executeQuery(query);   //skickar commando till postgresql
        result.next();
        if(result.getInt(1) == 0){
            query = "INSERT INTO countries VALUES("+ setString(country)+")";
            statement.executeUpdate(query);
        }
        
        /*
        skapar aarea 
        */
        query = "SELECT count(*) FROM areas where country = " + setString(country) + " AND name = "+setString(name);        
        result = statement.executeQuery(query);   
        result.next();
        if(result.getInt(1) == 0){
            query = "INSERT INTO areas VALUES("+ setString(country)+","+setString(name)+","+setString(population)+")"; 
            statement.executeUpdate(query);
        }
        
        /*
        skapaar city
        */
        query = makeQuery("SELECT count(*) FROM cities where country = % AND name = %", new String[]{country, name});
        
        result = statement.executeQuery(query);   
        result.next();
        if(result.getInt(1) == 0){
            query = "INSERT INTO cities VALUES("+ setString(country)+","+setString(name)+","+"0"+")"; 
            statement.executeUpdate(query);
        }
        
        
    }

    private String makeQuery(String s, String[] args){
      
        for(int i = 0 ; i < args.length; i++){
            s = s.replaceFirst("%", setString(args[i]));
        }
        //System.out.println("makeq: " + s);
        return s;
    }
    
    /* Given two areas, this function
      * should try to insert a government owned road with tax 0
      * between these two areas.
      */
    void insertRoad(Connection conn, String area1, String country1, String area2, String country2) throws SQLException {
        
        //statement = conn.createStatement(); //connection till databas
        //String query = "INSERT INTO roads VALUES(%,%,%,%,%,%)" + setString(country); //skapar commando till postgresql
        //ResultSet result = statement.executeQuery(query);
        //ResultSet result = statement.executeQuery(query);
        
        
        
        
        
        // TODO: Your implementation here

        // TODO TO HERE
    }

    /* Given a player, this function
     * should return the area name of the player's current location.
     */
    String getCurrentArea(Connection conn, Player person) throws SQLException {
        statement = conn.createStatement();
        String query = makeQuery("SELECT locationarea FROM persons where country = % AND personnummer = %", new String[]{person.country, person.personnummer});
        System.out.println("test 1: " + query);
        ResultSet result = statement.executeQuery(query);
        result.next();
        return (result.getString("locationarea"));
    }

    /* Given a player, this function
     * should return the country name of the player's current location.
     */
    String getCurrentCountry(Connection conn, Player person) throws SQLException {
        statement = conn.createStatement();
        String query = makeQuery("SELECT locationcountry FROM persons where country = % AND personnummer = %", new String[]{person.country, person.personnummer});
        System.out.println("test 1: " + query);
        ResultSet result = statement.executeQuery(query);
        result.next();
        return (result.getString("locationcountry"));
    }

    /* Given a player, this function
      * should try to insert a table entry in persons for this player
     * and return 1 in case of a success and 0 otherwise.
      * The location should be random and the budget should be 1000.
     */
    int createPlayer(Connection conn, Player person) throws SQLException {
        System.out.println("personnummer : +" + person.personnummer);
        System.out.println("name : +" + person.playername);
        System.out.println("countr : +" + person.country);
        System.out.println("start : +" + person.startingArea);
            /*
        test
        */
            
        statement = conn.createStatement();
        String query = makeQuery("SELECT count(*) FROM persons where country = % AND personnummer = %", new String[]{person.country, person.personnummer});
        System.out.println("test 1: " + query);
        ResultSet result = statement.executeQuery(query);
        result.next();
        System.out.println("test 1 result: " + result.getInt(1));
        if(result.getInt(1)==0){
            query = "SELECT count(*) FROM areas";
            result = statement.executeQuery(query);   
            result.next();
            int i = result.getInt(1);
            int test = result.getInt(1);
            System.out.println("areas count : " + test);
            Random rand = new Random();
             i = rand.nextInt(i - 0 + 1);
            System.out.println("random count : " + i);
            
            for(int j = 0; j < test; j++ ){
                i = rand.nextInt(test - 0 + 1);
                System.out.println("random count : " + i);
            }
             
             query = "SELECT * FROM areas";
             
             
             result = statement.executeQuery(query);
             
            
             
             result.next();
             for(int j = 0; j <= i ; j++){
                 result.next();
             } 
             String lcountry = result.getString("country");
              String larea = result.getString("name");
             
             System.out.println("random country is: " + lcountry + "  random area is : " + larea);
             
             
             System.out.println("innan makequery persons");
             System.out.println("test 3 : " + query);
             System.out.println(makeQuery("INSERT INTO persons VALUES(%,%,%,%,%,1000)", new String[]{person.country, person.personnummer, person.playername, lcountry, larea}));
             query = makeQuery("INSERT INTO persons VALUES(%,%,%,%,%,1000)", new String[]{person.country, person.personnummer, person.playername, lcountry, larea});
             statement.executeUpdate(query);
             
             System.out.println(result);
             return 1;
        }
        else{
         
            return 0;
        }
    }

    /* Given a player and an area name and country name, this function
     * sould show all directly-reachable destinations for the player from the
     * area from the arguments.
     * The output should include area names, country names and the associated road-taxes
      */
    void getNextMoves(Connection conn, Player person, String area, String country) throws SQLException {
        statement = conn.createStatement();
        String query = makeQuery("SELECT * FROM combinedroads2 WHERE fromcountry = % AND fromarea = %",new String[]{country, area});
        ResultSet result = statement.executeQuery(query);
        result.next();
        System.out.println("Possible moves from " + area + "," + country + "requested by "+person.playername);
        while(result.next()){
            System.out.println(result.getString("destarea") + ", " + result.getString("destcountry")+ ", cost: " + result.getString("cost"));
        }
    }

    /* Given a player, this function
       * sould show all directly-reachable destinations for the player from
     * the player's current location.
     * The output should include area names, country names and the associated road-taxes
     */
    void getNextMoves(Connection conn, Player person) throws SQLException {
        // TODO: Your implementation here
        // hint: Use your implementation of the overloaded getNextMoves function
        statement = conn.createStatement();
        String query = makeQuery("SELECT * FROM nextmoves where country = % AND personnummer = %", new String[]{person.country, person.personnummer});
        System.out.println("getNextMoves : " + query);
        ResultSet result = statement.executeQuery(query);
        result.next();
        System.out.println("Possible moves for " + person.playername+ ":");
        while(result.next()){
            System.out.println(result.getString("destarea") + " " + result.getString("destcountry")+ " " + result.getString("cost"));
        }
        

        // TODO TO HERE
    }

    /* Given a personnummer and a country, this function
     * should list all properties (roads and hotels) of the person
     * that is identified by the tuple of personnummer and country.
     */
    void listProperties(Connection conn, String personnummer, String country) throws SQLException {
        statement = conn.createStatement();
        String query = makeQuery("SELECT * FROM roads WHERE ownerpersonnummer = % AND ownercountry = %",new String[]{personnummer, country});
        ResultSet result = statement.executeQuery(query);
        result.next();
        System.out.println("Properties of person with personnummer: "+ personnummer+" and country: " +country );
        System.out.println("\nRoads");
        while(result.next()){
            System.out.println(result.getString("fromarea")+", "+result.getString("fromcountry")+" - "
                                +result.getString("destarea")+", "+result.getString("destcountry")+" Roadtax: " + result.getString("roadtax"));
        }
        query = makeQuery("SELECT * FROM hotels WHERE ownercountry = % AND ownerpersonnummer = %",new String[]{country, personnummer});
        result = statement.executeQuery(query);
        result.next();
        System.out.println("\nHotels");
        while(result.next()){
            System.out.println(result.getString("name")+" - "+result.getString("locationname")+", "+result.getString("locationcountry")
                                            /*+" cost: " +result.getString("price")*/);
        }


    }

    /* Given a player, this function
     * should list all properties of the player.
     */
    void listProperties(Connection conn, Player person) throws SQLException {
        statement = conn.createStatement();
        String query = makeQuery("SELECT * FROM roads WHERE ownerpersonnummer = % AND ownercountry = %",new String[]{person.personnummer, person.country});
        ResultSet result = statement.executeQuery(query);
        result.next();
        System.out.println("Properties of plaer: "+ person.playername );
        System.out.println("\nRoads");
        while(result.next()){
            System.out.println(result.getString("fromarea")+", "+result.getString("fromcountry")+" - "
                    +result.getString("destarea")+", "+result.getString("destcountry")+" Roadtax: " + result.getString("roadtax"));
        }
        query = makeQuery("SELECT * FROM hotels WHERE ownercountry = % AND ownerpersonnummer = %",new String[]{person.country, person.personnummer});
        result = statement.executeQuery(query);
        result.next();
        System.out.println("\nHotels");
        while(result.next()){
            System.out.println(result.getString("name")+" - "+result.getString("locationname")+", "+result.getString("locationcountry")
                                            /*+" cost: " +result.getString("price")*/);
        }
    }

    /* This function should print the budget, assets and refund values for all players.
     */
    void showScores(Connection conn) throws SQLException {
        statement = conn.createStatement();
        String query = "SELECT * FROM assetsummary";
        ResultSet result = statement.executeQuery(query);
        result.next();
        System.out.println("\nAsset summary:");
        while(result.next()){
            System.out.println("("+result.getString("country")+", "+result.getString("personnummer")+") - Budget: "+
                    result.getString("budget")+" Assets: "+result.getString("assets")+" Reclaimable: "+result.getString("reclaimable"));
        }

    }

    /* Given a player, a from area and a to area, this function
     * should try to sell the road between these areas owned by the player
     * and return 1 in case of a success and 0 otherwise.
     */
    int sellRoad(Connection conn, Player person, String area1, String country1, String area2, String country2) throws SQLException {
        statement = conn.createStatement();
        String query = makeQuery("SELECT count(*) from roads where fromcountry = % and fromarea % and tocountry = %" +
                "                    and toarea = % and ownercountry = % and ownerpersonnummer = % ",
                                    new String[]{country1, area1, country2,area2, person.country, person.personnummer});
        ResultSet result = statement.executeQuery(query);
        result.next();
        if(result.getInt(1) == 1){
            query = makeQuery("DELETE FROM roads WHERE fromcountry = % and fromarea % and tocountry = %" +
                                "and toarea = % and ownercountry = % and ownerpersonnummer = % ",
                                new String[]{country1,area1,country2,area2, person.country, person.personnummer});
            statement.executeUpdate(query);
            return(1);
        }
        else{
            query = makeQuery("SELECT count(*) from roads where fromcountry = % and fromarea % and tocountry = %" +
                            "and toarea = % and ownercountry = % and ownerpersonnummer = % ",
                            new String[]{country2, area2, country1,area1, person.country, person.personnummer});
            result = statement.executeQuery(query);
            result.next();
            if(result.getInt(1) == 1){
                query = makeQuery("DELETE FROM roads WHERE fromcountry = % and fromarea % and tocountry = %" +
                                "and toarea = % and ownercountry = % and ownerpersonnummer = % ",
                        new String[]{country2,area2,country1,area1, person.country, person.personnummer});
                statement.executeUpdate(query);
                return(1);
            }
            else{
                return(0);
            }

        }
    }

    /* Given a player and a city, this function
     * should try to sell the hotel in this city owned by the player
     * and return 1 in case of a success and 0 otherwise.
     */
    int sellHotel(Connection conn, Player person, String city, String country) throws SQLException {
        try{
            statement = conn.createStatement();
            String query = makeQuery("DELETE FROM hotels WHERE locationcountry = % AND locationarea = % AND ownercountry = % AND ownerpersonnummer = %", 
                    new String[]{
                        country,                //area
                        city,                   //coutnry
                        person.country,         //ownercoutbry
                        person.personnummer     //ownerpersonnummer
                    });
            System.out.println("Sending query : " + query);
            statement.executeUpdate(query);
        }catch(SQLException e){
            System.out.println(e.toString());
            return 0;
        }
        return 1;   
    }

    /* Given a player, a from area and a to area, this function
     * should try to buy a road between these areas owned by the player
     * and return 1 in case of a success and 0 otherwise.
     */
    int buyRoad(Connection conn, Player person, String area1, String country1, String area2, String country2) throws SQLException {
        try{
            statement = conn.createStatement();
            String query = makeQuery("INSERT INTO roads VALUES(%,%,%,%,%,%,getval('roadtax'))", 
                    new String[]{
                        country1,               //fromcoutnry
                        area1,                  //fromarea
                        country2,               //tocountry
                        area2,                  //toarea    
                        person.country,         //ownercoutbry
                        person.personnummer     //ownerpersonnummer
                    });

            System.out.println("Sending query : " + query);
            statement.executeUpdate(query);
        }catch(SQLException e){
            System.out.println(e.toString());
            return 0;
        }
        return 1;
    }

    /* Given a player and a city, this function
     * should try to buy a hotel in this city owned by the player
     * and return 1 in case of a success and 0 otherwise.
     */
    int buyHotel(Connection conn, Player person, String name, String city, String country) throws SQLException {
        
        try{
            statement = conn.createStatement();
            String query = makeQuery("INSERT INTO hotels VALUES(%,%,%,%,%)", 
                    new String[]{
                        name,
                        country,
                        city,
                        person.country, 
                        person.personnummer
                    });
            
            System.out.println("Sending query : " + query);
            statement.executeUpdate(query);
        }catch(SQLException e){
            System.out.println(e.toString());
            return 0;
        }
        return 1;
    }

    /* Given a player and a new location, this function
     * should try to update the players location
     * and return 1 in case of a success and 0 otherwise.
     */
    int changeLocation(Connection conn, Player person, String area, String country) throws SQLException {
        try{
            statement = conn.createStatement();
            String query = makeQuery("UPDATE persons SET locationarea = %, locationcountry = % WHERE country = % AND personnummer = %", 
                new String[]{
                    area,
                    country,
                    person.country, 
                    person.personnummer
                });
            System.out.println("Sending query : " + query);
            statement.executeUpdate(query);
        }catch(SQLException e){
            System.out.println(e.toString());
            return 0;
        }
        return 1;
    }

    /* 
    This function should add the visitbonus of 1000 to a random city
    */
    void setVisitingBonus(Connection conn) throws SQLException {
        /*
        Select random nummer between 0 - count cities
        */
        String query = "SELECT count(*) FROM cities";
        ResultSet result = statement.executeQuery(query);   
        result.next();
        int i = result.getInt(1);
        Random rand = new Random();
         i = rand.nextInt(i - 0 + 1);
        System.out.println("random count : " + i);
        
        /*
        select random nummered cities
        */
        query = "SELECT * FROM cities";
        result = statement.executeQuery(query);
        result.next();
        for(int j = 0; j <= i ; j++){
            result.next();
        } 
        
        /*
        Send the update city with visitingbonus
        */
        query = makeQuery("UPDATE cities SET visitbonus = '1000' WHERE country = % AND name = %",
                new String[]{result.getString("country"),result.getString("name")});
        System.out.println("Sending query :" + query);
        
        statement.executeUpdate(query);
    }
    
    void resetDatabase(Connection conn) throws SQLException {
        statement = conn.createStatement();
        String query = "truncate countries cascade";
        statement.executeUpdate(query);
        System.out.println("Resetting database");
    }

    /* This function should print the winner of the game based on the currently highest budget.
      */
    void announceWinner(Connection conn) throws SQLException {
        statement = conn.createStatement();
        String query = "SELECT * FROM persons";
        System.out.println("Query sendt : " + query);
        ResultSet result = statement.executeQuery(query);
        result.next();
        
        ArrayList<ResultSet> resultList = new ArrayList<ResultSet>();
        ResultSet currentWinner = result;
        result.next();
        while(result.next()){
            if(result.getString("budget") == currentWinner.getString("budget"))
                resultList.add(result);
            else if (Integer.parseInt(result.getString("budget")) > Integer.parseInt(result.getString(currentWinner.getString("budget"))))
                currentWinner = result;
        }
        
        resultList.add(currentWinner);
        
        System.out.println("The winner iz: ");
        for(ResultSet rS: resultList){
            System.out.println(rS.getString("name") + " with personnummer: " + rS.getString("personnummer") + " from :" + rS.getString("country"));
        }
        
    }

    void play (String worldfile) throws IOException {

        // Read username and password from config.cfg
        try {
            BufferedReader nf = new BufferedReader(new FileReader("config.cfg"));
            String line;
            if ((line = nf.readLine()) != null) {
                USERNAME = line;
            }
            if ((line = nf.readLine()) != null) {
                PASSWORD = line;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (USERNAME.equals("USERNAME") || PASSWORD.equals("PASSWORD")) {
            System.out.println("CONFIG FILE HAS WRONG FORMAT");
            return;
        }

        try {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            System.out.println("un: "+ USERNAME + " pw: " + PASSWORD);
            String url = "jdbc:postgresql://ate.ita.chalmers.se/";
            Properties props = new Properties();
            props.setProperty("user",USERNAME);
            props.setProperty("password",PASSWORD);

            final Connection conn = DriverManager.getConnection(url, props);
            resetDatabase(conn);

			/* This block creates the government entry and the necessary
			 * country and area for that.
			 */
            try {
                PreparedStatement statement = conn.prepareStatement("INSERT INTO Countries (name) VALUES (?)");
                statement.setString(1, "");
                statement.executeUpdate();
                statement = conn.prepareStatement("INSERT INTO Areas (country, name, population) VALUES (?, ?, cast(? as INT))");
                statement.setString(1, "");
                statement.setString(2, "");
                statement.setString(3, "1");
                statement.executeUpdate();
                statement = conn.prepareStatement("INSERT INTO Persons (country, personnummer, name, locationcountry, locationarea, budget) VALUES (?, ?, ?, ?, ?, cast(? as NUMERIC))");
                statement.setString(1, "");
                statement.setString(2, "");
                statement.setString(3, "Government");
                statement.setString(4, "");
                statement.setString(5, "");
                statement.setString(6, "0");
                statement.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

            // Initialize the database from the worldfile
            try {
                BufferedReader br = new BufferedReader(new FileReader(worldfile));
                String line;
                while ((line = br.readLine()) != null) {
                    
                System.out.println(line);
                    String[] cmd = line.split(" +");
                    if ("ROAD".equals(cmd[0]) && (cmd.length == 5)) {
                        insertRoad(conn, cmd[1], cmd[2], cmd[3], cmd[4]);
                    } else if ("TOWN".equals(cmd[0]) && (cmd.length == 4)) {
						/* Create an area and a town entry in the database */
                        insertTown(conn, cmd[1], cmd[2], cmd[3]);
                    } else if ("CITY".equals(cmd[0]) && (cmd.length == 4)) {
						/* Create an area and a city entry in the database */
                        insertCity(conn, cmd[1], cmd[2], cmd[3]);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            ArrayList<Player> players = new ArrayList<Player>();

            while(true) {
                optionssetup();
                String mode = readLine("? > ");
                String[] cmd = mode.split(" +");
                cmd[0] = cmd[0].toLowerCase();
                
                if ("new player".startsWith(cmd[0]) && (cmd.length == 5)) {
                    
                    System.out.println("Cmd input as: ");
                    for(String s : cmd)
                        System.out.print(s + "   :   ");
                    System.out.println("Cmd input end ----------------");
                    
                    Player nextplayer = new Player(cmd[1], cmd[2], cmd[3], cmd[4]);
                    System.out.println("Player try add: " + cmd[1]+ cmd[2]+ cmd[3]+ cmd[4]);
                    
                    if (createPlayer(conn, nextplayer) == 1) {
                        players.add(nextplayer);
                        System.out.println("Player added: " + nextplayer);
                    }
                } else if ("done".startsWith(cmd[0]) && (cmd.length == 1)) {
                    break;
                } else {
                    System.out.println("\nInvalid option.");
                }
            }

            System.out.println("\nGL HF!");
            int roundcounter = 1;
            int maxrounds = 5;
            while(roundcounter <= maxrounds) {
                System.out.println("\nWe are starting the " + roundcounter + ". round!!!");
				/* for each player from the playerlist */
                for (int i = 0; i < players.size(); ++i) {
                    System.out.println("\nIt's your turn " + players.get(i).playername + "!");
                    System.out.println("You are currently located in " + getCurrentArea(conn, players.get(i)) + " (" + getCurrentCountry(conn, players.get(i)) + ")");
                    while (true) {
                        options();
                        String mode = readLine("? > ");
                        String[] cmd = mode.split(" +");
                        cmd[0] = cmd[0].toLowerCase();
                        if ("next moves".startsWith(cmd[0]) && (cmd.length == 1 || cmd.length == 3)) {
							/* Show next moves from a location or current location. Turn continues. */
                            if (cmd.length == 1) {
                                String area = getCurrentArea(conn, players.get(i));
                                String country = getCurrentCountry(conn, players.get(i));
                                getNextMoves(conn, players.get(i));
                            } else {
                                getNextMoves(conn, players.get(i), cmd[1], cmd[2]);
                            }
                        } else if ("list properties".startsWith(cmd[0]) && (cmd.length == 1 || cmd.length == 3)) {
							/* List properties of a player. Can be a specified player
							   or the player himself. Turn continues. */
                            if (cmd.length == 1) {
                                listProperties(conn, players.get(i));
                            } else {
                                listProperties(conn, cmd[1], cmd[2]);
                            }
                        } else if ("scores".startsWith(cmd[0]) && cmd.length == 1) {
							/* Show scores for all players. Turn continues. */
                            showScores(conn);
                        } else if ("players".startsWith(cmd[0]) && cmd.length == 1) {
							/* Show scores for all players. Turn continues. */
                            System.out.println("\nPlayers:");
                            for (int k = 0; k < players.size(); ++k) {
                                System.out.println("\t" + players.get(k).playername + ": " + players.get(k).personnummer + " (" + players.get(k).country + ") ");
                            }
                        } else if ("refund".startsWith(cmd[0]) && (cmd.length == 3 || cmd.length == 5)) {
                            if (cmd.length == 5) {
								/* Sell road from arguments. If no road was sold the turn
								   continues. Otherwise the turn ends. */
                                if (sellRoad(conn, players.get(i), cmd[1], cmd[2], cmd[3], cmd[4]) == 1) {
                                    break;
                                } else {
                                    System.out.println("\nTry something else.");
                                }
                            } else {
								/* Sell hotel from arguments. If no hotel was sold the turn
								   continues. Otherwise the turn ends. */
                                if (sellHotel(conn, players.get(i), cmd[1], cmd[2]) == 1) {
                                    break;
                                } else {
                                    System.out.println("\nTry something else.");
                                }
                            }
                        } else if ("buy".startsWith(cmd[0]) && (cmd.length == 4 || cmd.length == 5)) {
                            if (cmd.length == 5) {
								/* Buy road from arguments. If no road was bought the turn
								   continues. Otherwise the turn ends. */
                                if (buyRoad(conn, players.get(i), cmd[1], cmd[2], cmd[3], cmd[4]) == 1) {
                                    break;
                                } else {
                                    System.out.println("\nTry something else.");
                                }
                            } else {
								/* Buy hotel from arguments. If no hotel was bought the turn
								   continues. Otherwise the turn ends. */
                                if (buyHotel(conn, players.get(i), cmd[1], cmd[2], cmd[3]) == 1) {
                                    break;
                                } else {
                                    System.out.println("\nTry something else.");
                                }
                            }
                        } else if ("move".startsWith(cmd[0]) && cmd.length == 3) {
							/* Change the location of the player to the area from the arguments.
							   If the move was legal the turn ends. Otherwise the turn continues. */
                            if (changeLocation(conn, players.get(i), cmd[1], cmd[2]) == 1) {
                                break;
                            } else {
                                System.out.println("\nTry something else.");
                            }
                        } else if ("quit".startsWith(cmd[0]) && cmd.length == 1) {
							/* End the move of the player without any action */
                            break;
                        } else {
                            System.out.println("\nYou chose an invalid option. Try again.");
                        }
                    }
                }
                setVisitingBonus(conn);
                ++roundcounter;
            }
            announceWinner(conn);
            System.out.println("\nGG!\n");
            
            resetDatabase(conn);

            conn.close();
        } catch (SQLException e) {
            System.err.println(e);
            System.exit(2);
        }
    }

    private String readLine(String s) throws IOException {
        System.out.print(s);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        char c;
        StringBuilder stringBuilder = new StringBuilder();
        do {
            c = (char) bufferedReader.read();
            stringBuilder.append(c);
        } while(String.valueOf(c).matches(".")); // Without the DOTALL switch, the dot in a java regex matches all characters except newlines

        System.out.println("");
        stringBuilder.deleteCharAt(stringBuilder.length()-1);

        return stringBuilder.toString();
    }

    /* main: parses the input commands.
     * /!\ You don't need to change this function! */
    public static void main(String[] args) throws Exception
    {
        String worldfile = args[0];
        Game g = new Game();
        g.play(worldfile);
    }
    
}