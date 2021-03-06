package com.ems.UI.internalframes;

import static com.ems.constants.MessageConstants.REPORT_KEY_SEPARATOR;
import static com.ems.constants.QueryConstants.SELECT_ENABLED_ENDEVICES;
import static com.ems.util.EMSSwingUtils.centerFrame;
import static com.ems.util.ExcelUtils.createReportHeaderMap;
import static com.ems.util.ExcelUtils.createWorkBook;
import static com.ems.util.ExcelUtils.createWorkSheet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.chart.ExtendedChartPanel;
import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.constants.EmsConstants;
import com.ems.constants.LimitConstants;
import com.ems.constants.MessageConstants;
import com.ems.constants.QueryConstants;
import com.ems.db.DBConnectionManager;
import com.ems.scheduler.SheetWriter;
import com.ems.util.CustomeDateFormatter;
import com.ems.util.EMSSwingUtils;
import com.ems.util.EMSUtility;
import com.ems.util.ExcelUtils;
import com.ems.util.Helper;
import com.ems.util.MyJDateComponentFactory;

public class ReportsIFrame extends JInternalFrame implements ActionListener {
	private static final Logger logger = LoggerFactory.getLogger(ReportsIFrame.class);
	private static final long serialVersionUID = 6606072613952247592L;
	private JDatePickerImpl datePickerStart;
	private JDatePickerImpl datePickerEnd;
	private JComboBox<String> comboBoxDevice;
	private static JComboBox<String> comboRecordCount;
	private List<DeviceDetailsDTO> deviceList;
	private Map<String, DeviceDetailsDTO> deviceMap;
	private JPanel panelChart;

	private JPanel seriesControlPanel;
	private ExtendedChartPanel extendedChart;

