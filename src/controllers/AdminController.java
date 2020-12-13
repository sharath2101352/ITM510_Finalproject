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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import models.Users;
import util.MD5Encript;

public class AdminController implements Initializable{

	@FXML
	private Pane pane1;
	@FXML
	private Pane pane2;
	@FXML
	private Pane pane3;
	@FXML
	private Pane pane4;
	@FXML
	private TextField fnU;
	@FXML
	private TextField lnU;
	@FXML
	private TextField emU;
	@FXML
	private TextField userIdD;

	@FXML
	private PasswordField pwU;
	@FXML
	private ChoiceBox<String> urU;
	@FXML
	private Label msgU;

	@FXML
	private TextField fn;
	@FXML
	private TextField ln;
	@FXML
	private TextField em;
	@FXML
	private TextField userIdU;


	@FXML
	private PasswordField pw;
	@FXML
	private ChoiceBox<String> ur;
	@FXML
	private Label msg;

	@FXML
	private Label msgD;



	// Declare DB objects
	DBConnect conn = null;
	Statement stmt = null;

	public AdminController() {
		conn = new DBConnect();
	}


	public void updateUser() {

		pane1.setVisible(false);
		pane2.setVisible(true);
		pane3.setVisible(false);
		pane4.setVisible(false);

	}

	public void deleteUser() {

		pane1.setVisible(false);
		pane2.setVisible(false);
		pane3.setVisible(true);
		pane4.setVisible(false);
	}

	public void addUser() {

		pane1.setVisible(true);
		pane2.setVisible(false);
		pane3.setVisible(false);
		pane4.setVisible(false);

	}

