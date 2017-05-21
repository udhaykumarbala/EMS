package com.ems.util;

import static com.ems.constants.LimitConstants.DASHBOARD_DEVICES_COUNT;
import static com.ems.constants.LimitConstants.DASHBOARD_REFRESH_FREQUENCY;
import static com.ems.constants.LimitConstants.DEFAULT_COMPANY_NAME;
import static com.ems.constants.LimitConstants.DEFAULT_COMPORT;
import static com.ems.constants.LimitConstants.DEFAULT_NUMBER_OF_DEVICES;
import static com.ems.constants.MessageConstants.COMPANYNAME_KEY;
import static com.ems.constants.MessageConstants.DASHBOARD_DEVICESCOUNT_KEY;
import static com.ems.constants.MessageConstants.DASHBOARD_DEVICES_KEY;
import static com.ems.constants.MessageConstants.DASHBOARD_REFRESHFREQUENCY_KEY;
import static com.ems.constants.MessageConstants.DEFAULTPORT_KEY;
import static com.ems.constants.MessageConstants.PASSWORD_KEY;
import static com.ems.constants.MessageConstants.USERNAME_KEY;
import static com.ems.constants.MessageConstants.NUMBER_OF_DEVICES_KEY;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.constants.MessageConstants;
import com.ems.tmp.datamngr.TempDataManager;

public abstract class Helper {
	private static final Logger logger = LoggerFactory
			.getLogger(Helper.class);
	private static final int PWD_LENGTH = 5;
	private static final String[] SEEDS = { "a", "b", "c", "d", "e", "f", "g",
		"h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
		"u", "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7",
		"8", "9", "0", "!", "@", "#", "$", "*","A", "B", "C", "D", "E", "F", "G",
		"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
		"U", "V", "W", "X", "Y", "Z" };

	public static long findDateDiff(Date startDate, Date endDate){
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.set(startDate.getYear(), startDate.getMonth(), startDate.getDate());
		calendar2.set(endDate.getYear(), endDate.getMonth(), endDate.getDate());

		long miliSecondForDate1 = calendar1.getTimeInMillis();
		long miliSecondForDate2 = calendar2.getTimeInMillis();
		long diffInMilis = miliSecondForDate2 - miliSecondForDate1;

		long diffInDays = (diffInMilis / (24 * 60 * 60 * 1000));
		return diffInDays;
	}

	public static long getEndOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime().getTime();
	}

	public static long getStartOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime().getTime();
	}

	public static String generateRandomPassword(){
		StringBuilder builder = new StringBuilder();
		Random random = new Random();
		for(int i = 1 ; i <= PWD_LENGTH ; i++){
			builder.append(SEEDS[random.nextInt(SEEDS.length)]);
		}
		logger.info("AuToGeNeRaTeDHIDDen{}{}{}","X",hidePassword(builder.toString()),"Z");
		return builder.toString();
	}

	public static Properties getInitialMainConfig(){
		Properties mainConfig = new Properties();
		mainConfig.put(USERNAME_KEY, "admin");
		mainConfig.put(PASSWORD_KEY, generateRandomPassword());
		mainConfig.put(DASHBOARD_DEVICESCOUNT_KEY, String.valueOf(DASHBOARD_DEVICES_COUNT));
		mainConfig.put(DASHBOARD_REFRESHFREQUENCY_KEY, String.valueOf(DASHBOARD_REFRESH_FREQUENCY));
		mainConfig.put(DASHBOARD_DEVICES_KEY,"");
		mainConfig.put(COMPANYNAME_KEY,DEFAULT_COMPANY_NAME);
		mainConfig.put(DEFAULTPORT_KEY,DEFAULT_COMPORT);
		mainConfig.put(NUMBER_OF_DEVICES_KEY,String.valueOf(DEFAULT_NUMBER_OF_DEVICES));
		logger.info("Initiatal setup is completed...");
		return mainConfig;
	}

	public static boolean checkNullEmpty(String value){
		return value == null || value.isEmpty();
	}

	public static String hidePassword(String hiddeMe){
		StringBuilder builder = new StringBuilder(hiddeMe);
		int length = builder.length();
		Random random = new Random();
		for(int i = 0 ; i < length * 2 ; i = i+2){
			builder.insert(i,SEEDS[random.nextInt(SEEDS.length)]);
		}
		return builder.toString();
	}

	/**
	 * Algorithm selects 'x' records from 'n' size array
	 */
	public static int[] selectOptimalRecords(int listSize, int requiredRecordCount){
		int n = listSize;
		int x = requiredRecordCount;
		int[] selected = null;

		if(n <= x || n == 0 || x == 0){
			int loop = n < x ? n : x;
			selected = new int[loop];
			for(int i = 0; i < loop; i++)
				selected[i] = i;
			return selected;
		}

		selected = new int[x];
		int diff = n / x;

		for(int i = 0; i < x; i++)
			selected[(x - i) - 1] = n - (i * diff) - 1; 

		return selected;
	}
	
	public static void main(String[] args) {
		/*Properties prop = getInitialMainConfig();
		prop.put(PASSWORD_KEY, "admin");
		prop.list(System.out);
		TempDataManager.writeTempConfig(prop, TempDataManager.MAIN_CONFIG);*/
		
		/*Properties props = TempDataManager.retrieveTempConfig(TempDataManager.MAIN_CONFIG);
		props.list(System.out);
		String[] a = props.getProperty(MessageConstants.DASHBOARD_DEVICES_KEY).split(";");
		List<String> list = Arrays.asList(a);
		System.out.println(Arrays.toString(a));
		System.out.println(list.contains("100"));*/
	}
}
