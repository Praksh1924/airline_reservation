import java.sql.*;
import java.util.*;

class Flight {
    private String flightNumber;
    private String origin;
    private String destination;
    private int availableSeats;

    public Flight(String flightNumber, String origin, String destination, int availableSeats) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.availableSeats = availableSeats;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void bookSeat() {
        if (availableSeats > 0) {
            availableSeats--;
            System.out.println("Seat booked successfully.");
        } else {
            System.out.println("No available seats on this flight.");
        }
    }
}

class Passenger {
    private String name;
    private String passportNumber;

    public Passenger(String name, String passportNumber) {
        this.name = name;
        this.passportNumber = passportNumber;
    }

    public String getName() {
        return name;
    }

    public String getPassportNumber() {
        return passportNumber;
    }
}

class Reservation {
    private Flight flight;
    private Passenger passenger;

    public Reservation(Flight flight, Passenger passenger) {
        this.flight = flight;
        this.passenger = passenger;
    }

    public void displayReservationDetails() {
        System.out.println("Flight Number: " + flight.getFlightNumber());
        System.out.println("Origin: " + flight.getOrigin());
        System.out.println("Destination: " + flight.getDestination());
        System.out.println("Passenger Name: " + passenger.getName());
        System.out.println("Passenger Passport Number: " + passenger.getPassportNumber());
    }
}

public class AirlineReservationSystem {
    private Connection connection;

    public AirlineReservationSystem() {
        try {
            // Connect to the database
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/airline", "root", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addFlight(Flight flight) {
        try {
            // Insert the flight into the database
            PreparedStatement statement = connection.prepareStatement("INSERT INTO flights (flight_number, origin, destination, available_seats) VALUES (?, ?, ?, ?)");
            statement.setString(1, flight.getFlightNumber());
            statement.setString(2, flight.getOrigin());
            statement.setString(3, flight.getDestination());
            statement.setInt(4, flight.getAvailableSeats());
            statement.executeUpdate();
            System.out.println("Flight added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Flight findFlight(String flightNumber) {
        try {
            // Retrieve the flight from the database
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM flights WHERE flight_number = ?");
            statement.setString(1, flightNumber);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String origin = resultSet.getString("origin");
                String destination = resultSet.getString("destination");
                int availableSeats = resultSet.getInt("available_seats");
                return new Flight(flightNumber, origin, destination, availableSeats);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void makeReservation(Flight flight, Passenger passenger) {
        try {
            // Check if the flight has available seats
            if (flight.getAvailableSeats() > 0) {
                // Insert the reservation into the database
                PreparedStatement statement = connection.prepareStatement("INSERT INTO reservations (flight_number, passenger_name, passport_number) VALUES (?, ?, ?)");
                statement.setString(1, flight.getFlightNumber());
                statement.setString(2, passenger.getName());
                statement.setString(3, passenger.getPassportNumber());
                statement.executeUpdate();

                // Update the available seats of the flight
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE flights SET available_seats = ? WHERE flight_number = ?");
                updateStatement.setInt(1, flight.getAvailableSeats() - 1);
                updateStatement.setString(2, flight.getFlightNumber());
                updateStatement.executeUpdate();

                System.out.println("Reservation successful.");
            } else {
                System.out.println("No available seats on this flight.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayReservations() {
        try {
            // Retrieve all reservations from the database
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM reservations");

            while (resultSet.next()) {
                String flightNumber = resultSet.getString("flight_number");
                String passengerName = resultSet.getString("passenger_name");
                String passportNumber = resultSet.getString("passport_number");

                // Retrieve the flight and passenger details
                Flight flight = findFlight(flightNumber);
                Passenger passenger = new Passenger(passengerName, passportNumber);

                // Create and display the reservation
                Reservation reservation = new Reservation(flight, passenger);
                reservation.displayReservationDetails();
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AirlineReservationSystem airlineSystem = new AirlineReservationSystem();

        // Create sample flights
        Flight flight1 = new Flight("F001", "City A", "City B", 50);
        Flight flight2 = new Flight("F002", "City B", "City C", 30);
        Flight flight3 = new Flight("F003", "City A", "City C", 20);

        // Add flights to the system
        airlineSystem.addFlight(flight1);
        airlineSystem.addFlight(flight2);
        airlineSystem.addFlight(flight3);

        // Create a sample passenger
        Passenger passenger = new Passenger("John Doe", "AB123456");

        // Make a reservation
        airlineSystem.makeReservation(flight1, passenger);

        // Display all reservations
        airlineSystem.displayReservations();
    }
}
