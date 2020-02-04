import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//import com.sun.tools.sjavac.server.SysInfo;

public class HomeScreen extends JFrame implements ActionListener, KeyListener, ListSelectionListener, WindowListener {

	private static final long serialVersionUID = 0;

	private JPanel mainPanel;
	private JPanel resultsPanel;
	private SQLHandler sql;

	private JLabel imdbSearchLabel;
	private JLabel searchByLabel;
	private JLabel searchLabel;
	private JLabel resultsLabel;

	private JButton searchButton;
	private JButton clearButton;
	private JButton ratingsButton;

	private JComboBox<String> searchTypeBox;
	private JTextField searchTextField;
	private JList<String> resultsText;
	private JScrollPane scrollPane;

	private JSeparator searchSeparator;
	private JSeparator pageSeparator;

	private int searchType;

	private DefaultListModel<String> listModel;
	private ArrayList<String> graphValues;

	public HomeScreen() {
		System.out.println("Test2");
		
		searchType = 0; // 0 - Name, 1 - ProductionTitle, 2- Genre, 3 - Type
		System.out.println("before");
		sql = SQLHandler.getSQLHandler();
		System.out.println("After");
		
		
		
		setTitle("IMDB Search");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		listModel = new DefaultListModel<String>();
		init();
		setLocationRelativeTo(null);
		setVisible(true);
		graphValues = new ArrayList<String>();
	}

	private void init() {
		System.out.println("Here");
		Container framePane = getContentPane();
		framePane.removeAll();

		mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		setResizable(false);

		JPanel titleRow = new JPanel();
		JPanel searchByRow = new JPanel();
		JPanel searchRow = new JPanel();
		JPanel clearPanel = new JPanel();

		searchByRow.setLayout(new BoxLayout(searchByRow, BoxLayout.X_AXIS));
		searchRow.setLayout(new BoxLayout(searchRow, BoxLayout.X_AXIS));

		imdbSearchLabel = new JLabel("IMDB Search Interface");
		imdbSearchLabel.setBorder(new EmptyBorder(20, 10, 10, 10));
		titleRow.add(imdbSearchLabel);

		searchSeparator = new JSeparator(JSeparator.HORIZONTAL);
		searchSeparator.setPreferredSize(new Dimension(200, 5));

		searchByLabel = new JLabel("Search By: ");
		String[] searchTypeStrings = { "Person Name", "Production Title", "Production Genre", "Production Type" };
		searchTypeBox = new JComboBox<String>(searchTypeStrings);
		searchTypeBox.setPreferredSize(new Dimension(40, 25));
		searchTypeBox.setEnabled(true);
		searchTypeBox.addActionListener(this);
		searchByRow.add(searchByLabel);
		searchByRow.add(searchTypeBox);
		searchByRow.setBorder(new EmptyBorder(20, 20, 20, 20));

		searchLabel = new JLabel("Search: ");
		searchTextField = new JTextField(60);
		searchTextField.addKeyListener(this);
		searchButton = new JButton("Search");
		searchButton.addActionListener(this);
		searchButton.setName("search");

		searchRow.add(searchLabel);
		searchRow.add(searchTextField);
		searchRow.add(searchButton);
		searchRow.setBorder(new EmptyBorder(0, 20, 20, 20));

		mainPanel.add(titleRow);
		mainPanel.add(searchByRow);
		mainPanel.add(searchRow);
		mainPanel.add(searchSeparator);

		resultsPanel = new JPanel();
		resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

		resultsLabel = new JLabel("Results");
		resultsLabel.setBorder(new EmptyBorder(10, 20, 10, 20));

		JPanel resultsLabelPanel = new JPanel();
		resultsLabelPanel.add(resultsLabel);

		resultsPanel.add(resultsLabelPanel);
		mainPanel.add(resultsPanel);

		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

		resultsText = new JList<String>(listModel);
		resultsText.addListSelectionListener(this);
		resultsText.setVisibleRowCount(30);
		resultsText.setFont(new Font("monospaced", Font.PLAIN, 12));

		scrollPane = new JScrollPane(resultsText);
		textPanel.add(scrollPane);
		textPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
		mainPanel.add(textPanel);

		pageSeparator = new JSeparator();
		mainPanel.add(pageSeparator);

		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		clearButton.setName("clear");

		ratingsButton = new JButton("View Ratings");
		ratingsButton.setEnabled(false);
		ratingsButton.setName("ratings");
		ratingsButton.addActionListener(this);

		clearPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

		clearPanel.add(clearButton);
		clearPanel.add(ratingsButton);

		mainPanel.add(clearPanel);

		framePane.add(mainPanel);

		pack();

	}

