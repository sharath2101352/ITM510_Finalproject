package controllers;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import application.Main;
import models.Alerts;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import models.LoginModel;
import models.StatsModel;
import util.Simulator;


public class LoginController {

	@FXML
	private TextField txtUsername;

	@FXML
	private PasswordField txtPassword;

	@FXML
	private Label lblError;

	private LoginModel model;

	public LoginController() {
		model = new LoginModel();
	}

	private ScheduledExecutorService scheduledExecutorService;
	final int WINDOW_SIZE = 20;
	LocalDate fromDate = LocalDate.now().minusDays(1);  
	LocalDate toDate = LocalDate.now();  
	StatsModel dao = new StatsModel();

	public void login() {

		lblError.setText("");
		String username = this.txtUsername.getText();
		String password = this.txtPassword.getText();

		// Validations
		if (username == null || username.trim().equals("")) {
			lblError.setText("Username Cannot be empty or spaces");
			return;
		}
		if (password == null || password.trim().equals("")) {
			lblError.setText("Password Cannot be empty or spaces");
			return;
		}
		if (username == null || username.trim().equals("") && (password == null || password.trim().equals(""))) {
			lblError.setText("User name / Password Cannot be empty or spaces");
			return;
		}

		// authentication check
		checkCredentials(username, password);

	}