	/**
	 * Create the frame.
	 */
	public ReportsIFrame() {
		setIconifiable(true);
		setFrameIcon(new ImageIcon(ReportsIFrame.class.getResource("/com/ems/resources/system_16x16.gif")));
		setTitle("Reports");
		setClosable(true);
		setBackground(EMSSwingUtils.getBackGroundColor());
		setBounds(100, 100, 949, 680);
		centerFrame(getMe());
		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

		addInternalFrameListener(new InternalFrameAdapter() {

			@Override
			public void internalFrameClosing(InternalFrameEvent arg0) {
				int option = JOptionPane.showConfirmDialog(getMe(), "Confirm Close?", "Exit",
						JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					// FIXME : hide or remove any datepicker if opened
					dispose();
				}
			}

			@Override
			public void internalFrameOpened(InternalFrameEvent arg0) {

			}
		});

		getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null)),
				"Report criteria", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setBounds(0, 0, 933, 90);
		panel.setLayout(null);
		getContentPane().add(panel);

		MyJDateComponentFactory factory = new MyJDateComponentFactory();
		Properties props = factory.getI18nStrings(Locale.US);
		Calendar today = Calendar.getInstance();

		UtilDateModel modelStart = new UtilDateModel();
		modelStart.setDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE));
		modelStart.setSelected(true);
		JDatePanelImpl datePanelStart = new JDatePanelImpl(modelStart, props);
		datePickerStart = new JDatePickerImpl(datePanelStart, new CustomeDateFormatter());
		datePickerStart.setToolTipText("Select from data");
		datePickerStart.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		datePickerStart.setBounds(82, 14, 123, 27);
		panel.add(datePickerStart);

		JLabel lblStartDate = new JLabel("Start date");
		lblStartDate.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblStartDate.setHorizontalAlignment(SwingConstants.CENTER);
		lblStartDate.setBounds(10, 14, 60, 23);
		panel.add(lblStartDate);

		JLabel lblNewLabel = new JLabel("End date");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel.setBounds(224, 14, 60, 23);
		panel.add(lblNewLabel);

		UtilDateModel modelEnd = new UtilDateModel();
		modelEnd.setDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE));
		modelEnd.setSelected(true);
		JDatePanelImpl datePanelEnd = new JDatePanelImpl(modelEnd, props);
		datePickerEnd = new JDatePickerImpl(datePanelEnd, new CustomeDateFormatter());
		datePickerEnd.setToolTipText("Select to date");
		datePickerEnd.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		datePickerEnd.setBounds(296, 14, 130, 27);
		panel.add(datePickerEnd);

		JButton btnExportToExcel = new JButton("Export to Excel");
		btnExportToExcel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnExportToExcel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// We set default date as today, so no need to check null
				UtilDateModel startModel = (UtilDateModel) datePickerStart.getModel();
				UtilDateModel endModel = (UtilDateModel) datePickerEnd.getModel();

				int deviceUniqueId = validateDeviceId(comboBoxDevice.getSelectedItem());
				long startDate = Helper.getStartOfDay(startModel.getValue());
				long endDate = Helper.getEndOfDay(endModel.getValue());
				DeviceDetailsDTO detailsDTO = DBConnectionManager.getDeviceById(deviceUniqueId);
				if (detailsDTO == null || detailsDTO.getMemoryMapping() == null
						|| detailsDTO.getMemoryMapping().trim().isEmpty()) {
					JOptionPane.showMessageDialog(getMe(), "No memory mapping details found", "Report",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				logger.info("Excel Report request , device:{},start:{},end:{}", deviceUniqueId, startDate, endDate);

				String fileName = ExcelUtils.prepareUnitData(startDate, endDate, detailsDTO);
				JOptionPane.showMessageDialog(getMe(), "Report created at " + fileName, "Report",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		btnExportToExcel.setBounds(471, 52, 117, 23);
		panel.add(btnExportToExcel);

		JLabel lblDevice = new JLabel("Device");
		lblDevice.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblDevice.setHorizontalAlignment(SwingConstants.CENTER);
		lblDevice.setBounds(463, 14, 60, 23);
		panel.add(lblDevice);

		comboBoxDevice = new JComboBox<String>();
		comboBoxDevice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				/*
				 * Object object = event.getItem(); if (object != null &&
				 * deviceMap != null && deviceMap.size() > 0) { String
				 * selectedDevice = object.toString(); DeviceDetailsDTO details
				 * = deviceMap.get(selectedDevice); details.getMemoryMapping();
				 * JComboBox<String> mappings = getComboBoxMapping();
				 * mappings.removeAllItems(); if (details.getMemoryMapping() !=
				 * null) { String[] regMap = details.getMemoryMapping()
				 * .split(System.lineSeparator());
				 * EMSSwingUtils.addItemsComboBox(mappings, 0, regMap); }
				 * mappings.revalidate(); mappings.repaint(); }
				 */
			}
		});
		comboBoxDevice.setBounds(533, 14, 131, 23);
		panel.add(comboBoxDevice);

		JButton btnViewChart = new JButton("View Chart");
		btnViewChart.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnViewChart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panelChart.removeAll();
				// We set default date as today, so no need to check null
				UtilDateModel startModel = (UtilDateModel) datePickerStart.getModel();
				UtilDateModel endModel = (UtilDateModel) datePickerEnd.getModel();
				int diff = (int) Helper.findDateDiff(startModel.getValue(), endModel.getValue());
				if (diff > LimitConstants.REPORT_DATE_DIFF) {
					JOptionPane.showMessageDialog(getMe(),
							"Please select less than " + LimitConstants.REPORT_DATE_DIFF + " days", "Report",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				int deviceUniqueId = validateDeviceId(comboBoxDevice.getSelectedItem());
				long startDate = Helper.getStartOfDay(startModel.getValue());
				long endDate = Helper.getEndOfDay(endModel.getValue());

				logger.info("Chart Report request , device:{},start:{},end:{}", deviceUniqueId, startDate, endDate);

				DeviceDetailsDTO detailsDTO = DBConnectionManager.getDeviceById(deviceUniqueId);

				if (detailsDTO != null) {
					createChart(deviceUniqueId, startDate, endDate, detailsDTO);
				} else {
					JOptionPane.showMessageDialog(getMe(), "Please try again!", "Report", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		btnViewChart.setBounds(348, 52, 117, 23);
		panel.add(btnViewChart);

		JLabel lblNewLabel_1 = new JLabel("Records per hour");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_1.setBounds(674, 14, 107, 23);
		panel.add(lblNewLabel_1);

		comboRecordCount = new JComboBox<String>();
		comboRecordCount.setModel(new DefaultComboBoxModel<String>(new String[] { "1", "2", "4", "6" }));
		comboRecordCount.setSelectedIndex(1);
		comboRecordCount.setBounds(791, 14, 73, 23);
		panel.add(comboRecordCount);

		panelChart = new JPanel();
		panelChart.setBorder(new TitledBorder(
				new CompoundBorder(new CompoundBorder(), new EtchedBorder(EtchedBorder.LOWERED, null, null)), "Chart",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelChart.setBounds(0, 90, 933, 480);
		getContentPane().add(panelChart);
		panelChart.setLayout(new BorderLayout(0, 0));

		seriesControlPanel = new JPanel();
		seriesControlPanel.setBorder(
				new TitledBorder(new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null)),
						"Report criteria", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		seriesControlPanel.setBounds(0, 575, 933, 80);
		getContentPane().add(seriesControlPanel);

		loadAvailableActiveDevices(comboBoxDevice);

		JButton btnExportAll = new JButton("Export All");
		btnExportAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UtilDateModel startModel = (UtilDateModel) datePickerStart.getModel();
				UtilDateModel endModel = (UtilDateModel) datePickerEnd.getModel();

				List<DeviceDetailsDTO> devices = DBConnectionManager
						.getAvailableDevices(QueryConstants.SELECT_ENABLED_ENDEVICES);

				if (devices == null || devices.size() == 0) {
					JOptionPane.showMessageDialog(getMe(), "No Active device(s) found", "Export Excel",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				long startDate = Helper.getStartOfDay(startModel.getValue());
				long endDate = Helper.getEndOfDay(endModel.getValue());

				HSSFWorkbook workBook = createWorkBook();

				for (DeviceDetailsDTO device : devices) {
					ExtendedSerialParameter param = EMSUtility.mapDeviceToSerialParam(device);
					Map<String, String> headers = createReportHeaderMap(param);
					HSSFSheet sheet = createWorkSheet(workBook, device.getDeviceName(), headers);

					try {
						SheetWriter sheetWriter = new SheetWriter(param, sheet, QueryConstants.RETRIEVE_ALL_DEVICE_STATE, new Object[] {
								param.getUniqueId(), startDate, endDate, param.getUniqueId(), startDate, endDate });
						sheetWriter.call();
					} catch (Exception e1) {
						logger.error("{}", e);
					}
				}

				File reportFile = new File(
						EMSUtility.getFormattedTime(System.currentTimeMillis(), EMSUtility.REPORTNAME_FORMAT) + ".xls");

				try {
					workBook.write(reportFile);
					workBook.close();
				} catch (Exception e1) {
					logger.error("Error writing all record to report {}", e1);
				}

				JOptionPane.showMessageDialog(getMe(), "Report created at " + reportFile.getAbsolutePath(), "Report",
						JOptionPane.INFORMATION_MESSAGE);

			}
		});
		btnExportAll.setBounds(604, 53, 89, 23);
		panel.add(btnExportAll);
	}

	public JPanel getSeriesControlPanel() {
		return seriesControlPanel;
	}

	private void createChart(long deviceUniqueId, long startTime, long endTime, DeviceDetailsDTO device) {
		Connection connection = DBConnectionManager.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		Properties props = new Properties();
		ExtendedSerialParameter serialDevice = EMSUtility.mapDeviceToSerialParam(device);
		Map<String, String> splitJoinRegisterMap = EMSUtility.getOrderedProperties(serialDevice);
		props.putAll(splitJoinRegisterMap);

		// Properties memoryMapping =
		// EMSUtility.loadProperties(device.getMemoryMapping());
		Properties memoryMapping = props;

		// Remove existing content
		JPanel seriesControlPanel = getSeriesControlPanel();
		seriesControlPanel.removeAll();

		int index = 0;
		for (Entry<Object, Object> entry : memoryMapping.entrySet()) {
			// Skip memory mapping record whose value is "NoMap"
			if (!EmsConstants.NO_MAP.equalsIgnoreCase(entry.getValue().toString().trim())) {
				JCheckBox box = new JCheckBox(entry.getValue().toString());
				box.setActionCommand(String.valueOf(index++));
				box.addActionListener(this);
				box.setSelected(true);
				seriesControlPanel.add(box);
			}
		}

		try {

			ps = connection.prepareStatement(QueryConstants.RETRIEVE_DEVICE_STATE4CHART);
			ps.setLong(1, deviceUniqueId);
			ps.setLong(2, startTime);
			ps.setLong(3, endTime);
			rs = ps.executeQuery();
			logger.debug(" Chart query executed ...");
			CategoryDataset dataset = createDataset(rs, memoryMapping);
			logger.info("Category dataset created for chart...");
/*			extendedChart = new ExtendedChartPanel(device.getDeviceName(), dataset);
*/
			panelChart.add(extendedChart);
			panelChart.repaint();
			panelChart.updateUI();
		} catch (Exception e) {
			logger.error("{}", e);
			logger.error("{}", e.getLocalizedMessage());
			JOptionPane.showMessageDialog(getMe(), "Please try again!", "Report", JOptionPane.WARNING_MESSAGE);
		} finally {
			DBConnectionManager.closeConnections(connection, ps, rs);
		}
	}

	// Creates data set and limits number of records to displayed in Chart
	private static CategoryDataset createDataset(ResultSet rs, Properties memoryMapping) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		Map<Long, ArrayList<PollingDetailDTO>> records = new LinkedHashMap<Long, ArrayList<PollingDetailDTO>>();

		try {
			long previousHour = 0;
			long currentHour = 0;
			int recordsPerHour = getRecordsPerHour();

			for (int i = 0; rs.next(); i++) {
				PollingDetailDTO detailDTO = new PollingDetailDTO();
				detailDTO.setFormattedDate(rs.getString("formatteddate"));
				detailDTO.setFormattedHour(rs.getLong("hourformat"));
				detailDTO.setUnitresponse(rs.getString("unitresponse"));
				detailDTO.setDeviceReading(memoryMapping);
				logger.trace("Chart DB record iteration {}", detailDTO);

				if (previousHour == 0)
					previousHour = detailDTO.getFormattedHour();

				currentHour = detailDTO.getFormattedHour();

				if (previousHour != currentHour) {
					addRecordsToDataset(recordsPerHour, previousHour, records, dataset);

					records.clear();
					addTempEntry(currentHour, detailDTO, records);
					previousHour = currentHour;
				} else {
					addTempEntry(currentHour, detailDTO, records);

					if (rs.isLast()) {
						addRecordsToDataset(recordsPerHour, currentHour, records, dataset);
					}
				}
			}

			logger.debug("Report added to view");

		} catch (Exception e) {
			logger.error("{}", e);
			logger.error("Failed creating dataset for chart...");
		}

		return dataset;
	}

	private static void addRecordsToDataset(int recordsPerHour, long currentHour,
			Map<Long, ArrayList<PollingDetailDTO>> records, DefaultCategoryDataset dataset) {
		ArrayList<PollingDetailDTO> previousRecordsPerHour = records.get(currentHour);
		ArrayList<PollingDetailDTO> recordsToDisplay = getHourRecordsToDisplay(previousRecordsPerHour, recordsPerHour);

		for (PollingDetailDTO record : recordsToDisplay) {
			addDatasetEntry(dataset, record);
		}
	}

	private static ArrayList<PollingDetailDTO> getHourRecordsToDisplay(
			ArrayList<PollingDetailDTO> previousRecordsPerHour, int recordsNeeded) {
		ArrayList<PollingDetailDTO> selectedRecords = new ArrayList<>();
		int size = 0;

		if (previousRecordsPerHour == null || (size = previousRecordsPerHour.size()) == 0) {
			return selectedRecords;
		}

		// Simple optimal selection algorithm
		int[] selectedIndexes = Helper.selectOptimalRecords(size, recordsNeeded);
		logger.trace("Optimal selection records : {}", Arrays.toString(selectedIndexes), size, recordsNeeded);
		for (int index = 0; index < selectedIndexes.length; index++) {
			selectedRecords.add(previousRecordsPerHour.get(selectedIndexes[index]));
		}

		return selectedRecords;
	}

	private static Map<Long, ArrayList<PollingDetailDTO>> addTempEntry(long hourKey, PollingDetailDTO detailDTO,
			Map<Long, ArrayList<PollingDetailDTO>> records) {
		ArrayList<PollingDetailDTO> hoursRecords = records.get(hourKey);

		if (hoursRecords == null) {
			hoursRecords = new ArrayList<PollingDetailDTO>();
			records.put(hourKey, hoursRecords);
		}

		hoursRecords.add(detailDTO);
		return records;
	}

	private static void addDatasetEntry(DefaultCategoryDataset dataset, PollingDetailDTO dto) {
		Properties props = EMSUtility.loadProperties(dto.getUnitresponse());
		logger.trace("Unit response : {} Device Reading : {}", dto.getUnitresponse(), dto.getDeviceReading());

		for (Entry<Object, Object> entry : props.entrySet()) {

			String seriesName = dto.getDeviceReading().getProperty(String.valueOf(entry.getKey().toString()));

			logger.trace("Reading: {} Series : {} Time : {}", entry.getValue().toString(), seriesName,
					dto.getFormattedDate());

			// Skip memory mapping record whose value is "NoMap"
			if (seriesName != null && !EmsConstants.NO_MAP.equalsIgnoreCase(seriesName.trim())) {

				try {
					dataset.addValue(Float.parseFloat(entry.getValue().toString())/* Reading */,
							seriesName/* Series name */, dto.getFormattedDate()/* Time */);
				} catch (Exception e) {
					logger.error("{}", e);
				}
			}
		}
	}

	private int validateDeviceId(Object selectedDevice) {
		int deviceId = 0;
		if (selectedDevice == null || selectedDevice.toString().trim().isEmpty()) {
			JOptionPane.showMessageDialog(getMe(), "No device selected", "Report", JOptionPane.WARNING_MESSAGE);
			return deviceId;
		}

		String deviceUniqueId = selectedDevice.toString().split(MessageConstants.REPORT_KEY_SEPARATOR)[0];
		deviceId = Integer.parseInt(deviceUniqueId);

		return deviceId;
	}

	public JDatePickerImpl getDatePickerStart() {
		return datePickerStart;
	}

	public JDatePickerImpl getDatePickerEnd() {
		return datePickerEnd;
	}

	public JComboBox<String> getComboBoxDevice() {
		return comboBoxDevice;
	}

	public JComboBox<String> getComboBoxMapping() {
		return comboRecordCount;
	}

	public void loadAvailableActiveDevices(JComboBox<String> devices) {

		try {
			deviceList = DBConnectionManager.getAvailableDevices(SELECT_ENABLED_ENDEVICES);
			String[] deviceIds = new String[deviceList.size()];
			int index = 0;
			deviceMap = new HashMap<>(deviceList.size());
			for (DeviceDetailsDTO unit : deviceList) {
				StringBuilder builder = new StringBuilder();
				builder.append(unit.getUniqueId());
				builder.append(REPORT_KEY_SEPARATOR);
				builder.append(unit.getDeviceName());
				deviceIds[index++] = builder.toString();
				deviceMap.put(builder.toString(), unit);
			}

			EMSSwingUtils.addItemsComboBox(devices, 0, deviceIds);
			devices.revalidate();
			devices.repaint();
		} catch (Exception e) {
			logger.error("{}", e);
			logger.error("Failed to load active devices , Reports page : {}", e.getLocalizedMessage());
		}
	}

	public JInternalFrame getMe() {
		return this;
	}

	public static int getRecordsPerHour() {
		return Integer.parseInt(comboRecordCount.getSelectedItem().toString());
	}

	@Override
	public void actionPerformed(ActionEvent action) {

		String commad = action.getActionCommand();

		if (commad != null && !commad.trim().isEmpty()) {
			int serieNumber = Integer.parseInt(commad);

			boolean visible = this.extendedChart.getCategoryRenderer().getItemVisible(serieNumber, 0);
			this.extendedChart.getCategoryRenderer().setSeriesVisible(serieNumber, new Boolean(!visible));
		}

	}
}