	// TODO remember to change results label to show # of hits
	public void search(int type, String query) throws SQLException {
		listModel.clear();
		String ret = "";
		ResultSet rs;
		ResultSet rs2;

		switch (type) {
		case 0:
			rs = sql.executeQuery("SELECT primaryName as Name, birthYear as BirthYear, deathYear as DeathYear, personID"
					+ " FROM Person " + "WHERE primaryName LIKE '%" + query + "%' LIMIT 500;", 0);
			sql.next(rs);
			while (!rs.isAfterLast()) {
				// Array to hold titles known for
				ArrayList<String> knownFor = new ArrayList<String>();
				// '1' as second param indicates alternate statement will be used
				rs2 = sql.executeQuery(
						"SELECT primaryTitle FROM CastAndCrew JOIN Production ON Production.prodID = CastAndCrew.prodID"
								+ " WHERE personID = '" + rs.getString(4) + "' LIMIT 10;",
						1);
				rs2.next();
				while (!rs2.isAfterLast()) {
					// tries to add to knownfor, if it fails, ignore the knownfor titles (none to
					// begin with)
					try {
						if (rs2.getString(1).indexOf("Episode") < 0) {
							knownFor.add(rs2.getString(1));
						}
					} catch (SQLException e) {
						// if it fails, quit the loop and continue
						break;
					}
					rs2.next();
				}

				ret = "<html>";
				ret += "Name: " + rs.getString(1) + "<br>";
				if (rs.getString(2) == null)
					ret += "Birth Year: N/A<br>";
				else
					ret += "Birth Year: " + rs.getString(2) + "<br>";
				if (rs.getString(2) == null)
					ret += "Death Year: N/A<br>";
				else
					ret += "Death Year: " + rs.getString(3) + "<br>";
				if (knownFor.size() == 0) {
					ret += "Known For: N/A<br><br>";
				} else {
					ret += "Known For: ";
					for (int i = 0; i < knownFor.size(); i++) {
						// If last element
						if (i + 1 == knownFor.size()) {
							ret += (knownFor.get(i) + "<br><br>");
						} else {
							ret += (knownFor.get(i) + ", ");
						}
					}
				}
				listModel.addElement(ret);
				sql.next(rs);
			}

			break;
		case 1:
			rs = sql.executeQuery("SELECT primaryTitle as Title, originalTitle as OriginalTitle, typeID as Type,"
					+ " isAdult as Adult, runTime as Duration, startYear as StartYear, endYear as EndYear, prodID FROM Production"
					+ " WHERE primaryTitle LIKE '%" + query + "%' LIMIT 500;", 0);

			sql.next(rs);
			resultsLabel.setText("Results (" + sql.getNumRows(rs) + " found)");
			while (!rs.isAfterLast()) {
				// Array to hold titles known for
				ArrayList<String> genres = new ArrayList<String>();
				// '1' as second param indicates alternate statement will be used
				rs2 = sql.executeQuery(
						"SELECT genreID FROM ProductionGenre JOIN Production ON Production.prodID = ProductionGenre.prodID"
								+ " WHERE Production.prodID = '" + rs.getString(8) + "' LIMIT 10;",
						1);
				rs2.next();
				while (!rs2.isAfterLast()) {
					// tries to add to knownfor, if it fails, ignore the knownfor titles (none to
					// begin with)
					try {
						genres.add(rs2.getString(1));
					} catch (SQLException e) {
						// if it fails, quit the loop and continue
						break;
					}
					rs2.next();
				}
				ret = "<html>";
				ret += "Primary Title: " + rs.getString(1) + "<br>";
				ret += "Original Title: " + rs.getString(2) + "<br>";
				ret += "Type: " + rs.getString(3) + "<br>";
				if (genres.size() == 0) {
					ret += "Genres: N/A<br>";
				} else {
					ret += "Genres: ";
					for (int i = 0; i < genres.size(); i++) {
						// If last element
						if (i + 1 == genres.size()) {
							ret += (genres.get(i) + "<br>");
						} else {
							ret += (genres.get(i) + ", ");
						}
					}
				}
				if (rs.getString(4).equals("0"))
					ret += "Adult: No<br>";
				else
					ret += "Adult: Yes<br>";
				if (rs.getString(5) == null)
					ret += "Duration: N/A<br>";
				else
					ret += "Duration: " + rs.getString(5) + " minutes<br>";
				ret += "Start Year: " + rs.getString(6) + "<br>";
				if (rs.getString(7) == null)
					ret += "End Year: N/A<br><br> </html>";
				else
					ret += "End Year: " + rs.getString(7) + "<br><br> </html>";
				listModel.addElement(ret);
				sql.next(rs);
			}
			break;

		case 2:
			rs = sql.executeQuery(
					"SELECT primaryTitle as Title, originalTitle as OriginalTitle, typeID as Type, genreID as Genre,"
							+ " isAdult as Adult, runTime as Duration, startYear as StartYear, endYear as EndYear FROM Production"
							+ " JOIN ProductionGenre ON Production.prodID = ProductionGenre.prodID WHERE genreID LIKE '%"
							+ query + "%' LIMIT 500;",
					0);

			sql.next(rs);
			resultsLabel.setText("Results (" + sql.getNumRows(rs) + " found)");
			while (!rs.isAfterLast()) {
				ret = "<html>";
				ret += "Primary Title: " + rs.getString(1) + "<br>";
				ret += "Original Title: " + rs.getString(2) + "<br>";
				ret += "Type: " + rs.getString(3) + "<br>";
				ret += "Genre: " + rs.getString(4) + "<br>";
				if (rs.getString(5).equals("0"))
					ret += "Adult: No<br>";
				else
					ret += "Adult: Yes<br>";
				if (rs.getString(6) == null)
					ret += "Duration: N/A<br>";
				else
					ret += "Duration: " + rs.getString(6) + " minutes<br>";
				ret += "Start Year: " + rs.getString(7) + "<br>";
				if (rs.getString(8) == null)
					ret += "End Year: N/A<br><br> </html>";
				else
					ret += "End Year: " + rs.getString(8) + "<br><br> </html>";
				listModel.addElement(ret);
				sql.next(rs);
			}
			break;
		case 3:
			rs = sql.executeQuery(
					"SELECT primaryTitle as Title, originalTitle as OriginalTitle, typeID as Type, genreID as Genre,"
							+ " isAdult as Adult, runTime as Duration, startYear as StartYear, endYear as EndYear FROM Production"
							+ " JOIN ProductionGenre ON Production.prodID = ProductionGenre.prodID WHERE typeID LIKE '%"
							+ query + "%' LIMIT 500;",
					0);

			sql.next(rs);
			resultsLabel.setText("Results (" + sql.getNumRows(rs) + " found)");
			while (!rs.isAfterLast()) {
				ret = "<html>";
				ret += "Primary Title: " + rs.getString(1) + "<br>";
				ret += "Original Title: " + rs.getString(2) + "<br>";
				ret += "Type: " + rs.getString(3) + "<br>";
				ret += "Genre: " + rs.getString(4) + "<br>";
				if (rs.getString(5).equals("0"))
					ret += "Adult: No<br>";
				else
					ret += "Adult: Yes<br>";
				if (rs.getString(6) == null)
					ret += "Duration: N/A<br>";
				else
					ret += "Duration: " + rs.getString(6) + " minutes<br>";
				ret += "Start Year: " + rs.getString(7) + "<br>";
				if (rs.getString(8) == null)
					ret += "End Year: N/A<br><br> </html>";
				else
					ret += "End Year: " + rs.getString(8) + "<br><br> </html>";
				listModel.addElement(ret);
				sql.next(rs);
			}
			break;
		}

	}