	public void checkCredentials(String username, String password) {
		Boolean isValid = model.getCredentials(username, password);
		if (!isValid) {
			lblError.setText("User does not exist!");
			return;
		}
		try {
			AnchorPane root;
			Scene scene;
			if (model.role().equals("ADMIN") && isValid) {
				// If user is admin, inflate admin view

				root = (AnchorPane) FXMLLoader.load(getClass().getResource("/views/AdminView.fxml"));

				Main.stage.setTitle("Admin View");
				scene = new Scene(root);
				Main.stage.setScene(scene);

			} else if(model.role().equals("MANAGER") && isValid) {
				// If user is manager, inflate manager view
				root = (AnchorPane) FXMLLoader.load(getClass().getResource("/views/ManagerView.fxml"));
				Main.stage.setTitle("Manager View");
				scene = new Scene(root);
				Main.stage.setScene(scene);

			} else {
				UserController.setUserid(username);
				//for live stats simmulator
				Timer timer = new Timer();
				timer.schedule(new Simulator(), 0, 8000);
				
				// If user is normal, inflate user view
				Main.stage.setTitle("User View");
				Main.stage.setScene(userTabs());
			}




		} catch (Exception e) {
			System.out.println("Error occured while inflating view: " + e);
		}

	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Scene userTabs() {


		final CategoryAxis xAxis2 = new CategoryAxis();
		xAxis2.setLabel("Time/s");
		xAxis2.setAnimated(false);
		final NumberAxis yAxis2 = new NumberAxis();
		yAxis2.setLabel("Failed Orders");
		yAxis2.setAnimated(false);

		final StackedAreaChart failedOrdersChart = new StackedAreaChart(xAxis2, yAxis2);
		failedOrdersChart.setAnimated(false);
		XYChart.Series failedOrdesSeries = new XYChart.Series();
		failedOrdesSeries.setName("Failed Orders");

		failedOrdersChart.getData().add(failedOrdesSeries);


		VBox failedOrdersLayout = new VBox(failedOrdersChart);
		failedOrdersLayout.setMaxHeight(600);
		failedOrdersLayout.setMaxWidth(1000);


		//defining the axes
		final CategoryAxis xAxisReal = new CategoryAxis(); // we are gonna plot against time
		final NumberAxis yAxisReal = new NumberAxis();
		xAxisReal.setLabel("Time/s");
		xAxisReal.setAnimated(false); // axis animations are removed
		yAxisReal.setLabel("New Orders");
		yAxisReal.setAnimated(false); // axis animations are removed

		//creating the line chart with two axis created above
		final LineChart<String, Number> newOrderslineChart = new LineChart<>(xAxisReal, yAxisReal);
		newOrderslineChart.setTitle("Realtime Orders");
		newOrderslineChart.setAnimated(false); // disable animations

		//defining a series to display data
		XYChart.Series<String, Number> orderSeries = new XYChart.Series<>();
		orderSeries.setName("Orders Live");

		// add series to chart
		newOrderslineChart.getData().add(orderSeries);

		VBox newOrderslayout = new VBox(newOrderslineChart);
		newOrderslayout.setMaxHeight(600);
		newOrderslayout.setMaxWidth(800);

		FlowPane liveflowpane = new FlowPane();
		liveflowpane.setPadding(new Insets(5, 0, 5, 80));
		liveflowpane.setVgap(4);
		liveflowpane.setHgap(4);
		liveflowpane.setPrefWrapLength(600); // preferred width allows for two columns
		liveflowpane.setAlignment(Pos.CENTER);
		liveflowpane.getChildren().add(newOrderslayout);
		liveflowpane.getChildren().add(failedOrdersLayout);

		TilePane realTimePane = new TilePane();

		realTimePane.getChildren().add(liveflowpane);

		// this is used to display time in HH:mm:ss format
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

		// setup a scheduled executor to periodically put data into the chart
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

		// put dummy data onto graph per second

		scheduledExecutorService.scheduleAtFixedRate(() -> {
			// get a random integer between 0-10

			try {
				ResultSet rs = dao.retrieveLiveStats();
				if(rs.next()){

					final int orders = (int) rs.getInt(1);
					final int failed = (int) rs.getInt(2);

					if(orders<5) {
						dao.createOrderAlert("Drop is new orders.","OPEN");
					}

					if(failed>3) {
						dao.createOrderAlert("Failed orders increased.","OPEN");			
					}

					// Update the chart
					Platform.runLater(() -> {
						// get current time
						Date now = new Date();

						// put random number with current time
						orderSeries.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), orders));
						failedOrdesSeries.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), failed));
						if (orderSeries.getData().size() > WINDOW_SIZE)
							orderSeries.getData().remove(0);
						if (failedOrdesSeries.getData().size() > WINDOW_SIZE)
							failedOrdesSeries.getData().remove(0);
					});
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // retrive resultset from db

		}, 0, 9, TimeUnit.SECONDS);


		/** start tab 3 Tickets**/
		TableView tableView = new TableView();

		TableColumn<Alerts, String> column1 = new TableColumn<>("ID");
		column1.setCellValueFactory(new PropertyValueFactory<>("id"));
		column1.setMinWidth(100);
		TableColumn<Alerts, String> column2 = new TableColumn<>("Alert");
		column2.setCellValueFactory(new PropertyValueFactory<>("alert"));
		column2.setMinWidth(350);
		TableColumn<Alerts, String> column3 = new TableColumn<>("Created");
		column3.setCellValueFactory(new PropertyValueFactory<>("createdTimestamp"));
		column2.setMinWidth(200);
		TableColumn<Alerts, String> column4 = new TableColumn<>("Status");
		column4.setCellValueFactory(new PropertyValueFactory<>("status"));
		column4.setMinWidth(100);


		TableColumn<Alerts, Void> colBtn = new TableColumn("Close Alert");

		Callback<TableColumn<Alerts, Void>, TableCell<Alerts, Void>> cellFactory = new Callback<TableColumn<Alerts, Void>, TableCell<Alerts, Void>>() {
			@Override
			public TableCell<Alerts, Void> call(final TableColumn<Alerts, Void> param) {
				final TableCell<Alerts, Void> cell = new TableCell<Alerts, Void>() {

					private final Button btn = new Button("Close");

					{
						btn.setOnAction((ActionEvent event) -> {
							Alerts data = getTableView().getItems().get(getIndex());
							data.setStatus("CLOSED");
							dao.closeAlert(data.getId());
							System.out.println("selectedData: " + data.getId());
							tableView.getItems().setAll(dao.getAlerts());
						});
					}

					@Override
					public void updateItem(Void item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
						} else {
							setGraphic(btn);
						}
					}
				};
				return cell;
			}
		};

		colBtn.setCellFactory(cellFactory);
		colBtn.setMinWidth(100);
		tableView.setMinHeight(850.00);
		tableView.getColumns().add(colBtn);

		tableView.getColumns().add(column1);
		tableView.getColumns().add(column2);
		tableView.getColumns().add(column3);
		tableView.getColumns().add(column4);


		tableView.setPlaceholder(new Label("No rows to display"));

		tableView.getItems().setAll(dao.getAlerts());


		VBox ticketsList = new VBox(tableView);
		/** End tab 3 Tickets**/

		/** start tab 1 Alerts**/
		/** end tab 1 Alerts**/

		/** start tab 4 analytics**/
		// TAB ANALYICS PIE AND ABR CHART WITH DATE PICKER AND BUTTON
		TilePane analyicsTilePane = new TilePane();

		// create a button 
		Button b = new Button("Submit"); 

		// create a date picker 
		DatePicker fd = new DatePicker(); 

		// action event 
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e) 
			{ 
				// get the date picker value 
				fromDate = fd.getValue();
			} 
		};

		// when datePicker is pressed 
		fd.setOnAction(event); 

		// create a date picker 
		DatePicker td = new DatePicker(); 

		// action event 
		EventHandler<ActionEvent> event2 = new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e) 
			{ 
				// get the date picker value 
				toDate = td.getValue();
			} 
		};

		// when datePicker is pressed 
		td.setOnAction(event2); 

		PieChart pieChart = new PieChart();
		XYChart.Series applePay = new XYChart.Series();
		XYChart.Series creditCard = new XYChart.Series();
		XYChart.Series cashDelivery = new XYChart.Series();
		ResultSet rs;
		Timestamp fromTime = Timestamp.valueOf(fromDate.atStartOfDay());
		Timestamp toTime = Timestamp.valueOf(toDate.atStartOfDay());
		try {

			rs = dao.retrieveAnalyticsStats(fromTime, toTime);

			if(rs.next()){

				PieChart.Data slice1 = new PieChart.Data("Desktop", rs.getInt(2));
				PieChart.Data slice2 = new PieChart.Data("Mobile"  , rs.getInt(1));
				PieChart.Data slice3 = new PieChart.Data("Store" , rs.getInt(3));

				pieChart.getData().add(slice1);
				pieChart.getData().add(slice2);
				pieChart.getData().add(slice3);
				pieChart.setTitle("Sales by Channel Type");

				VBox channelType = new VBox(pieChart);
				channelType.setMaxHeight(600);
				channelType.setMaxWidth(800);

				CategoryAxis xAxis    = new CategoryAxis();
				xAxis.setLabel("Payment Type");

				NumberAxis yAxis = new NumberAxis();
				yAxis.setLabel("Sales");

				BarChart barChart = new BarChart(xAxis, yAxis);

				applePay.getData().add(new XYChart.Data("Apple Pay", rs.getInt(5)));
				applePay.setName("ApplePay");
				creditCard.getData().add(new XYChart.Data("Credit Card", rs.getInt(4)));
				creditCard.setName("Credit Card");
				cashDelivery.getData().add(new XYChart.Data("Cash on Delivery", rs.getInt(6)));
				cashDelivery.setName("Cash on Delivery");

				barChart.getData().add(applePay);
				barChart.getData().add(creditCard);
				barChart.getData().add(cashDelivery);
				barChart.setTitle("Sales by Payment Type");

				VBox paymentMethods = new VBox(barChart);
				paymentMethods.setMaxHeight(600);
				paymentMethods.setMaxWidth(800);

				HBox hboxdateForm = new HBox();
				hboxdateForm.setPadding(new Insets(15, 12, 15, 12));
				hboxdateForm.setSpacing(10);
				hboxdateForm.setAlignment(Pos.TOP_LEFT);
				hboxdateForm.getChildren().addAll(fd, td,b);

				FlowPane analyticsflowpane = new FlowPane();
				analyticsflowpane.setPadding(new Insets(5, 0, 5, 150));
				analyticsflowpane.setVgap(4);
				analyticsflowpane.setHgap(4);
				analyticsflowpane.setPrefWrapLength(500); // preferred width allows for two columns
				analyticsflowpane.setAlignment(Pos.CENTER);
				analyticsflowpane.getChildren().add(hboxdateForm);
				analyticsflowpane.getChildren().add(channelType);
				analyticsflowpane.getChildren().add(paymentMethods);


				analyicsTilePane.getChildren().add(analyticsflowpane);

			}

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// action event
		EventHandler<ActionEvent> eventb = new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e) 
			{ 
				try {
					Timestamp fromTime = Timestamp.valueOf(fromDate.atStartOfDay());
					Timestamp toTime = Timestamp.valueOf(toDate.atStartOfDay());

					ResultSet newrs = dao.retrieveAnalyticsStats(fromTime, toTime);
					if(newrs.next()){


						pieChart.getData().removeAll(pieChart.getData());
						pieChart.getData().add(new PieChart.Data("Desktop", newrs.getInt(2)));
						pieChart.getData().add(new PieChart.Data("Mobile"  ,newrs.getInt(1)));
						pieChart.getData().add(new PieChart.Data("Store" , newrs.getInt(3)));

						applePay.getData().removeAll(applePay.getData());
						creditCard.getData().removeAll(creditCard.getData());
						cashDelivery.getData().removeAll(cashDelivery.getData());
						applePay.getData().add(new XYChart.Data("Apple Pay", newrs.getInt(5)));
						creditCard.getData().add(new XYChart.Data("Credit Card", newrs.getInt(4)));
						cashDelivery.getData().add(new XYChart.Data("Cash on Delivery", newrs.getInt(6)));

					}
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} 
		};

		// when button is pressed 
		b.setOnAction(eventb);
		AnchorPane tickets = null;
		try {
			tickets = (AnchorPane) FXMLLoader.load(getClass().getResource("/views/ManagerView.fxml"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		TabPane tabPane = new TabPane();

		Tab tab1 = new Tab("Tickets", tickets);
		Tab tab2 = new Tab("RealTime"  , realTimePane);
		Tab tab3 = new Tab("Alerts" , ticketsList);
		Tab tab4 = new Tab("Analytics" , analyicsTilePane);

		tabPane.getTabs().add(tab2);
		tabPane.getTabs().add(tab4);
		tabPane.getTabs().add(tab3);
		tabPane.getTabs().add(tab1);


		VBox vBox = new VBox(tabPane);
		Scene scene = new Scene(vBox,800,900);


		return scene;
	}


}