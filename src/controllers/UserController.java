package controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;

import Dao.DBConnect;
import application.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import models.Tickets;

public class UserController implements Initializable{

	@FXML
	private Pane pane1;
	@FXML
	private Pane pane2;
	@FXML
	private Pane pane3;

	@FXML
	private TextField issueC;
	@FXML
	private TextField issueU;

	@FXML
	private TextField assignC;
	@FXML
	private TextField assignU;

	@FXML
	private TextField desC;
	@FXML
	private TextField desU;

	@FXML
	private TextField idU;


	@FXML
	private ChoiceBox<String> statusU;
	@FXML
	private ChoiceBox<String> statusC;

	@FXML
	private Label msgC;
	@FXML
	private Label msgU;
	
	static String userid;


	// Declare DB objects
	DBConnect conn = null;
	Statement stmt = null;

	public UserController() {
		conn = new DBConnect();
	}


	public void updateTickets() {

		pane1.setVisible(false);
		pane2.setVisible(true);
		pane3.setVisible(false);

	}



	public void addTicket() {

		pane1.setVisible(true);
		pane2.setVisible(false);
		pane3.setVisible(false);

	}

	public void listTickets() {

		pane1.setVisible(false);
		pane2.setVisible(false);
		pane3.setVisible(true);
		try {
			viewTickets();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/***** TABLEVIEW intel *********************************************************************/

	@FXML
	private TableView<Tickets> tblIssues;
	@FXML
	private TableColumn<Tickets, String> id;
	@FXML
	private TableColumn<Tickets, String> issue;
	@FXML
	private TableColumn<Tickets, String> assigned;
	@FXML
	private TableColumn<Tickets, String> status;


	public void initialize(URL location, ResourceBundle resources) {

		id.setCellValueFactory(new PropertyValueFactory<Tickets, String>("id"));
		issue.setCellValueFactory(new PropertyValueFactory<Tickets, String>("issue"));
		assigned.setCellValueFactory(new PropertyValueFactory<Tickets, String>("assigned"));
		status.setCellValueFactory(new PropertyValueFactory<Tickets, String>("status"));
		// auto adjust width of columns depending on their content
		tblIssues.setColumnResizePolicy((param) -> true);
		Platform.runLater(() -> customResize(tblIssues));

		tblIssues.setVisible(false); // set invisible initially
	}

	public void customResize(TableView<?> view) {

		AtomicLong width = new AtomicLong();
		view.getColumns().forEach(col -> {
			width.addAndGet((long) col.getWidth());
		});
		double tableWidth = view.getWidth();

		if (tableWidth > width.get()) {
			view.getColumns().forEach(col -> {
				col.setPrefWidth(col.getWidth()+((tableWidth-width.get())/view.getColumns().size()));
			});
		}
	}

	public void viewTickets() throws IOException {

		tblIssues.getItems().setAll(getTickts()); // load table data from ClientModel List
		tblIssues.setVisible(true); // set tableview to visible if not
	}
	/***** End TABLEVIEW intel *********************************************************************/

	public void createTicket() {

		msgC.setText("");
		String issue = this.issueC.getText();
		String assign = this.assignC.getText();
		String desc = this.desC.getText();
		String status = this.statusC.getValue();
		// Validations
		if (issue == null || issue.trim().equals("")) {
			msgC.setText("Issue Cannot be empty or spaces");
			return;
		}
		if (assign == null || assign.trim().equals("")) {
			msgC.setText("Assigned Cannot be empty or spaces");
			return;
		}
		if (desc == null || desc.trim().equals("")) {
			msgC.setText("Description Cannot be empty or spaces");
			return;
		}


		// INSERT INTO Tickets TABLE
		try {
			// Execute a query
			System.out.println("Inserting records into the table...");
			// Include all object data to the database table
			String generatedColumns[] = { "id" };

			PreparedStatement stmt = conn.getConnection().prepareStatement("INSERT into ms_tickets (issue,status,description,assign) values (?,?,?,?)",generatedColumns);

			stmt.setString(1, issue);
			stmt.setString(2, status);
			stmt.setString(3, desc);
			stmt.setString(4, assign);
			stmt.executeUpdate();

			ResultSet rs = stmt.getGeneratedKeys();

			if (rs.next()) {
				long id = rs.getLong(1);
				msgC.setText("New Ticket with ID:"+id+" is Created"); // display inserted record
			}

			System.out.println("Ticket created");


		} catch (SQLException se) {
			se.printStackTrace();
		}
	}


	public void updateTicket() {


		msgU.setText("");
		String id = this.idU.getText();
		String issue = this.issueU.getText();
		String assign = this.assignU.getText();
		String desc = this.desU.getText();
		String status = this.statusU.getValue();

		// Validations
		if (id == null || id.trim().equals("")) {
			msgU.setText("Ticket ID Cannot be empty or spaces");
			return;
		}
		if (issue == null || issue.trim().equals("")) {
			msgU.setText("Issue Cannot be empty or spaces");
			return;
		}
		if (assign == null || assign.trim().equals("")) {
			msgU.setText("Assigned Cannot be empty or spaces");
			return;
		}
		if (desc == null || desc.trim().equals("")) {
			msgU.setText("Description Cannot be empty or spaces");
			return;
		}



		// INSERT INTO USERS TABLE
		try {
			// Execute a query
			System.out.println("updating records into the table...");

			// Include all object data to the database table

			PreparedStatement stmt = conn.getConnection().prepareStatement("UPDATE ms_tickets SET issue=? ,assign= ? ,status=? ,description=?  WHERE id=?");

			stmt.setString(1, issue);
			stmt.setString(2, assign);
			stmt.setString(3, status);
			stmt.setString(4, desc);
			stmt.setString(5, id);
			stmt.executeUpdate();

			msgU.setText("User with Ticket ID:"+id+" is Updated."); // display inserted record

			System.out.println("Ticket Updated");


		} catch (SQLException se) {
			se.printStackTrace();
		}
	}

	public void getTicket() {


		msgU.setText("");
		String ticketID = this.idU.getText();


		// Validations
		if (ticketID == null || ticketID.trim().equals("")) {
			msgU.setText("Ticket ID Cannot be empty or spaces");
			return;
		}


		ResultSet rs=null;

		try {
			// Execute a query
			System.out.println("Geting record from the table ticket for udpate...");

			// Include all object data to the database table

			PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT issue,assign,status,description FROM ms_tickets where id=?");
			stmt.setString(1, ticketID);
			rs=stmt.executeQuery();  

			while (rs.next()) {
				this.issueU.setText(rs.getString("issue"));
				this.assignU.setText(rs.getString("assign"));
				this.statusU.setValue(rs.getString("status"));
				this.desU.setText(rs.getString("description"));
			}

			System.out.println("got  ticket");


		} catch (SQLException se) {
			se.printStackTrace();
		}
	}

	public List<Tickets>  getTickts() {

		List<Tickets> tickets = new ArrayList<>();
		ResultSet rs=null;
		// SELECT ALL FROM TICKETS TABLE
		try {
			// Execute a query
			System.out.println("select records from the table...");

			// Include all object data to the database table

			PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT id,issue,assign,status FROM ms_tickets WHERE assign=?");

			stmt.setString(1, userid); 
			
			rs=stmt.executeQuery();  

			while (rs.next()) {
				Tickets ticket = new Tickets();

				// grab record data by table field name into user object
				ticket.setId(rs.getString("id"));
				ticket.setIssue(rs.getString("issue"));
				ticket.setAssigned(rs.getString("assign"));
				ticket.setStatus(rs.getString("status"));

				tickets.add(ticket); // add tickets data to arraylist
			}

			System.out.println("Got tickets list");

		} catch (SQLException se) {
			se.printStackTrace();
		}
		return tickets;
	}

	public static void setUserid(String user_id) {
		userid = user_id;
		
	}


	public void logout() {
		// System.exit(0);
		try {
			AnchorPane root = (AnchorPane) FXMLLoader.load(getClass().getResource("/views/LoginView.fxml"));
			Scene scene = new Scene(root);
			//scene.getStylesheets().add(getClass().getResource("/application/styles.css").toExternalForm());
			Main.stage.setScene(scene);
			Main.stage.setTitle("Login");
		} catch (Exception e) {
			System.out.println("Error occured while inflating view: " + e);
		}
	}

}