	public void createBarGraph(String title, String str) throws NumberFormatException, SQLException {
		graphValues.add(str);

		JFrame f = new JFrame();
		f.setSize(400, 300);
		f.setLocationByPlatform(true);
		double[] values = new double[2];
		String[] names = new String[2];
		values[0] = Double.parseDouble(str);
		System.out.println(str); // printing value of selected rating
		names[0] = title + "(" + values[0] + ")";

		ResultSet rs;

		rs = sql.executeQuery("SELECT AVG(averageRating) " + "FROM Ratings LIMIT 500;", 0);
		rs.next();

		values[1] = Double.parseDouble(rs.getString(1)); // Needs to be a query for the Avg rating
		names[1] = "Average Rating (" + values[1] + ")";

		f.getContentPane().add(new ChartPanel(values, names, "Ratings"));

		f.addWindowListener(this);
		f.setVisible(true);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if (src instanceof JComboBox) {
			searchType = ((JComboBox) src).getSelectedIndex();
		}

		if (e.getSource() == ratingsButton) {
			ResultSet rs;
			String[] fields = resultsText.getSelectedValue().split("\\n");

			int brLoc = fields[0].indexOf("<br>");
			System.out.println(fields[0].substring(21, brLoc));
			rs = sql.executeQuery(
					"SELECT primaryTitle, averageRating FROM Ratings JOIN Production ON Ratings.prodID = Production.prodID WHERE Production.primaryTitle = '"
							+ fields[0].substring(21, brLoc) + "';",
					0);

			try {
				rs.next();
				createBarGraph(rs.getString(1), rs.getString(2));
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		else if (src instanceof JButton) {
			if (((JButton) src).getName().equals("clear")) {

				listModel.removeAllElements();
				ratingsButton.setEnabled(false);
				searchTextField.setText("");
				resultsLabel.setText("Results");
			} else if (((JButton) src).getName().equals("search")) {
				try {

					search(searchType, searchTextField.getText());
				} catch (SQLException ex) {
					resultsLabel.setText("An error occured while conducting your search.");
				}
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {

		if (e.getKeyCode() == (KeyEvent.VK_ENTER) && (!searchTextField.getText().isEmpty())) {
			try {
				search(searchType, searchTextField.getText());
			} catch (SQLException e1) {

				e1.printStackTrace();
			}
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() && searchType > 0) {
			ratingsButton.setEnabled(true);
		}

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

}