	public void auditUser() {

		pane1.setVisible(false);
		pane2.setVisible(false);
		pane3.setVisible(false);
		pane4.setVisible(true);
		try {
			viewAccounts();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/***** TABLEVIEW intel *********************************************************************/

	@FXML
	private TableView<Users> tblUsers;
	@FXML
	private TableColumn<Users, String> id;
	@FXML
	private TableColumn<Users, String> fname;
	@FXML
	private TableColumn<Users, String> lname;
	@FXML
	private TableColumn<Users, String> mail;
	@FXML
	private TableColumn<Users, String> role;

	public void initialize(URL location, ResourceBundle resources) {

		id.setCellValueFactory(new PropertyValueFactory<Users, String>("id"));
		fname.setCellValueFactory(new PropertyValueFactory<Users, String>("fname"));
		lname.setCellValueFactory(new PropertyValueFactory<Users, String>("lname"));
		mail.setCellValueFactory(new PropertyValueFactory<Users, String>("mail"));
		role.setCellValueFactory(new PropertyValueFactory<Users, String>("role"));
		// auto adjust width of columns depending on their content
		tblUsers.setColumnResizePolicy((param) -> true);
		Platform.runLater(() -> customResize(tblUsers));

		tblUsers.setVisible(false); // set invisible initially
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

	public void viewAccounts() throws IOException {

		tblUsers.getItems().setAll(getUserRec()); // load table data from ClientModel List
		tblUsers.setVisible(true); // set tableview to visible if not
	}
	/***** End TABLEVIEW intel *********************************************************************/

	public void createUser() {

		msg.setText("");
		String firstname = this.fn.getText();
		String lastname = this.ln.getText();
		String email = this.em.getText();
		String password = this.pw.getText();
		String role = this.ur.getValue();
		// Validations
		if (firstname == null || firstname.trim().equals("")) {
			msg.setText("First Name Cannot be empty or spaces");
			return;
		}
		if (lastname == null || lastname.trim().equals("")) {
			msg.setText("Last Name Cannot be empty or spaces");
			return;
		}
		if (email == null || email.trim().equals("")) {
			msg.setText("Email Cannot be empty or spaces");
			return;
		}
		if (password == null || password.trim().equals("")) {
			msg.setText("Password Cannot be empty or spaces");
			return;
		}

		// INSERT INTO USERS TABLE
		try {
			// Execute a query
			System.out.println("Inserting records into the table...");
			password = MD5Encript.passwordEncript(password);
			// Include all object data to the database table
			String generatedColumns[] = { "userId" };

			PreparedStatement stmt = conn.getConnection().prepareStatement("INSERT into ms_users (firstname,lastname,email,password,role) values (?,?,?,?,?)",generatedColumns);

			stmt.setString(1, firstname);
			stmt.setString(2, lastname);
			stmt.setString(3, email);
			stmt.setString(4, password);
			stmt.setString(5, role);
			stmt.executeUpdate();

			ResultSet rs = stmt.getGeneratedKeys();

			if (rs.next()) {
				long id = rs.getLong(1);
				msg.setText("New User with UseID:"+id+" is Created"); // display inserted record
			}

			System.out.println("User created");


		} catch (SQLException se) {
			se.printStackTrace();
		}
	}



	public void updateUserRec() {


		msgU.setText("");
		String userid = this.userIdU.getText();
		String firstname = this.fnU.getText();
		String lastname = this.lnU.getText();
		String email = this.emU.getText();
		String password = this.pwU.getText();
		String role = this.urU.getValue();

		// Validations
		if (userid == null || userid.trim().equals("")) {
			msgU.setText("UserId Cannot be empty or spaces");
			return;
		}
		if (firstname == null || firstname.trim().equals("")) {
			msgU.setText("First Name Cannot be empty or spaces");
			return;
		}
		if (lastname == null || lastname.trim().equals("")) {
			msgU.setText("Last Name Cannot be empty or spaces");
			return;
		}
		if (email == null || email.trim().equals("")) {
			msgU.setText("Email Cannot be empty or spaces");
			return;
		}
		if (password == null || password.trim().equals("")) {
			msgU.setText("Password Cannot be empty or spaces");
			return;
		}

		password = MD5Encript.passwordEncript(password);
		// INSERT INTO USERS TABLE
		try {
			// Execute a query
			System.out.println("updating records into the table...");

			// Include all object data to the database table


			PreparedStatement stmt = conn.getConnection().prepareStatement("UPDATE ms_users SET firstname=? ,lastname= ? ,email=? ,password=? ,role=? WHERE userId=?");

			stmt.setString(1, firstname);
			stmt.setString(2, lastname);
			stmt.setString(3, email);
			stmt.setString(4, password);
			stmt.setString(5, role);
			stmt.setString(6, userid);
			stmt.executeUpdate();

			msgU.setText("User with UseID:"+userid+" is Updated."); // display inserted record

			System.out.println("User Updated");


		} catch (SQLException se) {
			se.printStackTrace();
		}
	}

	public void deleteUserRec() {


		msgD.setText("");
		String userid = this.userIdD.getText();


		// Validations
		if (userid == null || userid.trim().equals("")) {
			msgD.setText("UserId Cannot be empty or spaces");
			return;
		}



		// INSERT INTO USERS TABLE
		try {
			// Execute a query
			System.out.println("deleting records from the table...");

			// Include all object data to the database table


			PreparedStatement stmt = conn.getConnection().prepareStatement("DELETE FROM ms_users WHERE userId=?");


			stmt.setString(1, userid);
			int i = stmt.executeUpdate();

			if(i>0)
				msgD.setText("User with UseID:"+userid+" is Deleted."); // display inserted record
			else
				msgD.setText("User with UseID:"+userid+" not found."); // display inserted record

			System.out.println("User Deleted");


		} catch (SQLException se) {
			se.printStackTrace();
		}
	}

	public List<Users>  getUserRec() {

		List<Users> users = new ArrayList<>();
		ResultSet rs=null;
		// SELECT ALL FROM USERS TABLE
		try {
			// Execute a query
			System.out.println("select records from the table...");

			// Include all object data to the database table

			PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT userId,firstname,lastname,email,role FROM ms_users");

			rs=stmt.executeQuery();  

			while (rs.next()) {
				Users user = new Users();

				// grab record data by table field name into user object
				user.setId(rs.getString("userId"));
				user.setFname(rs.getString("firstname"));
				user.setLname(rs.getString("lastname"));
				user.setMail(rs.getString("email"));
				user.setRole(rs.getString("role"));

				users.add(user); // add user data to arraylist
			}

			System.out.println("Got Users list");

		} catch (SQLException se) {
			se.printStackTrace();
		}
		return users;
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
