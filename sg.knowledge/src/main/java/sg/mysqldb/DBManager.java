package sg.mysqldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import sg.actuator.Actuator;
import sg.greenhouse.Greenhouse;
import sg.history.History;
import sg.plan.Plan;
import sg.sensor.Sensor;

public class DBManager {

	final private String port = "localhost:3306/seas";
	final private String user = "root";
	final private String pwd = "root";
	final private String timeZone = TimeZone.getTimeZone("Europe/Rome").getID();
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
		int rows;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sqlInsert);
			ps.setInt(1, id);

			rows = ps.executeUpdate();

			if (rows == 0) {
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
						rs.getInt("opt_temp"), rs.getInt("opt_hum"),  rs.getInt("opt_t_hum"), rs.getInt("range_temp"), 
						rs.getInt("range_hum"), rs.getInt("range_t_hum"));

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
						rs.getInt("opt_temp"), rs.getInt("opt_hum"), rs.getInt("opt_t_hum"), rs.getInt("range_temp"),
						rs.getInt("range_hum"), rs.getInt("range_t_hum"));
				
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

	public HashMap<String,Actuator> selectGreenhouseActuators(int id) {

		String sql = "SELECT * FROM actuator WHERE id_greenhouse = ? AND status = 1";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		HashMap<String,Actuator> actuators = new HashMap<String,Actuator>();

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);

			rs = ps.executeQuery();

			while (rs.next()) {

				actuators.put(rs.getString("type"), new Actuator(rs.getInt("id"), rs.getString("type"), rs.getString("name"),
						rs.getInt("power"), rs.getInt("id_greenhouse")));

				
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

		String sql = "SELECT * FROM actuator WHERE id_greenhouse = ? AND status = 1";
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

				actuators.put(rs.getString("type"), actuators.put(rs.getString("type"), new Actuator(rs.getInt("id"), rs.getString("type"), 
						rs.getString("name"), rs.getInt("power"), rs.getInt("id_greenhouse"))));
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

	public HashMap<String,Sensor> selectGreenhouseSensors(int id) {

		String sql = "SELECT * FROM sensor WHERE status = 1 AND id_greenhouse = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		HashMap<String,Sensor> sensors = new HashMap<String,Sensor>();

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);

			rs = ps.executeQuery();

			while (rs.next()) {

				sensors.put(rs.getString("type"), new Sensor(rs.getInt("id"),  rs.getString("type"), rs.getString("name"),
						rs.getDouble("value"), rs.getInt("id_greenhouse")));
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

		String sql = "SELECT * FROM sensor WHERE status = 1";
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

		String sql = "SELECT * FROM actuator where status = 1";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		ArrayList<Actuator> actuators = new ArrayList<Actuator>();

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);

			rs = ps.executeQuery();

			while (rs.next()) {

				actuators.add(new Actuator(rs.getInt("id"), rs.getString("type"), rs.getString("name"),
						 rs.getInt("power"), rs.getInt("id_greenhouse")));
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

			//System.out.println("Sensor Inserted!");

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
			//System.out.println("Sensor Updated!");

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

			//System.out.println("Actuator Inserted!");

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
			//System.out.println("Actuator Updated!");

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
				
				//System.out.println("TIPO: " + type);
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
				//System.out.println("Actuator Updated!");
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
	
	public Map<String,Plan> selectActivePlans(int id_greenhouse) {

		String sql = "SELECT * FROM plan WHERE active = 1 AND id_greenhouse = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		Map<String,Plan> plans = new HashMap<String,Plan>();;

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);
			ps.setInt(1, id_greenhouse);
			rs = ps.executeQuery();

			while (rs.next()) {
				Calendar start = new GregorianCalendar();
				start.setTime(rs.getTimestamp("start_date"));
				Plan plan = new Plan(rs.getInt("id"), rs.getString("type"), rs.getString("mode"), rs.getBoolean("active"),rs.getDouble("trigger_value"),
						rs.getInt("problem_code"), start, new GregorianCalendar() ,rs.getInt("id_greenhouse"));
				plans.put(plan.getType(),plan);
			}
			
			return plans;

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
		return plans;
	}
	
	
	
	public void insertPlan(int id_gh, String type, String mode, boolean active, double trigger_value, int problem_code, Calendar start_date) {

		String sql = "INSERT INTO plan (type, mode, active, trigger_value, problem_code, start_date ,id_greenhouse) VALUES (?,?,?,?,?,?,?)";

		PreparedStatement ps = null;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);

			// Inserisco il piano
			ps = connection.prepareStatement(sql);

			ps.setString(1, type);
			ps.setString(2, mode);
			ps.setBoolean(3, active);
			ps.setDouble(4, trigger_value);
			ps.setInt(5, problem_code);
			ps.setTimestamp(6, new Timestamp(start_date.getTimeInMillis()));
			ps.setInt(7, id_gh);

			ps.executeUpdate();
			//System.out.println("Plan Inserted!");

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
	
	
	public void deactivatePlan(int id, Calendar end) {

		String sql = "UPDATE plan SET active = 0, end_date = ? WHERE id=?";
		PreparedStatement ps = null;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);
			ps = connection.prepareStatement(sql);

			ps.setTimestamp(1, new Timestamp(end.getTimeInMillis()));
			ps.setInt(2, id);

			ps.executeUpdate();
			//System.out.println("Plan deactivated!");

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Plan NOT deactivated!");
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
	
	public void toDanger(int id) {

		String sql = "UPDATE plan SET type = '3' WHERE id=?";
		PreparedStatement ps = null;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);
			ps = connection.prepareStatement(sql);
			
			
			ps.setInt(1, id);
			

			ps.executeUpdate();

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
	
	public void clearActivePlans() {

		String sql = "UPDATE plan SET active = 0, end_date = '2000-01-01 00:00:00'  WHERE active=1";
		PreparedStatement ps = null;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);
			ps = connection.prepareStatement(sql);
			ps.executeUpdate();

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
	
	
	//history
	
	public void insertHistoryRecords(Map<Integer,History> history_records) {

		String sql = "INSERT INTO history (extTemp, intTemp, extHum, intHum, wind, light, rain, grHum,"
				+ "air_vents,humidifier,conditioner,net,sprinkler, datetime, id_greenhouse) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		PreparedStatement ps = null;
		Connection connection = null;

		try {
			connection = this.getConnection(connection);

			for(Map.Entry<Integer, History> record : history_records.entrySet()){
			// Inserisco il Sensor
				ps = connection.prepareStatement(sql);
	
				ps.setDouble(1, record.getValue().getExtTemp());
				ps.setDouble(2, record.getValue().getIntTemp());
				ps.setDouble(3, record.getValue().getExtHum());
				ps.setDouble(4, record.getValue().getIntHum());
				ps.setDouble(5, record.getValue().getWind());
				ps.setDouble(6, record.getValue().getLight());
				ps.setDouble(7, record.getValue().getRain());
				ps.setDouble(8, record.getValue().getGrHum());
				ps.setInt(9, record.getValue().getAir_vents());
				ps.setInt(10, record.getValue().getHumidifier());
				ps.setInt(11, record.getValue().getConditioner());
				ps.setInt(12, record.getValue().getNet());
				ps.setInt(13, record.getValue().getSprinkler());
				ps.setTimestamp(14, new Timestamp(record.getValue().getDatetime().getTimeInMillis()));
				ps.setInt(15, record.getValue().getId_greenhouse());
	
				ps.executeUpdate();
				
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
	
	
	public Map<Calendar,ArrayList<History>> selectLastMonthHistoryG(int idGh, Calendar datetime) {

		String sql = "SELECT * FROM history WHERE id_greenhouse=? AND datetime > ? AND HOUR(datetime) BETWEEN ? AND ? ORDER BY datetime" ;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		Map<Calendar,ArrayList<History>> lastMonth = new HashMap<Calendar,ArrayList<History>>();

		try {
			connection = this.getConnection(connection);

			ps = connection.prepareStatement(sql);
			datetime.set(Calendar.MONTH,-1);
			ps.setInt(1, idGh);
			ps.setTimestamp(2, new Timestamp(datetime.getTimeInMillis()));
			ps.setInt(3, datetime.get(Calendar.HOUR));
			ps.setInt(4, datetime.get(Calendar.HOUR) + 3);
			rs = ps.executeQuery();

			while (rs.next()) {
				
				Calendar cal = new GregorianCalendar();
				cal.setTime(rs.getTimestamp("datetime"));
				
				Calendar onlyDate = new GregorianCalendar();
				onlyDate.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
				
				lastMonth.putIfAbsent(onlyDate, new ArrayList<History>());
				lastMonth.get(onlyDate).add(new History(rs.getDouble("extTemp"),rs.getDouble("intTemp"),rs.getDouble("extHum"),rs.getDouble("intHum"), 
						rs.getDouble("wind"), rs.getDouble("light"),rs.getDouble("rain"),rs.getDouble("grHum"), rs.getInt("air_vents"), rs.getInt("humidifier"),
						rs.getInt("conditioner"),rs.getInt("net"),rs.getInt("sprinkler"),cal,rs.getInt("id_greenhouse")));
			}

			return lastMonth;
		} catch (SQLException e) {

			return lastMonth;
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