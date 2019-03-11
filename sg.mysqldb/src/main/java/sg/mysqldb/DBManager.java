package sg.mysqldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import sg.actuator.Actuator;
import sg.greenhouse.Greenhouse;
import sg.sensor.Sensor;

public class DBManager {

	final private String port = "localhost:8889/seas";
	final private String user = "root";
	final private String pwd = "root";
	final private String timeZone = "UTC";
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	public Connection getConnection(Connection connection) {

		try {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				System.out.println("Errore Connessione");
				e.printStackTrace();
			}

			connection = DriverManager.getConnection("jdbc:mysql://" + port + "?user=" + user + "&password=" + pwd
					+ "&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone="
					+ timeZone);
			// System.out.println("Connection established");

		} catch (Exception e) {
			System.out.println("SQLException: " + e.getMessage());
		}
		return connection;
	}

	// eliminare users??
	public void insertUser(String email, String pwd) {

		String sql = "INSERT INTO users (email, password) VALUES (?,?)";
		PreparedStatement ps = null;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);

			if (this.checkUser(email)) {

				// NON viene inserito l'user perchè l'e-mail è già esistente nel DB
			} else {

				// Viene inserito l'user
				ps = connection.prepareStatement(sql);
				ps.setString(1, email);
				ps.setString(2, pwd);

				System.out.println("User Inserted!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	public void deleteUser(Integer id) {

		String sql = "DELETE FROM users WHERE email = ?";
		String sqlSelect = "SELECT * FROM users WHERE id = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		String email = null;

		Connection connection = null;

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sqlSelect);
			ps.setInt(1, id);

			rs = ps.executeQuery();

			while (rs.next()) {
				email = rs.getString("email");
			}

			if (!this.checkUser(email)) {

				// NON cancello l'User perchè l'e-mail non esiste nel DB
				System.out.println("User NOT Deleted!");
			} else {

				ps = connection.prepareStatement(sql);
				ps.setString(1, email);

				System.out.println("User Deleted!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	public boolean checkUser(String email) {

		String sql = "SELECT * FROM users WHERE email = ?";
		PreparedStatement ps = null;
		Connection connection = null;
		ResultSet rs = null;

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);
			ps.setString(1, email);

			rs = ps.executeQuery();
			rs.last();

			if (rs.getRow() > 0) {
				System.out.println("User is in the DB!");
				return true;
			} else {
				System.out.println("User NOT in the Database");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	public void insertGreenhouse(String name, String plant, int optimal_temp, int optimal_hum, int optimal_light,
			int optimal_t_hum, int range_temp, int range_hum, int range_t_hum) {

		String sqlInsert = "INSERT INTO greenhouse (name, plant, opt_temp, opt_hum, opt_light, opt_t_hum,"
				+ "range_temp, range_hum, range_t_hum) VALUES (?,?,?,?,?,?,?,?,?)";
		PreparedStatement ps = null;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sqlInsert);
			ps.setString(1, name);
			ps.setString(2, plant);
			ps.setInt(3, optimal_temp);
			ps.setInt(4, optimal_hum);
			ps.setInt(5, optimal_light);
			ps.setInt(6, optimal_t_hum);
			ps.setInt(7, range_temp);
			ps.setInt(8, range_hum);
			ps.setInt(10, range_t_hum);

			System.out.println("Greenhouse Inserted!");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	public void deleteGreenhouse(int id) {

		String sqlInsert = "DELETE FROM greenhouse WHERE id = ?";
		PreparedStatement ps = null;
		int rows = 0;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sqlInsert);
			ps.setInt(1, id);

			rows = ps.executeUpdate();

			if (rows > 0) {

				System.out.println("Greenhouse Deleted!");
			} else {

				System.out.println("Greenhouse NOT Existing!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	public ArrayList<Greenhouse> selectAllGreenhouses() {

		System.out.println("DENTRO");
		String sql = "SELECT * FROM greenhouse";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		ArrayList<Greenhouse> greenhouses = new ArrayList<Greenhouse>();

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);

			rs = ps.executeQuery();

			while (rs.next()) {

				Greenhouse gh = new Greenhouse(rs.getInt("id"), rs.getString("name"), rs.getString("plant"),
						rs.getInt("opt_temp"), rs.getInt("opt_hum"), rs.getInt("opt_light"), rs.getInt("opt_t_hum"),
						rs.getInt("range_temp"), rs.getInt("range_hum"), rs.getInt("range_t_hum"));

				greenhouses.add(gh);
			}

			return greenhouses;
		} catch (SQLException e) {

			return greenhouses;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	public Greenhouse selectGreenhouseById(int id) {

		String sql = "SELECT * FROM greenhouse WHERE id = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		Greenhouse greenhouse = new Greenhouse();

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);

			rs = ps.executeQuery();

			while (rs.next()) {

				Greenhouse gh = new Greenhouse(rs.getInt("id"), rs.getString("name"), rs.getString("plant"),
						rs.getInt("opt_temp"), rs.getInt("opt_hum"), rs.getInt("opt_light"), rs.getInt("opt_t_hum"),
						rs.getInt("range_temp"), rs.getInt("range_hum"), rs.getInt("range_t_hum"));

				greenhouse = gh;
			}
			// System.out.println("GREENHOUSE: " + greenhouse.getName());
			return greenhouse;
		} catch (SQLException e) {

			return greenhouse;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	public ArrayList<Actuator> selectGreenhouseActuators(int id) {

		String sql = "SELECT * FROM actuator WHERE id_greenhouse = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		ArrayList<Actuator> actuators = new ArrayList<Actuator>();

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);

			rs = ps.executeQuery();

			while (rs.next()) {

				Actuator act = new Actuator();

				act.setId(rs.getInt("id"));
				act.setName(rs.getString("name"));
				act.setIdGreenhouse(rs.getInt("id_greenhouse"));
				act.setType(rs.getString("type"));
				act.setStatus(rs.getBoolean("status"));
				act.setPower(rs.getInt("power"));

				actuators.add(act);
				;
			}

			return actuators;
		} catch (SQLException e) {

			return actuators;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	public HashMap<String, Actuator> selectGreenhouseActuatorsType(int id) {

		String sql = "SELECT * FROM actuator WHERE id_greenhouse = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		HashMap<String, Actuator> actuators = new HashMap<String, Actuator>();

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);

			rs = ps.executeQuery();

			while (rs.next()) {

				Actuator act = new Actuator();

				act.setId(rs.getInt("id"));
				act.setName(rs.getString("name"));
				act.setIdGreenhouse(rs.getInt("id_greenhouse"));
				act.setType(rs.getString("type"));
				act.setStatus(rs.getBoolean("status"));
				act.setPower(rs.getInt("power"));

				actuators.put(rs.getString("type"), act);
			}

			// System.out.println("ACTUATORS: " + actuators);

			return actuators;
		} catch (SQLException e) {

			return actuators;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	public ArrayList<Sensor> selectGreenhouseSensors(int id) {

		String sql = "SELECT * FROM sensor WHERE id_greenhouse = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		ArrayList<Sensor> sensors = new ArrayList<Sensor>();

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);

			rs = ps.executeQuery();

			while (rs.next()) {

				Sensor sn = new Sensor();

				sn.setId(rs.getInt("id"));
				sn.setName(rs.getString("name"));
				sn.setIdGreenhouse(rs.getInt("id_greenhouse"));
				sn.setType(rs.getString("type"));
				sn.setStatus(rs.getBoolean("status"));
				sn.setValue(rs.getDouble("value"));

				sensors.add(sn);
				;
			}

			for (int i = 0; i < sensors.size(); i++) {

				// System.out.println("SENSORS: " + sensors.get(i).getName());
			}

			return sensors;
		} catch (SQLException e) {

			return sensors;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	public ArrayList<Sensor> selectAllSensors() {

		String sql = "SELECT * FROM sensor";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		ArrayList<Sensor> sensors = new ArrayList<Sensor>();

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);

			rs = ps.executeQuery();

			while (rs.next()) {

				Sensor sn = new Sensor();

				sn.setId(rs.getInt("id"));
				sn.setName(rs.getString("name"));
				sn.setIdGreenhouse(rs.getInt("id_greenhouse"));
				sn.setType(rs.getString("type"));
				sn.setStatus(rs.getBoolean("status"));
				sn.setValue(rs.getInt("value"));

				sensors.add(sn);

			}

			return sensors;
		} catch (SQLException e) {

			return sensors;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	public ArrayList<Actuator> selectAllActuators() {

		String sql = "SELECT * FROM actuator";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		ArrayList<Actuator> actuators = new ArrayList<Actuator>();

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);

			rs = ps.executeQuery();

			while (rs.next()) {

				Actuator act = new Actuator();

				act.setId(rs.getInt("id"));
				act.setName(rs.getString("name"));
				act.setIdGreenhouse(rs.getInt("id_greenhouse"));
				act.setType(rs.getString("type"));
				act.setStatus(rs.getBoolean("status"));
				act.setPower(rs.getInt("power"));

				actuators.add(act);
			}

			for (int i = 0; i < actuators.size(); i++) {

				System.out.println("ACTUATORS: " + actuators.get(i).getName());
			}

			return actuators;
		} catch (SQLException e) {

			return actuators;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	public boolean checkActivePlan(int id_greenhouse) {

		String sql = "SELECT * FROM actuator WHERE active = true AND id_greenhouse = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);
			ps.setInt(1, id_greenhouse);
			rs = ps.executeQuery();

			if (rs != null) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
		return false;
	}

	public void insertSensor(int id_gh, String type, int value, boolean status, String name) {

		String sql = "INSERT INTO sensor (id_greenhouse, type, value, status, name) VALUES (?,?,?,?,?)";

		PreparedStatement ps = null;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);

			// Inserisco il Sensor
			ps = connection.prepareStatement(sql);

			ps.setInt(1, id_gh);
			ps.setString(2, type);
			ps.setInt(3, value);
			ps.setBoolean(4, status);
			ps.setString(5, name);

			System.out.println("Sensor Inserted!");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	// Modifichiamo tutti i campi 'modificabili' es.il tipo non cambia mai così come
	// l'id
	public void updateSensor(int id, int id_gh, double value, boolean status, String name) {

		String sql = "UPDATE sensor SET id_greenhouse=?, value=?, status=?, name=? WHERE id=?";
		PreparedStatement ps = null;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);
			ps = connection.prepareStatement(sql);

			ps.setInt(1, id_gh);
			ps.setDouble(2, value);
			ps.setBoolean(3, status);
			ps.setString(4, name);
			ps.setInt(5, id);

			ps.executeUpdate();
			System.out.println("Sensor Updated!");

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Sensor NOT Updated!");
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	public void insertActuator(int id_gh, String type, int power, boolean status, String name) {

		String sql = "INSERT INTO actuator (id_greenhouse, type, power, status, name) VALUES (?,?,?,?,?)";

		PreparedStatement ps = null;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);

			// Inserisco l'Actuator
			ps = connection.prepareStatement(sql);

			ps.setInt(1, id_gh);
			ps.setString(2, type);
			ps.setInt(3, power);
			ps.setBoolean(4, status);
			ps.setString(5, name);

			System.out.println("Actuator Inserted!");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	// Modifichiamo tutti i campi 'modificabili' es.il tipo non cambia mai
	// così come l'id
	public void updateActuator(int id, int id_gh, int power, boolean status, String name) {

		String sql = "UPDATE actuator SET id_greenhouse=?, power=?, status=?, name=? WHERE id=?";
		PreparedStatement ps = null;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);
			ps = connection.prepareStatement(sql);

			ps.setInt(1, id_gh);
			ps.setInt(2, power);
			ps.setBoolean(3, status);
			ps.setString(4, name);
			ps.setInt(5, id);

			ps.executeUpdate();
			System.out.println("Actuator Updated!");

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Actuator NOT Updated!");
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}

	// Modifichiamo tutti i campi 'modificabili' es.il tipo non cambia mai
	// così come l'id
	public void updateActuatorPower(int power, String type, int idGh) {

		String sql = "UPDATE actuator SET power=?, status=? WHERE type=? AND id_greenhouse=?";
		String sql2 = "UPDATE actuator SET status=? WHERE type=?  AND id_greenhouse=?";
		
		PreparedStatement ps = null;
		Connection connection = null;

		try {
			if (type.equals("temp") || type.equals("hum")) {
				
				System.out.println("TIPO: " + type);
				connection = this.getConnection(connection);
				ps = connection.prepareStatement(sql);

				ps.setInt(1, power);
				ps.setInt(2, 1);
				ps.setString(3, type);
				ps.setInt(4, idGh);
				
				ps.executeUpdate();
				System.out.println("Actuator Updated!");
			} else {
				
				System.out.println("TIPO: " + type);
				connection = this.getConnection(connection);
				ps = connection.prepareStatement(sql2);

				ps.setInt(1, power);
				ps.setString(2, type);
				ps.setInt(3, idGh);
				
				ps.executeUpdate();
				System.out.println("Actuator Updated!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Actuator NOT Updated!");
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
					// System.out.println("Connection closed");
				} catch (SQLException e) {
				}
			}
		}
	}
}