package com.ems.util;

import static com.ems.UI.internalframes.IFrameMapping.getCanonicalHashCode;
import static com.ems.constants.EmsConstants.RETRYCOUNT;
import static com.ems.constants.EmsConstants.TIMEOUT;
import static com.ems.util.EMSUtility.processRegistersForDashBoard;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.GroupDTO;
import com.ems.UI.dto.GroupsDTO;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.UI.internalframes.PingInternalFrame;
import com.ems.constants.EmsConstants;
import com.ems.db.DBConnectionManager;

public abstract class EMSSwingUtils {

	private static final Logger logger = LoggerFactory.getLogger(EMSSwingUtils.class);

	/**
	 * @param combo
	 * @param arg
	 * 
	 *            Adds item to Combobox
	 */
	public static void addItemsComboBox(JComboBox<String> combo, int selectedIndex, String... arg) {
		if (arg != null && combo != null) {
			for (String port : arg) {
				combo.addItem(port);
			}
			if (arg.length == 0)
				selectedIndex = -1;

			combo.setSelectedIndex(selectedIndex);
		}
	}

	/**
	 * @param combo
	 * @param arg
	 * 
	 *            Adds item to Combobox
	 */
	public static void addItemsComboBox(JComboBox<String> combo, int selectedIndex, int... arg) {
		if (arg != null & combo != null) {
			for (int item : arg) {
				combo.addItem(String.valueOf(item));
			}
			combo.setSelectedIndex(selectedIndex);
		}
	}

	/**
	 * Create SerialParameters from PingInternalFrame
	 */
	public static ExtendedSerialParameter getSerialParameters(JInternalFrame iFrame) {
		ExtendedSerialParameter params = new ExtendedSerialParameter();

		if (iFrame != null && iFrame instanceof PingInternalFrame) {
			PingInternalFrame pingFrame = (PingInternalFrame) iFrame;
			// Connection config
			params.setPortName(pingFrame.getComboBoxPorts());
			params.setBaudRate(pingFrame.getComboBoxBaudRate());
			params.setFlowControlIn(0);// TODO : Config later
			params.setFlowControlOut(0);// TODO : Config later
			params.setDatabits(pingFrame.getComboBoxWordLength());
			params.setStopbits(pingFrame.getComboBoxStopBit());
			params.setParity(pingFrame.getComboBoxParity());
			params.setEcho(false);
			params.setEncoding(pingFrame.getComboBoxEncoding());

			// Unit and its config
			params.setRetries(RETRYCOUNT);
			params.setUnitId(pingFrame.getTxtFieldDeviceId());
			params.setReference(pingFrame.getTxtFieldAddress());// Address is
																// mapped
			params.setCount(pingFrame.getSpinnerLength());// Total number of
															// registers to be
															// read
			params.setPointType(3);
			params.setTimeout(TIMEOUT[1]);
			params.setPollDelay(pingFrame.getTxtFieldPollDelay());
		}

		return params;
	}

