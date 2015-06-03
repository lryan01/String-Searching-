import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JMenuBar;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.json.*;
import javax.json.stream.JsonParsingException;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Iterator;

/**
 * Starting application code for CS 311 Lab 4. Feel free to modify however you
 * wish or replace with your own custom GUI.
 * 
 * @author Scot Morse
 */
public class Main implements Runnable, KeyListener, ListSelectionListener {
	private static HybridTST<String> trie = new HybridTST<String>();

	private static String windowTitle = "Webster's Unabridged Dictionary, 1913 ed.";
	private static int windowWidth = 500;
	private static int windowHeight = 800;
	private JFrame mainWindow;
	private JTextField searchField;
	private JList<String> resultList;
	private DefaultListModel<String> resultListModel;
	private JTextArea definitionArea;
	private JMenuBar menuBar;

	/**
	 * Don't do any Swing GUI stuff here. That needs to be executed from run
	 * which is invoked in the correct event thread from main
	 */
	public Main() {
		
	}

	public static void createTrie(String file) {
		String infile = file;
		JsonReader jsonReader;
		JsonObject jobj = null;
		try {
			jsonReader = Json.createReader(new FileReader(infile));
			// assumes the top level JSON entity is an "Object", i.e. a
			// dictionary
			jobj = jsonReader.readObject();
		} catch (FileNotFoundException e) {
			System.out.println("Could not find the file to read: ");
			e.printStackTrace();
		} catch (JsonParsingException e) {
			System.out
					.println("There is a problem with the JSON syntax; could not parse: ");
			e.printStackTrace();
		} catch (JsonException e) {
			System.out.println("Could not create a JSON object: ");
			e.printStackTrace();
		} catch (IllegalStateException e) {
			System.out
					.println("JSON input was already read or the object was closed: ");
			e.printStackTrace();
		}
		if (jobj == null)
			return;

		System.out.printf("The dictionary file %s has %d entries\n", infile,
				jobj.size());

		Iterator<Map.Entry<String, JsonValue>> it = jobj.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, JsonValue> me = it.next();
			String word = me.getKey().toLowerCase();
			String definition = me.getValue().toString();
			trie.put(word, definition);

		}
	}

	/**
	 * Setup GUI in the correct thread. Required by the Runnable interface
	 */
	public void run() {
		setupUI();
	}

	/**
	 * Setup the user interface
	 */
	private void setupUI() {
		mainWindow = new JFrame(windowTitle);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		menuBar = new JMenuBar();
		mainWindow.setJMenuBar(menuBar);
		JMenu options = new JMenu("Options");
		menuBar.add(options);

		JMenu font = new JMenu("Font Size");
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem sD = new JRadioButtonMenuItem("Default", true);
		JRadioButtonMenuItem s12 = new JRadioButtonMenuItem("12");
		JRadioButtonMenuItem s14 = new JRadioButtonMenuItem("14");
		JRadioButtonMenuItem s16 = new JRadioButtonMenuItem("16");
		group.add(sD);
		group.add(s12);
		group.add(s14);
		group.add(s16);
		font.add(sD);
		font.add(s12);
		font.add(s14);
		font.add(s16);
		options.add(font);

		s12.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				change12();
			}
		});
		s14.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				change14();
			}
		});
		s16.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				change16();
			}
		});

		// The user enters their search query here
		searchField = new JTextField();
		searchField.setBorder(BorderFactory.createTitledBorder("Search"));
		searchField.addKeyListener(this);
		searchField.setFocusTraversalKeysEnabled(false); // to capture VK_TAB or
															// VK_ENTER key
															// press
		// tab can mean complete this word while enter can mean a different kind
		// of search

		// Search results are displayed in a JList
		// (so we can let them click one and figure out which one was selected)
		resultListModel = new DefaultListModel<String>();
		// resultListModel.addElement("No results yet");
		resultList = new JList<String>(resultListModel);
		resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultList.addListSelectionListener(this);
		resultList.setVisibleRowCount(10);
		JScrollPane scrollPane = new JScrollPane(resultList);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Results"));

		// text area to display the definition of a word
		definitionArea = new JTextArea();
		definitionArea.setLineWrap(true);
		definitionArea.setWrapStyleWord(true);
		JScrollPane scrollPane2 = new JScrollPane(definitionArea);
		scrollPane2.setBorder(BorderFactory.createTitledBorder("Definition"));

		// layout
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.add(scrollPane);
		mainPanel.add(scrollPane2);

		Container cp = mainWindow.getContentPane();
		cp.add(searchField, BorderLayout.NORTH);
		cp.add(mainPanel, BorderLayout.CENTER);

		mainWindow.setSize(windowWidth, windowHeight);
		mainWindow.setVisible(true);

	}

	/**
	 * Application entry point.
	 * 
	 * @param args
	 *            This application takes no command line arguments
	 */
	public static void main(String[] args) {
		createTrie(args[0]);
		Main main = new Main();
		// Setup GUI on Swing's event thread
		SwingUtilities.invokeLater(main);
	}

	/**
	 * Method required to fulfill the KeyListener contract.
	 *
	 * @param e
	 *            The event object.
	 */
	public void keyTyped(KeyEvent e) {

	}

	/**
	 * Method required to fulfill the KeyListener contract.
	 * VK_ENTER: searches for an exact match and displays the definition. If no match is found 
	 * apology is displayed and possible alternate matches are displayed.
	 * VK_TAB: completes the word being typed with the first match from keysWithPrefix and 
	 * does not highlight the text field allowing you to continue to add on letters to search as
	 * desired.
	 *
	 * @param e
	 *            The event object.
	 */
	public void keyPressed(KeyEvent e) {
		String currentString = searchField.getText();
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			searchField.selectAll();
			resultListModel.clear();
			for (String i : trie.keysThatMatch(currentString)) {
				resultListModel.addElement(i);
				definitionArea
						.setText(trie.get(resultListModel.firstElement()));
			}
			System.out.println("result size " + resultListModel.size());
			if (resultListModel.isEmpty()) {
				String apology = ("I'm sorry, no results exist for " + "'"
						+ currentString + "'. Possible alternates:");
				resultListModel.addElement(apology);
				String wild = currentString.substring(0,2) + "..";
				System.out.println(wild);
				for (String j : trie.keysThatMatch(wild)) {
					System.out.println("k" + j);
					resultListModel.addElement(j);
				}
				definitionArea
						.setText(trie.get(resultListModel.firstElement()));
			}

		} else if ((e.getKeyCode() == KeyEvent.VK_TAB)) {
			resultListModel.clear();
			for (String i : trie.keysWithPrefix(currentString)) {
				resultListModel.addElement(i);
			}
			searchField.setText(resultListModel.firstElement());
		}

	}

	/**
	 * Allows results to populate the result list as you type
	 *
	 * @param e
	 *            The event object.
	 */
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			return;
		} else if (e.getKeyCode() == KeyEvent.VK_TAB) {
			return;
		}
		resultListModel.clear();
		definitionArea.setText("");
		String currentString = searchField.getText().toLowerCase();
		if (currentString.length() > 2) {
			for (String s : trie.keysWithPrefix(currentString)) {
				resultListModel.addElement(s);
			}
		}
		for (String i : trie.keysThatMatch(currentString)) {
			if (!resultListModel.contains(i)) {
				resultListModel.addElement(i);
			}
		}
	}

	/**
	 * Allows for selection of item in resultList to display the associated definition.
	 */
	public void valueChanged(ListSelectionEvent e) {

		if (e.getValueIsAdjusting() == false) {
			if (resultList.getSelectedIndex() == -1) {

			} else {
				definitionArea.setText(trie.get(resultList.getSelectedValue()));

			}
		}
		searchField.selectAll();
	}

	public void change12() {
		Font font = new Font("Verdana", Font.BOLD, 12);
		searchField.setFont(font);
		definitionArea.setFont(font);
		resultList.setFont(font);
	}

	public void change14() {
		Font font = new Font("Verdana", Font.BOLD, 14);
		searchField.setFont(font);
		definitionArea.setFont(font);
		resultList.setFont(font);
	}

	public void change16() {
		Font font = new Font("Verdana", Font.BOLD, 16);
		searchField.setFont(font);
		definitionArea.setFont(font);
		resultList.setFont(font);
	}

} // end class Main