	public static KeyAdapter getNumericListener() {
		return new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char vChar = e.getKeyChar();
				if (!(Character.isDigit(vChar) || (vChar == KeyEvent.VK_BACK_SPACE) || (vChar == KeyEvent.VK_DELETE))) {
					e.consume();
				}
			}
		};
	}

	public static void openSingletonIFrame(JDesktopPane desktopPane, Class className) {

		if (desktopPane != null) {

			JInternalFrame[] avlFrames = desktopPane.getAllFrames();
			boolean exit = false;

			if (avlFrames != null && avlFrames.length != 0) {
				for (JInternalFrame iFrame : avlFrames) {

					if (getCanonicalHashCode(iFrame) == getCanonicalHashCode(className)) {
						exit = true;
						try {
							Rectangle bound = iFrame.getBounds();
							desktopPane.getDesktopManager().maximizeFrame(iFrame);
							iFrame.setBounds(bound);
							iFrame.setSelected(true);
							iFrame.setVisible(true);
						} catch (PropertyVetoException e) {
							logger.error("Failed to setSelect true : {} ", e.getLocalizedMessage());
							logger.error("{}", e);
						}
					}
				}
			}

			if (!exit) {
				JInternalFrame frame = null;
				try {
					frame = (JInternalFrame) Class.forName(className.getCanonicalName()).newInstance();
					desktopPane.add(frame);
					frame.setVisible(true);
					frame.setSelected(true);
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException
						| PropertyVetoException e1) {
					logger.error("Failed to create IFrame instance : {}", className.getCanonicalName());
					e1.printStackTrace();
				}
			}
		}
	}

	public static void centerFrame(JInternalFrame frame) {

		if (frame == null)
			return;

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		int x = (int) ((dim.getHeight() / 2) - (frame.getHeight() / 2)) - 50;
		int y = (int) ((dim.getWidth() / 2) - (frame.getWidth() / 2));

		frame.setBounds(y, x, frame.getWidth(), frame.getHeight());
	}

	public static Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	public static void setMaximizedSize(JInternalFrame frame) {
		Dimension dim = EMSSwingUtils.getScreenSize();
		frame.setBounds(0, 0, (int) dim.getWidth() - 20, (int) dim.getHeight() - 80);
	}

	public static JLabel getDeviceDetailLabel(ExtendedSerialParameter device) {
		StringBuilder builder = new StringBuilder();
		builder.append(
				"<html><head><style>body{background-color:#BBD2F7;}");
		builder.append(
				"table#table{width:100%;margin-left:20%;margin-right:15%;}tr{color: black;font-family: Times New Roman, Georgia, Serif; font-size: 16pt;}</style></head><body>");
		builder.append("<div><center><table id='table'>");

		Map<String, String> registerValue = null;

		// Always load from DB only
		// Load recent polling response from DB for the first time;
		List<PollingDetailDTO> list = DBConnectionManager.fetchRecentPollingDetails(device.getUniqueId());
		if (list.size() > 0) {
			Properties props = EMSUtility.loadProperties(list.get(0).getUnitresponse());
			registerValue = EMSUtility.convertProp2Map(props);
		}

		Map<String, String> mappings = EMSUtility.getOrderedProperties(device);

		logger.trace("Processed register : {} Mapping : {}", registerValue, mappings);

		for (Entry<String, String> memory : mappings.entrySet()) {
			// Skip memory mapping record whose value is "NoMap"
			if (!EmsConstants.NO_MAP.equalsIgnoreCase(memory.getValue().trim())) {
				builder.append("<tr><td align='center'>");
				builder.append(memory.getValue());
				String value = null;

				try {
					String lookup = String.valueOf(Integer.valueOf(memory.getKey()));
					value = registerValue.get(lookup);
					logger.trace("looked up values : {}", value);
				} catch (Exception e) {
					logger.error("{}", e);
					value = "0.00";
				}

				builder.append("</td><td align='center'>" + (value == null ? "0.00" : value) + "</td></tr>");
			}
		}
		builder.append("</table></center></div></body></html>");
		logger.trace("prepared device label : {}", builder.toString());
		JLabel label = new JLabel(builder.toString());
		label.setHorizontalAlignment(SwingConstants.CENTER);
		return label;
	}

	public static String getAvlConfigureDevicesString() {
		int configured = DBConnectionManager.getConfiguredDeviceCount();
		int total = ConfigHelper.getDefaultDevices();
		StringBuilder builder = new StringBuilder();
		builder.append(configured);
		builder.append("/");
		builder.append(total);
		builder.append(" Device Configured");

		return builder.toString();
	}

	public static GroupsDTO getAllGroupNodes(JTree tree) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

		GroupsDTO groups = (GroupsDTO) root.getUserObject();
		groups.setGroups(new ArrayList<GroupDTO>());

		int childCount = root.getChildCount();
		for (int i = 0; i < childCount; i++) {
			DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) root.getChildAt(i);
			GroupDTO group = (GroupDTO) groupNode.getUserObject();
			group.setDevices(new ArrayList<DeviceDetailsDTO>());
			groups.getGroups().add(group);

			int leafCount = groupNode.getChildCount();
			for (int j = 0; j < leafCount; j++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) groupNode.getChildAt(j);
				DeviceDetailsDTO device = (DeviceDetailsDTO) child.getUserObject();
				group.getDevices().add(device);
			}
		}

		return groups;
	}

	public static void addTrayIcon(String toolTip) {
		if (!SystemTray.isSupported()) {
			logger.debug("SystemTray is not supported");
			return;
		}

		try {
			SystemTray tray = SystemTray.getSystemTray();
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			URL url = EMSSwingUtils.class.getResource("/com/ems/resources/gnome-monitor.png");
			Image image = toolkit.getImage(url);
			TrayIcon icon = new TrayIcon(image, toolTip);
			icon.setImageAutoSize(true);
			tray.add(icon);
		} catch (AWTException e) {
			logger.error("{}", e);
		}
	}

	public static Color getBackGroundColor() {
		return new Color(187, 210, 247);
	}
}
